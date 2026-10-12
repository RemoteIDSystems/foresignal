package ai.foresignal.llm.client;

import ai.foresignal.llm.LlmHttp;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** JSON-over-HTTP helpers shared by the {@code java.net.http} provider clients. */
final class HttpJson {

    /** Request timeout of a chat or embedding call. */
    static final Duration REQUEST_TIMEOUT = Duration.ofMinutes(10);

    /** Request timeout of a model listing. */
    static final Duration LIST_TIMEOUT = Duration.ofSeconds(30);

    private HttpJson() {}

    /** A JSON request with the given headers. */
    static HttpRequest.Builder request(URI uri, Duration timeout, Map<String, String> headers) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                .timeout(timeout)
                .header("Accept", "application/json");
        headers.forEach(builder::header);
        return builder;
    }

    /** Sends {@code request}; a non-2xx status becomes an {@link LlmProviderException}. */
    static JsonNode call(LlmHttp http, JsonMapper json, String providerId, HttpRequest request) {
        HttpResponse<String> response = http.send(providerId, request);
        if (response.statusCode() / 100 != 2) {
            throw failure(providerId, response.statusCode(), response.body(), response.headers()
                    .firstValue("retry-after").orElse(null), json);
        }
        return parse(json, providerId, response.body());
    }

    /** Parses a response body. */
    static JsonNode parse(JsonMapper json, String providerId, String body) {
        try {
            return json.readTree(body == null ? "" : body);
        } catch (JacksonException e) {
            throw new LlmProviderException(LlmProviderException.Kind.INVALID_RESPONSE, providerId,
                    "answered with a body that is not JSON", null, e);
        }
    }

    /** The exception for a non-2xx response; the provider's error message is kept (it never echoes our key). */
    static LlmProviderException failure(String providerId, int status, String body, String retryAfter,
            JsonMapper json) {
        String message = errorMessage(json, body);
        LlmProviderException.Kind kind = LlmProviderException.kindForStatus(status);
        return new LlmProviderException(kind, providerId,
                "HTTP " + status + (message == null ? "" : ": " + message),
                kind == LlmProviderException.Kind.RATE_LIMITED ? LlmProviderException.parseRetryAfter(retryAfter) : null,
                null);
    }

    /** {@code error.message}, {@code error} (a string) or {@code message} of an error body, truncated. */
    static String errorMessage(JsonMapper json, String body) {
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            JsonNode node = json.readTree(body);
            JsonNode error = node.get("error");
            String message = null;
            if (error != null && error.isObject() && error.get("message") != null) {
                message = error.get("message").asString();
            } else if (error != null && error.isString()) {
                message = error.asString();
            } else if (node.get("message") != null && node.get("message").isString()) {
                message = node.get("message").asString();
            }
            return message == null ? null : truncate(message);
        } catch (JacksonException e) {
            return truncate(body);
        }
    }

    static String truncate(String text) {
        return text.length() <= 300 ? text : text.substring(0, 300) + "…";
    }

    /** A float vector from a JSON array of numbers. */
    static float[] vector(JsonNode array) {
        float[] out = new float[array.size()];
        for (int i = 0; i < out.length; i++) {
            out[i] = (float) array.get(i).asDouble();
        }
        return out;
    }

    /** {@code node.get(name)} as a long, or 0. */
    static long longOf(JsonNode node, String name) {
        if (node == null) {
            return 0;
        }
        JsonNode value = node.get(name);
        return value == null || !value.isNumber() ? 0 : value.asLong();
    }
}
