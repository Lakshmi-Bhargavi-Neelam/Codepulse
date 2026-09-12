package com.codepulse.workerservice.dto;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Sent to incident-service's POST /internal/incidents/status-change when a monitor flips UP/DOWN. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentRequest {

    private UUID monitorId;
    private UUID projectId;
    private String monitorName;
    private String url;
    private String previousStatus;
    private String currentStatus;
    private Long responseTimeMs;
    private Instant checkedAt;
    private String errorMessage;
}
