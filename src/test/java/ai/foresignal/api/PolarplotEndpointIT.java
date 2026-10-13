package ai.foresignal.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.media.LiveImageStore;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.spi.ImageRejectedException;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.media.ImageFixtures;
import ai.foresignal.testsupport.raster.RasterTestSources;
import ai.foresignal.testsupport.security.TestTokens;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

/**
 * {@code GET /v1/ground-stations/{sourceId}/polarplot.jpeg} over HTTP (prompt 049,
 * Weather-Imagery-Design §6.6): the held image with {@code Cache-Control: private, max-age=5},
 * {@code 404 NO_ACTIVE_PASS} without a pass, {@code 404 NOT_FOUND} for an unknown station, the read
 * scope with {@code openReads} off, and never a fetch on client demand (only what the adapter held).
 */
@WebIntegrationTest
@Import(RasterTestSources.class)
class PolarplotEndpointIT {

    static final String SOURCE = "satdump-" + RasterTestSources.SATDUMP_STATION;
    static final String PATH = "/v1/ground-stations/" + SOURCE + "/polarplot.jpeg";
    static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    @LocalServerPort int port;
    @Autowired LiveImageStore images;
    @Autowired AppSettingService settings;
    @Autowired TokenService tokens;
    @Autowired MutableClock clock;

    @BeforeEach
    void setUp() {
        clock.setInstant(Instant.parse("2026-10-12T18:05:00Z"));
        settings.invalidateCache();
        images.clear(SOURCE);
    }

    @AfterEach
    void tearDown() {
        images.clear(SOURCE);
    }

    private HttpResponse<byte[]> get(String path, String... headers) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(20)).GET();
        for (int i = 0; i < headers.length; i += 2) {
            request.header(headers[i], headers[i + 1]);
        }
        return HTTP.send(request.build(), HttpResponse.BodyHandlers.ofByteArray());
    }

    @Test
    @DisplayName("an active pass: the held JPEG, private max-age=5; without one: 404 NO_ACTIVE_PASS")
    void heldImage() throws Exception {
        HttpResponse<byte[]> none = get(PATH);
        assertThat(none.statusCode()).isEqualTo(404);
        assertThat(new String(none.body(), StandardCharsets.UTF_8)).contains("NO_ACTIVE_PASS");

        byte[] jpeg = ImageFixtures.jpeg(64, 64);
        images.hold(SOURCE, jpeg, clock.instant());
        HttpResponse<byte[]> ok = get(PATH);
        assertThat(ok.statusCode()).isEqualTo(200);
        assertThat(ok.headers().firstValue("Content-Type")).contains("image/jpeg");
        assertThat(ok.headers().firstValue("Cache-Control")).contains("private, max-age=5");
        assertThat(ok.body()).isEqualTo(jpeg);

        images.clear(SOURCE);
        assertThat(get(PATH).statusCode()).isEqualTo(404);
    }

    @Test
    @DisplayName("an unknown or non-SatDump source is 404 NOT_FOUND")
    void unknown() throws Exception {
        HttpResponse<byte[]> unknown = get("/v1/ground-stations/satdump-nowhere/polarplot.jpeg");
        assertThat(unknown.statusCode()).isEqualTo(404);
        assertThat(new String(unknown.body(), StandardCharsets.UTF_8)).contains("NOT_FOUND")
                .doesNotContain("NO_ACTIVE_PASS");
        assertThat(get("/v1/ground-stations/usgs-earthquakes/polarplot.jpeg").statusCode()).isEqualTo(404);
    }

    @Test
    @DisplayName("with openReads off a READ token reads it and no token gets 401")
    void readScope() throws Exception {
        images.hold(SOURCE, ImageFixtures.jpeg(32, 32), clock.instant());
        settings.put(SettingKeys.SECURITY, new SecuritySettings(false));
        try {
            assertThat(get(PATH).statusCode()).isEqualTo(401);
            String reader = TestTokens.bearer(TestTokens.create(tokens, "polar-reader", Scope.READ));
            assertThat(get(PATH, "Authorization", reader).statusCode()).isEqualTo(200);
        } finally {
            settings.put(SettingKeys.SECURITY, new SecuritySettings(true));
        }
    }

    @Test
    @DisplayName("the guard refuses a non-JPEG and drops the previous plot, so a stale one is never served")
    void guard() throws Exception {
        images.hold(SOURCE, ImageFixtures.jpeg(32, 32), clock.instant());
        assertThatThrownBy(() -> images.hold(SOURCE, ImageFixtures.png(8, 8), clock.instant()))
                .isInstanceOf(ImageRejectedException.class);
        assertThat(get(PATH).statusCode()).isEqualTo(404);
    }
}
