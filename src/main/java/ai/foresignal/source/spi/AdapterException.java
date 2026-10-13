package ai.foresignal.source.spi;

import java.util.OptionalInt;

/**
 * An adapter could not complete a fetch.
 *
 * <p>Checked, so an adapter cannot accidentally let an upstream failure escape as an unchecked
 * exception and be mistaken for a bug in Foresignal. The collector turns these into health state and
 * backoff rather than propagating them.
 */
public class AdapterException extends Exception {

    private static final long serialVersionUID = 1L;

    public AdapterException(String message) {
        super(message);
    }

    public AdapterException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * The HTTP status the upstream answered with, when this failure is an error status (400 or more).
     *
     * <p>Lets an adapter treat one status specially without importing the collector's exception type
     * (prompt 047: {@code spc-outlooks} reads a 404 for one layer as "layer missing", not as a failed
     * run). Empty for every other failure.
     */
    public OptionalInt httpStatus() {
        return OptionalInt.empty();
    }
}
