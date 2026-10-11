package ai.foresignal.store;

/** What started a fetch recorded in {@code fetch_log} (Core-Data-Model §8). */
public enum FetchTrigger {
    /** The collector scheduler's regular poll. */
    SCHEDULED,
    /** An operator's manual run. */
    MANUAL,
    /** A flush of a streaming connection. */
    STREAM,
    /** A push into an ingest endpoint. */
    PUSH
}
