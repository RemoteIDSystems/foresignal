package ai.elaro.collect;

import java.time.Duration;
import java.time.Instant;

/**
 * What one upstream request did, with no body kept (Core-Data-Model, prompt 005 notes).
 *
 * <p>Prompt 008 turns these into {@code fetch_log} rows. The body is deliberately absent: a fetch
 * log that stored bodies would become the largest table in the database and the most likely place
 * for a credential or a licensed payload to end up. The SHA-256 is computed while the body streams
 * past, which gives "did the response change?" without keeping it.
 *
 * @param sourceId the source
 * @param method HTTP method
 * @param redactedUrl the URL with credential values masked
 * @param status HTTP status, or 0 when the request never completed
 * @param etag the response ETag, if any
 * @param bytes bytes read
 * @param bodySha256 lowercase hex SHA-256 of the bytes read, or null when none were
 * @param truncated true when the byte cap stopped the read
 * @param notModified true for a 304
 * @param duration wall time for the request
 * @param startedAt when it began
 * @param failure the exception class name when the request failed, else null
 */
public record FetchTrace(
        String sourceId,
        String method,
        String redactedUrl,
        int status,
        String etag,
        long bytes,
        String bodySha256,
        boolean truncated,
        boolean notModified,
        Duration duration,
        Instant startedAt,
        String failure) {

    /** True when the upstream answered with a success status. */
    public boolean isSuccess() {
        return status >= 200 && status < 400;
    }

    /** A trace for a request that never reached the upstream, for example a refused budget. */
    public static FetchTrace notSent(
            String sourceId, String method, String redactedUrl, Instant startedAt, String failure) {
        return new FetchTrace(
                sourceId, method, redactedUrl, 0, null, 0, null, false, false, Duration.ZERO, startedAt, failure);
    }
}
