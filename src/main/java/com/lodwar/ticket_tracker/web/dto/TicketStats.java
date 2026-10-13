package com.lodwar.ticket_tracker.web.dto;

/**
 * Aggregate counts for the landing-page stat cards, matching the frontend
 * contract: status counts (open/in_progress/closed) plus total, and a priority
 * breakdown (low/medium/high). The JSON key {@code in_progress} comes from the
 * record component name, so it matches the wire contract exactly.
 */
public record TicketStats(
        long open,
        long in_progress,
        long closed,
        long total,
        long low,
        long medium,
        long high
) {
}
