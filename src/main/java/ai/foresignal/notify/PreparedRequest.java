package ai.foresignal.notify;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * One outbound request a channel type rendered: method, URL, headers and a body that is raw bytes,
 * form fields, multipart parts or an email (prompt 055: image attachments use the multipart body and
 * the email's inline images; prompt 060: an MQTT publish on the channel's supervised connection; prompt 084: a
 * LoRa mesh transmission).
 *
 * @param method {@code POST}, {@code PUT}, {@code PATCH}, {@code SMTP}, {@code PUBLISH} or {@code TRANSMIT} (mesh)
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
    public sealed interface Body permits Bytes, Form, Multipart, Mail, Mqtt, Mesh {}

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
     * A LoRa mesh transmission (prompt 084), sent through the {@link MeshDeliveryGateway}: every one passes the
     * channel's airtime governor first.
     *
     * @param channelId the channel whose governor and radio path carry it
     * @param network {@code meshtastic} (an MQTT JSON downlink) or {@code meshcore} (a companion command)
     * @param topic the MQTT topic of a Meshtastic downlink; {@code channel} or {@code dm} for MeshCore
     * @param payload the downlink JSON (Meshtastic) or the companion command payload without its envelope (MeshCore)
     * @param airtimeBytes the bytes that go on the air (text or datagram), before the network's overhead
     * @param severity {@code info}, {@code warning} or {@code critical}; critical jumps the governor's queue
     * @param shortCode the alert's short code, stored on the delivery row after the send, or null
     * @param deliveryId the delivery (0 for a channel test or a field-report reply)
     * @param folded how many alerts a digest line stands for beyond the first (0 for a single message)
     * @param requireAck true when a MeshCore direct message waits for the recipient's acknowledgement
     */
    public record Mesh(java.util.UUID channelId, String network, String topic, byte[] payload, int airtimeBytes,
            String severity, String shortCode, long deliveryId, int folded, boolean requireAck) implements Body {

        /** Requires the channel, network, topic and payload. */
        public Mesh {
            Objects.requireNonNull(channelId, "channelId");
            Objects.requireNonNull(network, "network");
            Objects.requireNonNull(topic, "topic");
            Objects.requireNonNull(payload, "payload");
            severity = severity == null ? "info" : severity;
        }

        /** True for a critical message. */
        public boolean critical() {
            return "critical".equals(severity);
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

    /** The body as UTF-8 text for a {@link Bytes} body, an MQTT payload or a mesh payload, else null (tests, logging). */
    public String bodyText() {
        if (body instanceof Mqtt m) {
            return m.message().text();
        }
        if (body instanceof Mesh m) {
            return new String(m.payload(), StandardCharsets.UTF_8);
        }
        return body instanceof Bytes b ? b.text() : null;
    }
}
