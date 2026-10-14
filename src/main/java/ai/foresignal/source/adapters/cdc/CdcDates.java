package ai.foresignal.source.adapters.cdc;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import tools.jackson.databind.JsonNode;

/**
 * CDC Socrata timestamp parsing (prompt 095). SODA floating timestamps carry no zone ("2025-09-12T00:00:00.000"),
 * so they are read as UTC, like every other CDC date. A bare date is read at UTC midnight.
 */
final class CdcDates {

    private static final DateTimeFormatter[] DATE_TIMES = {
        DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSS"),
        DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss"),
        DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss"),
    };

    private CdcDates() {}

    /** The instant of a JSON value, or null when it is absent or unreadable. */
    static Instant instant(JsonNode node) {
        String value = CdcText.text(node);
        return instant(value);
    }

    /** The instant of a SODA date or date-time string, or null. */
    static Instant instant(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String text = value.strip();
        if (text.length() == 10) {
            try {
                return LocalDate.parse(text).atStartOfDay(ZoneOffset.UTC).toInstant();
            } catch (DateTimeParseException e) {
                return null;
            }
        }
        for (DateTimeFormatter formatter : DATE_TIMES) {
            try {
                return LocalDateTime.parse(text, formatter).toInstant(ZoneOffset.UTC);
            } catch (DateTimeParseException ignored) {
                // try the next pattern
            }
        }
        try {
            return Instant.parse(text);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
