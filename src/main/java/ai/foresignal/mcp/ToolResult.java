package ai.foresignal.mcp;

import java.util.List;
import java.util.Objects;
import tools.jackson.databind.JsonNode;

/**
 * What a tool returns: content blocks for the model, plus an optional structured result
 * ({@code structuredContent}) that matches the tool's output schema.
 *
 * <p>Text, JSON and image results are supported; images (055 {@code get_map_image}, 036 pass
 * thumbnails) always travel with a text caption, so a model without vision still gets the facts.
 *
 * @param content the content blocks, in order
 * @param structured the structured result, or null
 * @param error true for a tool error the agent should read and act on
 */
public record ToolResult(List<Content> content, JsonNode structured, boolean error) {

    /** Copies the content list. */
    public ToolResult {
        content = content == null ? List.of() : List.copyOf(content);
    }

    /** A content block. */
    public sealed interface Content permits Text, Image {}

    /**
     * A text block.
     *
     * @param text the text
     */
    public record Text(String text) implements Content {

        /** Requires the text. */
        public Text {
            Objects.requireNonNull(text, "text");
        }
    }

    /**
     * An image block.
     *
     * @param data the image bytes, base64 encoded
     * @param mimeType for example {@code image/png}
     */
    public record Image(String data, String mimeType) implements Content {

        /** Requires both members. */
        public Image {
            Objects.requireNonNull(data, "data");
            Objects.requireNonNull(mimeType, "mimeType");
        }
    }

    /** A JSON result: the JSON text as content and the same value as the structured result. */
    public static ToolResult json(JsonNode value) {
        Objects.requireNonNull(value, "value");
        return new ToolResult(List.of(new Text(value.toString())), value, false);
    }

    /** A plain text result. */
    public static ToolResult text(String text) {
        return new ToolResult(List.of(new Text(text)), null, false);
    }

    /**
     * An image with its caption (Weather-Imagery-Design §6.9).
     *
     * @param base64 the image, base64 encoded
     * @param mimeType its media type
     * @param caption what the image shows, for models without vision
     * @param structured an optional structured result, or null
     */
    public static ToolResult image(String base64, String mimeType, String caption, JsonNode structured) {
        return new ToolResult(List.of(new Image(base64, mimeType), new Text(caption)), structured, false);
    }

    /** A tool error with a message for the agent. */
    public static ToolResult error(String message) {
        return new ToolResult(List.of(new Text(message)), null, true);
    }
}
