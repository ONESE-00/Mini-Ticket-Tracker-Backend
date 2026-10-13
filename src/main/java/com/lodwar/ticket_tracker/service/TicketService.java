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
import com.lodwar.ticket_tracker.web.dto.UpdateTicketRequest;
import com.lodwar.ticket_tracker.web.error.ConflictException;
import com.lodwar.ticket_tracker.web.error.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
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
     * Partially update a ticket. Only non-null fields in the request are applied.
     * A status change is validated against the forward-only state machine (same
     * status is a no-op; an illegal jump throws ConflictException -> 409). A
     * history row is written per changed field in the same transaction, and
     * updatedAt is bumped by auditing when anything changed.
     */
    @Transactional
    public Ticket update(UUID id, UpdateTicketRequest req) {
        Ticket ticket = getById(id);
        OffsetDateTime now = OffsetDateTime.now();
        List<TicketHistory> changes = new ArrayList<>();

        // Status: validate transition first, record a status_changed entry.
        if (req.status() != null && req.status() != ticket.getStatus()) {
            if (!ticket.getStatus().canTransitionTo(req.status())) {
                throw new ConflictException("Cannot move a ticket from "
                        + ticket.getStatus().getValue() + " to " + req.status().getValue() + ".");
            }
            changes.add(historyEntry(id, HistoryConstants.ACTION_STATUS_CHANGED,
                    HistoryConstants.FIELD_STATUS,
                    ticket.getStatus().getValue(), req.status().getValue(), now));
            ticket.setStatus(req.status());
        }

        // Title
        if (req.title() != null && !req.title().trim().equals(ticket.getTitle())) {
            String newTitle = req.title().trim();
            changes.add(historyEntry(id, HistoryConstants.ACTION_UPDATED,
                    HistoryConstants.FIELD_TITLE, ticket.getTitle(), newTitle, now));
            ticket.setTitle(newTitle);
        }

        // Description (empty string normalizes to null)
        if (req.description() != null) {
            String newDesc = normalize(req.description());
            if (!java.util.Objects.equals(newDesc, ticket.getDescription())) {
                changes.add(historyEntry(id, HistoryConstants.ACTION_UPDATED,
                        HistoryConstants.FIELD_DESCRIPTION, ticket.getDescription(), newDesc, now));
                ticket.setDescription(newDesc);
            }
        }

        // Priority
        if (req.priority() != null && req.priority() != ticket.getPriority()) {
            changes.add(historyEntry(id, HistoryConstants.ACTION_UPDATED,
                    HistoryConstants.FIELD_PRIORITY,
                    ticket.getPriority().getValue(), req.priority().getValue(), now));
            ticket.setPriority(req.priority());
        }

        if (!changes.isEmpty()) {
            history.saveAll(changes);
        }
        // updatedAt is bumped by @LastModifiedDate auditing when the entity is dirty.
        return tickets.saveAndFlush(ticket);
    }

    private TicketHistory historyEntry(UUID ticketId, String action, String field,
                                       String from, String to, OffsetDateTime at) {
        return new TicketHistory(UUID.randomUUID(), ticketId, action, field, from, to, at);
    }

    /**
     * Audit trail for a ticket, newest first. Verifies the ticket exists first so
     * a missing id returns 404 rather than an empty list.
     */
    @Transactional(readOnly = true)
    public List<TicketHistory> history(UUID id) {
        if (!tickets.existsById(id)) {
            throw new NotFoundException("That ticket no longer exists.");
        }
        return history.findByTicketIdOrderByChangedAtDesc(id);
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
