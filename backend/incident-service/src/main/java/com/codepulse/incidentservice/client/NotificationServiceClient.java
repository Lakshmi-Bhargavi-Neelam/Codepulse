package com.codepulse.incidentservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.codepulse.incidentservice.dto.NotificationRequest;
import com.codepulse.incidentservice.exception.NotificationServiceException;

import lombok.extern.slf4j.Slf4j;

/**
 * REST client for notification-service's send endpoint.
 * Base URL comes from configuration (notification-service.base-url).
 * No authentication — internal service-to-service call.
 */
@Slf4j
@Component
public class NotificationServiceClient {

    private final RestClient restClient;

    public NotificationServiceClient(RestClient.Builder restClientBuilder,
                                      @Value("${notification-service.base-url}") String baseUrl) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    /**
     * POST /internal/notifications/send
     * Notifies notification-service that an incident was created or resolved.
     */
    public void sendNotification(NotificationRequest request) {
        try {
            restClient.post()
                    .uri("/internal/notifications/send")
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Notification sent successfully for incident {}", request.getIncidentId());
        } catch (RestClientException ex) {
            log.error("Notification failure for incident {}: {}", request.getIncidentId(), ex.getMessage());
            throw new NotificationServiceException(
                    "Failed to notify notification-service for incident " + request.getIncidentId(), ex);
        }
    }
}
