package ai.foresignal.collect;

/**
 * Money spent by a paid source this calendar month (prompt 135: OpenAPI {@code SourceHealth.spentUsdThisMonth}),
 * contributed as a Spring bean by the component that keeps the money ledger.
 */
@FunctionalInterface
public interface SourceSpendLookup {

    /**
     * The US dollars a source spent this calendar month (UTC).
     *
     * @param sourceId the source
     * @return the spend, or null when the source is not a paid one
     */
    Double spentUsdThisMonth(String sourceId);
}
