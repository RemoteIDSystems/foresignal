package ai.foresignal.api;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.mapper.TileJsonMapper;
import ai.foresignal.api.support.AsOfParam;
import ai.foresignal.api.support.TimeTravelCache;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.history.AsOfPlan;
import ai.foresignal.store.history.AsOfResolver;
import ai.foresignal.store.tiles.AsOfTileQueryService;
import java.time.Clock;
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
import java.util.List;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.restrict.RestrictedRead;
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

    /** The canonical {@code asOf} of an asOf tile (prompt 037). */
    public static final String AS_OF_HEADER = "Foresignal-As-Of";

    /** {@code S(t)} of an asOf tile. */
    public static final String AS_OF_SEQ_HEADER = "Foresignal-As-Of-Seq";

    /** {@code true} when an asOf tile is approximate (sampled sources or downsampled history). */
    public static final String APPROXIMATE_HEADER = "Foresignal-Approximate";

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
    private final AsOfTileQueryService asOfTiles;
    private final AsOfResolver asOf;
    private final Clock clock;

    /** Creates the controller. */
    public TilesController(
            TileLayerResolver layers,
            TileQueryService tiles,
            TileJsonMapper tileJson,
            ReadCallerResolver callers,
            AppSettingService settings,
            TileProperties properties,
            JsonMapper json,
            AsOfTileQueryService asOfTiles,
            AsOfResolver asOf,
            Clock clock) {
        this.layers = layers;
        this.tiles = tiles;
        this.tileJson = tileJson;
        this.callers = callers;
        this.settings = settings;
        this.properties = properties;
        this.json = json;
        this.asOfTiles = asOfTiles;
        this.asOf = asOf;
        this.clock = clock;
    }

    /**
     * {@code getTile}: a Mapbox Vector Tile of current live state. {@code 200} with
     * {@code application/vnd.mapbox-vector-tile} even when empty; a strong {@code ETag} hashed from
     * the bytes and {@code 304} on {@code If-None-Match}; {@code Cache-Control max-age} of
     * {@code rawMaxAge} (5 s) for raw tiles and {@code aggMaxAge} (60 s) for aggregated ones,
     * {@code public} while {@code openReads} is on, else {@code private}.
     *
     * <p>With {@code asOf} (prompt 037) the tile shows the layer as known at that instant
     * ({@link AsOfTileQueryService}); other spellings of the instant are {@code 301} to the
     * canonical URL, the response carries {@code Foresignal-As-Of}, {@code Foresignal-As-Of-Seq} and
     * {@code Foresignal-Approximate}, and caching follows {@link TimeTravelCache}.
     */
    @GetMapping("/v1/tiles/{layer}/{z}/{x}/{y}.mvt")
    @Operation(operationId = "getTile", tags = "Tiles")
    @RestrictedRead("predicate inside the MVT SQL; Cache-Control private when restricted")
    public ResponseEntity<byte[]> getTile(
            @PathVariable String layer,
            @PathVariable String z,
            @PathVariable String x,
            @PathVariable String y,
            @RequestParam(required = false) String agg,
            @RequestParam(name = AsOfParam.NAME, required = false) String asOfText,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch,
            HttpServletRequest request) {
        TileCoordinates coordinates = TileCoordinates.parse(z, x, y);
        AggMode mode = AggMode.parse(agg);
        AsOfParam at = AsOfParam.parse(asOfText);
        if (at != null && at.redirect()) {
            return redirect(request, at);
        }
        // place=, view= and inWatchlist= narrow the caller's restriction (prompt 036).
        ReadCaller caller = callers.resolveScoped(request);
        Set<String> excluded = callers.excludedSources(caller);
        TileLayer resolved = allowed(layers.resolve(layer, excluded), caller.restriction());
        if (at != null) {
            return asOfTile(resolved, coordinates, mode, at, caller, excluded, ifNoneMatch);
        }
        TileResult result = tiles.render(resolved, coordinates, mode, caller.restriction());

        String etag = ETags.forBytes(result.mvt());
        Duration maxAge = result.shape().aggregated() ? properties.aggMaxAge() : properties.rawMaxAge();
        HttpHeaders headers = headers(etag, caller.publicCache(), maxAge);
        if (result.truncated()) {
            headers.set(TRUNCATED_HEADER, "true");
        }
        if (ETags.matches(ifNoneMatch, etag)) {
            return ResponseEntity.status(304).headers(headers).build();
        }
        return ResponseEntity.ok().headers(headers).contentType(MVT).body(result.mvt());
    }

    /** The asOf branch of {@link #getTile}. */
    private ResponseEntity<byte[]> asOfTile(TileLayer layer, TileCoordinates coordinates, AggMode mode, AsOfParam at,
            ReadCaller caller, Set<String> excluded, String ifNoneMatch) {
        AsOfPlan plan = asOf.plan(at.instant(), layer.sourceIds(), excluded);
        TileResult result = asOfTiles.render(layer, coordinates, mode, plan, caller.restriction());
        String etag = ETags.forBytes(result.mvt());
        boolean immutable = TimeTravelCache.immutable(at.instant(), clock.instant(), plan.horizon(), asOf.settled(plan));
        HttpHeaders headers = new HttpHeaders();
        headers.setETag(etag);
        headers.setCacheControl(TimeTravelCache.cacheControl(immutable, publicAsOf(caller, layer)));
        headers.set(HttpHeaders.VARY, VARY);
        headers.set(AS_OF_HEADER, at.canonical());
        headers.set(AS_OF_SEQ_HEADER, Long.toString(plan.s()));
        headers.set(APPROXIMATE_HEADER, Boolean.toString(plan.approximate()));
        if (result.truncated()) {
            headers.set(TRUNCATED_HEADER, "true");
        }
        if (ETags.matches(ifNoneMatch, etag)) {
            return ResponseEntity.status(304).headers(headers).build();
        }
        return ResponseEntity.ok().headers(headers).contentType(MVT).body(result.mvt());
    }

    /**
     * True when an asOf response may be {@code public}: {@code openReads} covers the path for this
     * caller and the layer has no private source.
     */
    public static boolean publicAsOf(ReadCaller caller, TileLayer layer) {
        return caller.publicCache() && layer.sources().stream().noneMatch(SourceDescriptor::privateSource);
    }

    /** {@code 301} to the canonical {@code asOf} spelling. */
    static <T> ResponseEntity<T> redirect(HttpServletRequest request, AsOfParam at) {
        return ResponseEntity.status(301)
                .header(HttpHeaders.LOCATION, AsOfParam.redirectLocation(request, at.canonical()))
                .build();
    }

    /** {@code listTileLayers}: every source id and kind the caller can see, with its TileJSON URL. */
    @GetMapping("/v1/tiles.json")
    @Operation(operationId = "listTileLayers", tags = "Tiles")
    @RestrictedRead("layers outside the restriction's sources and kinds are left out")
    public ResponseEntity<String> listTileLayers(
            @RequestParam(name = AsOfParam.NAME, required = false) String asOfText,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch,
            HttpServletRequest request) {
        AsOfParam at = AsOfParam.parse(asOfText);
        if (at != null && at.redirect()) {
            return redirect(request, at);
        }
        if (at != null && at.instant().isAfter(clock.instant())) {
            throw new ValidationException(ErrorCodes.ASOF_INVALID, "asOf must not be in the future.", List.of());
        }
        ReadCaller caller = callers.resolve(request);
        Set<String> excluded = callers.excludedSources(caller);
        List<TileLayer> visible = layers.layers(excluded).stream()
                .filter(l -> layerAllowed(l, caller.restriction()))
                .toList();
        return document(tileJson.index(visible, baseUrl(request), at == null ? null : at.canonical()), caller,
                ifNoneMatch);
    }

    /** {@code getTileJson}: the TileJSON 3.0.0 document of one layer; {@code 404 UNKNOWN_LAYER}. */
    @GetMapping("/v1/tiles/{layer}.json")
    @Operation(operationId = "getTileJson", tags = "Tiles")
    @RestrictedRead("a layer outside the restriction is UNKNOWN_LAYER")
    public ResponseEntity<String> getTileJson(
            @PathVariable String layer,
            @RequestParam(name = AsOfParam.NAME, required = false) String asOfText,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch,
            HttpServletRequest request) {
        AsOfParam at = AsOfParam.parse(asOfText);
        if (at != null && at.redirect()) {
            return redirect(request, at);
        }
        ReadCaller caller = callers.resolve(request);
        Set<String> excluded = callers.excludedSources(caller);
        TileLayer resolved = allowed(layers.resolve(layer, excluded), caller.restriction());
        if (at != null) {
            // Validates the instant against the layer's sources (future, retention horizon).
            asOf.plan(at.instant(), resolved.sourceIds(), excluded);
            return document(tileJson.asOfTileJson(resolved, baseUrl(request), at.canonical()), caller, ifNoneMatch);
        }
        return document(tileJson.tileJson(resolved, baseUrl(request)), caller, ifNoneMatch);
    }

    /**
     * {@code layer}, or {@code 404 UNKNOWN_LAYER} when a kind layer's kind is outside the caller's
     * kind restriction (source layers are already hidden through the excluded sources).
     */
    static TileLayer allowed(TileLayer layer, ReadRestriction restriction) {
        if (!layerAllowed(layer, restriction)) {
            throw ai.foresignal.store.tiles.TileLayerResolver.unknownLayer(layer.id());
        }
        return layer;
    }

    /** True when {@code layer} can hold records the restriction allows. */
    static boolean layerAllowed(TileLayer layer, ReadRestriction restriction) {
        if (layer.type() == ai.foresignal.store.tiles.TileLayerType.SOURCE) {
            return restriction.allowsSource(layer.id());
        }
        return restriction.allowsKind(layer.id());
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
        HttpHeaders headers = headers(etag, caller.publicCache(), TILEJSON_MAX_AGE);
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
