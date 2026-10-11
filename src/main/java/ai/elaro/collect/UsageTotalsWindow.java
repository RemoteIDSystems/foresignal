package ai.elaro.collect;

/**
 * Request and ingest totals for one window.
 *
 * @param requests requests sent upstream
 * @param bytes response bytes received
 * @param errors requests that failed
 * @param refused requests the budget declined to send
 * @param ingest what ingest did with the results
 */
public record UsageTotalsWindow(long requests, long bytes, long errors, long refused, IngestCounts ingest) {

    public static final UsageTotalsWindow ZERO = new UsageTotalsWindow(0, 0, 0, 0, IngestCounts.ZERO);

    public UsageTotalsWindow {
        ingest = ingest == null ? IngestCounts.ZERO : ingest;
    }

    public UsageTotalsWindow plus(UsageTotalsWindow other) {
        return new UsageTotalsWindow(
                requests + other.requests,
                bytes + other.bytes,
                errors + other.errors,
                refused + other.refused,
                ingest.plus(other.ingest));
    }

    /**
     * The share of attempts the budget refused, 0..1.
     *
     * <p>Health uses this: more than 20% refusals in an hour means the source is configured to want
     * more than it is allowed, which is a misconfiguration rather than an upstream fault.
     */
    public double refusalRate() {
        long attempts = requests + refused;
        return attempts == 0 ? 0.0 : (double) refused / attempts;
    }
}
