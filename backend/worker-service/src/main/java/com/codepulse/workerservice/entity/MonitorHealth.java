package com.codepulse.workerservice.entity;

import java.time.Instant;
import java.util.UUID;

import com.codepulse.workerservice.entity.base.BaseEntity;
import com.codepulse.workerservice.entity.enums.HealthStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The latest known status of a single monitor — one row per monitorId,
 * overwritten on every check. Backs GET /internal/health/latest/{monitorId}.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "monitor_health",
        uniqueConstraints = @UniqueConstraint(name = "uq_monitor_health_monitor_id", columnNames = "monitor_id")
)
public class MonitorHealth extends BaseEntity {

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
    private HealthStatus status;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    @Column(name = "checked_at", nullable = false)
    private Instant checkedAt;

    @Column(name = "error_message")
    private String errorMessage;
}
