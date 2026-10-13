package com.lodwar.ticket_tracker.service;

import com.lodwar.ticket_tracker.domain.HistoryConstants;
import com.lodwar.ticket_tracker.domain.Ticket;
import com.lodwar.ticket_tracker.domain.TicketHistory;
import com.lodwar.ticket_tracker.domain.TicketPriority;
import com.lodwar.ticket_tracker.domain.TicketStatus;
import com.lodwar.ticket_tracker.repository.TicketHistoryRepository;
import com.lodwar.ticket_tracker.repository.TicketRepository;
import com.lodwar.ticket_tracker.web.dto.CreateTicketRequest;
import com.lodwar.ticket_tracker.web.dto.TicketStats;
import com.lodwar.ticket_tracker.web.error.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
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

    /**
     * List tickets with optional status/priority/search filters, paginated and
     * sorted per the Pageable. Returns a Spring Page so the controller can emit
     * the native Page JSON the frontend expects.
     */
    @Transactional(readOnly = true)
    public Page<Ticket> search(TicketStatus status, TicketPriority priority,
                               String search, Pageable pageable) {
        Specification<Ticket> spec = Specification.allOf(
                TicketSpecifications.hasStatus(status),
                TicketSpecifications.hasPriority(priority),
                TicketSpecifications.matchesSearch(search)
        );
        return tickets.findAll(spec, pageable);
    }

    /**
     * Aggregate counts for the stat cards: per-status, per-priority and total.
     * Counts are computed in the DB via grouped queries; this method folds the
     * [enum, count] rows into the TicketStats shape (missing buckets stay 0).
     */
    @Transactional(readOnly = true)
    public TicketStats stats() {
        EnumMap<TicketStatus, Long> byStatus = new EnumMap<>(TicketStatus.class);
        for (Object[] row : tickets.countByStatus()) {
            byStatus.put((TicketStatus) row[0], (Long) row[1]);
        }
        EnumMap<TicketPriority, Long> byPriority = new EnumMap<>(TicketPriority.class);
        for (Object[] row : tickets.countByPriority()) {
            byPriority.put((TicketPriority) row[0], (Long) row[1]);
        }

        long open = byStatus.getOrDefault(TicketStatus.OPEN, 0L);
        long inProgress = byStatus.getOrDefault(TicketStatus.IN_PROGRESS, 0L);
        long closed = byStatus.getOrDefault(TicketStatus.CLOSED, 0L);

        return new TicketStats(
                open, inProgress, closed,
                open + inProgress + closed,
                byPriority.getOrDefault(TicketPriority.LOW, 0L),
                byPriority.getOrDefault(TicketPriority.MEDIUM, 0L),
                byPriority.getOrDefault(TicketPriority.HIGH, 0L)
        );
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
