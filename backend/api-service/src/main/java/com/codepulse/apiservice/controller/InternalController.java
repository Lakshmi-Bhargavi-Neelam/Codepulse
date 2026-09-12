package com.codepulse.apiservice.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codepulse.apiservice.dto.MonitorConfigResponse;
import com.codepulse.apiservice.service.MonitorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * Internal API endpoints for inter-service communication.
 */
@RestController
@RequestMapping("/api/v1/internal")
@RequiredArgsConstructor
@Tag(name = "Internal", description = "Internal endpoints for service-to-service communication")
public class InternalController {

    private final MonitorService monitorService;

    @GetMapping("/monitors/active")
    @Operation(summary = "Get all active monitors (called by scheduler-service)")
    public ResponseEntity<List<MonitorConfigResponse>> getActiveMonitors() {
        List<MonitorConfigResponse> monitors = monitorService.getAllActiveMonitors();
        return ResponseEntity.ok(monitors);
    }
}