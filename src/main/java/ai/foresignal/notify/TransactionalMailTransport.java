package ai.foresignal.notify;

import ai.foresignal.settings.SmtpSettings;

/**
 * Sends a {@link TransactionalMail} through the global SMTP settings (prompt R2-007). The production implementation is
 * {@link SmtpTransactionalMailTransport}; tests replace it with an intercepting one, so nothing is ever really sent.
 */
public interface TransactionalMailTransport {

    /**
     * Sends one email.
     *
     * @param settings the SMTP settings, usable
     * @param password the decrypted SMTP password, or null
     * @param mail the email
     * @throws Exception when the SMTP server refuses or cannot be reached; the message never contains the password
     */
    void send(SmtpSettings settings, String password, TransactionalMail mail) throws Exception;
}
