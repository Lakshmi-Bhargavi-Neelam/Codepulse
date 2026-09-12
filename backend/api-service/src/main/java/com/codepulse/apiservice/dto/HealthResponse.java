package com.codepulse.apiservice.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Simple health check response DTO. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthResponse {

    private String service;
    private String status;
    private Instant timestamp;
}