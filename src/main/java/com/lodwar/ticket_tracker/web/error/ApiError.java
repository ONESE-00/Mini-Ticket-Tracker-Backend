package com.lodwar.ticket_tracker.web.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * Error response body shared by all failures, matching the frontend contract:
 * {@code {status, error, message, fieldErrors?}}.
 *
 * <p>{@code fieldErrors} is omitted from JSON when null (NON_NULL) so non-validation
 * errors don't carry an empty key. The frontend reads {@code message} for the
 * toast and {@code fieldErrors} to place messages on form controls.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        int status,
        String error,
        String message,
        Map<String, String> fieldErrors
) {

    public static ApiError of(int status, String error, String message) {
        return new ApiError(status, error, message, null);
    }

    public static ApiError of(int status, String error, String message, Map<String, String> fieldErrors) {
        return new ApiError(status, error, message, fieldErrors);
    }
}
