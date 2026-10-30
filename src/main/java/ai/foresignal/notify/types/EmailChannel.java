package ai.foresignal.notify.types;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.notify.ChannelCapabilities;
import ai.foresignal.notify.ChannelFieldSpec;
import ai.foresignal.notify.ChannelRateLimit;
import ai.foresignal.notify.ChannelSettings;
import ai.foresignal.notify.ChannelTypeSpec;
import ai.foresignal.notify.MarkdownRenderer;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.OriginMarker;
import ai.foresignal.notify.PreparedRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * {@code email}: SMTP through a {@code JavaMailSender} built per channel (host, port, TLS mode, auth),
 * sending multipart/alternative plain text and HTML. Config {@code plainTextOnly} sends
 * {@code text/plain} only, for gateways such as Winlink. The origin marker travels as the
 * {@code X-Foresignal-Origin} MIME header. Lifecycle updates are follow-ups.
 *
 * <p>Prompt 055: a message with an image adds {@code <img src="cid:map">} to the HTML, and the sender
 * builds {@code multipart/alternative} of the plain text and a {@code multipart/related} holding the HTML
 * and the PNG ({@code Content-ID: <map>}). A plain-text-only channel sends no image.
 */
@Component
public class EmailChannel extends AbstractChannelType {

    /** The type id. */
    public static final String ID = "email";

    /** The {@code Content-ID} of the inline map (prompt 055). */
    public static final String CONTENT_ID = "map";

    private static final ChannelTypeSpec SPEC = new ChannelTypeSpec(ID, "Email",
            "SMTP email, multipart plain text and HTML (or plain text only)",
            List.of(ChannelFieldSpec.required("smtpHost", ChannelFieldSpec.STRING, "SMTP host"),
                    ChannelFieldSpec.optional("smtpPort", ChannelFieldSpec.INTEGER, "SMTP port", 587),
                    ChannelFieldSpec.choice("tls", false, "TLS mode", "starttls", List.of("starttls", "ssl", "none")),
                    ChannelFieldSpec.required("from", ChannelFieldSpec.STRING, "Sender address"),
                    ChannelFieldSpec.required("to", ChannelFieldSpec.STRING_LIST, "Recipient addresses"),
                    ChannelFieldSpec.optional("plainTextOnly", ChannelFieldSpec.BOOLEAN,
                            "Send text/plain only (Winlink gateways)", false)),
            List.of(ChannelFieldSpec.optional("username", ChannelFieldSpec.STRING, "SMTP user", null),
                    ChannelFieldSpec.optional("password", ChannelFieldSpec.STRING, "SMTP password", null)),
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
        return new ChannelCapabilities(false, true, false);
    }

    @Override
    public ChannelRateLimit rateLimit() {
        return ChannelRateLimit.perMinute(30);
    }

    /** The largest audio attachment (prompt 141), within common SMTP message limits (25 MB with encoding). */
    static final long AUDIO_MAX_BYTES = 18L * 1024 * 1024;

    /** Prompt 141: briefing audio is attached to the email (plain-text mode included). */
    @Override
    public long audioAttachmentLimit() {
        return AUDIO_MAX_BYTES;
    }

    @Override
    public List<FieldError> validate(Map<String, Object> config, Map<String, String> secrets) {
        List<FieldError> errors = new ArrayList<>();
        Object port = config.get("smtpPort");
        if (port instanceof Number n && (n.intValue() < 1 || n.intValue() > 65_535)) {
            errors.add(new FieldError("config.smtpPort", "must be between 1 and 65535"));
        }
        Object to = config.get("to");
        if (to instanceof List<?> list && list.isEmpty()) {
            errors.add(new FieldError("config.to", "must list at least one address"));
        }
        if ((secrets.get("username") == null) != (secrets.get("password") == null)) {
            errors.add(new FieldError("secrets", "username and password go together"));
        }
        return errors;
    }

    @Override
    public PreparedRequest render(NotificationMessage m, ChannelSettings s) {
        String host = s.config("smtpHost", "localhost");
        int port = Integer.parseInt(s.config("smtpPort", "587").replaceAll("\\.0+$", ""));
        StringBuilder text = new StringBuilder(m.title()).append("\n\n");
        String plain = MarkdownRenderer.render(m.bodyMarkdown(), MarkdownRenderer.Format.PLAIN);
        if (!plain.isBlank()) {
            text.append(plain).append("\n\n");
        }
        for (NotificationMessage.Link link : buttons(m)) {
            text.append(link.label()).append(": ").append(link.url()).append('\n');
        }
        text.append("-- \nForesignal · ").append(m.severity()).append(" · ").append(displayTime(m, s)).append('\n');
        String html = null;
        if (!s.flag("plainTextOnly", false) && editionHtml(m) != null) {
            // NP-004: an edition sent in the email format carries its whole sanitized page (BriefingHtmlRenderer).
            html = editionHtml(m);
        } else if (!s.flag("plainTextOnly", false)) {
            StringBuilder h = new StringBuilder("<html><body><h2>")
                    .append(MarkdownRenderer.escape(m.title(), MarkdownRenderer.Format.EMAIL_HTML)).append("</h2>");
            String body = MarkdownRenderer.render(m.bodyMarkdown(), MarkdownRenderer.Format.EMAIL_HTML);
            if (!body.isBlank()) {
                h.append("<p>").append(body).append("</p>");
            }
            if (m.image() != null) {
                h.append("<p><img src=\"cid:").append(CONTENT_ID).append("\" alt=\"Map\" style=\"max-width:100%\"></p>");
            }
            for (NotificationMessage.Link link : buttons(m)) {
                h.append("<p><a href=\"").append(MarkdownRenderer.escape(link.url(), MarkdownRenderer.Format.EMAIL_HTML)
                        .replace("\"", "&quot;")).append("\">")
                        .append(MarkdownRenderer.escape(link.label(), MarkdownRenderer.Format.EMAIL_HTML)).append("</a></p>");
            }
            h.append("<p style=\"color:#888\">Foresignal · ").append(m.severity()).append(" · ")
                    .append(MarkdownRenderer.escape(displayTime(m, s), MarkdownRenderer.Format.EMAIL_HTML))
                    .append("</p></body></html>");
            html = h.toString();
        }
        NotificationMessage.Attachment image = m.image();
        List<PreparedRequest.Inline> inlines = html == null || image == null ? List.of()
                : List.of(new PreparedRequest.Inline(CONTENT_ID, image.filename(), image.contentType(), image.bytes()));
        PreparedRequest.Mail mail = new PreparedRequest.Mail(host, port, s.config("tls", "starttls"),
                s.secret("username"), s.secret("password"), s.config("from", ""), s.list("to"),
                m.title().replace('\n', ' '), text.toString(), html,
                s.origin() == null ? Map.of() : Map.of(OriginMarker.HEADER, s.origin()), inlines,
                m.audio() == null ? List.of() : List.of(new PreparedRequest.FileAttachment(m.audio().filename(),
                        m.audio().contentType(), m.audio().bytes())));
        return new PreparedRequest("SMTP", "smtp://" + host + ":" + port, Map.of(), mail);
    }

    /** The data key of an edition's whole HTML page (NP-004). */
    public static final String EDITION_HTML = "editionHtml";

    /** The sanitized page of an edition sent in the email format (NP-004), or null for any other message. */
    static String editionHtml(NotificationMessage m) {
        if (!NotificationMessage.BRIEFING.equals(m.event()) || m.data() == null) {
            return null;
        }
        return m.data().get(EDITION_HTML) instanceof String page && !page.isBlank() ? page : null;
    }
}
