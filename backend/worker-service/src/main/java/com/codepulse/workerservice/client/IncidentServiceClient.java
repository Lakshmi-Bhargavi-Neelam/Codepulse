package com.codepulse.workerservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.codepulse.workerservice.dto.IncidentRequest;
import com.codepulse.workerservice.exception.IncidentServiceException;

import lombok.extern.slf4j.Slf4j;

/**
 * REST client for incident-service's status-change endpoint.
 * Base URL comes from configuration (incident-service.base-url).
 * No authentication — internal service-to-service call.
 */
@Slf4j
@Component
public class IncidentServiceClient {

    private final RestClient restClient;

    public IncidentServiceClient(RestClient.Builder restClientBuilder,
                                  @Value("${incident-service.base-url}") String baseUrl) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    /**
     * POST /internal/incidents/status-change
     * Notifies incident-service that a monitor flipped UP <-> DOWN.
     */
    public void notifyStatusChange(IncidentRequest request) {
        try {
            restClient.post()
                    .uri("/internal/incidents/status-change")
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            throw new IncidentServiceException(
                    "Failed to notify incident-service of status change for monitor " + request.getMonitorId(), ex);
        }
    }
}
