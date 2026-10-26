package ai.foresignal.notify;

/**
 * One transactional email (prompt R2-007): a single recipient, a subject, and a plain-text and an HTML body sent as
 * {@code multipart/alternative}.
 *
 * @param to the recipient address
 * @param subject the subject
 * @param text the plain-text body
 * @param html the HTML body
 */
public record TransactionalMail(String to, String subject, String text, String html) {}
