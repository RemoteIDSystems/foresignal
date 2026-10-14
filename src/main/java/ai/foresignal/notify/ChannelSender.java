package ai.foresignal.notify;

import ai.foresignal.net.OutboundUrlDeniedException;
import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.net.PinnedHttp;
import jakarta.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 * Sends a {@link PreparedRequest} (prompt 018).
 *
 * <p>HTTP goes through {@link PinnedHttp} clients on virtual threads with the configured connect
 * timeout (5 s), a per-request timeout (15 s) and <strong>no redirects</strong>: a 3xx is returned as
 * it is and counts as a failure, so a receiver cannot bounce a delivery (and its secrets in headers)
 * to another host. Email builds a {@link JavaMailSenderImpl} per request from the channel's host,
 * port, TLS mode and credentials, and reports an accepted message as status 250.
 *
 * <p><strong>Outbound URL policy (prompt 035).</strong> Every request, including the 033 pollers'
 * {@code getUpdates}, {@code /sync} and receipt calls that also go through here, is checked with
 * {@link OutboundUrlPolicy} profile {@code LAN_ALLOWED} just before it is sent: public, loopback,
 * private, unique-local and shared 100.64/10 destinations are allowed (a LAN Gotify or ntfy), link-local,
 * metadata and the other blocked addresses never. An HTTP request then connects to the checked
 * address with the original {@code Host} and TLS name, so a DNS answer that changed since the channel
 * was saved cannot redirect it (DNS rebinding). The SMTP host gets the same check
 * ({@link OutboundUrlPolicy#checkHost}); JavaMail resolves it again right after, which the JVM's DNS
 * cache answers with the checked address, because connecting to a literal would break JavaMail's
 * server identity check. A refusal is an {@link OutboundUrlDeniedException}: a delivery failure.
 *
 * <p><strong>MQTT (prompt 060).</strong> A {@link PreparedRequest.Mqtt} body is published on the channel's
 * supervised connection through the {@link MqttDeliveryGateway}, which applies the same policy to the broker
 * host when it connects. PUBACK (QoS 1) or a completed write (QoS 0) is reported as status 200; a broker that
 * is not connected, a refusal or a timeout is an {@link IOException}, so the delivery follows the retry
 * ladder.
 *
 * <p><strong>Mesh (prompt 084).</strong> A {@link PreparedRequest.Mesh} body goes to the {@link MeshDeliveryGateway},
 * which passes it through the channel's airtime governor before it reaches the radio path; a governor without room
 * answers {@link SendDeferredException}, which the delivery worker turns into a deferral.
 */
@Component
public class ChannelSender {

    /** Headers the JDK client sets itself and refuses from callers. */
    static final Set<String> RESTRICTED = Set.of("connection", "content-length", "expect", "host", "upgrade");

    /** The SMTP status reported for an accepted email. */
    public static final int SMTP_OK = 250;

    /** The outbound profile of every channel destination. */
    public static final OutboundUrlPolicy.Profile PROFILE = OutboundUrlPolicy.Profile.LAN_ALLOWED;

    private final NotifyProperties properties;
    private final OutboundUrlPolicy policy;
    private final PinnedHttp pinned;
    private final PinnedHttp.ClientSpec spec;

    /** The MQTT publisher (prompt 060), looked up lazily because it depends on the channel service. */
    private org.springframework.beans.factory.ObjectProvider<MqttDeliveryGateway> mqttGateway;

    /** Creates the sender. */
    public ChannelSender(NotifyProperties properties, OutboundUrlPolicy policy, PinnedHttp pinned) {
        this.properties = properties;
        this.policy = policy;
        this.pinned = pinned;
        // HTTP/1.1: over HTTP/2 the JDK derives :authority from the (pinned, literal) URI rather than
        // from the Host header, which virtual-hosted receivers would refuse.
        this.spec = new PinnedHttp.ClientSpec(properties.connectTimeout(), HttpClient.Version.HTTP_1_1);
    }

    /** The mesh transmitter (prompt 084), looked up lazily because it depends on the channel service. */
    private org.springframework.beans.factory.ObjectProvider<MeshDeliveryGateway> meshGateway;

    /** Injects the mesh transmitter (prompt 084); absent in hand-built unit tests. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setMeshGateway(org.springframework.beans.factory.ObjectProvider<MeshDeliveryGateway> gateway) {
        this.meshGateway = gateway;
    }

    /** Injects the MQTT publisher (prompt 060); absent in hand-built unit tests. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setMqttGateway(org.springframework.beans.factory.ObjectProvider<MqttDeliveryGateway> gateway) {
        this.mqttGateway = gateway;
    }

    /**
     * Sends {@code request} and returns the response.
     *
     * @throws OutboundUrlDeniedException when the outbound URL policy refuses the destination
     * @throws IOException when the destination cannot be reached or the exchange fails
     */
    public ChannelResponse send(PreparedRequest request) throws IOException {
        return send(request, properties.requestTimeout());
    }

    /**
     * {@link #send(PreparedRequest)} with an explicit HTTP request timeout, for long polls (prompt 061:
     * Telegram {@code getUpdates} and Matrix {@code /sync} wait up to 50 s for news, longer than the
     * channel request timeout). Mail and MQTT bodies ignore it.
     *
     * @throws OutboundUrlDeniedException when the outbound URL policy refuses the destination
     * @throws IOException when the destination cannot be reached or the exchange fails
     */
    public ChannelResponse send(PreparedRequest request, java.time.Duration requestTimeout) throws IOException {
        if (request.body() instanceof PreparedRequest.Mqtt mqtt) {
            MqttDeliveryGateway gateway = mqttGateway == null ? null : mqttGateway.getIfAvailable();
            if (gateway == null) {
                throw new IOException("MQTT is not available");
            }
            return gateway.publish(mqtt);
        }
        if (request.body() instanceof PreparedRequest.Mesh mesh) {
            MeshDeliveryGateway gateway = meshGateway == null ? null : meshGateway.getIfAvailable();
            if (gateway == null) {
                throw new IOException("mesh transmission is not available");
            }
            return gateway.transmit(mesh);
        }
        if (request.body() instanceof PreparedRequest.Mail mail) {
            OutboundUrlDeniedException.require(policy.checkHost(mail.host(), PROFILE));
            return sendMail(mail);
        }
        URI uri;
        try {
            uri = URI.create(request.url());
        } catch (IllegalArgumentException e) {
            throw new OutboundUrlDeniedException("the destination URL is not valid");
        }
        OutboundUrlPolicy.Allowed allowed = OutboundUrlDeniedException.require(policy.check(uri, PROFILE));
        HttpRequest.Builder builder = HttpRequest.newBuilder(PinnedHttp.pinnedUri(uri, allowed.pinned()))
                .timeout(requestTimeout == null ? properties.requestTimeout() : requestTimeout);
        PinnedHttp.setHostHeader(builder, uri);
        String contentType = null;
        for (Map.Entry<String, String> header : request.headers().entrySet()) {
            String name = header.getKey().toLowerCase(Locale.ROOT);
            if (RESTRICTED.contains(name) || header.getValue() == null) {
                continue;
            }
            if (name.equals("content-type")) {
                contentType = header.getValue();
                continue;
            }
            builder.header(header.getKey(), header.getValue());
        }
        HttpRequest.BodyPublisher publisher;
        switch (request.body()) {
            case PreparedRequest.Bytes b -> {
                publisher = HttpRequest.BodyPublishers.ofByteArray(b.bytes());
                contentType = contentType != null ? contentType : b.contentType();
            }
            case PreparedRequest.Form f -> {
                publisher = HttpRequest.BodyPublishers.ofString(formEncode(f.fields()));
                contentType = "application/x-www-form-urlencoded";
            }
            case PreparedRequest.Multipart m -> {
                String boundary = "foresignal-" + UUID.randomUUID();
                publisher = HttpRequest.BodyPublishers.ofByteArray(multipart(m.parts(), boundary));
                contentType = "multipart/form-data; boundary=" + boundary;
            }
            case PreparedRequest.Mail mail -> throw new IllegalStateException("unreachable");
            case PreparedRequest.Mqtt mqtt -> throw new IllegalStateException("unreachable");
            case PreparedRequest.Mesh mesh -> throw new IllegalStateException("unreachable");
        }
        if (contentType != null) {
            builder.header("Content-Type", contentType);
        }
        builder.method(request.method(), publisher);
        try {
            HttpResponse<String> response = pinned.client(uri, spec).send(builder.build(),
                    HttpResponse.BodyHandlers.ofString());
            return new ChannelResponse(response.statusCode(), response.headers().map(), response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("interrupted", e);
        }
    }

    /** {@code application/x-www-form-urlencoded} of {@code fields}. */
    static String formEncode(Map<String, String> fields) {
        StringJoiner out = new StringJoiner("&");
        fields.forEach((k, v) -> out.add(URLEncoder.encode(k, StandardCharsets.UTF_8) + "="
                + URLEncoder.encode(v == null ? "" : v, StandardCharsets.UTF_8)));
        return out.toString();
    }

    /** A {@code multipart/form-data} body. */
    static byte[] multipart(List<PreparedRequest.Part> parts, String boundary) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (PreparedRequest.Part part : parts) {
            StringBuilder head = new StringBuilder("--").append(boundary).append("\r\n")
                    .append("Content-Disposition: form-data; name=\"").append(part.name().replace("\"", "")).append('"');
            if (part.filename() != null) {
                head.append("; filename=\"").append(part.filename().replace("\"", "")).append('"');
            }
            head.append("\r\n");
            if (part.contentType() != null) {
                head.append("Content-Type: ").append(part.contentType()).append("\r\n");
            }
            head.append("\r\n");
            out.writeBytes(head.toString().getBytes(StandardCharsets.UTF_8));
            out.writeBytes(part.bytes());
            out.writeBytes("\r\n".getBytes(StandardCharsets.UTF_8));
        }
        out.writeBytes(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        return out.toByteArray();
    }

    /**
     * The body of an email with inline images (prompt 055): {@code multipart/alternative} of the plain text
     * and a {@code multipart/related} that holds the HTML part and each image ({@code Content-ID: <id>},
     * {@code Content-Disposition: inline}), so a client without HTML still reads the text.
     */
    static jakarta.mail.internet.MimeMultipart alternativeWithInlines(PreparedRequest.Mail mail)
            throws jakarta.mail.MessagingException {
        jakarta.mail.internet.MimeMultipart alternative = new jakarta.mail.internet.MimeMultipart("alternative");
        jakarta.mail.internet.MimeBodyPart text = new jakarta.mail.internet.MimeBodyPart();
        text.setText(mail.text(), "UTF-8");
        alternative.addBodyPart(text);
        jakarta.mail.internet.MimeMultipart related = new jakarta.mail.internet.MimeMultipart("related");
        jakarta.mail.internet.MimeBodyPart html = new jakarta.mail.internet.MimeBodyPart();
        html.setText(mail.html(), "UTF-8", "html");
        related.addBodyPart(html);
        for (PreparedRequest.Inline inline : mail.inlines()) {
            jakarta.mail.internet.MimeBodyPart image = new jakarta.mail.internet.MimeBodyPart();
            image.setDataHandler(new jakarta.activation.DataHandler(
                    new jakarta.mail.util.ByteArrayDataSource(inline.bytes(), inline.contentType())));
            image.setContentID("<" + inline.contentId() + ">");
            image.setDisposition(jakarta.mail.Part.INLINE);
            image.setFileName(inline.filename());
            related.addBodyPart(image);
        }
        jakarta.mail.internet.MimeBodyPart relatedPart = new jakarta.mail.internet.MimeBodyPart();
        relatedPart.setContent(related);
        alternative.addBodyPart(relatedPart);
        return alternative;
    }

    private ChannelResponse sendMail(PreparedRequest.Mail mail) throws IOException {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(mail.host());
        sender.setPort(mail.port());
        boolean ssl = "ssl".equals(mail.tls());
        sender.setProtocol(ssl ? "smtps" : "smtp");
        sender.setDefaultEncoding("UTF-8");
        String prefix = ssl ? "mail.smtps." : "mail.smtp.";
        Properties props = new Properties();
        props.put(prefix + "connectiontimeout", Long.toString(properties.connectTimeout().toMillis()));
        props.put(prefix + "timeout", Long.toString(properties.requestTimeout().toMillis()));
        props.put(prefix + "writetimeout", Long.toString(properties.requestTimeout().toMillis()));
        if ("starttls".equals(mail.tls())) {
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
        }
        if (mail.username() != null) {
            sender.setUsername(mail.username());
            sender.setPassword(mail.password());
            props.put(prefix + "auth", "true");
        }
        sender.setJavaMailProperties(props);
        try {
            MimeMessage message = sender.createMimeMessage();
            if (mail.html() != null && !mail.inlines().isEmpty()) {
                message.setFrom(new jakarta.mail.internet.InternetAddress(mail.from()));
                message.setRecipients(jakarta.mail.Message.RecipientType.TO,
                        jakarta.mail.internet.InternetAddress.parse(String.join(",", mail.to())));
                message.setSubject(mail.subject(), "UTF-8");
                message.setContent(alternativeWithInlines(mail));
            } else {
                MimeMessageHelper helper = new MimeMessageHelper(message, mail.html() != null, "UTF-8");
                helper.setFrom(mail.from());
                helper.setTo(mail.to().toArray(String[]::new));
                helper.setSubject(mail.subject());
                if (mail.html() != null) {
                    helper.setText(mail.text(), mail.html());
                } else {
                    helper.setText(mail.text(), false);
                }
            }
            for (Map.Entry<String, String> header : mail.headers().entrySet()) {
                message.setHeader(header.getKey(), header.getValue());
            }
            sender.send(message);
            return new ChannelResponse(SMTP_OK, Map.of(), "");
        } catch (jakarta.mail.MessagingException | org.springframework.mail.MailException e) {
            throw new IOException("SMTP: " + e.getMessage(), e);
        }
    }
}
