package ai.foresignal.llm.client;

import java.util.Objects;
import tools.jackson.databind.JsonNode;

/**
 * One tool call the model asked for.
 *
 * @param id the provider's call id, echoed in the {@link ToolResult}; synthesized for providers
 *     that do not assign one (Ollama)
 * @param name the tool name
 * @param input the call's arguments as a JSON object
 */
public record ToolCall(String id, String name, JsonNode input) {

    /** Validates the members. */
    public ToolCall {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(input, "input");
    }
}
