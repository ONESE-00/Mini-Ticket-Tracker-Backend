package com.lodwar.ticket_tracker;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base for integration tests. Starts a single, shared PostgreSQL container for
 * the whole suite (static field + @Container) and wires Spring Boot's datasource
 * to it via @ServiceConnection — so Flyway runs the real migrations against a
 * throwaway, production-like database. Requires Docker to be running.
 */
@Testcontainers
public abstract class AbstractIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:18-alpine");
}
