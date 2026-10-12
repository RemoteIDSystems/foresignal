package ai.foresignal.stream.sse;

/** How a topic's replay ended. */
public sealed interface ReplayOutcome {

    /**
     * Every matching event was sent.
     *
     * @param scannedThrough every cursor at or below this was scanned; buffered live events at or
     *     below it are duplicates
     */
    record Done(long scannedThrough) implements ReplayOutcome {}

    /** The replay cap was reached; the client sends {@code reset {reason: replay_cap}} and closes. */
    record CapReached() implements ReplayOutcome {}

    /**
     * The resume point is below the retention floor; the client sends
     * {@code reset {reason: resync_required}} and closes, and re-snapshots.
     *
     * @param watermarkSeq the current change watermark
     */
    record ResyncRequired(long watermarkSeq) implements ReplayOutcome {}
}
