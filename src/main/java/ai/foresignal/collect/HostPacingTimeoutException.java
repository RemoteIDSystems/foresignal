package ai.foresignal.collect;

import java.time.Duration;
import java.time.Instant;

/**
 * A request waited the whole bounded wait for its host's {@link HostPacer} gate and was not sent (prompt 080).
 *
 * <p>An {@link InBackoffException}, so every path that treats a backoff as a deliberate skip rather than a
 * failure (health, the streaming supervisor) does the same here, and no budget was consumed. The fetch executor
 * reports it with the skip reason {@value FetchOutcome#SKIP_HOST_PACING} and an error starting with
 * {@value #REASON}.
 */
public class HostPacingTimeoutException extends InBackoffException {

    private static final long serialVersionUID = 1L;

    /** The reason the skip reports. */
    public static final String REASON = "host pacing";

    private final String host;

    /**
     * Creates the exception.
     *
     * @param sourceId the source whose request was not sent
     * @param host the paced host
     * @param waited how long the request waited
     * @param now the time of the skip
     */
    public HostPacingTimeoutException(String sourceId, String host, Duration waited, Instant now) {
        super(sourceId, now, REASON + ": " + sourceId + " waited " + waited.toSeconds() + " s for a request slot on "
                + host + " and sent nothing");
        this.host = host;
    }

    /** The paced host. */
    public String getHost() {
        return host;
    }
}
