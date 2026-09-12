package com.codepulse.incidentservice.dto;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Sent to notification-service's POST /internal/notifications/send when an incident opens or resolves. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    private UUID incidentId;
    private UUID projectId;
    private UUID monitorId;
    private String monitorName;
    private String url;
    private String incidentStatus;
    private Instant occurredAt;
}
