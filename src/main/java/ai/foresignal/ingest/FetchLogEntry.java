package ai.foresignal.ingest;

import ai.foresignal.source.spi.Coverage;
import ai.foresignal.store.FetchStatus;
import ai.foresignal.store.FetchTrigger;
import java.time.Instant;

/**
 * The provenance of one batch, filled by the collector (prompt 010) and written by the ingest
 * writer as one {@code fetch_log} row (Core-Data-Model §8, §14).
 *
 * <p>The writer adds the counts, {@code ingested_at} and the seq range; nothing here is computed
 * by ingest.
 *
 * @param sourceId the source
 * @param trigger why the fetch ran
 * @param status how it ended
 * @param startedAt when the fetch started
 * @param receivedAt when the response, message or push arrived
 * @param httpStatus the final HTTP status, or null for non-HTTP transports
 * @param requestCount upstream requests made
 * @param urlRedacted the request URL with credentials masked
 * @param etag the response ETag, if any
 * @param bytes response bytes, if known
 * @param bodySha256 hex SHA-256 of the body, if computed
 * @param adapterVersion the adapter's version string
 * @param licence the descriptor's data license, snapshotted (the stored column is
 *     {@code fetch_log.licence})
 * @param principal who pushed the data, for push sources
 * @param coverage the upstream's coverage report
 * @param error the redacted error, for failed fetches
 */
public record FetchLogEntry(
        String sourceId,
        FetchTrigger trigger,
        FetchStatus status,
        Instant startedAt,
        Instant receivedAt,
        Integer httpStatus,
        int requestCount,
        String urlRedacted,
        String etag,
        Long bytes,
        String bodySha256,
        String adapterVersion,
        String licence,
        String principal,
        Coverage coverage,
        String error) {

    public FetchLogEntry {
        coverage = coverage == null ? Coverage.none() : coverage;
    }

    /** A minimal successful entry, for tests and simple producers. */
    public static FetchLogEntry ok(String sourceId, FetchTrigger trigger, Instant startedAt, Instant receivedAt) {
        return new FetchLogEntry(
                sourceId, trigger, FetchStatus.OK, startedAt, receivedAt, null, 0, null, null, null, null,
                null, null, null, Coverage.none(), null);
    }
}
