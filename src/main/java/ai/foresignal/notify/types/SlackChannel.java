package ai.foresignal.notify.types;

import ai.foresignal.notify.ChannelFieldSpec;
import ai.foresignal.notify.ChannelRateLimit;
import ai.foresignal.notify.ChannelSettings;
import ai.foresignal.notify.ChannelTypeSpec;
import ai.foresignal.notify.MarkdownRenderer;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.PreparedRequest;
import ai.foresignal.notify.Truncator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * {@code slack}: an incoming webhook with a Block Kit message: a header, a mrkdwn section, a context
 * line (severity and time) and link buttons, with a {@code text} fallback. Incoming webhooks cannot
 * edit messages, so lifecycle updates are follow-ups.
 */
@Component
public class SlackChannel extends AbstractChannelType {

    /** The type id. */
    public static final String ID = "slack";

    /** Slack's header text limit. */
    static final int HEADER_MAX = 150;

    /** Slack's section text limit. */
    static final int SECTION_MAX = 3000;

    /** Slack's button label limit. */
    static final int BUTTON_MAX = 75;

    private static final ChannelTypeSpec SPEC = new ChannelTypeSpec(ID, "Slack",
            "Slack incoming webhook (Block Kit)",
            List.of(ChannelFieldSpec.optional("username", ChannelFieldSpec.STRING, "Display name override", null),
                    ChannelFieldSpec.optional("iconEmoji", ChannelFieldSpec.STRING, "Icon emoji, e.g. :satellite:", null)),
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
        List<Object> blocks = new ArrayList<>();
        String header = Truncator.truncate(m.title(), HEADER_MAX, Truncator.Unit.CHARS, Truncator.Mode.PLAIN, null);
        blocks.add(obj("type", "header", "text", obj("type", "plain_text", "text", header, "emoji", true)));
        String body = MarkdownRenderer.render(m.bodyMarkdown(), MarkdownRenderer.Format.SLACK);
        if (!body.isBlank()) {
            String section = Truncator.truncate(body, SECTION_MAX, Truncator.Unit.CHARS, Truncator.Mode.PLAIN,
                    m.url() == null ? null : "<" + MarkdownRenderer.escape(m.url(), MarkdownRenderer.Format.SLACK) + "|more>");
            blocks.add(obj("type", "section", "text", obj("type", "mrkdwn", "text", section)));
        }
        String context = "Severity: *" + m.severity() + "* · " + displayTime(m, s);
        blocks.add(obj("type", "context", "elements", List.of(obj("type", "mrkdwn", "text", context))));
        List<Object> buttons = new ArrayList<>();
        for (NotificationMessage.Link link : buttons(m)) {
            String label = Truncator.truncate(link.label(), BUTTON_MAX, Truncator.Unit.CHARS, Truncator.Mode.PLAIN, null);
            buttons.add(obj("type", "button", "text", obj("type", "plain_text", "text", label), "url", link.url()));
        }
        if (!buttons.isEmpty()) {
            blocks.add(obj("type", "actions", "elements", buttons));
        }
        Map<String, Object> payload = obj(
                "text", MarkdownRenderer.escape(m.title(), MarkdownRenderer.Format.SLACK),
                "username", s.config("username", null),
                "icon_emoji", s.config("iconEmoji", null),
                "blocks", blocks);
        return json("POST", s.secret("webhookUrl"), null, payload, s);
    }
}
