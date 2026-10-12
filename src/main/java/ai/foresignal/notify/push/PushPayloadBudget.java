package ai.foresignal.notify.push;

import ai.foresignal.notify.Truncator;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import tools.jackson.databind.json.JsonMapper;

/**
 * Keeps a Web Push plaintext within one {@code aes128gcm} record (prompt 031): at most
 * {@value WebPushCrypto#MAX_PLAINTEXT} bytes of UTF-8 JSON (4096 − 86 header − 16 tag − 1
 * delimiter).
 *
 * <p>The payload is serialized; while it is too large it is shrunk in this order:
 * <ol>
 *   <li>truncate {@code body} (UTF-8 aware, on grapheme boundaries, never splitting a surrogate
 *       pair, with an ellipsis), down to nothing if need be;
 *   <li>drop {@code links.api} (and its alias {@code url});
 *   <li>cap {@code tags} at 5;
 *   <li>truncate {@code title} to 120 characters.
 * </ol>
 * A payload that still does not fit falls back to {@code {v, event, id, severity, shortTitle}}.
 * The result never exceeds the cap.
 */
public final class PushPayloadBudget {

    /** The largest plaintext in bytes. */
    public static final int MAX_BYTES = WebPushCrypto.MAX_PLAINTEXT;

    /** Tags kept when they must be capped. */
    public static final int MAX_TAGS = 5;

    /** Title length when it must be truncated. */
    public static final int MAX_TITLE = 120;

    private final JsonMapper json;
    private final int maxBytes;

    /** A budget of {@link #MAX_BYTES}. */
    public PushPayloadBudget(JsonMapper json) {
        this(json, MAX_BYTES);
    }

    /** A budget of {@code maxBytes} (tests). */
    PushPayloadBudget(JsonMapper json, int maxBytes) {
        this.json = json;
        this.maxBytes = maxBytes;
    }

    /**
     * The UTF-8 JSON of {@code message} in {@code format} and {@code lang}, within the cap.
     */
    public byte[] fit(PushMessage message, String format, String lang) {
        PushPayloads.Fields fields = PushPayloads.Fields.of(message, lang);
        byte[] out = bytes(PushPayloads.render(fields, format));
        if (out.length <= maxBytes) {
            return out;
        }
        // 1. The body.
        if (fields.body() != null && !fields.body().isEmpty()) {
            String body = fields.body();
            int limit = body.getBytes(StandardCharsets.UTF_8).length - (out.length - maxBytes);
            while (true) {
                String cut = limit <= 0 ? "" : Truncator.truncate(body, limit, Truncator.Unit.UTF8_BYTES,
                        Truncator.Mode.PLAIN, null);
                fields = new PushPayloads.Fields(message, lang, fields.title(), cut, fields.linkApi(), fields.tags());
                out = bytes(PushPayloads.render(fields, format));
                if (out.length <= maxBytes || cut.isEmpty()) {
                    break;
                }
                // JSON escaping can make the cut text larger than its raw bytes; take off the excess.
                limit = Math.min(limit - 1, cut.getBytes(StandardCharsets.UTF_8).length - (out.length - maxBytes));
            }
            if (out.length <= maxBytes) {
                return out;
            }
        }
        // 2. links.api.
        fields = new PushPayloads.Fields(message, lang, fields.title(), fields.body(), null, fields.tags());
        out = bytes(PushPayloads.render(fields, format));
        if (out.length <= maxBytes) {
            return out;
        }
        // 3. Tags.
        if (fields.tags().size() > MAX_TAGS) {
            fields = new PushPayloads.Fields(message, lang, fields.title(), fields.body(), null,
                    fields.tags().subList(0, MAX_TAGS));
            out = bytes(PushPayloads.render(fields, format));
            if (out.length <= maxBytes) {
                return out;
            }
        }
        // 4. The title.
        if (fields.title() != null && fields.title().length() > MAX_TITLE) {
            String title = Truncator.truncate(fields.title(), MAX_TITLE, Truncator.Unit.CHARS, Truncator.Mode.PLAIN, null);
            fields = new PushPayloads.Fields(message, lang, title, fields.body(), null, fields.tags());
            out = bytes(PushPayloads.render(fields, format));
            if (out.length <= maxBytes) {
                return out;
            }
        }
        out = bytes(PushPayloads.minimal(message, format));
        if (out.length <= maxBytes) {
            return out;
        }
        // Only reachable with an absurdly small cap (tests): the bare event.
        return bytes(Map.of("v", 1, "event", message.event()));
    }

    private byte[] bytes(Map<String, Object> payload) {
        return json.writeValueAsBytes(payload);
    }
}
