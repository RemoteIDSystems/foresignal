package ai.foresignal.notify.types;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.notify.ChannelFieldSpec;
import ai.foresignal.notify.ChannelRateLimit;
import ai.foresignal.notify.ChannelResponse;
import ai.foresignal.notify.ChannelSettings;
import ai.foresignal.notify.ChannelTypeSpec;
import ai.foresignal.notify.MarkdownRenderer;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.OriginMarker;
import ai.foresignal.notify.PreparedRequest;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * {@code apprise}: apprise-api {@code POST {serverUrl}/notify/{configKey}}, or the stateless
 * {@code POST {serverUrl}/notify/} with the secret {@code urls}, with {@code {title, body, type:
 * info|warning|failure, format: markdown, tag}}.
 *
 * <p><b>HTTP 424</b> means at least one target failed while others succeeded: the delivery is marked
 * delivered with a warning in {@code last_error} and never retried, because a retry would notify the
 * successful targets again ({@link #partialSuccess}). Lifecycle updates are follow-ups.
 */
@Component
public class AppriseChannel extends AbstractChannelType {

    /** The type id. */
    public static final String ID = "apprise";

    /** apprise-api's partial-failure status. */
    public static final int PARTIAL_FAILURE = 424;

    private static final ChannelTypeSpec SPEC = new ChannelTypeSpec(ID, "Apprise",
            "apprise-api server (stateful config key or stateless URLs)",
            List.of(ChannelFieldSpec.required("serverUrl", ChannelFieldSpec.URL, "apprise-api URL"),
                    ChannelFieldSpec.optional("configKey", ChannelFieldSpec.STRING, "Saved configuration key", null),
                    ChannelFieldSpec.optional("tag", ChannelFieldSpec.STRING, "Tag filter of the configuration", null)),
            List.of(ChannelFieldSpec.optional("urls", ChannelFieldSpec.STRING,
                    "Stateless mode: Apprise URLs (when no configKey)", null)),
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
        return ChannelRateLimit.perSecond(5);
    }

    @Override
    public List<FieldError> validate(Map<String, Object> config, Map<String, String> secrets) {
        Object key = config.get("configKey");
        boolean hasKey = key instanceof String text && !text.isBlank();
        if (!hasKey && secrets.get("urls") == null) {
            return List.of(new FieldError("config.configKey", "configKey or secrets.urls is required"));
        }
        if (hasKey && !((String) key).matches("[A-Za-z0-9_-]{1,128}")) {
            return List.of(new FieldError("config.configKey", "must be letters, digits, _ or -"));
        }
        return List.of();
    }

    @Override
    public PreparedRequest render(NotificationMessage m, ChannelSettings s) {
        String key = s.config("configKey", null);
        String url = ChannelSettings.trimSlash(s.config("serverUrl", "")) + "/notify/" + (key == null ? "" : key);
        String body = MarkdownRenderer.render(m.bodyMarkdown(), MarkdownRenderer.Format.ZULIP);
        Map<String, Object> payload = obj(
                "title", m.title(),
                "body", body.isBlank() ? m.title() : body,
                "type", switch (m.severity()) {
                    case "critical" -> "failure";
                    case "warning" -> "warning";
                    default -> "info";
                },
                "format", "markdown",
                "tag", s.config("tag", null),
                "urls", key == null ? s.secret("urls") : null,
                OriginMarker.JSON_FIELD, s.origin());
        return json("POST", url, null, payload, s);
    }

    @Override
    public boolean partialSuccess(ChannelResponse response) {
        return response.status() == PARTIAL_FAILURE;
    }
}
