package com.lodwar.ticket_tracker.service;

import com.lodwar.ticket_tracker.domain.Ticket;
import com.lodwar.ticket_tracker.domain.TicketPriority;
import com.lodwar.ticket_tracker.domain.TicketStatus;
import org.springframework.data.jpa.domain.Specification;

/**
 * Composable JPA Specifications for the list endpoint's optional filters.
 * Each returns null when its argument is absent, so the service can chain them
 * with {@code Specification.allOf(...)} and only the present filters apply.
 */
public final class TicketSpecifications {

    private TicketSpecifications() {
    }

    public static Specification<Ticket> hasStatus(TicketStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Ticket> hasPriority(TicketPriority priority) {
        if (priority == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("priority"), priority);
    }

    /** Case-insensitive match on title OR description. */
    public static Specification<Ticket> matchesSearch(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        String like = "%" + search.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), like),
                cb.like(cb.lower(root.get("description")), like)
        );
    }
}
