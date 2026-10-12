package com.lodwar.ticket_tracker.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Ticket priority. Wire/DB value is the lowercase string
 * ({@code low}, {@code medium}, {@code high}).
 */
public enum TicketPriority {
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high");

    private final String value;

    TicketPriority(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static TicketPriority fromValue(String value) {
        for (TicketPriority p : values()) {
            if (p.value.equals(value)) {
                return p;
            }
        }
        throw new IllegalArgumentException("Unknown priority: " + value);
    }
}
