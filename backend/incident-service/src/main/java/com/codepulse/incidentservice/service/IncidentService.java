package com.codepulse.incidentservice.service;

import java.util.List;
import java.util.UUID;

import com.codepulse.incidentservice.dto.IncidentRequest;
import com.codepulse.incidentservice.dto.IncidentResponse;

public interface IncidentService {

    IncidentResponse processStatusChange(IncidentRequest request);

    List<IncidentResponse> getProjectIncidents(UUID projectId);

    List<IncidentResponse> getRecentIncidents();

    IncidentResponse getIncident(UUID incidentId);
}
