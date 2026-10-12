package ai.foresignal.raster;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.raster.RasterApiSupport;
import ai.foresignal.testsupport.raster.RasterTestSources;
import java.awt.image.BufferedImage;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

/**
 * {@code getProductImage} (prompt 045): one frame over a box in EPSG:3857 or EPSG:4326, the snapped
 * bounds in {@code Foresignal-Image-Bounds}, the 2048 px cap, PNG and JPEG, the cache rule and the
 * validation errors.
 */
@WebIntegrationTest
@Import(RasterTestSources.class)
class ImageEndpointIT extends RasterApiSupport {

    private String path(String frameKey, String format, String query) {
        return "/v1/products/" + OBS + "/image/" + frameKey + "." + format + "?" + query;
    }

    @Test
    @DisplayName("a 4326 image of the grid box is exact: bounds header, size from the aspect, colors in place")
    void geographic() throws Exception {
        RasterFrameRow frame = commitObservation(T, 0);
        HttpResponse<byte[]> response = getBytes(path(frame.frameKey(), "png",
                "bbox=-100,28,-94,34&width=400&crs=EPSG:4326&style=nws-reflectivity@1"));
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(header(response, "Content-Type")).isEqualTo("image/png");
        assertThat(header(response, "Foresignal-Image-Bounds")).isEqualTo("-100,28,-94,34");
        assertThat(header(response, "Cache-Control")).isEqualTo("public, max-age=31536000, immutable");
        BufferedImage image = image(response.body());
        assertThat(image.getWidth()).isEqualTo(400);
        assertThat(image.getHeight()).isEqualTo(400);
        // Row 1, column 3 (−95.5..−94, 30..32) is 20 dBZ; row 0 is no coverage.
        assertThat(image.getRGB(370, 180)).isEqualTo(0xFF02FD02);
        assertThat(image.getRGB(50, 20) >>> 24).isZero();
        assertThat(getBytes(path(frame.frameKey(), "png",
                "bbox=-100,28,-94,34&width=400&crs=EPSG:4326&style=nws-reflectivity@1"), "If-None-Match",
                header(response, "ETag")).statusCode()).isEqualTo(304);
    }

    @Test
    @DisplayName("the box is snapped outwards to 0.01°; a 3857 image keeps the mercator aspect")
    void snappedMercator() throws Exception {
        RasterFrameRow frame = commitObservation(T, 0);
        HttpResponse<byte[]> response = getBytes(path(frame.frameKey(), "png", "bbox=-100.004,27.996,-93.991,34.004"));
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(header(response, "Foresignal-Image-Bounds")).isEqualTo("-100.01,27.99,-93.99,34.01");
        BufferedImage image = image(response.body());
        assertThat(image.getWidth()).isEqualTo(1024);
        assertThat(image.getHeight()).as("taller than wide in mercator at 31° N").isGreaterThan(1024);
        assertThat(header(response, "Cache-Control")).as("no style param: not content-addressed")
                .isEqualTo("public, max-age=60");
        assertThat(header(response, "Foresignal-Frame-Time")).isEqualTo(T.toString());
    }

    @Test
    @DisplayName("up to 2048 px a side; 2049 is refused, as is a width that would make the height exceed it")
    void sizeCap() throws Exception {
        RasterFrameRow frame = commitObservation(T, 0);
        assertThat(getBytes(path(frame.frameKey(), "png", "bbox=-100,28,-94,34&width=2048&height=2048")).statusCode())
                .isEqualTo(200);
        HttpResponse<String> wide = get(path(frame.frameKey(), "png", "bbox=-100,28,-94,34&width=2049"));
        assertThat(wide.statusCode()).isEqualTo(400);
        assertThat(code(wide)).isEqualTo("VALIDATION_FAILED");
        assertThat(code(get(path(frame.frameKey(), "png", "bbox=-100,0,-99,60&width=2000")))).isEqualTo(
                "VALIDATION_FAILED");
    }

    @Test
    @DisplayName("JPEG, latest alias, and the validation errors")
    void formatsAndErrors() throws Exception {
        commitObservation(T, 0);
        HttpResponse<byte[]> jpeg = getBytes(path("latest", "jpg", "bbox=-100,28,-94,34&width=300"));
        assertThat(jpeg.statusCode()).isEqualTo(200);
        assertThat(header(jpeg, "Content-Type")).isEqualTo("image/jpeg");
        assertThat(image(jpeg.body()).getWidth()).isEqualTo(300);
        assertThat(header(jpeg, "Cache-Control")).isEqualTo("public, max-age=60");
        assertThat(code(get(path("latest", "webp", "bbox=-100,28,-94,34")))).isEqualTo("VALIDATION_FAILED");
        assertThat(code(get(path("latest", "png", "width=10")))).isEqualTo("VALIDATION_FAILED");
        assertThat(code(get(path("latest", "png", "bbox=-100,34,-94,28")))).isEqualTo("VALIDATION_FAILED");
        assertThat(code(get(path("latest", "png", "bbox=-100,28,-94,34&crs=EPSG:27700")))).isEqualTo(
                "VALIDATION_FAILED");
        assertThat(code(get(path("latest", "png", "bbox=-100,28,-94,34&style=nope")))).isEqualTo("UNKNOWN_STYLE");
        assertThat(code(get("/v1/products/gibs-imerg-precipitation-rate/image/latest.png?bbox=-100,28,-94,34")))
                .isEqualTo("VALIDATION_FAILED");
        HttpResponse<String> iso = get(path("2026-10-10T14:00:00Z", "png", "bbox=-100,28,-94,34"));
        assertThat(iso.statusCode()).isEqualTo(301);
        assertThat(header(iso, "Location")).contains("/image/20261010T140000Z.png?bbox=-100,28,-94,34");
    }
}
