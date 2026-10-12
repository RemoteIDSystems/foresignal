package ai.foresignal.inbound.format;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * One inbound push as an {@link IngestFormat} sees it: the raw body (already size-capped and
 * decompressed), the headers and the query parameters.
 *
 * @param body the raw body bytes; never null
 * @param contentType the request's content type, or null when none was sent
 * @param headers the request headers
 * @param query the query parameters, each name to its values in order
 * @param receivedAt when Foresignal received the request
 */
public record IngestRequest(
        byte[] body, MediaType contentType, HttpHeaders headers, Map<String, List<String>> query, Instant receivedAt) {

    /** Copies the collections and replaces nulls with empty values. */
    public IngestRequest {
        body = body == null ? new byte[0] : body;
        HttpHeaders copy = new HttpHeaders();
        if (headers != null) {
            headers.forEach((name, values) -> copy.put(name, List.copyOf(values)));
        }
        headers = HttpHeaders.readOnlyHttpHeaders(copy);
        Map<String, List<String>> q = new LinkedHashMap<>();
        if (query != null) {
            query.forEach((name, values) -> q.put(name, values == null ? List.of() : List.copyOf(values)));
        }
        query = Map.copyOf(q);
        Objects.requireNonNull(receivedAt, "receivedAt");
    }

    /** A request with only a body and a content type, for tests and simple callers. */
    public static IngestRequest of(byte[] body, MediaType contentType, Instant receivedAt) {
        return new IngestRequest(body, contentType, new HttpHeaders(), Map.of(), receivedAt);
    }

    /** The first value of the header {@code name} (case-insensitive), or null. */
    public String header(String name) {
        return headers.getFirst(name);
    }

    /** The first value of the query parameter {@code name}, or null. */
    public String queryParam(String name) {
        List<String> values = query.get(name);
        return values == null || values.isEmpty() ? null : values.get(0);
    }

    /** The body decoded as UTF-8. */
    public String bodyText() {
        return new String(body, StandardCharsets.UTF_8);
    }

    /** True when the body has at least one byte. */
    public boolean hasBody() {
        return body.length > 0;
    }

    /** True when the content type is compatible with {@code type}. */
    public boolean isContentType(MediaType type) {
        return contentType != null && type.isCompatibleWith(contentType);
    }
}
