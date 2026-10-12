package com.lodwar.ticket_tracker.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Persists {@link TicketStatus} as its lowercase wire value (e.g. {@code in_progress}),
 * matching the DB CHECK constraint — not the enum constant name ({@code IN_PROGRESS}).
 *
 * <p>{@code autoApply = true} means every TicketStatus field is converted without a
 * per-field {@code @Convert} annotation.
 */
@Converter(autoApply = true)
public class TicketStatusConverter implements AttributeConverter<TicketStatus, String> {

    @Override
    public String convertToDatabaseColumn(TicketStatus status) {
        return status == null ? null : status.getValue();
    }

    @Override
    public TicketStatus convertToEntityAttribute(String dbValue) {
        return dbValue == null ? null : TicketStatus.fromValue(dbValue);
    }
}
