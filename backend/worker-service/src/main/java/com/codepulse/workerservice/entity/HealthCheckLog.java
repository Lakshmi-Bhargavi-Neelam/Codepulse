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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Append-only log of every health check execution for a monitor.
 * Backs GET /internal/health/history/{monitorId}.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "health_check_log")
public class HealthCheckLog extends BaseEntity {

    @Column(name = "monitor_id", nullable = false)
    private UUID monitorId;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

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
