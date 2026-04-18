package com.aris.repository;

import com.aris.model.Ambulance;
import com.aris.model.AmbulanceStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AmbulanceRepository extends JpaRepository<Ambulance, Long> {
    List<Ambulance> findByStatus(AmbulanceStatus status);
    Optional<Ambulance> findByUnitCode(String unitCode);
    long countByStatus(AmbulanceStatus status);
    boolean existsByUnitCode(String unitCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Ambulance a WHERE a.id = :id")
    Optional<Ambulance> findByIdWithLock(@Param("id") Long id);

    boolean existsByCurrentIncidentId(Long incidentId);
}
