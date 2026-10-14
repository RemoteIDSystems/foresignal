package ai.foresignal.api.dto;

import java.time.Instant;

/**
 * The {@code FetchLogEntry} schema: provenance of one fetch, stream flush or push batch. Never
 * carries a request or response body.
 *
 * @param id fetch id (the {@code batchId} on versions)
 * @param trigger {@code SCHEDULED}, {@code MANUAL}, {@code STREAM} or {@code PUSH}
 * @param status {@code OK}, {@code NOT_MODIFIED}, {@code FAILED}, {@code REFUSED} or {@code DISCARDED}
 * @param startedAt when it started
 * @param receivedAt when the response arrived
 * @param ingestedAt when the writer inserted the row
 * @param httpStatus last upstream HTTP status
 * @param requestCount requests made
 * @param urlRedacted last request URL with credentials redacted (by prompt 005, not here)
 * @param etag the response ETag
 * @param bytes response bytes
 * @param bodySha256 SHA-256 of the response body
 * @param adapterVersion the adapter version
 * @param licence license snapshotted on the row (wire name {@code licence})
 * @param principal the pushing principal (PUSH rows)
 * @param recordsSeen records seen
 * @param rejected rejected observations
 * @param late LATE observations
 * @param skipped skipped observations
 * @param versionsWritten versions written
 * @param liveUpdates live updates
 * @param touches touch observations
 * @param fromSeq first seq written
 * @param toSeq last seq written
 * @param coverage reported coverage
 * @param error redacted error
 */
public record FetchLogEntryDto(
        long id,
        String trigger,
        String status,
        Instant startedAt,
        Instant receivedAt,
        Instant ingestedAt,
        Integer httpStatus,
        int requestCount,
        String urlRedacted,
        String etag,
        Long bytes,
        String bodySha256,
        String adapterVersion,
        String licence,
        String principal,
        int recordsSeen,
        int rejected,
        int late,
        int skipped,
        int versionsWritten,
        int liveUpdates,
        int touches,
        Long fromSeq,
        Long toSeq,
        CoverageDto coverage,
        String error,
        String runId) {}
