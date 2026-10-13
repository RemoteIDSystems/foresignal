package ai.foresignal.llm.client;

import java.util.List;
import java.util.Objects;

/**
 * The result of one tool call, returned to the model.
 *
 * @param toolCallId the {@link ToolCall#id()} it answers
 * @param toolName the tool's name (Ollama's {@code tool_name})
 * @param content the result text, usually JSON; delimited as untrusted data by the caller
 * @param isError true when the tool failed: the model sees the failure instead of a dropped call
 * @param images images to show the model with the text (prompt 056, {@code get_map_image}); only providers that take
 *     images in tool results send them (Anthropic), the others send the text alone, which always carries a caption.
 *     Images are not stored with a thread's turns, so a replayed turn carries the text only
 */
public record ToolResult(String toolCallId, String toolName, String content, boolean isError, List<Image> images) {

    /**
     * An image of a tool result.
     *
     * @param base64 the image bytes, base64 encoded
     * @param mimeType for example {@code image/png}
     */
    public record Image(String base64, String mimeType) {

        /** Requires both members. */
        public Image {
            Objects.requireNonNull(base64, "base64");
            Objects.requireNonNull(mimeType, "mimeType");
        }
    }

    /** Validates the members. */
    public ToolResult {
        Objects.requireNonNull(toolCallId, "toolCallId");
        content = content == null ? "" : content;
        images = images == null ? List.of() : List.copyOf(images);
    }

    /** A result without images. */
    public ToolResult(String toolCallId, String toolName, String content, boolean isError) {
        this(toolCallId, toolName, content, isError, List.of());
    }

    /** A successful result. */
    public static ToolResult ok(ToolCall call, String content) {
        return new ToolResult(call.id(), call.name(), content, false);
    }

    /** A successful result with images (prompt 056). */
    public static ToolResult ok(ToolCall call, String content, List<Image> images) {
        return new ToolResult(call.id(), call.name(), content, false, images);
    }

    /** An error result. */
    public static ToolResult error(ToolCall call, String message) {
        return new ToolResult(call.id(), call.name(), message, true);
    }
}
