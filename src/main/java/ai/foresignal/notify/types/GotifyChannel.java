package ai.foresignal.notify.types;

import ai.foresignal.notify.ChannelFieldSpec;
import ai.foresignal.notify.ChannelRateLimit;
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
 * {@code gotify}: {@code POST {serverUrl}/message} with {@code X-Gotify-Key}; priority info 4,
 * warning 6, critical 9; markdown display, a click URL and the {@link OriginMarker} in
 * {@code extras}. Lifecycle updates are follow-ups.
 */
@Component
public class GotifyChannel extends AbstractChannelType {

    /** The type id. */
    public static final String ID = "gotify";

    private static final ChannelTypeSpec SPEC = new ChannelTypeSpec(ID, "Gotify",
            "Gotify application message (priority from severity, markdown)",
            List.of(ChannelFieldSpec.required("serverUrl", ChannelFieldSpec.URL, "Gotify server")),
            List.of(ChannelFieldSpec.required("appToken", ChannelFieldSpec.STRING, "Application token")),
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
    public PreparedRequest render(NotificationMessage m, ChannelSettings s) {
        String body = MarkdownRenderer.render(m.bodyMarkdown(), MarkdownRenderer.Format.ZULIP);
        if (m.imageUrl() != null) {
            body = (body.isBlank() ? "" : body + "\n\n") + "![map](" + m.imageUrl() + ")";
        }
        Map<String, Object> extras = obj(
                "client::display", obj("contentType", "text/markdown"),
                "client::notification", m.url() == null ? null : obj("click", obj("url", m.url())),
                OriginMarker.GOTIFY_EXTRA, OriginMarker.gotifyExtra(s.origin() == null ? "unknown" : s.origin()));
        Map<String, Object> payload = obj(
                "title", m.title(),
                "message", body.isBlank() ? m.title() : body,
                "priority", switch (m.severity()) {
                    case "critical" -> 9;
                    case "warning" -> 6;
                    default -> 4;
                },
                "extras", extras);
        String url = ChannelSettings.trimSlash(s.config("serverUrl", "")) + "/message";
        return json("POST", url, Map.of("X-Gotify-Key", s.secret("appToken")), payload, s);
    }
}
