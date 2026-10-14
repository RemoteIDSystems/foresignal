package ai.foresignal.source.adapters.humanitarian;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Tolerant timestamp parsing for the prompt 090 humanitarian sources: an ISO offset, a zoned instant, an
 * offset-less ISO date-time (read as UTC), or a space-separated {@code yyyy-MM-dd HH:mm:ss} (HANS) read as UTC.
 */
public final class Instants {

    private static final DateTimeFormatter SPACE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Instants() {}

    /** The instant of {@code value}, or null when it cannot be read. */
    public static Instant parse(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String text = value.strip();
        try {
            return OffsetDateTime.parse(text).toInstant();
        } catch (DateTimeParseException ignored) {
            // try the next form
        }
        try {
            return Instant.parse(text);
        } catch (DateTimeParseException ignored) {
            // try the next form
        }
        try {
            return LocalDateTime.parse(text).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException ignored) {
            // try the next form
        }
        try {
            return LocalDateTime.parse(text, SPACE).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }
}
