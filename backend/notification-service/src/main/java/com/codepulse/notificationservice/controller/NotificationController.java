package com.codepulse.notificationservice.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codepulse.notificationservice.dto.NotificationRequest;
import com.codepulse.notificationservice.dto.NotificationResponse;
import com.codepulse.notificationservice.entity.NotificationDelivery;
import com.codepulse.notificationservice.service.NotificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Internal API surface for notification-service. /send is called only by
 * incident-service; /history/{projectId} is a read endpoint for delivery logs.
 */
@RestController
@RequestMapping("/internal/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Email dispatch and delivery-history endpoints")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    @Operation(summary = "Send incident alert emails to a project's active channels (called by incident-service)")
    public ResponseEntity<NotificationResponse> send(@Valid @RequestBody NotificationRequest request) {
        NotificationResponse response = notificationService.sendNotifications(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/{projectId}")
    @Operation(summary = "Get the last 20 notification delivery records for a project")
    public ResponseEntity<List<NotificationDelivery>> history(@PathVariable UUID projectId) {
        return ResponseEntity.ok(notificationService.getDeliveryHistory(projectId));
    }
}
