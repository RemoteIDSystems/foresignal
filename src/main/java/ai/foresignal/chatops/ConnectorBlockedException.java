package ai.foresignal.chatops;

/**
 * The connector cannot run until the operator changes something (prompt 061), for example a Telegram bot
 * with a foreign webhook while {@code takeOverWebhook} is off. The supervisor sets health {@code degraded}
 * with this message and retries at the longest backoff.
 */
public class ConnectorBlockedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** Creates the exception. */
    public ConnectorBlockedException(String message) {
        super(message);
    }
}
