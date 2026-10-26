package ai.foresignal.notify;

import java.time.Duration;
import java.util.Properties;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * Builds the {@link JavaMailSenderImpl} of one SMTP send: host, port, TLS mode, timeouts and optional authentication.
 * Shared by the email channel ({@link ChannelSender}) and the transactional account emails (prompt R2-007), so both
 * speak SMTP the same way.
 */
public final class MailSenders {

    private MailSenders() {}

    /**
     * A sender for one SMTP server.
     *
     * @param host the SMTP host
     * @param port the SMTP port
     * @param tls {@code ssl} (implicit TLS, {@code smtps}), {@code starttls} (required) or {@code none}
     * @param username the user name, or null for no authentication
     * @param password the password; ignored without a user name
     * @param connectTimeout the connect timeout
     * @param readTimeout the read and write timeout
     * @return the configured sender
     */
    public static JavaMailSenderImpl build(String host, int port, String tls, String username, String password,
            Duration connectTimeout, Duration readTimeout) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(port);
        boolean ssl = "ssl".equals(tls);
        sender.setProtocol(ssl ? "smtps" : "smtp");
        sender.setDefaultEncoding("UTF-8");
        String prefix = ssl ? "mail.smtps." : "mail.smtp.";
        Properties props = new Properties();
        props.put(prefix + "connectiontimeout", Long.toString(connectTimeout.toMillis()));
        props.put(prefix + "timeout", Long.toString(readTimeout.toMillis()));
        props.put(prefix + "writetimeout", Long.toString(readTimeout.toMillis()));
        if ("starttls".equals(tls)) {
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
        }
        if (username != null) {
            sender.setUsername(username);
            sender.setPassword(password);
            props.put(prefix + "auth", "true");
        }
        sender.setJavaMailProperties(props);
        return sender;
    }
}
