package com.aris.service;

import com.aris.dto.DispatchRequest;
import com.aris.exception.AlreadyAssignedException;
import com.aris.exception.UnitNotAvailableException;
import com.aris.model.Ambulance;
import com.aris.model.Incident;
import com.aris.model.IncidentStatus;
import com.aris.repository.AmbulanceRepository;
import com.aris.repository.IncidentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class DispatchServiceTest {

    @Autowired private DispatchService dispatchService;
    @Autowired private IncidentRepository incidentRepo;
    @Autowired private AmbulanceRepository ambulanceRepo;

    private Incident createTestIncident() {
        Incident incident = new Incident();
        incident.setPatientUserId(1L);
        incident.setCondition("Test Issue");
        incident.setLat(28.5);
        incident.setLng(77.2);
        incident.setStatus(IncidentStatus.PENDING);
        return incidentRepo.save(incident);
    }

    private Ambulance createTestAmbulance(String code) {
        Ambulance amb = new Ambulance(code, 28.5, 77.2);
        return ambulanceRepo.save(amb);
    }

    @Test
    void shouldNotAllowTwoAmbulancesForSameIncident() {
        Incident incident = createTestIncident();
        Ambulance amb1 = createTestAmbulance("AMB-01");
        Ambulance amb2 = createTestAmbulance("AMB-02");

        assertDoesNotThrow(() -> dispatchService.dispatch(
            new DispatchRequest(amb1.getId(), null, incident.getId())
        ));

        assertThrows(AlreadyAssignedException.class, () -> dispatchService.dispatch(
            new DispatchRequest(amb2.getId(), null, incident.getId())
        ));

        Incident updated = incidentRepo.findById(incident.getId()).orElseThrow();
        assertEquals("AMB-01", updated.getAssignedAmbulance().getUnitCode());
        assertEquals(IncidentStatus.EN_ROUTE_PATIENT, updated.getStatus());
    }

    @Test
    void shouldNotAllowOneAmbulanceForTwoIncidents() {
        Incident inc1 = createTestIncident();
        Incident inc2 = createTestIncident();
        Ambulance amb = createTestAmbulance("AMB-01");

        assertDoesNotThrow(() -> dispatchService.dispatch(
            new DispatchRequest(amb.getId(), null, inc1.getId())
        ));

        assertThrows(UnitNotAvailableException.class, () -> dispatchService.dispatch(
            new DispatchRequest(amb.getId(), null, inc2.getId())
        ));
    }
}
