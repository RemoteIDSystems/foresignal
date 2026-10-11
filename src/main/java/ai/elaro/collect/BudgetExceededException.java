package ai.elaro.collect;

import ai.elaro.source.spi.AdapterException;

/**
 * The request would exceed the source's budget, so it was not sent.
 *
 * <p>Refused, not queued (FR-8). Queueing would turn a budget into a delay and the guarantee in G2
 * — "no source ever exceeds its configured budget" — would stop being true of the upstream's view
 * of us, which is the only view that matters.
 */
public class BudgetExceededException extends AdapterException {

    private static final long serialVersionUID = 1L;

    private final String sourceId;

    public BudgetExceededException(String sourceId, String detail) {
        super(detail);
        this.sourceId = sourceId;
    }

    public String getSourceId() {
        return sourceId;
    }
}
