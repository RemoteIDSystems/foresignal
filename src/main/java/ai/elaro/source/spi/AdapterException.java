package ai.elaro.source.spi;

/**
 * An adapter could not complete a fetch.
 *
 * <p>Checked, so an adapter cannot accidentally let an upstream failure escape as an unchecked
 * exception and be mistaken for a bug in Elaro. The collector turns these into health state and
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
}
