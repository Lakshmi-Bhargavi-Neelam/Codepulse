package com.codepulse.workerservice.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.codepulse.workerservice.entity.HealthCheckLog;

@Repository
public interface HealthCheckLogRepository extends JpaRepository<HealthCheckLog, UUID> {

    List<HealthCheckLog> findTop20ByMonitorIdOrderByCheckedAtDesc(UUID monitorId);
}
