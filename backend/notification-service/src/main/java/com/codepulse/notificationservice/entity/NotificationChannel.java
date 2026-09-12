package com.codepulse.notificationservice.entity;

import java.util.UUID;

import com.codepulse.notificationservice.entity.base.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * An email destination configured for a project. Only email channels are
 * supported in this MVP — `destination` is always an email address.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "notification_channel")
public class NotificationChannel extends BaseEntity {

    @NotNull(message = "Project ID is required")
    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @NotBlank(message = "Destination email is required")
    @Email(message = "Destination must be a valid email address")
    @Column(name = "destination", nullable = false)
    private String destination;

    @NotNull(message = "Active status is required")
    @Column(name = "active", nullable = false)
    private Boolean active;
}
