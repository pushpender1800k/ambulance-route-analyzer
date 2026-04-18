package com.aris.service;

import com.aris.model.Ambulance;
import com.aris.model.AmbulanceStatus;
import com.aris.repository.AmbulanceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AmbulanceService {

    private final AmbulanceRepository ambulanceRepository;

    public AmbulanceService(AmbulanceRepository ambulanceRepository) {
        this.ambulanceRepository = ambulanceRepository;
    }

    public List<Ambulance> getAll() {
        return ambulanceRepository.findAll();
    }

    public Ambulance getById(Long id) {
        return ambulanceRepository.findById(id).orElseThrow(() ->
                new RuntimeException("Ambulance not found: " + id));
    }

    public long countDeployed() {
        return ambulanceRepository.countByStatus(AmbulanceStatus.TRANSIT_TO_PATIENT) + 
               ambulanceRepository.countByStatus(AmbulanceStatus.TRANSIT_TO_HOSPITAL) +
               ambulanceRepository.countByStatus(AmbulanceStatus.ON_SCENE) +
               ambulanceRepository.countByStatus(AmbulanceStatus.AT_HOSPITAL);
    }

    public long countTotal() {
        return ambulanceRepository.count();
    }
}
