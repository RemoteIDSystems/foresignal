package ai.foresignal.chatops;

/**
 * The chat platform refused the connector's credentials (HTTP 401 or 403 on a connector-wide call such as
 * {@code getUpdates} or {@code /sync}). The supervisor stops the connector, raises a system event and sets
 * its health to {@code failing} until the channel is changed.
 */
public class ChatAuthException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** Creates the exception. */
    public ChatAuthException(String message) {
        super(message);
    }
}
