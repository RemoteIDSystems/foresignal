package ai.foresignal.security.user;

import ai.foresignal.notify.TransactionalMail;
import ai.foresignal.notify.TransactionalMailTransport;
import ai.foresignal.settings.ClientLinks;
import ai.foresignal.settings.ServerSettingsService;
import ai.foresignal.settings.SmtpSettings;
import ai.foresignal.settings.SmtpSettingsService;
import ai.foresignal.system.SystemWarningContributor;
import ai.foresignal.system.SystemWarnings;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.util.HtmlUtils;

/**
 * The transactional account emails (prompt R2-007): welcome (full name, role names, activation link and its expiry),
 * password reset (link, expiry, "ignore if you did not ask"), role changed and suspended. US English, plain text plus
 * HTML, no tracking and no remote assets. Sent through the global SMTP settings ({@link SmtpSettingsService}) by
 * {@link TransactionalMailTransport}.
 *
 * <p>Links come from {@link ClientLinks#activate()} and {@link ClientLinks#passwordReset()} with {@code {token}} and
 * {@code {publicBaseUrl}} (URL-encoded) filled in; the raw token appears nowhere else in the email.
 *
 * <p><strong>No SMTP never fails the caller.</strong> When SMTP is off or unconfigured, or the send fails, the email is
 * logged as not sent (user id and token prefix only, never the token), {@code false} comes back so user management
 * returns the one-time link to the admin (R2-005), and {@code /v1/system} carries {@value #NOT_CONFIGURED} while
 * accounts exist without a usable SMTP server. The two notices without a link (role changed, suspended) are sent after
 * the surrounding transaction commits, so a slow SMTP server never holds the user-management lock.
 */
@Service
public class TransactionalEmailService implements AccountMailer, SystemWarningContributor {

    /** The {@code /v1/system} warning code. */
    public static final String NOT_CONFIGURED = "SMTP_NOT_CONFIGURED";

    private static final Logger LOG = LoggerFactory.getLogger(TransactionalEmailService.class);

    private static final DateTimeFormatter EXPIRY =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm 'UTC'").withZone(ZoneOffset.UTC);

    private final SmtpSettingsService smtp;
    private final ServerSettingsService server;
    private final TransactionalMailTransport transport;
    private final UserRepository users;
    private final Clock clock;
    private volatile Instant lastUndelivered;

    /**
     * Creates the service.
     *
     * @param smtp the SMTP settings
     * @param server the server settings, for the base URL and link templates
     * @param transport the mail transport
     * @param users the user store, for the warning
     * @param clock the clock
     */
    public TransactionalEmailService(SmtpSettingsService smtp, ServerSettingsService server,
            TransactionalMailTransport transport, UserRepository users, Clock clock) {
        this.smtp = smtp;
        this.server = server;
        this.transport = transport;
        this.users = users;
        this.clock = clock;
    }

    /** One rendered email before addressing. */
    record Content(String subject, String text, String html) {}

    @Override
    public boolean sendWelcome(UserEntity user, List<String> roleNames, IssuedUserToken token) {
        String link = link(links().activate(), token.plaintext());
        return deliver(user, welcome(user.getFullName(), roleNames, link, token.token().getExpiresAt(), fromName()),
                "welcome", token.token().getTokenPrefix());
    }

    @Override
    public boolean sendPasswordReset(UserEntity user, IssuedUserToken token) {
        String link = link(links().passwordReset(), token.plaintext());
        return deliver(user, reset(user.getFullName(), link, token.token().getExpiresAt(), fromName()),
                "password reset", token.token().getTokenPrefix());
    }

    @Override
    public void sendRoleChanged(UserEntity user, List<String> roleNames) {
        afterCommit(() -> deliver(user, roleChanged(user.getFullName(), roleNames, fromName()), "role-changed", null));
    }

    @Override
    public void sendSuspended(UserEntity user) {
        afterCommit(() -> deliver(user, suspended(user.getFullName(), fromName()), "suspended", null));
    }

    /**
     * Sends a test message with the stored settings ({@code testSmtpSettings}).
     *
     * @param to the recipient
     * @return the {@code SmtpTestResult} view: {@code ok}, {@code message} (the failure, never the password) and
     *     {@code sentAt}
     */
    public Map<String, Object> test(String to) {
        SmtpSettings settings = smtp.get();
        Map<String, Object> out = new LinkedHashMap<>();
        if (!settings.usable()) {
            out.put("ok", false);
            out.put("message", "SMTP is not enabled, or has no host.");
            out.put("sentAt", null);
            return out;
        }
        String from = settings.effectiveFromName();
        TransactionalMail mail = new TransactionalMail(to, "Foresignal SMTP test",
                "This is a test message from " + from + ". Account emails can be sent.\n",
                "<p>This is a test message from " + HtmlUtils.htmlEscape(from) + ". Account emails can be sent.</p>");
        try {
            transport.send(settings, smtp.password(settings), mail);
            out.put("ok", true);
            out.put("message", "Sent to " + to + " through " + settings.host() + ":" + settings.effectivePort() + ".");
            out.put("sentAt", clock.instant());
        } catch (Exception e) {
            out.put("ok", false);
            out.put("message", reason(e));
            out.put("sentAt", null);
        }
        return out;
    }

    @Override
    public List<SystemWarnings.Warning> warnings() {
        if (smtp.get().usable() || (users.count() == 0 && lastUndelivered == null)) {
            return List.of();
        }
        return List.of(new SystemWarnings.Warning(NOT_CONFIGURED, "warning", "User accounts exist but SMTP is not"
                + " configured, so welcome and password reset emails are not sent; admins get each link in the"
                + " response instead. Configure it under PUT /v1/settings/smtp."));
    }

    /** The welcome email. */
    static Content welcome(String fullName, List<String> roleNames, String link, Instant expiresAt, String from) {
        String roles = roleNames.isEmpty() ? "none yet" : String.join(", ", roleNames);
        String expiry = EXPIRY.format(expiresAt);
        String text = "Hello " + fullName + ",\n\n"
                + "An account has been created for you on " + from + ".\n"
                + "Your roles: " + roles + ".\n\n"
                + "Activate your account and choose a password:\n" + link + "\n\n"
                + "This link works once and expires on " + expiry + ". If you did not expect this email, you can"
                + " ignore it.\n\n"
                + "— " + from + "\n";
        String html = "<p>Hello " + esc(fullName) + ",</p>"
                + "<p>An account has been created for you on " + esc(from) + ".<br>Your roles: " + esc(roles) + ".</p>"
                + "<p><a href=\"" + esc(link) + "\">Activate your account and choose a password</a></p>"
                + "<p>This link works once and expires on " + esc(expiry) + ". If you did not expect this email, you"
                + " can ignore it.</p><p>— " + esc(from) + "</p>";
        return new Content("Your " + from + " account", text, html);
    }

    /** The password reset email. */
    static Content reset(String fullName, String link, Instant expiresAt, String from) {
        String expiry = EXPIRY.format(expiresAt);
        String text = "Hello " + fullName + ",\n\n"
                + "A password reset was requested for your " + from + " account.\n\n"
                + "Choose a new password:\n" + link + "\n\n"
                + "This link works once and expires on " + expiry + ". If you did not request this, ignore this"
                + " email; your password stays the same.\n\n"
                + "— " + from + "\n";
        String html = "<p>Hello " + esc(fullName) + ",</p>"
                + "<p>A password reset was requested for your " + esc(from) + " account.</p>"
                + "<p><a href=\"" + esc(link) + "\">Choose a new password</a></p>"
                + "<p>This link works once and expires on " + esc(expiry) + ". If you did not request this, ignore"
                + " this email; your password stays the same.</p><p>— " + esc(from) + "</p>";
        return new Content("Reset your " + from + " password", text, html);
    }

    /** The role-changed notice. */
    static Content roleChanged(String fullName, List<String> roleNames, String from) {
        String roles = roleNames.isEmpty() ? "You no longer hold any role." : "Your roles are now: "
                + String.join(", ", roleNames) + ".";
        String text = "Hello " + fullName + ",\n\n"
                + "An administrator changed your roles on " + from + ". " + roles + "\n\n"
                + "The change applies the next time you use the service.\n\n"
                + "— " + from + "\n";
        String html = "<p>Hello " + esc(fullName) + ",</p>"
                + "<p>An administrator changed your roles on " + esc(from) + ". " + esc(roles) + "</p>"
                + "<p>The change applies the next time you use the service.</p><p>— " + esc(from) + "</p>";
        return new Content("Your " + from + " roles changed", text, html);
    }

    /** The suspended notice. */
    static Content suspended(String fullName, String from) {
        String text = "Hello " + fullName + ",\n\n"
                + "An administrator suspended your " + from + " account. You cannot sign in until it is restored;"
                + " your settings are kept.\n\n"
                + "— " + from + "\n";
        String html = "<p>Hello " + esc(fullName) + ",</p>"
                + "<p>An administrator suspended your " + esc(from) + " account. You cannot sign in until it is"
                + " restored; your settings are kept.</p><p>— " + esc(from) + "</p>";
        return new Content("Your " + from + " account was suspended", text, html);
    }

    /**
     * The link of a template with {@code {token}} and {@code {publicBaseUrl}} filled in, both URL-encoded.
     *
     * @param template the {@link ClientLinks} template
     * @param token the one-time token
     * @return the link
     */
    String link(String template, String token) {
        String base = server.get().normalizedPublicBaseUrl();
        return ClientLinks.fill(template, Map.of(
                "token", URLEncoder.encode(token, StandardCharsets.UTF_8),
                "publicBaseUrl", base == null ? "" : URLEncoder.encode(base, StandardCharsets.UTF_8)));
    }

    private ClientLinks links() {
        return server.get().effectiveClientLinks();
    }

    private String fromName() {
        return smtp.get().effectiveFromName();
    }

    private boolean deliver(UserEntity user, Content content, String kind, String tokenPrefix) {
        SmtpSettings settings = smtp.get();
        String token = tokenPrefix == null ? "" : " (token " + tokenPrefix + "…)";
        if (!settings.usable()) {
            lastUndelivered = clock.instant();
            LOG.warn("SMTP is not configured: the {} email for user {}{} was not sent", kind, user.getId(), token);
            return false;
        }
        try {
            transport.send(settings, smtp.password(settings), new TransactionalMail(user.getEmail(), content.subject(),
                    content.text(), content.html()));
            return true;
        } catch (Exception e) {
            lastUndelivered = clock.instant();
            LOG.warn("The {} email for user {}{} was not sent: {}", kind, user.getId(), token, reason(e));
            return false;
        }
    }

    private static void afterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }

    private static String reason(Exception e) {
        String message = e.getMessage();
        return e.getClass().getSimpleName() + (message == null || message.isBlank() ? "" : ": " + message);
    }

    private static String esc(String value) {
        return HtmlUtils.htmlEscape(value == null ? "" : value);
    }
}
