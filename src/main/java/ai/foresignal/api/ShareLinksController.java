package ai.foresignal.api;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.PublicUrls;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.export.ExportJobEntity;
import ai.foresignal.export.ExportJobService;
import ai.foresignal.provenance.AttributionService;
import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.restrict.RequestPrincipal;
import ai.foresignal.security.restrict.RestrictedRead;
import ai.foresignal.security.visibility.Viewer;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.ClientLinks;
import ai.foresignal.settings.ServerSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.share.ShareLinkEntity;
import ai.foresignal.share.ShareLinkService;
import ai.foresignal.store.tiles.AggMode;
import ai.foresignal.store.tiles.TileCoordinates;
import ai.foresignal.store.tiles.TileLayer;
import ai.foresignal.store.tiles.TileLayerResolver;
import ai.foresignal.store.tiles.TileQueryService;
import ai.foresignal.store.tiles.TileResult;
import ai.foresignal.view.SavedViewEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * Share links (prompt 112): {@code createShareLink}, {@code listShareLinks}, {@code revokeShareLink} (agent scope;
 * private targets need admin) and the key's own URLs {@code openShareLink}, {@code openShareLinkGeoJson},
 * {@code getShareLinkTile} and {@code downloadShareLink} ({@code open}: the {@code fss_} key in the path is the
 * credential, checked by {@code PathKeyFilter} with {@code ShareKeyResolver}; it authorizes nothing else).
 *
 * <p>Every key URL counts one use ({@code 410 SHARE_EXPIRED} when revoked, expired or exhausted; 60 uses per
 * minute per link) and answers JSON or GeoJSON, never HTML (Charter: no UI). With {@code ?open=1} and
 * {@code clientLinks.share} configured the key URL redirects (302) to the client template.
 */
@RestController
@Tag(name = "ShareLinks")
public class ShareLinksController {

    static final MediaType GEO_JSON = MediaType.parseMediaType("application/geo+json");

    private final ShareLinkService links;
    private final ReadCallerResolver callers;
    private final AppSettingService settings;
    private final AttributionService attributions;
    private final TileLayerResolver layers;
    private final TileQueryService tiles;
    private final ExportJobService exports;
    private final Clock clock;

    /** Creates the controller. */
    public ShareLinksController(ShareLinkService links, ReadCallerResolver callers, AppSettingService settings,
            AttributionService attributions, TileLayerResolver layers, TileQueryService tiles, ExportJobService exports,
            Clock clock) {
        this.links = links;
        this.callers = callers;
        this.settings = settings;
        this.attributions = attributions;
        this.layers = layers;
        this.tiles = tiles;
        this.exports = exports;
        this.clock = clock;
    }

    // --------------------------------------------------------------------------------- management

    /** {@code createShareLink}: creates a link; the plaintext key is in this response only. */
    @PostMapping("/v1/share-links")
    @Operation(operationId = "createShareLink", tags = "ShareLinks")
    public ResponseEntity<Map<String, Object>> createShareLink(@RequestBody JsonNode body, HttpServletRequest request) {
        ShareLinkService.Create create = parseCreate(body);
        ShareLinkService.Created created = links.create(create, caller(request, callers));
        Map<String, Object> out = view(created.link());
        out.put("key", created.key());
        out.put("url", PublicUrls.base(settings, request) + "/v1/k/" + created.key());
        return ResponseEntity.status(HttpStatus.CREATED).body(out);
    }

    /** {@code listShareLinks}: the caller's links, or every link for admin. */
    @GetMapping("/v1/share-links")
    @Operation(operationId = "listShareLinks", tags = "ShareLinks")
    public Map<String, Object> listShareLinks(@RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit, HttpServletRequest request) {
        int size = QueryParams.intInRange("limit", limit, 1, 500, 100);
        ShareLinkService.Page page = links.list(caller(request, callers), cursor, size);
        List<Map<String, Object>> items = new ArrayList<>();
        for (ShareLinkEntity link : page.items()) {
            items.add(view(link));
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("nextCursor", page.nextCursor());
        return out;
    }

    /** {@code revokeShareLink}: revokes a link. */
    @DeleteMapping("/v1/share-links/{shareLinkId}")
    @Operation(operationId = "revokeShareLink", tags = "ShareLinks")
    public ResponseEntity<Void> revokeShareLink(@PathVariable String shareLinkId, HttpServletRequest request) {
        links.revoke(shareLinkId, caller(request, callers));
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------------------- key URLs

    /** {@code openShareLink}: the shared target as JSON, GeoJSON by {@code Accept}, or a redirect with {@code open=1}. */
    @GetMapping("/v1/k/{key}")
    @Operation(operationId = "openShareLink", tags = "ShareLinks")
    @RestrictedRead("rendered within the creator's restriction snapshot; private and non-redistributable records removed")
    public ResponseEntity<Map<String, Object>> openShareLink(@PathVariable String key,
            @RequestParam(required = false) String open,
            @RequestHeader(value = HttpHeaders.ACCEPT, required = false) String accept, HttpServletRequest request) {
        if (open != null && !"1".equals(open)) {
            throw ValidationException.forField("open", "must be 1");
        }
        ShareLinkEntity link = links.use(key, request.getRemoteAddr());
        if ("1".equals(open)) {
            ServerSettings server = settings.getOrDefault(SettingKeys.SERVER, ServerSettings.class,
                    ServerSettings::defaults);
            String template = server.clientLinks() == null ? null : server.clientLinks().share();
            if (template != null && !template.isBlank()) {
                String target = ClientLinks.fill(template, Map.of("key", URLEncoder.encode(key, StandardCharsets.UTF_8)));
                return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(target)).build();
            }
        }
        ShareLinkService.Rendered rendered = links.render(link);
        if (accept != null && accept.contains("application/geo+json")) {
            return geoJson(rendered);
        }
        String base = PublicUrls.base(settings, request) + "/v1/k/" + key;
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("targetType", link.getTargetType());
        out.put("title", rendered.title());
        out.put("expiresAt", link.getExpiresAt().toString());
        out.put("masked", link.isMasked());
        out.put("precisionM", link.getPrecisionM());
        out.put("data", rendered.data());
        out.put("attributions", AttributionService.asMaps(rendered.attributions()));
        Map<String, Object> sub = new LinkedHashMap<>();
        sub.put("geojson", base + "/geojson");
        sub.put("tiles", "view".equals(link.getTargetType()) && tilesOffered(link)
                ? base + "/tiles/{z}/{x}/{y}.mvt" : null);
        sub.put("download", "export".equals(link.getTargetType()) ? base + "/download" : null);
        out.put("links", sub);
        return ResponseEntity.ok().headers(attributionHeaders(rendered)).contentType(MediaType.APPLICATION_JSON)
                .body(out);
    }

    /** {@code openShareLinkGeoJson}: the shared target as a GeoJSON FeatureCollection. */
    @GetMapping("/v1/k/{key}/geojson")
    @Operation(operationId = "openShareLinkGeoJson", tags = "ShareLinks")
    @RestrictedRead("rendered within the creator's restriction snapshot; private and non-redistributable records removed")
    public ResponseEntity<Map<String, Object>> openShareLinkGeoJson(@PathVariable String key,
            HttpServletRequest request) {
        ShareLinkEntity link = links.use(key, request.getRemoteAddr());
        return geoJson(links.render(link));
    }

    /**
     * {@code getShareLinkTile}: an MVT tile of a shared view, within the creator's restriction and the view, without
     * private or non-redistributable sources. {@code 404} for other targets and for masked links (tiles cannot carry
     * the precision mask or the field allowlist of every property).
     */
    @GetMapping("/v1/k/{key}/tiles/{z}/{x}/{y}.mvt")
    @Operation(operationId = "getShareLinkTile", tags = "ShareLinks")
    @RestrictedRead("the creator's restriction snapshot narrowed by the view is inside the MVT SQL")
    public ResponseEntity<byte[]> getShareLinkTile(@PathVariable String key, @PathVariable String z,
            @PathVariable String x, @PathVariable String y, HttpServletRequest request) {
        TileCoordinates coordinates = TileCoordinates.parse(z, x, y);
        if (coordinates.z() > 16) {
            throw ValidationException.forField("z", "must be at most 16");
        }
        ShareLinkEntity link = links.use(key, request.getRemoteAddr());
        if (!"view".equals(link.getTargetType()) || !tilesOffered(link)) {
            throw new NotFoundException(ErrorCodes.NOT_FOUND, "This share link has no tiles.");
        }
        SavedViewEntity view = links.view(link);
        TileLayer layer = layers.viewLayer(view.getSlug(), view.getName(), view.getSources(), links.deniedSources());
        TileResult result = tiles.render(layer, coordinates, AggMode.parse(null), links.viewRestriction(link, view));
        return ResponseEntity.ok().contentType(TilesController.MVT).body(result.mvt());
    }

    /** {@code downloadShareLink}: the shared export's file, with {@code Range} support. */
    @GetMapping("/v1/k/{key}/download")
    @Operation(operationId = "downloadShareLink", tags = "ShareLinks")
    public ResponseEntity<FileSystemResource> downloadShareLink(@PathVariable String key, HttpServletRequest request) {
        ShareLinkEntity link = links.use(key, request.getRemoteAddr());
        if (!"export".equals(link.getTargetType())) {
            throw new NotFoundException(ErrorCodes.NOT_FOUND, "This share link has no download.");
        }
        links.render(link);
        ExportJobEntity job = links.export(link);
        ExportJobService.Download download = exports.download(job, null, false);
        return ResponseEntity.ok()
                .eTag("\"" + download.sha256() + "\"")
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(download.path().getFileName().toString()).build().toString())
                .contentType(MediaType.parseMediaType(download.mediaType()))
                .body(new FileSystemResource(download.path()));
    }

    // ------------------------------------------------------------------------------------ helpers

    private ResponseEntity<Map<String, Object>> geoJson(ShareLinkService.Rendered rendered) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("type", "FeatureCollection");
        out.put("features", rendered.features());
        out.put("attributions", AttributionService.asMaps(rendered.attributions()));
        return ResponseEntity.ok().headers(attributionHeaders(rendered)).contentType(GEO_JSON).body(out);
    }

    private HttpHeaders attributionHeaders(ShareLinkService.Rendered rendered) {
        HttpHeaders headers = new HttpHeaders();
        String text = attributions.header(rendered.attributions());
        if (text != null && !text.isEmpty()) {
            headers.set(RecordsController.ATTRIBUTION_HEADER, text);
        }
        for (String url : attributions.licenceLinks(rendered.attributions())) {
            headers.add(HttpHeaders.LINK, "<" + url + ">; rel=\"license\"");
        }
        return headers;
    }

    private static boolean tilesOffered(ShareLinkEntity link) {
        return !link.isMasked() && link.getPrecisionM() == null && link.getFieldMask() == null;
    }

    /** The {@code ShareLink} JSON of a link (without its key). */
    Map<String, Object> view(ShareLinkEntity link) {
        return ShareLinkService.json(link, clock.instant());
    }

    /** Parses a {@code ShareLinkCreate} body. */
    static ShareLinkService.Create parseCreate(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The body must be a ShareLinkCreate object.");
        }
        for (String name : body.propertyNames()) {
            if (!List.of("targetType", "targetRef", "fieldMask", "precisionM", "expiresAt", "maxUses").contains(name)) {
                throw ValidationException.forField(name, "is not a ShareLinkCreate member");
            }
        }
        String targetType = text(body, "targetType");
        String targetRef = body.has("targetRef") && body.get("targetRef").isNumber()
                ? body.get("targetRef").asString() : text(body, "targetRef");
        List<String> mask = null;
        JsonNode m = body.get("fieldMask");
        if (m != null && !m.isNull()) {
            if (!m.isArray()) {
                throw ValidationException.forField("fieldMask", "must be an array of strings");
            }
            mask = new ArrayList<>();
            for (JsonNode f : m) {
                if (!f.isString()) {
                    throw ValidationException.forField("fieldMask", "must be an array of strings");
                }
                mask.add(f.asString());
            }
        }
        Instant expiresAt = null;
        String expires = text(body, "expiresAt");
        if (expires != null) {
            try {
                expiresAt = OffsetDateTime.parse(expires).toInstant();
            } catch (DateTimeParseException e) {
                throw ValidationException.forField("expiresAt", "must be an RFC 3339 date-time");
            }
        }
        return new ShareLinkService.Create(targetType, targetRef, mask, integer(body, "precisionM"), expiresAt,
                integer(body, "maxUses"));
    }

    private static String text(JsonNode body, String name) {
        JsonNode v = body.get(name);
        if (v == null || v.isNull()) {
            return null;
        }
        if (!v.isString()) {
            throw ValidationException.forField(name, "must be a string");
        }
        return v.asString();
    }

    private static Integer integer(JsonNode body, String name) {
        JsonNode v = body.get(name);
        if (v == null || v.isNull()) {
            return null;
        }
        if (!v.isIntegralNumber() || !v.canConvertToInt()) {
            throw ValidationException.forField(name, "must be an integer");
        }
        return v.intValue();
    }

    /** The share-link caller of a request. */
    static ShareLinkService.Caller caller(HttpServletRequest request, ReadCallerResolver callers) {
        ReadCaller caller = callers.resolve(request);
        RequestPrincipal principal = RequestPrincipal.of(request).orElse(null);
        boolean admin = caller.scope() != null && caller.scope().includes(Scope.ADMIN);
        String tokenName = CurrentToken.of(request).map(CurrentToken::name).orElse(null);
        TokenRestrictions restrictions = CurrentToken.of(request).map(CurrentToken::restrictions).orElse(null);
        return new ShareLinkService.Caller(ExportCallers.tokenId(request), tokenName, admin, Viewer.of(principal),
                restrictions, caller.restriction(), callers.excludedSources(caller));
    }
}
