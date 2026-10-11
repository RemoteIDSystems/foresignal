package ai.foresignal.collect;

import ai.foresignal.ingest.IngestOutcome;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.store.FetchStatus;
import java.time.Instant;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * What one fetch did (OpenAPI {@code FetchOutcome}).
 *
 * <p>The ingest counts come from the writer's {@link IngestOutcome} when the fetch returned records,
 * and are zero otherwise. Two components are Java-side only and not part of the API schema:
 * {@link #status()}, the {@code fetch_log} status the fetch ended with, and {@link #skipReason()},
 * set when the run never happened.
 *
 * @param sourceId the source
 * @param startedAt when the fetch started
 * @param durationMs wall time, in milliseconds
 * @param success true when the upstream answered and anything returned was ingested
 * @param recordsSeen records the adapter returned
 * @param recordsChanged alias of {@code persisted}, kept for v1.1 clients
 * @param persisted versions written
 * @param live live-only updates
 * @param touched touches written
 * @param touchesThrottled touches suppressed by the throttle
 * @param late observations dropped as late
 * @param skipped tombstones for absent entities
 * @param rejected records the normalizer refused
 * @param byReason versions per persist reason; every reason present
 * @param fromSeq first seq written, or null
 * @param toSeq last seq written, or null
 * @param error the redacted error, or null
 * @param fetchId the {@code fetch_log} row, or null when none was written (or could not be found)
 * @param status the {@code fetch_log} status, or null when no row was written
 * @param skipReason {@code overlap} or {@code backoff} when the run was skipped, else null
 */
public record FetchOutcome(
        String sourceId,
        Instant startedAt,
        long durationMs,
        boolean success,
        int recordsSeen,
        int recordsChanged,
        int persisted,
        int live,
        int touched,
        int touchesThrottled,
        int late,
        int skipped,
        int rejected,
        Map<PersistReason, Integer> byReason,
        Long fromSeq,
        Long toSeq,
        String error,
        Long fetchId,
        FetchStatus status,
        String skipReason) {

    /** A run skipped because the previous one was still going. */
    public static final String SKIP_OVERLAP = "overlap";

    /** A run skipped because the source is in backoff. */
    public static final String SKIP_BACKOFF = "backoff";

    /** Completes {@code byReason} so every reason is present. */
    public FetchOutcome {
        EnumMap<PersistReason, Integer> all = new EnumMap<>(PersistReason.class);
        for (PersistReason reason : PersistReason.values()) {
            Integer count = byReason == null ? null : byReason.get(reason);
            all.put(reason, count == null ? 0 : count);
        }
        byReason = Collections.unmodifiableMap(all);
    }

    /** True when the run never happened. */
    public boolean wasSkipped() {
        return skipReason != null;
    }

    /** A run that did not happen. */
    public static FetchOutcome skipped(String sourceId, Instant at, String reason, String error) {
        return new FetchOutcome(
                sourceId, at, 0, false, 0, 0, 0, 0, 0, 0, 0, 0, 0, null, null, null, error, null, null, reason);
    }

    /** A fetch that ingested nothing: NOT_MODIFIED, an empty OK, FAILED, REFUSED or DISCARDED. */
    public static FetchOutcome withoutIngest(
            String sourceId,
            Instant startedAt,
            long durationMs,
            boolean success,
            int recordsSeen,
            String error,
            Long fetchId,
            FetchStatus status) {
        return new FetchOutcome(
                sourceId, startedAt, durationMs, success, recordsSeen, 0, 0, 0, 0, 0, 0, 0, 0, null, null, null, error,
                fetchId, status, null);
    }

    /** A fetch whose records the writer ingested. */
    public static FetchOutcome ingested(
            String sourceId, Instant startedAt, long durationMs, IngestOutcome outcome, Long fetchId) {
        return new FetchOutcome(
                sourceId,
                startedAt,
                durationMs,
                true,
                outcome.seen(),
                outcome.persisted(),
                outcome.persisted(),
                outcome.live(),
                outcome.touched(),
                outcome.touchesThrottled(),
                outcome.late(),
                outcome.skipped(),
                outcome.rejected(),
                outcome.byReason(),
                outcome.fromSeq() > 0 ? outcome.fromSeq() : null,
                outcome.toSeq() > 0 ? outcome.toSeq() : null,
                null,
                fetchId,
                FetchStatus.OK,
                null);
    }
}
