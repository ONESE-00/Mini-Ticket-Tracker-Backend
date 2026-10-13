package com.lodwar.ticket_tracker.web.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Translates exceptions into the contract's ApiError JSON. Centralizing error
 * handling here keeps controllers and services free of try/catch and
 * ResponseEntity error plumbing.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Missing resource -> 404. */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException ex) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status)
                .body(ApiError.of(status.value(), "NotFound", ex.getMessage()));
    }

    /** Illegal state transition (forward-only state machine) -> 409. */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException ex) {
        HttpStatus status = HttpStatus.CONFLICT;
        return ResponseEntity.status(status)
                .body(ApiError.of(status.value(), "InvalidTransition", ex.getMessage()));
    }

    /**
     * Bean-validation failures on @Valid request bodies -> 400 with per-field
     * messages, so the frontend can place them on the matching form controls.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (var error : ex.getBindingResult().getFieldErrors()) {
            // keep the first message per field
            fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(ApiError.of(status.value(), "ValidationError",
                        "Please check the form and try again.", fieldErrors));
    }

    /**
     * Malformed or unreadable request body -> 400. Covers invalid enum values
     * (e.g. priority "highj"), bad JSON, and type mismatches that fail during
     * deserialization before bean validation runs. These are client errors, not
     * server errors, so they must not surface as 500.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(ApiError.of(status.value(), "ValidationError",
                        "Request body is invalid or malformed."));
    }

    /**
     * Invalid path/query parameter value -> 400. E.g. a bad enum filter
     * ({@code ?status=bogus}) or a malformed UUID in the path.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(ApiError.of(status.value(), "ValidationError",
                        "Invalid value for parameter '" + ex.getName() + "'."));
    }

    /** Last-resort fallback -> 500, without leaking internals to the client. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        // Log the full cause server-side; the client only sees a generic message.
        log.error("Unhandled exception -> 500", ex);
        return ResponseEntity.status(status)
                .body(ApiError.of(status.value(), "InternalError",
                        "Something went wrong. Please try again."));
    }
}
