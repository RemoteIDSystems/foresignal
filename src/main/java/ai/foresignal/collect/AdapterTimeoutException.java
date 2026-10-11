package ai.foresignal.collect;

import ai.foresignal.source.spi.AdapterException;
import java.time.Duration;

/**
 * A fetch ran past its descriptor's {@code fetchTimeout} and was interrupted.
 *
 * <p>Counted as an upstream failure: from the operator's point of view a hanging upstream and a
 * failing one need the same attention. The interruption is what keeps one hanging adapter from
 * holding its source's overlap guard forever (Product Charter NFR-5).
 */
public class AdapterTimeoutException extends AdapterException {

    private static final long serialVersionUID = 1L;

    private final transient Duration timeout;

    /** A timeout of {@code sourceId}'s fetch after {@code timeout}. */
    public AdapterTimeoutException(String sourceId, Duration timeout) {
        super("The fetch of " + sourceId + " did not finish within " + timeout.toSeconds() + " s and was interrupted");
        this.timeout = timeout;
    }

    /** The timeout that was exceeded. */
    public Duration getTimeout() {
        return timeout;
    }
}
