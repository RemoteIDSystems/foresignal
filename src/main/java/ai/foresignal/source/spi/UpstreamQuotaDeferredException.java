package ai.foresignal.source.spi;

import java.time.Instant;
import java.util.Objects;

/**
 * An adapter decided not to send a request because the upstream's own quota would not cover it
 * (prompt 062: the OpenSky credit governor, when the stored {@code X-Rate-Limit-Remaining} is below the
 * call's cost plus the reserve).
 *
 * <p>Thrown before anything is sent. {@code FetchExecutor} treats it like a budget refusal: a
 * {@code REFUSED} {@code fetch_log} row, a refusal counted in usage and {@code HealthTracker.recordRefused}
 * rather than {@code recordFailure}, so the source shows {@code degraded} through the refusal rule instead
 * of {@code failing}.
 */
public class UpstreamQuotaDeferredException extends AdapterException {

    private static final long serialVersionUID = 1L;

    private final Instant retryAt;

    /**
     * Creates the exception.
     *
     * @param message why the request was deferred, for the fetch log
     * @param retryAt when the quota is expected to cover the request again
     */
    public UpstreamQuotaDeferredException(String message, Instant retryAt) {
        super(message);
        this.retryAt = Objects.requireNonNull(retryAt, "retryAt");
    }

    /** When the quota is expected to cover the request again. */
    public Instant retryAt() {
        return retryAt;
    }
}
