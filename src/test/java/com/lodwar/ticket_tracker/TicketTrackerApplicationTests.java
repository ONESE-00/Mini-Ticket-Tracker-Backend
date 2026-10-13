package com.lodwar.ticket_tracker;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke test: the full application context boots against a real Postgres
 * (Testcontainers) with Flyway applied.
 */
@SpringBootTest
class TicketTrackerApplicationTests extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
    }
}
