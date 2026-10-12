package ai.foresignal.inbound.format;

import ai.foresignal.api.error.InvalidInputException;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import tools.jackson.databind.JsonNode;

/**
 * Timestamp parsing shared by the ingest formats (prompt 024).
 *
 * <p>Accepts ISO-8601 instants ({@code 2026-10-12T08:00:00Z}), offset date-times
 * ({@code 2026-10-12T10:00:00+02:00}) and, from JSON numbers, epoch milliseconds. Anything else is
 * the caller's mistake and is reported as {@link InvalidInputException}.
 */
public final class IngestTimes {

    private IngestTimes() {}

    /**
     * Parses a timestamp node.
     *
     * @return the instant, or null when the node is absent, missing or JSON null
     * @throws InvalidInputException when the value is not a timestamp
     */
    public static Instant parse(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }
        if (node.isNumber()) {
            if (!node.canConvertToLong()) {
                throw new InvalidInputException("Timestamp " + node + " is not an integer of epoch milliseconds");
            }
            return Instant.ofEpochMilli(node.longValue());
        }
        if (node.isString()) {
            return parse(node.asString());
        }
        throw new InvalidInputException("Timestamp must be a string or a number, but got " + node.getNodeType());
    }

    /**
     * Parses an ISO-8601 instant or offset date-time string.
     *
     * @return the instant, or null when the text is null or blank
     * @throws InvalidInputException when the text is not a timestamp
     */
    public static Instant parse(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String trimmed = text.strip();
        try {
            return Instant.parse(trimmed);
        } catch (DateTimeException ignored) {
            // Fall through to the offset forms.
        }
        try {
            return OffsetDateTime.parse(trimmed).toInstant();
        } catch (DateTimeException ignored) {
            // Fall through.
        }
        try {
            return ZonedDateTime.parse(trimmed, DateTimeFormatter.ISO_ZONED_DATE_TIME).toInstant();
        } catch (DateTimeException e) {
            throw new InvalidInputException("'" + abbreviate(trimmed) + "' is not an ISO-8601 timestamp", e);
        }
    }

    /**
     * Parses epoch seconds or milliseconds from a node holding a number or a numeric string.
     *
     * @param millis true for milliseconds, false for seconds
     * @return the instant, or null when absent
     * @throws InvalidInputException when the value is not numeric
     */
    public static Instant parseEpoch(JsonNode node, boolean millis) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }
        double value;
        if (node.isNumber()) {
            value = node.doubleValue();
        } else if (node.isString()) {
            try {
                value = Double.parseDouble(node.asString().strip());
            } catch (NumberFormatException e) {
                throw new InvalidInputException("'" + abbreviate(node.asString()) + "' is not an epoch number", e);
            }
        } else {
            throw new InvalidInputException("Epoch timestamp must be a number, but got " + node.getNodeType());
        }
        if (!Double.isFinite(value)) {
            throw new InvalidInputException("Epoch timestamp is not finite");
        }
        long ms = millis ? (long) value : (long) (value * 1000d);
        return Instant.ofEpochMilli(ms);
    }

    private static String abbreviate(String text) {
        return text.length() <= 64 ? text : text.substring(0, 64) + "…";
    }
}
