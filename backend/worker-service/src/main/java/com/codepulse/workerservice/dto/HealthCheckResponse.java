package com.codepulse.workerservice.dto;

import java.time.Instant;
import java.util.UUID;

import com.codepulse.workerservice.entity.enums.HealthStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response returned from executeHealthCheck() and the latest-health lookup. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthCheckResponse {

    private UUID monitorId;
    private HealthStatus status;
    private Integer statusCode;
    private Long responseTimeMs;
    private Instant checkedAt;
    private String errorMessage;
}
