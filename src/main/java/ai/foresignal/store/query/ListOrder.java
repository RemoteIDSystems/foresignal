package ai.foresignal.store.query;

/** The order of a current-state listing (Core-Data-Model §12). */
public enum ListOrder {
    /** Newest {@code observed_at} first; keyset {@code (observed_at, source_id, external_id)} descending. */
    OBSERVED,
    /** Snapshot order; keyset {@code (source_id, external_id)} ascending; pins the watermark. */
    ID,
    /** Nearest first ({@code sort=distance}); keyset {@code (distance, source_id, external_id)}. */
    DISTANCE
}
