package com.lodwar.ticket_tracker.web.dto;

import com.lodwar.ticket_tracker.domain.TicketPriority;
import com.lodwar.ticket_tracker.domain.TicketStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Response shape for a single ticket, matching the frontend contract:
 * {@code {id, title, description, status, priority, createdAt, updatedAt}}.
 *
 * <p>Enums serialize to their lowercase wire values via @JsonValue; timestamps
 * serialize as ISO-8601 strings. Field names map 1:1 to the TypeScript model.
 */
public record TicketResponse(
        UUID id,
        String title,
        String description,
        TicketStatus status,
        TicketPriority priority,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
