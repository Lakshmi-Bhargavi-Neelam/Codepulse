package com.codepulse.workerservice.controller;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codepulse.workerservice.dto.HealthCheckResponse;
import com.codepulse.workerservice.dto.HealthResponse;
import com.codepulse.workerservice.dto.MonitorConfig;
import com.codepulse.workerservice.entity.HealthCheckLog;
import com.codepulse.workerservice.service.WorkerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Internal API surface for worker-service. /internal/check is called only
 * by scheduler-service; the /internal/health/* endpoints are debug/read
 * endpoints for the latest status and history of a monitor.
 */
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
@Tag(name = "Worker", description = "Health-check execution and read endpoints for worker-service")
public class WorkerController {

    private final WorkerService workerService;

    @PostMapping("/check")
    @Operation(summary = "Execute a health check for the given monitor (called by scheduler-service)")
    public ResponseEntity<HealthCheckResponse> check(@Valid @RequestBody MonitorConfig monitor) {
        HealthCheckResponse response = workerService.executeHealthCheck(monitor);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health/latest/{monitorId}")
    @Operation(summary = "Get the latest known status of a monitor")
    public ResponseEntity<HealthCheckResponse> latest(@PathVariable UUID monitorId) {
        return ResponseEntity.ok(workerService.getLatestHealth(monitorId));
    }

    @GetMapping("/health/history/{monitorId}")
    @Operation(summary = "Get the last 20 health check log entries for a monitor")
    public ResponseEntity<List<HealthCheckLog>> history(@PathVariable UUID monitorId) {
        return ResponseEntity.ok(workerService.getHealthHistory(monitorId));
    }

    @GetMapping("/health")
    @Operation(summary = "Liveness check for worker-service")
    public ResponseEntity<HealthResponse> health() {
        HealthResponse response = HealthResponse.builder()
                .service("worker-service")
                .status("UP")
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.ok(response);
    }
}
