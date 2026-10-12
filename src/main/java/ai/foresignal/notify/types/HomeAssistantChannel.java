package ai.foresignal.notify.types;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.notify.ChannelFieldSpec;
import ai.foresignal.notify.ChannelRateLimit;
import ai.foresignal.notify.ChannelSettings;
import ai.foresignal.notify.ChannelTypeSpec;
import ai.foresignal.notify.MarkdownRenderer;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.OriginMarker;
import ai.foresignal.notify.PreparedRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * {@code homeassistant}: two modes.
 *
 * <ul>
 *   <li><b>{@code webhook}</b> (default): {@code POST {baseUrl}/api/webhook/{webhookId}} (secret
 *       {@code webhookId}) with the default {@code webhook} JSON body, to trigger automations.
 *   <li><b>{@code notify}</b>: {@code POST {baseUrl}/api/services/notify/{service}} with bearer
 *       {@code accessToken} and {@code {title, message, data}}. On critical, {@code data} asks for an
 *       iOS critical sound ({@code push.sound {name, critical: 1, volume: 1.0}}, config
 *       {@code iosCritical}) and the Android alarm stream ({@code ttl: 0, priority: high,
 *       channel: alarm_stream}, config {@code androidAlarmStream}); both default on. The service
 *       schema accepts only {@code message}, {@code title}, {@code target} and {@code data}, so the
 *       origin marker travels inside {@code data}.
 * </ul>
 *
 * <p>Lifecycle updates are follow-ups.
 */
@Component
public class HomeAssistantChannel extends AbstractChannelType {

    /** The type id. */
    public static final String ID = "homeassistant";

    private static final ChannelTypeSpec SPEC = new ChannelTypeSpec(ID, "Home Assistant",
            "Home Assistant webhook trigger (default) or notify service (critical alerts on phones)",
            List.of(ChannelFieldSpec.required("baseUrl", ChannelFieldSpec.URL, "Home Assistant URL"),
                    ChannelFieldSpec.choice("mode", false, "webhook or notify", "webhook", List.of("webhook", "notify")),
                    ChannelFieldSpec.optional("service", ChannelFieldSpec.STRING,
                            "notify mode: the notify service, e.g. mobile_app_pixel", null),
                    ChannelFieldSpec.optional("iosCritical", ChannelFieldSpec.BOOLEAN,
                            "notify mode: critical alerts use the iOS critical sound", true),
                    ChannelFieldSpec.optional("androidAlarmStream", ChannelFieldSpec.BOOLEAN,
                            "notify mode: critical alerts use the Android alarm stream", true)),
            List.of(ChannelFieldSpec.optional("webhookId", ChannelFieldSpec.STRING, "webhook mode: webhook id", null),
                    ChannelFieldSpec.optional("accessToken", ChannelFieldSpec.STRING,
                            "notify mode: long-lived access token", null)),
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
        if ("notify".equals(config.get("mode"))) {
            if (!(config.get("service") instanceof String service) || service.isBlank()) {
                errors.add(new FieldError("config.service", "is required in notify mode"));
            } else if (!service.matches("[a-z0-9_]+")) {
                errors.add(new FieldError("config.service", "must be a notify service name (lowercase, digits, _)"));
            }
            if (secrets.get("accessToken") == null) {
                errors.add(new FieldError("secrets.accessToken", "is required in notify mode"));
            }
        } else if (secrets.get("webhookId") == null) {
            errors.add(new FieldError("secrets.webhookId", "is required in webhook mode"));
        }
        return errors;
    }

    @Override
    public PreparedRequest render(NotificationMessage m, ChannelSettings s) {
        String base = ChannelSettings.trimSlash(s.config("baseUrl", ""));
        if (!"notify".equals(s.config("mode", "webhook"))) {
            return json("POST", base + "/api/webhook/" + s.secret("webhookId"), null,
                    WebhookChannel.defaultBody(m, s.origin()), s);
        }
        Map<String, Object> data = obj("url", m.url(), "clickAction", m.url(), "tag", m.alertId() == null ? null
                : "foresignal-alert-" + m.alertId(), OriginMarker.JSON_FIELD, s.origin());
        if ("critical".equals(m.severity())) {
            if (s.flag("iosCritical", true)) {
                data.put("push", obj("sound", obj("name", "default", "critical", 1, "volume", 1.0)));
            }
            if (s.flag("androidAlarmStream", true)) {
                data.put("ttl", 0);
                data.put("priority", "high");
                data.put("channel", "alarm_stream");
            }
        }
        String message = MarkdownRenderer.render(m.bodyMarkdown(), MarkdownRenderer.Format.PLAIN);
        Map<String, Object> payload = obj("title", m.title(), "message", message.isBlank() ? m.title() : message,
                "data", data);
        return json("POST", base + "/api/services/notify/" + s.config("service", ""),
                Map.of("Authorization", "Bearer " + s.secret("accessToken")), payload, s);
    }
}
