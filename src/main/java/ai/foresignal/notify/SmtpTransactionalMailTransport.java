package ai.foresignal.notify;

import ai.foresignal.settings.SmtpSettings;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.time.Duration;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 * Sends transactional emails over SMTP with the sender {@link MailSenders} builds for the email channel too. The
 * contract's {@code tls} security is implicit TLS ({@code smtps}), which the shared builder calls {@code ssl}.
 */
@Component
public class SmtpTransactionalMailTransport implements TransactionalMailTransport {

    /** Connect timeout. */
    static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);

    /** Read and write timeout. */
    static final Duration READ_TIMEOUT = Duration.ofSeconds(30);

    @Override
    public void send(SmtpSettings settings, String password, TransactionalMail mail) throws Exception {
        String security = settings.effectiveSecurity();
        JavaMailSenderImpl sender = MailSenders.build(settings.host(), settings.effectivePort(),
                "tls".equals(security) ? "ssl" : security, settings.username(), password, CONNECT_TIMEOUT,
                READ_TIMEOUT);
        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(new InternetAddress(settings.effectiveFromAddress(), settings.effectiveFromName(), "UTF-8"));
        helper.setTo(mail.to());
        helper.setSubject(mail.subject());
        helper.setText(mail.text(), mail.html());
        sender.send(message);
    }
}
