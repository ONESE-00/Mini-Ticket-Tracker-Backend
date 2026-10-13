package com.lodwar.ticket_tracker.web.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * One audit entry in the response, matching the frontend contract:
 * {@code {id, ticketId, action, field, from, to, changedAt}}.
 *
 * <p>The entity stores the values in columns {@code from_value}/{@code to_value}
 * (since {@code from}/{@code to} are awkward SQL identifiers); the mapper renames
 * them to the wire keys {@code from}/{@code to} here.
 */
public record TicketHistoryResponse(
        UUID id,
        UUID ticketId,
        String action,
        String field,
        String from,
        String to,
        OffsetDateTime changedAt
) {
}
