package com.codepulse.schedulerservice.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of one scheduling run — either the automatic 60-second tick or a
 * manual POST /internal/trigger. Returned by SchedulerService.triggerHealthChecks().
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TriggerSummaryResponse {

    private Integer totalMonitors;
    private Integer successfulTriggers;
    private Integer failedTriggers;
    private Instant triggeredAt;
}
