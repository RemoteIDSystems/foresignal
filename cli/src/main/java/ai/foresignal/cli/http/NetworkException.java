package ai.foresignal.cli.http;

/**
 * The server could not be reached or the connection failed; the command exits with code 3.
 */
public final class NetworkException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception.
     *
     * @param message what failed
     * @param cause   the I/O failure
     */
    public NetworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
