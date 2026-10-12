package ai.foresignal.api;

import ai.foresignal.api.mapper.TileJsonMapper;
import ai.foresignal.api.support.ETags;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.security.restrict.ReadScope;
import ai.foresignal.security.restrict.RestrictedRead;
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
import ai.foresignal.stream.sse.ChangeFilter;
import ai.foresignal.stream.sse.ChangesTopic;
import ai.foresignal.stream.sse.StreamPrincipal;
import ai.foresignal.stream.sse.StreamPrincipalResolver;
import ai.foresignal.stream.sse.TopicParams;
import ai.foresignal.view.SavedViewEntity;
import ai.foresignal.view.SavedViewMapper;
import ai.foresignal.view.SavedViewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Saved views (OpenAPI tag {@code Views}, prompt 036): definitions (CRUD) and their renderings
 * through records, tiles and SSE. A view is addressed by id or slug.
 *
 * <p>Definitions need {@code read} (open while {@code openReads} is on); writes need {@code agent}.
 * Renderings follow the normal data-read rules, except that a view referencing a place or a
 * watchlist needs a token ({@code 401}). Every rendering runs the view's predicate through the
 * caller's restriction ({@code RestrictionClamp}), so it is intersected with the 035 restriction and
 * renders the same set in records, tiles and the stream.
 */
@RestController
@Tag(name = "Views")
public class ViewsController {

    static final int MAX_LIMIT = 500;

    private final SavedViewService views;
    private final SavedViewMapper mapper;
    private final RecordsController records;
    private final ReadCallerResolver callers;
    private final TileLayerResolver layers;
    private final TileQueryService tiles;
    private final TileJsonMapper tileJson;
    private final TileProperties tileProperties;
    private final AppSettingService settings;
    private final StreamController streams;
    private final ChangesTopic changes;
    private final StreamPrincipalResolver principals;
    private final JsonMapper json;

    /** Creates the controller. */
    public ViewsController(SavedViewService views, SavedViewMapper mapper, RecordsController records,
            ReadCallerResolver callers, TileLayerResolver layers, TileQueryService tiles, TileJsonMapper tileJson,
            TileProperties tileProperties, AppSettingService settings, StreamController streams, ChangesTopic changes,
            StreamPrincipalResolver principals, JsonMapper json) {
        this.views = views;
        this.mapper = mapper;
        this.records = records;
        this.callers = callers;
        this.layers = layers;
        this.tiles = tiles;
        this.tileJson = tileJson;
        this.tileProperties = tileProperties;
        this.settings = settings;
        this.streams = streams;
        this.changes = changes;
        this.principals = principals;
        this.json = json;
    }

    // ------------------------------------------------------------------ definitions

    /** {@code listViews}: by slug. */
    @GetMapping("/v1/views")
    @Operation(operationId = "listViews", tags = "Views")
    public Map<String, Object> listViews(@RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit) {
        return mapper.page(views.list(cursor, QueryParams.intInRange("limit", limit, 1, MAX_LIMIT, 100)));
    }

    /** {@code createView}: 201; 409 {@code VIEW_EXISTS} when the slug is taken. */
    @PostMapping("/v1/views")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createView", tags = "Views")
    public Map<String, Object> createView(@RequestBody JsonNode body) {
        return mapper.view(views.create(body));
    }

    /** {@code getView}: 404 {@code UNKNOWN_VIEW}. */
    @GetMapping("/v1/views/{viewId}")
    @Operation(operationId = "getView", tags = "Views")
    public Map<String, Object> getView(@PathVariable String viewId) {
        return mapper.view(views.get(viewId));
    }

    /** {@code updateView}: bumps the version (tile caches key on it). */
    @PutMapping("/v1/views/{viewId}")
    @Operation(operationId = "updateView", tags = "Views")
    public Map<String, Object> updateView(@PathVariable String viewId, @RequestBody JsonNode body) {
        return mapper.view(views.update(viewId, body));
    }

    /** {@code deleteView}: 204. */
    @DeleteMapping("/v1/views/{viewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteView", tags = "Views")
    public void deleteView(@PathVariable String viewId) {
        views.delete(viewId);
    }

    // ------------------------------------------------------------------ renderings

    /**
     * {@code getViewRecords}: the view's current records in every 014 format, keyset-paged, with its
     * {@code fields} projection (bound {@code payload #> :path} lookups) and expiry choice.
     */
    @GetMapping("/v1/views/{viewId}/records")
    @Operation(operationId = "getViewRecords", tags = "Views")
    @RestrictedRead("the view's predicate runs through the caller's restriction")
    public ResponseEntity<Object> getViewRecords(
            @PathVariable String viewId,
            @RequestParam(required = false) String format,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit,
            @RequestHeader(value = HttpHeaders.ACCEPT, required = false) String accept,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        SavedViewEntity view = views.get(viewId);
        int size = QueryParams.intInRange("limit", limit, 1, MAX_LIMIT, 100);
        SavedViewService.CompiledView compiled = views.compile(view);
        ReadScope scope = new ReadScope(List.of(), null, view.getId().toString(), null);
        return records.listRecords(scope, null, null, null, null, null, null, null, null, view.getTimeField(), null,
                Boolean.toString(view.isIncludeExpired()), null, null, null, null, null, compiled.fieldsParam(), null,
                null, format, cursor, Integer.toString(size), accept, ifNoneMatch, request, response);
    }

    /**
     * {@code getViewTile}: a Mapbox Vector Tile of the view's current records (016 properties); the
     * {@code ETag} includes the view version.
     */
    @GetMapping("/v1/views/{viewId}/tiles/{z}/{x}/{y}.mvt")
    @Operation(operationId = "getViewTile", tags = "Views")
    @RestrictedRead("the view's predicate and the restriction are inside the MVT SQL")
    public ResponseEntity<byte[]> getViewTile(
            @PathVariable String viewId,
            @PathVariable String z,
            @PathVariable String x,
            @PathVariable String y,
            @RequestParam(required = false) String agg,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch,
            HttpServletRequest request) {
        SavedViewEntity view = views.get(viewId);
        TileCoordinates coordinates = TileCoordinates.parse(z, x, y);
        AggMode mode = AggMode.parse(agg);
        ReadCaller caller = callers.resolveScoped(request, new ReadScope(List.of(), null, view.getId().toString(), null));
        TileLayer layer = layers.viewLayer(view.getSlug(), view.getName(), view.getSources(),
                callers.excludedSources(caller));
        TileResult result = tiles.render(layer, coordinates, mode, caller.restriction());
        String etag = "\"v" + view.getVersion() + "-" + ETags.forBytes(result.mvt()).replace("\"", "") + "\"";
        Duration maxAge = result.shape().aggregated() ? tileProperties.aggMaxAge() : tileProperties.rawMaxAge();
        HttpHeaders headers = new HttpHeaders();
        headers.setETag(etag);
        headers.setCacheControl(TilesController.cacheControl(caller.publicCache(), maxAge));
        headers.set(HttpHeaders.VARY, TilesController.VARY);
        if (result.truncated()) {
            headers.set(TilesController.TRUNCATED_HEADER, "true");
        }
        if (ETags.matches(ifNoneMatch, etag)) {
            return ResponseEntity.status(304).headers(headers).build();
        }
        return ResponseEntity.ok().headers(headers).contentType(TilesController.MVT).body(result.mvt());
    }

    /** {@code getViewTileJson}: TileJSON 3.0.0 whose tile template carries the view version. */
    @GetMapping("/v1/views/{viewId}/tiles.json")
    @Operation(operationId = "getViewTileJson", tags = "Views")
    @RestrictedRead("layers outside the restriction's sources are left out of the view layer")
    public ResponseEntity<String> getViewTileJson(
            @PathVariable String viewId,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch,
            HttpServletRequest request) {
        SavedViewEntity view = views.get(viewId);
        ReadCaller caller = callers.resolveScoped(request, new ReadScope(List.of(), null, view.getId().toString(), null));
        TileLayer layer = layers.viewLayer(view.getSlug(), view.getName(), view.getSources(),
                callers.excludedSources(caller));
        String base = baseUrl(request);
        String template = base + "/v1/views/" + view.getId() + "/tiles/{z}/{x}/{y}.mvt?v=" + view.getVersion();
        String text = json.writeValueAsString(tileJson.tileJson(layer, base, template));
        String etag = ETags.forDocument(text);
        HttpHeaders headers = new HttpHeaders();
        headers.setETag(etag);
        headers.setCacheControl(TilesController.cacheControl(caller.publicCache(), Duration.ofSeconds(300)));
        headers.set(HttpHeaders.VARY, TilesController.VARY);
        if (ETags.matches(ifNoneMatch, etag)) {
            return ResponseEntity.status(304).headers(headers).build();
        }
        return ResponseEntity.ok().headers(headers)
                .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)).body(text);
    }

    /**
     * {@code streamView}: 015 SSE (replay from {@code Last-Event-ID}/{@code after}, live and touch
     * lanes, resync and reset) filtered by the view's predicate; a version that stops matching is
     * delivered with {@code leftFilter: true}.
     */
    @GetMapping("/v1/views/{viewId}/stream")
    @Operation(operationId = "streamView", tags = "Views")
    @RestrictedRead("the view's predicate runs through the stream filter's restriction")
    public SseEmitter streamView(
            @PathVariable String viewId,
            @RequestHeader(value = StreamController.LAST_EVENT_ID, required = false) String lastEventId,
            @RequestParam(required = false) String after,
            HttpServletRequest request,
            HttpServletResponse response) {
        SavedViewEntity view = views.get(viewId);
        StreamPrincipal principal = principals.resolve(request);
        Map<String, List<String>> params = new HashMap<>(StreamController.parameters(request));
        params.keySet().removeAll(List.of("place", "inWatchlist", "after", "ticket"));
        params.put("view", List.of(view.getId().toString()));
        ChangeFilter filter = changes.parseQuery(new TopicParams("", params), principal);
        return streams.openChanges(principal, filter, lastEventId, after, response);
    }

    /** {@code server.publicBaseUrl} when set, else the request's own origin. */
    private String baseUrl(HttpServletRequest request) {
        ServerSettings server = settings.getOrDefault(SettingKeys.SERVER, ServerSettings.class, ServerSettings::defaults);
        String configured = server.normalizedPublicBaseUrl();
        if (configured != null) {
            return configured;
        }
        return TilesController.requestBase(request.getScheme(), request.getServerName(), request.getServerPort());
    }
}
