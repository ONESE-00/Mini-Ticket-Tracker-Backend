package com.lodwar.ticket_tracker.web.dto;

import com.lodwar.ticket_tracker.domain.TicketPriority;
import com.lodwar.ticket_tracker.domain.TicketStatus;
import jakarta.validation.constraints.Size;

/**
 * Partial update body for PATCH /api/v1/tickets/{id}. Every field is optional:
 * a {@code null} field means "leave unchanged" (the frontend sends only changed
 * fields). When {@code title} is present it is still size-constrained.
 *
 * <p>Note the deliberate semantics tradeoff: because this is a record, we cannot
 * distinguish "absent" from "explicit null" in JSON, so null is treated as
 * "unchanged". Clearing a description to empty is done by sending an empty
 * string, which the service normalizes to null.
 */
public record UpdateTicketRequest(

        @Size(max = 200, message = "Title must be 200 characters or fewer.")
        String title,

        String description,

        TicketStatus status,

        TicketPriority priority

) {
}
