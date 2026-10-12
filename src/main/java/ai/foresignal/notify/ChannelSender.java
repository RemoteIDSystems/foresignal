package ai.foresignal.notify;

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
import java.util.concurrent.Executors;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 * Sends a {@link PreparedRequest} (prompt 018).
 *
 * <p>HTTP goes through one dedicated {@link HttpClient} on virtual threads with the configured
 * connect timeout (5 s), a per-request timeout (15 s) and <strong>no redirects</strong>: a 3xx is
 * returned as it is and counts as a failure, so a receiver cannot bounce a delivery (and its secrets
 * in headers) to another host. Email builds a {@link JavaMailSenderImpl} per request from the
 * channel's host, port, TLS mode and credentials, and reports an accepted message as status 250.
 *
 * <p>Destinations may be private-network hosts (a LAN Gotify or ntfy), because channels are
 * operator-configured; prompt 035 retrofits the outbound URL policy.
 */
@Component
public class ChannelSender {

    /** Headers the JDK client sets itself and refuses from callers. */
    static final Set<String> RESTRICTED = Set.of("connection", "content-length", "expect", "host", "upgrade");

    /** The SMTP status reported for an accepted email. */
    public static final int SMTP_OK = 250;

    private final HttpClient http;
    private final NotifyProperties properties;

    /** Creates the sender. */
    public ChannelSender(NotifyProperties properties) {
        this.properties = properties;
        this.http = HttpClient.newBuilder()
                .connectTimeout(properties.connectTimeout())
                .followRedirects(HttpClient.Redirect.NEVER)
                .executor(Executors.newVirtualThreadPerTaskExecutor())
                .build();
    }

    /**
     * Sends {@code request} and returns the response.
     *
     * @throws IOException when the destination cannot be reached or the exchange fails
     */
    public ChannelResponse send(PreparedRequest request) throws IOException {
        if (request.body() instanceof PreparedRequest.Mail mail) {
            return sendMail(mail);
        }
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(request.url()))
                .timeout(properties.requestTimeout());
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
        }
        if (contentType != null) {
            builder.header("Content-Type", contentType);
        }
        builder.method(request.method(), publisher);
        try {
            HttpResponse<String> response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
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
            MimeMessageHelper helper = new MimeMessageHelper(message, mail.html() != null, "UTF-8");
            helper.setFrom(mail.from());
            helper.setTo(mail.to().toArray(String[]::new));
            helper.setSubject(mail.subject());
            if (mail.html() != null) {
                helper.setText(mail.text(), mail.html());
            } else {
                helper.setText(mail.text(), false);
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
