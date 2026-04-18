package com.aris.websocket;

import com.aris.model.EventLog;
import com.aris.repository.EventLogRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class EventBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;
    private final EventLogRepository eventLogRepository;

    public EventBroadcaster(SimpMessagingTemplate messagingTemplate,
                            EventLogRepository eventLogRepository) {
        this.messagingTemplate = messagingTemplate;
        this.eventLogRepository = eventLogRepository;
    }

    public void broadcast(String source, String message, String type) {
        EventLog event = new EventLog(source, message, type);
        eventLogRepository.save(event);
        messagingTemplate.convertAndSend("/topic/events", event);
    }

    public void broadcastAmbulanceLocation(Long ambulanceId, Double lat, Double lng,
                                          String status, String ambulanceCode, Integer etaMinutes, Long patientId) {
        Map<String, Object> locationData = new HashMap<>();
        locationData.put("ambulanceId", ambulanceId);
        locationData.put("lat", lat);
        locationData.put("lng", lng);
        locationData.put("status", status);
        locationData.put("ambulanceCode", ambulanceCode);
        locationData.put("etaMinutes", etaMinutes);
        locationData.put("patientId", patientId);
        locationData.put("timestamp", System.currentTimeMillis());

        messagingTemplate.convertAndSend("/topic/ambulance/locations", locationData);

        if (patientId != null) {
            messagingTemplate.convertAndSend("/topic/patient/" + patientId + "/updates", locationData);
        }
    }

    public void broadcastIncidentUpdate(Long incidentId, String status, Long ambulanceId, 
                                       String ambulanceCode, Long hospitalId, String hospitalName, Integer eta) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("incidentId", incidentId);
        updateData.put("status", status);
        updateData.put("ambulanceId", ambulanceId);
        updateData.put("ambulanceCode", ambulanceCode);
        updateData.put("hospitalId", hospitalId);
        updateData.put("hospitalName", hospitalName);
        updateData.put("etaMinutes", eta);
        updateData.put("timestamp", System.currentTimeMillis());

        messagingTemplate.convertAndSend("/topic/incident/updates", updateData);
    }
}
