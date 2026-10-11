package ai.foresignal.ingest;

/**
 * A component that caches ingest state loaded from the database and can reload it.
 *
 * <p>The ingest writer (prompt 009) initializes its allocator position, last ingest time and
 * per-entity state from the database (Core-Data-Model §6.1). When tests truncate the history tables
 * and restart {@code record_seq} between methods, that cached state is stale; the test database
 * cleaner calls {@link #resetState()} on every bean implementing this interface right after it
 * cleans.
 */
public interface IngestStateReset {

    /** Discards cached state and reloads it from the database. */
    void resetState();
}
