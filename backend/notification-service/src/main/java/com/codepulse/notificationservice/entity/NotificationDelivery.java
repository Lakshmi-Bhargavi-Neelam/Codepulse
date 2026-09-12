package com.codepulse.notificationservice.entity;

import java.time.Instant;
import java.util.UUID;

import com.codepulse.notificationservice.entity.base.BaseEntity;
import com.codepulse.notificationservice.entity.enums.DeliveryStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One row per attempted email delivery. Backs the delivery-history read
 * endpoint (GET /internal/notifications/history/{projectId}).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "notification_delivery")
public class NotificationDelivery extends BaseEntity {

    @Column(name = "incident_id", nullable = false)
    private UUID incidentId;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "monitor_id", nullable = false)
    private UUID monitorId;

    @Column(name = "recipient_email", nullable = false)
    private String recipientEmail;

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private DeliveryStatus status;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    @Column(name = "error_message")
    private String errorMessage;
}
