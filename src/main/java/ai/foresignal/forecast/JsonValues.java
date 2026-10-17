package ai.foresignal.forecast;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.JsonNode;

/** Converts JSON trees to plain Java maps, lists and scalars, and reads date-times (prompt 122). */
public final class JsonValues {

    private JsonValues() {}

    /** An object node as an ordered map (empty for anything else). */
    public static Map<String, Object> map(JsonNode node) {
        Object value = java(node);
        if (value instanceof Map<?, ?> m) {
            Map<String, Object> out = new LinkedHashMap<>();
            m.forEach((k, v) -> out.put(String.valueOf(k), v));
            return out;
        }
        return new LinkedHashMap<>();
    }

    /** A node as plain Java: maps, lists, strings, booleans, longs (integral numbers), doubles or null. */
    public static Object java(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }
        if (node.isObject()) {
            Map<String, Object> out = new LinkedHashMap<>();
            for (String field : node.propertyNames()) {
                out.put(field, java(node.get(field)));
            }
            return out;
        }
        if (node.isArray()) {
            List<Object> out = new ArrayList<>();
            for (JsonNode item : node) {
                out.add(java(item));
            }
            return out;
        }
        if (node.isBoolean()) {
            return node.asBoolean();
        }
        if (node.isIntegralNumber()) {
            return node.asLong();
        }
        if (node.isNumber()) {
            return node.asDouble();
        }
        return node.asString();
    }

    /** Parses an ISO-8601 date-time with {@code Z} or an offset. */
    public static Instant instant(String text) {
        try {
            return Instant.parse(text);
        } catch (RuntimeException e) {
            return OffsetDateTime.parse(text).toInstant();
        }
    }

    /** A map member as text, or null. */
    public static String text(Map<String, Object> map, String key) {
        Object v = map == null ? null : map.get(key);
        return v == null ? null : v.toString();
    }

    /** A map member as a number, or null. */
    public static Double number(Map<String, Object> map, String key) {
        Object v = map == null ? null : map.get(key);
        return v instanceof Number n ? n.doubleValue() : null;
    }

    /** A map member as a map, or null. */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> child(Map<String, Object> map, String key) {
        Object v = map == null ? null : map.get(key);
        return v instanceof Map<?, ?> m ? (Map<String, Object>) m : null;
    }

    /** A map member as a list of strings (empty when absent). */
    public static List<String> strings(Map<String, Object> map, String key) {
        Object v = map == null ? null : map.get(key);
        List<String> out = new ArrayList<>();
        if (v instanceof List<?> list) {
            for (Object o : list) {
                if (o != null) {
                    out.add(o.toString());
                }
            }
        }
        return out;
    }
}
