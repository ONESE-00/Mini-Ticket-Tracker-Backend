package com.lodwar.ticket_tracker.web.dto;

import com.lodwar.ticket_tracker.domain.Ticket;
import com.lodwar.ticket_tracker.domain.TicketHistory;

/**
 * Maps domain entities to response DTOs. Plain static mapping — no MapStruct:
 * with a single small DTO the hand-written mapper is clearer and adds no
 * dependency. Kept out of the entity so the domain stays free of web concerns.
 */
public final class TicketMapper {

    private TicketMapper() {
    }

    public static TicketResponse toResponse(Ticket t) {
        return new TicketResponse(
                t.getId(),
                t.getTitle(),
                t.getDescription(),
                t.getStatus(),
                t.getPriority(),
                t.getCreatedAt(),
                t.getUpdatedAt()
        );
    }

    public static TicketHistoryResponse toResponse(TicketHistory h) {
        return new TicketHistoryResponse(
                h.getId(),
                h.getTicketId(),
                h.getAction(),
                h.getField(),
                h.getFromValue(),
                h.getToValue(),
                h.getChangedAt()
        );
    }
}
