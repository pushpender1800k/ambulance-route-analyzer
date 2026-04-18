package com.aris.service;

import com.aris.dto.DispatchRequest;
import com.aris.exception.AlreadyAssignedException;
import com.aris.exception.NotFoundException;
import com.aris.exception.UnitNotAvailableException;
import com.aris.model.*;
import com.aris.repository.AmbulanceRepository;
import com.aris.repository.DispatchRepository;
import com.aris.repository.HospitalRepository;
import com.aris.repository.IncidentRepository;
import com.aris.websocket.EventBroadcaster;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class DispatchService {

    private final DispatchRepository dispatchRepository;
    private final AmbulanceRepository ambulanceRepository;
    private final HospitalRepository hospitalRepository;
    private final IncidentRepository incidentRepository;
    private final RoutingService routingService;
    private final EventBroadcaster eventBroadcaster;

    public DispatchService(DispatchRepository dispatchRepository,
                           AmbulanceRepository ambulanceRepository,
                           HospitalRepository hospitalRepository,
                           IncidentRepository incidentRepository,
                           RoutingService routingService,
                           EventBroadcaster eventBroadcaster) {
        this.dispatchRepository = dispatchRepository;
        this.ambulanceRepository = ambulanceRepository;
        this.hospitalRepository = hospitalRepository;
        this.incidentRepository = incidentRepository;
        this.routingService = routingService;
        this.eventBroadcaster = eventBroadcaster;
    }

    /**
     * ATOMIC DISPATCH — only one ambulance per incident, guaranteed.
     * Uses pessimistic locking + status validation to prevent race conditions.
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Dispatch dispatch(DispatchRequest request) {

        // STEP 1: Lock the incident row — no other thread can touch it
        Incident incident = incidentRepository.findByIdWithLock(request.getIncidentId())
                .orElseThrow(() -> new NotFoundException("Incident not found: " + request.getIncidentId()));

        // STEP 2: Guard — is this incident already assigned?
        if (incident.getStatus() != IncidentStatus.PENDING && incident.getStatus() != IncidentStatus.UNASSIGNED) {
            String assignedUnit = incident.getAssignedAmbulance() != null ? 
                                  incident.getAssignedAmbulance().getUnitCode() : "unknown";
            throw new AlreadyAssignedException(
                    "Incident " + incident.getId() + " already assigned to ambulance: " + assignedUnit
            );
        }

        // STEP 3: Lock the ambulance row — no other thread can grab it
        Ambulance ambulance = ambulanceRepository.findByIdWithLock(request.getUnitId())
                .orElseThrow(() -> new NotFoundException("Ambulance not found: " + request.getUnitId()));

        // STEP 4: Guard — is this ambulance already busy?
        if (ambulance.getStatus() != AmbulanceStatus.STANDBY) {
            throw new UnitNotAvailableException(
                    "Ambulance " + ambulance.getUnitCode() + " is currently: " + ambulance.getStatus()
            );
        }

        // STEP 5: Hospital processing (optional but usually provided)
        Hospital hospital = null;
        if (request.getHospitalId() != null) {
            hospital = hospitalRepository.findById(request.getHospitalId())
                    .orElseThrow(() -> new NotFoundException("Hospital not found"));
            
            incident.setAssignedHospitalId(hospital.getId());
            incident.setAssignedHospitalName(hospital.getName());
            
            if (hospital.getAvailableBeds() > 0) {
                hospital.setAvailableBeds(hospital.getAvailableBeds() - 1);
                hospitalRepository.save(hospital);
            }
        }

        // Calculate ETA from ambulance to incident
        double etaMinutes = 0.0;
        var routeToIncident = routingService.calculateRoute(
                ambulance.getLat(), ambulance.getLng(),
                incident.getLat(), incident.getLng());
        etaMinutes = routeToIncident.getDurationMinutes();

        // STEP 6: Atomic assignment — both happen in same transaction
        incident.setAssignedAmbulance(ambulance);
        incident.setStatus(IncidentStatus.EN_ROUTE_PATIENT);
        incident.setDispatchedAt(java.time.LocalDateTime.now());
        ambulance.setStatus(AmbulanceStatus.TRANSIT_TO_PATIENT);
        ambulance.setCurrentIncident(incident);
        ambulance.setCurrentPatientName(incident.getPatientName());
        ambulance.setDestinationHospital(hospital != null ? hospital.getName() : null);
        ambulance.setDestinationHospitalId(hospital != null ? hospital.getId() : null);
        ambulance.setDestinationLat(incident.getLat());
        ambulance.setDestinationLng(incident.getLng());

        incidentRepository.save(incident);
        ambulanceRepository.save(ambulance);

        // STEP 7: Create dispatch record
        Dispatch dispatch = new Dispatch();
        dispatch.setIncidentId(incident.getId());
        dispatch.setAmbulanceId(ambulance.getId());
        if(hospital != null) {
            dispatch.setHospitalId(hospital.getId());
        } else {
            dispatch.setHospitalId(0L); // fallback
        }
        dispatch.setEtaMinutes(etaMinutes);
        dispatch.setDispatchedAt(java.time.LocalDateTime.now());
        dispatch = dispatchRepository.save(dispatch);

        // STEP 8: Broadcast event
        String hospitalName = hospital != null ? hospital.getName() : "Unknown location";
        eventBroadcaster.broadcast("NPC DISPATCH",
                String.format("🚑 Unit %s dispatched to %s for Incident #%d — ETA: %.1f min",
                        ambulance.getUnitCode(), hospitalName,
                        incident.getId(), etaMinutes),
                "DISPATCH");

        // STEP 9: Broadcast ambulance location to patient for live tracking
        eventBroadcaster.broadcastAmbulanceLocation(
                ambulance.getId(),
                ambulance.getLat(),
                ambulance.getLng(),
                ambulance.getStatus().name(),
                ambulance.getUnitCode(),
                (int) etaMinutes,
                incident.getPatientUserId()
        );

        // STEP 10: Broadcast incident update to patient
        eventBroadcaster.broadcastIncidentUpdate(
                incident.getId(),
                "EN_ROUTE_PATIENT",
                ambulance.getId(),
                ambulance.getUnitCode(),
                hospital != null ? hospital.getId() : null,
                hospitalName,
                (int) etaMinutes
        );

        return dispatch;
    }

    /**
     * Auto-assign nearest available ambulance to a new incident.
     * If no hospital is specified, automatically finds the nearest hospital with available beds.
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void autoAssignNearest(Incident incident, Long targetHospitalId) {

        List<Ambulance> available = ambulanceRepository.findByStatus(AmbulanceStatus.STANDBY);

        if (available.isEmpty()) {
            eventBroadcaster.broadcast("ALERT",
                    "WARNING: No units available for incident " + incident.getId(), "WARNING");
            return;
        }

        // Find nearest ambulance
        Ambulance nearest = available.stream()
                .min(Comparator.comparingDouble(a ->
                        routingService.calculateRoute(incident.getLat(), incident.getLng(), a.getLat(), a.getLng()).getDistanceKm()
                ))
                .orElseThrow();

        // If no hospital was specified, auto-select the nearest one with available beds
        if (targetHospitalId == null) {
            List<Hospital> hospitals = hospitalRepository.findAll();
            Hospital nearestHospital = hospitals.stream()
                    .filter(h -> h.getAvailableBeds() != null && h.getAvailableBeds() > 0)
                    .filter(h -> h.getLat() != null && h.getLng() != null)
                    .min(Comparator.comparingDouble(h ->
                            routingService.calculateRoute(incident.getLat(), incident.getLng(), h.getLat(), h.getLng()).getDistanceKm()
                    ))
                    .orElse(null);

            if (nearestHospital != null) {
                targetHospitalId = nearestHospital.getId();
            }
        }

        // Dispatch with resolved hospital
        DispatchRequest req = new DispatchRequest(nearest.getId(), targetHospitalId, incident.getId());
        dispatch(req);
    }
}
