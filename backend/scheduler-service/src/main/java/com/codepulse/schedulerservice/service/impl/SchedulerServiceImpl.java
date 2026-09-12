package com.codepulse.schedulerservice.service.impl;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.codepulse.schedulerservice.client.ApiServiceClient;
import com.codepulse.schedulerservice.client.WorkerServiceClient;
import com.codepulse.schedulerservice.dto.MonitorConfig;
import com.codepulse.schedulerservice.dto.TriggerSummaryResponse;
import com.codepulse.schedulerservice.exception.WorkerServiceException;
import com.codepulse.schedulerservice.service.SchedulerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Fetches active monitors from api-service and triggers a health check on
 * worker-service for each one, counting successes/failures along the way.
 * Stateless — nothing here is persisted; the summary is returned to the
 * caller (the scheduler tick or a manual /internal/trigger request) and
 * only ever logged, never stored.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerServiceImpl implements SchedulerService {

    private final ApiServiceClient apiServiceClient;
    private final WorkerServiceClient workerServiceClient;

    @Override
    public TriggerSummaryResponse triggerHealthChecks() {
        log.info("Scheduler run started");

        List<MonitorConfig> monitors = apiServiceClient.fetchActiveMonitors();
        log.info("Fetched {} monitor(s) from api-service", monitors.size());

        int successCount = 0;
        int failureCount = 0;

        for (MonitorConfig monitor : monitors) {
            if (monitor.getActive() == null || !monitor.getActive()) {
                log.debug("Skipping inactive monitor {}", monitor.getId());
                continue;
            }

            try {
                workerServiceClient.triggerCheck(monitor);
                successCount++;
                log.info("Triggered health check for monitor {} ({})", monitor.getId(), monitor.getName());
            } catch (WorkerServiceException ex) {
                failureCount++;
                log.warn("Failed to trigger health check for monitor {} ({}): {}",
                        monitor.getId(), monitor.getName(), ex.getMessage());
            }
        }

        TriggerSummaryResponse summary = TriggerSummaryResponse.builder()
                .totalMonitors(monitors.size())
                .successfulTriggers(successCount)
                .failedTriggers(failureCount)
                .triggeredAt(Instant.now())
                .build();

        log.info("Scheduler run finished: total={}, successful={}, failed={}",
                summary.getTotalMonitors(), summary.getSuccessfulTriggers(), summary.getFailedTriggers());

        return summary;
    }
}
