package ai.foresignal.raster;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.openapi.SpecSchemas;
import ai.foresignal.testsupport.raster.RasterFixtures;
import ai.foresignal.testsupport.raster.RasterTestSources;
import ai.foresignal.testsupport.raster.SyntheticRasterAdapter;
import ai.foresignal.testsupport.security.TestTokens;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The {@code Products} operations over HTTP (prompt 042): {@code listProducts}, {@code getProduct},
 * {@code listProductFrames} and {@code getLatestProductFrame}; their problem codes; Cache-Control
 * {@code public} versus {@code private}; ETags and 304; and responses that validate against the
 * spec's schemas. Frames are committed straight through the pipeline; no upstream is involved.
 */
@WebIntegrationTest
@Import(RasterTestSources.class)
class ProductsApiIT {

    static final Instant T = Instant.parse("2026-10-10T14:00:00Z");
    static final String OBS = SyntheticRasterAdapter.OBS;
    static final String FCST = SyntheticRasterAdapter.FCST;
    static final String SOURCE = SyntheticRasterAdapter.SOURCE;
    static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    static final SpecSchemas SCHEMAS = new SpecSchemas();

    @LocalServerPort int port;
    @Autowired RasterPipeline pipeline;
    @Autowired RasterPaths paths;
    @Autowired ProductStatusTracker status;
    @Autowired AppSettingService settings;
    @Autowired TokenService tokens;
    @Autowired MutableClock clock;
    @Autowired JsonMapper json;
    @Autowired SyntheticRasterAdapter adapter;
    @Autowired org.springframework.jdbc.core.JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        clock.setInstant(T.plus(Duration.ofMinutes(5)));
        settings.invalidateCache();
        status.reset();
        RasterFixtures.deleteTree(paths.productFrames(OBS));
        RasterFixtures.deleteTree(paths.productFrames(FCST));
    }

    private HttpResponse<String> get(String path, String... headers) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(20)).GET();
        for (int i = 0; i < headers.length; i += 2) {
            request.header(headers[i], headers[i + 1]);
        }
        return HTTP.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private JsonNode body(HttpResponse<String> response) {
        return json.readTree(response.body());
    }

    private String code(HttpResponse<String> response) {
        return body(response).get("code").asString();
    }

    private void seedObservations() throws Exception {
        pipeline.commit(SOURCE, RasterFixtures.observation(T.minusSeconds(240), 0));
        pipeline.commit(SOURCE, RasterFixtures.observation(T.minusSeconds(120), 1));
        pipeline.commit(SOURCE, RasterFixtures.observation(T, 2));
        pipeline.commit(SOURCE, RasterFixtures.partialObservation(T.plusSeconds(120)));
    }

    @Test
    @DisplayName("listProducts returns every catalog field, filters, compact items, and validates against ProductPage")
    void listProducts() throws Exception {
        seedObservations();
        HttpResponse<String> response = get("/v1/products");
        assertThat(response.statusCode()).isEqualTo(200);
        JsonNode page = body(response);
        assertThat(SCHEMAS.validateComponent("ProductPage", page)).isEmpty();
        JsonNode obs = null;
        for (JsonNode item : page.get("items")) {
            if (OBS.equals(item.get("id").asString())) {
                obs = item;
            }
        }
        assertThat(obs).isNotNull();
        assertThat(obs.get("source").asString()).isEqualTo(SOURCE);
        assertThat(obs.get("coverage").asString()).isEqualTo("CONUS");
        assertThat(obs.get("coverageMeaning").asString()).contains("no-coverage");
        assertThat(obs.get("temporal").get("cadence").asString()).isEqualTo("2m");
        assertThat(obs.get("temporal").get("latest").asString()).isEqualTo(T.toString());
        assertThat(obs.get("temporal").get("retention").get("summary").asString())
                .isEqualTo("every frame for 6h, then 10m frames to 48h");
        assertThat(obs.get("spatial").get("bounds").toString()).isEqualTo("[-100.0,28.0,-94.0,34.0]");
        assertThat(obs.get("render").get("defaultStyle").asString()).isEqualTo("test-reflectivity@1");
        assertThat(obs.get("attribution").get("licence").asString()).isEqualTo("US-PD");
        assertThat(obs.get("codes").get("1").asString()).isEqualTo("noEcho");
        assertThat(obs.get("links").get("frames").asString()).endsWith("/v1/products/" + OBS + "/frames");
        assertThat(obs.get("status").get("stale").asBoolean()).isFalse();

        assertThat(body(get("/v1/products?category=model")).get("items")).hasSize(1);
        assertThat(body(get("/v1/products?category=model")).get("items").get(0).get("id").asString()).isEqualTo(FCST);
        assertThat(body(get("/v1/products?q=REFLECT")).get("items")).hasSize(1);
        assertThat(body(get("/v1/products?source=nope")).get("items")).isEmpty();
        assertThat(body(get("/v1/products?bbox=10,10,20,20")).get("items").size())
                .as("only products whose bounds meet the box").isLessThan(page.get("items").size());
        JsonNode compact = body(get("/v1/products?compact=true&category=radar")).get("items").get(0);
        assertThat(compact.has("links")).isFalse();
        assertThat(compact.has("temporal")).isTrue();

        JsonNode first = body(get("/v1/products?limit=1"));
        assertThat(first.get("items")).hasSize(1);
        String next = first.get("nextCursor").asString();
        JsonNode second = body(get("/v1/products?limit=1&cursor=" + next));
        assertThat(second.get("items").get(0).get("id").asString())
                .isNotEqualTo(first.get("items").get(0).get("id").asString());
        assertThat(get("/v1/products?category=weather").statusCode()).isEqualTo(400);
        assertThat(get("/v1/products?cursor=junk").statusCode()).isEqualTo(400);
    }

    @Test
    @DisplayName("the catalog and a product answer 304 to a matching If-None-Match")
    void etags() throws Exception {
        HttpResponse<String> list = get("/v1/products");
        String etag = list.headers().firstValue("ETag").orElseThrow();
        assertThat(get("/v1/products", "If-None-Match", etag).statusCode()).isEqualTo(304);
        HttpResponse<String> one = get("/v1/products/" + OBS);
        assertThat(one.statusCode()).isEqualTo(200);
        assertThat(SCHEMAS.validateComponent("Product", body(one))).isEmpty();
        String tag = one.headers().firstValue("ETag").orElseThrow();
        assertThat(get("/v1/products/" + OBS, "If-None-Match", tag).statusCode()).isEqualTo(304);
    }

    @Test
    @DisplayName("an unknown product is 404 UNKNOWN_PRODUCT")
    void unknownProduct() throws Exception {
        HttpResponse<String> response = get("/v1/products/no-such-product");
        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(code(response)).isEqualTo("UNKNOWN_PRODUCT");
        assertThat(code(get("/v1/products/no-such-product/frames"))).isEqualTo("UNKNOWN_PRODUCT");
    }

    @Test
    @DisplayName("listProductFrames pages newest first with expanded templates, a prefetch hint and partials on request")
    void listFrames() throws Exception {
        seedObservations();
        HttpResponse<String> response = get("/v1/products/" + OBS + "/frames?limit=2");
        assertThat(response.statusCode()).isEqualTo(200);
        JsonNode page = body(response);
        assertThat(SCHEMAS.validateComponent("RasterFramePage", page)).isEmpty();
        assertThat(page.get("items")).hasSize(2);
        JsonNode newest = page.get("items").get(0);
        assertThat(newest.get("time").asString()).isEqualTo(T.toString());
        assertThat(newest.get("timeKey").asString()).isEqualTo("20261010T140000Z");
        assertThat(newest.get("frameKey").asString()).startsWith("20261010T140000Z~");
        assertThat(newest.get("tiles").asString())
                .isEqualTo("http://localhost:" + port + "/v1/products/" + OBS + "/tiles/" + newest.get("frameKey").asString()
                        + "/{z}/{x}/{y}.png?style=test-reflectivity@1");
        assertThat(newest.get("image").asString()).contains("{bbox}").contains("{width}").contains("{height}");
        assertThat(page.get("prefetch").get("recommendedFrames").asInt()).isEqualTo(12);
        assertThat(page.get("prefetch").get("maxZoomForAnimation").asInt()).isEqualTo(7);
        assertThat(page.get("serverTime").asString()).isNotBlank();
        assertThat(page.get("earliest").asString()).isEqualTo(T.minusSeconds(240).toString());
        JsonNode rest = body(get("/v1/products/" + OBS + "/frames?limit=2&cursor=" + page.get("nextCursor").asString()));
        assertThat(rest.get("items")).hasSize(1);
        assertThat(rest.get("nextCursor").isNull()).isTrue();

        assertThat(body(get("/v1/products/" + OBS + "/frames?includePartial=true")).get("items")).hasSize(4);
        assertThat(body(get("/v1/products/" + OBS + "/frames?from=" + T.minusSeconds(130) + "&to=" + T))
                .get("items")).hasSize(2);
        assertThat(get("/v1/products/" + OBS + "/frames?limit=0").statusCode()).isEqualTo(400);
        assertThat(get("/v1/products/" + OBS + "/frames?from=yesterday").statusCode()).isEqualTo(400);
        assertThat(get("/v1/products/" + OBS + "/frames?run=latest").statusCode()).isEqualTo(400);
    }

    @Test
    @DisplayName("frames/latest: the newest complete frame, short max-age, Foresignal-Frame-Time, ETag and 304")
    void latest() throws Exception {
        seedObservations();
        HttpResponse<String> response = get("/v1/products/" + OBS + "/frames/latest");
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(SCHEMAS.validateComponent("RasterFrame", body(response))).isEmpty();
        assertThat(body(response).get("time").asString()).isEqualTo(T.toString());
        assertThat(response.headers().firstValue("Cache-Control").orElseThrow())
                .as("openReads is on by default: public, cadence 2m / 2 = 60 s").isEqualTo("public, max-age=60");
        assertThat(response.headers().firstValue("Foresignal-Frame-Time").orElseThrow()).isEqualTo(T.toString());
        String etag = response.headers().firstValue("ETag").orElseThrow();
        assertThat(get("/v1/products/" + OBS + "/frames/latest", "If-None-Match", etag).statusCode()).isEqualTo(304);
        assertThat(body(get("/v1/products/" + OBS + "/frames/latest?includePartial=true")).get("status").asString())
                .isEqualTo("partial");
    }

    @Test
    @DisplayName("with openReads off a token gets Cache-Control private, and no token gets 401")
    void privateCaching() throws Exception {
        seedObservations();
        settings.put(SettingKeys.SECURITY, new SecuritySettings(false));
        String reader = TestTokens.bearer(TestTokens.create(tokens, "products-reader", Scope.READ));
        assertThat(get("/v1/products/" + OBS + "/frames/latest").statusCode()).isEqualTo(401);
        HttpResponse<String> response = get("/v1/products/" + OBS + "/frames/latest", "Authorization", reader);
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("Cache-Control").orElseThrow()).isEqualTo("private, max-age=60");
        assertThat(get("/v1/products", "Authorization", reader).headers().firstValue("Cache-Control").orElseThrow())
                .startsWith("private");
    }

    @Test
    @DisplayName("no frame yet is 404 UNKNOWN_FRAME; a disabled product is 503 PRODUCT_UNAVAILABLE")
    void codes() throws Exception {
        HttpResponse<String> none = get("/v1/products/" + OBS + "/frames/latest");
        assertThat(none.statusCode()).isEqualTo(404);
        assertThat(code(none)).isEqualTo("UNKNOWN_FRAME");
        status.disable(OBS, SOURCE, "decoder self-test failed", T);
        HttpResponse<String> disabled = get("/v1/products/" + OBS + "/frames");
        assertThat(disabled.statusCode()).isEqualTo(503);
        assertThat(code(disabled)).isEqualTo("PRODUCT_UNAVAILABLE");
        assertThat(get("/v1/products/" + OBS + "/frames/latest").statusCode()).isEqualTo(503);
        assertThat(body(get("/v1/products/" + OBS)).get("status").get("reason").asString())
                .contains("decoder self-test failed");
    }

    @Test
    @DisplayName("forecast frames carry run and step; run=previous needs a second complete run; a bad run is 400")
    void forecastFrames() throws Exception {
        Instant run = T.minus(Duration.ofHours(2));
        for (int step = 0; step <= 3; step++) {
            pipeline.commit(SOURCE, RasterFixtures.forecast(run, step));
        }
        pipeline.completeRun(SOURCE, FCST, run);
        JsonNode latest = body(get("/v1/products/" + FCST + "/frames/latest"));
        assertThat(latest.get("run").asString()).isEqualTo("2026-10-10T12:00:00Z");
        assertThat(latest.get("step").asInt()).isEqualTo(2);
        assertThat(latest.get("tiles").asString()).contains("run=2026-10-10T12:00:00Z");
        assertThat(body(get("/v1/products/" + FCST + "/frames?run=latest")).get("items")).hasSize(4);
        assertThat(code(get("/v1/products/" + FCST + "/frames/latest?run=previous"))).isEqualTo("UNKNOWN_FRAME");
        assertThat(get("/v1/products/" + FCST + "/frames?run=someday").statusCode()).isEqualTo(400);
        JsonNode product = body(get("/v1/products/" + FCST));
        assertThat(product.get("temporal").get("runs").get(0).get("status").asString()).isEqualTo("complete");
        assertThat(product.get("temporal").get("runs").get(0).get("steps").toString()).isEqualTo("[0,1,2,3]");
    }

    @Test
    @DisplayName("no product request causes a fetch of the product source")
    void noUpstreamFetch() throws Exception {
        seedObservations();
        int before = adapter.fetchCount();
        for (String path : List.of("/v1/products", "/v1/products/" + OBS, "/v1/products/" + OBS + "/frames",
                "/v1/products/" + OBS + "/frames/latest", "/v1/products/" + FCST + "/frames/latest")) {
            get(path);
        }
        assertThat(adapter.fetchCount()).isEqualTo(before);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM fetch_log", Long.class)).isZero();
    }

    @Test
    @DisplayName("the rasters block of /v1/system reports the profile, the store and the frame cache")
    void systemBlock() throws Exception {
        seedObservations();
        String admin = TestTokens.bearer(TestTokens.create(tokens, "products-admin", Scope.ADMIN));
        JsonNode system = body(get("/v1/system", "Authorization", admin));
        JsonNode rasters = system.get("rasters");
        assertThat(rasters).isNotNull();
        assertThat(SCHEMAS.validateComponent("SystemRasters", rasters)).isEmpty();
        assertThat(rasters.get("profile").asString()).isEqualTo("small");
        assertThat(rasters.get("frames").asLong()).isEqualTo(4);
        assertThat(rasters.get("storeBytes").asLong()).isPositive();
        assertThat(rasters.get("diskQuotaGb").asDouble()).isEqualTo(200.0);
        assertThat(rasters.get("caches").get("frame").get("maxBytes").asLong()).isEqualTo(768L * 1024 * 1024);
        assertThat(List.of(system.get("status").asString())).containsAnyOf("ok", "degraded");
    }
}
