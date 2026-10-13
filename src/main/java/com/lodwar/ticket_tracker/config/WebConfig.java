package com.lodwar.ticket_tracker.config;

import com.lodwar.ticket_tracker.domain.TicketPriority;
import com.lodwar.ticket_tracker.domain.TicketStatus;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Binds query-string values to our enums by their lowercase wire value
 * (e.g. {@code ?status=open}, {@code ?priority=high}).
 *
 * <p>{@code @JsonValue}/{@code @JsonCreator} only affect JSON body
 * (de)serialization; Spring MVC converts {@code @RequestParam} enums by the
 * constant name ({@code OPEN}) by default. These converters route param binding
 * through {@code fromValue(...)} so the wire contract spelling works on filters.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToTicketStatusConverter());
        registry.addConverter(new StringToTicketPriorityConverter());
    }

    static class StringToTicketStatusConverter implements Converter<String, TicketStatus> {
        @Override
        public TicketStatus convert(String source) {
            return TicketStatus.fromValue(source);
        }
    }

    static class StringToTicketPriorityConverter implements Converter<String, TicketPriority> {
        @Override
        public TicketPriority convert(String source) {
            return TicketPriority.fromValue(source);
        }
    }
}
