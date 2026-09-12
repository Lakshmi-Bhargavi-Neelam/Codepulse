package com.codepulse.workerservice.service;

import java.util.List;
import java.util.UUID;

import com.codepulse.workerservice.dto.HealthCheckResponse;
import com.codepulse.workerservice.dto.MonitorConfig;
import com.codepulse.workerservice.entity.HealthCheckLog;

public interface WorkerService {

    HealthCheckResponse executeHealthCheck(MonitorConfig monitor);

    HealthCheckResponse getLatestHealth(UUID monitorId);

    List<HealthCheckLog> getHealthHistory(UUID monitorId);
}
