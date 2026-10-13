package com.lodwar.ticket_tracker.web;

import com.lodwar.ticket_tracker.domain.Ticket;
import com.lodwar.ticket_tracker.domain.TicketPriority;
import com.lodwar.ticket_tracker.domain.TicketStatus;
import com.lodwar.ticket_tracker.service.TicketService;
import com.lodwar.ticket_tracker.web.dto.CreateTicketRequest;
import com.lodwar.ticket_tracker.web.dto.TicketMapper;
import com.lodwar.ticket_tracker.web.dto.TicketResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST endpoints for tickets, under the contract base path /api/v1/tickets.
 * The controller is thin: validate input, delegate to the service, map the
 * entity to a response DTO. Errors flow to the global handler.
 */
@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketService service;

    public TicketController(TicketService service) {
        this.service = service;
    }

    /** POST /api/v1/tickets — create a ticket (always starts 'open'). 201 Created. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse create(@Valid @RequestBody CreateTicketRequest request) {
        Ticket created = service.create(request);
        return TicketMapper.toResponse(created);
    }

    /**
     * GET /api/v1/tickets — filtered, sorted, paginated list.
     * Spring binds page/size/sort from the query string into {@code Pageable};
     * status/priority/search are optional. Returns a native Spring Page so the
     * JSON matches the shape the frontend adapts (content, number, size, ...).
     * Defaults to newest-first when no sort is supplied.
     */
    @GetMapping
    public Page<TicketResponse> list(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return service.search(status, priority, search, pageable)
                .map(TicketMapper::toResponse);
    }

    /** GET /api/v1/tickets/{id} — single ticket, 404 if missing. */
    @GetMapping("/{id}")
    public TicketResponse getById(@PathVariable UUID id) {
        return TicketMapper.toResponse(service.getById(id));
    }
}
