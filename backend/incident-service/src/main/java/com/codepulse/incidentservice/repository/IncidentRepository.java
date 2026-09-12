package com.codepulse.incidentservice.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.codepulse.incidentservice.entity.Incident;
import com.codepulse.incidentservice.entity.enums.IncidentStatus;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, UUID> {

    Optional<Incident> findByMonitorIdAndStatus(UUID monitorId, IncidentStatus status);

    List<Incident> findByProjectIdOrderByStartedAtDesc(UUID projectId);

    List<Incident> findTop20ByOrderByStartedAtDesc();
}
