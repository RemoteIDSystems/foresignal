package ai.foresignal.source.support;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import tools.jackson.databind.JsonNode;

/**
 * Typed reads of one member of a JSON object (prompt 075), for the pure feature mappers: every getter returns null
 * for a missing, null, blank or unreadable value, never a default.
 */
public final class JsonProps {

    private JsonProps() {}

    /**
     * The member as trimmed text; numbers and booleans are given as text.
     *
     * @param object the JSON object, or null
     * @param field the member name
     * @return the text, or null when missing, null or blank after trimming
     */
    public static String text(JsonNode object, String field) {
        JsonNode v = member(object, field);
        if (v == null || !(v.isString() || v.isNumber() || v.isBoolean())) {
            return null;
        }
        String s = v.asString().strip();
        return s.isEmpty() ? null : s;
    }

    /**
     * The member as a finite number; numeric text is read too.
     *
     * @param object the JSON object, or null
     * @param field the member name
     */
    public static Double number(JsonNode object, String field) {
        JsonNode v = member(object, field);
        if (v == null) {
            return null;
        }
        double d;
        if (v.isNumber()) {
            d = v.doubleValue();
        } else if (v.isString()) {
            try {
                d = Double.parseDouble(v.stringValue().strip());
            } catch (NumberFormatException e) {
                return null;
            }
        } else {
            return null;
        }
        return Double.isFinite(d) ? d : null;
    }

    /**
     * The member as an integer, rounding a fractional value to the nearest whole number.
     *
     * @param object the JSON object, or null
     * @param field the member name
     */
    public static Long integer(JsonNode object, String field) {
        Double d = number(object, field);
        return d == null ? null : Math.round(d);
    }

    /**
     * The member as an instant from epoch milliseconds (ArcGIS date fields).
     *
     * @param object the JSON object, or null
     * @param field the member name
     */
    public static Instant epochMillis(JsonNode object, String field) {
        JsonNode v = member(object, field);
        return v != null && v.isNumber() ? Instant.ofEpochMilli(v.longValue()) : null;
    }

    /**
     * The member as an instant from ISO-8601 text with an offset ({@code 2026-10-13T12:00:00Z}).
     *
     * @param object the JSON object, or null
     * @param field the member name
     */
    public static Instant iso(JsonNode object, String field) {
        String s = text(object, field);
        if (s == null) {
            return null;
        }
        try {
            return OffsetDateTime.parse(s).toInstant();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static JsonNode member(JsonNode object, String field) {
        if (object == null || !object.isObject()) {
            return null;
        }
        JsonNode v = object.get(field);
        return v == null || v.isNull() || v.isMissingNode() ? null : v;
    }
}
