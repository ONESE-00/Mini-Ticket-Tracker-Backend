package com.lodwar.ticket_tracker.repository;

import com.lodwar.ticket_tracker.domain.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

/**
 * {@link JpaRepository} provides CRUD + paging/sorting out of the box.
 * {@link JpaSpecificationExecutor} lets the list endpoint (Step 5) compose
 * dynamic status/priority/search filters without hand-written queries.
 */
public interface TicketRepository
        extends JpaRepository<Ticket, UUID>, JpaSpecificationExecutor<Ticket> {

    /**
     * Count tickets grouped by status. Returns rows of [status, count] computed
     * in the DB (no entities loaded) — the service folds these into TicketStats.
     */
    @Query("select t.status, count(t) from Ticket t group by t.status")
    List<Object[]> countByStatus();

    /** Count tickets grouped by priority. Returns rows of [priority, count]. */
    @Query("select t.priority, count(t) from Ticket t group by t.priority")
    List<Object[]> countByPriority();
}
