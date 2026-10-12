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
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * {@code discord}: {@code POST {webhookUrl}?wait=true} with one embed (title, description of at most
 * 4,096 characters, severity color, url, timestamp). The response's message {@code id} is stored, and
 * a lifecycle update edits that message with {@code PATCH {webhookUrl}/messages/{id}}. Mentions are
 * disabled, so a payload containing {@code @everyone} pings nobody.
 */
@Component
public class DiscordChannel extends AbstractChannelType {

    /** The type id. */
    public static final String ID = "discord";

    /** Discord's embed title limit. */
    static final int TITLE_MAX = 256;

    /** Discord's embed description limit. */
    static final int DESCRIPTION_MAX = 4096;

    private static final ChannelTypeSpec SPEC = new ChannelTypeSpec(ID, "Discord",
            "Discord webhook with an embed; lifecycle updates edit the original message",
            List.of(ChannelFieldSpec.optional("username", ChannelFieldSpec.STRING, "Display name override", null)),
            List.of(ChannelFieldSpec.required("webhookUrl", ChannelFieldSpec.URL, "Discord webhook URL")),
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
        return new ChannelRateLimit(5, Duration.ofSeconds(2));
    }

    @Override
    public PreparedRequest render(NotificationMessage m, ChannelSettings s) {
        Map<String, Object> payload = obj(
                "username", s.config("username", null),
                "embeds", List.of(embed(m, s)),
                "allowed_mentions", obj("parse", List.of()));
        return json("POST", withQuery(s.secret("webhookUrl"), "wait=true"), null, payload, s);
    }

    @Override
    public Optional<PreparedRequest> renderUpdate(NotificationMessage m, String ref, ChannelSettings s) {
        if (ref == null || ref.isBlank()) {
            return Optional.empty();
        }
        String url = s.secret("webhookUrl");
        int query = url.indexOf('?');
        String base = query < 0 ? url : url.substring(0, query);
        String target = ChannelSettings.trimSlash(base) + "/messages/" + ref + (query < 0 ? "" : url.substring(query));
        Map<String, Object> payload = obj("embeds", List.of(embed(m, s)), "allowed_mentions", obj("parse", List.of()));
        return Optional.of(json("PATCH", target, null, payload, s));
    }

    @Override
    public Optional<String> extractRef(ChannelResponse response) {
        return refAt(response, "id");
    }

    private static Map<String, Object> embed(NotificationMessage m, ChannelSettings s) {
        StringBuilder description = new StringBuilder(MarkdownRenderer.render(m.bodyMarkdown(), MarkdownRenderer.Format.DISCORD));
        for (NotificationMessage.Link link : m.links()) {
            if (!link.url().equals(m.url())) {
                description.append(description.length() == 0 ? "" : "\n")
                        .append('[').append(MarkdownRenderer.escape(link.label(), MarkdownRenderer.Format.DISCORD))
                        .append("](").append(link.url()).append(')');
            }
        }
        Instant at = m.createdAt() != null ? m.createdAt() : s.now();
        return obj(
                "title", Truncator.truncate(m.title(), TITLE_MAX, Truncator.Unit.CHARS, Truncator.Mode.PLAIN, null),
                "description", description.length() == 0 ? null
                        : Truncator.truncate(description.toString(), DESCRIPTION_MAX, Truncator.Unit.CHARS,
                                Truncator.Mode.MARKDOWN, m.url()),
                "url", m.url(),
                "color", color(m.severity()),
                "timestamp", at.toString(),
                "footer", obj("text", "Foresignal · " + m.severity()));
    }
}
