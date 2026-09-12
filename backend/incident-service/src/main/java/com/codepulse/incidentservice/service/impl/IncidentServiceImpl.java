package com.codepulse.incidentservice.service.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codepulse.incidentservice.client.NotificationServiceClient;
import com.codepulse.incidentservice.dto.IncidentRequest;
import com.codepulse.incidentservice.dto.IncidentResponse;
import com.codepulse.incidentservice.dto.NotificationRequest;
import com.codepulse.incidentservice.entity.Incident;
import com.codepulse.incidentservice.entity.enums.IncidentStatus;
import com.codepulse.incidentservice.exception.IncidentProcessingException;
import com.codepulse.incidentservice.exception.ResourceNotFoundException;
import com.codepulse.incidentservice.mapper.IncidentMapper;
import com.codepulse.incidentservice.repository.IncidentRepository;
import com.codepulse.incidentservice.service.IncidentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implements the incident lifecycle state machine described by worker-service
 * status-change events: UP-&gt;DOWN opens (or updates) an incident,
 * DOWN-&gt;UP resolves it, and notification-service is notified exactly once
 * per open/resolve transition (never on a repeated-failure update).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepository;
    private final IncidentMapper incidentMapper;
    private final NotificationServiceClient notificationServiceClient;

    @Override
    @Transactional
    public IncidentResponse processStatusChange(IncidentRequest request) {
        log.info("Status change received for monitor {}: {} -> {}",
                request.getMonitorId(), request.getPreviousStatus(), request.getCurrentStatus());

        String currentStatus = request.getCurrentStatus();

        if ("DOWN".equalsIgnoreCase(currentStatus)) {
            return handleMonitorDown(request);
        } else if ("UP".equalsIgnoreCase(currentStatus)) {
            return handleMonitorUp(request);
        } else {
            throw new IncidentProcessingException("Unrecognized currentStatus: " + currentStatus);
        }
    }

    /** Case 1 — UP -> DOWN: open a new incident, or bump the failure count on an already-open one. */
    private IncidentResponse handleMonitorDown(IncidentRequest request) {
        Optional<Incident> existingOpen =
                incidentRepository.findByMonitorIdAndStatus(request.getMonitorId(), IncidentStatus.OPEN);

        if (existingOpen.isEmpty()) {
            Incident incident = incidentMapper.toEntity(request);
            incident.setStatus(IncidentStatus.OPEN);
            incident.setStartedAt(request.getCheckedAt());
            incident.setFailureCount(1);
            incident.setLastErrorMessage(request.getErrorMessage());

            Incident saved = incidentRepository.save(incident);
            log.info("Incident created for monitor {} (incidentId={})", request.getMonitorId(), saved.getId());

            notify(saved, "OPEN");
            return incidentMapper.toResponse(saved);
        }

        Incident incident = existingOpen.get();
        incident.setFailureCount(incident.getFailureCount() + 1);
        incident.setLastErrorMessage(request.getErrorMessage());

        Incident saved = incidentRepository.save(incident);
        log.info("Incident {} failure count incremented to {}", saved.getId(), saved.getFailureCount());

        return incidentMapper.toResponse(saved);
    }

    /** Case 2 — DOWN -> UP: resolve the open incident, if one exists. */
    private IncidentResponse handleMonitorUp(IncidentRequest request) {
        Optional<Incident> existingOpen =
                incidentRepository.findByMonitorIdAndStatus(request.getMonitorId(), IncidentStatus.OPEN);

        if (existingOpen.isEmpty()) {
            log.info("Received UP status for monitor {} with no OPEN incident — nothing to resolve",
                    request.getMonitorId());
            return null;
        }

        Incident incident = existingOpen.get();
        incident.setStatus(IncidentStatus.RESOLVED);
        incident.setResolvedAt(request.getCheckedAt());
        incident.setDurationSeconds(Duration.between(incident.getStartedAt(), request.getCheckedAt()).getSeconds());

        Incident saved = incidentRepository.save(incident);
        log.info("Incident {} resolved for monitor {} (duration={}s)",
                saved.getId(), request.getMonitorId(), saved.getDurationSeconds());

        notify(saved, "RESOLVED");
        return incidentMapper.toResponse(saved);
    }

    private void notify(Incident incident, String incidentStatus) {
        NotificationRequest notificationRequest = NotificationRequest.builder()
                .incidentId(incident.getId())
                .projectId(incident.getProjectId())
                .monitorId(incident.getMonitorId())
                .monitorName(incident.getMonitorName())
                .url(incident.getUrl())
                .incidentStatus(incidentStatus)
                .occurredAt(Instant.now())
                .build();

        notificationServiceClient.sendNotification(notificationRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidentResponse> getProjectIncidents(UUID projectId) {
        return incidentRepository.findByProjectIdOrderByStartedAtDesc(projectId).stream()
                .map(incidentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidentResponse> getRecentIncidents() {
        return incidentRepository.findTop20ByOrderByStartedAtDesc().stream()
                .map(incidentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public IncidentResponse getIncident(UUID incidentId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found: " + incidentId));
        return incidentMapper.toResponse(incident);
    }
}
