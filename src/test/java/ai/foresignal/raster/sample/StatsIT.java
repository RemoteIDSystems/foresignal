package ai.foresignal.raster.sample;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Percentage.withPercentage;

import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.openapi.SpecSchemas;
import ai.foresignal.testsupport.raster.RasterApiSupport;
import ai.foresignal.testsupport.raster.RasterTestSources;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.JsonNode;

/**
 * {@code postProductStats} (prompt 052): statistics and areas above thresholds over a bbox or a GeoJSON polygon
 * on the synthetic lat/lon grid, its spherical area within 1% of the exact box (the {@code lcc} case is in
 * {@link CellAreasTest}), non-ok statuses without statistics, and the geometry limits.
 */
@WebIntegrationTest
@Import(RasterTestSources.class)
class StatsIT extends RasterApiSupport {

    static final SpecSchemas SCHEMAS = new SpecSchemas();

    /** The synthetic grid's box: −100..−94 × 28..34. */
    static double exactKm2() {
        return CellAreasTest.boxKm2(-100, 28, -94, 34);
    }

    @Test
    @DisplayName("a bbox over the whole grid: area within 1%, coverage 8 of 12, statistics and areas above thresholds")
    void bbox() throws Exception {
        commitObservation(T, 0);
        HttpResponse<String> r = post("/v1/products/" + OBS + "/stats",
                "{\"bbox\":\"-100,28,-94,34\",\"thresholds\":[10,25,40]}");
        assertThat(r.statusCode()).isEqualTo(200);
        JsonNode doc = body(r);
        assertThat(SCHEMAS.validateComponent("StatsResponse", doc)).isEmpty();
        assertThat(doc.get("status").asString()).isEqualTo("ok");
        assertThat(doc.get("areaKm2").asDouble()).isCloseTo(exactKm2(), withPercentage(1));
        // Codes 0 (×4, no coverage), 1, 66, 86, 106, 1, 1, 126, 2: −32.5 … 30 dBZ over 8 covered cells.
        assertThat(doc.get("coveragePct").asDouble()).isEqualTo(66.7);
        assertThat(doc.get("min").asDouble()).isEqualTo(-32.5);
        assertThat(doc.get("max").asDouble()).isEqualTo(30.0);
        assertThat(doc.get("validTime").asString()).isEqualTo(T.toString());
        assertThat(doc.get("frameKey").asString()).isNotBlank();
        JsonNode t = doc.get("thresholds");
        assertThat(t).hasSize(3);
        double above10 = t.get(0).get("areaKm2").asDouble();
        double above25 = t.get(1).get("areaKm2").asDouble();
        assertThat(above10).isGreaterThan(above25).isPositive();
        assertThat(t.get(2).get("areaKm2").asDouble()).isZero();
        // Three of twelve cells reach 10 dBZ (rows of equal height but unequal area): about a quarter.
        assertThat(above10 / doc.get("areaKm2").asDouble()).isBetween(0.2, 0.3);
    }

    @Test
    @DisplayName("a GeoJSON polygon over the middle row only")
    void polygon() throws Exception {
        commitObservation(T, 0);
        JsonNode doc = body(post("/v1/products/" + OBS + "/stats", "{\"geometry\":{\"type\":\"Polygon\",\"coordinates\":"
                + "[[[-100,30],[-94,30],[-94,32],[-100,32],[-100,30]]]}}"));
        assertThat(doc.get("status").asString()).isEqualTo("ok");
        assertThat(doc.get("coveragePct").asDouble()).isEqualTo(100.0);
        assertThat(doc.get("areaKm2").asDouble()).isCloseTo(CellAreasTest.boxKm2(-100, 30, -94, 32), withPercentage(1));
        assertThat(doc.get("max").asDouble()).isEqualTo(20.0);
    }

    @Test
    @DisplayName("non-ok statuses carry no statistics: stale, unavailable, no coverage")
    void unknownIsNotClear() throws Exception {
        commitObservation(T, 0);
        clock.setInstant(T.plus(Duration.ofMinutes(10)));
        JsonNode stale = body(post("/v1/products/" + OBS + "/stats", "{\"bbox\":\"-100,28,-94,34\",\"thresholds\":[10]}"));
        assertThat(stale.get("status").asString()).isEqualTo("stale");
        assertThat(stale.get("max").isNull()).isTrue();
        assertThat(stale.get("thresholds")).isEmpty();
        assertThat(stale.get("statusText").asString()).isNotBlank();
        JsonNode outside = body(post("/v1/products/" + OBS + "/stats", "{\"bbox\":\"10,10,11,11\"}"));
        assertThat(outside.get("status").asString()).isIn("no-coverage", "stale");
        JsonNode none = body(post("/v1/products/" + OBS + "/stats", "{\"bbox\":\"-100,28,-94,34\",\"time\":\"-PT5H\"}"));
        assertThat(none.get("status").asString()).isEqualTo("unavailable");
        assertThat(SCHEMAS.validateComponent("StatsResponse", none)).isEmpty();
    }

    @Test
    @DisplayName("exactly one of geometry, place and bbox; an area over 2,000,000 km² and a place without a token are refused")
    void validation() throws Exception {
        assertThat(post("/v1/products/" + OBS + "/stats", "{}").statusCode()).isEqualTo(400);
        assertThat(post("/v1/products/" + OBS + "/stats", "{\"bbox\":\"-100,28,-94,34\",\"place\":\"x\"}").statusCode())
                .isEqualTo(400);
        HttpResponse<String> huge = post("/v1/products/" + OBS + "/stats", "{\"bbox\":\"-125,20,-65,50\"}");
        assertThat(huge.statusCode()).isEqualTo(400);
        assertThat(code(huge)).isEqualTo("VALIDATION_FAILED");
        assertThat(post("/v1/products/" + OBS + "/stats", "{\"place\":\"home\"}").statusCode()).isEqualTo(401);
        assertThat(post("/v1/products/nope/stats", "{\"bbox\":\"-100,28,-94,34\"}").statusCode()).isEqualTo(404);
    }
}
