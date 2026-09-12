package com.codepulse.notificationservice.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response body for GET /internal/health (liveness check). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthResponse {

    private String service;
    private String status;
    private Instant timestamp;
}
