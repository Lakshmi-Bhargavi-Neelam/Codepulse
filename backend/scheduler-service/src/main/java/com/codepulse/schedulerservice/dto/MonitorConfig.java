package com.codepulse.schedulerservice.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight monitor projection returned by api-service's internal
 * "active monitors" endpoint and forwarded as-is to worker-service to
 * trigger a check. Kept intentionally minimal — scheduler-service has no
 * database and does not persist or enrich this data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitorConfig {

    @NotNull(message = "Monitor ID is required")
    private UUID id;
    
    @NotNull(message = "Project ID is required")
    private UUID projectId;
    
    @NotBlank(message = "Monitor name is required")
    private String name;
    
    @NotBlank(message = "URL is required")
    private String url;
    
    private String httpMethod;
    
    @Positive(message = "Timeout must be positive")
    private Integer timeoutMs;
    
    @Positive(message = "Expected status code must be positive")
    private Integer expectedStatusCode;
    
    private Boolean active;
}
