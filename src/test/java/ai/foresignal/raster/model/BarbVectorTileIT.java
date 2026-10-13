package ai.foresignal.raster.model;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.raster.FrameKeys;
import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.source.adapters.gfs.GfsAdapter;
import ai.foresignal.source.adapters.hrrr.HrrrAdapter;
import ai.foresignal.source.spi.DecodedBands;
import ai.foresignal.source.spi.FrameInput;
import ai.foresignal.source.spi.GridDefinition;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.raster.ModelFixtures;
import ai.foresignal.testsupport.raster.RasterApiSupport;
import ai.foresignal.testsupport.raster.RasterTestSources;
import ai.foresignal.testsupport.tiles.MvtDecoder;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Barb vector tiles (prompt 051) from synthetic wind fields: the MVT decodes to layer {@code wind}
 * with an 8 × 8 lattice at 32 px spacing, and {@code speedMs}, {@code fromDeg} and {@code barbKt} are
 * right for a westerly (u = 10, v = 0: from 270°, 19.4 kt → 20) and a northerly (u = 0, v = −5: from
 * 0°, 9.7 kt → 10); a Lambert (HRRR) frame draws only where it has data; refusals and caching follow
 * the raster tiles.
 */
@WebIntegrationTest
@Import(RasterTestSources.class)
class BarbVectorTileIT extends RasterApiSupport {

    static final Instant RUN = Instant.parse("2026-10-12T12:00:00Z");

    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void setUp() throws Exception {
        ModelFixtures.clean(jdbc, paths);
        pipeline.commit(GfsAdapter.ID, ModelFixtures.gfsStep(RUN, 3, ModelFixtures.constant(10),
                ModelFixtures.constant(0)));
        pipeline.commit(GfsAdapter.ID, ModelFixtures.gfsStep(RUN, 6, ModelFixtures.constant(0),
                ModelFixtures.constant(-5)));
        pipeline.completeRun(GfsAdapter.ID, GfsAdapter.WIND, RUN);
        clock.setInstant(Instant.parse("2026-10-12T15:00:00Z"));
    }

    @AfterEach
    void tearDown() {
        ModelFixtures.clean(jdbc, paths);
    }

    private RasterFrameRow frame(int step) {
        return frames.find(GfsAdapter.WIND, FrameKeys.forecastFrameId(RUN, step)).orElseThrow();
    }

    private String path(String product, RasterFrameRow f, String zxy) {
        return "/v1/products/" + product + "/vectors/" + f.frameKey() + "/" + zxy + ".mvt?run=" + FrameKeys.runIso(RUN);
    }

    @Test
    @DisplayName("a westerly: 64 points on the 32 px lattice, speedMs 10.0, fromDeg 270, barbKt 20")
    void westerly() throws Exception {
        HttpResponse<byte[]> response = getBytes(path("gfs", frame(3), "2/1/1"));
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(header(response, "Content-Type")).startsWith("application/vnd.mapbox-vector-tile");
        assertThat(header(response, "Cache-Control")).contains("immutable");
        MvtDecoder.Layer layer = MvtDecoder.singleLayer(response.body());
        assertThat(layer.name()).isEqualTo("wind");
        assertThat(layer.extent()).isEqualTo(4096);
        assertThat(layer.features()).hasSize(64);
        Set<Integer> xs = layer.features().stream().map(f -> f.firstPoint()[0]).collect(Collectors.toSet());
        Set<Integer> ys = layer.features().stream().map(f -> f.firstPoint()[1]).collect(Collectors.toSet());
        Set<Integer> lattice = Arrays.stream(new int[] {256, 768, 1280, 1792, 2304, 2816, 3328, 3840}).boxed()
                .collect(Collectors.toSet());
        assertThat(xs).isEqualTo(lattice);
        assertThat(ys).isEqualTo(lattice);
        for (MvtDecoder.Feature f : layer.features()) {
            assertThat(f.type()).isEqualTo(MvtDecoder.GeomType.POINT);
            assertThat(((Number) f.get("speedMs")).doubleValue()).isEqualTo(10.0);
            assertThat(((Number) f.get("fromDeg")).intValue()).isEqualTo(270);
            assertThat(((Number) f.get("barbKt")).intValue()).isEqualTo(20);
        }
        HttpResponse<byte[]> again = getBytes(path("gfs", frame(3), "2/1/1"), "If-None-Match", header(response, "ETag"));
        assertThat(again.statusCode()).isEqualTo(304);
    }

    @Test
    @DisplayName("a northerly: fromDeg 0, barbKt 10; the latest alias gets the short lifetime")
    void northerly() throws Exception {
        MvtDecoder.Layer layer = MvtDecoder.singleLayer(getBytes(path("gfs", frame(6), "3/2/2")).body());
        assertThat(layer.features()).hasSize(64).allSatisfy(f -> {
            assertThat(((Number) f.get("speedMs")).doubleValue()).isEqualTo(5.0);
            assertThat(((Number) f.get("fromDeg")).intValue()).isZero();
            assertThat(((Number) f.get("barbKt")).intValue()).isEqualTo(10);
        });
        HttpResponse<byte[]> latest = getBytes("/v1/products/gfs/vectors/latest/0/0/0.mvt");
        assertThat(latest.statusCode()).isEqualTo(200);
        assertThat(header(latest, "Cache-Control")).doesNotContain("immutable");
        assertThat(header(latest, "Foresignal-Frame-Time")).isEqualTo(frame(3).validTime().toString());
    }

    @Test
    @DisplayName("an HRRR (Lambert) frame draws points only over its grid; elsewhere the tile is empty")
    void lambertFrame() throws Exception {
        Map<String, Object> params = Map.of("lat1", 38.5, "lat2", 38.5, "lov", -97.5, "radius", 6_371_229.0,
                "la1", 37.0, "lo1", -100.0, "dx", 3000.0, "dy", 3000.0);
        GridDefinition grid = new GridDefinition(GridDefinition.LCC, 160, 120, -101, 36.5, -94, 41, params);
        float[] u = new float[160 * 120];
        float[] v = new float[160 * 120];
        Arrays.fill(u, -3f);
        Arrays.fill(v, -4f);
        pipeline.commit(HrrrAdapter.ID, FrameInput.builder(HrrrAdapter.WIND).forecast(RUN, 1).grid(grid)
                .bands(DecodedBands.physical(u, v)).build());
        pipeline.completeRun(HrrrAdapter.ID, HrrrAdapter.WIND, RUN);
        RasterFrameRow f = frames.find(HrrrAdapter.WIND, FrameKeys.forecastFrameId(RUN, 1)).orElseThrow();
        List<MvtDecoder.Feature> inside = MvtDecoder.singleLayer(getBytes(path("hrrr", f, "6/14/24")).body()).features();
        assertThat(inside).isNotEmpty().hasSizeLessThan(64).allSatisfy(feature -> {
            assertThat(((Number) feature.get("speedMs")).doubleValue()).isEqualTo(5.0);
            assertThat(((Number) feature.get("fromDeg")).intValue()).as("from the north-east").isEqualTo(37);
        });
        HttpResponse<byte[]> outside = getBytes(path("hrrr", f, "6/0/0"));
        assertThat(outside.statusCode()).isEqualTo(200);
        assertThat(outside.body()).isEmpty();
    }

    @Test
    @DisplayName("products without wind, zooms above the native max and unknown frames are refused")
    void refusals() throws Exception {
        HttpResponse<String> gust = get("/v1/products/gfs-gust/vectors/latest/0/0/0.mvt");
        assertThat(gust.statusCode()).isEqualTo(400);
        assertThat(code(gust)).isEqualTo("VALIDATION_FAILED");
        HttpResponse<String> deep = get(path("gfs", frame(3), "5/1/1"));
        assertThat(deep.statusCode()).isEqualTo(400);
        assertThat(code(deep)).isEqualTo("TILE_OUT_OF_RANGE");
        HttpResponse<String> unknown = get("/v1/products/gfs/vectors/20991231T000000Z/0/0/0.mvt?run=latest");
        assertThat(unknown.statusCode()).isEqualTo(404);
        assertThat(code(unknown)).isEqualTo("UNKNOWN_FRAME");
    }
}
