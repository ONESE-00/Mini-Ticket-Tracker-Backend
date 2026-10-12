package com.lodwar.ticket_tracker.repository;

import com.lodwar.ticket_tracker.domain.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

/**
 * {@link JpaRepository} provides CRUD + paging/sorting out of the box.
 * {@link JpaSpecificationExecutor} lets the list endpoint (Step 5) compose
 * dynamic status/priority/search filters without hand-written queries.
 */
public interface TicketRepository
        extends JpaRepository<Ticket, UUID>, JpaSpecificationExecutor<Ticket> {
}
