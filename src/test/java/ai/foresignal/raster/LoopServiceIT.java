package ai.foresignal.raster;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.place.PlaceService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.openapi.SpecSchemas;
import ai.foresignal.testsupport.raster.RasterApiSupport;
import ai.foresignal.testsupport.raster.RasterTestSources;
import ai.foresignal.testsupport.security.TestTokens;
import java.awt.image.BufferedImage;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.JsonNode;

/**
 * {@code getProductLoop} (prompt 045): the box snapped once, its corner coordinates, up to 24
 * expanded {@code /image} frame URLs that each serve an image of the loop's size, the seamless
 * past/future loop with one forecast run, places (token required, {@code UNKNOWN_PLACE}) and the
 * validation errors.
 */
@WebIntegrationTest
@Import(RasterTestSources.class)
class LoopServiceIT extends RasterApiSupport {

    static final SpecSchemas SCHEMAS = new SpecSchemas();

    @Autowired TokenService tokens;
    @Autowired PlaceService places;

    @Test
    @DisplayName("the newest frames, oldest first, over one snapped box; each URL serves the loop's image size")
    void observedLoop() throws Exception {
        for (int k = 4; k >= 0; k--) {
            commitObservation(T.minusSeconds(120L * k), k);
        }
        HttpResponse<String> response = get("/v1/products/loop?products=" + OBS
                + "&bbox=-100.004,28,-94,34&frames=3&width=256");
        assertThat(response.statusCode()).isEqualTo(200);
        JsonNode loop = body(response);
        assertThat(SCHEMAS.validateComponent("ProductLoop", loop)).isEmpty();
        assertThat(loop.get("crs").asString()).isEqualTo("EPSG:3857");
        assertThat(loop.get("bounds").get(0).asDouble()).isEqualTo(-100.01);
        assertThat(loop.get("coordinates").get(0).get(0).asDouble()).isEqualTo(-100.01);
        assertThat(loop.get("coordinates").get(0).get(1).asDouble()).isEqualTo(34.0);
        assertThat(loop.get("coordinates").get(2).get(1).asDouble()).isEqualTo(28.0);
        assertThat(loop.get("width").asInt()).isEqualTo(256);
        int height = loop.get("height").asInt();
        assertThat(height).isGreaterThan(256);
        JsonNode frames = loop.get("frames");
        assertThat(frames.size()).isEqualTo(3);
        assertThat(frames.get(0).get("time").asString()).isEqualTo(T.minusSeconds(240).toString());
        assertThat(frames.get(2).get("time").asString()).isEqualTo(T.toString());
        assertThat(frames.get(2).get("kind").asString()).isEqualTo("observed");
        String url = frames.get(2).get("url").asString();
        assertThat(url).contains("/v1/products/" + OBS + "/image/" + frames.get(2).get("frameKey").asString() + ".png?")
                .contains("bbox=-100.01,28,-94,34").contains("width=256").contains("height=" + height)
                .contains("style=nws-reflectivity@1");
        assertThat(frames.get(2).get("bytes").isNull()).isTrue();
        assertThat(loop.get("legendUrl").asString()).endsWith("/legend?style=nws-reflectivity@1");
        assertThat(loop.get("attribution").get(0).get("text").asString()).isEqualTo("Synthetic test data");
        assertThat(header(response, "Cache-Control")).isEqualTo("public, max-age=60");
        HttpResponse<byte[]> image = getBytes(url);
        assertThat(image.statusCode()).isEqualTo(200);
        assertThat(header(image, "Foresignal-Image-Bounds")).isEqualTo("-100.01,28,-94,34");
        BufferedImage decoded = image(image.body());
        assertThat(decoded.getWidth()).isEqualTo(256);
        assertThat(decoded.getHeight()).isEqualTo(height);
        JsonNode again = body(get("/v1/products/loop?products=" + OBS + "&bbox=-100.004,28,-94,34&frames=3&width=256"));
        assertThat(again.get("frames").get(2).get("bytes").asInt()).isEqualTo(image.body().length);
    }

    @Test
    @DisplayName("past/future joins observed frames up to now with the steps of one forecast run after now")
    void seamless() throws Exception {
        commitObservation(T.minusSeconds(120), 1);
        commitObservation(T, 0);
        commitRun(T.minus(Duration.ofHours(1)), 5);
        JsonNode loop = body(get("/v1/products/loop?past=" + OBS + "&future=" + FCST
                + "&bbox=-100,28,-94,34&frames=4&width=128"));
        assertThat(SCHEMAS.validateComponent("ProductLoop", loop)).isEmpty();
        JsonNode frames = loop.get("frames");
        assertThat(frames.size()).isEqualTo(4);
        assertThat(frames.get(0).get("kind").asString()).isEqualTo("observed");
        assertThat(frames.get(1).get("kind").asString()).isEqualTo("observed");
        assertThat(frames.get(2).get("kind").asString()).isEqualTo("forecast");
        assertThat(frames.get(2).get("time").asString()).isEqualTo(T.plus(Duration.ofHours(1)).toString());
        assertThat(frames.get(3).get("run").asString()).isEqualTo("2026-10-10T13:00:00Z");
        assertThat(frames.get(3).get("url").asString()).contains("run=2026-10-10T13:00:00Z");
        assertThat(loop.get("runs").get(FCST).asString()).isEqualTo("2026-10-10T13:00:00Z");
        assertThat(loop.get("products").size()).isEqualTo(2);
    }

    @Test
    @DisplayName("a place needs a token, resolves to its box, and an unknown place is 404 UNKNOWN_PLACE")
    void places() throws Exception {
        commitObservation(T, 0);
        places.create(json.readTree("{\"id\":\"austin\",\"name\":\"Austin\",\"shape\":\"point\",\"geometry\":"
                + "{\"type\":\"Point\",\"coordinates\":[-97.74,30.27]},\"radiusKm\":50}"));
        String path = "/v1/products/loop?products=" + OBS + "&place=austin&frames=1";
        assertThat(get(path).statusCode()).isEqualTo(401);
        String reader = TestTokens.bearer(TestTokens.create(tokens, "loop-reader", Scope.READ));
        HttpResponse<String> response = get(path, "Authorization", reader);
        assertThat(response.statusCode()).isEqualTo(200);
        JsonNode loop = body(response);
        assertThat(loop.get("bounds").get(0).asDouble()).isBetween(-98.3, -98.2);
        assertThat(loop.get("bounds").get(1).asDouble()).isBetween(29.8, 29.83);
        assertThat(header(response, "Cache-Control")).as("a place loop is never publicly cacheable")
                .startsWith("private");
        HttpResponse<String> unknown = get("/v1/products/loop?products=" + OBS + "&place=nowhere", "Authorization",
                reader);
        assertThat(unknown.statusCode()).isEqualTo(404);
        assertThat(code(unknown)).isEqualTo("UNKNOWN_PLACE");
    }

    @Test
    @DisplayName("validation: bbox xor place, frames ≤ 24, width ≤ 1024, EPSG:3857 only, one or two products")
    void validation() throws Exception {
        String box = "&bbox=-100,28,-94,34";
        assertThat(code(get("/v1/products/loop?products=" + OBS))).isEqualTo("VALIDATION_FAILED");
        assertThat(code(get("/v1/products/loop?products=" + OBS + box + "&place=austin"))).isEqualTo(
                "VALIDATION_FAILED");
        assertThat(code(get("/v1/products/loop?products=" + OBS + box + "&frames=25"))).isEqualTo("VALIDATION_FAILED");
        assertThat(code(get("/v1/products/loop?products=" + OBS + box + "&width=2000"))).isEqualTo("VALIDATION_FAILED");
        assertThat(code(get("/v1/products/loop?products=" + OBS + box + "&crs=EPSG:4326"))).isEqualTo(
                "VALIDATION_FAILED");
        assertThat(code(get("/v1/products/loop?products=a,b,c" + box))).isEqualTo("VALIDATION_FAILED");
        assertThat(code(get("/v1/products/loop?past=" + OBS + box))).isEqualTo("VALIDATION_FAILED");
        assertThat(code(get("/v1/products/loop?past=" + FCST + "&future=" + OBS + box))).isEqualTo(
                "VALIDATION_FAILED");
        assertThat(code(get("/v1/products/loop?products=nope" + box))).isEqualTo("UNKNOWN_PRODUCT");
        assertThat(code(get("/v1/products/loop?products=" + OBS + box + "&style=nope"))).isEqualTo("UNKNOWN_STYLE");
        assertThat(code(get("/v1/products/loop?products=gibs-goes-east-dust" + box))).isEqualTo("VALIDATION_FAILED");
    }
}
