package ai.elaro.source.spi;

/** How a source gets its data. */
public enum Mode {
    /** Elaro fetches on a schedule. */
    POLLING,
    /** Elaro holds a connection open and receives pushes. */
    STREAMING,
    /**
     * Something outside Elaro pushes in through {@code POST /v1/ingest/{sourceId}}.
     *
     * <p>Never polled, so the scheduler ignores it; budgets apply as an inbound rate limit
     * (Architecture §16). Arrives with custom sources in prompt 020.
     */
    PUSH,
    /**
     * Computed from data Elaro already holds, or from events.
     *
     * <p>Makes no upstream requests at all — {@code FetchContext.http()} throws — and must have no
     * {@code recordTtl}, because liveness comes from explicit tombstones and startup reconciliation
     * rather than expiry. Otherwise it behaves like any source: enabled, parameterised,
     * health-tracked and retained. The runtime arrives in a later prompt; the registry accepts the
     * mode now.
     */
    DERIVED;

    /** The wire value used in the API. */
    public String wireValue() {
        return name().toLowerCase();
    }

    /** True when the scheduler should poll this source. */
    public boolean isPolled() {
        return this == POLLING;
    }

    /** True when the source may make upstream requests. */
    public boolean allowsUpstreamRequests() {
        return this == POLLING || this == STREAMING;
    }
}
