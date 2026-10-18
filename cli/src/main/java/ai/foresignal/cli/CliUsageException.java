package ai.foresignal.cli;

/**
 * A usage or input problem found while running a command (missing configuration, unreadable or invalid file);
 * the command exits with {@link ExitCodes#USAGE}.
 */
public class CliUsageException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception.
     *
     * @param message the message printed on standard error
     */
    public CliUsageException(String message) {
        super(message);
    }

    /**
     * Creates the exception with a cause.
     *
     * @param message the message printed on standard error
     * @param cause   the underlying failure
     */
    public CliUsageException(String message, Throwable cause) {
        super(message, cause);
    }
}
