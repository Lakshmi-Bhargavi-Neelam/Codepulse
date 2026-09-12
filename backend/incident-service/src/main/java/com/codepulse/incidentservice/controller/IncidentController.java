package com.codepulse.incidentservice.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codepulse.incidentservice.dto.IncidentRequest;
import com.codepulse.incidentservice.dto.IncidentResponse;
import com.codepulse.incidentservice.service.IncidentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Internal API surface for incident-service. /status-change is called only
 * by worker-service; the remaining endpoints are read APIs for incidents.
 */
@RestController
@RequestMapping("/internal/incidents")
@RequiredArgsConstructor
@Tag(name = "Incidents", description = "Incident lifecycle processing and read endpoints")
public class IncidentController {

    private final IncidentService incidentService;

    @PostMapping("/status-change")
    @Operation(summary = "Process a monitor status change (called by worker-service)")
    public ResponseEntity<IncidentResponse> statusChange(@Valid @RequestBody IncidentRequest request) {
        IncidentResponse response = incidentService.processStatusChange(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "List all incidents for a project, most recent first")
    public ResponseEntity<List<IncidentResponse>> byProject(@PathVariable UUID projectId) {
        return ResponseEntity.ok(incidentService.getProjectIncidents(projectId));
    }

    @GetMapping("/recent")
    @Operation(summary = "List the 20 most recent incidents across all projects")
    public ResponseEntity<List<IncidentResponse>> recent() {
        return ResponseEntity.ok(incidentService.getRecentIncidents());
    }

    @GetMapping("/{incidentId}")
    @Operation(summary = "Get a single incident's details")
    public ResponseEntity<IncidentResponse> byId(@PathVariable UUID incidentId) {
        return ResponseEntity.ok(incidentService.getIncident(incidentId));
    }
}
