package ai.foresignal.source.spi;

import java.util.List;

/**
 * What one fetch produced.
 *
 * @param records the observations, possibly empty
 * @param complete true when this is the full current set. Records absent from a complete result are
 *     <strong>not</strong> auto-tombstoned: adapters emit explicit tombstones where they can, because
 *     a truncated response would otherwise delete the world
 * @param notModified true when the upstream answered 304, so there is nothing to ingest and the run
 *     still counts as a success
 * @param coverage how much of the expected reporting arrived, where the upstream says
 */
public record FetchResult(
        List<NormalizedRecord> records, boolean complete, boolean notModified, Coverage coverage) {

    public FetchResult {
        records = records == null ? List.of() : List.copyOf(records);
        coverage = coverage == null ? Coverage.none() : coverage;
    }

    /** A partial set of records, for example one page or one bbox. */
    public static FetchResult of(List<NormalizedRecord> records) {
        return new FetchResult(records, false, false, Coverage.none());
    }

    /** The full current set. */
    public static FetchResult complete(List<NormalizedRecord> records) {
        return new FetchResult(records, true, false, Coverage.none());
    }

    /**
     * The upstream answered 304 Not Modified.
     *
     * <p>Counts as a success with zero changes and does not trigger zero-result degradation, which
     * is what makes conditional GET safe to use on feeds that require it.
     *
     * <p>Named {@code unchanged} rather than {@code notModified} because the record component of
     * that name already generates a no-argument {@code notModified()} accessor, and Java does not
     * allow a static method to share its signature.
     */
    public static FetchResult unchanged() {
        return new FetchResult(List.of(), false, true, Coverage.none());
    }

    /** This result with coverage attached. */
    public FetchResult withCoverage(Coverage coverage) {
        return new FetchResult(records, complete, notModified, coverage);
    }

    public int size() {
        return records.size();
    }

    public boolean isEmpty() {
        return records.isEmpty();
    }
}
