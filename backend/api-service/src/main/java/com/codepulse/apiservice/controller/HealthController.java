package com.codepulse.apiservice.controller;

import java.time.Instant;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codepulse.apiservice.dto.HealthResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Health check endpoint for api-service.
 */
@RestController
@RequestMapping("/internal")
@Tag(name = "Health", description = "Service health check endpoint")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "Liveness check for api-service")
    public ResponseEntity<HealthResponse> health() {
        HealthResponse response = HealthResponse.builder()
                .service("api-service")
                .status("UP")
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.ok(response);
    }
}