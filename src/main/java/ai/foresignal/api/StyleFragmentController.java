package ai.foresignal.api;

import ai.foresignal.api.support.PublicUrls;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.raster.FrameResolver;
import ai.foresignal.raster.ProductUrls;
import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.raster.render.RasterRenderService;
import ai.foresignal.raster.render.StyleRegistry;
import ai.foresignal.raster.render.TileMatrixSet;
import ai.foresignal.raster.render.TileMatrixSets;
import ai.foresignal.security.visibility.SourceVisibility;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.source.spi.ProductSpec;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.json.JsonMapper;

/**
 * MapLibre style fragments and ATAK MOBAC sources (OpenAPI tag {@code Products}, prompt 108;
 * Weather-Imagery-Design §6.10, §11). A fragment carries concrete content-addressed tile URLs and
 * namespaced layer ids ({@code foresignal-{product}}) for merging below a layer of the client's own
 * basemap. The server never fetches a basemap style.
 */
@RestController
@Tag(name = "Products")
public class StyleFragmentController {

    private final ai.foresignal.raster.ProductCatalogService catalog;
    private final ReadCallerResolver callers;
    private final RasterRenderService renderer;
    private final FrameResolver resolver;
    private final SourceVisibility visibility;
    private final ProductUrls urls;
    private final AppSettingService settings;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the controller. */
    public StyleFragmentController(ai.foresignal.raster.ProductCatalogService catalog, ReadCallerResolver callers,
            RasterRenderService renderer, FrameResolver resolver, SourceVisibility visibility, ProductUrls urls,
            AppSettingService settings, JsonMapper json, Clock clock) {
        this.catalog = catalog;
        this.callers = callers;
        this.renderer = renderer;
        this.resolver = resolver;
        this.visibility = visibility;
        this.urls = urls;
        this.settings = settings;
        this.json = json;
        this.clock = clock;
    }

    /** {@code getProductStyleFragment}: a fragment for one product. */
    @GetMapping("/v1/products/{productId}/style-fragment.json")
    @Operation(operationId = "getProductStyleFragment", tags = "Products")
    public ResponseEntity<Map<String, Object>> getProductStyleFragment(@PathVariable String productId,
            @RequestParam(required = false) String frameKey, @RequestParam(required = false) String style,
            @RequestParam(required = false) String tms, HttpServletRequest request) {
        ReadCaller caller = callers.resolve(request);
        ai.foresignal.raster.ProductRegistry.Registered product = catalog.visible(productId,
                callers.excludedSources(caller));
        return ResponseEntity.ok(fragment(List.of(product.spec()), caller, productId, frameKey, style, tms, request));
    }

    /** {@code getProductsStyleFragment}: a fragment for a comma-separated product list. */
    @GetMapping("/v1/products/style-fragment.json")
    @Operation(operationId = "getProductsStyleFragment", tags = "Products")
    public ResponseEntity<Map<String, Object>> getProductsStyleFragment(
            @RequestParam List<String> products, @RequestParam(required = false) String frameKey,
            @RequestParam(required = false) String style, @RequestParam(required = false) String tms,
            HttpServletRequest request) {
        ReadCaller caller = callers.resolve(request);
        List<String> ids = QueryParams.multi("products", products);
        List<ProductSpec> specs = new ArrayList<>();
        for (String id : ids) {
            specs.add(catalog.visible(id, callers.excludedSources(caller)).spec());
        }
        return ResponseEntity.ok(fragment(specs, caller, ids.size() == 1 ? ids.get(0) : null, frameKey, style, tms,
                request));
    }

    /** {@code getProductMobacXml}: an ATAK MOBAC custom map source. */
    @GetMapping(value = "/v1/products/{productId}/mobac.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @Operation(operationId = "getProductMobacXml", tags = "Products")
    public ResponseEntity<String> getProductMobacXml(@PathVariable String productId,
            @RequestParam(required = false) String style, HttpServletRequest request) {
        ReadCaller caller = callers.resolve(request);
        ai.foresignal.raster.ProductRegistry.Registered product = catalog.visible(productId,
                callers.excludedSources(caller));
        ProductSpec spec = product.spec();
        StyleRegistry.Resolved resolved = renderer.style(spec, style);
        int min = spec.spatial() == null || spec.spatial().minZoom() == null ? 0 : spec.spatial().minZoom();
        int max = spec.spatial() == null || spec.spatial().nativeMaxZoom() == null ? 0
                : spec.spatial().nativeMaxZoom();
        PublicUrls base = PublicUrls.of(settings, request);
        String template = base.api("/v1/products/" + spec.id() + "/tiles/latest/{$z}/{$x}/{$y}.png")
                + "?style=" + resolved.ref();
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<customMapSource>\n");
        xml.append("  <name>").append(esc(spec.title())).append("</name>\n");
        xml.append("  <minZoom>").append(min).append("</minZoom>\n");
        xml.append("  <maxZoom>").append(max).append("</maxZoom>\n");
        xml.append("  <tileType>png</tileType>\n");
        xml.append("  <tileUpdate>None</tileUpdate>\n");
        xml.append("  <url>").append(esc(template)).append("</url>\n");
        if (spec.attribution() != null) {
            xml.append("  <attributionText>").append(esc(spec.attribution().text())).append("</attributionText>\n");
        }
        xml.append("</customMapSource>\n");
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML)
                .body(xml.toString());
    }

    /** The fragment document. */
    private Map<String, Object> fragment(List<ProductSpec> specs, ReadCaller caller, String singleId, String frameKey,
            String style, String tms, HttpServletRequest request) {
        TileMatrixSet set = TileMatrixSets.resolve(tms);
        String base = urls.base(request);
        Map<String, Object> sources = new LinkedHashMap<>();
        List<Map<String, Object>> layers = new ArrayList<>();
        Map<String, String> legends = new LinkedHashMap<>();
        List<Map<String, Object>> attribution = new ArrayList<>();
        for (ProductSpec spec : specs) {
            String sourceId = "foresignal-" + spec.id();
            StyleRegistry.Resolved resolved = renderer.style(spec, specs.size() == 1 ? style : null);
            FrameResolver.Resolution resolution = resolver.resolve(spec, frameKey == null ? FrameResolver.LATEST
                    : frameKey, null, false, clock.instant(), k -> k);
            if (resolution instanceof FrameResolver.Redirect redirect) {
                resolution = resolver.resolve(spec, redirect.canonicalToken(), null, false, clock.instant(), k -> k);
            }
            RasterFrameRow frame = ((FrameResolver.Found) resolution).frame();
            int min = spec.spatial() == null || spec.spatial().minZoom() == null ? 0 : spec.spatial().minZoom();
            int max = spec.spatial() == null || spec.spatial().nativeMaxZoom() == null ? 0
                    : spec.spatial().nativeMaxZoom();
            Map<String, Object> source = new LinkedHashMap<>();
            source.put("type", "raster");
            source.put("tiles", List.of(tileTemplate(base, spec, frame, resolved, set)));
            source.put("tileSize", set == TileMatrixSets.DEFAULT ? 256 : 256);
            source.put("minzoom", min);
            source.put("maxzoom", max);
            if (spec.attribution() != null) {
                source.put("attribution", spec.attribution().text());
            }
            sources.put(sourceId, source);

            Map<String, Object> layer = new LinkedHashMap<>();
            layer.put("id", sourceId);
            layer.put("type", "raster");
            layer.put("source", sourceId);
            layer.put("minzoom", min);
            layer.put("maxzoom", max);
            layers.add(layer);
            legends.put(spec.id(), ProductUrls.legend(base, spec.id(), resolved.ref()));
            if (spec.attribution() != null) {
                Map<String, Object> a = new LinkedHashMap<>();
                a.put("text", spec.attribution().text());
                a.put("html", spec.attribution().html());
                a.put("url", spec.attribution().url());
                a.put("licence", spec.attribution().licence());
                a.put("licenceUrl", spec.attribution().licenceUrl());
                a.put("requiredDisplay", spec.attribution().requiredDisplay());
                if (!attribution.contains(a)) {
                    attribution.add(a);
                }
            }
        }
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("legends", legends);
        metadata.put("attribution", attribution);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("version", 8);
        out.put("sources", sources);
        out.put("layers", layers);
        out.put("metadata", metadata);
        return out;
    }

    private static String tileTemplate(String base, ProductSpec spec, RasterFrameRow frame,
            StyleRegistry.Resolved style, TileMatrixSet set) {
        String tms = set == TileMatrixSets.DEFAULT ? "" : set.id() + "/";
        return ProductUrls.product(base, spec.id()) + "/tiles/" + frame.frameKey() + "/" + tms + "{z}/{x}/{y}.png"
                + "?style=" + style.ref();
    }

    private static String esc(String value) {
        return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
