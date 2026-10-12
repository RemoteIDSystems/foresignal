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
 * @param boundExternalId the entity a device-scoped key is bound to (prompt 039), or null. A format
 *     that reads an explicit device id from the payload throws {@link DeviceEntityMismatchException}
 *     when it differs, and otherwise uses this id for every record.
 */
public record IngestRequest(
        byte[] body, MediaType contentType, HttpHeaders headers, Map<String, List<String>> query, Instant receivedAt,
        String boundExternalId) {

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

    /** A request without a device binding (the shape before prompt 039). */
    public IngestRequest(byte[] body, MediaType contentType, HttpHeaders headers, Map<String, List<String>> query,
            Instant receivedAt) {
        this(body, contentType, headers, query, receivedAt, null);
    }

    /** This request bound to {@code externalId} (a device key, prompt 039). */
    public IngestRequest withBoundExternalId(String externalId) {
        return new IngestRequest(body, contentType, headers, query, receivedAt, externalId);
    }

    /**
     * The externalId to use for a record whose payload names {@code claimed} (null when it names none)
     * and would otherwise default to {@code fallback}: the bound id for a device key (403
     * {@code DEVICE_ENTITY_MISMATCH} when {@code claimed} differs), else {@code claimed}, else
     * {@code fallback}.
     */
    public String externalId(String claimed, String fallback) {
        if (boundExternalId != null) {
            if (claimed != null && !claimed.isBlank() && !boundExternalId.equals(claimed.strip())) {
                throw new DeviceEntityMismatchException(claimed.strip(), boundExternalId);
            }
            return boundExternalId;
        }
        if (claimed != null && !claimed.isBlank()) {
            return claimed.strip();
        }
        return fallback;
    }

    /** The first value of the query parameter {@code name}, matched case-insensitively, or null. */
    public String queryParamIgnoreCase(String name) {
        for (Map.Entry<String, List<String>> e : query.entrySet()) {
            if (e.getKey().equalsIgnoreCase(name) && !e.getValue().isEmpty()) {
                return e.getValue().get(0);
            }
        }
        return null;
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
