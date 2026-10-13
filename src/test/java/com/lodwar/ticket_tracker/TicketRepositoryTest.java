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
 * Persistence slice test over the Testcontainers Postgres. Verifies the entity
 * maps to the Flyway schema, the enum converters store the lowercase wire values
 * (the DB CHECK would reject anything else), and auditing populates timestamps.
 *
 * <p>Replace.NONE keeps the container datasource (from AbstractIntegrationTest)
 * instead of swapping in an in-memory DB.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TicketRepositoryTest extends AbstractIntegrationTest {

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
