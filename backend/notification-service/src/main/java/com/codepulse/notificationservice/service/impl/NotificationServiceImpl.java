package com.codepulse.notificationservice.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codepulse.notificationservice.dto.NotificationRequest;
import com.codepulse.notificationservice.dto.NotificationResponse;
import com.codepulse.notificationservice.entity.NotificationChannel;
import com.codepulse.notificationservice.entity.NotificationDelivery;
import com.codepulse.notificationservice.entity.enums.DeliveryStatus;
import com.codepulse.notificationservice.exception.EmailDeliveryException;
import com.codepulse.notificationservice.repository.NotificationChannelRepository;
import com.codepulse.notificationservice.repository.NotificationDeliveryRepository;
import com.codepulse.notificationservice.service.MailService;
import com.codepulse.notificationservice.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Fetches a project's active email channels, sends one alert email per
 * channel, and records one NotificationDelivery row per attempt regardless
 * of outcome.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationChannelRepository notificationChannelRepository;
    private final NotificationDeliveryRepository notificationDeliveryRepository;
    private final MailService mailService;

    @Override
    @Transactional
    public NotificationResponse sendNotifications(NotificationRequest request) {
        log.info("Notification request received for incident {} (project {}, status {})",
                request.getIncidentId(), request.getProjectId(), request.getIncidentStatus());

        List<NotificationChannel> channels =
                notificationChannelRepository.findByProjectIdAndActiveTrue(request.getProjectId());

        log.info("Found {} active recipient(s) for project {}", channels.size(), request.getProjectId());

        if (channels.isEmpty()) {
            return NotificationResponse.builder()
                    .incidentId(request.getIncidentId())
                    .totalRecipients(0)
                    .successfulDeliveries(0)
                    .failedDeliveries(0)
                    .processedAt(Instant.now())
                    .build();
        }

        String subject = buildSubject(request);
        String message = buildMessage(request);

        int successCount = 0;
        int failedCount = 0;

        for (NotificationChannel channel : channels) {
            NotificationDelivery delivery = new NotificationDelivery();
            delivery.setIncidentId(request.getIncidentId());
            delivery.setProjectId(request.getProjectId());
            delivery.setMonitorId(request.getMonitorId());
            delivery.setRecipientEmail(channel.getDestination());
            delivery.setSubject(subject);
            delivery.setMessage(message);
            delivery.setSentAt(Instant.now());

            try {
                mailService.sendEmail(channel.getDestination(), subject, message);
                delivery.setStatus(DeliveryStatus.SENT);
                successCount++;
            } catch (EmailDeliveryException ex) {
                delivery.setStatus(DeliveryStatus.FAILED);
                delivery.setErrorMessage(ex.getMessage());
                failedCount++;
            }

            notificationDeliveryRepository.save(delivery);
            log.info("Delivery history saved for recipient {} (status={})",
                    channel.getDestination(), delivery.getStatus());
        }

        return NotificationResponse.builder()
                .incidentId(request.getIncidentId())
                .totalRecipients(channels.size())
                .successfulDeliveries(successCount)
                .failedDeliveries(failedCount)
                .processedAt(Instant.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDelivery> getDeliveryHistory(UUID projectId) {
        return notificationDeliveryRepository.findTop20ByProjectIdOrderBySentAtDesc(projectId);
    }

    private String buildSubject(NotificationRequest request) {
        if ("DOWN".equalsIgnoreCase(request.getIncidentStatus())
                || "OPEN".equalsIgnoreCase(request.getIncidentStatus())) {
            return "[CodePulse] Monitor DOWN - " + request.getMonitorName();
        }
        if ("UP".equalsIgnoreCase(request.getIncidentStatus())
                || "RESOLVED".equalsIgnoreCase(request.getIncidentStatus())) {
            return "[CodePulse] Monitor RECOVERED - " + request.getMonitorName();
        }
        return "[CodePulse] Monitor status changed - " + request.getMonitorName();
    }

    private String buildMessage(NotificationRequest request) {
        return "Monitor: " + request.getMonitorName() + "\n"
                + "URL: " + request.getUrl() + "\n"
                + "Status: " + request.getIncidentStatus() + "\n"
                + "Occurred at: " + request.getOccurredAt() + "\n"
                + "Incident ID: " + request.getIncidentId();
    }
}
