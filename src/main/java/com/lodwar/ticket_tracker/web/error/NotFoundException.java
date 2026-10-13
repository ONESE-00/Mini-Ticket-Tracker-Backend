package com.lodwar.ticket_tracker.web.error;

/**
 * Thrown when a requested resource does not exist. The global exception handler
 * translates it into a 404 ApiError, keeping controllers free of error plumbing.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
