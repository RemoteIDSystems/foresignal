package ai.foresignal.inbound.format;

import java.util.LinkedHashMap;
import java.util.Map;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;

/**
 * A custom source's options for one format (stored under {@code formats.{id}}).
 *
 * @param node the options object; never null (an empty object when the source sets none)
 */
public record FormatOptions(JsonNode node) {

    /** Replaces a null or non-object node with an empty object. */
    public FormatOptions {
        node = node == null || !node.isObject() ? JsonNodeFactory.instance.objectNode() : node;
    }

    /** No options. */
    public static FormatOptions empty() {
        return new FormatOptions(null);
    }

    /** The option {@code name}, or a missing node. */
    public JsonNode get(String name) {
        return node.path(name);
    }

    /** The text option {@code name}, or {@code fallback} when absent or null. */
    public String text(String name, String fallback) {
        JsonNode value = node.get(name);
        return value == null || value.isNull() || !value.isValueNode() ? fallback : value.asString();
    }

    /** The boolean option {@code name}, or {@code fallback} when absent or not a boolean. */
    public boolean bool(String name, boolean fallback) {
        JsonNode value = node.get(name);
        return value != null && value.isBoolean() ? value.booleanValue() : fallback;
    }

    /** The object option {@code name} as a string-to-string map; empty when absent. */
    public Map<String, String> stringMap(String name) {
        JsonNode value = node.get(name);
        Map<String, String> out = new LinkedHashMap<>();
        if (value != null && value.isObject()) {
            for (Map.Entry<String, JsonNode> entry : value.properties()) {
                if (entry.getValue().isValueNode() && !entry.getValue().isNull()) {
                    out.put(entry.getKey(), entry.getValue().asString());
                }
            }
        }
        return out;
    }
}
