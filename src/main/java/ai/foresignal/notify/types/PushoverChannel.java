package ai.foresignal.notify.types;

import ai.foresignal.notify.ChannelFieldSpec;
import ai.foresignal.notify.ChannelRateLimit;
import ai.foresignal.notify.ChannelResponse;
import ai.foresignal.notify.ChannelSettings;
import ai.foresignal.notify.ChannelTypeSpec;
import ai.foresignal.notify.MarkdownRenderer;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.PreparedRequest;
import ai.foresignal.notify.Truncator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * {@code pushover}: {@code POST https://api.pushover.net/1/messages.json} with {@code html=1}, priority
 * info 0, warning 1, critical 2 (emergency, {@code retry=300}, {@code expire=3600}) and the URL.
 *
 * <p>An emergency message returns a {@code receipt}, which is stored. When the alert is resolved or
 * acknowledged, {@code POST /1/receipts/{receipt}/cancel.json} stops the emergency retries before the
 * follow-up is sent ({@link #beforeUpdate}).
 */
@Component
public class PushoverChannel extends AbstractChannelType {

    /** The type id. */
    public static final String ID = "pushover";

    /** Pushover's message limit. */
    static final int MESSAGE_MAX = 1024;

    /** Pushover's title limit. */
    static final int TITLE_MAX = 250;

    private static final ChannelTypeSpec SPEC = new ChannelTypeSpec(ID, "Pushover",
            "Pushover message; critical is an emergency priority, canceled on resolve or acknowledge",
            List.of(ChannelFieldSpec.optional("device", ChannelFieldSpec.STRING, "Target device name", null),
                    ChannelFieldSpec.optional("sound", ChannelFieldSpec.STRING, "Sound name", null),
                    ChannelFieldSpec.optional("apiBaseUrl", ChannelFieldSpec.URL, "API base URL",
                            "https://api.pushover.net")),
            List.of(ChannelFieldSpec.required("appToken", ChannelFieldSpec.STRING, "Application API token"),
                    ChannelFieldSpec.required("userKey", ChannelFieldSpec.STRING, "User or group key")),
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
        return ChannelRateLimit.perSecond(2);
    }

    @Override
    public PreparedRequest render(NotificationMessage m, ChannelSettings s) {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("token", s.secret("appToken"));
        form.put("user", s.secret("userKey"));
        form.put("title", Truncator.truncate(m.title(), TITLE_MAX, Truncator.Unit.CHARS, Truncator.Mode.PLAIN, null));
        form.put("message", message(m));
        form.put("html", "1");
        String priority = switch (m.severity()) {
            case "critical" -> "2";
            case "warning" -> "1";
            default -> "0";
        };
        form.put("priority", priority);
        if ("2".equals(priority)) {
            form.put("retry", "300");
            form.put("expire", "3600");
        }
        if (m.url() != null) {
            form.put("url", m.url());
            form.put("url_title", buttons(m).getFirst().label());
        }
        if (s.config("device", null) != null) {
            form.put("device", s.config("device", null));
        }
        if (s.config("sound", null) != null) {
            form.put("sound", s.config("sound", null));
        }
        form.put("timestamp", Long.toString((m.createdAt() != null ? m.createdAt() : s.now()).getEpochSecond()));
        return form(api(s) + "/1/messages.json", form, null, s);
    }

    @Override
    public Optional<String> extractRef(ChannelResponse response) {
        return refAt(response, "receipt");
    }

    @Override
    public List<PreparedRequest> beforeUpdate(NotificationMessage m, String ref, ChannelSettings s) {
        if (ref == null || ref.isBlank()
                || !("alert.resolved".equals(m.subtype()) || "alert.acknowledged".equals(m.subtype()))) {
            return List.of();
        }
        Map<String, String> form = new LinkedHashMap<>();
        form.put("token", s.secret("appToken"));
        return List.of(form(api(s) + "/1/receipts/" + ref + "/cancel.json", form, null, s));
    }

    /** The body in Pushover's HTML subset ({@code b}, {@code i}, {@code a}), cut to 1,024 characters. */
    static String message(NotificationMessage m) {
        String html = MarkdownRenderer.render(m.bodyMarkdown(), MarkdownRenderer.Format.TELEGRAM_HTML)
                .replaceAll("</?(s|code|pre)>", "");
        if (html.isBlank()) {
            html = MarkdownRenderer.escape(m.title(), MarkdownRenderer.Format.TELEGRAM_HTML);
        }
        return Truncator.truncate(html, MESSAGE_MAX, Truncator.Unit.CHARS, Truncator.Mode.HTML, null);
    }

    private static String api(ChannelSettings s) {
        return ChannelSettings.trimSlash(s.config("apiBaseUrl", "https://api.pushover.net"));
    }
}
