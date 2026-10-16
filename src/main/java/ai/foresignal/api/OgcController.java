package ai.foresignal.api;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.PublicUrls;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.raster.FrameKeys;
import ai.foresignal.raster.FrameResolver;
import ai.foresignal.raster.ProductRegistry;
import ai.foresignal.raster.RasterFrameQueries;
import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.raster.render.RasterCachePolicy;
import ai.foresignal.raster.render.RasterRenderService;
import ai.foresignal.raster.render.TileMatrixSet;
import ai.foresignal.raster.render.TileMatrixSets;
import ai.foresignal.raster.sample.EdrArea;
import ai.foresignal.raster.sample.FrameChoice;
import ai.foresignal.raster.sample.FrameView;
import ai.foresignal.raster.sample.SampleService;
import ai.foresignal.raster.sample.SampleValue;
import ai.foresignal.raster.sample.TimeSpec;
import ai.foresignal.security.visibility.SourceVisibility;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.source.spi.ProductSpec;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * OGC API &ndash; Tiles Part 1 and OGC API &ndash; EDR 1.1 for raster products (OpenAPI tag
 * {@code Products}, prompt 108; Weather-Imagery-Design §11), root {@code /v1/ogc}. Collections are
 * products (coverage collections); 112 will add feature collections under the same root, so the
 * landing and collection code stays extensible.
 *
 * <p>Tiles redirect concrete times to the content-addressed tile URL; EDR position and area answer
 * CoverageJSON matching {@code /v1/sample}.
 */
@RestController
@Tag(name = "Products")
public class OgcController {

    static final MediaType COVERAGE_JSON = MediaType.parseMediaType("application/prs.coverage+json");
    private static final Pattern POINT = Pattern.compile("POINT\\s*\\(\\s*([-0-9.eE]+)\\s+([-0-9.eE]+)\\s*\\)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern POLYGON = Pattern.compile("POLYGON\\s*\\(\\((.*)\\)\\)", Pattern.CASE_INSENSITIVE);
    private static final int MAX_AREA_CELLS = 100_000;

    private final ProductRegistry registry;
    private final FrameResolver resolver;
    private final RasterFrameQueries frames;
    private final SampleService samples;
    private final ReadCallerResolver callers;
    private final SourceVisibility visibility;
    private final AppSettingService settings;
    private final Clock clock;

    /** Creates the controller. */
    public OgcController(ProductRegistry registry, FrameResolver resolver, RasterFrameQueries frames,
            SampleService samples, ReadCallerResolver callers, SourceVisibility visibility, AppSettingService settings,
            Clock clock) {
        this.registry = registry;
        this.resolver = resolver;
        this.frames = frames;
        this.samples = samples;
        this.callers = callers;
        this.visibility = visibility;
        this.settings = settings;
        this.clock = clock;
    }

    /** {@code ogcLanding}: the OGC API landing page. */
    @GetMapping("/v1/ogc")
    @Operation(operationId = "ogcLanding", tags = "Products")
    public Map<String, Object> ogcLanding(HttpServletRequest request) {
        PublicUrls urls = PublicUrls.of(settings, request);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("title", "Foresignal OGC API");
        out.put("description", "Raster products as OGC API collections with map tiles (20-057) and"
                + " EDR queries (19-086r6).");
        out.put("attribution", null);
        out.put("links", List.of(
                link(urls.api("/v1/ogc"), "self", "application/json", "This document"),
                link(urls.api("/v1/ogc/conformance"), "conformance", "application/json", "Conformance classes"),
                link(urls.api("/v1/ogc/collections"), "data", "application/json", "Collections"),
                link(urls.api("/v1/ogc/tileMatrixSets"), "http://www.opengis.net/def/rel/ogc/1.0/tiling-schemes",
                        "application/json", "Tile matrix sets"),
                link(urls.api("/v1/ogc/api"), "service-desc", "application/vnd.oai.openapi+json;version=3.1",
                        "The OpenAPI document")));
        return out;
    }

    /** {@code ogcConformance}: the conformance classes. */
    @GetMapping("/v1/ogc/conformance")
    @Operation(operationId = "ogcConformance", tags = "Products")
    public Map<String, Object> ogcConformance() {
        return Map.of("conformsTo", List.of(
                "http://www.opengis.net/spec/ogcapi-tiles-1/1.0/conf/core",
                "http://www.opengis.net/spec/ogcapi-tiles-1/1.0/conf/tileset",
                "http://www.opengis.net/spec/ogcapi-tiles-1/1.0/conf/tilesets-list",
                "http://www.opengis.net/spec/ogcapi-common-2/1.0/conf/collections",
                "http://www.opengis.net/spec/ogcapi-edr-1/1.1/conf/core",
                "http://www.opengis.net/spec/ogcapi-edr-1/1.1/conf/position",
                "http://www.opengis.net/spec/ogcapi-edr-1/1.1/conf/area",
                "http://www.opengis.net/spec/ogcapi-edr-1/1.1/conf/instances"));
    }

    /** {@code ogcApiDefinition}: 302 to the OpenAPI document. */
    @GetMapping("/v1/ogc/api")
    @Operation(operationId = "ogcApiDefinition", tags = "Products")
    public ResponseEntity<Void> ogcApiDefinition(HttpServletRequest request) {
        PublicUrls urls = PublicUrls.of(settings, request);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(urls.api("/v1/openapi.json"))).build();
    }

    /** {@code ogcTileMatrixSets}: the offered tile matrix sets. */
    @GetMapping("/v1/ogc/tileMatrixSets")
    @Operation(operationId = "ogcTileMatrixSets", tags = "Products")
    public Map<String, Object> ogcTileMatrixSets(HttpServletRequest request) {
        PublicUrls urls = PublicUrls.of(settings, request);
        List<Map<String, Object>> sets = new ArrayList<>();
        for (TileMatrixSet set : TileMatrixSets.ALL) {
            Map<String, Object> ref = new LinkedHashMap<>();
            ref.put("id", set.id());
            ref.put("title", set.id());
            ref.put("uri", set.crs());
            ref.put("links", List.of(link(urls.api("/v1/ogc/tileMatrixSets/" + set.id()), "self",
                    "application/json", set.id())));
            sets.add(ref);
        }
        return Map.of("tileMatrixSets", sets);
    }

    /** {@code ogcTileMatrixSet}: one tile matrix set (OGC 17-083r4 JSON). */
    @GetMapping("/v1/ogc/tileMatrixSets/{tileMatrixSetId}")
    @Operation(operationId = "ogcTileMatrixSet", tags = "Products")
    public Map<String, Object> ogcTileMatrixSet(@PathVariable String tileMatrixSetId) {
        TileMatrixSet set = TileMatrixSets.find(tileMatrixSetId)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.NOT_FOUND, "Unknown tile matrix set "
                        + tileMatrixSetId + "."));
        return tileMatrixSetJson(set);
    }

    /** {@code ogcCollections}: the products as coverage collections. */
    @GetMapping("/v1/ogc/collections")
    @Operation(operationId = "ogcCollections", tags = "Products")
    public Map<String, Object> ogcCollections(HttpServletRequest request) {
        ReadCaller caller = callers.resolve(request);
        PublicUrls urls = PublicUrls.of(settings, request);
        List<Map<String, Object>> out = new ArrayList<>();
        for (ProductRegistry.Registered product : registry.all()) {
            if (callers.excludedSources(caller).contains(product.sourceId()) || product.spec().isExternal()) {
                continue;
            }
            out.add(collection(urls, product));
        }
        return Map.of("links", List.of(link(urls.api("/v1/ogc/collections"), "self", "application/json",
                "Collections")), "collections", out);
    }

    /** {@code ogcCollection}: one product as a coverage collection. */
    @GetMapping("/v1/ogc/collections/{collectionId}")
    @Operation(operationId = "ogcCollection", tags = "Products")
    public Map<String, Object> ogcCollection(@PathVariable String collectionId, HttpServletRequest request) {
        ReadCaller caller = callers.resolve(request);
        ProductRegistry.Registered product = registry.all().stream()
                .filter(p -> p.spec().id().equals(collectionId) && !callers.excludedSources(caller).contains(p.sourceId())
                        && !p.spec().isExternal())
                .findFirst().orElseThrow(() -> new NotFoundException(ErrorCodes.NOT_FOUND, "Unknown collection "
                        + collectionId + "."));
        return collection(PublicUrls.of(settings, request), product);
    }

    /** {@code ogcCollectionTilesets}: the tilesets of a collection. */
    @GetMapping("/v1/ogc/collections/{collectionId}/map/tiles")
    @Operation(operationId = "ogcCollectionTilesets", tags = "Products")
    public Map<String, Object> ogcCollectionTilesets(@PathVariable String collectionId, HttpServletRequest request) {
        ProductSpec spec = visibleSpec(collectionId, request);
        PublicUrls urls = PublicUrls.of(settings, request);
        List<Map<String, Object>> sets = new ArrayList<>();
        for (TileMatrixSet set : TileMatrixSets.ALL) {
            Map<String, Object> t = new LinkedHashMap<>();
            t.put("title", spec.title() + " (" + set.id() + ")");
            t.put("dataType", "map");
            t.put("crs", set.crs());
            t.put("tileMatrixSetURI", set.crs());
            t.put("links", List.of(link(urls.api("/v1/ogc/collections/" + spec.id() + "/map/tiles/" + set.id()), "self",
                    "application/json", set.id())));
            sets.add(t);
        }
        return Map.of("tilesets", sets);
    }

    /** {@code ogcCollectionTileset}: one tileset's metadata. */
    @GetMapping("/v1/ogc/collections/{collectionId}/map/tiles/{tileMatrixSetId}")
    @Operation(operationId = "ogcCollectionTileset", tags = "Products")
    public Map<String, Object> ogcCollectionTileset(@PathVariable String collectionId,
            @PathVariable String tileMatrixSetId, HttpServletRequest request) {
        ProductSpec spec = visibleSpec(collectionId, request);
        TileMatrixSet set = TileMatrixSets.find(tileMatrixSetId)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.NOT_FOUND, "Unknown tile matrix set."));
        PublicUrls urls = PublicUrls.of(settings, request);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("title", spec.title());
        out.put("dataType", "map");
        out.put("crs", set.crs());
        out.put("tileMatrixSetURI", set.crs());
        out.put("links", List.of(
                link(urls.api("/v1/ogc/collections/" + spec.id() + "/map/tiles/" + set.id()), "self",
                        "application/json", "This tileset"),
                link(template(urls, spec, set), "item", "image/png", "Tiles", true)));
        if (spec.attribution() != null) {
            out.put("attribution", spec.attribution().text());
        }
        return out;
    }

    /** {@code ogcCollectionTile}: a map tile, 301 to the content-addressed tile. */
    @GetMapping("/v1/ogc/collections/{collectionId}/map/tiles/{tileMatrixSetId}/{tileMatrix}/{tileRow}/{tileCol}")
    @Operation(operationId = "ogcCollectionTile", tags = "Products")
    public ResponseEntity<Void> ogcCollectionTile(@PathVariable String collectionId,
            @PathVariable String tileMatrixSetId, @PathVariable String tileMatrix, @PathVariable int tileRow,
            @PathVariable int tileCol, @RequestParam(required = false) String datetime,
            @RequestParam(required = false) String style, @RequestParam(required = false) String f,
            HttpServletRequest request) {
        ProductSpec spec = visibleSpec(collectionId, request);
        TileMatrixSet set = TileMatrixSets.find(tileMatrixSetId)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.NOT_FOUND, "Unknown tile matrix set."));
        int z = parseInt("tileMatrix", tileMatrix);
        RasterRenderService.checkTile(spec, set, z, tileCol, tileRow);
        String token = datetime == null || datetime.isBlank() || "current".equals(datetime) ? FrameResolver.LATEST
                : datetime.strip();
        FrameResolver.Resolution resolution = resolver.resolve(spec, token, null, false, clock.instant(), k -> k);
        if (resolution instanceof FrameResolver.Redirect redirect) {
            resolution = resolver.resolve(spec, redirect.canonicalToken(), null, false, clock.instant(), k -> k);
        }
        FrameResolver.Found found = (FrameResolver.Found) resolution;
        RasterFrameRow frame = found.frame();
        PublicUrls urls = PublicUrls.of(settings, request);
        StringBuilder target = new StringBuilder(urls.api("/v1/products/" + spec.id() + "/tiles/"))
                .append(frame.frameKey()).append('/').append(set.id()).append('/').append(z).append('/').append(tileCol)
                .append('/').append(tileRow).append(".png");
        if (style != null && !style.isBlank()) {
            target.append("?style=").append(java.net.URLEncoder.encode(style,
                    java.nio.charset.StandardCharsets.UTF_8));
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("public, max-age=" + (found.contentAddressed()
                ? RasterCachePolicy.IMMUTABLE_SECONDS : 60));
        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY).headers(headers)
                .location(URI.create(target.toString())).build();
    }

    /** {@code edrPosition}: a point value as CoverageJSON, matching {@code /v1/sample}. */
    @GetMapping("/v1/ogc/collections/{collectionId}/position")
    @Operation(operationId = "edrPosition", tags = "Products")
    public ResponseEntity<Map<String, Object>> edrPosition(@PathVariable String collectionId,
            @RequestParam String coords, @RequestParam(required = false) String datetime,
            @RequestParam(name = "parameter-name", required = false) List<String> parameterName,
            HttpServletRequest request) {
        double[] point = parsePoint(coords);
        ReadCaller caller = callers.resolve(request);
        ProductRegistry.Registered product = samples.visible(collectionId, callers.excludedSources(caller));
        TimeSpec time = TimeSpec.parse("datetime", datetime);
        SampleValue value = samples.value(product, new SampleService.Point(point[0], point[1], null, ZoneOffset.UTC),
                time, null, false, clock.instant());
        return ResponseEntity.ok().contentType(COVERAGE_JSON)
                .body(coveragePoint(product.spec(), point, value));
    }

    /** {@code edrArea}: a polygon subset as CoverageJSON (413 {@code EDR_TOO_LARGE} above the cap). */
    @GetMapping("/v1/ogc/collections/{collectionId}/area")
    @Operation(operationId = "edrArea", tags = "Products")
    public ResponseEntity<Map<String, Object>> edrArea(@PathVariable String collectionId,
            @RequestParam String coords, @RequestParam(required = false) String datetime,
            @RequestParam(name = "parameter-name", required = false) List<String> parameterName,
            HttpServletRequest request) {
        double[] bbox = parsePolygonBox(coords);
        ReadCaller caller = callers.resolve(request);
        ProductRegistry.Registered product = samples.visible(collectionId, callers.excludedSources(caller));
        TimeSpec time = TimeSpec.parse("datetime", datetime);
        FrameChoice choice = samples.choose(product, time, null, clock.instant());
        if (!choice.hasFrame()) {
            throw new ForesignalException(HttpStatus.NOT_FOUND, ErrorCodes.UNKNOWN_FRAME, choice.text());
        }
        FrameView view = samples.view(choice.frame()).orElseThrow(() -> new ForesignalException(HttpStatus.NOT_FOUND,
                ErrorCodes.UNKNOWN_FRAME, "Frame " + choice.frame().frameKey() + " could not be read."));
        EdrArea.Subset subset = EdrArea.of(view, bbox, MAX_AREA_CELLS);
        if (subset.tooLarge()) {
            throw new ForesignalException(HttpStatus.PAYLOAD_TOO_LARGE, ErrorCodes.EDR_TOO_LARGE,
                    "The area covers " + subset.cells() + " native cells; at most " + MAX_AREA_CELLS + ".");
        }
        return ResponseEntity.ok().contentType(COVERAGE_JSON)
                .body(coverageArea(product.spec(), choice.frame(), subset));
    }

    /** {@code edrInstances}: the forecast runs of a product. */
    @GetMapping("/v1/ogc/collections/{collectionId}/instances")
    @Operation(operationId = "edrInstances", tags = "Products")
    public Map<String, Object> edrInstances(@PathVariable String collectionId, HttpServletRequest request) {
        ProductSpec spec = visibleSpec(collectionId, request);
        PublicUrls urls = PublicUrls.of(settings, request);
        List<Map<String, Object>> instances = new ArrayList<>();
        for (RasterFrameQueries.RunSummary run : frames.runs(spec.id())) {
            String id = FrameKeys.runIso(run.run());
            Map<String, Object> extent = new LinkedHashMap<>();
            extent.put("temporal", Map.of("interval", List.of(List.of(
                    run.validFrom() == null ? null : run.validFrom().toString(),
                    run.validTo() == null ? null : run.validTo().toString()))));
            instances.add(Map.of("id", id, "title", "Run " + id, "extent", extent,
                    "links", List.of(link(urls.api("/v1/ogc/collections/" + spec.id() + "/instances/" + id), "self",
                            "application/json", id))));
        }
        return Map.of("instances", instances, "links", List.of(link(
                urls.api("/v1/ogc/collections/" + spec.id() + "/instances"), "self", "application/json", "Instances")));
    }

    private SampleService.Point point(double lon, double lat) {
        return new SampleService.Point(lon, lat, null, ZoneOffset.UTC);
    }

    /** The coverage collection document for a raster product. */
    private Map<String, Object> collection(PublicUrls urls, ProductRegistry.Registered product) {
        ProductSpec spec = product.spec();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", spec.id());
        out.put("title", spec.title());
        out.put("description", spec.legendSummary());
        out.put("itemType", "coverage");
        List<Double> bounds = spec.spatial() == null ? null : spec.spatial().bounds();
        if (bounds != null) {
            out.put("extent", Map.of("spatial", Map.of("bbox", List.of(bounds), "crs", "http://www.opengis.net/def/crs/OGC/1.3/CRS84")));
        }
        Map<String, Object> position = Map.of("link", link(urls.api("/v1/ogc/collections/" + spec.id() + "/position"),
                "http://www.opengis.net/def/rel/ogc/1.0/data", COVERAGE_JSON.toString(), "Position query"));
        Map<String, Object> area = Map.of("link", link(urls.api("/v1/ogc/collections/" + spec.id() + "/area"),
                "http://www.opengis.net/def/rel/ogc/1.0/data", COVERAGE_JSON.toString(), "Area query"));
        Map<String, Object> instances = Map.of("link", link(urls.api("/v1/ogc/collections/" + spec.id() + "/instances"),
                "http://www.opengis.net/def/rel/ogc/1.0/data", "application/json", "Instances"));
        out.put("data_queries", Map.of("position", position, "area", area, "instances", instances));
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("value", Map.of("type", "Parameter", "unit", Map.of("symbol", spec.units() == null ? "" : spec.units()),
                "observedProperty", Map.of("label", spec.title()), "nodata", spec.nodata()));
        out.put("parameter_names", parameters);
        out.put("output_formats", List.of("CoverageJSON"));
        out.put("crs", List.of("http://www.opengis.net/def/crs/OGC/1.3/CRS84"));
        out.put("attribution", spec.attribution() == null ? null : spec.attribution().text());
        List<Map<String, Object>> links = new ArrayList<>();
        links.add(link(urls.api("/v1/ogc/collections/" + spec.id()), "self", "application/json", "This collection"));
        links.add(link(urls.api("/v1/ogc/collections/" + spec.id() + "/map/tiles"),
                "http://www.opengis.net/def/rel/ogc/1.0/tilesets-map", "application/json", "Tilesets"));
        out.put("links", links);
        return out;
    }

    /** The CoverageJSON of one point value (one {@code t,y,x} cell; {@code null} when there is no value). */
    static Map<String, Object> coveragePoint(ProductSpec spec, double[] point, SampleValue value) {
        Map<String, Object> t = Map.of("values", List.of(value.validTime() == null ? null
                : value.validTime().toString()));
        Map<String, Object> domain = new LinkedHashMap<>();
        domain.put("type", "Domain");
        domain.put("domainType", "Point");
        domain.put("axes", Map.of("x", Map.of("values", List.of(point[0])), "y", Map.of("values", List.of(point[1])),
                "t", t));
        domain.put("referencing", List.of(
                Map.of("coordinates", List.of("x", "y"), "system", Map.of("type", "GeographicCRS",
                        "id", "http://www.opengis.net/def/crs/OGC/1.3/CRS84")),
                Map.of("coordinates", List.of("t"), "system", Map.of("type", "TemporalRS", "calendar", "Gregorian"))));
        Map<String, Object> range = new LinkedHashMap<>();
        range.put("type", "NdArray");
        range.put("dataType", "float");
        range.put("axisNames", List.of("t", "y", "x"));
        range.put("shape", List.of(1, 1, 1));
        // shape [1,1,1] always holds one value: null is the CoverageJSON no-data marker
        range.put("values", java.util.Collections.singletonList(value.value()));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("type", "Coverage");
        out.put("domain", domain);
        out.put("parameters", Map.of("value", parameter(spec, value)));
        out.put("ranges", Map.of("value", range));
        return out;
    }

    /** The CoverageJSON of a native-grid subset: flat values, the subset's domain and the frame's time. */
    static Map<String, Object> coverageArea(ProductSpec spec, RasterFrameRow frame, EdrArea.Subset subset) {
        Map<String, Object> domain = new LinkedHashMap<>(subset.domain());
        if (frame != null && frame.validTime() != null) {
            Map<String, Object> axes = new LinkedHashMap<>(castMap(domain.get("axes")));
            axes.put("t", Map.of("values", List.of(frame.validTime().toString())));
            domain.put("axes", axes);
        }
        Map<String, Object> range = new LinkedHashMap<>();
        range.put("type", "NdArray");
        range.put("dataType", "float");
        range.put("axisNames", subset.axisNames());
        range.put("shape", subset.shape());
        range.put("values", subset.values());
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("type", "Coverage");
        out.put("domain", domain);
        out.put("parameters", Map.of("value", parameter(spec, null)));
        out.put("ranges", Map.of("value", range));
        return out;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Object value) {
        return (Map<String, Object>) value;
    }

    private static Map<String, Object> parameter(ProductSpec spec, SampleValue value) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("type", "Parameter");
        p.put("description", Map.of("en", spec.title()));
        p.put("unit", Map.of("symbol", value != null && value.units() != null ? value.units()
                : spec.units() == null ? "" : spec.units()));
        p.put("observedProperty", Map.of("label", Map.of("en", spec.title())));
        return p;
    }

    /** The tile URL template of a collection in a set. */
    private static String template(PublicUrls urls, ProductSpec spec, TileMatrixSet set) {
        return urls.api("/v1/ogc/collections/" + spec.id() + "/map/tiles/" + set.id()
                + "/{tileMatrix}/{tileRow}/{tileCol}");
    }

    private ProductSpec visibleSpec(String collectionId, HttpServletRequest request) {
        ReadCaller caller = callers.resolve(request);
        ProductRegistry.Registered product = registry.all().stream()
                .filter(p -> p.spec().id().equals(collectionId)
                        && !callers.excludedSources(caller).contains(p.sourceId()) && !p.spec().isExternal())
                .findFirst().orElseThrow(() -> new NotFoundException(ErrorCodes.NOT_FOUND, "Unknown collection "
                        + collectionId + "."));
        return product.spec();
    }

    /** The OGC 17-083r4 JSON of a tile matrix set. */
    private static Map<String, Object> tileMatrixSetJson(TileMatrixSet set) {
        boolean mercator = "WebMercatorQuad".equals(set.id());
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", set.id());
        out.put("title", set.id());
        out.put("uri", set.crs());
        out.put("crs", set.crs());
        out.put("orderedAxes", mercator ? List.of("X", "Y") : List.of("Lon", "Lat"));
        out.put("wellKnownScaleSet", mercator
                ? "http://www.opengis.net/def/wkss/OGC/1.0/GoogleMapsCompatible"
                : "http://www.opengis.net/def/wkss/OGC/1.0/GoogleCRS84Quad");
        List<Map<String, Object>> matrices = new ArrayList<>();
        for (int z = 0; z <= 24; z++) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", String.valueOf(z));
            m.put("scaleDenominator", mercator ? 559082264.0287178 / (1 << z) : 279541132.0143589 / (1 << z));
            m.put("cellSize", mercator ? 156543.03392804097 / (1 << z) : 0.703125 / (1 << z));
            m.put("pointOfOrigin", mercator ? List.of(-20037508.342789244, 20037508.342789244) : List.of(-180.0, 90.0));
            m.put("tileWidth", 256);
            m.put("tileHeight", 256);
            m.put("matrixWidth", set.tilesX(z));
            m.put("matrixHeight", set.tilesY(z));
            m.put("cornerOfOrigin", "topLeft");
            matrices.add(m);
        }
        out.put("tileMatrices", matrices);
        return out;
    }

    private static Map<String, Object> link(String href, String rel, String type, String title) {
        return link(href, rel, type, title, false);
    }

    private static Map<String, Object> link(String href, String rel, String type, String title, boolean templated) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("href", href);
        m.put("rel", rel);
        if (type != null) {
            m.put("type", type);
        }
        if (title != null) {
            m.put("title", title);
        }
        if (templated) {
            m.put("templated", true);
        }
        return m;
    }

    /** Parses {@code POINT(lon lat)}. */
    static double[] parsePoint(String coords) {
        Matcher m = POINT.matcher(coords == null ? "" : coords.strip());
        if (!m.matches()) {
            throw ValidationException.forField("coords", "must be POINT(lon lat)");
        }
        return new double[] {Double.parseDouble(m.group(1)), Double.parseDouble(m.group(2))};
    }

    /** Parses {@code POLYGON((lon lat, lon lat, ...))} and returns its bounding box {@code w,s,e,n}. */
    static double[] parsePolygonBox(String coords) {
        Matcher m = POLYGON.matcher(coords == null ? "" : coords.strip());
        if (!m.matches()) {
            throw ValidationException.forField("coords", "must be POLYGON((lon lat, ...))");
        }
        double minLon = Double.POSITIVE_INFINITY;
        double minLat = Double.POSITIVE_INFINITY;
        double maxLon = Double.NEGATIVE_INFINITY;
        double maxLat = Double.NEGATIVE_INFINITY;
        for (String pair : m.group(1).split(",")) {
            String[] parts = pair.strip().split("\\s+");
            if (parts.length < 2) {
                continue;
            }
            double lon = Double.parseDouble(parts[0]);
            double lat = Double.parseDouble(parts[1]);
            minLon = Math.min(minLon, lon);
            minLat = Math.min(minLat, lat);
            maxLon = Math.max(maxLon, lon);
            maxLat = Math.max(maxLat, lat);
        }
        if (!(minLon < maxLon) || !(minLat < maxLat)) {
            throw ValidationException.forField("coords", "must enclose a non-empty box");
        }
        return new double[] {minLon, minLat, maxLon, maxLat};
    }

    private static int parseInt(String name, String raw) {
        try {
            return Integer.parseInt(raw.strip());
        } catch (RuntimeException e) {
            throw ValidationException.forField(name, "must be an integer");
        }
    }
}
