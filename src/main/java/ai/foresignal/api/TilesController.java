package ai.foresignal.api;

import ai.foresignal.api.mapper.TileJsonMapper;
import ai.foresignal.api.support.ETags;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.ServerSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.store.tiles.AggMode;
import ai.foresignal.store.tiles.TileCoordinates;
import ai.foresignal.store.tiles.TileLayer;
import ai.foresignal.store.tiles.TileLayerResolver;
import ai.foresignal.store.tiles.TileProperties;
import ai.foresignal.store.tiles.TileQueryService;
import ai.foresignal.store.tiles.TileResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.json.JsonMapper;

/**
 * Vector tiles and TileJSON (OpenAPI tag {@code Tiles}, prompt 016): {@code getTile},
 * {@code listTileLayers} and {@code getTileJson}.
 *
 * <p>The three routes are bound explicitly. Spring's path-pattern parser does no suffix-pattern
 * matching, so {@code .mvt} and {@code .json} are literal parts of the patterns, {@code {y}} binds
 * the digits before {@code .mvt}, and {@code /v1/tiles/{layer}.json} cannot collide with
 * {@code /v1/tiles/{layer}/{z}/{x}/{y}.mvt} (different segment counts).
 *
 * <p>Every operation needs the {@code read} scope, open while {@code security.openReads} is on;
 * private sources are invisible to anonymous callers. CORS read rules apply (all three are GETs).
 */
@RestController
@Tag(name = "Tiles")
public class TilesController {

    /** The Mapbox Vector Tile media type. */
    public static final MediaType MVT = MediaType.parseMediaType("application/vnd.mapbox-vector-tile");

    /** Sent with {@code true} when a raw tile hit {@code foresignal.tiles.maxFeatures}. */
    public static final String TRUNCATED_HEADER = "Foresignal-Tile-Truncated";

    /** {@code max-age} of the TileJSON documents. */
    static final Duration TILEJSON_MAX_AGE = Duration.ofSeconds(300);

    /** {@code Vary} of tiles and TileJSON: they depend on the caller (private sources) and encoding. */
    static final String VARY = "Accept-Encoding, Authorization";

    private final TileLayerResolver layers;
    private final TileQueryService tiles;
    private final TileJsonMapper tileJson;
    private final ReadCallerResolver callers;
    private final AppSettingService settings;
    private final TileProperties properties;
    private final JsonMapper json;

    /** Creates the controller. */
    public TilesController(
            TileLayerResolver layers,
            TileQueryService tiles,
            TileJsonMapper tileJson,
            ReadCallerResolver callers,
            AppSettingService settings,
            TileProperties properties,
            JsonMapper json) {
        this.layers = layers;
        this.tiles = tiles;
        this.tileJson = tileJson;
        this.callers = callers;
        this.settings = settings;
        this.properties = properties;
        this.json = json;
    }

    /**
     * {@code getTile}: a Mapbox Vector Tile of current live state. {@code 200} with
     * {@code application/vnd.mapbox-vector-tile} even when empty; a strong {@code ETag} hashed from
     * the bytes and {@code 304} on {@code If-None-Match}; {@code Cache-Control max-age} of
     * {@code rawMaxAge} (5 s) for raw tiles and {@code aggMaxAge} (60 s) for aggregated ones,
     * {@code public} while {@code openReads} is on, else {@code private}.
     */
    @GetMapping("/v1/tiles/{layer}/{z}/{x}/{y}.mvt")
    @Operation(operationId = "getTile", tags = "Tiles")
    public ResponseEntity<byte[]> getTile(
            @PathVariable String layer,
            @PathVariable String z,
            @PathVariable String x,
            @PathVariable String y,
            @RequestParam(required = false) String agg,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch,
            HttpServletRequest request) {
        TileCoordinates coordinates = TileCoordinates.parse(z, x, y);
        AggMode mode = AggMode.parse(agg);
        ReadCaller caller = callers.resolve(request);
        TileLayer resolved = layers.resolve(layer, callers.excludedSources(caller));
        TileResult result = tiles.render(resolved, coordinates, mode);

        String etag = ETags.forBytes(result.mvt());
        Duration maxAge = result.shape().aggregated() ? properties.aggMaxAge() : properties.rawMaxAge();
        HttpHeaders headers = headers(etag, caller.openReads(), maxAge);
        if (result.truncated()) {
            headers.set(TRUNCATED_HEADER, "true");
        }
        if (ETags.matches(ifNoneMatch, etag)) {
            return ResponseEntity.status(304).headers(headers).build();
        }
        return ResponseEntity.ok().headers(headers).contentType(MVT).body(result.mvt());
    }

    /** {@code listTileLayers}: every source id and kind the caller can see, with its TileJSON URL. */
    @GetMapping("/v1/tiles.json")
    @Operation(operationId = "listTileLayers", tags = "Tiles")
    public ResponseEntity<String> listTileLayers(
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch,
            HttpServletRequest request) {
        ReadCaller caller = callers.resolve(request);
        Set<String> excluded = callers.excludedSources(caller);
        return document(tileJson.index(layers.layers(excluded), baseUrl(request)), caller, ifNoneMatch);
    }

    /** {@code getTileJson}: the TileJSON 3.0.0 document of one layer; {@code 404 UNKNOWN_LAYER}. */
    @GetMapping("/v1/tiles/{layer}.json")
    @Operation(operationId = "getTileJson", tags = "Tiles")
    public ResponseEntity<String> getTileJson(
            @PathVariable String layer,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch,
            HttpServletRequest request) {
        ReadCaller caller = callers.resolve(request);
        TileLayer resolved = layers.resolve(layer, callers.excludedSources(caller));
        return document(tileJson.tileJson(resolved, baseUrl(request)), caller, ifNoneMatch);
    }

    /**
     * The base of generated URLs: {@code server.publicBaseUrl} when set, else the request's scheme,
     * host and port (default ports omitted). {@code X-Forwarded-*} headers are not trusted here;
     * prompt 023 adds trusted-proxy handling.
     */
    String baseUrl(HttpServletRequest request) {
        ServerSettings server = settings.getOrDefault(SettingKeys.SERVER, ServerSettings.class, ServerSettings::defaults);
        String configured = server.normalizedPublicBaseUrl();
        if (configured != null) {
            return configured;
        }
        return requestBase(request.getScheme(), request.getServerName(), request.getServerPort());
    }

    /** {@code scheme://host[:port]}, bracketing an IPv6 literal and omitting 80/443 for http/https. */
    static String requestBase(String scheme, String host, int port) {
        String h = host.contains(":") && !host.startsWith("[") ? "[" + host + "]" : host;
        boolean defaultPort = port <= 0
                || ("http".equalsIgnoreCase(scheme) && port == 80)
                || ("https".equalsIgnoreCase(scheme) && port == 443);
        return scheme + "://" + h + (defaultPort ? "" : ":" + port);
    }

    private ResponseEntity<String> document(Map<String, Object> body, ReadCaller caller, String ifNoneMatch) {
        String text = json.writeValueAsString(body);
        String etag = ETags.forDocument(text);
        HttpHeaders headers = headers(etag, caller.openReads(), TILEJSON_MAX_AGE);
        if (ETags.matches(ifNoneMatch, etag)) {
            return ResponseEntity.status(304).headers(headers).build();
        }
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
                .body(text);
    }

    private static HttpHeaders headers(String etag, boolean openReads, Duration maxAge) {
        HttpHeaders headers = new HttpHeaders();
        headers.setETag(etag);
        headers.setCacheControl(cacheControl(openReads, maxAge));
        headers.set(HttpHeaders.VARY, VARY);
        return headers;
    }

    /** {@code public, max-age=N} while {@code openReads} is on, else {@code private, max-age=N}. */
    static String cacheControl(boolean openReads, Duration maxAge) {
        return (openReads ? "public" : "private") + ", max-age=" + maxAge.toSeconds();
    }
}
