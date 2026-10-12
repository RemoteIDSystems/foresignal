package ai.foresignal.notify.types;

import ai.foresignal.notify.ChannelCapabilities;
import ai.foresignal.notify.ChannelFieldSpec;
import ai.foresignal.notify.ChannelRateLimit;
import ai.foresignal.notify.ChannelResponse;
import ai.foresignal.notify.ChannelSettings;
import ai.foresignal.notify.ChannelTypeSpec;
import ai.foresignal.notify.MarkdownRenderer;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.PreparedRequest;
import ai.foresignal.notify.Truncator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * {@code telegram}: Bot API {@code sendMessage} with {@code parse_mode=HTML}, link previews off and the
 * text cut to 4,096 characters by {@link Truncator} on a tag- and entity-safe boundary. The returned
 * {@code result.message_id} is stored; a lifecycle update calls {@code editMessageText} (or
 * {@code editMessageCaption} when the original was a photo, whose reference prompt 055 stores as
 * {@code photo:<id>}).
 *
 * <p>Link previews are disabled with {@code link_preview_options.is_disabled}, which replaced the
 * deprecated {@code disable_web_page_preview} parameter in Bot API 7.0.
 */
@Component
public class TelegramChannel extends AbstractChannelType {

    /** The type id. */
    public static final String ID = "telegram";

    /** Telegram's message text limit. */
    static final int TEXT_MAX = 4096;

    /** Telegram's caption limit. */
    static final int CAPTION_MAX = 1024;

    /** The reference prefix of a photo message (055). */
    public static final String PHOTO_REF = "photo:";

    private static final ChannelTypeSpec SPEC = new ChannelTypeSpec(ID, "Telegram",
            "Telegram bot message (HTML); lifecycle updates edit the original message",
            List.of(ChannelFieldSpec.required("chatId", ChannelFieldSpec.STRING, "Chat id (negative for groups)"),
                    ChannelFieldSpec.optional("apiBaseUrl", ChannelFieldSpec.URL, "Bot API base URL",
                            "https://api.telegram.org")),
            List.of(ChannelFieldSpec.required("botToken", ChannelFieldSpec.STRING, "Bot token from @BotFather")),
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
    public ChannelCapabilities capabilities() {
        return new ChannelCapabilities(true, false, false);
    }

    @Override
    public ChannelRateLimit rateLimit() {
        return ChannelRateLimit.perMinute(20);
    }

    @Override
    public PreparedRequest render(NotificationMessage m, ChannelSettings s) {
        Map<String, Object> payload = obj(
                "chat_id", s.config("chatId", ""),
                "text", text(m, TEXT_MAX),
                "parse_mode", "HTML",
                "link_preview_options", obj("is_disabled", true));
        return json("POST", method(s, "sendMessage"), null, payload, s);
    }

    @Override
    public Optional<PreparedRequest> renderUpdate(NotificationMessage m, String ref, ChannelSettings s) {
        if (ref == null || ref.isBlank()) {
            return Optional.empty();
        }
        if (ref.startsWith(PHOTO_REF)) {
            Map<String, Object> payload = obj(
                    "chat_id", s.config("chatId", ""),
                    "message_id", messageId(ref.substring(PHOTO_REF.length())),
                    "caption", text(m, CAPTION_MAX),
                    "parse_mode", "HTML");
            return Optional.of(json("POST", method(s, "editMessageCaption"), null, payload, s));
        }
        Map<String, Object> payload = obj(
                "chat_id", s.config("chatId", ""),
                "message_id", messageId(ref),
                "text", text(m, TEXT_MAX),
                "parse_mode", "HTML",
                "link_preview_options", obj("is_disabled", true));
        return Optional.of(json("POST", method(s, "editMessageText"), null, payload, s));
    }

    @Override
    public Optional<String> extractRef(ChannelResponse response) {
        return refAt(response, "result", "message_id");
    }

    /** {@code <b>title</b>}, the body and the links, in Telegram HTML, cut to {@code max}. */
    static String text(NotificationMessage m, int max) {
        StringBuilder out = new StringBuilder("<b>")
                .append(MarkdownRenderer.escape(m.title(), MarkdownRenderer.Format.TELEGRAM_HTML)).append("</b>");
        String body = MarkdownRenderer.render(m.bodyMarkdown(), MarkdownRenderer.Format.TELEGRAM_HTML);
        if (!body.isBlank()) {
            out.append("\n\n").append(body);
        }
        List<NotificationMessage.Link> links = buttons(m);
        if (!links.isEmpty()) {
            out.append("\n");
            for (NotificationMessage.Link link : links) {
                out.append("\n<a href=\"").append(MarkdownRenderer.escape(link.url(), MarkdownRenderer.Format.TELEGRAM_HTML)
                        .replace("\"", "&quot;")).append("\">")
                        .append(MarkdownRenderer.escape(link.label(), MarkdownRenderer.Format.TELEGRAM_HTML)).append("</a>");
            }
        }
        return Truncator.truncate(out.toString(), max, Truncator.Unit.CHARS, Truncator.Mode.HTML, m.url());
    }

    private static String method(ChannelSettings s, String name) {
        return ChannelSettings.trimSlash(s.config("apiBaseUrl", "https://api.telegram.org"))
                + "/bot" + s.secret("botToken") + "/" + name;
    }

    private static Object messageId(String ref) {
        try {
            return Long.parseLong(ref.strip());
        } catch (NumberFormatException e) {
            return ref;
        }
    }
}
