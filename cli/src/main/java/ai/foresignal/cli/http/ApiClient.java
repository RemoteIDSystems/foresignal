package ai.foresignal.cli.http;

import ai.foresignal.cli.config.Connection;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.MissingNode;

/**
 * A thin client of the Foresignal REST API over {@code java.net.http}: bearer authentication, JSON bodies,
 * ProblemDetail errors ({@link ApiException}) and connection failures ({@link NetworkException}).
 *
 * <p>The token is sent only in the {@code Authorization} header and is never logged or printed.
 */
public final class ApiClient {

    /** The {@code User-Agent} the CLI sends. */
    public static final String USER_AGENT = "foresignal-cli/" + ai.foresignal.cli.ForesignalCli.VERSION;

    /**
     * The {@code Accept} of buffered calls: JSON first, GeoJSON (tracks, features) next, anything as a last resort;
     * {@code application/problem+json} is always added.
     */
    public static final String JSON_ACCEPT = "application/json, application/geo+json;q=0.9, */*;q=0.1";

    private final Connection connection;
    private final HttpClient http;
    private final Duration timeout;
    private final JsonMapper json;

    /**
     * Creates a client.
     *
     * @param connection the base URL and token
     * @param http       the HTTP client
     * @param timeout    the per-request timeout of buffered calls
     * @param json       the JSON mapper
     */
    public ApiClient(Connection connection, HttpClient http, Duration timeout, JsonMapper json) {
        this.connection = connection;
        this.http = http;
        this.timeout = timeout;
        this.json = json;
    }

    /**
     * The connection this client uses.
     *
     * @return the connection
     */
    public Connection connection() {
        return connection;
    }

    /**
     * The JSON mapper.
     *
     * @return the mapper
     */
    public JsonMapper json() {
        return json;
    }

    /**
     * Builds an absolute URI from an API path (or an absolute URL, such as a {@code Location} header) and a query.
     *
     * @param path  {@code /v1/…}, or an absolute {@code http(s)://} URL
     * @param query the query, or null
     * @return the URI
     */
    public URI uri(String path, Query query) {
        String base = path.startsWith("http://") || path.startsWith("https://")
                ? path
                : connection.baseUrl() + (path.startsWith("/") ? path : "/" + path);
        if (query == null || query.isEmpty()) {
            return URI.create(base);
        }
        return URI.create(base + (base.contains("?") ? "&" : "?") + query.encode());
    }

    /**
     * {@code GET} returning JSON.
     *
     * @param path  the API path
     * @param query the query, or null
     * @return the parsed body
     * @throws ApiException     on an error status
     * @throws NetworkException when the server cannot be reached
     */
    public JsonNode get(String path, Query query) {
        return parse(checked(send("GET", path, query, null, Map.of(), timeout)));
    }

    /**
     * {@code POST} with an optional JSON body, returning JSON.
     *
     * @param path  the API path
     * @param query the query, or null
     * @param body  the body, or null for none
     * @return the parsed body ({@link MissingNode} when empty)
     * @throws ApiException     on an error status
     * @throws NetworkException when the server cannot be reached
     */
    public JsonNode post(String path, Query query, JsonNode body) {
        return parse(checked(send("POST", path, query, body, Map.of(), timeout)));
    }

    /**
     * {@code PUT} with a JSON body, returning JSON.
     *
     * @param path the API path
     * @param body the body
     * @return the parsed body
     * @throws ApiException     on an error status
     * @throws NetworkException when the server cannot be reached
     */
    public JsonNode put(String path, JsonNode body) {
        return parse(checked(send("PUT", path, null, body, Map.of(), timeout)));
    }

    /**
     * {@code DELETE}.
     *
     * @param path the API path
     * @return the parsed body ({@link MissingNode} for 204)
     * @throws ApiException     on an error status
     * @throws NetworkException when the server cannot be reached
     */
    public JsonNode delete(String path) {
        return parse(checked(send("DELETE", path, null, null, Map.of(), timeout)));
    }

    /**
     * Sends a buffered request without checking the status.
     *
     * @param method      the HTTP method
     * @param path        the API path or an absolute URL
     * @param query       the query, or null
     * @param body        a JSON body, or null
     * @param headers     extra headers
     * @param callTimeout the timeout of this call
     * @return the response
     * @throws NetworkException when the server cannot be reached
     */
    public ApiResponse send(String method, String path, Query query, JsonNode body, Map<String, String> headers,
            Duration callTimeout) {
        HttpRequest request = request(method, path, query, body, headers, callTimeout, JSON_ACCEPT);
        try {
            HttpResponse<byte[]> response = http.send(request, HttpResponse.BodyHandlers.ofByteArray());
            byte[] bytes = response.body() == null ? new byte[0] : response.body();
            return new ApiResponse(response.statusCode(), response.headers(), bytes);
        } catch (IOException e) {
            throw network(request.uri(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new NetworkException("Interrupted while calling " + request.uri(), e);
        }
    }

    /**
     * Opens a streaming response (SSE) without checking the status; the caller reads or closes the body. There is
     * no timeout once the response headers have arrived.
     *
     * @param method        the HTTP method
     * @param path          the API path or an absolute URL
     * @param query         the query, or null
     * @param body          a JSON body, or null
     * @param headers       extra headers (e.g. {@code Last-Event-ID})
     * @param accept        the {@code Accept} header
     * @param headerTimeout how long to wait for the response headers, or null for no limit
     * @return the response with an unread body
     * @throws NetworkException when the server cannot be reached or the headers do not arrive in time
     */
    public HttpResponse<InputStream> open(String method, String path, Query query, JsonNode body,
            Map<String, String> headers, String accept, Duration headerTimeout) {
        HttpRequest request = request(method, path, query, body, headers, headerTimeout, accept);
        try {
            return http.send(request, HttpResponse.BodyHandlers.ofInputStream());
        } catch (IOException e) {
            throw network(request.uri(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new NetworkException("Interrupted while calling " + request.uri(), e);
        }
    }

    /**
     * Downloads a response body into a file.
     *
     * @param path    the API path
     * @param query   the query, or null
     * @param headers extra headers
     * @param target  the file to write
     * @return the response headers
     * @throws ApiException     on an error status
     * @throws NetworkException when the server cannot be reached or the transfer fails
     */
    public HttpHeaders download(String path, Query query, Map<String, String> headers, Path target) {
        HttpRequest request = request("GET", path, query, null, headers, null, "*/*");
        try {
            HttpResponse<InputStream> response = http.send(request, HttpResponse.BodyHandlers.ofInputStream());
            try (InputStream in = response.body()) {
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throw problem(response.statusCode(), response.headers(), in.readAllBytes());
                }
                java.nio.file.Files.copy(in, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            return response.headers();
        } catch (IOException e) {
            throw network(request.uri(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new NetworkException("Interrupted while calling " + request.uri(), e);
        }
    }

    /**
     * Throws for a non-2xx response.
     *
     * @param response the response
     * @return the same response when it is 2xx
     * @throws ApiException otherwise
     */
    public ApiResponse checked(ApiResponse response) {
        if (!response.ok()) {
            throw problem(response.status(), response.headers(), response.body());
        }
        return response;
    }

    /**
     * Parses a response body as JSON.
     *
     * @param response the response
     * @return the document, or {@link MissingNode} when the body is empty
     * @throws ApiException when a 2xx body is not JSON
     */
    public JsonNode parse(ApiResponse response) {
        if (response.body().length == 0) {
            return MissingNode.getInstance();
        }
        try {
            return json.readTree(response.body());
        } catch (JacksonException e) {
            throw new ApiException(response.status(), "INVALID_RESPONSE", "The server's response is not JSON",
                    e.getOriginalMessage(), null, null);
        }
    }

    /**
     * Builds the {@link ApiException} for an error response, reading the ProblemDetail when there is one.
     *
     * @param status  the HTTP status
     * @param headers the response headers
     * @param body    the body bytes
     * @return the exception
     */
    public ApiException problem(int status, HttpHeaders headers, byte[] body) {
        JsonNode problem = null;
        if (body != null && body.length > 0) {
            try {
                JsonNode node = json.readTree(body);
                if (node != null && node.isObject()) {
                    problem = node;
                }
            } catch (JacksonException e) {
                // Not JSON (a proxy's HTML page, say); fall back to the status.
            }
        }
        String code = problem != null && problem.path("code").isString()
                ? problem.path("code").asString()
                : "HTTP_" + status;
        String title = problem != null && problem.path("title").isString() ? problem.path("title").asString() : null;
        String detail = problem != null && problem.path("detail").isString()
                ? problem.path("detail").asString()
                : null;
        return new ApiException(status, code, title, detail, problem,
                retryAfter(headers.firstValue("Retry-After").orElse(null), Instant.now()));
    }

    /**
     * Parses a {@code Retry-After} header: delta seconds or an HTTP date.
     *
     * @param value the header value, or null
     * @param now   the current time (for HTTP dates)
     * @return the delay, or null when absent or unparsable
     */
    public static Duration retryAfter(String value, Instant now) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String v = value.trim();
        try {
            long seconds = Long.parseLong(v);
            return seconds < 0 ? null : Duration.ofSeconds(seconds);
        } catch (NumberFormatException e) {
            try {
                Instant at = ZonedDateTime.parse(v, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
                Duration d = Duration.between(now, at);
                return d.isNegative() ? Duration.ZERO : d;
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }
    }

    private HttpRequest request(String method, String path, Query query, JsonNode body, Map<String, String> headers,
            Duration callTimeout, String accept) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri(path, query))
                .header("Accept", accept + ", application/problem+json")
                .header("User-Agent", USER_AGENT);
        if (callTimeout != null) {
            builder.timeout(callTimeout);
        }
        if (connection.hasToken()) {
            builder.header("Authorization", "Bearer " + connection.token());
        }
        headers.forEach(builder::header);
        if (body != null) {
            builder.header("Content-Type", "application/json");
            builder.method(method, HttpRequest.BodyPublishers.ofByteArray(json.writeValueAsBytes(body)));
        } else {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        }
        return builder.build();
    }

    private static NetworkException network(URI uri, IOException e) {
        String what = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        return new NetworkException("Cannot reach " + uri.getScheme() + "://" + uri.getAuthority() + ": " + what, e);
    }
}
