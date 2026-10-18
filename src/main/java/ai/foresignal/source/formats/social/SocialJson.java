package ai.foresignal.source.formats.social;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import tools.jackson.databind.JsonNode;

/**
 * Null-safe JSON reading for the social adapters (prompt 135): text, numbers that arrive as strings (YouTube
 * statistics, Mastodon tag history), instants in RFC 3339 or epoch seconds, and string arrays.
 */
public final class SocialJson {

    /** RFC 3339 with an offset written without a colon ({@code +0000}), as Meta's Graph APIs send timestamps. */
    private static final java.time.format.DateTimeFormatter COMPACT_OFFSET =
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssxx");

    private SocialJson() {}

    /** The text of a member, or null when missing, null or blank. */
    public static String text(JsonNode node, String field) {
        if (node == null || !node.isObject()) {
            return null;
        }
        JsonNode v = node.get(field);
        if (v == null || v.isNull() || v.isMissingNode()) {
            return null;
        }
        String s = v.isString() ? v.asString() : v.toString();
        return s == null || s.isBlank() ? null : s;
    }

    /** The node at a path of member names, or null. */
    public static JsonNode at(JsonNode node, String... path) {
        JsonNode current = node;
        for (String p : path) {
            if (current == null || !current.isObject()) {
                return null;
            }
            current = current.get(p);
        }
        return current == null || current.isNull() || current.isMissingNode() ? null : current;
    }

    /** A whole number from a number or a numeric string, or null. */
    public static Long number(JsonNode node, String field) {
        JsonNode v = node == null ? null : node.get(field);
        if (v == null || v.isNull()) {
            return null;
        }
        if (v.isNumber()) {
            return v.asLong();
        }
        try {
            String s = v.asString();
            return s == null || s.isBlank() ? null : Long.valueOf(s.strip());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** An instant from RFC 3339 text, or null. */
    public static Instant instant(JsonNode node, String field) {
        return instant(text(node, field));
    }

    /** An instant from RFC 3339 text (with or without offset, {@code +00:00} or {@code +0000}) or epoch seconds, or null. */
    public static Instant instant(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String t = text.strip();
        try {
            return OffsetDateTime.parse(t).toInstant();
        } catch (DateTimeParseException e) {
            // try the other forms
        }
        try {
            return Instant.parse(t);
        } catch (DateTimeParseException e) {
            // try the offset without a colon
        }
        try {
            return OffsetDateTime.parse(t, COMPACT_OFFSET).toInstant();
        } catch (DateTimeParseException e) {
            // try epoch seconds
        }
        try {
            double seconds = Double.parseDouble(t);
            return Instant.ofEpochMilli((long) (seconds * 1000));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** The strings of an array member (or of the given array), skipping blanks. */
    public static List<String> strings(JsonNode node) {
        List<String> out = new ArrayList<>();
        if (node == null || !node.isArray()) {
            return out;
        }
        for (JsonNode v : node) {
            if (v != null && !v.isNull()) {
                String s = v.isString() ? v.asString() : v.toString();
                if (s != null && !s.isBlank()) {
                    out.add(s.strip());
                }
            }
        }
        return out;
    }
}
