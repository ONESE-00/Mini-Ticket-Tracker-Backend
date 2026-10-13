package com.lodwar.ticket_tracker.web.error;

/**
 * Thrown when a request conflicts with the resource's current state — here, an
 * illegal status transition (e.g. open -> closed). The global handler maps it to
 * a 409 ApiError with error code "InvalidTransition".
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
