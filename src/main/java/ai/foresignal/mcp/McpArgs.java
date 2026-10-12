package ai.foresignal.mcp;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import tools.jackson.databind.JsonNode;

/**
 * Typed reads of validated tool arguments. The schema has already checked types and ranges; these
 * helpers turn the JSON into values and report anything the schema cannot express (a malformed
 * date-time, a bad UUID) as an {@link McpToolException} naming the argument.
 */
public final class McpArgs {

    private McpArgs() {}

    /** A string, or null when absent or JSON null. */
    public static String text(JsonNode args, String name) {
        JsonNode node = args == null ? null : args.get(name);
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }
        String value = node.isString() ? node.asString() : node.toString();
        return value.isBlank() ? null : value.strip();
    }

    /** A required string. */
    public static String requiredText(JsonNode args, String name) {
        String value = text(args, name);
        if (value == null) {
            throw new McpToolException("The argument '" + name + "' is required.");
        }
        return value;
    }

    /** An integer, or {@code defaultValue} when absent. */
    public static int integer(JsonNode args, String name, int defaultValue) {
        JsonNode node = args == null ? null : args.get(name);
        return node == null || node.isNull() ? defaultValue : node.asInt();
    }

    /** A long, or null when absent. */
    public static Long optionalLong(JsonNode args, String name) {
        JsonNode node = args == null ? null : args.get(name);
        return node == null || node.isNull() ? null : node.asLong();
    }

    /** A required long. */
    public static long requiredLong(JsonNode args, String name) {
        Long value = optionalLong(args, name);
        if (value == null) {
            throw new McpToolException("The argument '" + name + "' is required.");
        }
        return value;
    }

    /** A number as text (for the shared REST parsers), or null when absent. */
    public static String numberText(JsonNode args, String name) {
        JsonNode node = args == null ? null : args.get(name);
        return node == null || node.isNull() ? null : node.asString();
    }

    /** A boolean, or {@code defaultValue} when absent. */
    public static boolean bool(JsonNode args, String name, boolean defaultValue) {
        JsonNode node = args == null ? null : args.get(name);
        return node == null || node.isNull() ? defaultValue : node.asBoolean();
    }

    /** A boolean, or null when absent. */
    public static Boolean optionalBool(JsonNode args, String name) {
        JsonNode node = args == null ? null : args.get(name);
        return node == null || node.isNull() ? null : node.asBoolean();
    }

    /** An array of strings (a single string is read as one element), empty when absent. */
    public static List<String> strings(JsonNode args, String name) {
        JsonNode node = args == null ? null : args.get(name);
        List<String> out = new ArrayList<>();
        if (node == null || node.isNull()) {
            return out;
        }
        if (node.isArray()) {
            for (JsonNode item : node) {
                if (item != null && !item.isNull() && !item.asString().isBlank()) {
                    out.add(item.asString().strip());
                }
            }
        } else if (!node.asString().isBlank()) {
            out.add(node.asString().strip());
        }
        return out;
    }

    /** An RFC 3339 date-time, or null when absent. */
    public static Instant instant(JsonNode args, String name) {
        String value = text(args, name);
        if (value == null) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            try {
                return OffsetDateTime.parse(value).toInstant();
            } catch (DateTimeParseException e2) {
                throw new McpToolException("The argument '" + name + "' must be an RFC 3339 date-time such as"
                        + " 2026-10-12T08:00:00Z; got '" + value + "'.");
            }
        }
    }

    /** A UUID, or null when absent. */
    public static UUID uuid(JsonNode args, String name) {
        String value = text(args, name);
        if (value == null) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new McpToolException("The argument '" + name + "' must be a UUID; got '" + value + "'.");
        }
    }

    /** A required UUID. */
    public static UUID requiredUuid(JsonNode args, String name) {
        UUID value = uuid(args, name);
        if (value == null) {
            throw new McpToolException("The argument '" + name + "' is required.");
        }
        return value;
    }
}
