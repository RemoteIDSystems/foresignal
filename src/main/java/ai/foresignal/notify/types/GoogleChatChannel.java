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
import org.springframework.stereotype.Component;

/**
 * {@code google-chat}: an incoming webhook with a Card v2 (header with title and severity, a text
 * paragraph, link buttons). Lifecycle updates are follow-ups.
 */
@Component
public class GoogleChatChannel extends AbstractChannelType {

    /** The type id. */
    public static final String ID = "google-chat";

    /** The paragraph limit used here (Google Chat messages are limited to 32,000 bytes). */
    static final int TEXT_MAX = 20_000;

    private static final ChannelTypeSpec SPEC = new ChannelTypeSpec(ID, "Google Chat",
            "Google Chat space webhook with a Card v2",
            List.of(),
            List.of(ChannelFieldSpec.required("webhookUrl", ChannelFieldSpec.URL, "Space webhook URL")),
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
        List<Object> widgets = new ArrayList<>();
        String text = MarkdownRenderer.render(m.bodyMarkdown(), MarkdownRenderer.Format.GOOGLE_CHAT);
        if (!text.isBlank()) {
            widgets.add(obj("textParagraph", obj("text",
                    Truncator.truncate(text, TEXT_MAX, Truncator.Unit.CHARS, Truncator.Mode.HTML, m.url()))));
        }
        List<Object> buttons = new ArrayList<>();
        for (NotificationMessage.Link link : buttons(m)) {
            buttons.add(obj("text", link.label(), "onClick", obj("openLink", obj("url", link.url()))));
        }
        if (!buttons.isEmpty()) {
            widgets.add(obj("buttonList", obj("buttons", buttons)));
        }
        Object card = obj(
                "header", obj("title", m.title(), "subtitle", m.severity() + " · " + displayTime(m, s)),
                "sections", List.of(obj("widgets", widgets)));
        String cardId = "foresignal-" + (m.id() == null ? "test" : m.id());
        Object payload = obj("cardsV2", List.of(obj("cardId", cardId, "card", card)));
        return json("POST", s.secret("webhookUrl"), null, payload, s);
    }
}
