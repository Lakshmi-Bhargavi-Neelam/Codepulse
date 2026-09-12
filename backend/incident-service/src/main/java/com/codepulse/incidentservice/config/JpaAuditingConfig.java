package com.codepulse.incidentservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** Enables Spring Data JPA auditing so BaseEntity's createdAt/updatedAt are populated automatically. */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
