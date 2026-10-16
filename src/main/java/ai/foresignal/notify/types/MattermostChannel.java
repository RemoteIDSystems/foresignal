package ai.foresignal.notify.types;

import ai.foresignal.notify.ChannelFieldSpec;
import ai.foresignal.notify.ChannelRateLimit;
import ai.foresignal.notify.ChannelSettings;
import ai.foresignal.notify.ChannelTypeSpec;
import ai.foresignal.notify.MarkdownRenderer;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.PreparedRequest;
import ai.foresignal.notify.Truncator;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * {@code mattermost}: an incoming webhook with {@code text} plus one attachment colored by severity;
 * optional {@code channel}, {@code username} and {@code icon_url}. Lifecycle updates are follow-ups.
 */
@Component
public class MattermostChannel extends AbstractChannelType {

    /** The type id. */
    public static final String ID = "mattermost";

    /** Mattermost's post limit (characters). */
    static final int TEXT_MAX = 16_383;

    private static final ChannelTypeSpec SPEC = new ChannelTypeSpec(ID, "Mattermost",
            "Mattermost incoming webhook with a severity-colored attachment",
            List.of(ChannelFieldSpec.optional("channel", ChannelFieldSpec.STRING, "Channel override", null),
                    ChannelFieldSpec.optional("username", ChannelFieldSpec.STRING, "Display name override", null),
                    ChannelFieldSpec.optional("iconUrl", ChannelFieldSpec.URL, "Icon URL", null)),
            List.of(ChannelFieldSpec.required("webhookUrl", ChannelFieldSpec.URL, "Incoming webhook URL")),
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
        return ChannelRateLimit.perSecond(1);
    }

    @Override
    public PreparedRequest render(NotificationMessage m, ChannelSettings s) {
        String body = MarkdownRenderer.render(m.bodyMarkdown(), MarkdownRenderer.Format.ZULIP);
        body = Truncator.truncate(body, TEXT_MAX, Truncator.Unit.CHARS, Truncator.Mode.MARKDOWN, m.url());
        Object attachment = obj(
                "fallback", m.title(),
                "color", hexColor(m.severity()),
                "title", m.title(),
                "title_link", m.url(),
                "text", body,
                "image_url", m.imageUrl(),
                "footer", "Foresignal · " + m.severity() + " · " + displayTime(m, s));
        Object payload = obj(
                "text", MarkdownRenderer.escape(m.title(), MarkdownRenderer.Format.ZULIP),
                "channel", s.config("channel", null),
                "username", s.config("username", null),
                "icon_url", s.config("iconUrl", null),
                "attachments", List.of(attachment));
        return json("POST", s.secret("webhookUrl"), null, payload, s);
    }
}
