package ai.foresignal.security.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.settings.AppSettingService;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.stream.StreamTestSources;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

/**
 * The spoofing counterpart of {@link ClientRateLimitFilterIT#trustedProxyKeying} (prompt 035): when
 * the connecting address is not a configured internal proxy, a client cannot escape its bucket by
 * sending a fresh {@code X-Forwarded-For} on every request; the key stays the socket address.
 */
@WebIntegrationTest
@Import(StreamTestSources.class)
@TestPropertySource(properties = {
        "foresignal.ratelimit.enabled=true",
        "server.tomcat.remoteip.internal-proxies=10\\.255\\.255\\.254"
})
class ClientRateLimitUntrustedProxyIT {

    static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    @LocalServerPort int port;
    @Autowired RateLimitSettingsService rateLimits;
    @Autowired ClientRateLimiter limiter;
    @Autowired MutableClock clock;
    @Autowired AppSettingService settings;

    @BeforeEach
    void setUp() {
        clock.setInstant(Instant.parse("2026-05-01T12:00:00Z"));
        settings.invalidateCache();
        limiter.reset();
        rateLimits.replace(new RateLimitSettings(2, 100, null, 100, 100, 100, null));
    }

    @AfterEach
    void tearDown() {
        rateLimits.replace(RateLimitSettings.defaults());
        limiter.reset();
    }

    int get(String forwardedFor) throws Exception {
        return HTTP.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/v1/records"))
                .timeout(Duration.ofSeconds(10))
                .header("X-Forwarded-For", forwardedFor)
                .GET()
                .build(), HttpResponse.BodyHandlers.ofString()).statusCode();
    }

    @Test
    @DisplayName("a spoofed X-Forwarded-For from an untrusted peer does not open a new bucket")
    void spoofedHeaderIgnored() throws Exception {
        assertThat(get("203.0.113.1")).isEqualTo(200);
        assertThat(get("203.0.113.2")).isEqualTo(200);

        assertThat(get("203.0.113.3")).isEqualTo(429);
    }
}
