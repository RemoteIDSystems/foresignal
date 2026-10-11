package ai.foresignal.ingest;

/** What the policy engine decided to do with one observation (Core-Data-Model §5.2). */
public enum Outcome {
    /** Write a version, upsert head and live. */
    PERSIST,
    /** Older than the live state: dropped with no side effects (invariant I4). */
    LATE,
    /** A tombstone for an entity that is absent or already tombstoned: nothing to do. */
    SKIP,
    /** Same content, re-observed: refresh {@code observed_at}, {@code last_received_at}, validity. */
    TOUCH,
    /** Same content, re-observed too soon after the last touch: nothing written. */
    TOUCH_THROTTLED,
    /** Changed but not worth a version: upsert live only, marking it dirty. */
    LIVE
}
