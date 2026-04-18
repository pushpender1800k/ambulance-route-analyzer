package com.aris.service;

import com.aris.dto.RouteResponse;
import com.aris.model.Ambulance;
import com.aris.model.AmbulanceStatus;
import com.aris.model.Dispatch;
import com.aris.model.Hospital;
import com.aris.model.Incident;
import com.aris.model.IncidentStatus;
import com.aris.repository.AmbulanceRepository;
import com.aris.repository.DispatchRepository;
import com.aris.repository.HospitalRepository;
import com.aris.repository.IncidentRepository;
import com.aris.websocket.EventBroadcaster;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SimulationService {

    private final AmbulanceRepository ambulanceRepository;
    private final DispatchRepository dispatchRepository;
    private final HospitalRepository hospitalRepository;
    private final IncidentRepository incidentRepository;
    private final RoutingService routingService;
    private final EventBroadcaster eventBroadcaster;

    // Route waypoint cache per ambulance
    private final Map<Long, List<double[]>> routeCache = new ConcurrentHashMap<>();
    // Current waypoint index per ambulance
    private final Map<Long, Integer> waypointIndex = new ConcurrentHashMap<>();
    // Track which phase the route was cached for
    private final Map<Long, IncidentStatus> routePhase = new ConcurrentHashMap<>();
    // Steps per tick (calculated from ETA) per ambulance
    private final Map<Long, Integer> stepsPerTick = new ConcurrentHashMap<>();

    // Tick interval in seconds
    private static final double TICK_INTERVAL_SEC = 2.0;
    // Speed multiplier: 10x means 9 min ETA ≈ 54 seconds real time
    private static final double SPEED_MULTIPLIER = 10.0;

    public SimulationService(AmbulanceRepository ambulanceRepository,
                             DispatchRepository dispatchRepository,
                             HospitalRepository hospitalRepository,
                             IncidentRepository incidentRepository,
                             RoutingService routingService,
                             EventBroadcaster eventBroadcaster) {
        this.ambulanceRepository = ambulanceRepository;
        this.dispatchRepository = dispatchRepository;
        this.hospitalRepository = hospitalRepository;
        this.incidentRepository = incidentRepository;
        this.routingService = routingService;
        this.eventBroadcaster = eventBroadcaster;
    }

    /**
     * Every 2 seconds, move TRANSIT ambulances along their OSRM road route.
     *
     * Speed is calculated from the OSRM ETA so ambulances arrive approximately
     * on time (accelerated by SPEED_MULTIPLIER for simulation).
     *
     * Phase tracking via incident status:
     *   ASSIGNED       → heading TO incident scene
     *   IN_PROGRESS    → picked up patient, heading TO hospital
     *   RESOLVED       → patient delivered, back to STANDBY
     */
    @Scheduled(fixedRate = 2000)
    public void moveTransitAmbulances() {
        List<Ambulance> transitUnits = ambulanceRepository.findByStatus(AmbulanceStatus.TRANSIT_TO_PATIENT);

        for (Ambulance amb : transitUnits) {
            try {
                List<Dispatch> dispatches = dispatchRepository.findByAmbulanceId(amb.getId());
                if (dispatches.isEmpty()) continue;

                Dispatch latest = dispatches.get(dispatches.size() - 1);
                Optional<Incident> incOpt = incidentRepository.findById(latest.getIncidentId());
                Optional<Hospital> hospOpt = hospitalRepository.findById(latest.getHospitalId());

                if (incOpt.isEmpty()) continue;

                Incident incident = incOpt.get();
                IncidentStatus phase = incident.getStatus();

                if (phase == IncidentStatus.EN_ROUTE_PATIENT) {
                    ensureRouteLoaded(amb.getId(), phase,
                            amb.getLat(), amb.getLng(),
                            incident.getLat(), incident.getLng());

                    boolean arrived = advanceAlongRoute(amb);

                    Long patientId = incident.getPatientUserId();

                    if (arrived) {
                        amb.setLat(incident.getLat());
                        amb.setLng(incident.getLng());
                        
                        // Transition to Hospital phase
                        if (hospOpt.isPresent()) {
                            Hospital h = hospOpt.get();
                            amb.setStatus(AmbulanceStatus.TRANSIT_TO_HOSPITAL);
                            amb.setDestinationLat(h.getLat());
                            amb.setDestinationLng(h.getLng());
                        } else {
                            amb.setStatus(AmbulanceStatus.ON_SCENE);
                        }
                        ambulanceRepository.save(amb);

                        incident.setStatus(IncidentStatus.PICKED_UP);
                        incident.setPickedUpAt(java.time.LocalDateTime.now());
                        incidentRepository.save(incident);
                        clearRoute(amb.getId());

                        eventBroadcaster.broadcast("SYSTEM",
                                String.format("📍 Unit %s reached incident scene — loading patient",
                                        amb.getUnitCode()), "INFO");

                        eventBroadcaster.broadcastIncidentUpdate(
                                incident.getId(), "PICKED_UP",
                                amb.getId(), amb.getUnitCode(),
                                latest.getHospitalId(), hospOpt.map(Hospital::getName).orElse(null),
                                0
                        );
                    } else {
                        int remainingWaypoints = routeCache.get(amb.getId()) != null ?
                                routeCache.get(amb.getId()).size() - waypointIndex.getOrDefault(amb.getId(), 0) : 0;
                        int eta = Math.max(1, (remainingWaypoints * stepsPerTick.getOrDefault(amb.getId(), 1)) / 30);
                        eventBroadcaster.broadcastAmbulanceLocation(
                                amb.getId(), amb.getLat(), amb.getLng(),
                                amb.getStatus().name(), amb.getUnitCode(),
                                eta, patientId
                        );
                    }
                }
            } catch (Exception e) {
                System.err.println("Error moving ambulance " + amb.getUnitCode() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        List<Ambulance> toHospitalUnits = ambulanceRepository.findByStatus(AmbulanceStatus.TRANSIT_TO_HOSPITAL);
        for (Ambulance amb : toHospitalUnits) {
            try {
                List<Dispatch> dispatches = dispatchRepository.findByAmbulanceId(amb.getId());
                if (dispatches.isEmpty()) continue;

                Dispatch latest = dispatches.get(dispatches.size() - 1);
                Optional<Hospital> hospOpt = hospitalRepository.findById(latest.getHospitalId());

                if (hospOpt.isEmpty()) continue;
                Hospital hospital = hospOpt.get();

                Incident incident = amb.getCurrentIncident();
                Long patientId = incident != null ? incident.getPatientUserId() : null;

                ensureRouteLoaded(amb.getId(), IncidentStatus.EN_ROUTE_HOSPITAL,
                        amb.getLat(), amb.getLng(),
                        hospital.getLat(), hospital.getLng());

                boolean arrived = advanceAlongRoute(amb);

                if (arrived) {
                    amb.setLat(hospital.getLat());
                    amb.setLng(hospital.getLng());
                    amb.setStatus(AmbulanceStatus.AT_HOSPITAL);
                    ambulanceRepository.save(amb);

                    if (incident != null) {
                        incident.setStatus(IncidentStatus.ARRIVED_HOSPITAL);
                        incident.setArrivedHospitalAt(java.time.LocalDateTime.now());
                        incidentRepository.save(incident);

                        eventBroadcaster.broadcastIncidentUpdate(
                                incident.getId(), "ARRIVED_HOSPITAL",
                                amb.getId(), amb.getUnitCode(),
                                hospital.getId(), hospital.getName(),
                                0
                        );
                    }
                    clearRoute(amb.getId());

                    eventBroadcaster.broadcast("SYSTEM",
                            String.format("✅ Unit %s arrived at %s — patient delivered",
                                    amb.getUnitCode(), hospital.getName()), "INFO");
                } else {
                    int remainingWaypoints = routeCache.get(amb.getId()) != null ?
                            routeCache.get(amb.getId()).size() - waypointIndex.getOrDefault(amb.getId(), 0) : 0;
                    int eta = Math.max(1, (remainingWaypoints * stepsPerTick.getOrDefault(amb.getId(), 1)) / 30);
                    eventBroadcaster.broadcastAmbulanceLocation(
                            amb.getId(), amb.getLat(), amb.getLng(),
                            amb.getStatus().name(), amb.getUnitCode(),
                            eta, patientId
                    );
                }
            } catch (Exception e) {
                System.err.println("Error moving ambulance " + amb.getUnitCode() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Load OSRM route if not already cached for this ambulance+phase.
     * Calculate waypoints-per-tick from the ETA to match displayed timing.
     */
    private void ensureRouteLoaded(Long ambId, IncidentStatus phase,
                                    double fromLat, double fromLng,
                                    double toLat, double toLng) {
        IncidentStatus cachedPhase = routePhase.get(ambId);
        if (routeCache.containsKey(ambId) && phase == cachedPhase) {
            return;
        }

        RouteResponse route = routingService.calculateRoute(fromLat, fromLng, toLat, toLng);
        List<double[]> waypoints = route.getCoordinates();
        double etaMinutes = route.getDurationMinutes();

        if (waypoints != null && waypoints.size() >= 2) {
            routeCache.put(ambId, waypoints);
            waypointIndex.put(ambId, 0);
            routePhase.put(ambId, phase);

            // Calculate speed: how many waypoints to advance per tick
            // to arrive in (ETA / SPEED_MULTIPLIER) real seconds
            double realTimeSeconds = (etaMinutes * 60.0) / SPEED_MULTIPLIER;
            double totalTicks = realTimeSeconds / TICK_INTERVAL_SEC;

            // waypoints per tick to use all waypoints across all ticks
            int wpt = Math.max(1, (int) Math.ceil(waypoints.size() / Math.max(totalTicks, 1)));
            stepsPerTick.put(ambId, wpt);
        }
    }

    /**
     * Advance ambulance along cached waypoints at the ETA-calculated speed.
     * Returns true if ambulance has reached the final waypoint.
     */
    private boolean advanceAlongRoute(Ambulance amb) {
        List<double[]> waypoints = routeCache.get(amb.getId());
        Integer idx = waypointIndex.get(amb.getId());
        Integer steps = stepsPerTick.getOrDefault(amb.getId(), 2);

        if (waypoints == null || idx == null) return false;

        for (int s = 0; s < steps; s++) {
            if (idx >= waypoints.size() - 1) {
                double[] last = waypoints.get(waypoints.size() - 1);
                amb.setLat(last[0]);
                amb.setLng(last[1]);
                ambulanceRepository.save(amb);
                return true;
            }
            idx++;
            double[] wp = waypoints.get(idx);
            amb.setLat(wp[0]);
            amb.setLng(wp[1]);
        }

        waypointIndex.put(amb.getId(), idx);
        ambulanceRepository.save(amb);
        return false;
    }

    private void clearRoute(Long ambId) {
        routeCache.remove(ambId);
        waypointIndex.remove(ambId);
        routePhase.remove(ambId);
        stepsPerTick.remove(ambId);
    }

}
