package com.codepulse.notificationservice.controller;

import java.time.Instant;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codepulse.notificationservice.dto.HealthResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/** Liveness check, kept separate from /internal/notifications since it lives at /internal/health. */
@RestController
@Tag(name = "Health", description = "Liveness check for notification-service")
public class HealthController {

    @GetMapping("/internal/health")
    @Operation(summary = "Liveness check for notification-service")
    public ResponseEntity<HealthResponse> health() {
        HealthResponse response = HealthResponse.builder()
                .service("notification-service")
                .status("UP")
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.ok(response);
    }
}
