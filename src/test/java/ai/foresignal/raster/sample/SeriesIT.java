package ai.foresignal.raster.sample;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.openapi.SpecSchemas;
import ai.foresignal.testsupport.raster.RasterApiSupport;
import ai.foresignal.testsupport.raster.RasterFixtures;
import ai.foresignal.testsupport.raster.RasterTestSources;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.JsonNode;

/**
 * {@code getProductSeries} (prompt 052): observed frames in time order with their status, one forecast run's
 * steps, and the 500-step cap (400 beyond). Validated against the spec's {@code Series}.
 */
@WebIntegrationTest
@Import(RasterTestSources.class)
class SeriesIT extends RasterApiSupport {

    static final SpecSchemas SCHEMAS = new SpecSchemas();

    @Autowired JdbcTemplate jdbc;

    private void clearFrames() {
        jdbc.update("delete from raster_frame where product_id in (?, ?)", OBS, FCST);
    }

    @Test
    @DisplayName("observed frames, oldest first, each sampled with status and frame key")
    void observed() throws Exception {
        clearFrames();
        commitObservation(T.minus(Duration.ofMinutes(4)), 0);
        commitObservation(T.minus(Duration.ofMinutes(2)), 1);
        commitObservation(T, 2);
        HttpResponse<String> r = get("/v1/products/" + OBS + "/series?lon=-97.75&lat=31.0");
        assertThat(r.statusCode()).isEqualTo(200);
        JsonNode doc = body(r);
        assertThat(SCHEMAS.validateComponent("Series", doc)).isEmpty();
        assertThat(doc.get("items")).hasSize(3);
        assertThat(doc.get("items").get(0).get("time").asString()).isEqualTo(T.minus(Duration.ofMinutes(4)).toString());
        assertThat(doc.get("items").get(2).get("kind").asString()).isEqualTo("observed");
        // Codes 66, 67, 68 at the sampled cell: 0.0, 0.5 and 1.0 dBZ.
        assertThat(doc.get("items").get(1).get("value").asDouble()).isEqualTo(0.5);
        assertThat(doc.get("items").get(2).get("status").asString()).isEqualTo("ok");
        assertThat(doc.get("items").get(2).get("frameKey").asString()).isNotBlank();
        JsonNode noCov = body(get("/v1/products/" + OBS + "/series?lon=-99.25&lat=33.0"));
        assertThat(noCov.get("items").get(0).get("status").asString()).isEqualTo("no-coverage");
    }

    @Test
    @DisplayName("a forecast product's series is the steps of one run")
    void forecast() throws Exception {
        clearFrames();
        commitRun(T, 3);
        JsonNode doc = body(get("/v1/products/" + FCST + "/series?lon=-97.75&lat=31.0&run=latest"));
        assertThat(SCHEMAS.validateComponent("Series", doc)).isEmpty();
        assertThat(doc.get("run").asString()).isNotBlank();
        assertThat(doc.get("items")).hasSize(3);
        assertThat(doc.get("items").get(2).get("kind").asString()).isEqualTo("forecast");
        assertThat(doc.get("items").get(2).get("step").asInt()).isEqualTo(2);
    }

    @Test
    @DisplayName("more than 500 steps in the range is 400 VALIDATION_FAILED; 500 is fine")
    void cap() throws Exception {
        clearFrames();
        Instant start = T.minus(Duration.ofMinutes(500));
        for (int i = 0; i < 500; i++) {
            pipeline.commit(SOURCE, RasterFixtures.observation(start.plus(Duration.ofMinutes(i)), i % 3));
        }
        String range = "&from=" + start.minusSeconds(1) + "&to=" + T;
        HttpResponse<String> ok = get("/v1/products/" + OBS + "/series?lon=-97.75&lat=31.0" + range);
        assertThat(ok.statusCode()).isEqualTo(200);
        assertThat(body(ok).get("items")).hasSize(500);
        pipeline.commit(SOURCE, RasterFixtures.observation(T, 0));
        HttpResponse<String> over = get("/v1/products/" + OBS + "/series?lon=-97.75&lat=31.0" + range);
        assertThat(over.statusCode()).isEqualTo(400);
        assertThat(code(over)).isEqualTo("VALIDATION_FAILED");
    }

    @Test
    @DisplayName("from after to is 400; unknown product 404")
    void errors() throws Exception {
        assertThat(get("/v1/products/" + OBS + "/series?lon=-97.75&lat=31.0&from=" + T + "&to=" + T.minusSeconds(60))
                .statusCode()).isEqualTo(400);
        assertThat(get("/v1/products/nope/series?lon=-97.75&lat=31.0").statusCode()).isEqualTo(404);
    }
}
