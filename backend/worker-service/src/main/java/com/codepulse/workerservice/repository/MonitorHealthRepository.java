package com.codepulse.workerservice.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.codepulse.workerservice.entity.MonitorHealth;

@Repository
public interface MonitorHealthRepository extends JpaRepository<MonitorHealth, UUID> {

    Optional<MonitorHealth> findByMonitorId(UUID monitorId);
}
