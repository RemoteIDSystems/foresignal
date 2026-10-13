package ai.foresignal.raster.model;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.raster.FrameKeys;
import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.source.adapters.gfs.GfsAdapter;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.openapi.SpecSchemas;
import ai.foresignal.testsupport.raster.ModelFixtures;
import ai.foresignal.testsupport.raster.RasterApiSupport;
import ai.foresignal.testsupport.raster.RasterTestSources;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.JsonNode;

/**
 * The wind field endpoints (prompt 051): the God's Eye View manifest (validated against the spec's
 * {@code FieldManifest}), the planar Float32 data sent with {@code Content-Encoding: gzip} (521,280
 * bytes raw at 1°, 8,305,920 at 0.25°), bbox snapping and the wrap across 0°, 406 when gzip is
 * refused, and products without fields.
 *
 * <p>The synthetic {@code gfs} step has {@code u = column} (= longitude, 0..359) and
 * {@code v = 90 − row} (= latitude), so every value says where it came from.
 */
@WebIntegrationTest
@Import(RasterTestSources.class)
class FieldEndpointIT extends RasterApiSupport {

    static final SpecSchemas SCHEMAS = new SpecSchemas();
    static final Instant RUN = Instant.parse("2026-10-12T12:00:00Z");

    @Autowired JdbcTemplate jdbc;

    RasterFrameRow frame;

    @BeforeEach
    void setUp() throws Exception {
        ModelFixtures.clean(jdbc, paths);
        pipeline.commit(GfsAdapter.ID, ModelFixtures.gfsStep(RUN, 6, (c, r) -> c, (c, r) -> 90 - r));
        pipeline.completeRun(GfsAdapter.ID, GfsAdapter.WIND, RUN);
        frame = frames.find(GfsAdapter.WIND, "2026101212Z-f006").orElseThrow();
        clock.setInstant(Instant.parse("2026-10-12T17:00:00Z"));
    }

    @AfterEach
    void tearDown() {
        ModelFixtures.clean(jdbc, paths);
    }

    private String pinned() {
        return "?run=" + FrameKeys.runIso(RUN);
    }

    private static float[] floats(byte[] raw) {
        float[] out = new float[raw.length / 4];
        ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().get(out);
        return out;
    }

    @Test
    @DisplayName("the manifest has the God's Eye View shape plus components, byteLength, sha256 and a pinned dataUrl")
    void manifest() throws Exception {
        HttpResponse<String> response = get("/v1/products/gfs/fields/" + frame.frameKey() + ".json" + pinned());
        assertThat(response.statusCode()).isEqualTo(200);
        JsonNode m = body(response);
        assertThat(SCHEMAS.validateComponent("FieldManifest", m)).isEmpty();
        assertThat(m.get("schemaVersion").asInt()).isEqualTo(1);
        assertThat(m.get("model").asString()).isEqualTo("gfs");
        assertThat(m.get("level").asString()).isEqualTo("10 m above ground");
        assertThat(m.get("units").asString()).isEqualTo("m/s");
        JsonNode cycle = m.get("cycle");
        assertThat(cycle.get("date").asString()).isEqualTo("20261012");
        assertThat(cycle.get("hour").asInt()).isEqualTo(12);
        assertThat(cycle.get("forecastHour").asInt()).isEqualTo(6);
        assertThat(cycle.get("validIso").asString()).isEqualTo("2026-10-12T18:00:00Z");
        JsonNode grid = m.get("grid");
        assertThat(grid.get("nx").asInt()).isEqualTo(360);
        assertThat(grid.get("ny").asInt()).isEqualTo(181);
        assertThat(grid.get("lo1").asDouble()).isZero();
        assertThat(grid.get("la1").asDouble()).isEqualTo(90.0);
        assertThat(grid.get("dx").asDouble()).isEqualTo(1.0);
        assertThat(m.get("byteLength").asInt()).isEqualTo(521_280);
        assertThat(m.get("components").get(1).get("offset").asInt()).isEqualTo(360 * 181 * 4);
        assertThat(m.get("unavailable").asBoolean()).isFalse();
        assertThat(m.get("attribution").get("licence").asString()).isEqualTo("US-PD");
        assertThat(m.get("dataUrl").asString()).endsWith("/v1/products/gfs/fields/" + frame.frameKey()
                + ".f32?run=2026-10-12T12:00:00Z&res=1");
        assertThat(header(response, "Cache-Control")).contains("immutable");
    }

    @Test
    @DisplayName("the 1° data is 521,280 bytes of planar little-endian u then v, gzip-encoded, matching sha256")
    void data() throws Exception {
        JsonNode m = body(get("/v1/products/gfs/fields/" + frame.frameKey() + ".json" + pinned()));
        HttpResponse<byte[]> response = getBytes(m.get("dataUrl").asString(), "Accept-Encoding", "gzip");
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(header(response, "Content-Encoding")).isEqualTo("gzip");
        assertThat(header(response, "Content-Type")).startsWith("application/octet-stream");
        assertThat(header(response, "Cache-Control")).contains("immutable");
        byte[] raw = FieldStore.gunzip(response.body());
        assertThat(raw).hasSize(521_280);
        assertThat(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(raw)))
                .isEqualTo(m.get("sha256").asString());
        float[] values = floats(raw);
        int cells = 360 * 181;
        assertThat(values[0]).as("u at lon 0, lat 90").isEqualTo(0f);
        assertThat(values[359]).as("u at lon 359").isEqualTo(359f);
        assertThat(values[cells]).as("v at lat 90").isEqualTo(90f);
        assertThat(values[cells + 180 * 360]).as("v at lat −90").isEqualTo(-90f);

        HttpResponse<byte[]> noHeader = getBytes(m.get("dataUrl").asString());
        assertThat(noHeader.statusCode()).as("no Accept-Encoding accepts anything").isEqualTo(200);
        HttpResponse<byte[]> conditional = getBytes(m.get("dataUrl").asString(), "If-None-Match",
                header(response, "ETag"));
        assertThat(conditional.statusCode()).isEqualTo(304);
    }

    @Test
    @DisplayName("0.25° is 1440 × 721 (8,305,920 bytes raw)")
    void quarterDegree() throws Exception {
        JsonNode m = body(get("/v1/products/gfs/fields/latest.json?res=0.25"));
        assertThat(m.get("grid").get("nx").asInt()).isEqualTo(1440);
        assertThat(m.get("byteLength").asInt()).isEqualTo(8_305_920);
        assertThat(m.get("dataUrl").asString()).contains(frame.frameKey()).contains("&res=0.25")
                .contains("run=2026-10-12T12:00:00Z");
        byte[] raw = FieldStore.gunzip(getBytes(m.get("dataUrl").asString()).body());
        assertThat(raw).hasSize(8_305_920);
        assertThat(floats(raw)[4]).as("column 4 of 0.25° is longitude 1").isEqualTo(1f);
    }

    @Test
    @DisplayName("a bbox is snapped outward to grid points and wraps across 0°")
    void bboxSeam() throws Exception {
        HttpResponse<String> response = get("/v1/products/gfs/fields/" + frame.frameKey() + ".json" + pinned()
                + "&bbox=-10.5,30.2,10.3,40.7");
        JsonNode m = body(response);
        assertThat(SCHEMAS.validateComponent("FieldManifest", m)).isEmpty();
        JsonNode grid = m.get("grid");
        assertThat(grid.get("nx").asInt()).isEqualTo(23);
        assertThat(grid.get("ny").asInt()).isEqualTo(12);
        assertThat(grid.get("lo1").asDouble()).isEqualTo(349.0);
        assertThat(grid.get("la1").asDouble()).isEqualTo(41.0);
        assertThat(m.get("byteLength").asInt()).isEqualTo(23 * 12 * 8);
        assertThat(m.get("dataUrl").asString()).endsWith("&bbox=-11,30,11,41");
        byte[] raw = FieldStore.gunzip(getBytes(m.get("dataUrl").asString()).body());
        assertThat(raw).hasSize(23 * 12 * 8);
        assertThat(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(raw)))
                .isEqualTo(m.get("sha256").asString());
        float[] values = floats(raw);
        assertThat(values[0]).as("first column is longitude 349").isEqualTo(349f);
        assertThat(values[10]).isEqualTo(359f);
        assertThat(values[11]).as("then the seam: longitude 0").isEqualTo(0f);
        assertThat(values[22]).isEqualTo(11f);
        assertThat(values[23 * 12]).as("v of the first row is latitude 41").isEqualTo(41f);
        assertThat(values[23 * 12 + 11 * 23]).as("last row is latitude 30").isEqualTo(30f);
        JsonNode again = body(get("/v1/products/gfs/fields/" + frame.frameKey() + ".json" + pinned()
                + "&bbox=-11,30,11,41"));
        assertThat(again.get("sha256").asString()).as("the snapped box is a fixed point").isEqualTo(m.get("sha256")
                .asString());
    }

    @Test
    @DisplayName("406 only when the client refuses gzip; 400 for a product without fields, a bad res or bbox")
    void refusals() throws Exception {
        String data = "/v1/products/gfs/fields/" + frame.frameKey() + ".f32" + pinned();
        HttpResponse<String> identity = get(data, "Accept-Encoding", "identity");
        assertThat(identity.statusCode()).isEqualTo(406);
        assertThat(code(identity)).isEqualTo("NOT_ACCEPTABLE");
        assertThat(get(data, "Accept-Encoding", "gzip;q=0, *;q=1").statusCode()).isEqualTo(406);
        assertThat(getBytes(data, "Accept-Encoding", "br, *").statusCode()).isEqualTo(200);
        for (String path : new String[] {"/v1/products/hrrr/fields/latest.json", "/v1/products/gfs-gust/fields/latest.json",
            "/v1/products/gfs/fields/latest.json?res=2", "/v1/products/gfs/fields/latest.json?bbox=1,2,3",
            "/v1/products/gibs-goes-east-geocolor/fields/latest.json"}) {
            HttpResponse<String> response = get(path);
            assertThat(response.statusCode()).as(path).isEqualTo(400);
            assertThat(code(response)).as(path).isEqualTo("VALIDATION_FAILED");
        }
        assertThat(get("/v1/products/nope/fields/latest.json").statusCode()).isEqualTo(404);
    }
}
