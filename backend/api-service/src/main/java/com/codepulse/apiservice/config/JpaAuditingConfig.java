package com.codepulse.apiservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables JPA auditing for automatic population of @CreatedDate and @LastModifiedDate.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}