package com.lodwar.ticket_tracker.domain;

/**
 * Centralized literals for ticket_history {@code action} and {@code field}
 * values. These must match the DB CHECK constraints in V1__init.sql exactly.
 * Kept as constants (not scattered string literals) so there is a single source
 * of truth shared by the service and any history-writing code.
 */
public final class HistoryConstants {

    private HistoryConstants() {
    }

    // action values
    public static final String ACTION_CREATED = "created";
    public static final String ACTION_UPDATED = "updated";
    public static final String ACTION_STATUS_CHANGED = "status_changed";

    // field values
    public static final String FIELD_TITLE = "title";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_PRIORITY = "priority";
    public static final String FIELD_STATUS = "status";
}
