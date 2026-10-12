package ai.foresignal.mcp;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Small builders for the JSON Schema 2020-12 documents MCP tools declare (prompt 025). Objects are
 * always closed ({@code additionalProperties: false}) and list their required members.
 */
public final class JsonSchemas {

    private JsonSchemas() {}

    /** A closed object schema. */
    public static Map<String, Object> object(Map<String, Object> properties, String... required) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("type", "object");
        out.put("properties", properties);
        if (required.length > 0) {
            out.put("required", List.of(required));
        }
        out.put("additionalProperties", false);
        return out;
    }

    /** An open object schema (for an output whose members vary), with a description. */
    public static Map<String, Object> openObject(String description, Map<String, Object> properties,
            String... required) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("type", "object");
        if (description != null) {
            out.put("description", description);
        }
        out.put("properties", properties);
        if (required.length > 0) {
            out.put("required", List.of(required));
        }
        return out;
    }

    /** An ordered property map: {@code name, schema, name, schema, …}. */
    public static Map<String, Object> props(Object... nameSchemaPairs) {
        if (nameSchemaPairs.length % 2 != 0) {
            throw new IllegalArgumentException("props takes name and schema pairs");
        }
        Map<String, Object> out = new LinkedHashMap<>();
        for (int i = 0; i < nameSchemaPairs.length; i += 2) {
            out.put((String) nameSchemaPairs[i], nameSchemaPairs[i + 1]);
        }
        return out;
    }

    /** A string. */
    public static Map<String, Object> string(String description) {
        return typed("string", description);
    }

    /** A string with a length limit. */
    public static Map<String, Object> string(String description, int minLength, int maxLength) {
        Map<String, Object> out = typed("string", description);
        out.put("minLength", minLength);
        out.put("maxLength", maxLength);
        return out;
    }

    /** An RFC 3339 date-time string. */
    public static Map<String, Object> dateTime(String description) {
        Map<String, Object> out = typed("string", description);
        out.put("format", "date-time");
        return out;
    }

    /** A UUID string. */
    public static Map<String, Object> uuid(String description) {
        Map<String, Object> out = typed("string", description);
        out.put("format", "uuid");
        return out;
    }

    /** A string limited to {@code values}, with a default (null for none). */
    public static Map<String, Object> enumeration(String description, String defaultValue, String... values) {
        Map<String, Object> out = typed("string", description);
        out.put("enum", Arrays.asList(values));
        if (defaultValue != null) {
            out.put("default", defaultValue);
        }
        return out;
    }

    /** An integer in {@code [minimum, maximum]} with a default (null for none). */
    public static Map<String, Object> integer(String description, long minimum, long maximum, Long defaultValue) {
        Map<String, Object> out = typed("integer", description);
        out.put("minimum", minimum);
        out.put("maximum", maximum);
        if (defaultValue != null) {
            out.put("default", defaultValue);
        }
        return out;
    }

    /** A non-negative integer with no upper bound. */
    public static Map<String, Object> nonNegative(String description) {
        Map<String, Object> out = typed("integer", description);
        out.put("minimum", 0);
        return out;
    }

    /** A number in {@code [minimum, maximum]}. */
    public static Map<String, Object> number(String description, double minimum, double maximum) {
        Map<String, Object> out = typed("number", description);
        out.put("minimum", minimum);
        out.put("maximum", maximum);
        return out;
    }

    /** A boolean with a default (null for none). */
    public static Map<String, Object> bool(String description, Boolean defaultValue) {
        Map<String, Object> out = typed("boolean", description);
        if (defaultValue != null) {
            out.put("default", defaultValue);
        }
        return out;
    }

    /** An array of strings, at most {@code maxItems}. */
    public static Map<String, Object> stringArray(String description, int maxItems) {
        Map<String, Object> out = typed("array", description);
        out.put("items", Map.of("type", "string"));
        out.put("maxItems", maxItems);
        return out;
    }

    /** An array of strings limited to {@code values}. */
    public static Map<String, Object> enumArray(String description, String... values) {
        Map<String, Object> out = typed("array", description);
        out.put("items", Map.of("type", "string", "enum", Arrays.asList(values)));
        out.put("maxItems", values.length);
        out.put("uniqueItems", true);
        return out;
    }

    /** The {@code detail} argument every shaping tool takes. */
    public static Map<String, Object> detail() {
        return enumeration("short (default): compact items capped at 4 KB in total, enough to cite and decide."
                + " long: full items including payloads, capped at 50 KB. Ask for long only for the records you"
                + " need in full.", "short", "short", "long");
    }

    /**
     * The output schema of a list tool: {@code items}, {@code total} and {@code truncated}, plus
     * whatever else the tool adds.
     */
    public static Map<String, Object> listOutput(String description) {
        return openObject(description, props(
                "items", Map.of("type", "array", "items", Map.of("type", "object")),
                "total", Map.of("type", "integer", "minimum", 0,
                        "description", "Items the query produced before size capping"),
                "truncated", Map.of("type", "boolean",
                        "description", "True when more matched than were returned (limit or size cap)"),
                "notice", Map.of("type", "string", "description", "Why the output was cut, when it was")),
                "items", "total", "truncated");
    }

    /** The output schema of a tool that returns one object. */
    public static Map<String, Object> objectOutput(String description) {
        return openObject(description, new LinkedHashMap<>());
    }

    private static Map<String, Object> typed(String type, String description) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("type", type);
        if (description != null) {
            out.put("description", description);
        }
        return out;
    }
}
