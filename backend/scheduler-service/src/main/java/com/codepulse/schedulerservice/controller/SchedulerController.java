package com.codepulse.schedulerservice.controller;

import java.time.Instant;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codepulse.schedulerservice.dto.HealthResponse;
import com.codepulse.schedulerservice.dto.TriggerSummaryResponse;
import com.codepulse.schedulerservice.service.SchedulerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * Debug/ops surface for scheduler-service: a liveness check and a manual
 * trigger endpoint so a scheduling run can be tested without waiting for
 * the 60-second tick.
 */
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
@Tag(name = "Scheduler", description = "Debug endpoints for the scheduler-service orchestrator")
public class SchedulerController {

    private final SchedulerService schedulerService;

    @GetMapping("/health")
    @Operation(summary = "Liveness check for scheduler-service")
    public ResponseEntity<HealthResponse> health() {
        HealthResponse response = HealthResponse.builder()
                .service("scheduler-service")
                .status("UP")
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/trigger")
    @Operation(summary = "Manually run one scheduling cycle immediately")
    public ResponseEntity<TriggerSummaryResponse> trigger() {
        TriggerSummaryResponse summary = schedulerService.triggerHealthChecks();
        return ResponseEntity.ok(summary);
    }
}
