package com.lodwar.ticket_tracker;

import com.lodwar.ticket_tracker.domain.Ticket;
import com.lodwar.ticket_tracker.domain.TicketPriority;
import com.lodwar.ticket_tracker.domain.TicketStatus;
import com.lodwar.ticket_tracker.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the entity maps to the Flyway-built schema, the enum converters store
 * and read the lowercase wire values (the DB CHECK would reject anything else),
 * and JPA auditing populates the timestamps.
 *
 * <p>{@code Replace.NONE} uses the real configured Postgres instead of an
 * in-memory DB, so Flyway, the converters and TIMESTAMPTZ behave as in production.
 * Each test runs in a transaction that is rolled back, so no rows are left behind.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TicketRepositoryTest {

    @Autowired
    private TicketRepository tickets;

    @Test
    void savesAndReadsBackTicketWithEnumsAndTimestamps() {
        Ticket t = new Ticket(UUID.randomUUID(), "First ticket", "A description",
                TicketStatus.OPEN, TicketPriority.MEDIUM);

        Ticket saved = tickets.saveAndFlush(t);

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();

        Ticket found = tickets.findById(saved.getId()).orElseThrow();
        assertThat(found.getStatus()).isEqualTo(TicketStatus.OPEN);
        assertThat(found.getPriority()).isEqualTo(TicketPriority.MEDIUM);
        assertThat(found.getTitle()).isEqualTo("First ticket");
    }
}
