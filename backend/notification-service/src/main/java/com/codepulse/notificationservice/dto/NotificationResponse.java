package com.codepulse.notificationservice.dto;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response returned by POST /internal/notifications/send. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private UUID incidentId;
    private Integer totalRecipients;
    private Integer successfulDeliveries;
    private Integer failedDeliveries;
    private Instant processedAt;
}
