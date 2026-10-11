package ai.foresignal.source.spi;

/**
 * The upstream refused the source's credentials.
 *
 * <p>A separate type because the right reaction differs from any other failure: retrying a
 * rejected key on the reconnect ladder would hammer the upstream with a request that cannot
 * succeed, so the streaming supervisor retries it hourly instead, and the operator sees the cause
 * in health.
 */
public class CredentialsRejectedException extends AdapterException {

    private static final long serialVersionUID = 1L;

    /** A rejection described by {@code message}. */
    public CredentialsRejectedException(String message) {
        super(message);
    }

    /** A rejection described by {@code message}, caused by {@code cause}. */
    public CredentialsRejectedException(String message, Throwable cause) {
        super(message, cause);
    }
}
