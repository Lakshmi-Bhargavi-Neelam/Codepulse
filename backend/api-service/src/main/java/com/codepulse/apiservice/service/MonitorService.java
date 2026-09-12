package com.codepulse.apiservice.service;

import java.util.List;

import com.codepulse.apiservice.dto.MonitorConfigResponse;

/**
 * Service interface for monitor operations.
 */
public interface MonitorService {

    /**
     * Get all active monitors across all projects.
     */
    List<MonitorConfigResponse> getAllActiveMonitors();
}