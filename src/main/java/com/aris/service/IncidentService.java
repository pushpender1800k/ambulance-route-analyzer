package com.aris.service;

import com.aris.dto.IncidentRequest;
import com.aris.model.Incident;
import com.aris.model.IncidentStatus;
import com.aris.repository.IncidentRepository;
import com.aris.websocket.EventBroadcaster;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final EventBroadcaster eventBroadcaster;

    public IncidentService(IncidentRepository incidentRepository, EventBroadcaster eventBroadcaster) {
        this.incidentRepository = incidentRepository;
        this.eventBroadcaster = eventBroadcaster;
    }

    public List<Incident> getAllActive() {
        List<Incident> active = new java.util.ArrayList<>();
        active.addAll(incidentRepository.findByStatus(IncidentStatus.UNASSIGNED));
        active.addAll(incidentRepository.findByStatus(IncidentStatus.ASSIGNED));
        active.addAll(incidentRepository.findByStatus(IncidentStatus.EN_ROUTE_PATIENT));
        active.addAll(incidentRepository.findByStatus(IncidentStatus.PICKED_UP));
        active.addAll(incidentRepository.findByStatus(IncidentStatus.EN_ROUTE_HOSPITAL));
        return active;
    }

    public List<Incident> getAll() {
        return incidentRepository.findAll();
    }

    public Incident create(IncidentRequest request) {
        Incident incident = new Incident();
        incident.setPatientUserId(request.getPatientId() != null ? Long.parseLong(request.getPatientId()) : null);
        incident.setCondition(request.getCondition());
        incident.setLat(request.getLat());
        incident.setLng(request.getLng());
        incident.setStatus(IncidentStatus.PENDING);
        incident = incidentRepository.save(incident);

        eventBroadcaster.broadcast("SYSTEM",
                String.format("🚨 NEW INCIDENT #%d — %s at [%.4f, %.4f]",
                        incident.getId(), incident.getCondition(), incident.getLat(), incident.getLng()),
                "ALERT");

        return incident;
    }

    public long countActive() {
        return incidentRepository.countByStatus(IncidentStatus.PENDING)
                + incidentRepository.countByStatus(IncidentStatus.UNASSIGNED)
                + incidentRepository.countByStatus(IncidentStatus.ASSIGNED)
                + incidentRepository.countByStatus(IncidentStatus.EN_ROUTE_PATIENT)
                + incidentRepository.countByStatus(IncidentStatus.PICKED_UP)
                + incidentRepository.countByStatus(IncidentStatus.EN_ROUTE_HOSPITAL)
                + incidentRepository.countByStatus(IncidentStatus.ARRIVED_HOSPITAL);
    }
}
