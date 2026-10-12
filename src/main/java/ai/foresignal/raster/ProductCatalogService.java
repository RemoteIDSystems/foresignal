package ai.foresignal.raster;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.BboxFilter;
import ai.foresignal.collect.HealthService;
import ai.foresignal.collect.SourceHealthView;
import ai.foresignal.source.spi.GridDefinition;
import ai.foresignal.source.spi.ProductSpec;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.locationtech.jts.geom.Envelope;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Builds the product and frame documents of the {@code Products} API (prompt 042;
 * Weather-Imagery-Design §6.1, §6.2; OpenAPI {@code Product}, {@code ProductPage},
 * {@code RasterFrame}, {@code RasterFramePage}). Pure reads: no request path ever causes an upstream
 * fetch.
 */
@Service
public class ProductCatalogService {

    /** Frames a client should prefetch for an animation. */
    public static final int RECOMMENDED_FRAMES = 12;

    /** The longest {@code max-age} of an alias response. */
    public static final Duration MAX_ALIAS_AGE = Duration.ofSeconds(60);

    static final String CURSOR_PREFIX = "f1.";
    static final String PRODUCT_CURSOR_PREFIX = "p1.";

    private static final Map<String, String> COVERAGE_MEANINGS = Map.of(
            "CONUS", "Contiguous US mosaic; outside it values are no-coverage, not clear weather",
            "global", "Global grid; every point has a value",
            "station-disc", "What the local station receives from its satellite; outside the disc is no-coverage",
            "swath", "One satellite pass's swath; outside it is no-coverage");

    private final ProductRegistry registry;
    private final RasterFrameQueries frames;
    private final GridStore grids;
    private final FrameResolver resolver;
    private final ProductStatusTracker status;
    private final ObjectProvider<HealthService> health;
    private final Clock clock;

    /** Creates the service. */
    public ProductCatalogService(ProductRegistry registry, RasterFrameQueries frames, GridStore grids,
            FrameResolver resolver, ProductStatusTracker status, ObjectProvider<HealthService> health, Clock clock) {
        this.registry = registry;
        this.frames = frames;
        this.grids = grids;
        this.resolver = resolver;
        this.status = status;
        this.health = health;
        this.clock = clock;
    }

    /**
     * The catalog filter of {@code listProducts}.
     *
     * @param category only this category, or null
     * @param kind only this product kind, or null
     * @param bbox only products whose bounds intersect it, or null
     * @param source only this source, or null
     * @param q case-insensitive substring of id or title, or null
     * @param compact compact items
     */
    public record ProductQuery(String category, String kind, BboxFilter bbox, String source, String q,
            boolean compact) {}

    /**
     * One page of the catalog.
     *
     * @param excludedSources sources the caller may not read
     * @param cursor continue after this cursor, or null
     * @param limit items per page
     * @param base the URL base
     */
    public Map<String, Object> listProducts(ProductQuery query, Set<String> excludedSources, String cursor, int limit,
            String base) {
        String after = decodeProductCursor(cursor);
        Instant now = clock.instant();
        List<Map<String, Object>> items = new ArrayList<>();
        String last = null;
        boolean more = false;
        for (ProductRegistry.Registered product : registry.all()) {
            if (excludedSources.contains(product.sourceId()) || !matches(product, query)) {
                continue;
            }
            if (after != null && product.id().compareTo(after) <= 0) {
                continue;
            }
            List<Double> bounds = bounds(product.spec());
            if (query.bbox() != null && !intersects(query.bbox(), bounds)) {
                continue;
            }
            if (items.size() == limit) {
                more = true;
                break;
            }
            items.add(product(product, base, query.compact(), now));
            last = product.id();
        }
        Map<String, Object> page = new LinkedHashMap<>();
        page.put("items", items);
        page.put("nextCursor", more && last != null ? PRODUCT_CURSOR_PREFIX + encode(last) : null);
        return page;
    }

    /**
     * One product, visible to the caller.
     *
     * @throws ForesignalException {@code 404 UNKNOWN_PRODUCT}
     */
    public ProductRegistry.Registered visible(String productId, Set<String> excludedSources) {
        ProductRegistry.Registered product = registry.require(productId);
        if (excludedSources.contains(product.sourceId())) {
            throw ProductRegistry.unknownProduct(productId);
        }
        return product;
    }

    /** The full document of one product. */
    public Map<String, Object> product(ProductRegistry.Registered product, String base, boolean compact, Instant now) {
        ProductSpec spec = product.spec();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", spec.id());
        out.put("title", spec.title());
        out.put("kind", spec.kind());
        out.put("category", spec.category());
        out.put("source", product.sourceId());
        String coverage = spec.spatial() == null ? null : spec.spatial().coverage();
        if (coverage != null) {
            out.put("coverage", coverage);
        }
        out.put("units", spec.units());
        Optional<Instant> latest = spec.isExternal() ? Optional.empty() : frames.latestComplete(spec.id());
        out.put("temporal", temporal(spec, latest, compact));
        out.put("status", status(product, latest, now));
        if (compact) {
            return out;
        }
        out.put("dynamic", product.dynamic());
        out.put("dtype", spec.dtype() == null ? null : spec.dtype().wireValue());
        if (coverage != null) {
            out.put("coverageMeaning", spec.coverageMeaning() != null ? spec.coverageMeaning()
                    : COVERAGE_MEANINGS.get(coverage));
        }
        if (!spec.codes().isEmpty()) {
            Map<String, Object> codes = new LinkedHashMap<>();
            spec.codes().forEach((code, meaning) -> codes.put(Integer.toString(code), meaning));
            out.put("codes", codes);
        }
        out.put("spatial", spatial(spec));
        if (spec.defaultStyle() != null || !spec.styles().isEmpty()) {
            Map<String, Object> render = new LinkedHashMap<>();
            render.put("defaultStyle", spec.defaultStyle() != null ? spec.defaultStyle() : spec.styles().get(0));
            render.put("styles", spec.styles().isEmpty() ? List.of(spec.defaultStyle()) : spec.styles());
            if (spec.defaultOpacity() != null) {
                render.put("defaultOpacity", spec.defaultOpacity());
            }
            out.put("render", render);
        }
        Map<String, Object> legend = new LinkedHashMap<>();
        legend.put("summary", spec.legendSummary());
        legend.put("url", ProductUrls.legend(base, spec.id()));
        legend.put("pngUrl", ProductUrls.legendPng(base, spec.id()));
        out.put("legend", legend);
        if (spec.attribution() != null) {
            out.put("attribution", ProductSpecJson.attribution(spec.attribution()));
        }
        out.put("external", null);
        Map<String, Object> links = new LinkedHashMap<>();
        links.put("self", ProductUrls.product(base, spec.id()));
        links.put("frames", ProductUrls.frames(base, spec.id()));
        links.put("latest", ProductUrls.latest(base, spec.id()));
        links.put("tilejson", ProductUrls.tileJson(base, spec.id()));
        links.put("legend", ProductUrls.legend(base, spec.id()));
        links.put("timeline", ProductUrls.timeline(base, spec.id()));
        out.put("links", links);
        return out;
    }

    private Map<String, Object> temporal(ProductSpec spec, Optional<Instant> latest, boolean compact) {
        Map<String, Object> t = new LinkedHashMap<>();
        ProductSpec.Temporal temporal = spec.temporal();
        t.put("type", temporal == null ? ProductSpec.Temporal.OBSERVATION : temporal.type());
        t.put("cadence", temporal == null ? null : ProductSpecJson.format(temporal.cadence()));
        t.put("maxGap", temporal == null ? null : ProductSpecJson.format(maxGap(spec)));
        if (spec.isExternal()) {
            return t;
        }
        t.put("earliest", frames.earliest(spec.id()).map(Instant::toString).orElse(null));
        t.put("latest", latest.map(Instant::toString).orElse(null));
        if (compact) {
            return t;
        }
        if (spec.retention() != null) {
            t.put("retention", ProductSpecJson.retention(spec.retention()));
        }
        if (spec.isForecast()) {
            List<Map<String, Object>> runs = new ArrayList<>();
            for (RasterFrameQueries.RunSummary run : frames.runs(spec.id())) {
                Map<String, Object> r = new LinkedHashMap<>();
                r.put("run", FrameKeys.runIso(run.run()));
                r.put("status", run.complete() && run.partialFrames() == 0 ? "complete" : "partial");
                r.put("steps", run.steps());
                r.put("validFrom", run.validFrom().toString());
                r.put("validTo", run.validTo().toString());
                runs.add(r);
            }
            t.put("runs", runs);
        }
        return t;
    }

    private Map<String, Object> spatial(ProductSpec spec) {
        Map<String, Object> s = new LinkedHashMap<>();
        List<Double> bounds = bounds(spec);
        s.put("bounds", bounds);
        int minZoom = spec.spatial() == null || spec.spatial().minZoom() == null ? 0 : spec.spatial().minZoom();
        s.put("minZoom", minZoom);
        s.put("maxZoom", spec.spatial() == null || spec.spatial().nativeMaxZoom() == null ? 0
                : spec.spatial().nativeMaxZoom());
        if (spec.spatial() != null && spec.spatial().maxZoomForAnimation() != null) {
            s.put("maxZoomForAnimation", spec.spatial().maxZoomForAnimation());
        }
        double west = bounds.get(0);
        double east = bounds.get(2);
        double centerLon = west <= east ? (west + east) / 2 : normalizeLon((west + east + 360) / 2);
        s.put("center", List.of(round(centerLon), round((bounds.get(1) + bounds.get(3)) / 2), (double) minZoom));
        return s;
    }

    private Map<String, Object> status(ProductRegistry.Registered product, Optional<Instant> latest, Instant now) {
        ProductSpec spec = product.spec();
        Map<String, Object> s = new LinkedHashMap<>();
        SourceHealthView view = null;
        HealthService service = health.getIfAvailable();
        if (service != null) {
            try {
                view = service.health(product.sourceId());
            } catch (RuntimeException e) {
                view = null;
            }
        }
        s.put("health", view == null ? "idle" : view.status().value());
        Duration gap = maxGap(spec);
        Instant last = spec.isForecast()
                ? frames.newestFrameTimes(List.of(), List.of(spec.id())).get(spec.id())
                : latest.orElse(null);
        boolean stale = !spec.isExternal() && (last == null || (gap != null && Duration.between(last, now).compareTo(gap) > 0));
        s.put("stale", stale);
        s.put("lastFrameAt", last == null ? null : last.toString());
        String reason = status.disabled(spec.id()).map(r -> "disabled: " + r.reason())
                .or(() -> status.formatChange(spec.id()).map(r -> "upstream format changed: " + r.reason()))
                .orElse(view == null ? null : view.statusReason());
        s.put("reason", reason);
        return s;
    }

    /**
     * A page of frames of a visible product.
     *
     * @throws ForesignalException {@code 503 PRODUCT_UNAVAILABLE}, {@code 400 VALIDATION_FAILED},
     *     {@code 400 INVALID_CURSOR} or {@code 404 UNKNOWN_FRAME} (an unknown run)
     */
    public Map<String, Object> listFrames(ProductRegistry.Registered product, Instant from, Instant to, String run,
            boolean includePartial, String cursor, int limit, String base) {
        ProductSpec spec = product.spec();
        requireAvailable(product);
        Instant selectedRun = null;
        if (run != null && !run.isBlank()) {
            if (!spec.isForecast()) {
                throw ValidationException.forField("run", "applies only to forecast products");
            }
            selectedRun = resolver.selectRun(spec, run, includePartial);
        }
        if (from != null && to != null && from.isAfter(to)) {
            throw ValidationException.forField("from", "must not be after to");
        }
        Position after = decodeFrameCursor(cursor, spec.id());
        List<RasterFrameRow> rows = spec.isExternal() ? List.of() : frames.page(spec.id(), from, to, selectedRun,
                includePartial, after == null ? null : after.time(), after == null ? null : after.frameId(), limit + 1);
        boolean more = rows.size() > limit;
        List<RasterFrameRow> pageRows = more ? rows.subList(0, limit) : rows;
        List<Map<String, Object>> items = new ArrayList<>();
        for (RasterFrameRow row : pageRows) {
            items.add(frame(spec, row, base));
        }
        Map<String, Object> page = new LinkedHashMap<>();
        page.put("items", items);
        RasterFrameRow last = pageRows.isEmpty() ? null : pageRows.get(pageRows.size() - 1);
        page.put("nextCursor", more && last != null ? frameCursor(spec.id(), last) : null);
        Map<String, Object> prefetch = new LinkedHashMap<>();
        prefetch.put("recommendedFrames", RECOMMENDED_FRAMES);
        if (spec.spatial() != null && spec.spatial().maxZoomForAnimation() != null) {
            prefetch.put("maxZoomForAnimation", spec.spatial().maxZoomForAnimation());
        }
        page.put("prefetch", prefetch);
        page.put("serverTime", clock.instant().toString());
        page.put("earliest", frames.earliest(spec.id()).map(Instant::toString).orElse(null));
        return page;
    }

    /**
     * The {@code latest} alias of a visible product.
     *
     * @throws ForesignalException {@code 503 PRODUCT_UNAVAILABLE} or {@code 404 UNKNOWN_FRAME}
     */
    public RasterFrameRow latestFrame(ProductRegistry.Registered product, String run, boolean includePartial) {
        requireAvailable(product);
        if (product.spec().isExternal()) {
            throw new ForesignalException(HttpStatus.NOT_FOUND, ErrorCodes.UNKNOWN_FRAME,
                    "Product " + product.id() + " is external; it has no frames.");
        }
        FrameResolver.Resolution resolution = resolver.resolve(product.spec(), FrameResolver.LATEST, run,
                includePartial, clock.instant(), null);
        return ((FrameResolver.Found) resolution).frame();
    }

    /** {@code max-age} of an alias response: {@code min(cadence / 2, 60 s)}, at least 1 s. */
    public static Duration aliasMaxAge(ProductSpec spec) {
        if (spec.temporal() == null || spec.temporal().cadence() == null) {
            return MAX_ALIAS_AGE;
        }
        Duration half = spec.temporal().cadence().dividedBy(2);
        Duration age = half.compareTo(MAX_ALIAS_AGE) < 0 ? half : MAX_ALIAS_AGE;
        return age.compareTo(Duration.ofSeconds(1)) < 0 ? Duration.ofSeconds(1) : age;
    }

    /** One frame's document. */
    public Map<String, Object> frame(ProductSpec spec, RasterFrameRow row, String base) {
        Map<String, Object> f = new LinkedHashMap<>();
        f.put("productId", spec.id());
        f.put("time", row.validTime().toString());
        f.put("timeKey", row.timeKey());
        f.put("frameKey", row.frameKey());
        f.put("status", row.status());
        f.put("expiresAt", row.expiresAt() == null ? null : row.expiresAt().toString());
        f.put("run", row.referenceTime() == null ? null : FrameKeys.runIso(row.referenceTime()));
        f.put("step", row.forecastStep());
        f.put("obsStart", row.obsStart() == null ? null : row.obsStart().toString());
        f.put("obsEnd", row.obsEnd() == null ? null : row.obsEnd().toString());
        f.put("tiles", ProductUrls.tiles(base, spec, row));
        f.put("tiles2x", ProductUrls.tiles2x(base, spec, row));
        f.put("image", ProductUrls.image(base, spec, row));
        f.put("bounds", grids.find(row.gridId()).map(g -> RasterPipeline.bounds(spec, g)).orElse(null));
        if (row.stats() != null) {
            f.put("stats", row.stats());
        }
        f.put("thinned", row.thinned());
        return f;
    }

    private void requireAvailable(ProductRegistry.Registered product) {
        status.disabled(product.id()).ifPresent(r -> {
            throw new ForesignalException(HttpStatus.SERVICE_UNAVAILABLE, ErrorCodes.PRODUCT_UNAVAILABLE,
                    "Product " + product.id() + " is disabled: " + r.reason());
        });
    }

    private static Duration maxGap(ProductSpec spec) {
        if (spec.temporal() == null) {
            return null;
        }
        if (spec.temporal().maxGap() != null) {
            return spec.temporal().maxGap();
        }
        return spec.temporal().cadence() == null ? null : spec.temporal().cadence().multipliedBy(3);
    }

    private boolean matches(ProductRegistry.Registered product, ProductQuery query) {
        ProductSpec spec = product.spec();
        if (query.category() != null && !query.category().equals(spec.category())) {
            return false;
        }
        if (query.kind() != null && !query.kind().equals(spec.kind())) {
            return false;
        }
        if (query.source() != null && !query.source().equals(product.sourceId())) {
            return false;
        }
        if (query.q() != null && !query.q().isBlank()) {
            String q = query.q().strip().toLowerCase(Locale.ROOT);
            String title = spec.title() == null ? "" : spec.title().toLowerCase(Locale.ROOT);
            return spec.id().contains(q) || title.contains(q);
        }
        return true;
    }

    /** The product bounds: the spec's, else the newest frame's grid, else the whole world. */
    List<Double> bounds(ProductSpec spec) {
        if (spec.spatial() != null && spec.spatial().bounds() != null) {
            return spec.spatial().bounds();
        }
        if (!spec.isExternal()) {
            Optional<RasterFrameRow> newest = frames.newest(spec.id(), true);
            if (newest.isPresent()) {
                Optional<GridDefinition> grid = grids.find(newest.get().gridId());
                if (grid.isPresent()) {
                    return RasterPipeline.bounds(spec, grid.get());
                }
            }
        }
        return List.of(-180.0, -90.0, 180.0, 90.0);
    }

    private static boolean intersects(BboxFilter filter, List<Double> bounds) {
        List<Envelope> boxes = new ArrayList<>();
        if (bounds.get(0) > bounds.get(2)) {
            boxes.add(new Envelope(bounds.get(0), 180, bounds.get(1), bounds.get(3)));
            boxes.add(new Envelope(-180, bounds.get(2), bounds.get(1), bounds.get(3)));
        } else {
            boxes.add(new Envelope(bounds.get(0), bounds.get(2), bounds.get(1), bounds.get(3)));
        }
        for (Envelope wanted : filter.envelopes()) {
            for (Envelope box : boxes) {
                if (wanted.intersects(box)) {
                    return true;
                }
            }
        }
        return false;
    }

    private record Position(Instant time, String frameId) {}

    static String frameCursor(String productId, RasterFrameRow row) {
        return CURSOR_PREFIX + encode(productId + "\n" + row.validTime().toEpochMilli() + "\n" + row.frameId());
    }

    private static Position decodeFrameCursor(String cursor, String productId) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            if (!cursor.startsWith(CURSOR_PREFIX)) {
                throw new IllegalArgumentException("prefix");
            }
            String[] parts = decode(cursor.substring(CURSOR_PREFIX.length())).split("\n", -1);
            if (parts.length != 3 || !parts[0].equals(productId)) {
                throw new IllegalArgumentException("shape");
            }
            return new Position(Instant.ofEpochMilli(Long.parseLong(parts[1])), parts[2]);
        } catch (IllegalArgumentException e) {
            throw invalidCursor();
        }
    }

    private static String decodeProductCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        if (!cursor.startsWith(PRODUCT_CURSOR_PREFIX)) {
            throw invalidCursor();
        }
        try {
            return decode(cursor.substring(PRODUCT_CURSOR_PREFIX.length()));
        } catch (IllegalArgumentException e) {
            throw invalidCursor();
        }
    }

    private static ForesignalException invalidCursor() {
        return new ForesignalException(HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_CURSOR,
                "The cursor is not one this endpoint issued for this query.");
    }

    private static String encode(String text) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String token) {
        return new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
    }

    private static double normalizeLon(double lon) {
        return lon > 180 ? lon - 360 : lon;
    }

    private static double round(double v) {
        return Math.round(v * 10_000.0) / 10_000.0;
    }
}
