package ai.foresignal.raster;

import static org.assertj.core.api.Assertions.assertThat;

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
 * {@code getProductTileJson} (prompt 045): TileJSON 3.0.0 whose template is concrete and
 * content-addressed (the alias resolved when the document is generated), the native zoom range,
 * bounds, center, {@code tileSize: 256} and the {@code @2x} variant; the document itself is short-lived
 * unless its own URL is content-addressed; the template it hands out serves a tile.
 */
@WebIntegrationTest
@Import(RasterTestSources.class)
class TileJsonIT extends RasterApiSupport {

    static final SpecSchemas SCHEMAS = new SpecSchemas();

    @Test
    @DisplayName("latest resolves to a concrete content-addressed template; the document has a short max-age")
    void latest() throws Exception {
        commitObservation(T.minusSeconds(120), 1);
        RasterFrameRow newest = commitObservation(T, 0);
        HttpResponse<String> response = get("/v1/products/" + OBS + "/tilejson");
        assertThat(response.statusCode()).isEqualTo(200);
        JsonNode doc = body(response);
        assertThat(SCHEMAS.validateComponent("TileJson", doc)).isEmpty();
        assertThat(doc.get("tilejson").asString()).isEqualTo("3.0.0");
        String template = doc.get("tiles").get(0).asString();
        assertThat(template).isEqualTo("http://localhost:" + port + "/v1/products/" + OBS + "/tiles/"
                + newest.frameKey() + "/{z}/{x}/{y}.png?style=nws-reflectivity@1");
        assertThat(template).doesNotContain("latest");
        assertThat(doc.get("foresignal:tiles2x").get(0).asString()).contains("/{z}/{x}/{y}@2x.png?style=nws-reflectivity@1");
        assertThat(doc.get("foresignal:frameKey").asString()).isEqualTo(newest.frameKey());
        assertThat(doc.get("foresignal:style").asString()).isEqualTo("nws-reflectivity@1");
        assertThat(doc.get("minzoom").asInt()).isZero();
        assertThat(doc.get("maxzoom").asInt()).isEqualTo(8);
        assertThat(doc.get("tileSize").asInt()).isEqualTo(256);
        assertThat(doc.get("bounds").get(0).asDouble()).isEqualTo(-100.0);
        assertThat(doc.get("bounds").get(3).asDouble()).isEqualTo(34.0);
        assertThat(doc.get("center").get(0).asDouble()).isEqualTo(-97.0);
        assertThat(doc.get("center").get(1).asDouble()).isEqualTo(31.0);
        assertThat(doc.get("attribution").asString()).isEqualTo("Synthetic test data");
        assertThat(header(response, "Cache-Control")).isEqualTo("public, max-age=60");
        assertThat(header(response, "Foresignal-Frame-Time")).isEqualTo(T.toString());
        assertThat(get("/v1/products/" + OBS + "/tilejson", "If-None-Match", header(response, "ETag")).statusCode())
                .isEqualTo(304);
        HttpResponse<byte[]> tile = getBytes(template.replace("{z}", "4").replace("{x}", "3").replace("{y}", "6"));
        assertThat(tile.statusCode()).isEqualTo(200);
        assertThat(header(tile, "Cache-Control")).isEqualTo("public, max-age=31536000, immutable");
    }

    @Test
    @DisplayName("a content-addressed frame key with a versioned style makes the document immutable")
    void contentAddressed() throws Exception {
        RasterFrameRow frame = commitObservation(T, 0);
        HttpResponse<String> response = get("/v1/products/" + OBS + "/tilejson?frameKey=" + frame.frameKey()
                + "&style=nws-reflectivity@1");
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(header(response, "Cache-Control")).isEqualTo("public, max-age=31536000, immutable");
        assertThat(get("/v1/products/" + OBS + "/tilejson?frameKey=" + frame.frameKey() + "&style=nws-reflectivity")
                .headers().firstValue("Cache-Control").orElseThrow()).isEqualTo("public, max-age=60");
    }

    @Test
    @DisplayName("a forecast template pins its run; an unknown style is 400; an external product is 400")
    void forecastAndErrors() throws Exception {
        commitRun(T.minus(Duration.ofHours(1)), 4);
        JsonNode doc = body(get("/v1/products/" + FCST + "/tilejson?run=latest"));
        assertThat(doc.get("tiles").get(0).asString()).contains("style=wind-speed@1&run=2026-10-10T13:00:00Z");
        assertThat(doc.get("foresignal:run").asString()).isEqualTo("2026-10-10T13:00:00Z");
        assertThat(doc.get("maxzoom").asInt()).isEqualTo(6);
        assertThat(code(get("/v1/products/" + OBS + "/tilejson?style=rainbow"))).isEqualTo("UNKNOWN_STYLE");
        assertThat(code(get("/v1/products/nowcoast-radar/tilejson"))).isEqualTo("VALIDATION_FAILED");
        assertThat(code(get("/v1/products/" + OBS + "/tilejson"))).as("no frame yet").isEqualTo("UNKNOWN_FRAME");
    }
}
