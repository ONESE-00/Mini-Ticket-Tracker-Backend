package com.lodwar.ticket_tracker.service;

import com.lodwar.ticket_tracker.domain.HistoryConstants;
import com.lodwar.ticket_tracker.domain.Ticket;
import com.lodwar.ticket_tracker.domain.TicketHistory;
import com.lodwar.ticket_tracker.domain.TicketStatus;
import com.lodwar.ticket_tracker.repository.TicketHistoryRepository;
import com.lodwar.ticket_tracker.repository.TicketRepository;
import com.lodwar.ticket_tracker.web.dto.CreateTicketRequest;
import com.lodwar.ticket_tracker.web.error.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Ticket business logic. Owns id generation, the forced-open rule on create, and
 * the audit trail. Write methods are {@code @Transactional} so a ticket and its
 * history row commit (or roll back) together — there is never a change without
 * its audit entry.
 */
@Service
public class TicketService {

    private final TicketRepository tickets;
    private final TicketHistoryRepository history;

    public TicketService(TicketRepository tickets, TicketHistoryRepository history) {
        this.tickets = tickets;
        this.history = history;
    }

    /**
     * Create a ticket. Status is forced to {@code open} regardless of input, and
     * a {@code created} history row is written in the same transaction.
     */
    @Transactional
    public Ticket create(CreateTicketRequest req) {
        UUID id = UUID.randomUUID();
        String description = normalize(req.description());

        Ticket ticket = new Ticket(id, req.title().trim(), description,
                TicketStatus.OPEN, req.priority());
        // saveAndFlush forces the persist now so JPA auditing populates
        // createdAt/updatedAt immediately; use the returned managed instance
        // (save may return a different, managed copy than the one passed in).
        Ticket saved = tickets.saveAndFlush(ticket);

        // changedAt mirrors the ticket's createdAt so the "created" audit row
        // lines up with the ticket's own timestamp.
        history.save(new TicketHistory(
                UUID.randomUUID(), saved.getId(),
                HistoryConstants.ACTION_CREATED, null, null, null,
                saved.getCreatedAt()
        ));

        return saved;
    }

    /** Fetch a ticket or throw NotFoundException (-> 404). */
    @Transactional(readOnly = true)
    public Ticket getById(UUID id) {
        return tickets.findById(id)
                .orElseThrow(() -> new NotFoundException("That ticket no longer exists."));
    }

    /** Trim and collapse an empty/blank description to null. */
    private static String normalize(String description) {
        if (description == null) {
            return null;
        }
        String trimmed = description.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
