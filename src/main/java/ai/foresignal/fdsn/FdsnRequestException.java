package ai.foresignal.fdsn;

/**
 * A request the FDSN event service refuses with {@code 400} (prompt 141). The message becomes the details line of the
 * FDSN plain-text error body, not a {@code ProblemDetail}: fdsnws clients expect text.
 */
public class FdsnRequestException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** Creates the exception with the details shown to the client. */
    public FdsnRequestException(String message) {
        super(message);
    }
}
