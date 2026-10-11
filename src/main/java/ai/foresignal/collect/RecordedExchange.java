package ai.foresignal.collect;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * One request and response captured for diagnostics.
 *
 * <p>Unlike {@link FetchTrace} this does keep a body, capped, because its only consumer is the
 * operator-triggered probe in prompt 008: when an adapter breaks because an upstream changed shape,
 * the first question is "what did it actually return?". Everything is redacted, and the capture
 * exists only for the duration of the probe request.
 *
 * @param method HTTP method
 * @param redactedUrl the URL with credential values masked
 * @param requestHeaders request headers, redacted
 * @param status HTTP status, or 0 when the request failed
 * @param responseHeaders response headers, redacted
 * @param contentType the declared content type
 * @param bytes bytes read
 * @param truncated true when the recorder's own cap stopped the capture
 * @param body up to the recorder's cap, redacted
 * @param duration wall time
 * @param failure the exception message when the request failed, else null
 */
public record RecordedExchange(
        String method,
        String redactedUrl,
        Map<String, List<String>> requestHeaders,
        int status,
        Map<String, List<String>> responseHeaders,
        String contentType,
        long bytes,
        boolean truncated,
        String body,
        Duration duration,
        String failure) {

    public RecordedExchange {
        requestHeaders = requestHeaders == null ? Map.of() : Map.copyOf(requestHeaders);
        responseHeaders = responseHeaders == null ? Map.of() : Map.copyOf(responseHeaders);
    }
}
