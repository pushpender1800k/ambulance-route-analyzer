package com.aris.controller;

import com.aris.model.Ambulance;
import com.aris.model.Incident;
import com.aris.model.IncidentStatus;
import com.aris.model.User;
import com.aris.repository.AmbulanceRepository;
import com.aris.repository.IncidentRepository;
import com.aris.repository.UserRepository;
import com.aris.service.DispatchService;
import com.aris.websocket.EventBroadcaster;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patient")
@PreAuthorize("hasRole('PATIENT')")
public class PatientController {

    private final UserRepository userRepository;
    private final IncidentRepository incidentRepository;
    private final AmbulanceRepository ambulanceRepository;
    private final EventBroadcaster eventBroadcaster;
    private final DispatchService dispatchService;

    public PatientController(UserRepository userRepository, IncidentRepository incidentRepository,
                            AmbulanceRepository ambulanceRepository, EventBroadcaster eventBroadcaster,
                            DispatchService dispatchService) {
        this.userRepository = userRepository;
        this.incidentRepository = incidentRepository;
        this.ambulanceRepository = ambulanceRepository;
        this.eventBroadcaster = eventBroadcaster;
        this.dispatchService = dispatchService;
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication auth) {
        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("username", user.getUsername());
        profile.put("fullName", user.getFullName());
        profile.put("phone", user.getPhone());
        profile.put("email", user.getEmail());
        profile.put("address", user.getAddress());
        profile.put("emergencyContactName", user.getEmergencyContactName());
        profile.put("emergencyContactPhone", user.getEmergencyContactPhone());
        profile.put("medicalHistory", user.getMedicalHistory());
        profile.put("bloodType", user.getBloodType());
        profile.put("dateOfBirth", user.getDateOfBirth());
        
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/request-ambulance")
    public ResponseEntity<?> requestAmbulance(@RequestBody Map<String, Object> request, Authentication auth) {
        User patient = userRepository.findByUsername(auth.getName()).orElse(null);
        if (patient == null) return ResponseEntity.badRequest().body(Map.of("error", "User not found"));

        Double lat = request.get("lat") != null ? Double.parseDouble(request.get("lat").toString()) : null;
        Double lng = request.get("lng") != null ? Double.parseDouble(request.get("lng").toString()) : null;
        String condition = (String) request.get("condition");
        Long preferredHospitalId = request.get("preferredHospitalId") != null ? 
            Long.parseLong(request.get("preferredHospitalId").toString()) : null;

        if (lat == null || lng == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Location is required"));
        }

        Incident incident = new Incident();
        incident.setPatientUserId(patient.getId());
        incident.setLat(lat);
        incident.setLng(lng);
        incident.setCondition(condition != null ? condition : "Emergency Request");
        incident.setStatus(IncidentStatus.PENDING);
        incident.setPatientName(patient.getFullName() != null ? patient.getFullName() : patient.getUsername());
        incident.setPatientPhone(patient.getPhone());
        incident.setPatientAddress(patient.getAddress());
        incident.setPatientPreferredHospitalId(preferredHospitalId);
        incident.setCreatedAt(LocalDateTime.now());

        incident = incidentRepository.save(incident);

        patient.setAssignedIncidentId(incident.getId());
        userRepository.save(patient);

        dispatchService.autoAssignNearest(incident, preferredHospitalId);

        incident = incidentRepository.findById(incident.getId()).orElse(incident);

        eventBroadcaster.broadcast("PATIENT_REQUEST", 
            String.format("Patient %s requested ambulance at (%.4f, %.4f)", 
                patient.getUsername(), lat, lng), "PATIENT");

        Map<String, Object> response = new HashMap<>();
        response.put("incidentId", incident.getId());
        response.put("status", incident.getStatus().name());
        response.put("message", incident.getAssignedAmbulance() != null ? 
            "Ambulance " + incident.getAssignedAmbulance().getUnitCode() + " is on the way!" : 
            "Request submitted. Searching for nearest ambulance...");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-incident")
    public ResponseEntity<?> getMyIncident(Authentication auth) {
        User patient = userRepository.findByUsername(auth.getName()).orElse(null);
        if (patient == null) return ResponseEntity.notFound().build();

        Long incidentId = patient.getAssignedIncidentId();
        if (incidentId == null) {
            return ResponseEntity.ok(Map.of("status", "NO_ACTIVE_INCIDENT", "message", "No active ambulance request"));
        }

        Incident incident = incidentRepository.findById(incidentId).orElse(null);
        if (incident == null) {
            return ResponseEntity.ok(Map.of("status", "NO_ACTIVE_INCIDENT", "message", "No active ambulance request"));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("incidentId", incident.getId());
        response.put("status", incident.getStatus().name());
        response.put("condition", incident.getCondition());
        response.put("lat", incident.getLat());
        response.put("lng", incident.getLng());
        response.put("createdAt", incident.getCreatedAt());
        response.put("assignedHospitalId", incident.getAssignedHospitalId());
        response.put("assignedHospitalName", incident.getAssignedHospitalName());
        response.put("patientPreferredHospitalId", incident.getPatientPreferredHospitalId());
        response.put("patientPreferredHospitalName", incident.getPatientPreferredHospitalName());

        if (incident.getAssignedAmbulance() != null) {
            Ambulance ambulance = incident.getAssignedAmbulance();
            response.put("ambulanceId", ambulance.getId());
            response.put("ambulanceCode", ambulance.getUnitCode());
            response.put("ambulanceLat", ambulance.getLat());
            response.put("ambulanceLng", ambulance.getLng());
            response.put("ambulanceStatus", ambulance.getStatus().name());
            response.put("etaMinutes", ambulance.getEtaMinutes());
            response.put("assignedDriverName", ambulance.getAssignedDriverName());
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/track-ambulance")
    public ResponseEntity<?> trackAmbulance(Authentication auth) {
        User patient = userRepository.findByUsername(auth.getName()).orElse(null);
        if (patient == null) return ResponseEntity.notFound().build();

        Long incidentId = patient.getAssignedIncidentId();
        if (incidentId == null) {
            return ResponseEntity.ok(Map.of("status", "NO_ASSIGNED_AMBULANCE", "message", "No ambulance assigned yet"));
        }

        Incident incident = incidentRepository.findById(incidentId).orElse(null);
        if (incident == null || incident.getAssignedAmbulance() == null) {
            return ResponseEntity.ok(Map.of("status", "SEARCHING", "message", "Searching for nearest ambulance..."));
        }

        Ambulance ambulance = incident.getAssignedAmbulance();
        Map<String, Object> tracking = new HashMap<>();
        tracking.put("ambulanceId", ambulance.getId());
        tracking.put("ambulanceCode", ambulance.getUnitCode());
        tracking.put("lat", ambulance.getLat());
        tracking.put("lng", ambulance.getLng());
        tracking.put("status", ambulance.getStatus().name());
        tracking.put("etaMinutes", ambulance.getEtaMinutes());
        tracking.put("driverName", ambulance.getAssignedDriverName());
        tracking.put("driverPhone", "Contact dispatch for driver info");
        tracking.put("incidentStatus", incident.getStatus().name());
        
        if (incident.getStatus() == IncidentStatus.PICKED_UP || 
            incident.getStatus() == IncidentStatus.EN_ROUTE_HOSPITAL ||
            incident.getStatus() == IncidentStatus.ARRIVED_HOSPITAL) {
            tracking.put("destinationHospital", incident.getAssignedHospitalName());
            tracking.put("destinationHospitalId", incident.getAssignedHospitalId());
        }

        return ResponseEntity.ok(tracking);
    }

    @PostMapping("/cancel-request/{incidentId}")
    public ResponseEntity<?> cancelRequest(@PathVariable Long incidentId, Authentication auth) {
        User patient = userRepository.findByUsername(auth.getName()).orElse(null);
        if (patient == null) return ResponseEntity.notFound().build();

        Incident incident = incidentRepository.findById(incidentId).orElse(null);
        if (incident == null || !incident.getPatientUserId().equals(patient.getId())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid incident"));
        }

        if (incident.getStatus() == IncidentStatus.PICKED_UP || 
            incident.getStatus() == IncidentStatus.EN_ROUTE_HOSPITAL) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cannot cancel - ambulance already en route to hospital"));
        }

        incident.setStatus(IncidentStatus.CANCELLED);
        incidentRepository.save(incident);

        patient.setAssignedIncidentId(null);
        userRepository.save(patient);

        eventBroadcaster.broadcast("PATIENT_CANCEL", 
            String.format("Patient %s cancelled ambulance request #%d", patient.getUsername(), incidentId), "PATIENT");

        return ResponseEntity.ok(Map.of("message", "Request cancelled successfully"));
    }

    @GetMapping("/hospitals")
    public ResponseEntity<?> getHospitals() {
        return ResponseEntity.ok("Use /api/hospitals endpoint");
    }
}
