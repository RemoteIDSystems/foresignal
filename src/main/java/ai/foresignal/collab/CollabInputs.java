package ai.foresignal.collab;

import ai.foresignal.api.error.ValidationException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import tools.jackson.databind.JsonNode;

/** Request parsing shared by the collaboration services (prompt 130). */
public final class CollabInputs {

    /** A {@code #rrggbb} color. */
    public static final Pattern COLOR = Pattern.compile("^#[0-9a-fA-F]{6}$");

    private CollabInputs() {}

    /**
     * Requires an object body whose members are all in {@code allowed}.
     *
     * @param body the body
     * @param schema the schema name, for messages
     * @param allowed the members it may have
     * @return the body
     */
    public static JsonNode object(JsonNode body, String schema, Set<String> allowed) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be a " + schema + " object.");
        }
        for (String field : body.propertyNames()) {
            if (!allowed.contains(field)) {
                throw ValidationException.forField(field, "is not a " + schema + " member");
            }
        }
        return body;
    }

    /** A string member, or null when absent or null; {@code min}/{@code max} bound its length. */
    public static String text(JsonNode body, String field, int min, int max) {
        JsonNode n = body == null ? null : body.get(field);
        if (n == null || n.isNull()) {
            return null;
        }
        if (!n.isString()) {
            throw ValidationException.forField(field, "must be a string");
        }
        String s = n.asString();
        if (s.strip().length() < min || s.length() > max) {
            throw ValidationException.forField(field, "must be " + min + " to " + max + " characters");
        }
        return s;
    }

    /** A required string member. */
    public static String required(JsonNode body, String field, int min, int max) {
        String s = text(body, field, min, max);
        if (s == null) {
            throw ValidationException.forField(field, "is required");
        }
        return s;
    }

    /** A boolean member, or {@code fallback}. */
    public static boolean bool(JsonNode body, String field, boolean fallback) {
        JsonNode n = body == null ? null : body.get(field);
        if (n == null || n.isNull()) {
            return fallback;
        }
        if (!n.isBoolean()) {
            throw ValidationException.forField(field, "must be a boolean");
        }
        return n.asBoolean();
    }

    /** An RFC 3339 instant member, or null. */
    public static Instant instant(JsonNode body, String field) {
        String s = text(body, field, 1, 64);
        if (s == null) {
            return null;
        }
        try {
            return Instant.parse(s);
        } catch (DateTimeParseException e) {
            throw ValidationException.forField(field, "must be an RFC 3339 date-time");
        }
    }

    /** An array of strings, at most {@code max} long; empty when absent. */
    public static List<String> strings(JsonNode body, String field, int max) {
        JsonNode n = body == null ? null : body.get(field);
        List<String> out = new ArrayList<>();
        if (n == null || n.isNull()) {
            return out;
        }
        if (!n.isArray()) {
            throw ValidationException.forField(field, "must be an array of strings");
        }
        for (JsonNode item : n) {
            if (!item.isString() || item.asString().isBlank()) {
                throw ValidationException.forField(field, "must hold non-empty strings");
            }
            if (!out.contains(item.asString().strip())) {
                out.add(item.asString().strip());
            }
        }
        if (out.size() > max) {
            throw ValidationException.forField(field, "holds more than " + max + " items");
        }
        return out;
    }

    /** True when the body has {@code field}, even as null (merge patches). */
    public static boolean has(JsonNode body, String field) {
        return body != null && body.has(field);
    }

    /** An opaque offset cursor. */
    public static String cursor(int offset) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(("o:" + offset).getBytes(
                StandardCharsets.UTF_8));
    }

    /**
     * The offset of an opaque cursor (0 for null).
     *
     * @throws ValidationException {@code INVALID_CURSOR} for a malformed one
     */
    public static int offset(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return 0;
        }
        try {
            String s = new String(Base64.getUrlDecoder().decode(cursor.strip()), StandardCharsets.UTF_8);
            if (!s.startsWith("o:")) {
                throw new IllegalArgumentException(s);
            }
            int offset = Integer.parseInt(s.substring(2));
            if (offset < 0) {
                throw new IllegalArgumentException(s);
            }
            return offset;
        } catch (IllegalArgumentException e) {
            throw new ValidationException(ai.foresignal.api.error.ErrorCodes.INVALID_CURSOR, "The cursor is not valid.",
                    List.of(new ai.foresignal.api.error.FieldError("cursor", "is not a cursor this endpoint issued")));
        }
    }
}
