package com.aris.controller;

import com.aris.model.Incident;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final List<Map<String, Object>> patientLogs = new ArrayList<>();
    private Long logIdCounter = 1L;

    @PostMapping("/patient")
    public ResponseEntity<Map<String, Object>> submitPatientData(@RequestBody Map<String, Object> patientData) {
        Map<String, Object> logEntry = new ConcurrentHashMap<>();
        logEntry.put("id", logIdCounter++);
        logEntry.put("patientData", patientData);
        logEntry.put("timestamp", LocalDateTime.now().toString());
        logEntry.put("status", "SHARED");
        
        if (patientData.containsKey("destinationHospital")) {
            Map<String, Object> hospital = (Map<String, Object>) patientData.get("destinationHospital");
            logEntry.put("hospitalName", hospital.get("name"));
        }
        
        patientLogs.add(0, logEntry);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("logId", logEntry.get("id"));
        response.put("message", "Patient data submitted and shared with hospital");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/logs")
    public ResponseEntity<List<Map<String, Object>>> getLogs(
            @RequestParam(required = false) String filter) {
        if (filter == null || filter.equals("all")) {
            return ResponseEntity.ok(new ArrayList<>(patientLogs));
        }
        List<Map<String, Object>> filtered = new ArrayList<>();
        for (Map<String, Object> log : patientLogs) {
            if (filter.equals("emergency") && log.containsKey("patientData")) {
                Map<String, Object> data = (Map<String, Object>) log.get("patientData");
                if (data.containsKey("currentProblem") && !data.get("currentProblem").toString().isEmpty()) {
                    filtered.add(log);
                }
            } else if (filter.equals("share") && log.containsKey("hospitalName")) {
                filtered.add(log);
            }
        }
        return ResponseEntity.ok(filtered);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPatients", patientLogs.size());
        stats.put("sharedWithHospital", patientLogs.stream().filter(l -> l.containsKey("hospitalName")).count());
        stats.put("criticalCases", patientLogs.stream().filter(l -> {
            if (l.containsKey("patientData")) {
                Map<String, Object> data = (Map<String, Object>) l.get("patientData");
                return data.containsKey("heartRate") || data.containsKey("oxygen");
            }
            return false;
        }).count());
        return ResponseEntity.ok(stats);
    }
}
