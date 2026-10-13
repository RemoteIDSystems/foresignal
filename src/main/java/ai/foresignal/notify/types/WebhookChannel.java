package ai.foresignal.notify.types;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.ingest.JsonValues;
import ai.foresignal.notify.ChannelFieldSpec;
import ai.foresignal.notify.ChannelRateLimit;
import ai.foresignal.notify.ChannelSettings;
import ai.foresignal.notify.ChannelTypeSpec;
import ai.foresignal.notify.HmacSigner;
import ai.foresignal.notify.NotificationJson;
import ai.foresignal.notify.NotificationAction;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.OriginMarker;
import ai.foresignal.notify.PreparedRequest;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * {@code webhook}: a generic JSON POST or PUT, HMAC-signed when a secret is set (Architecture §17,
 * FR-37).
 *
 * <p>The default body is {@code {"event", "subtype", "message": NotificationMessage, "data",
 * "foresignalOrigin"}}. An operator {@code bodyTemplate} replaces it; its placeholders
 * ({@code {{title}}}, {@code {{shortTitle}}}, {@code {{body}}}, {@code {{severity}}},
 * {@code {{event}}}, {@code {{subtype}}}, {@code {{url}}}, {@code {{id}}}, {@code {{alertId}}},
 * {@code {{origin}}} and {@code {{data.<path>}}}) are substituted JSON-escaped, without quotes, so a
 * template places them inside its own string literals.
 *
 * <p>Headers: {@code X-Foresignal-Event}, {@code X-Foresignal-Delivery}, {@code X-Foresignal-Timestamp}
 * (epoch seconds), {@code X-Foresignal-Origin}, {@code User-Agent}, the configured {@code headers},
 * every secret named {@code header.<Name>} as header {@code <Name>}, and with {@code hmacSecret}
 * {@code X-Foresignal-Signature: sha256=<hex HMAC-SHA256("<timestamp>.<body>")>}. Lifecycle updates
 * are follow-ups whose {@code subtype} says what changed.
 */
@Component
public class WebhookChannel extends AbstractChannelType {

    /** The type id. */
    public static final String ID = "webhook";

    /** Secret names with this prefix are sent as headers. */
    public static final String SECRET_HEADER_PREFIX = "header.";

    private static final ChannelTypeSpec SPEC = new ChannelTypeSpec(ID, "Webhook",
            "Generic JSON webhook, optionally HMAC-SHA256 signed",
            List.of(ChannelFieldSpec.required("url", ChannelFieldSpec.URL, "Receiver URL"),
                    ChannelFieldSpec.choice("method", false, "HTTP method", "POST", List.of("POST", "PUT")),
                    ChannelFieldSpec.optional("headers", ChannelFieldSpec.MAP, "Extra request headers", null),
                    ChannelFieldSpec.optional("bodyTemplate", ChannelFieldSpec.STRING,
                            "JSON body with JSON-escaped {{…}} placeholders; default is the Foresignal JSON", null)),
            List.of(ChannelFieldSpec.optional("hmacSecret", ChannelFieldSpec.STRING,
                            "Signs X-Foresignal-Signature: sha256=HMAC(\"<timestamp>.<body>\")", null),
                    ChannelFieldSpec.optional("header.<Name>", ChannelFieldSpec.STRING,
                            "Any secret named header.<Name> is sent as the header <Name>", null)),
            null);

    @Override
    public String id() {
        return ID;
    }

    @Override
    public ChannelTypeSpec spec() {
        return SPEC;
    }

    @Override
    public ChannelRateLimit rateLimit() {
        return ChannelRateLimit.perSecond(10);
    }

    @Override
    public List<FieldError> validate(Map<String, Object> config, Map<String, String> secrets) {
        List<FieldError> errors = new ArrayList<>();
        Object template = config.get("bodyTemplate");
        if (template instanceof String text && !text.isBlank()) {
            String probe = renderTemplate(text, NotificationMessage.builder("test", "test", "t", "info").build());
            try {
                JSON.readTree(probe);
            } catch (RuntimeException e) {
                errors.add(new FieldError("config.bodyTemplate", "must render to valid JSON"));
            }
        }
        return errors;
    }

    @Override
    public PreparedRequest render(NotificationMessage m, ChannelSettings s) {
        String template = s.config("bodyTemplate", null);
        String body = template == null ? JSON.writeValueAsString(defaultBody(m, s.origin(), s.actions()))
                : renderTemplate(template, m);
        String timestamp = Long.toString(s.now().getEpochSecond());
        Map<String, String> headers = baseHeaders(s);
        headers.put("Content-Type", "application/json");
        headers.put("X-Foresignal-Event", m.event());
        headers.put("X-Foresignal-Delivery", Long.toString(s.deliveryId()));
        headers.put("X-Foresignal-Timestamp", timestamp);
        headers.putAll(s.map("headers"));
        s.secrets().forEach((name, value) -> {
            if (name.startsWith(SECRET_HEADER_PREFIX) && name.length() > SECRET_HEADER_PREFIX.length()) {
                headers.put(name.substring(SECRET_HEADER_PREFIX.length()), value);
            }
        });
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        String secret = s.secret("hmacSecret");
        if (secret != null) {
            headers.put(HmacSigner.HEADER, HmacSigner.sign(secret, timestamp, bytes));
        }
        return new PreparedRequest(s.config("method", "POST"), s.config("url", ""), headers,
                new PreparedRequest.Bytes("application/json", bytes));
    }

    /** The default Foresignal JSON body (also Home Assistant's webhook mode). */
    static Map<String, Object> defaultBody(NotificationMessage m, String origin) {
        return defaultBody(m, origin, List.of());
    }

    /**
     * The default body with an {@code attachments} array {@code [{mediaId, url, contentType, bytes}]} when
     * the message carries a map (prompt 055), and an {@code actions} array {@code [{action, label, url, expiresAt}]} when
     * the delivery carries capabilities (prompt 033).
     */
    static Map<String, Object> defaultBody(NotificationMessage m, String origin, List<NotificationAction> actions) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("event", m.event());
        out.put("subtype", m.subtype());
        out.put("message", NotificationJson.toMap(m));
        out.put("data", m.data());
        out.put(OriginMarker.JSON_FIELD, origin);
        if (!m.attachments().isEmpty()) {
            // Prompt 055: the map as metadata; the image itself is at the in-app URL.
            List<Map<String, Object>> list = new ArrayList<>();
            for (NotificationMessage.Attachment a : m.attachments()) {
                list.add(obj("mediaId", a.mediaId(), "url", a.url(), "contentType", a.contentType(), "bytes", a.size()));
            }
            out.put("attachments", list);
        }
        if (actions != null && !actions.isEmpty()) {
            List<Map<String, Object>> list = new ArrayList<>();
            for (NotificationAction action : actions) {
                list.add(obj("action", action.action(), "label", action.label(), "url", action.url(),
                        "expiresAt", action.expiresAt() == null ? null : action.expiresAt().toString()));
            }
            out.put("actions", list);
        }
        return out;
    }

    /** Substitutes the placeholders of {@code template}, JSON-escaped. */
    static String renderTemplate(String template, NotificationMessage m) {
        StringBuilder out = new StringBuilder();
        int i = 0;
        while (i < template.length()) {
            int open = template.indexOf("{{", i);
            int close = open < 0 ? -1 : template.indexOf("}}", open + 2);
            if (open < 0 || close < 0) {
                out.append(template, i, template.length());
                break;
            }
            out.append(template, i, open);
            out.append(jsonEscape(value(template.substring(open + 2, close).strip(), m)));
            i = close + 2;
        }
        return out.toString();
    }

    private static String value(String key, NotificationMessage m) {
        if (key.startsWith("data.")) {
            if (m.data() == null) {
                return "";
            }
            String text = JsonValues.text(JsonValues.at(m.data(), key.substring(5)));
            return text == null ? "" : text;
        }
        Object value = switch (key) {
            case "title" -> m.title();
            case "shortTitle" -> m.shortTitle();
            case "body" -> m.bodyMarkdown();
            case "severity" -> m.severity();
            case "event" -> m.event();
            case "subtype" -> m.subtype();
            case "url" -> m.url();
            case "id" -> m.id();
            case "alertId" -> m.alertId();
            case "origin" -> m.origin();
            default -> null;
        };
        return value == null ? "" : String.valueOf(value);
    }

    /** The JSON string escape of {@code text}, without the surrounding quotes. */
    static String jsonEscape(String text) {
        String quoted = JSON.writeValueAsString(text);
        return quoted.substring(1, quoted.length() - 1);
    }
}
