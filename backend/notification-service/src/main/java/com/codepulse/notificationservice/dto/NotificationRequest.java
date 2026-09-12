package com.codepulse.notificationservice.dto;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request received from incident-service on POST /internal/notifications/send. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    @NotNull(message = "Incident ID is required")
    private UUID incidentId;
    
    @NotNull(message = "Project ID is required")
    private UUID projectId;
    
    @NotNull(message = "Monitor ID is required")
    private UUID monitorId;
    
    @NotBlank(message = "Monitor name is required")
    private String monitorName;
    
    @NotBlank(message = "URL is required")
    private String url;
    
    @NotBlank(message = "Incident status is required")
    private String incidentStatus;
    
    @NotNull(message = "Occurred at timestamp is required")
    private Instant occurredAt;
}
