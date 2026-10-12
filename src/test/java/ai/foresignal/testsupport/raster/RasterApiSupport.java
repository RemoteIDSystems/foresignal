package ai.foresignal.testsupport.raster;

import ai.foresignal.raster.ProductStatusTracker;
import ai.foresignal.raster.RasterFrameQueries;
import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.raster.RasterPaths;
import ai.foresignal.raster.RasterPipeline;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.test.MutableClock;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Shared plumbing of the prompt 045 raster HTTP integration tests: they are
 * {@code @WebIntegrationTest @Import(RasterTestSources.class)}, so they share 042's one cached context.
 * Frames are committed straight through the pipeline; no upstream is involved.
 */
public abstract class RasterApiSupport {

    /** The frame time the tests commit at. */
    public static final Instant T = Instant.parse("2026-10-10T14:00:00Z");

    /** The observation product. */
    public static final String OBS = SyntheticRasterAdapter.OBS;

    /** The forecast product. */
    public static final String FCST = SyntheticRasterAdapter.FCST;

    /** The synthetic source. */
    public static final String SOURCE = SyntheticRasterAdapter.SOURCE;

    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    @LocalServerPort protected int port;
    @Autowired protected RasterPipeline pipeline;
    @Autowired protected RasterPaths paths;
    @Autowired protected RasterFrameQueries frames;
    @Autowired protected ProductStatusTracker status;
    @Autowired protected AppSettingService settings;
    @Autowired protected MutableClock clock;
    @Autowired protected JsonMapper json;

    /** Resets the clock, settings and frame files. */
    @BeforeEach
    void resetRaster() {
        clock.setInstant(T.plus(Duration.ofMinutes(1)));
        settings.invalidateCache();
        status.reset();
        RasterFixtures.deleteTree(paths.productFrames(OBS));
        RasterFixtures.deleteTree(paths.productFrames(FCST));
    }

    /** GET as text. */
    protected HttpResponse<String> get(String path, String... headers) throws Exception {
        return HTTP.send(request(path, headers), HttpResponse.BodyHandlers.ofString());
    }

    /** GET as bytes. */
    protected HttpResponse<byte[]> getBytes(String path, String... headers) throws Exception {
        return HTTP.send(request(path, headers), HttpResponse.BodyHandlers.ofByteArray());
    }

    private HttpRequest request(String path, String... headers) {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(path.startsWith("http") ? path
                : "http://localhost:" + port + path)).timeout(Duration.ofSeconds(20)).GET();
        for (int i = 0; i < headers.length; i += 2) {
            b.header(headers[i], headers[i + 1]);
        }
        return b.build();
    }

    /** The JSON body. */
    protected JsonNode body(HttpResponse<String> response) {
        return json.readTree(response.body());
    }

    /** The problem code of an error response. */
    protected String code(HttpResponse<String> response) {
        return body(response).get("code").asString();
    }

    /** A decoded PNG or JPEG body. */
    protected static BufferedImage image(byte[] body) throws Exception {
        return ImageIO.read(new ByteArrayInputStream(body));
    }

    /** A header, or null. */
    protected static String header(HttpResponse<?> response, String name) {
        return response.headers().firstValue(name).orElse(null);
    }

    /** Commits a complete observation frame at {@code t} and returns its row. */
    protected RasterFrameRow commitObservation(Instant t, int shift) throws Exception {
        pipeline.commit(SOURCE, RasterFixtures.observation(t, shift));
        return frames.atValidTime(OBS, t).get(0);
    }

    /** Commits a complete forecast run of {@code steps} steps. */
    protected void commitRun(Instant run, int steps) throws Exception {
        for (int step = 0; step < steps; step++) {
            pipeline.commit(SOURCE, RasterFixtures.forecast(run, step));
        }
        pipeline.completeRun(SOURCE, FCST, run);
    }
}
