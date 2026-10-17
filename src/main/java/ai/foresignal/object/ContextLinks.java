package ai.foresignal.object;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ValidationException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

/**
 * Deep links (docs/Client-Contract.md §6): a context travels as {@code {"v":1,"ws":…,"ctx":{…}}}, JSON,
 * base64url-encoded without padding, at most {@value #MAX_BYTES} bytes, carried in a {@code #fs=} URL fragment.
 */
public final class ContextLinks {

    /** Largest encoded payload, in bytes. */
    public static final int MAX_BYTES = 2048;

    /** The fragment prefix. */
    public static final String FRAGMENT = "#fs=";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private ContextLinks() {}

    /**
     * A decoded deep link.
     *
     * @param version the payload version
     * @param workspaceId the workspace, or null for an ad-hoc context
     * @param context the context
     */
    public record Decoded(int version, String workspaceId, JsonNode context) {}

    /**
     * Encodes a context as the {@code ContextLink} view.
     *
     * @param workspaceId the workspace, or null
     * @param context the context (validated first)
     * @return the view: {@code version}, {@code workspaceId}, {@code payload}, {@code fragment}, {@code bytes}
     * @throws ForesignalException 413 when the payload exceeds {@value #MAX_BYTES} bytes
     */
    public static Map<String, Object> encode(String workspaceId, JsonNode context) {
        WorkspaceContextValidator.validate(context, "context");
        ObjectNode doc = JsonNodeFactory.instance.objectNode();
        doc.put("v", ObjectTypes.DEEP_LINK_VERSION);
        if (workspaceId == null) {
            doc.putNull("ws");
        } else {
            doc.put("ws", workspaceId);
        }
        doc.set("ctx", stripNulls(context));
        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(JSON.writeValueAsString(doc).getBytes(StandardCharsets.UTF_8));
        int bytes = payload.getBytes(StandardCharsets.US_ASCII).length;
        if (bytes > MAX_BYTES) {
            throw new ForesignalException(HttpStatus.CONTENT_TOO_LARGE, ErrorCodes.PAYLOAD_TOO_LARGE,
                    "The encoded context is " + bytes + " bytes; a deep link holds at most " + MAX_BYTES
                            + ". Select fewer objects or drop filters.").withProperty("bytes", bytes);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("version", ObjectTypes.DEEP_LINK_VERSION);
        out.put("workspaceId", workspaceId);
        out.put("payload", payload);
        out.put("fragment", FRAGMENT + payload);
        out.put("bytes", bytes);
        return out;
    }

    /**
     * Decodes a payload (with or without the {@code #fs=} prefix) and validates its context.
     *
     * @throws ValidationException 400 when it is malformed
     */
    public static Decoded decode(String payload) {
        if (payload == null || payload.isBlank()) {
            throw ValidationException.forField("payload", "is required");
        }
        String text = payload.startsWith(FRAGMENT) ? payload.substring(FRAGMENT.length()) : payload;
        if (text.length() > MAX_BYTES) {
            throw ValidationException.forField("payload", "is longer than " + MAX_BYTES + " bytes");
        }
        JsonNode doc;
        try {
            doc = JSON.readTree(new String(Base64.getUrlDecoder().decode(text), StandardCharsets.UTF_8));
        } catch (RuntimeException e) {
            throw ValidationException.forField("payload", "is not base64url JSON");
        }
        if (doc == null || !doc.isObject() || !doc.path("v").isInt()) {
            throw ValidationException.forField("payload", "must be {\"v\":1,\"ws\":…,\"ctx\":{…}}");
        }
        int version = doc.get("v").asInt();
        if (version != ObjectTypes.DEEP_LINK_VERSION) {
            throw ValidationException.forField("payload", "has unsupported version " + version);
        }
        JsonNode ctx = doc.get("ctx");
        WorkspaceContextValidator.validate(ctx, "ctx");
        JsonNode ws = doc.get("ws");
        return new Decoded(version, ws == null || ws.isNull() ? null : ws.asString(), ctx);
    }

    /** {@code node} without null members (a deep link carries only what is set). */
    static JsonNode stripNulls(JsonNode node) {
        if (node == null || !node.isObject()) {
            return node == null ? JsonNodeFactory.instance.objectNode() : node;
        }
        ObjectNode out = JsonNodeFactory.instance.objectNode();
        for (String name : node.propertyNames()) {
            JsonNode v = node.get(name);
            if (v != null && !v.isNull()) {
                out.set(name, v);
            }
        }
        return out;
    }
}
