package com.lodwar.ticket_tracker.web;

import com.lodwar.ticket_tracker.domain.Ticket;
import com.lodwar.ticket_tracker.service.TicketService;
import com.lodwar.ticket_tracker.web.dto.CreateTicketRequest;
import com.lodwar.ticket_tracker.web.dto.TicketMapper;
import com.lodwar.ticket_tracker.web.dto.TicketResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

    /** GET /api/v1/tickets/{id} — single ticket, 404 if missing. */
    @GetMapping("/{id}")
    public TicketResponse getById(@PathVariable UUID id) {
        return TicketMapper.toResponse(service.getById(id));
    }
}
