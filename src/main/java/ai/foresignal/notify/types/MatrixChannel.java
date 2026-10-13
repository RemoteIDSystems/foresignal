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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * {@code matrix}: {@code PUT {homeserver}/_matrix/client/v3/rooms/{roomId}/send/m.room.message/{txnId}}
 * with an {@code org.matrix.custom.html} body. The transaction id is the delivery id, so a retried
 * delivery is idempotent on the homeserver. The returned {@code event_id} is stored; a lifecycle
 * update sends a new event whose {@code body} starts with {@code "* "}, carrying {@code m.new_content}
 * and {@code m.relates_to {rel_type: "m.replace", event_id}} (an edit).
 *
 * <p>Prompt 061: with {@code interactive: true} the channel also accepts commands from its unencrypted
 * {@code allowedChats} rooms ({@code ai.foresignal.chatops.MatrixConnector}).
 */
@Component
public class MatrixChannel extends AbstractChannelType {

    /** The type id. */
    public static final String ID = "matrix";

    /** The HTML format id. */
    static final String FORMAT = "org.matrix.custom.html";

    private static final ChannelTypeSpec SPEC = new ChannelTypeSpec(ID, "Matrix",
            "Matrix room message (HTML); lifecycle updates edit the original event",
            TelegramChannel.fields(List.of(ChannelFieldSpec.required("homeserverUrl", ChannelFieldSpec.URL, "Homeserver base URL"),
                    ChannelFieldSpec.required("roomId", ChannelFieldSpec.STRING, "Room id, e.g. !abc:example.org")), false),
            List.of(ChannelFieldSpec.required("accessToken", ChannelFieldSpec.STRING, "Bot user's access token")),
            null);

    @Override
    public String id() {
        return ID;
    }

    @Override
    public ChannelTypeSpec spec() {
        return SPEC;
    }

    /** Prompt 061: the interactive members ({@code allowedChats}, rate limits). */
    @Override
    public List<ai.foresignal.api.error.FieldError> validate(Map<String, Object> config, Map<String, String> secrets) {
        return ai.foresignal.chatops.InteractiveConfig.validate(ID, config);
    }

    @Override
    public ChannelCapabilities capabilities() {
        return new ChannelCapabilities(true, false, false);
    }

    @Override
    public ChannelRateLimit rateLimit() {
        return ChannelRateLimit.perSecond(5);
    }

    @Override
    public PreparedRequest render(NotificationMessage m, ChannelSettings s) {
        Map<String, Object> content = content(m, "", "");
        return json("PUT", url(s), auth(s), content, s);
    }

    @Override
    public Optional<PreparedRequest> renderUpdate(NotificationMessage m, String ref, ChannelSettings s) {
        if (ref == null || ref.isBlank()) {
            return Optional.empty();
        }
        Map<String, Object> content = content(m, "* ", "* ");
        content.put("m.new_content", content(m, "", ""));
        content.put("m.relates_to", obj("rel_type", "m.replace", "event_id", ref));
        return Optional.of(json("PUT", url(s), auth(s), content, s));
    }

    @Override
    public Optional<String> extractRef(ChannelResponse response) {
        return refAt(response, "event_id");
    }

    private static Map<String, Object> content(NotificationMessage m, String plainPrefix, String htmlPrefix) {
        String plainBody = MarkdownRenderer.render(m.bodyMarkdown(), MarkdownRenderer.Format.PLAIN);
        String htmlBody = MarkdownRenderer.render(m.bodyMarkdown(), MarkdownRenderer.Format.MATRIX_HTML);
        StringBuilder plain = new StringBuilder(plainPrefix).append(m.title());
        StringBuilder html = new StringBuilder(htmlPrefix).append("<strong>")
                .append(MarkdownRenderer.escape(m.title(), MarkdownRenderer.Format.MATRIX_HTML)).append("</strong>");
        if (!plainBody.isBlank()) {
            plain.append("\n\n").append(plainBody);
            html.append("<br>").append(htmlBody);
        }
        for (NotificationMessage.Link link : buttons(m)) {
            plain.append("\n").append(link.label()).append(": ").append(link.url());
            html.append("<br><a href=\"").append(MarkdownRenderer.escape(link.url(), MarkdownRenderer.Format.MATRIX_HTML)
                    .replace("\"", "&quot;")).append("\">")
                    .append(MarkdownRenderer.escape(link.label(), MarkdownRenderer.Format.MATRIX_HTML)).append("</a>");
        }
        return obj("msgtype", "m.text", "body", plain.toString(), "format", FORMAT, "formatted_body", html.toString());
    }

    private static String url(ChannelSettings s) {
        String txn = s.deliveryId() > 0 ? Long.toString(s.deliveryId()) : "test-" + s.now().toEpochMilli();
        return ChannelSettings.trimSlash(s.config("homeserverUrl", "")) + "/_matrix/client/v3/rooms/"
                + URLEncoder.encode(s.config("roomId", ""), StandardCharsets.UTF_8).replace("+", "%20")
                + "/send/m.room.message/" + txn;
    }

    private static Map<String, String> auth(ChannelSettings s) {
        return Map.of("Authorization", "Bearer " + s.secret("accessToken"));
    }
}
