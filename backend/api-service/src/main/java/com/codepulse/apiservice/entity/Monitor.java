package com.codepulse.apiservice.entity;

import java.util.UUID;

import com.codepulse.apiservice.entity.base.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Monitor entity containing URL monitoring configuration.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "monitors")
public class Monitor extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "url", nullable = false, length = 2048)
    private String url;

    @Column(name = "http_method", nullable = false, length = 10)
    private String httpMethod = "GET";

    @Column(name = "timeout_ms", nullable = false)
    private Integer timeoutMs = 30000;

    @Column(name = "expected_status_code", nullable = false)
    private Integer expectedStatusCode = 200;

    @Column(name = "active", nullable = false)
    private Boolean active = true;
}