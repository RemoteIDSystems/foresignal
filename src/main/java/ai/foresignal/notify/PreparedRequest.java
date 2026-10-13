package ai.foresignal.notify;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * One outbound request a channel type rendered: method, URL, headers and a body that is raw bytes,
 * form fields, multipart parts or an email (prompt 055: image attachments use the multipart body and
 * the email's inline images; prompt 060: an MQTT publish on the channel's supervised connection).
 *
 * @param method {@code POST}, {@code PUT}, {@code PATCH}, {@code SMTP} or {@code PUBLISH}
 * @param url the target URL ({@code smtp://host:port} for email, the {@code brokerUrl} for MQTT)
 * @param headers the request headers, in order
 * @param body the body
 */
public record PreparedRequest(String method, String url, Map<String, String> headers, Body body) {

    /** Requires a method, a URL and a body; copies the headers in order. */
    public PreparedRequest {
        Objects.requireNonNull(method, "method");
        Objects.requireNonNull(url, "url");
        Objects.requireNonNull(body, "body");
        headers = headers == null ? Map.of() : java.util.Collections.unmodifiableMap(new LinkedHashMap<>(headers));
    }

    /** The body of a request. */
    public sealed interface Body permits Bytes, Form, Multipart, Mail, Mqtt {}

    /**
     * A raw body.
     *
     * @param contentType its media type
     * @param bytes the content
     */
    public record Bytes(String contentType, byte[] bytes) implements Body {

        /** UTF-8 text. */
        public static Bytes text(String contentType, String text) {
            return new Bytes(contentType, text.getBytes(StandardCharsets.UTF_8));
        }

        /** The content as UTF-8 text. */
        public String text() {
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    /**
     * An {@code application/x-www-form-urlencoded} body.
     *
     * @param fields the fields, in order
     */
    public record Form(Map<String, String> fields) implements Body {

        /** Copies in order. */
        public Form {
            fields = java.util.Collections.unmodifiableMap(new LinkedHashMap<>(fields));
        }
    }

    /**
     * A {@code multipart/form-data} body.
     *
     * @param parts the parts, in order
     */
    public record Multipart(List<Part> parts) implements Body {

        /** Copies. */
        public Multipart {
            parts = List.copyOf(parts);
        }
    }

    /**
     * One multipart part.
     *
     * @param name the form field name
     * @param filename the file name, or null for a plain field
     * @param contentType the part's media type, or null
     * @param bytes the content
     */
    public record Part(String name, String filename, String contentType, byte[] bytes) {

        /** A text field. */
        public static Part field(String name, String value) {
            return new Part(name, null, null, value.getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * An email sent over SMTP.
     *
     * @param host the SMTP host
     * @param port the SMTP port
     * @param tls {@code starttls}, {@code ssl} or {@code none}
     * @param username the SMTP user, or null
     * @param password the SMTP password, or null
     * @param from the sender address
     * @param to the recipients
     * @param subject the subject
     * @param text the plain-text part
     * @param html the HTML part, or null for a {@code text/plain}-only message
     * @param headers extra MIME headers
     * @param inlines images the HTML references as {@code cid:} (prompt 055), sent in a
     *     {@code multipart/related} with the HTML part
     */
    public record Mail(
            String host,
            int port,
            String tls,
            String username,
            String password,
            String from,
            List<String> to,
            String subject,
            String text,
            String html,
            Map<String, String> headers,
            List<Inline> inlines) implements Body {

        /** Copies. */
        public Mail {
            to = List.copyOf(to);
            headers = headers == null ? Map.of() : java.util.Collections.unmodifiableMap(new LinkedHashMap<>(headers));
            inlines = inlines == null ? List.of() : List.copyOf(inlines);
        }

        /** A mail without inline images (the 018 shape). */
        public Mail(String host, int port, String tls, String username, String password, String from, List<String> to,
                String subject, String text, String html, Map<String, String> headers) {
            this(host, port, tls, username, password, from, to, subject, text, html, headers, List.of());
        }
    }

    /**
     * An MQTT publish (prompt 060), sent on the channel's supervised connection rather than over HTTP.
     *
     * @param channelId the channel whose connection carries it
     * @param message the topic, payload, QoS, retain flag, expiry and content type
     */
    public record Mqtt(java.util.UUID channelId, ai.foresignal.mqtt.MqttMessage message) implements Body {

        /** Requires both. */
        public Mqtt {
            Objects.requireNonNull(channelId, "channelId");
            Objects.requireNonNull(message, "message");
        }
    }

    /**
     * An inline image of an email (prompt 055).
     *
     * @param contentId the {@code Content-ID} without angle brackets ({@code map}: {@code <img src="cid:map">})
     * @param filename the file name
     * @param contentType the media type
     * @param bytes the content
     */
    public record Inline(String contentId, String filename, String contentType, byte[] bytes) {}

    /** The body as UTF-8 text for a {@link Bytes} body or an MQTT payload, else null (tests, logging). */
    public String bodyText() {
        if (body instanceof Mqtt m) {
            return m.message().text();
        }
        return body instanceof Bytes b ? b.text() : null;
    }
}
