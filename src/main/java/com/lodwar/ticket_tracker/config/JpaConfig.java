package com.lodwar.ticket_tracker.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables Spring Data JPA auditing so {@code @CreatedDate} and
 * {@code @LastModifiedDate} fields are populated automatically on save.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
