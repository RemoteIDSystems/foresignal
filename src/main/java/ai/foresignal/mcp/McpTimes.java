package ai.foresignal.mcp;

import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.settings.Durations;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import tools.jackson.databind.JsonNode;

/**
 * Time inputs of the time-travel tools (prompt 037): an RFC 3339 instant
 * ({@code 2026-10-12T08:00:00Z}), {@code now}, or a duration back from now, ISO-8601
 * ({@code -PT2H}, {@code -P1D}) or shorthand ({@code -2h}, {@code -30m}); the leading minus is
 * optional, since a relative time always counts back.
 */
public final class McpTimes {

    private McpTimes() {}

    /** The instant of argument {@code name} of {@code args}, or {@code defaultValue} when absent. */
    public static Instant instant(JsonNode args, String name, Instant now, Instant defaultValue) {
        String value = McpArgs.text(args, name);
        if (value == null) {
            return defaultValue;
        }
        return parse(name, value, now);
    }

    /** {@code value} as an instant relative to {@code now}. */
    public static Instant parse(String name, String value, Instant now) {
        String text = value.strip();
        if (text.equalsIgnoreCase("now")) {
            return now;
        }
        try {
            return Instant.parse(text);
        } catch (DateTimeParseException e) {
            // try the other forms
        }
        try {
            return OffsetDateTime.parse(text).toInstant();
        } catch (DateTimeParseException e) {
            // try a relative duration
        }
        String relative = text.startsWith("-") ? text.substring(1) : text;
        try {
            Duration back = Durations.parse(relative);
            if (back.isNegative()) {
                throw new McpToolException(message(name, value));
            }
            return now.minus(back);
        } catch (InvalidInputException | DateTimeParseException e) {
            throw new McpToolException(message(name, value));
        }
    }

    private static String message(String name, String value) {
        return "The argument '" + name + "' must be an RFC 3339 time such as 2026-10-12T08:00:00Z, 'now', or a"
                + " duration back from now such as -PT2H or -2h; got '" + value + "'.";
    }
}
