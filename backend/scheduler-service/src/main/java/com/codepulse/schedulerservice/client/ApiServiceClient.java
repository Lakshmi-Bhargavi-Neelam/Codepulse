package com.codepulse.schedulerservice.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.codepulse.schedulerservice.dto.MonitorConfig;
import com.codepulse.schedulerservice.exception.ApiServiceException;

import lombok.extern.slf4j.Slf4j;

/**
 * REST client for api-service's internal monitor-listing endpoint.
 * Base URL comes from configuration (api-service.base-url).
 */
@Slf4j
@Component
public class ApiServiceClient {

    private final RestClient restClient;

    public ApiServiceClient(RestClient.Builder restClientBuilder,
                             @Value("${api-service.base-url}") String baseUrl) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    /**
     * GET /api/v1/internal/monitors/active
     * Returns every currently-active monitor across all projects.
     */
    public List<MonitorConfig> fetchActiveMonitors() {
        try {
            List<MonitorConfig> monitors = restClient.get()
                    .uri("/api/v1/internal/monitors/active")
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<List<MonitorConfig>>() {});

            return monitors != null ? monitors : List.of();
        } catch (RestClientException ex) {
            throw new ApiServiceException("Failed to fetch active monitors from api-service", ex);
        }
    }
}
