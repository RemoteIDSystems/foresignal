package ai.foresignal.collect;

import ai.foresignal.source.spi.AdapterException;
import java.time.Instant;

/**
 * The source is in backoff after an upstream asked us to slow down, so the request was not sent.
 *
 * <p>Checked before the budget is consumed: a request we are not going to send must not cost a
 * token, or an upstream throttling us would also burn through our daily allowance.
 */
public class InBackoffException extends AdapterException {

    private static final long serialVersionUID = 1L;

    private final String sourceId;
    private final Instant until;

    public InBackoffException(String sourceId, Instant until) {
        super("Source " + sourceId + " is in backoff until " + until);
        this.sourceId = sourceId;
        this.until = until;
    }

    public String getSourceId() {
        return sourceId;
    }

    /** When the source may be fetched again. */
    public Instant getUntil() {
        return until;
    }
}
