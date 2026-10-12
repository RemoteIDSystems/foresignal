package ai.foresignal.notify.types;

import ai.foresignal.notify.ChannelFieldSpec;
import ai.foresignal.notify.ChannelRateLimit;
import ai.foresignal.notify.ChannelSettings;
import ai.foresignal.notify.ChannelTypeSpec;
import ai.foresignal.notify.MarkdownRenderer;
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
 */
@Component
public class NtfyChannel extends AbstractChannelType {

    /** The type id. */
    public static final String ID = "ntfy";

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
        for (NotificationMessage.Link link : m.links()) {
            actions.add("view, " + link.label().replace(",", " ").replace(";", " ") + ", " + link.url());
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
        return new PreparedRequest("POST", url, headers, PreparedRequest.Bytes.text("text/markdown; charset=utf-8", body));
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
