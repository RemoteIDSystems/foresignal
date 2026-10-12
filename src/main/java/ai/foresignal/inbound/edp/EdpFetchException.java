package ai.foresignal.inbound.edp;

import ai.foresignal.source.spi.AdapterException;

/** Fetching a provider URL failed: refused, unreachable, too large, or an HTTP error status. */
public class EdpFetchException extends AdapterException {

    private static final long serialVersionUID = 1L;

    private final Integer status;

    /** A failure without an HTTP status. */
    public EdpFetchException(String message, Throwable cause) {
        super(message, cause);
        this.status = null;
    }

    /** An HTTP error status ({@code >= 400}) or a refused redirect, with a body snippet in the message. */
    public EdpFetchException(String message, Integer status) {
        super(message);
        this.status = status;
    }

    /** The HTTP status for an error response, or null. */
    public Integer status() {
        return status;
    }
}
