package ai.foresignal.cli.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import ai.foresignal.cli.config.Connection;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/** URI building, problem parsing and {@code Retry-After}. */
class ApiClientTest {

    private final ApiClient api = new ApiClient(new Connection("https://fs.example/", "fsr_x"),
            HttpClient.newHttpClient(), Duration.ofSeconds(5), JsonMapper.builder().build());

    private static HttpHeaders headers(Map<String, List<String>> map) {
        return HttpHeaders.of(map, (a, b) -> true);
    }

    @Test
    void buildsUrisFromPathsAndAbsoluteLocations() {
        assertEquals("https://fs.example/v1/system", api.uri("/v1/system", null).toString());
        assertEquals("https://fs.example/v1/records?source=a", api.uri("v1/records", Query.of().add("source", "a"))
                .toString());
        assertEquals("http://other/x?y=1&z=2", api.uri("http://other/x?y=1", Query.of().add("z", 2)).toString());
    }

    @Test
    void readsTheProblemCode() {
        ApiException e = api.problem(409, headers(Map.of("Retry-After", List.of("3"))),
                "{\"title\":\"Busy\",\"status\":409,\"code\":\"BUSY\",\"detail\":\"later\"}"
                        .getBytes(StandardCharsets.UTF_8));
        assertEquals("BUSY", e.code());
        assertEquals("Busy", e.title());
        assertEquals("later", e.detail());
        assertEquals(Duration.ofSeconds(3), e.retryAfter());
        assertEquals("BUSY (HTTP 409): Busy: later", e.getMessage());
    }

    @Test
    void fallsBackToTheStatusWithoutAProblem() {
        ApiException e = api.problem(500, headers(Map.of()), "oops".getBytes(StandardCharsets.UTF_8));
        assertEquals("HTTP_500", e.code());
        assertNull(e.problem());
    }

    @Test
    void parsesRetryAfterSecondsAndDates() {
        Instant now = Instant.parse("2026-10-18T10:00:00Z");
        assertEquals(Duration.ofSeconds(5), ApiClient.retryAfter("5", now));
        assertEquals(Duration.ofSeconds(30), ApiClient.retryAfter("Sun, 18 Oct 2026 10:00:30 GMT", now));
        assertEquals(Duration.ZERO, ApiClient.retryAfter("Sun, 18 Oct 2026 09:00:00 GMT", now));
        assertNull(ApiClient.retryAfter("soon", now));
        assertNull(ApiClient.retryAfter("-1", now));
        assertNull(ApiClient.retryAfter(null, now));
    }

    @Test
    void anEmptyBodyIsMissing() {
        assertEquals(true, api.parse(new ApiResponse(204, headers(Map.of()), new byte[0])).isMissingNode());
    }
}
