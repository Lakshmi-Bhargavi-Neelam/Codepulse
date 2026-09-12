package com.codepulse.incidentservice.entity;

import java.time.Instant;
import java.util.UUID;

import com.codepulse.incidentservice.entity.base.BaseEntity;
import com.codepulse.incidentservice.entity.enums.IncidentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A single incident lifecycle for a monitor. At most one OPEN incident may
 * exist per monitor at a time (enforced in IncidentServiceImpl by looking
 * up findByMonitorIdAndStatus(monitorId, OPEN) before creating a new one).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "incident")
public class Incident extends BaseEntity {

    @Column(name = "monitor_id", nullable = false)
    private UUID monitorId;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "monitor_name", nullable = false)
    private String monitorName;

    @Column(name = "url", nullable = false)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private IncidentStatus status;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "duration_seconds")
    private Long durationSeconds;

    @Column(name = "failure_count", nullable = false)
    private Integer failureCount;

    @Column(name = "last_error_message")
    private String lastErrorMessage;
}
