package ai.foresignal.ingest;

import java.util.Map;

/**
 * What the ingest writer did with one batch (Core-Data-Model §6.2, "IngestOutcome").
 *
 * <p>Defined here by prompt 006 so health can consume it; prompt 008 owns the final shape and may
 * replace {@code byReason}'s string keys with its persist-reason enum.
 *
 * @param seen observations received
 * @param persisted versions written; health reports this as {@code lastRecordsChanged}
 * @param live live-row updates without a new version
 * @param touched touch rows written
 * @param touchesThrottled touches suppressed by throttling
 * @param late observations dropped for arriving out of order
 * @param skipped observations the policy chose not to persist
 * @param rejected observations refused for violating a limit
 * @param clamped observations whose time the time contract clamped
 * @param fromSeq the first sequence number allocated, or 0 when none was
 * @param toSeq the last sequence number allocated, or 0 when none was
 * @param byReason persisted versions per persist reason
 */
public record IngestOutcome(
        int seen,
        int persisted,
        int live,
        int touched,
        int touchesThrottled,
        int late,
        int skipped,
        int rejected,
        int clamped,
        long fromSeq,
        long toSeq,
        Map<String, Integer> byReason) {

    public IngestOutcome {
        byReason = byReason == null ? Map.of() : Map.copyOf(byReason);
    }

    /** An outcome with only the seen and persisted counts, for callers that know nothing more. */
    public static IngestOutcome of(int seen, int persisted) {
        return new IngestOutcome(seen, persisted, 0, 0, 0, 0, 0, 0, 0, 0, 0, Map.of());
    }

    /** The outcome of a batch with no records. */
    public static IngestOutcome empty() {
        return of(0, 0);
    }
}
