package com.lodwar.ticket_tracker.web.dto;

import com.lodwar.ticket_tracker.domain.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request body for POST /api/v1/tickets.
 *
 * <p>Status is intentionally absent: new tickets always start {@code open}
 * (set by the service), so the client cannot choose it. Validation is enforced
 * by {@code @Valid} in the controller; the global handler turns failures into a
 * 400 ApiError with per-field messages (Step 4).
 */
public record CreateTicketRequest(

        @NotBlank(message = "Title is required.")
        @Size(max = 200, message = "Title must be 200 characters or fewer.")
        String title,

        String description,

        @NotNull(message = "Priority is required.")
        TicketPriority priority

) {
}
