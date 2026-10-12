package ai.foresignal.llm.client;

import java.util.Objects;
import tools.jackson.databind.JsonNode;

/**
 * A tool the model may call.
 *
 * @param name the tool name, unique within a request
 * @param description what it does and when to use it
 * @param inputSchema the JSON Schema of its input (an object schema)
 */
public record ToolSpec(String name, String description, JsonNode inputSchema) {

    /** Validates the members. */
    public ToolSpec {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(inputSchema, "inputSchema");
        description = description == null ? "" : description;
    }

    /**
     * True when the schema is closed ({@code additionalProperties: false}) and lists {@code required}
     * members: such a tool is sent with {@code strict: true} to Anthropic, which then guarantees
     * schema-valid input.
     */
    public boolean strictEligible() {
        JsonNode additional = inputSchema.get("additionalProperties");
        JsonNode required = inputSchema.get("required");
        return additional != null && additional.isBoolean() && !additional.asBoolean()
                && required != null && required.isArray();
    }
}
