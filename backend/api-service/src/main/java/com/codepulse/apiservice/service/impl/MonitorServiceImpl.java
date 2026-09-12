package com.codepulse.apiservice.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codepulse.apiservice.dto.MonitorConfigResponse;
import com.codepulse.apiservice.entity.Monitor;
import com.codepulse.apiservice.mapper.MonitorMapper;
import com.codepulse.apiservice.repository.MonitorRepository;
import com.codepulse.apiservice.service.MonitorService;

import lombok.RequiredArgsConstructor;

/**
 * Implementation of MonitorService.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MonitorServiceImpl implements MonitorService {

    private final MonitorRepository monitorRepository;
    private final MonitorMapper monitorMapper;

    @Override
    public List<MonitorConfigResponse> getAllActiveMonitors() {
        List<Monitor> monitors = monitorRepository.findAllActiveMonitors();
        return monitors.stream()
                .map(monitorMapper::toMonitorConfigResponse)
                .toList();
    }
}