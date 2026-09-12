package com.codepulse.apiservice.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Internal DTO returned by /api/v1/internal/monitors/active endpoint.
 * This matches exactly what scheduler-service expects in MonitorConfig.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitorConfigResponse {

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