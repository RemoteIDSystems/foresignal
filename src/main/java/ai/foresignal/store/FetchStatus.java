package ai.foresignal.store;

/** How a fetch recorded in {@code fetch_log} ended (Core-Data-Model §8). */
public enum FetchStatus {
    /** The fetch returned data and the writer processed it. */
    OK,
    /** The upstream answered "not modified". */
    NOT_MODIFIED,
    /** The fetch or the ingest failed. */
    FAILED,
    /** The budget governor or backoff refused the request. */
    REFUSED,
    /** The response arrived but was thrown away, for example after the source was disabled. */
    DISCARDED
}
