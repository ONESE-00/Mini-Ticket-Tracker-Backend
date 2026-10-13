package com.lodwar.ticket_tracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * Enables Spring Data JPA auditing so {@code @CreatedDate} / {@code @LastModifiedDate}
 * are populated automatically on save.
 *
 * <p>Our audit fields are {@link OffsetDateTime} (to match the TIMESTAMPTZ columns
 * and the ISO-8601-with-offset wire contract). Spring Data's default date provider
 * yields a {@code LocalDateTime}, which its converter cannot turn into an
 * {@code OffsetDateTime}. We therefore register a custom {@link DateTimeProvider}
 * that supplies {@code OffsetDateTime} directly, and point auditing at it via
 * {@code dateTimeProviderRef}.
 */
@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
public class JpaConfig {

    @Bean
    public DateTimeProvider auditingDateTimeProvider() {
        return () -> Optional.of(OffsetDateTime.now());
    }
}
