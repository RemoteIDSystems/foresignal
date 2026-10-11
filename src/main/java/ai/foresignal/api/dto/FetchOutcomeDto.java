package ai.foresignal.api.dto;

import java.time.Instant;
import java.util.Map;

/**
 * The {@code FetchOutcome} schema: what a manual fetch did.
 *
 * @param sourceId the source
 * @param startedAt when the fetch started
 * @param durationMs how long it took
 * @param success whether it succeeded
 * @param recordsSeen records the adapter returned
 * @param recordsChanged alias of {@code persisted}, kept for v1.1 clients
 * @param fromSeq first seq written, or null
 * @param toSeq last seq written, or null
 * @param error the redacted error, or null
 * @param fetchId the {@code fetch_log} row of this fetch
 * @param persisted versions persisted
 * @param live live (non-persisted) updates
 * @param touched touch observations (009 Deviation 8: observations, not rows)
 * @param touchesThrottled touches suppressed by the throttle
 * @param late LATE observations
 * @param skipped skipped observations
 * @param rejected rejected observations
 * @param byReason persisted versions per persist reason; all nine reasons always present
 */
public record FetchOutcomeDto(
        String sourceId,
        Instant startedAt,
        long durationMs,
        boolean success,
        int recordsSeen,
        int recordsChanged,
        Long fromSeq,
        Long toSeq,
        String error,
        Long fetchId,
        int persisted,
        int live,
        int touched,
        int touchesThrottled,
        int late,
        int skipped,
        int rejected,
        Map<String, Integer> byReason) {}
