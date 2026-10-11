package ai.foresignal.collect;

import java.util.List;

/**
 * What a probe returns (OpenAPI {@code ProbeResult}).
 *
 * @param sourceId the source
 * @param requests the upstream exchanges, credentials redacted, bodies truncated to the probe's
 *     {@code maxBytes}
 * @param records up to 100 parsed records
 * @param parseError why the adapter failed, or null
 */
public record ProbeResult(
        String sourceId, List<ProbeResult.Request> requests, List<NormalizedRecordPreview> records, String parseError) {

    /** The most previews a probe returns. */
    public static final int MAX_RECORDS = 100;

    /** Copies the lists. */
    public ProbeResult {
        requests = requests == null ? List.of() : List.copyOf(requests);
        records = records == null ? List.of() : List.copyOf(records);
    }

    /**
     * One upstream exchange.
     *
     * @param method HTTP method
     * @param url the URL, credentials redacted
     * @param status HTTP status, or 0 when the request failed
     * @param durationMs wall time
     * @param contentType the declared content type
     * @param bytes bytes read
     * @param truncated true when the body shown is not the whole response
     * @param body the body, redacted and truncated
     */
    public record Request(
            String method,
            String url,
            int status,
            long durationMs,
            String contentType,
            long bytes,
            boolean truncated,
            String body) {

        /** The view of a recorded exchange, truncated to {@code maxBytes}. */
        public static Request of(RecordedExchange exchange, int maxBytes) {
            String body = exchange.body();
            boolean capped = body != null && body.endsWith(RequestRecorder.TRUNCATION_MARKER);
            return new Request(
                    exchange.method(),
                    exchange.redactedUrl(),
                    exchange.status(),
                    exchange.duration() == null ? 0 : exchange.duration().toMillis(),
                    exchange.contentType(),
                    exchange.bytes(),
                    exchange.truncated() || capped || exchange.bytes() > maxBytes,
                    body);
        }
    }
}
