package com.codepulse.schedulerservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Turns on Spring's @Scheduled support so MonitorScheduler's fixed-delay job runs. */
@Configuration
@EnableScheduling
public class SchedulerConfig {
}
