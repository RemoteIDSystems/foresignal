package ai.foresignal.source.spi;

import java.util.List;
import java.util.Set;

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
 * @param release the dataset's own version identity for a complete-set reference run (prompt 094):
 *     the upstream's ETag, Last-Modified or own version string. Null for a source that does not
 *     publish one
 * @param reconcileKinds for a complete-set reference run (prompt 094), the kinds whose complete set this
 *     run read, when that is fewer than all of them: a multi-file source whose one file answered 304 has
 *     re-read only the other file's kinds, and only those may be reconciled. Null means every declared kind
 */
public record FetchResult(
        List<NormalizedRecord> records,
        boolean complete,
        boolean notModified,
        Coverage coverage,
        String release,
        Set<String> reconcileKinds) {

    public FetchResult {
        records = records == null ? List.of() : List.copyOf(records);
        coverage = coverage == null ? Coverage.none() : coverage;
        release = release == null || release.isBlank() ? null : release;
        reconcileKinds = reconcileKinds == null ? null : Set.copyOf(reconcileKinds);
    }

    /** A result whose complete set, if any, covers every declared kind. */
    public FetchResult(
            List<NormalizedRecord> records, boolean complete, boolean notModified, Coverage coverage, String release) {
        this(records, complete, notModified, coverage, release, null);
    }

    /** A partial set of records, for example one page or one bbox. */
    public static FetchResult of(List<NormalizedRecord> records) {
        return new FetchResult(records, false, false, Coverage.none(), null);
    }

    /** The full current set. */
    public static FetchResult complete(List<NormalizedRecord> records) {
        return new FetchResult(records, true, false, Coverage.none(), null);
    }

    /**
     * The full current set with its upstream release identity (prompt 094); the executor emits one
     * {@code dataset-release} record after reconciliation.
     */
    public static FetchResult complete(List<NormalizedRecord> records, String release) {
        return new FetchResult(records, true, false, Coverage.none(), release);
    }

    /**
     * The full current set of {@code kinds} only (prompt 094, multi-file reference sources): the files of the
     * other kinds answered 304 and were not re-read, so the executor reconciles {@code kinds} alone and the
     * unchanged files keep their records. An empty {@code kinds} is a plain partial result.
     */
    public static FetchResult completeFor(Set<String> kinds, List<NormalizedRecord> records, String release) {
        if (kinds == null || kinds.isEmpty()) {
            return new FetchResult(records, false, false, Coverage.none(), release, null);
        }
        return new FetchResult(records, true, false, Coverage.none(), release, kinds);
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
        return new FetchResult(List.of(), false, true, Coverage.none(), null);
    }

    /** This result with coverage attached. */
    public FetchResult withCoverage(Coverage coverage) {
        return new FetchResult(records, complete, notModified, coverage, release, reconcileKinds);
    }

    /** This result with a release identity attached (prompt 094). */
    public FetchResult withRelease(String release) {
        return new FetchResult(records, complete, notModified, coverage, release, reconcileKinds);
    }

    public int size() {
        return records.size();
    }

    public boolean isEmpty() {
        return records.isEmpty();
    }
}
