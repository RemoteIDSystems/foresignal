package ai.foresignal.mapping;

import ai.foresignal.inbound.IngestUrls;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/**
 * The raw requests kept while a custom source's {@code captureSamples} is on (prompt 059): the last
 * {@value #PER_SOURCE} per source, newest first, in memory only (a restart forgets them).
 *
 * <p>A sample keeps the method, the format segment, {@code Content-Type}, {@code Content-Encoding}, the query
 * string and the decompressed body as text, at most {@value #MAX_BODY_CHARS} characters. Every other header is
 * dropped. Credentials are redacted before anything is stored: the key-in-path segment never reaches here (only
 * the format is kept), and the values of {@link IngestUrls#SECRET_PARAMETERS} ({@code PASSWORD}, {@code PASSKEY},
 * {@code key}, {@code auth}, {@code token}) are masked in the query and in a form body, and as JSON string
 * members of a JSON body.
 */
@Component
public class SampleCapture {

    /** Samples kept per source. */
    public static final int PER_SOURCE = 5;

    /** The longest body text kept. */
    public static final int MAX_BODY_CHARS = 64 * 1024;

    private static final Pattern JSON_SECRET = Pattern.compile(
            "(\"(?i:password|passkey|key|auth|token)\"\\s*:\\s*)\"(?:[^\"\\\\]|\\\\.)*\"");

    private final Map<String, Deque<Sample>> samples = new ConcurrentHashMap<>();

    /**
     * One captured request (OpenAPI {@code CapturedSample}).
     *
     * @param receivedAt when it was received
     * @param method the HTTP method
     * @param format the format path segment, or null for the default endpoint
     * @param contentType the {@code Content-Type}, or null
     * @param contentEncoding the {@code Content-Encoding}, or null
     * @param query the redacted query string, or null
     * @param body the redacted, decompressed body text, or null when empty
     * @param bodyBytes the decompressed body size
     * @param truncated true when the body text was cut at {@link #MAX_BODY_CHARS}
     */
    public record Sample(Instant receivedAt, String method, String format, String contentType, String contentEncoding,
            String query, String body, int bodyBytes, boolean truncated) {

        /** The {@code CapturedSample} JSON object. */
        public Map<String, Object> view() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("receivedAt", receivedAt);
            out.put("method", method);
            out.put("format", format);
            out.put("contentType", contentType);
            out.put("contentEncoding", contentEncoding);
            out.put("query", query);
            out.put("body", body);
            out.put("bodyBytes", bodyBytes);
            out.put("truncated", truncated);
            return out;
        }
    }

    /**
     * Keeps one request for {@code sourceId}, dropping the oldest beyond {@value #PER_SOURCE}.
     *
     * @param format the format segment, or null for {@code POST /v1/ingest/{sourceId}}
     * @param body the decompressed body
     */
    public void capture(String sourceId, Instant receivedAt, String method, String format, String contentType,
            String contentEncoding, String query, byte[] body) {
        Sample sample = sample(receivedAt, method, format, contentType, contentEncoding, query, body);
        Deque<Sample> deque = samples.computeIfAbsent(sourceId, k -> new ArrayDeque<>());
        synchronized (deque) {
            deque.addFirst(sample);
            while (deque.size() > PER_SOURCE) {
                deque.removeLast();
            }
        }
    }

    /** The samples of {@code sourceId}, newest first. */
    public List<Sample> list(String sourceId) {
        Deque<Sample> deque = samples.get(sourceId);
        if (deque == null) {
            return List.of();
        }
        synchronized (deque) {
            return List.copyOf(new ArrayList<>(deque));
        }
    }

    /** Forgets the samples of {@code sourceId}. */
    public void clear(String sourceId) {
        samples.remove(sourceId);
    }

    /** The redacted sample of one request. */
    static Sample sample(Instant receivedAt, String method, String format, String contentType, String contentEncoding,
            String query, byte[] body) {
        String text = body == null || body.length == 0 ? null : new String(body, StandardCharsets.UTF_8);
        boolean truncated = false;
        if (text != null && text.length() > MAX_BODY_CHARS) {
            text = text.substring(0, MAX_BODY_CHARS);
            truncated = true;
        }
        if (text != null) {
            text = redactBody(text, contentType);
        }
        String redactedQuery = query == null || query.isEmpty() ? null : IngestUrls.redactQuery(query);
        return new Sample(receivedAt, method == null ? null : method.toUpperCase(Locale.ROOT), format, contentType,
                contentEncoding, redactedQuery, text, body == null ? 0 : body.length, truncated);
    }

    /** A body with credential values masked: form pairs by name, JSON string members by name. */
    static String redactBody(String text, String contentType) {
        MediaType type = null;
        try {
            type = contentType == null ? null : MediaType.parseMediaType(contentType);
        } catch (org.springframework.util.InvalidMimeTypeException e) {
            type = null;
        }
        if (type != null && MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(type)) {
            return IngestUrls.redactQuery(text);
        }
        return JSON_SECRET.matcher(text).replaceAll("$1\"***\"");
    }
}
