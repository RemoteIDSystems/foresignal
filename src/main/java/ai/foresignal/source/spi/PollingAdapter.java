package ai.foresignal.source.spi;

/** A source Foresignal fetches on a schedule. */
public interface PollingAdapter extends SourceAdapter {

    /**
     * Fetches once.
     *
     * <p>All network access must go through {@code ctx.http()}.
     *
     * @throws AdapterException when the upstream could not be fetched or understood
     */
    FetchResult fetch(FetchContext ctx) throws AdapterException;
}
