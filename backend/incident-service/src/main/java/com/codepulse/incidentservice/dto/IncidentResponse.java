package com.codepulse.incidentservice.dto;

import java.time.Instant;
import java.util.UUID;

import com.codepulse.incidentservice.entity.enums.IncidentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response body returned by all incident read/write endpoints. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentResponse {

    private UUID id;
    private UUID monitorId;
    private UUID projectId;
    private String monitorName;
    private String url;
    private IncidentStatus status;
    private Instant startedAt;
    private Instant resolvedAt;
    private Long durationSeconds;
    private Integer failureCount;
    private String lastErrorMessage;
}
