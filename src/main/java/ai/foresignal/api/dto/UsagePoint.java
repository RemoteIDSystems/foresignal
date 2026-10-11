package ai.foresignal.api.dto;

import java.time.Instant;

/**
 * The {@code UsagePoint} schema: request, byte and ingest counters for one bucket.
 *
 * @param t bucket start (for totals, the start of the range)
 * @param requests requests sent
 * @param bytes response bytes
 * @param errors failed requests
 * @param refused requests the budget refused
 * @param recordsSeen records adapters returned
 * @param versions versions persisted
 * @param liveUpdates live (non-persisted) updates
 * @param touches touch observations
 * @param late LATE observations
 * @param skipped skipped observations
 * @param rejected rejected observations
 * @param versionBytes bytes of persisted versions
 */
public record UsagePoint(
        Instant t,
        long requests,
        long bytes,
        long errors,
        long refused,
        long recordsSeen,
        long versions,
        long liveUpdates,
        long touches,
        long late,
        long skipped,
        long rejected,
        long versionBytes) {}
