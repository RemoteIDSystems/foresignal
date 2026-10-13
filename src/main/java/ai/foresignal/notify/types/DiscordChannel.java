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
 *
 * <p>Prompt 055: a message with an image attachment is sent as {@code multipart/form-data} with a
 * {@code payload_json} part (the same payload plus {@code attachments[{id: 0, filename}]}) and a
 * {@code files[0]} part, and the embed shows it with {@code image.url = attachment://map.png}. An edit in
 * place sends the embed as JSON; Discord keeps the uploaded file.
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
        return new ChannelCapabilities(true, true, false);
    }

    @Override
    public ChannelRateLimit rateLimit() {
        return new ChannelRateLimit(5, Duration.ofSeconds(2));
    }

    @Override
    public PreparedRequest render(NotificationMessage m, ChannelSettings s) {
        NotificationMessage.Attachment image = m.image();
        Map<String, Object> embed = embed(m, s);
        if (image != null) {
            embed.put("image", obj("url", "attachment://" + image.filename()));
        }
        Map<String, Object> payload = obj(
                "username", s.config("username", null),
                "embeds", List.of(embed),
                "allowed_mentions", obj("parse", List.of()));
        String url = withQuery(s.secret("webhookUrl"), "wait=true");
        if (image == null) {
            return json("POST", url, null, payload, s);
        }
        // Prompt 055: multipart with payload_json and files[0]; the embed shows it as attachment://map.png.
        payload.put("attachments", List.of(obj("id", 0, "filename", image.filename())));
        List<PreparedRequest.Part> parts = List.of(
                new PreparedRequest.Part("payload_json", null, "application/json",
                        JSON.writeValueAsString(payload).getBytes(java.nio.charset.StandardCharsets.UTF_8)),
                new PreparedRequest.Part("files[0]", image.filename(), image.contentType(), image.bytes()));
        return new PreparedRequest("POST", url, baseHeaders(s), new PreparedRequest.Multipart(parts));
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
