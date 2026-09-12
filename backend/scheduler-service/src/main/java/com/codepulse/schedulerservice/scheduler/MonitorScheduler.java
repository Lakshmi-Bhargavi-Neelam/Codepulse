package com.codepulse.schedulerservice.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.codepulse.schedulerservice.dto.TriggerSummaryResponse;
import com.codepulse.schedulerservice.service.SchedulerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Fires every `scheduler.fixed-delay` milliseconds (default 60s) and asks
 * SchedulerService to run one full trigger cycle. Contains no orchestration
 * logic itself — just the timing trigger and a log of the outcome.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MonitorScheduler {

    private final SchedulerService schedulerService;

    @Scheduled(fixedDelayString = "${scheduler.fixed-delay}")
    public void runScheduledHealthChecks() {
        log.info("Scheduler tick fired");
        TriggerSummaryResponse summary = schedulerService.triggerHealthChecks();
        log.info("Scheduled run summary: {}", summary);
    }
}
