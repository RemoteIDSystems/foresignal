package ai.foresignal.store.history;

import ai.foresignal.source.spi.HistoryTier;

/**
 * One attached seq-range leaf of {@code record_version}.
 *
 * @param name the leaf table, for example {@code rv_t_20000000}
 * @param tier the tier parent it is attached under
 * @param fromSeq the inclusive lower bound
 * @param toSeq the exclusive upper bound
 * @param detachPending true when a {@code DETACH PARTITION ... CONCURRENTLY} was interrupted and
 *     the leaf waits for {@code FINALIZE}; such a leaf no longer counts as coverage
 */
public record LeafInfo(String name, HistoryTier tier, long fromSeq, long toSeq, boolean detachPending) {

    /** Whether {@code seq} falls inside this leaf's range. */
    public boolean contains(long seq) {
        return seq >= fromSeq && seq < toSeq;
    }
}
