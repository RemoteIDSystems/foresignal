package ai.foresignal.notify;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * One outbound request a channel type rendered: method, URL, headers and a body that is raw bytes,
 * form fields, multipart parts (prompt 055 only adds parts) or an email.
 *
 * @param method {@code POST}, {@code PUT}, {@code PATCH} or {@code SMTP}
 * @param url the target URL ({@code smtp://host:port} for email)
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
    public sealed interface Body permits Bytes, Form, Multipart, Mail {}

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
            Map<String, String> headers) implements Body {

        /** Copies. */
        public Mail {
            to = List.copyOf(to);
            headers = headers == null ? Map.of() : java.util.Collections.unmodifiableMap(new LinkedHashMap<>(headers));
        }
    }

    /** The body as UTF-8 text for a {@link Bytes} body, else null (tests, logging). */
    public String bodyText() {
        return body instanceof Bytes b ? b.text() : null;
    }
}
