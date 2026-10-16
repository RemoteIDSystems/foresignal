package ai.foresignal.ingest;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Tombstones the records a complete-set reference dataset no longer lists (prompt 094). Submitted by the
 * {@link ai.foresignal.collect.FetchExecutor} after the last chunk of a successful, complete run.
 *
 * <p>Candidates are the non-tombstoned heads of {@code sourceId} whose kinds are in {@code kinds} and whose
 * {@code record_live.last_received_at} is older than {@code runStartedAt}: the run did not see them. The
 * shrink guard refuses to write anything when more than {@code maxAbsentRatio} of the dataset would be
 * tombstoned at once (a truncated download must never read as a mass deletion); the operator releases it
 * once with {@code allowShrink=true} on {@code POST /v1/sources/{id}/fetch}.
 *
 * <p>Tombstones are written through the normal fold ({@code persist_reason=TOMBSTONE}) in chunks of 10,000,
 * the command re-enqueuing itself at the queue tail like {@link HeadPurge}, so one writer turn stays short.
 *
 * @param sourceId the source
 * @param kinds the record kinds to reconcile; {@code dataset-release} and other bookkeeping kinds are left out
 * @param runStartedAt when the run started; a live record older than this was not seen by the run
 * @param maxAbsentRatio the largest fraction of the dataset that may be tombstoned at once, from the
 *     descriptor's {@code reconcile.maxAbsentRatio}
 * @param allowShrink true when the operator's one-off override releases the shrink guard
 * @param runId the run id of the fetch being reconciled, stamped on the {@code fetch_log} rows of the
 *     tombstone batches so they join the run's chunk rows; null for none
 * @param future completed with what the reconciliation did
 */
public record TombstoneAbsent(
        String sourceId,
        Set<String> kinds,
        Instant runStartedAt,
        double maxAbsentRatio,
        boolean allowShrink,
        String runId,
        CompletableFuture<TombstoneOutcome> future)
        implements IngestCommand {

    public TombstoneAbsent {
        kinds = kinds == null ? Set.of() : Set.copyOf(kinds);
        future = future == null ? new CompletableFuture<>() : future;
    }

    /** A reconciliation with no run id. */
    public TombstoneAbsent(String sourceId, Set<String> kinds, Instant runStartedAt, double maxAbsentRatio,
            boolean allowShrink, CompletableFuture<TombstoneOutcome> future) {
        this(sourceId, kinds, runStartedAt, maxAbsentRatio, allowShrink, null, future);
    }
}
