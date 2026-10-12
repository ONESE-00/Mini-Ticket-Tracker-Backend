package com.lodwar.ticket_tracker.repository;

import com.lodwar.ticket_tracker.domain.TicketHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TicketHistoryRepository extends JpaRepository<TicketHistory, UUID> {

    /**
     * History for one ticket, newest first. Spring Data derives the query from
     * the method name (WHERE ticket_id = ? ORDER BY changed_at DESC) — serves
     * the GET /tickets/{id}/history contract directly.
     */
    List<TicketHistory> findByTicketIdOrderByChangedAtDesc(UUID ticketId);
}
