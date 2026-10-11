package ai.foresignal.collect;

import ai.foresignal.store.CoverageSample;
import ai.foresignal.store.SourceHealthEntity;
import java.time.Instant;
import java.util.List;

/**
 * An immutable copy of one {@code source_health} row, which is what {@link HealthEvaluator} reads.
 *
 * <p>A record rather than the JPA entity, so the evaluator stays a pure function that a test can
 * feed with literals.
 *
 * @param sourceId the source
 * @param lastAttemptAt the last attempt, or null when none has been made
 * @param lastSuccessAt the last success, or null
 * @param lastErrorAt the last failure, or null
 * @param lastError the last failure's redacted message, or null
 * @param lastHttpStatus the last failure's HTTP status, when it was an HTTP error
 * @param consecutiveFailures failures since the last success
 * @param lastRecordsSeen records seen by the last success, or null
 * @param lastRecordsChanged records changed by the last success, or null
 * @param recentRecordCounts the counted successes' record counts, newest last (304s excluded)
 * @param backoffUntil the persisted backoff deadline, or null
 * @param lastCoverage the latest coverage report, or null
 * @param recentCoverage the coverage ring, newest last
 * @param lastDataAt the last success that returned records, or null
 */
public record HealthSnapshot(
        String sourceId,
        Instant lastAttemptAt,
        Instant lastSuccessAt,
        Instant lastErrorAt,
        String lastError,
        Integer lastHttpStatus,
        int consecutiveFailures,
        Integer lastRecordsSeen,
        Integer lastRecordsChanged,
        List<Integer> recentRecordCounts,
        Instant backoffUntil,
        CoverageSample lastCoverage,
        List<CoverageSample> recentCoverage,
        Instant lastDataAt) {

    public HealthSnapshot {
        recentRecordCounts = recentRecordCounts == null ? List.of() : List.copyOf(recentRecordCounts);
        recentCoverage = recentCoverage == null ? List.of() : List.copyOf(recentCoverage);
    }

    /** The state of a source with no health row yet. */
    public static HealthSnapshot empty(String sourceId) {
        return new HealthSnapshot(
                sourceId, null, null, null, null, null, 0, null, null, List.of(), null, null, List.of(), null);
    }

    /** A copy of {@code entity}. */
    public static HealthSnapshot of(SourceHealthEntity entity) {
        return new HealthSnapshot(
                entity.getSourceId(),
                entity.getLastAttemptAt(),
                entity.getLastSuccessAt(),
                entity.getLastErrorAt(),
                entity.getLastError(),
                entity.getLastHttpStatus(),
                entity.getConsecutiveFailures(),
                entity.getLastRecordsSeen(),
                entity.getLastRecordsChanged(),
                entity.getRecentRecordCounts(),
                entity.getBackoffUntil(),
                entity.getLastCoverage(),
                entity.getRecentCoverage(),
                entity.getLastDataAt());
    }
}
