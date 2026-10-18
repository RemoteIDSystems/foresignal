package ai.foresignal.notify.types;

import ai.foresignal.notify.ChannelCapabilities;
import ai.foresignal.notify.ChannelFieldSpec;
import ai.foresignal.notify.ChannelResponse;
import ai.foresignal.notify.ChannelRateLimit;
import ai.foresignal.notify.ChannelSettings;
import ai.foresignal.notify.ChannelTypeSpec;
import ai.foresignal.notify.MarkdownRenderer;
import ai.foresignal.notify.NotificationAction;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.OriginMarker;
import ai.foresignal.notify.PreparedRequest;
import ai.foresignal.notify.Truncator;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * {@code ntfy}: {@code POST {serverUrl}/{topic}} with the markdown body and the publish headers
 * {@code Title}, {@code Priority} (info 3, warning 4, critical 5), {@code Tags} (the message tags plus
 * the {@link OriginMarker} tag), {@code Click}, {@code Actions} (view actions from the links) and
 * {@code Markdown: yes}; an optional access token as a bearer.
 *
 * <p>With config {@code call} (a phone number, or {@code yes} for the account's first verified
 * number), {@code X-Call} is sent on <em>critical</em> messages only; that needs an ntfy account with
 * a verified phone number. Header values outside ASCII are RFC 2047 encoded, which ntfy decodes.
 * Lifecycle updates are follow-ups.
 *
 * <p>A firing alert with action capabilities (prompt 033) gets up to three actions instead of the
 * link views: {@code http} actions for the first two capabilities ({@code method=POST},
 * {@code clear=true}, so one tap acts without a token on the phone) and {@code view, Open} to the web
 * link, else the API link.
 *
 * <p>Prompt 055: a message with an image is a {@code PUT {serverUrl}/{topic}} whose body is the PNG, with
 * {@code Filename: map.png} and the text in the {@code Message} header. The server needs attachments
 * enabled ({@code attachment-cache-dir} and {@code base-url}); a 413, or a 400 with code 40014, makes the
 * worker send the text message instead ({@link #attachmentRefused}).
 */
@Component
public class NtfyChannel extends AbstractChannelType {

    /** The type id. */
    public static final String ID = "ntfy";

    /** ntfy's error code for "attachments not allowed" (server.errors.go, 40014). */
    static final String ATTACHMENTS_DISALLOWED = "40014";

    /** ntfy's default message size limit, in bytes. */
    static final int BODY_MAX_BYTES = 4096;

    private static final ChannelTypeSpec SPEC = new ChannelTypeSpec(ID, "ntfy",
            "ntfy topic (priority from severity, click URL, view actions)",
            List.of(ChannelFieldSpec.optional("serverUrl", ChannelFieldSpec.URL, "ntfy server", "https://ntfy.sh"),
                    ChannelFieldSpec.required("topic", ChannelFieldSpec.STRING, "Topic name"),
                    ChannelFieldSpec.optional("call", ChannelFieldSpec.STRING,
                            "Phone number or 'yes': voice call on critical (needs a verified number)", null)),
            List.of(ChannelFieldSpec.optional("accessToken", ChannelFieldSpec.STRING, "Access token", null)),
            null);

    @Override
    public String id() {
        return ID;
    }

    @Override
    public ChannelTypeSpec spec() {
        return SPEC;
    }

    /** ntfy's default {@code attachment-file-size-limit} (15 MB). */
    static final long AUDIO_MAX_BYTES = 15L * 1024 * 1024;

    /** Prompt 141: briefing audio is sent as an attachment, like a map image. */
    @Override
    public long audioAttachmentLimit() {
        return AUDIO_MAX_BYTES;
    }

    @Override
    public ChannelCapabilities capabilities() {
        return new ChannelCapabilities(false, true, false);
    }

    @Override
    public ChannelRateLimit rateLimit() {
        return ChannelRateLimit.perSecond(10);
    }

    @Override
    public PreparedRequest render(NotificationMessage m, ChannelSettings s) {
        Map<String, String> headers = baseHeaders(s);
        headers.put("Content-Type", "text/markdown; charset=utf-8");
        headers.put("Title", header(m.title()));
        headers.put("Priority", switch (m.severity()) {
            case "critical" -> "5";
            case "warning" -> "4";
            default -> "3";
        });
        List<String> tags = new ArrayList<>(m.tags());
        tags.add(OriginMarker.tag(s.origin() == null ? "unknown" : s.origin()));
        headers.put("Tags", header(String.join(",", tags.stream().map(t -> t.replace(",", " ")).toList())));
        if (m.url() != null) {
            headers.put("Click", m.url());
        }
        List<String> actions = new ArrayList<>();
        if (!s.actions().isEmpty()) {
            // Prompt 033: two one-tap http actions (POST to the capability, no token on the device) and Open.
            for (NotificationAction action : s.actions().subList(0, Math.min(2, s.actions().size()))) {
                actions.add("http, " + clean(action.label()) + ", " + action.url()
                        + ", method=POST, headers.Accept=application/json, clear=true");
            }
            String open = openLink(m);
            if (open != null) {
                actions.add("view, Open, " + open);
            }
        } else {
            for (NotificationMessage.Link link : m.links()) {
                actions.add("view, " + clean(link.label()) + ", " + link.url());
            }
        }
        if (!actions.isEmpty()) {
            headers.put("Actions", header(String.join("; ", actions)));
        }
        headers.put("Markdown", "yes");
        String token = s.secret("accessToken");
        if (token != null) {
            headers.put("Authorization", "Bearer " + token);
        }
        String call = s.config("call", null);
        if (call != null && "critical".equals(m.severity())) {
            headers.put("X-Call", call);
        }
        String body = MarkdownRenderer.render(m.bodyMarkdown(), MarkdownRenderer.Format.ZULIP);
        if (body.isBlank()) {
            body = m.title();
        }
        body = Truncator.truncate(body, BODY_MAX_BYTES, Truncator.Unit.UTF8_BYTES, Truncator.Mode.MARKDOWN, m.url());
        String url = ChannelSettings.trimSlash(s.config("serverUrl", "https://ntfy.sh")) + "/" + s.config("topic", "");
        NotificationMessage.Attachment image = m.image() != null ? m.image() : m.audio();
        if (image != null) {
            // Prompt 055 (prompt 141: also briefing audio): PUT the file as the body with Filename; the text travels in the Message header,
            // newlines as the literal \n that ntfy turns back into newlines, non-ASCII RFC 2047 encoded.
            headers.put("Content-Type", image.contentType());
            headers.put("Filename", image.filename());
            headers.put("Message", messageHeader(body));
            return new PreparedRequest("PUT", url, headers, new PreparedRequest.Bytes(image.contentType(), image.bytes()));
        }
        return new PreparedRequest("POST", url, headers, PreparedRequest.Bytes.text("text/markdown; charset=utf-8", body));
    }

    /**
     * True when ntfy refused the attachment: 413 (code 41301, attachment too large or bandwidth reached) or
     * 400 code 40014 (attachments not allowed on the server). The worker then sends the text instead.
     */
    @Override
    public boolean attachmentRefused(ChannelResponse response) {
        if (response.status() == 413) {
            return true;
        }
        return response.status() == 400 && response.body() != null && response.body().contains(ATTACHMENTS_DISALLOWED);
    }

    /** {@code text} as an ntfy {@code Message} header: newlines as the literal {@code \n}, then {@link #header}. */
    static String messageHeader(String text) {
        return header(text.replace("\r\n", "\n").replace("\r", "\n").replace("\n", "\\n"));
    }

    /** A label without the separators of ntfy's short action format. */
    static String clean(String label) {
        return label.replace(",", " ").replace(";", " ");
    }

    /** The {@code Open} target: an https web link (023 {@code clientLinks}) when present, else the API link. */
    static String openLink(NotificationMessage m) {
        for (NotificationMessage.Link link : m.links()) {
            if (link.url().startsWith("https://") && !link.url().equals(m.url())) {
                return link.url();
            }
        }
        return m.url();
    }

    /** {@code value} as is when it is printable ASCII, else RFC 2047 base64 UTF-8. */
    static String header(String value) {
        boolean ascii = value.chars().allMatch(c -> c >= 0x20 && c < 0x7F);
        if (ascii) {
            return value;
        }
        String clean = value.replace('\r', ' ').replace('\n', ' ');
        return "=?UTF-8?B?" + Base64.getEncoder().encodeToString(clean.getBytes(StandardCharsets.UTF_8)) + "?=";
    }
}
