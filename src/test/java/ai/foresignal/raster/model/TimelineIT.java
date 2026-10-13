package ai.foresignal.raster.model;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.adapters.hrrr.HrrrAdapter;
import ai.foresignal.source.adapters.mrms.MrmsAdapter;
import ai.foresignal.source.spi.DecodedBands;
import ai.foresignal.source.spi.FrameInput;
import ai.foresignal.source.spi.GridDefinition;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.openapi.SpecSchemas;
import ai.foresignal.testsupport.raster.ModelFixtures;
import ai.foresignal.testsupport.raster.RasterApiSupport;
import ai.foresignal.testsupport.raster.RasterFixtures;
import ai.foresignal.testsupport.raster.RasterTestSources;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.JsonNode;

/**
 * The cross-product timeline (prompt 051): seamless MRMS past plus HRRR future, one run per forecast
 * product (never mixed), {@code nowIndex}, ticks chosen within {@code maxGap}, synthesized external
 * ticks marked {@code unverified}, and the 500-tick cap. Validated against the spec's
 * {@code ProductTimeline}.
 */
@WebIntegrationTest
@Import(RasterTestSources.class)
class TimelineIT extends RasterApiSupport {

    static final SpecSchemas SCHEMAS = new SpecSchemas();
    static final Instant NOW = Instant.parse("2026-10-12T14:05:00Z");
    static final Instant R1 = Instant.parse("2026-10-12T06:00:00Z");
    static final Instant R2 = Instant.parse("2026-10-12T12:00:00Z");
    static final Instant R3 = Instant.parse("2026-10-12T13:00:00Z");
    static final GridDefinition LCC = new GridDefinition(GridDefinition.LCC, 4, 3, -101, 36.5, -94, 41, Map.of(
            "lat1", 38.5, "lat2", 38.5, "lov", -97.5, "radius", 6_371_229.0, "la1", 37.0, "lo1", -100.0, "dx", 3000.0,
            "dy", 3000.0));

    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void setUp() throws Exception {
        clean();
        for (String t : List.of("2026-10-12T13:40:00Z", "2026-10-12T13:50:00Z", "2026-10-12T14:00:00Z")) {
            pipeline.commit(MrmsAdapter.ID, FrameInput.builder(MrmsAdapter.REFLECTIVITY).validTime(Instant.parse(t))
                    .grid(RasterFixtures.GRID).bands(DecodedBands.codes(RasterFixtures.codes(0))).build());
        }
        commitRefc(R1, 1, 4);
        commitRefc(R2, 1, 4);
        pipeline.completeRun(HrrrAdapter.ID, HrrrAdapter.REFC, R1);
        pipeline.completeRun(HrrrAdapter.ID, HrrrAdapter.REFC, R2);
        commitRefc(R3, 1, 2);
        clock.setInstant(NOW);
    }

    @AfterEach
    void tearDown() {
        clean();
    }

    private void clean() {
        ModelFixtures.clean(jdbc, paths);
        jdbc.update("DELETE FROM raster_frame WHERE product_id = ?", MrmsAdapter.REFLECTIVITY);
        RasterFixtures.deleteTree(paths.productFrames(MrmsAdapter.REFLECTIVITY));
        RasterFixtures.deleteTree(paths.productFrames(HrrrAdapter.REFC));
    }

    private void commitRefc(Instant run, int from, int to) throws Exception {
        for (int step = from; step <= to; step++) {
            float[] dbz = new float[12];
            Arrays.fill(dbz, 20 + step);
            pipeline.commit(HrrrAdapter.ID, FrameInput.builder(HrrrAdapter.REFC).forecast(run, step).grid(LCC)
                    .bands(DecodedBands.physical(dbz)).build());
        }
    }

    private static List<String> times(JsonNode doc) {
        List<String> out = new ArrayList<>();
        doc.get("ticks").forEach(t -> out.add(t.get("time").asString()));
        return out;
    }

    @Test
    @DisplayName("seamless: observed MRMS ticks up to now, then the newest complete HRRR run's steps after now")
    void seamless() throws Exception {
        HttpResponse<String> response = get("/v1/products/timeline?mode=seamless&past=mrms-reflectivity&future=hrrr-refc");
        assertThat(response.statusCode()).isEqualTo(200);
        JsonNode doc = body(response);
        assertThat(SCHEMAS.validateComponent("ProductTimeline", doc)).isEmpty();
        assertThat(doc.get("mode").asString()).isEqualTo("seamless");
        assertThat(times(doc)).containsExactly("2026-10-12T13:40:00Z", "2026-10-12T13:50:00Z", "2026-10-12T14:00:00Z",
                "2026-10-12T15:00:00Z", "2026-10-12T16:00:00Z");
        assertThat(doc.get("nowIndex").asInt()).isEqualTo(2);
        assertThat(doc.get("runs").get("hrrr-refc").asString()).as("R2: complete; R3 is still in progress")
                .isEqualTo("2026-10-12T12:00:00Z");
        JsonNode ticks = doc.get("ticks");
        for (int i = 0; i < 3; i++) {
            assertThat(ticks.get(i).get("kind").asString()).isEqualTo("observed");
            assertThat(ticks.get(i).get("frames")).hasSize(1);
            JsonNode f = ticks.get(i).get("frames").get(0);
            assertThat(f.get("productId").asString()).isEqualTo("mrms-reflectivity");
            assertThat(f.get("availability").asString()).isEqualTo("available");
            assertThat(f.get("tiles").asString()).contains("/v1/products/mrms-reflectivity/tiles/")
                    .contains(f.get("frameKey").asString()).contains("{z}/{x}/{y}.png?style=nws-reflectivity@1");
        }
        for (int i = 3; i < 5; i++) {
            assertThat(ticks.get(i).get("kind").asString()).isEqualTo("forecast");
            JsonNode f = ticks.get(i).get("frames").get(0);
            assertThat(f.get("productId").asString()).isEqualTo("hrrr-refc");
            assertThat(f.get("run").asString()).isEqualTo("2026-10-12T12:00:00Z");
            assertThat(f.get("tiles").asString()).contains("run=2026-10-12T12:00:00Z");
        }
        assertThat(ticks.get(3).get("frames").get(0).get("step").asInt()).isEqualTo(3);
    }

    @Test
    @DisplayName("products mode: the union of frame times, each product's frame within maxGap, never mixed runs")
    void productsMode() throws Exception {
        JsonNode doc = body(get("/v1/products/timeline?products=mrms-reflectivity,hrrr-refc"));
        assertThat(SCHEMAS.validateComponent("ProductTimeline", doc)).isEmpty();
        assertThat(times(doc)).containsExactly("2026-10-12T13:00:00Z", "2026-10-12T13:40:00Z", "2026-10-12T13:50:00Z",
                "2026-10-12T14:00:00Z", "2026-10-12T15:00:00Z", "2026-10-12T16:00:00Z");
        JsonNode ticks = doc.get("ticks");
        assertThat(ticks.get(0).get("kind").asString()).as("only HRRR at 13:00").isEqualTo("forecast");
        assertThat(ticks.get(1).get("frames")).hasSize(2);
        assertThat(ticks.get(1).get("kind").asString()).isEqualTo("observed");
        assertThat(ticks.get(1).get("frames").get(1).get("time").asString()).as("HRRR 13:00 is within 1 h of 13:40")
                .isEqualTo("2026-10-12T13:00:00Z");
        assertThat(ticks.get(4).get("frames")).as("MRMS 14:00 is beyond 6 min of 15:00").hasSize(1);
        doc.get("ticks").forEach(t -> t.get("frames").forEach(f -> {
            if (f.get("productId").asString().equals("hrrr-refc")) {
                assertThat(f.get("run").asString()).isEqualTo("2026-10-12T12:00:00Z");
            }
        }));
        assertThat(doc.get("nowIndex").asInt()).isEqualTo(3);
        assertThat(doc.get("from").asString()).isEqualTo(NOW.minus(Duration.ofHours(6)).toString());
        assertThat(doc.get("to").asString()).isEqualTo(NOW.plus(Duration.ofHours(48)).toString());
    }

    @Test
    @DisplayName("external catalog entries get synthesized ticks every period up to now − defaultLag, unverified")
    void externalTicks() throws Exception {
        JsonNode doc = body(get("/v1/products/timeline?products=gibs-goes-east-geocolor&from=2026-10-12T13:00:00Z"
                + "&to=2026-10-12T14:05:00Z"));
        assertThat(SCHEMAS.validateComponent("ProductTimeline", doc)).isEmpty();
        assertThat(times(doc)).as("PT10M up to 14:05 − 40 min").containsExactly("2026-10-12T13:00:00Z",
                "2026-10-12T13:10:00Z", "2026-10-12T13:20:00Z");
        JsonNode f = doc.get("ticks").get(0).get("frames").get(0);
        assertThat(f.get("availability").asString()).isEqualTo("unverified");
        assertThat(f.get("frameKey").isNull()).isTrue();
        assertThat(f.get("tiles").asString()).contains("/default/2026-10-12T13:00:00Z/GoogleMapsCompatible_Level7/");
    }

    @Test
    @DisplayName("more than 500 ticks, and bad parameter combinations, are 400 VALIDATION_FAILED")
    void refusals() throws Exception {
        HttpResponse<String> tooMany = get("/v1/products/timeline?products=gibs-goes-east-geocolor"
                + "&from=2026-10-08T00:00:00Z&to=2026-10-12T14:05:00Z");
        assertThat(tooMany.statusCode()).isEqualTo(400);
        assertThat(code(tooMany)).isEqualTo("VALIDATION_FAILED");
        for (String query : List.of("", "?mode=seamless&past=hrrr-refc&future=hrrr-refc",
                "?mode=seamless&past=mrms-reflectivity&future=mrms-reflectivity", "?mode=loop&products=hrrr-refc",
                "?products=hrrr-refc&past=mrms-reflectivity", "?products=hrrr-refc&from=2026-10-13T00:00:00Z"
                        + "&to=2026-10-12T00:00:00Z", "?mode=seamless&past=mrms-reflectivity")) {
            HttpResponse<String> response = get("/v1/products/timeline" + query);
            assertThat(response.statusCode()).as(query).isEqualTo(400);
            assertThat(code(response)).as(query).isEqualTo("VALIDATION_FAILED");
        }
        assertThat(get("/v1/products/timeline?products=nope").statusCode()).isEqualTo(404);
    }
}
