package ai.foresignal.raster;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.place.PlaceService;
import ai.foresignal.raster.render.RenderQueue;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.raster.RasterApiSupport;
import ai.foresignal.testsupport.raster.RasterTestSources;
import ai.foresignal.testsupport.security.TestTokens;
import java.awt.image.BufferedImage;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/**
 * {@code getStaticMap} (prompt 055): parameters, the size caps, {@code Foresignal-Image-Bounds}, the cache
 * rule (immutable only for pinned frames, otherwise a short max-age with {@code Foresignal-Frame-Time} and an
 * ETag checked before rendering), the errors, and {@code 503 RENDER_BUSY} when the render queue is saturated.
 * Shares the prompt 042 raster context; frames are committed straight through the pipeline.
 */
@WebIntegrationTest
@Import(RasterTestSources.class)
class StaticMapEndpointIT extends RasterApiSupport {

    @Autowired TokenService tokens;
    @Autowired PlaceService places;
    @Autowired RenderQueue queue;

    static final String PATH = "/v1/maps/static";

    @Test
    @DisplayName("a center map: PNG at the requested width × 0.75, bounds header, short max-age, frame time")
    void centerMap() throws Exception {
        commitObservation(T, 0);
        HttpResponse<byte[]> response = getBytes(PATH + "?products=" + OBS + "&center=-97,31&radiusKm=200&width=640");
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(header(response, "Content-Type")).isEqualTo("image/png");
        BufferedImage image = image(response.body());
        assertThat(image.getWidth()).isEqualTo(640);
        assertThat(image.getHeight()).isEqualTo(480);
        String[] bounds = header(response, "Foresignal-Image-Bounds").split(",");
        assertThat(bounds).hasSize(4);
        assertThat(Double.parseDouble(bounds[0])).isLessThan(-97);
        assertThat(Double.parseDouble(bounds[2])).isGreaterThan(-97);
        assertThat(header(response, "Cache-Control")).isEqualTo("public, max-age=60");
        assertThat(header(response, "Foresignal-Frame-Time")).isEqualTo(T.toString());
        String etag = header(response, "ETag");
        assertThat(getBytes(PATH + "?products=" + OBS + "&center=-97,31&radiusKm=200&width=640", "If-None-Match", etag)
                .statusCode()).isEqualTo(304);
    }

    @Test
    @DisplayName("pinned: a concrete time, a versioned style and a complete frame are immutable, without frame time")
    void pinnedIsImmutable() throws Exception {
        commitObservation(T, 0);
        HttpResponse<byte[]> response = getBytes(PATH + "?products=" + OBS + "&style=nws-reflectivity@1&time="
                + T + "&bbox=-100,28,-94,34");
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(header(response, "Cache-Control")).isEqualTo("public, max-age=31536000, immutable");
        assertThat(header(response, "Foresignal-Frame-Time")).isNull();
        HttpResponse<byte[]> unversioned = getBytes(PATH + "?products=" + OBS + "&time=" + T + "&bbox=-100,28,-94,34");
        assertThat(header(unversioned, "Cache-Control")).as("an unversioned style is an alias").isEqualTo(
                "public, max-age=60");
        assertThat(header(unversioned, "ETag")).isNotEqualTo(header(response, "ETag"));
    }

    @Test
    @DisplayName("default width 800, height ≤ 1280 and width ≤ 1280; oversize and bad boxes are VALIDATION_FAILED")
    void sizeCaps() throws Exception {
        commitObservation(T, 0);
        BufferedImage image = image(getBytes(PATH + "?bbox=-100,28,-94,34").body());
        assertThat(image.getWidth()).isEqualTo(800);
        assertThat(image.getHeight()).isEqualTo(600);
        assertThat(image(getBytes(PATH + "?bbox=-100,28,-94,34&width=1280&height=1280").body()).getHeight())
                .isEqualTo(1280);
        assertThat(code(get(PATH + "?bbox=-100,28,-94,34&width=1281"))).isEqualTo("VALIDATION_FAILED");
        assertThat(code(get(PATH + "?bbox=-100,28,-94,34&height=1281"))).isEqualTo("VALIDATION_FAILED");
        assertThat(code(get(PATH + "?bbox=-100,34,-94,28"))).isEqualTo("VALIDATION_FAILED");
        assertThat(code(get(PATH + "?bbox=-100,28,-94"))).isEqualTo("VALIDATION_FAILED");
        assertThat(code(get(PATH + "?products=" + OBS))).as("no box, center, alert or place").isEqualTo(
                "VALIDATION_FAILED");
        assertThat(code(get(PATH + "?bbox=-100,28,-94,34&center=-97,31"))).isEqualTo("VALIDATION_FAILED");
        assertThat(code(get(PATH + "?bbox=-100,28,-94,34&theme=sepia"))).isEqualTo("VALIDATION_FAILED");
    }

    @Test
    @DisplayName("errors: UNKNOWN_PRODUCT, RUN_REQUIRED, UNKNOWN_FRAME, UNKNOWN_STYLE; an alias with no frame still draws")
    void errors() throws Exception {
        assertThat(get(PATH + "?products=" + OBS + "&bbox=-100,28,-94,34").statusCode())
                .as("no frame yet: the map draws the unavailable note").isEqualTo(200);
        commitObservation(T, 0);
        assertThat(code(get(PATH + "?products=nope&bbox=-100,28,-94,34"))).isEqualTo("UNKNOWN_PRODUCT");
        assertThat(code(get(PATH + "?products=" + FCST + "&time=" + T + "&bbox=-100,28,-94,34"))).isEqualTo(
                "RUN_REQUIRED");
        HttpResponse<String> before = get(PATH + "?products=" + OBS + "&time=2026-10-09T00:00:00Z&bbox=-100,28,-94,34");
        assertThat(before.statusCode()).isEqualTo(404);
        assertThat(code(before)).isEqualTo("UNKNOWN_FRAME");
        assertThat(code(get(PATH + "?products=" + OBS + "&style=nope&bbox=-100,28,-94,34"))).isEqualTo("UNKNOWN_STYLE");
        assertThat(code(get(PATH + "?alert=999999&bbox=-100,28,-94,34"))).isEqualTo("NOT_FOUND");
    }

    @Test
    @DisplayName("a place needs a token, draws privately, and an unknown place is UNKNOWN_PLACE")
    void place() throws Exception {
        commitObservation(T, 0);
        places.create(json.readTree("{\"id\":\"austin\",\"name\":\"Austin\",\"shape\":\"point\",\"geometry\":"
                + "{\"type\":\"Point\",\"coordinates\":[-97.74,30.27]},\"radiusKm\":50,\"timezone\":\"America/Chicago\"}"));
        String path = PATH + "?products=" + OBS + "&place=austin&radiusKm=60";
        assertThat(get(path).statusCode()).isEqualTo(401);
        String reader = TestTokens.bearer(TestTokens.create(tokens, "map-reader", Scope.READ));
        HttpResponse<byte[]> response = getBytes(path, "Authorization", reader);
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(header(response, "Cache-Control")).startsWith("private");
        HttpResponse<String> unknown = get(PATH + "?place=nowhere", "Authorization", reader);
        assertThat(unknown.statusCode()).isEqualTo(404);
        assertThat(code(unknown)).isEqualTo("UNKNOWN_PLACE");
    }

    @Test
    @Timeout(30)
    @DisplayName("503 RENDER_BUSY with Retry-After when every render thread is busy for longer than the 2-s budget")
    void renderBusy() throws Exception {
        commitObservation(T, 0);
        CountDownLatch release = new CountDownLatch(1);
        List<CompletableFuture<byte[]>> blockers = new ArrayList<>();
        try {
            for (int k = 0; k < queue.poolSize(); k++) {
                blockers.add(queue.submit("blocker-" + k + "-" + System.nanoTime(), RenderQueue.Priority.WARM, "test",
                        () -> {
                            try {
                                release.await(20, TimeUnit.SECONDS);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            return new byte[0];
                        }));
            }
            HttpResponse<String> busy = get(PATH + "?products=" + OBS + "&bbox=-100,28,-94,34&width=321");
            assertThat(busy.statusCode()).isEqualTo(503);
            assertThat(code(busy)).isEqualTo("RENDER_BUSY");
            assertThat(header(busy, "Retry-After")).isEqualTo("1");
        } finally {
            release.countDown();
            for (CompletableFuture<byte[]> blocker : blockers) {
                blocker.get(20, TimeUnit.SECONDS);
            }
        }
        assertThat(getBytes(PATH + "?products=" + OBS + "&bbox=-100,28,-94,34&width=322").statusCode()).isEqualTo(200);
    }
}
