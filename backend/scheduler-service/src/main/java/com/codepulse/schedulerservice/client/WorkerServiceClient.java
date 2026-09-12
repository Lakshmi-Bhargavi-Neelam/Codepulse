package com.codepulse.schedulerservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.codepulse.schedulerservice.dto.MonitorConfig;
import com.codepulse.schedulerservice.exception.WorkerServiceException;

/**
 * REST client for worker-service's internal check-trigger endpoint.
 * Base URL comes from configuration (worker-service.base-url).
 * No authentication — internal service-to-service call.
 */
@Component
public class WorkerServiceClient {

    private final RestClient restClient;

    public WorkerServiceClient(RestClient.Builder restClientBuilder,
                                @Value("${worker-service.base-url}") String baseUrl) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    /**
     * POST /internal/check
     * Triggers an immediate health check for the given monitor.
     */
    public void triggerCheck(MonitorConfig monitor) {
        try {
            restClient.post()
                    .uri("/internal/check")
                    .body(monitor)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            throw new WorkerServiceException(
                    "Failed to trigger health check on worker-service for monitor " + monitor.getId(), ex);
        }
    }
}
