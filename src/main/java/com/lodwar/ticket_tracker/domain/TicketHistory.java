package com.lodwar.ticket_tracker.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * An immutable audit row recording one change to a ticket. Maps to the
 * {@code ticket_history} table.
 *
 * <p>{@code action} is {@code created | updated | status_changed} and
 * {@code field} is {@code title | description | priority | status | null}
 * (both enforced by DB CHECK constraints). The DB columns {@code from_value}/
 * {@code to_value} are exposed to the API as {@code from}/{@code to} by the
 * response DTO. {@code changedAt} is set explicitly by the service so a history
 * row's timestamp matches the ticket's {@code updatedAt}.
 */
@Entity
@Table(name = "ticket_history")
public class TicketHistory {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "ticket_id", nullable = false)
    private UUID ticketId;

    @Column(nullable = false, length = 20)
    private String action;

    @Column(length = 20)
    private String field;

    @Column(name = "from_value", columnDefinition = "text")
    private String fromValue;

    @Column(name = "to_value", columnDefinition = "text")
    private String toValue;

    @Column(name = "changed_at", nullable = false)
    private OffsetDateTime changedAt;

    protected TicketHistory() {
        // JPA requires a no-arg constructor; not for application use.
    }

    public TicketHistory(UUID id, UUID ticketId, String action, String field,
                         String fromValue, String toValue, OffsetDateTime changedAt) {
        this.id = id;
        this.ticketId = ticketId;
        this.action = action;
        this.field = field;
        this.fromValue = fromValue;
        this.toValue = toValue;
        this.changedAt = changedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTicketId() {
        return ticketId;
    }

    public String getAction() {
        return action;
    }

    public String getField() {
        return field;
    }

    public String getFromValue() {
        return fromValue;
    }

    public String getToValue() {
        return toValue;
    }

    public OffsetDateTime getChangedAt() {
        return changedAt;
    }
}
