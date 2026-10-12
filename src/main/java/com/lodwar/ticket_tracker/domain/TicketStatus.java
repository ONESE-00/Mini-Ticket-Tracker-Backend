package com.lodwar.ticket_tracker.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Ticket lifecycle status. The wire/DB value is the lowercase string
 * ({@code open}, {@code in_progress}, {@code closed}) — never the enum constant
 * name or ordinal.
 *
 * <p>{@code @JsonValue} controls JSON output, {@code @JsonCreator} controls JSON
 * input, and the DB stores the same string via a dedicated AttributeConverter,
 * so all three layers (JSON, Java, SQL) agree on the exact contract spelling.
 */
public enum TicketStatus {
    OPEN("open"),
    IN_PROGRESS("in_progress"),
    CLOSED("closed");

    private final String value;

    TicketStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static TicketStatus fromValue(String value) {
        for (TicketStatus s : values()) {
            if (s.value.equals(value)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + value);
    }
}
