package ai.foresignal.llm.client;

import java.util.Objects;

/**
 * The result of one tool call, returned to the model.
 *
 * @param toolCallId the {@link ToolCall#id()} it answers
 * @param toolName the tool's name (Ollama's {@code tool_name})
 * @param content the result text, usually JSON; delimited as untrusted data by the caller
 * @param isError true when the tool failed: the model sees the failure instead of a dropped call
 */
public record ToolResult(String toolCallId, String toolName, String content, boolean isError) {

    /** Validates the members. */
    public ToolResult {
        Objects.requireNonNull(toolCallId, "toolCallId");
        content = content == null ? "" : content;
    }

    /** A successful result. */
    public static ToolResult ok(ToolCall call, String content) {
        return new ToolResult(call.id(), call.name(), content, false);
    }

    /** An error result. */
    public static ToolResult error(ToolCall call, String message) {
        return new ToolResult(call.id(), call.name(), message, true);
    }
}
