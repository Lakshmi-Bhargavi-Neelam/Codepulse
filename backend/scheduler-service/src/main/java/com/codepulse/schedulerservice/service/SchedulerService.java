package com.codepulse.schedulerservice.service;

import com.codepulse.schedulerservice.dto.TriggerSummaryResponse;

/** Orchestrates one scheduling run: fetch active monitors, trigger a check for each. */
public interface SchedulerService {

    TriggerSummaryResponse triggerHealthChecks();
}
