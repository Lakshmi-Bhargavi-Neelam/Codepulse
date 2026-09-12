package com.codepulse.incidentservice.dto;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request received from worker-service on POST /internal/incidents/status-change. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentRequest {

    @NotNull(message = "Monitor ID is required")
    private UUID monitorId;
    
    @NotNull(message = "Project ID is required")
    private UUID projectId;
    
    @NotBlank(message = "Monitor name is required")
    private String monitorName;
    
    @NotBlank(message = "URL is required")
    private String url;
    
    private String previousStatus;
    
    @NotBlank(message = "Current status is required")
    private String currentStatus;
    
    private Long responseTimeMs;
    
    @NotNull(message = "Checked at timestamp is required")
    private Instant checkedAt;
    
    private String errorMessage;
}
