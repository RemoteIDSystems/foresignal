package ai.foresignal.raster.model;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.raster.FrameKeys;
import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.source.adapters.gfs.GfsAdapter;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.raster.ModelFixtures;
import ai.foresignal.testsupport.raster.RasterApiSupport;
import ai.foresignal.testsupport.raster.RasterTestSources;
import java.net.http.HttpResponse;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Forecast frame semantics over HTTP (prompt 051; Weather-Imagery-Design §6.1) on {@code gfs}: a
 * concrete time without {@code run} is {@code 400 RUN_REQUIRED}; {@code latest} is the newest complete
 * run's step nearest now, {@code now} the step at or before now within {@code maxGap}, {@code previous}
 * the complete run before; a URL is immutable only with a pinned run, a {@code ~sha8} frame key and a
 * versioned style.
 */
@WebIntegrationTest
@Import(RasterTestSources.class)
class ForecastSemanticsIT extends RasterApiSupport {

    static final Instant R1 = Instant.parse("2026-10-12T00:00:00Z");
    static final Instant R2 = Instant.parse("2026-10-12T06:00:00Z");
    static final Instant R3 = Instant.parse("2026-10-12T12:00:00Z");
    static final Instant NOW = Instant.parse("2026-10-12T10:00:00Z");

    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void setUp() throws Exception {
        ModelFixtures.clean(jdbc, paths);
        for (Instant run : new Instant[] {R1, R2}) {
            for (int step : new int[] {0, 3, 6}) {
                float shift = run.equals(R1) ? 0 : 1;
                pipeline.commit(GfsAdapter.ID, ModelFixtures.gfsStep(run, step, ModelFixtures.constant(5 + step + shift),
                        ModelFixtures.constant(-2)));
            }
            pipeline.completeRun(GfsAdapter.ID, GfsAdapter.WIND, run);
        }
        // A newer run still in progress never answers latest.
        pipeline.commit(GfsAdapter.ID, ModelFixtures.gfsStep(R3, 0, ModelFixtures.constant(1), ModelFixtures.constant(1)));
        clock.setInstant(NOW);
    }

    @AfterEach
    void tearDown() {
        ModelFixtures.clean(jdbc, paths);
    }

    private RasterFrameRow frame(Instant run, int step) {
        return frames.find(GfsAdapter.WIND, FrameKeys.forecastFrameId(run, step)).orElseThrow();
    }

    private String tile(String token, String query) {
        return "/v1/products/gfs/tiles/" + token + "/0/0/0.png" + query;
    }

    @Test
    @DisplayName("a concrete forecast time without run is 400 RUN_REQUIRED on tiles, images, fields and vectors")
    void runRequired() throws Exception {
        String timeKey = frame(R2, 3).timeKey();
        for (String path : new String[] {tile(timeKey, ""),
            "/v1/products/gfs/image/" + timeKey + ".png?bbox=-10,-10,10,10",
            "/v1/products/gfs/fields/" + timeKey + ".json",
            "/v1/products/gfs/fields/" + timeKey + ".f32",
            "/v1/products/gfs/vectors/" + timeKey + "/0/0/0.mvt"}) {
            HttpResponse<String> response = get(path);
            assertThat(response.statusCode()).as(path).isEqualTo(400);
            assertThat(code(response)).as(path).isEqualTo("RUN_REQUIRED");
        }
        assertThat(get(tile(timeKey, "?run=latest")).statusCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("latest is the newest complete run's step nearest now; now is at or before now; previous is the run before")
    void aliases() throws Exception {
        HttpResponse<String> latest = get("/v1/products/gfs/fields/latest.json");
        assertThat(latest.statusCode()).isEqualTo(200);
        assertThat(body(latest).get("cycle").get("runIso").asString()).isEqualTo("2026-10-12T06:00:00Z");
        assertThat(body(latest).get("cycle").get("forecastHour").asInt()).as("09:00 is nearest 10:00").isEqualTo(3);
        assertThat(body(latest).get("stale").asBoolean()).isFalse();

        clock.setInstant(Instant.parse("2026-10-12T11:40:00Z"));
        HttpResponse<String> nearest = get("/v1/products/gfs/fields/latest.json");
        assertThat(body(nearest).get("cycle").get("forecastHour").asInt()).as("12:00 is nearer 11:40").isEqualTo(6);
        HttpResponse<String> now = get("/v1/products/gfs/fields/now.json");
        assertThat(body(now).get("cycle").get("forecastHour").asInt()).as("09:00 is at or before 11:40").isEqualTo(3);

        HttpResponse<String> previous = get("/v1/products/gfs/fields/latest.json?run=previous");
        assertThat(body(previous).get("cycle").get("runIso").asString()).isEqualTo("2026-10-12T00:00:00Z");
        assertThat(body(previous).get("stale").asBoolean()).as("not the newest complete run").isTrue();

        clock.setInstant(Instant.parse("2026-10-12T19:00:00Z"));
        HttpResponse<String> gone = get("/v1/products/gfs/fields/now.json");
        assertThat(gone.statusCode()).as("12:00 is more than maxGap (6 h) before 19:00").isEqualTo(404);
        assertThat(code(gone)).isEqualTo("UNKNOWN_FRAME");

        HttpResponse<String> pinnedInProgress = get("/v1/products/gfs/fields/latest.json?run=" + R3);
        assertThat(pinnedInProgress.statusCode()).as("a run in progress is not complete").isEqualTo(404);
    }

    @Test
    @DisplayName("immutable only with a pinned run, a ~sha8 frame key and a versioned style")
    void immutability() throws Exception {
        RasterFrameRow f = frame(R2, 3);
        String pinned = "?run=" + FrameKeys.runIso(R2);
        HttpResponse<byte[]> immutable = getBytes(tile(f.frameKey(), pinned + "&style=wind-speed@1"));
        assertThat(immutable.statusCode()).isEqualTo(200);
        assertThat(header(immutable, "Cache-Control")).contains("immutable").contains("max-age=31536000");

        for (String query : new String[] {"?run=latest&style=wind-speed@1", pinned, pinned + "&style=wind-speed"}) {
            HttpResponse<byte[]> response = getBytes(tile(f.frameKey(), query));
            assertThat(response.statusCode()).as(query).isEqualTo(200);
            assertThat(header(response, "Cache-Control")).as(query).doesNotContain("immutable");
        }
        HttpResponse<byte[]> bareTimeKey = getBytes(tile(f.timeKey(), pinned + "&style=wind-speed@1"));
        assertThat(header(bareTimeKey, "Cache-Control")).doesNotContain("immutable");
        assertThat(header(bareTimeKey, "Foresignal-Frame-Time")).isEqualTo(f.validTime().toString());

        HttpResponse<String> manifest = get("/v1/products/gfs/fields/" + f.frameKey() + ".json" + pinned);
        assertThat(header(manifest, "Cache-Control")).contains("immutable");
        HttpResponse<String> manifestLatestRun = get("/v1/products/gfs/fields/" + f.frameKey() + ".json?run=latest");
        assertThat(header(manifestLatestRun, "Cache-Control")).doesNotContain("immutable");

        HttpResponse<String> stale = get("/v1/products/gfs/fields/" + f.timeKey() + "~00000000.json" + pinned);
        assertThat(stale.statusCode()).isEqualTo(404);
        assertThat(code(stale)).isEqualTo("UNKNOWN_FRAME");
        assertThat(header(stale, "Link")).contains(f.frameKey()).contains("rel=\"current\"");
    }

    @Test
    @DisplayName("an ISO-extended time is redirected (301) to its time key; timeline and loop are fixed routes")
    void canonicalAndReserved() throws Exception {
        RasterFrameRow f = frame(R2, 3);
        HttpResponse<String> redirect = get("/v1/products/gfs/fields/" + f.validTime() + ".json?run=latest");
        assertThat(redirect.statusCode()).isEqualTo(301);
        assertThat(header(redirect, "Location")).contains("/fields/" + f.timeKey() + ".json?run=latest");
        HttpResponse<String> timeline = get("/v1/products/timeline");
        assertThat(timeline.statusCode()).as("the timeline route, not an unknown product").isEqualTo(400);
        assertThat(code(timeline)).isEqualTo("VALIDATION_FAILED");
    }
}
