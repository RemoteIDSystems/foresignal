package ai.foresignal.api;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.BboxFilter;
import ai.foresignal.api.support.BboxParser;
import ai.foresignal.api.support.PublicUrls;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.ogc.OgcDatetime;
import ai.foresignal.ogc.OgcFeatureDocuments;
import ai.foresignal.ogc.OgcFeatureService;
import ai.foresignal.ogc.cql2.Cql2;
import ai.foresignal.ogc.cql2.Cql2Exception;
import ai.foresignal.ogc.cql2.Cql2JsonParser;
import ai.foresignal.ogc.cql2.Cql2TextParser;
import ai.foresignal.provenance.AttributionService;
import ai.foresignal.provenance.LicencePolicy;
import ai.foresignal.provenance.LicencePolicyService;
import ai.foresignal.security.restrict.RestrictedRead;
import ai.foresignal.settings.AppSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeMap;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * OGC API &ndash; Features items, single features and queryables (prompt 112, base {@code /v1/ogc}): GeoJSON
 * feature collections of current records per kind and of versions per {@code {kind}-history} collection, with
 * basic CQL2 filters in {@code cql2-text} or {@code cql2-json}. The landing page, conformance and the collection
 * documents are {@link OgcController}'s.
 *
 * <p>Unknown query parameters are refused with {@code 400} (17-069r4 §7.6 permits it): a typo never silently
 * returns unfiltered data. {@code f=json} and {@code f=geojson} are tolerated because some clients append them.
 * {@code bbox-crs} and {@code filter-crs} accept only CRS84. Private sources are excluded unless the caller may
 * see them, restricted tokens and feed keys apply ({@link RestrictedRead}), and the license policy prunes rows
 * and sets {@code Foresignal-Attribution} and {@code Link rel="license"} (109).
 *
 * <p>Left out in edge mode (prompt 113: OGC).
 */
@RestController
@Tag(name = "OGC")
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = ai.foresignal.config.EdgeMode.PROPERTY,
        havingValue = ai.foresignal.config.EdgeMode.FULL, matchIfMissing = true)
public class OgcFeaturesController {

    static final MediaType GEO_JSON = MediaType.parseMediaType(OgcFeatureDocuments.GEOJSON);
    static final MediaType SCHEMA_JSON = MediaType.parseMediaType(OgcFeatureDocuments.SCHEMA_JSON);

    /** The parameters {@code items} accepts. */
    static final Set<String> ITEM_PARAMS = Set.of("limit", "bbox", "bbox-crs", "datetime", "filter", "filter-lang",
            "filter-crs", "count", "cursor", "source", "licencePolicy", "f");

    /** The parameters a single feature accepts. */
    static final Set<String> FEATURE_PARAMS = Set.of("licencePolicy", "f");

    static final int DEFAULT_LIMIT = 100;
    static final int MAX_LIMIT = 10_000;

    private final OgcFeatureService features;
    private final ReadCallerResolver callers;
    private final AttributionService attributions;
    private final AppSettingService settings;
    private final Clock clock;

    /** Creates the controller. */
    public OgcFeaturesController(OgcFeatureService features, ReadCallerResolver callers,
            AttributionService attributions, AppSettingService settings, Clock clock) {
        this.features = features;
        this.callers = callers;
        this.attributions = attributions;
        this.settings = settings;
        this.clock = clock;
    }

    /** {@code ogcItems}: a page of features of a collection. */
    @GetMapping("/v1/ogc/collections/{collectionId}/items")
    @Operation(operationId = "ogcItems", tags = "OGC")
    @RestrictedRead("feature queries add the caller's restriction through RestrictionClamp")
    public ResponseEntity<Map<String, Object>> ogcItems(@PathVariable String collectionId,
            @RequestParam(required = false) String limit, @RequestParam(required = false) String bbox,
            @RequestParam(name = "bbox-crs", required = false) String bboxCrs,
            @RequestParam(required = false) String datetime, @RequestParam(required = false) String filter,
            @RequestParam(name = "filter-lang", required = false) String filterLang,
            @RequestParam(name = "filter-crs", required = false) String filterCrs,
            @RequestParam(required = false) String count, @RequestParam(required = false) String cursor,
            @RequestParam(required = false) List<String> source,
            @RequestParam(required = false) List<String> licencePolicy, HttpServletRequest request) {
        checkParams(request, ITEM_PARAMS);
        checkCrs("bbox-crs", bboxCrs);
        checkCrs("filter-crs", filterCrs);
        checkFormat(request);
        ReadCaller caller = callers.resolve(request);
        Set<String> excluded = callers.excludedSources(caller);
        OgcFeatureService.FeatureCollection collection = features.require(collectionId, excluded, caller.restriction());
        int size = QueryParams.intInRange("limit", limit, 1, MAX_LIMIT, DEFAULT_LIMIT);
        BboxFilter box = parseBbox(bbox);
        OgcDatetime time = OgcDatetime.parse(datetime);
        Cql2.Expr parsed = parseFilter(filter, filterLang);
        boolean withCount = "true".equalsIgnoreCase(count);
        if (count != null && !withCount && !"false".equalsIgnoreCase(count)) {
            throw ValidationException.forField("count", "must be true or false");
        }
        List<String> sources = QueryParams.multi("source", source);
        LicencePolicy policy = LicencePolicyService.of(request);
        OgcFeatureService.ItemsPage page;
        try {
            page = features.items(new OgcFeatureService.ItemsQuery(collection, size, box, time, parsed,
                    sources == null || sources.isEmpty() ? null : sources, cursor, withCount, excluded,
                    caller.restriction(), policy, canonical(collectionId, request)));
        } catch (Cql2Exception e) {
            throw filterInvalid(e);
        }
        PublicUrls urls = PublicUrls.of(settings, request);
        String path = "/v1/ogc/collections/" + collectionId + "/items";
        String self = urls.api(path) + query(request, null);
        String next = page.nextCursor() == null ? null : urls.api(path) + query(request, page.nextCursor());
        HttpHeaders headers = licenceHeaders(page.attributions());
        if (next != null) {
            headers.add(HttpHeaders.LINK, "<" + next + ">; rel=\"next\"; type=\"" + OgcFeatureDocuments.GEOJSON + "\"");
        }
        return ResponseEntity.ok().headers(headers).contentType(GEO_JSON)
                .body(OgcFeatureDocuments.items(urls, collection, page, self, next, clock.instant()));
    }

    /** {@code ogcItem}: one feature. */
    @GetMapping("/v1/ogc/collections/{collectionId}/items/{featureId}")
    @Operation(operationId = "ogcItem", tags = "OGC")
    @RestrictedRead("the single-feature query adds the caller's restriction through RestrictionClamp")
    public ResponseEntity<Map<String, Object>> ogcItem(@PathVariable String collectionId, @PathVariable String featureId,
            @RequestParam(required = false) List<String> licencePolicy, HttpServletRequest request) {
        checkParams(request, FEATURE_PARAMS);
        checkFormat(request);
        ReadCaller caller = callers.resolve(request);
        Set<String> excluded = callers.excludedSources(caller);
        OgcFeatureService.FeatureCollection collection = features.require(collectionId, excluded, caller.restriction());
        OgcFeatureService.ItemsPage page = features.item(collection, featureId, excluded, caller.restriction(),
                LicencePolicyService.of(request)).orElseThrow(() -> new NotFoundException(ErrorCodes.NOT_FOUND,
                        "Unknown feature " + featureId + " in " + collectionId + "."));
        PublicUrls urls = PublicUrls.of(settings, request);
        return ResponseEntity.ok().headers(licenceHeaders(page.attributions())).contentType(GEO_JSON)
                .body(OgcFeatureDocuments.feature(urls, collection, page.features().getFirst(), true));
    }

    /** {@code ogcQueryables}: the queryables of a collection as a JSON Schema. */
    @GetMapping("/v1/ogc/collections/{collectionId}/queryables")
    @Operation(operationId = "ogcQueryables", tags = "OGC")
    public ResponseEntity<Map<String, Object>> ogcQueryables(@PathVariable String collectionId,
            HttpServletRequest request) {
        ReadCaller caller = callers.resolve(request);
        OgcFeatureService.FeatureCollection collection = features.require(collectionId,
                callers.excludedSources(caller), caller.restriction());
        PublicUrls urls = PublicUrls.of(settings, request);
        return ResponseEntity.ok().contentType(SCHEMA_JSON).body(OgcFeatureDocuments.queryables(urls, collection,
                features.fieldSpecs(collection.kind(), collection.sources())));
    }

    // ------------------------------------------------------------------------------------ helpers

    /** Parses a CQL2 filter in its language; a parse error is {@code 400 FILTER_INVALID} with the position. */
    static Cql2.Expr parseFilter(String filter, String filterLang) {
        if (filter == null || filter.isBlank()) {
            if (filterLang != null && !filterLang.isBlank()) {
                language(filterLang);
            }
            return null;
        }
        try {
            return switch (language(filterLang)) {
                case "cql2-json" -> Cql2JsonParser.parse(filter);
                default -> Cql2TextParser.parse(filter);
            };
        } catch (Cql2Exception e) {
            throw filterInvalid(e);
        }
    }

    private static String language(String filterLang) {
        String lang = filterLang == null || filterLang.isBlank() ? "cql2-text" : filterLang.strip().toLowerCase(Locale.ROOT);
        if (!lang.equals("cql2-text") && !lang.equals("cql2-json")) {
            throw ValidationException.forField("filter-lang", "must be cql2-text or cql2-json");
        }
        return lang;
    }

    static ForesignalException filterInvalid(Cql2Exception e) {
        ForesignalException ex = new ForesignalException(HttpStatus.BAD_REQUEST, ErrorCodes.FILTER_INVALID,
                e.getMessage());
        return e.position() == null ? ex : ex.withProperty("position", e.position());
    }

    /** Refuses parameters outside {@code allowed} (OGC permits a server to reject unknown parameters). */
    static void checkParams(HttpServletRequest request, Set<String> allowed) {
        for (String name : request.getParameterMap().keySet()) {
            if (!allowed.contains(name)) {
                throw ValidationException.forField(name, "is not a parameter of this operation; allowed: "
                        + String.join(", ", new java.util.TreeSet<>(allowed)));
            }
        }
    }

    static void checkCrs(String name, String value) {
        if (value != null && !value.isBlank() && !OgcFeatureDocuments.CRS84.equals(value.strip())) {
            throw ValidationException.forField(name, "only " + OgcFeatureDocuments.CRS84 + " is supported");
        }
    }

    private static void checkFormat(HttpServletRequest request) {
        String f = request.getParameter("f");
        if (f != null && !f.equalsIgnoreCase("json") && !f.equalsIgnoreCase("geojson")) {
            throw ValidationException.forField("f", "only json and geojson are offered");
        }
    }

    /** Parses 4 or 6 numbers ({@code minLon,minLat[,minZ],maxLon,maxLat[,maxZ]}); heights are ignored. */
    static BboxFilter parseBbox(String bbox) {
        if (bbox == null || bbox.isBlank()) {
            return null;
        }
        String[] parts = bbox.split(",", -1);
        if (parts.length == 6) {
            return BboxParser.parse(parts[0] + "," + parts[1] + "," + parts[3] + "," + parts[4]);
        }
        if (parts.length != 4) {
            throw ValidationException.forField("bbox", "must have 4 or 6 numbers");
        }
        return BboxParser.parse(bbox);
    }

    private HttpHeaders licenceHeaders(List<AttributionService.Attribution> attrs) {
        HttpHeaders headers = new HttpHeaders();
        String text = attributions.header(attrs);
        if (text != null && !text.isEmpty()) {
            headers.set(RecordsController.ATTRIBUTION_HEADER, text);
        }
        for (String url : attributions.licenceLinks(attrs)) {
            headers.add(HttpHeaders.LINK, "<" + url + ">; rel=\"license\"");
        }
        headers.add(HttpHeaders.VARY, LicencePolicyService.POLICY_HEADER);
        return headers;
    }

    /** The query's canonical form (sorted parameters without the cursor), for the cursor fingerprint. */
    static String canonical(String collectionId, HttpServletRequest request) {
        StringJoiner out = new StringJoiner("&", collectionId + "?", "");
        new TreeMap<>(request.getParameterMap()).forEach((k, v) -> {
            if (!"cursor".equals(k) && !"count".equals(k)) {
                out.add(k + "=" + String.join(",", v));
            }
        });
        return out.toString();
    }

    /** The request's query string with {@code cursor} replaced by {@code cursor} (or dropped when null). */
    static String query(HttpServletRequest request, String cursor) {
        StringJoiner out = new StringJoiner("&", "?", "").setEmptyValue("");
        request.getParameterMap().forEach((k, values) -> {
            if (!"cursor".equals(k)) {
                for (String v : values) {
                    out.add(encode(k) + "=" + encode(v));
                }
            }
        });
        if (cursor != null) {
            out.add("cursor=" + encode(cursor));
        }
        return out.toString();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
