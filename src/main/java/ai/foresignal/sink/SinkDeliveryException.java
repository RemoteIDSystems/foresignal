package ai.foresignal.sink;

import java.time.Duration;

/**
 * A CHANGE_FEED delivery the target did not acknowledge (prompt 110). The supervisor keeps the cursor where it
 * is, marks the sink {@code FAILING} and retries after {@link #retryAfter()} when the target named one (a
 * {@code Retry-After} on 429 or 5xx), else after its restart backoff. A {@link #permanent()} failure (a 4xx other
 * than 408/429) is retried only at the backoff ceiling's pace: the target refuses the request as sent.
 */
public class SinkDeliveryException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final transient Duration retryAfter;
    private final boolean permanent;
    private final Integer status;

    /**
     * Creates the exception.
     *
     * @param message what went wrong
     * @param status the target's HTTP status, or null for a transport failure
     * @param retryAfter the delay the target asked for, or null
     * @param permanent true for a refusal that will not succeed unchanged
     */
    public SinkDeliveryException(String message, Integer status, Duration retryAfter, boolean permanent) {
        super(message);
        this.status = status;
        this.retryAfter = retryAfter;
        this.permanent = permanent;
    }

    /** The delay the target asked for, or null. */
    public Duration retryAfter() {
        return retryAfter;
    }

    /** True when the target refused the request itself (a 4xx other than 408/429). */
    public boolean permanent() {
        return permanent;
    }

    /** The target's HTTP status, or null for a transport failure. */
    public Integer status() {
        return status;
    }
}
