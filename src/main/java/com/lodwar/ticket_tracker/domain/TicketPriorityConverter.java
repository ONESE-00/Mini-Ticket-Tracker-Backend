package com.lodwar.ticket_tracker.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Persists {@link TicketPriority} as its lowercase wire value, matching the DB
 * CHECK constraint. {@code autoApply = true} applies it to every TicketPriority field.
 */
@Converter(autoApply = true)
public class TicketPriorityConverter implements AttributeConverter<TicketPriority, String> {

    @Override
    public String convertToDatabaseColumn(TicketPriority priority) {
        return priority == null ? null : priority.getValue();
    }

    @Override
    public TicketPriority convertToEntityAttribute(String dbValue) {
        return dbValue == null ? null : TicketPriority.fromValue(dbValue);
    }
}
