package ai.foresignal.raster;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.raster.render.TransparentTile;
import ai.foresignal.raster.render.WebMercator;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.SettingKeys;
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
 * Tiles, styles, legends and the external catalog over HTTP (prompt 045): a committed frame renders
 * through {@code getProductTile} with the right cache headers (content-addressed URLs immutable,
 * nothing else), conditional GETs, the shared transparent tile, every problem code, {@code private}
 * caching with {@code openReads} off, and the {@code /v1/system} render gauges.
 */
@WebIntegrationTest
@Import(RasterTestSources.class)
class ProductTilesIT extends RasterApiSupport {

    static final SpecSchemas SCHEMAS = new SpecSchemas();

    /** z4/3/6 holds the whole synthetic grid (−100..−94, 28..34). */
    static final int Z = 4;
    static final int X = 3;
    static final int Y = 6;

    @Autowired TokenService tokens;

    /** The pixel of a lon/lat inside tile z/x/y of {@code size} pixels. */
    static int[] pixel(int z, int x, int y, int size, double lon, double lat) {
        double[] m = WebMercator.tileMeters(z, x, y);
        double step = (m[2] - m[0]) / size;
        return new int[] {(int) ((WebMercator.x(lon) - m[0]) / step), (int) ((m[3] - WebMercator.y(lat)) / step)};
    }

    private String tilePath(String frameKey, String suffix) {
        return "/v1/products/" + OBS + "/tiles/" + frameKey + "/" + Z + "/" + X + "/" + Y + suffix;
    }

    @Test
    @DisplayName("a content-addressed tile is immutable with a strong ETag, draws the frame, and answers 304")
    void contentAddressedTile() throws Exception {
        RasterFrameRow frame = commitObservation(T, 0);
        HttpResponse<byte[]> response = getBytes(tilePath(frame.frameKey(), ".png?style=nws-reflectivity@1"));
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(header(response, "Content-Type")).isEqualTo("image/png");
        assertThat(header(response, "Cache-Control")).isEqualTo("public, max-age=31536000, immutable");
        assertThat(header(response, "Foresignal-Frame-Time")).isNull();
        assertThat(header(response, "Content-Encoding")).as("PNG is never gzipped").isNull();
        String etag = header(response, "ETag");
        assertThat(etag).startsWith("\"").doesNotStartWith("W/");
        BufferedImage image = image(response.body());
        int[] green = pixel(Z, X, Y, 256, -95.0, 31.0);
        assertThat(image.getRGB(green[0], green[1])).as("20 dBZ").isEqualTo(0xFF02FD02);
        int[] noCoverage = pixel(Z, X, Y, 256, -99.0, 33.0);
        assertThat(image.getRGB(noCoverage[0], noCoverage[1]) >>> 24).as("no coverage is transparent").isZero();
        int[] outside = pixel(Z, X, Y, 256, -105.0, 31.0);
        assertThat(image.getRGB(outside[0], outside[1]) >>> 24).isZero();
        assertThat(getBytes(tilePath(frame.frameKey(), ".png?style=nws-reflectivity@1"), "If-None-Match", etag)
                .statusCode()).isEqualTo(304);
    }

    @Test
    @DisplayName("aliases, bare time keys and unversioned styles get the short max-age and Foresignal-Frame-Time")
    void shortLivedUrls() throws Exception {
        RasterFrameRow frame = commitObservation(T, 0);
        for (String path : new String[] {tilePath("latest", ".png?style=nws-reflectivity@1"),
                tilePath(frame.timeKey(), ".png?style=nws-reflectivity@1"),
                tilePath(frame.frameKey(), ".png?style=nws-reflectivity"),
                tilePath(frame.frameKey(), ".png")}) {
            HttpResponse<byte[]> response = getBytes(path);
            assertThat(response.statusCode()).as(path).isEqualTo(200);
            assertThat(header(response, "Cache-Control")).as(path).isEqualTo("public, max-age=60");
            assertThat(header(response, "Foresignal-Frame-Time")).as(path).isEqualTo(T.toString());
            String etag = header(response, "ETag");
            assertThat(getBytes(path, "If-None-Match", etag).statusCode()).as(path).isEqualTo(304);
        }
    }

    @Test
    @DisplayName("@2x renders 512 px for the same extent")
    void retina() throws Exception {
        RasterFrameRow frame = commitObservation(T, 0);
        HttpResponse<byte[]> response = getBytes(tilePath(frame.frameKey(), "@2x.png?style=nws-reflectivity@1"));
        assertThat(response.statusCode()).isEqualTo(200);
        BufferedImage image = image(response.body());
        assertThat(image.getWidth()).isEqualTo(512);
        int[] green = pixel(Z, X, Y, 512, -95.0, 31.0);
        assertThat(image.getRGB(green[0], green[1])).isEqualTo(0xFF02FD02);
    }

    @Test
    @DisplayName("an out-of-bounds tile is the shared transparent PNG with its fixed ETag; empty=204 opts into 204")
    void transparentTile() throws Exception {
        RasterFrameRow frame = commitObservation(T, 0);
        String path = "/v1/products/" + OBS + "/tiles/" + frame.frameKey() + "/4/0/0.png?style=nws-reflectivity@1";
        HttpResponse<byte[]> response = getBytes(path);
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo(TransparentTile.png());
        assertThat(header(response, "ETag")).isEqualTo(TransparentTile.ETAG);
        assertThat(header(response, "Cache-Control")).isEqualTo("public, max-age=31536000, immutable");
        assertThat(getBytes(path, "If-None-Match", TransparentTile.ETAG).statusCode()).isEqualTo(304);
        assertThat(getBytes(path + "&empty=204").statusCode()).isEqualTo(204);
    }

    @Test
    @DisplayName("TILE_OUT_OF_RANGE, UNKNOWN_STYLE, stale sha8 with Link, 301 for ISO times, FRAME_EXPIRED and onExpired")
    void problems() throws Exception {
        RasterFrameRow frame = commitObservation(T, 0);
        HttpResponse<String> deep = get("/v1/products/" + OBS + "/tiles/" + frame.frameKey() + "/9/0/0.png");
        assertThat(deep.statusCode()).isEqualTo(400);
        assertThat(code(deep)).isEqualTo("TILE_OUT_OF_RANGE");
        HttpResponse<String> style = get(tilePath(frame.frameKey(), ".png?style=rainbow@1"));
        assertThat(style.statusCode()).isEqualTo(400);
        assertThat(code(style)).isEqualTo("UNKNOWN_STYLE");
        HttpResponse<String> stale = get(tilePath(frame.timeKey() + "~00000000", ".png?style=nws-reflectivity@1"));
        assertThat(stale.statusCode()).isEqualTo(404);
        assertThat(code(stale)).isEqualTo("UNKNOWN_FRAME");
        assertThat(header(stale, "Link")).contains(frame.frameKey()).contains("rel=\"current\"");
        HttpResponse<String> iso = get(tilePath("2026-10-10T14:00:00Z", ".png?style=nws-reflectivity@1"));
        assertThat(iso.statusCode()).isEqualTo(301);
        assertThat(header(iso, "Location")).contains("/tiles/20261010T140000Z/4/3/6.png?style=nws-reflectivity@1");
        String old = FrameKeys.timeKey(T.minus(Duration.ofDays(1)));
        HttpResponse<String> expired = get(tilePath(old, ".png"));
        assertThat(expired.statusCode()).isEqualTo(410);
        assertThat(code(expired)).isEqualTo("FRAME_EXPIRED");
        HttpResponse<byte[]> transparent = getBytes(tilePath(old, ".png?onExpired=transparent"));
        assertThat(transparent.statusCode()).isEqualTo(200);
        assertThat(header(transparent, "Foresignal-Frame-Expired")).isEqualTo("true");
        assertThat(transparent.body()).isEqualTo(TransparentTile.png());
        HttpResponse<String> badRow = get("/v1/products/" + OBS + "/tiles/latest/4/3/6@3x.png");
        assertThat(badRow.statusCode()).isEqualTo(400);
        status.disable(OBS, SOURCE, "decoder self-test failed", T);
        assertThat(code(get(tilePath("latest", ".png")))).isEqualTo("PRODUCT_UNAVAILABLE");
    }

    @Test
    @DisplayName("external products refuse tiles; /v1/products?kind=external lists them with their external entry")
    void externalProducts() throws Exception {
        HttpResponse<String> refused = get("/v1/products/gibs-goes-east-geocolor/tiles/latest/1/0/0.png");
        assertThat(refused.statusCode()).isEqualTo(400);
        assertThat(code(refused)).isEqualTo("VALIDATION_FAILED");
        assertThat(refused.body()).contains("external product: fetch externalTemplate directly");
        JsonNode page = body(get("/v1/products?kind=external&limit=50"));
        assertThat(SCHEMAS.validateComponent("ProductPage", page)).isEmpty();
        assertThat(page.get("items").size()).isEqualTo(14);
        JsonNode geocolor = null;
        for (JsonNode item : page.get("items")) {
            if (item.get("id").asString().equals("gibs-goes-east-geocolor")) {
                geocolor = item;
            }
        }
        assertThat(geocolor).isNotNull();
        assertThat(geocolor.get("source").asString()).isEqualTo(ExternalCatalogSource.SOURCE_ID);
        assertThat(geocolor.get("external").get("tilingScheme").asString()).isEqualTo("GoogleMapsCompatible_Level7");
        assertThat(geocolor.get("external").get("externalTemplate").asString())
                .startsWith("https://gibs.earthdata.nasa.gov/wmts/epsg3857/best/GOES-East_ABI_GeoColor/default/{time}/");
    }

    @Test
    @DisplayName("with openReads off, a token gets private caching even on immutable URLs; no token is 401")
    void privateCaching() throws Exception {
        RasterFrameRow frame = commitObservation(T, 0);
        settings.put(SettingKeys.SECURITY, new SecuritySettings(false));
        String reader = TestTokens.bearer(TestTokens.create(tokens, "tiles-reader", Scope.READ));
        String path = tilePath(frame.frameKey(), ".png?style=nws-reflectivity@1");
        assertThat(get(path).statusCode()).isEqualTo(401);
        HttpResponse<byte[]> response = getBytes(path, "Authorization", reader);
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(header(response, "Cache-Control")).isEqualTo("private, max-age=31536000, immutable");
    }

    @Test
    @DisplayName("styles list and get validate; legends in JSON and PNG; unknown styles are 400")
    void stylesAndLegends() throws Exception {
        JsonNode styles = body(get("/v1/styles"));
        assertThat(SCHEMAS.validateComponent("StylePage", styles)).isEmpty();
        // 045 shipped nine styles; 053 added ffg-ratio, qpe and rotation (src/main/resources/raster/styles).
        assertThat(styles.get("items").size()).isEqualTo(12);
        JsonNode first = body(get("/v1/styles?limit=2"));
        assertThat(first.get("items").size()).isEqualTo(2);
        JsonNode second = body(get("/v1/styles?limit=2&cursor=" + first.get("nextCursor").asString()));
        assertThat(second.get("items").get(0).get("ref").asString()).isEqualTo("ir-gray@1");
        JsonNode style = body(get("/v1/styles/nws-reflectivity"));
        assertThat(SCHEMAS.validateComponent("Style", style)).isEmpty();
        assertThat(style.get("ref").asString()).isEqualTo("nws-reflectivity@1");
        assertThat(style.get("current").asBoolean()).isTrue();
        assertThat(code(get("/v1/styles/nope"))).isEqualTo("UNKNOWN_STYLE");
        HttpResponse<String> legend = get("/v1/products/" + OBS + "/legend?style=nws-reflectivity@1");
        assertThat(legend.statusCode()).isEqualTo(200);
        assertThat(SCHEMAS.validateComponent("Legend", body(legend))).isEmpty();
        assertThat(header(legend, "Cache-Control")).isEqualTo("public, max-age=31536000, immutable");
        assertThat(header(get("/v1/products/" + OBS + "/legend"), "Cache-Control")).isEqualTo("public, max-age=60");
        assertThat(get("/v1/products/" + OBS + "/legend?style=nws-reflectivity@1", "If-None-Match",
                header(legend, "ETag")).statusCode()).isEqualTo(304);
        HttpResponse<byte[]> png = getBytes("/v1/products/" + OBS + "/legend.png?width=320&orientation=vertical"
                + "&theme=dark&units=imperial");
        assertThat(png.statusCode()).isEqualTo(200);
        BufferedImage image = image(png.body());
        assertThat(image.getWidth()).isEqualTo(320);
        assertThat(image.getHeight()).isEqualTo(256);
        assertThat(get("/v1/products/" + OBS + "/legend.png?orientation=diagonal").statusCode()).isEqualTo(400);
        assertThat(code(get("/v1/products/" + OBS + "/legend?units=nautical"))).isEqualTo("VALIDATION_FAILED");
    }

    @Test
    @DisplayName("/v1/system reports the tile and index caches and the render queue")
    void systemGauges() throws Exception {
        // The rasters block is part of the full document, which needs a token (SystemInfoService.build).
        String reader = TestTokens.bearer(TestTokens.create(tokens, "system-reader", Scope.READ));
        JsonNode rasters = body(get("/v1/system", "Authorization", reader)).get("rasters");
        assertThat(rasters.get("caches").has("tile")).isTrue();
        assertThat(rasters.get("caches").has("index")).isTrue();
        assertThat(rasters.get("renderQueue").get("poolSize").asInt()).isPositive();
        // 057: the queue depth is reported too (Weather-Imagery-Design §6.3, the render queue).
        assertThat(rasters.get("renderQueue").get("depth").isIntegralNumber()).isTrue();
        assertThat(rasters.get("renderQueue").get("depth").asInt()).isGreaterThanOrEqualTo(0);
        assertThat(rasters.get("renderBusyLastHour").asInt()).isGreaterThanOrEqualTo(0);
        assertThat(SCHEMAS.validateComponent("SystemRasters", rasters)).isEmpty();
    }
}
