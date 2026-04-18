package com.aris.controller;

import com.aris.model.Ambulance;
import com.aris.model.AmbulanceStatus;
import com.aris.model.Incident;
import com.aris.model.IncidentStatus;
import com.aris.model.User;
import com.aris.repository.AmbulanceRepository;
import com.aris.repository.IncidentRepository;
import com.aris.repository.UserRepository;
import com.aris.websocket.EventBroadcaster;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/driver")
@PreAuthorize("hasRole('DRIVER')")
public class DriverController {

    private final UserRepository userRepository;
    private final IncidentRepository incidentRepository;
    private final AmbulanceRepository ambulanceRepository;
    private final EventBroadcaster eventBroadcaster;

    public DriverController(UserRepository userRepository, IncidentRepository incidentRepository,
                           AmbulanceRepository ambulanceRepository, EventBroadcaster eventBroadcaster) {
        this.userRepository = userRepository;
        this.incidentRepository = incidentRepository;
        this.ambulanceRepository = ambulanceRepository;
        this.eventBroadcaster = eventBroadcaster;
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication auth) {
        User driver = userRepository.findByUsername(auth.getName()).orElse(null);
        if (driver == null) return ResponseEntity.notFound().build();
        
        Ambulance ambulance = driver.getAssignedAmbulanceId() != null ? 
            ambulanceRepository.findById(driver.getAssignedAmbulanceId()).orElse(null) : null;
        
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", driver.getId());
        profile.put("username", driver.getUsername());
        profile.put("fullName", driver.getFullName());
        profile.put("phone", driver.getPhone());
        profile.put("email", driver.getEmail());
        
        if (ambulance != null) {
            profile.put("ambulanceId", ambulance.getId());
            profile.put("ambulanceCode", ambulance.getUnitCode());
            profile.put("ambulanceStatus", ambulance.getStatus().name());
            profile.put("vehicleNumber", ambulance.getVehicleNumber());
            profile.put("vehicleModel", ambulance.getVehicleModel());
            profile.put("baseLocation", ambulance.getBaseLocation());
            profile.put("equipmentLevel", ambulance.getEquipmentLevel());
        }
        
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/my-ambulance")
    public ResponseEntity<?> getMyAmbulance(Authentication auth) {
        User driver = userRepository.findByUsername(auth.getName()).orElse(null);
        if (driver == null) return ResponseEntity.notFound().build();

        Long ambulanceId = driver.getAssignedAmbulanceId();
        if (ambulanceId == null) {
            return ResponseEntity.ok(Map.of("status", "NO_ASSIGNED_AMBULANCE", "message", "No ambulance assigned to you"));
        }

        Ambulance ambulance = ambulanceRepository.findById(ambulanceId).orElse(null);
        if (ambulance == null) {
            return ResponseEntity.ok(Map.of("status", "AMBULANCE_NOT_FOUND", "message", "Assigned ambulance not found"));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("id", ambulance.getId());
        response.put("unitCode", ambulance.getUnitCode());
        response.put("lat", ambulance.getLat());
        response.put("lng", ambulance.getLng());
        response.put("status", ambulance.getStatus().name());
        response.put("vehicleNumber", ambulance.getVehicleNumber());
        response.put("vehicleModel", ambulance.getVehicleModel());
        response.put("equipmentLevel", ambulance.getEquipmentLevel());
        response.put("baseLocation", ambulance.getBaseLocation());

        if (ambulance.getCurrentIncident() != null) {
            Incident incident = ambulance.getCurrentIncident();
            response.put("incidentId", incident.getId());
            response.put("patientName", incident.getPatientName());
            response.put("patientPhone", incident.getPatientPhone());
            response.put("patientAddress", incident.getPatientAddress());
            response.put("incidentLat", incident.getLat());
            response.put("incidentLng", incident.getLng());
            response.put("incidentStatus", incident.getStatus().name());
            response.put("condition", incident.getCondition());
            response.put("priorityLevel", incident.getPriorityLevel());
            response.put("preferredHospitalId", incident.getPatientPreferredHospitalId());
            response.put("preferredHospitalName", incident.getPatientPreferredHospitalName());
            response.put("assignedHospitalId", incident.getAssignedHospitalId());
            response.put("assignedHospitalName", incident.getAssignedHospitalName());
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/assignment")
    public ResponseEntity<?> getCurrentAssignment(Authentication auth) {
        User driver = userRepository.findByUsername(auth.getName()).orElse(null);
        if (driver == null) return ResponseEntity.notFound().build();

        Long incidentId = driver.getAssignedIncidentId();
        if (incidentId == null) {
            return ResponseEntity.ok(Map.of("status", "NO_ASSIGNMENT", "message", "No active assignment"));
        }

        Incident incident = incidentRepository.findById(incidentId).orElse(null);
        if (incident == null) {
            return ResponseEntity.ok(Map.of("status", "NO_ASSIGNMENT", "message", "Assignment not found"));
        }

        Ambulance ambulance = incident.getAssignedAmbulance();

        Map<String, Object> response = new HashMap<>();
        response.put("incidentId", incident.getId());
        response.put("status", incident.getStatus().name());
        response.put("patientName", incident.getPatientName());
        response.put("patientPhone", incident.getPatientPhone());
        response.put("patientAddress", incident.getPatientAddress());
        response.put("incidentLat", incident.getLat());
        response.put("incidentLng", incident.getLng());
        response.put("condition", incident.getCondition());
        response.put("emergencyType", incident.getEmergencyType());
        response.put("priorityLevel", incident.getPriorityLevel());
        response.put("notes", incident.getNotes());
        response.put("createdAt", incident.getCreatedAt());
        response.put("dispatchedAt", incident.getDispatchedAt());

        if (ambulance != null) {
            response.put("ambulanceId", ambulance.getId());
            response.put("ambulanceCode", ambulance.getUnitCode());
            response.put("ambulanceLat", ambulance.getLat());
            response.put("ambulanceLng", ambulance.getLng());
            response.put("destinationHospitalId", ambulance.getDestinationHospitalId());
            response.put("destinationHospital", ambulance.getDestinationHospital());
            response.put("etaMinutes", ambulance.getEtaMinutes());
        }

        if (incident.getAssignedHospitalId() != null) {
            response.put("assignedHospitalId", incident.getAssignedHospitalId());
            response.put("assignedHospitalName", incident.getAssignedHospitalName());
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/update-status")
    public ResponseEntity<?> updateStatus(@RequestBody Map<String, Object> request, Authentication auth) {
        User driver = userRepository.findByUsername(auth.getName()).orElse(null);
        if (driver == null) return ResponseEntity.notFound().build();

        String status = (String) request.get("status");
        AmbulanceStatus newStatus;
        try {
            newStatus = AmbulanceStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid status: " + status));
        }

        Ambulance ambulance = driver.getAssignedAmbulanceId() != null ?
            ambulanceRepository.findById(driver.getAssignedAmbulanceId()).orElse(null) : null;
        
        if (ambulance == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "No ambulance assigned"));
        }

        ambulance.setStatus(newStatus);
        ambulanceRepository.save(ambulance);

        if (ambulance.getCurrentIncident() != null) {
            Incident incident = ambulance.getCurrentIncident();
            
            switch (newStatus) {
                case TRANSIT_TO_PATIENT:
                    incident.setStatus(IncidentStatus.EN_ROUTE_PATIENT);
                    incident.setDispatchedAt(LocalDateTime.now());
                    eventBroadcaster.broadcast("DISPATCH",
                        String.format("Ambulance %s en route to patient at (%.4f, %.4f)",
                            ambulance.getUnitCode(), incident.getLat(), incident.getLng()), "DISPATCH");
                    eventBroadcaster.broadcastIncidentUpdate(
                        incident.getId(), "EN_ROUTE_PATIENT",
                        ambulance.getId(), ambulance.getUnitCode(),
                        incident.getAssignedHospitalId(), incident.getAssignedHospitalName(),
                        ambulance.getEtaMinutes() != null ? ambulance.getEtaMinutes().intValue() : 0
                    );
                    break;
                case ON_SCENE:
                    incident.setStatus(IncidentStatus.PICKED_UP);
                    incident.setPickedUpAt(LocalDateTime.now());
                    eventBroadcaster.broadcast("SYSTEM",
                        String.format("Ambulance %s picked up patient: %s",
                            ambulance.getUnitCode(), incident.getPatientName()), "SYSTEM");
                    // Broadcast PICKED_UP with hospital info so patient map switches to hospital route
                    eventBroadcaster.broadcastIncidentUpdate(
                        incident.getId(), "PICKED_UP",
                        ambulance.getId(), ambulance.getUnitCode(),
                        incident.getAssignedHospitalId(), incident.getAssignedHospitalName(),
                        ambulance.getEtaMinutes() != null ? ambulance.getEtaMinutes().intValue() : 0
                    );
                    // Also push ambulance location update with hospital context
                    if (incident.getPatientUserId() != null) {
                        eventBroadcaster.broadcastAmbulanceLocation(
                            ambulance.getId(), ambulance.getLat(), ambulance.getLng(),
                            "PICKED_UP", ambulance.getUnitCode(), 0, incident.getPatientUserId()
                        );
                    }
                    break;
                case TRANSIT_TO_HOSPITAL:
                    incident.setStatus(IncidentStatus.EN_ROUTE_HOSPITAL);
                    eventBroadcaster.broadcast("DISPATCH",
                        String.format("Ambulance %s transporting patient to %s",
                            ambulance.getUnitCode(), incident.getAssignedHospitalName()), "DISPATCH");
                    // Broadcast EN_ROUTE_HOSPITAL so patient map draws blue hospital route
                    eventBroadcaster.broadcastIncidentUpdate(
                        incident.getId(), "EN_ROUTE_HOSPITAL",
                        ambulance.getId(), ambulance.getUnitCode(),
                        incident.getAssignedHospitalId(), incident.getAssignedHospitalName(),
                        ambulance.getEtaMinutes() != null ? ambulance.getEtaMinutes().intValue() : 0
                    );
                    break;
                case AT_HOSPITAL:
                    incident.setStatus(IncidentStatus.ARRIVED_HOSPITAL);
                    incident.setArrivedHospitalAt(LocalDateTime.now());
                    eventBroadcaster.broadcast("SYSTEM",
                        String.format("Ambulance %s arrived at %s",
                            ambulance.getUnitCode(), incident.getAssignedHospitalName()), "SYSTEM");
                    eventBroadcaster.broadcastIncidentUpdate(
                        incident.getId(), "ARRIVED_HOSPITAL",
                        ambulance.getId(), ambulance.getUnitCode(),
                        incident.getAssignedHospitalId(), incident.getAssignedHospitalName(), 0
                    );
                    break;
                case STANDBY:
                    incident.setStatus(IncidentStatus.RESOLVED);
                    incident.setResolvedAt(LocalDateTime.now());
                    driver.setAssignedIncidentId(null);
                    userRepository.save(driver);
                    eventBroadcaster.broadcast("SYSTEM",
                        String.format("Incident #%d resolved - Patient delivered to %s",
                            incident.getId(), incident.getAssignedHospitalName()), "SYSTEM");
                    break;
            }
            incidentRepository.save(incident);
        }

        return ResponseEntity.ok(Map.of(
            "message", "Status updated",
            "ambulanceStatus", newStatus.name()
        ));
    }

    @PostMapping("/update-location")
    public ResponseEntity<?> updateLocation(@RequestBody Map<String, Object> request, Authentication auth) {
        User driver = userRepository.findByUsername(auth.getName()).orElse(null);
        if (driver == null) return ResponseEntity.notFound().build();

        Double lat = request.get("lat") != null ? Double.parseDouble(request.get("lat").toString()) : null;
        Double lng = request.get("lng") != null ? Double.parseDouble(request.get("lng").toString()) : null;

        if (lat == null || lng == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Location required"));
        }

        Ambulance ambulance = driver.getAssignedAmbulanceId() != null ?
            ambulanceRepository.findById(driver.getAssignedAmbulanceId()).orElse(null) : null;
        
        if (ambulance == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "No ambulance assigned"));
        }

        ambulance.setLat(lat);
        ambulance.setLng(lng);
        ambulanceRepository.save(ambulance);

        eventBroadcaster.broadcast("LOCATION_UPDATE", 
            String.format("Ambulance %s location updated: (%.4f, %.4f)", 
                ambulance.getUnitCode(), lat, lng), "LOCATION");

        return ResponseEntity.ok(Map.of(
            "message", "Location updated",
            "lat", lat,
            "lng", lng
        ));
    }

    @PostMapping("/record-vitals")
    public ResponseEntity<?> recordVitals(@RequestBody Map<String, Object> vitals, Authentication auth) {
        User driver = userRepository.findByUsername(auth.getName()).orElse(null);
        if (driver == null) return ResponseEntity.notFound().build();

        Incident incident = driver.getAssignedIncidentId() != null ?
            incidentRepository.findById(driver.getAssignedIncidentId()).orElse(null) : null;
        
        if (incident == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "No active incident"));
        }

        if (vitals.get("bp") != null) incident.setVitalsBp((String) vitals.get("bp"));
        if (vitals.get("pulse") != null) incident.setVitalsPulse(Integer.parseInt(vitals.get("pulse").toString()));
        if (vitals.get("temperature") != null) incident.setVitalsTemperature(Double.parseDouble(vitals.get("temperature").toString()));
        if (vitals.get("oxygen") != null) incident.setVitalsOxygen(Integer.parseInt(vitals.get("oxygen").toString()));
        if (vitals.get("heartRate") != null) incident.setVitalsHeartRate(Integer.parseInt(vitals.get("heartRate").toString()));
        if (vitals.get("respiratoryRate") != null) incident.setVitalsRespiratoryRate(Integer.parseInt(vitals.get("respiratoryRate").toString()));
        incident.setVitalsRecordedAt(LocalDateTime.now());
        
        incidentRepository.save(incident);

        eventBroadcaster.broadcast("VITALS", 
            String.format("Vitals recorded for patient %s: HR=%s, BP=%s, O2=%s%%", 
                incident.getPatientName(),
                vitals.get("heartRate") != null ? vitals.get("heartRate") : "N/A",
                vitals.get("bp") != null ? vitals.get("bp") : "N/A",
                vitals.get("oxygen") != null ? vitals.get("oxygen") : "N/A"), "MEDICAL");

        return ResponseEntity.ok(Map.of("message", "Vitals recorded successfully"));
    }

    @PostMapping("/accept-dispatch/{incidentId}")
    public ResponseEntity<?> acceptDispatch(@PathVariable Long incidentId, Authentication auth) {
        User driver = userRepository.findByUsername(auth.getName()).orElse(null);
        if (driver == null) return ResponseEntity.notFound().build();

        Incident incident = incidentRepository.findById(incidentId).orElse(null);
        if (incident == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Incident not found"));
        }

        Ambulance ambulance = driver.getAssignedAmbulanceId() != null ?
            ambulanceRepository.findById(driver.getAssignedAmbulanceId()).orElse(null) : null;
        
        if (ambulance == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "No ambulance assigned to you"));
        }

        if (ambulance.getStatus() != AmbulanceStatus.STANDBY) {
            return ResponseEntity.badRequest().body(Map.of("error", "Ambulance not available"));
        }

        incident.setAssignedAmbulance(ambulance);
        incident.setStatus(IncidentStatus.EN_ROUTE_PATIENT);
        incident.setDispatchedAt(LocalDateTime.now());
        incidentRepository.save(incident);

        ambulance.setCurrentIncident(incident);
        ambulance.setStatus(AmbulanceStatus.TRANSIT_TO_PATIENT);
        ambulance.setCurrentPatientName(incident.getPatientName());
        ambulanceRepository.save(ambulance);

        driver.setAssignedIncidentId(incidentId);
        userRepository.save(driver);

        eventBroadcaster.broadcast("DISPATCH_ACCEPTED", 
            String.format("Ambulance %s accepted dispatch to patient at (%.4f, %.4f)", 
                ambulance.getUnitCode(), incident.getLat(), incident.getLng()), "DISPATCH");

        return ResponseEntity.ok(Map.of(
            "message", "Dispatch accepted",
            "incidentId", incidentId,
            "patientLocation", String.format("(%.4f, %.4f)", incident.getLat(), incident.getLng())
        ));
    }

    @PostMapping("/reject-dispatch/{incidentId}")
    public ResponseEntity<?> rejectDispatch(@PathVariable Long incidentId, Authentication auth) {
        User driver = userRepository.findByUsername(auth.getName()).orElse(null);
        if (driver == null) return ResponseEntity.notFound().build();

        eventBroadcaster.broadcast("DISPATCH_REJECTED", 
            String.format("Ambulance driver %s rejected dispatch for incident #%d", 
                driver.getUsername(), incidentId), "DISPATCH");

        return ResponseEntity.ok(Map.of("message", "Dispatch rejected"));
    }
}
