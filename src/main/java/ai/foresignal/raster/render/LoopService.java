package ai.foresignal.raster.render;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.raster.FrameKeys;
import ai.foresignal.raster.FrameResolver;
import ai.foresignal.raster.ProductCatalogService;
import ai.foresignal.raster.ProductRegistry;
import ai.foresignal.raster.ProductSpecJson;
import ai.foresignal.raster.ProductUrls;
import ai.foresignal.raster.RasterFrameQueries;
import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.source.spi.ProductSpec;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * Animation loops (prompt 045; Weather-Imagery-Design §6.3): the primary mobile and animation path.
 * The server snaps the box once, then returns its corners and up to {@value #MAX_FRAMES}
 * fully expanded {@code /image} URLs, one per frame, all over the same box and size, so a client
 * overlays them with one set of {@code coordinates} and never builds a URL.
 *
 * <ul>
 *   <li>{@code products=a[,b]}: the newest frames of one or two products (observation products: the
 *       newest complete frames up to {@code to}; forecast products: the steps of one run from
 *       {@code from}, default now);
 *   <li>{@code past=…&future=…}: a seamless loop, the newest observed frames up to now followed by
 *       forecast steps after now.
 * </ul>
 * A forecast product contributes the steps of exactly one run (never mixed); {@code runs} names it.
 */
@Service
public class LoopService {

    /** The most frames in a loop. */
    public static final int MAX_FRAMES = 24;

    /** Frames when the request does not say. */
    public static final int DEFAULT_FRAMES = 12;

    /** The widest loop image, pixels. */
    public static final int MAX_WIDTH = 1024;

    /** The narrowest loop image, pixels. */
    public static final int MIN_WIDTH = 64;

    /** The width when the request does not say, pixels. */
    public static final int DEFAULT_WIDTH = 512;

    /** The snapping grid of loop and image boxes, degrees. */
    public static final double SNAP_DEGREES = 0.01;

    private final ProductCatalogService catalog;
    private final RasterFrameQueries frames;
    private final FrameResolver resolver;
    private final RasterRenderService renderer;

    /** Creates the service. */
    public LoopService(ProductCatalogService catalog, RasterFrameQueries frames, FrameResolver resolver,
            RasterRenderService renderer) {
        this.catalog = catalog;
        this.frames = frames;
        this.resolver = resolver;
        this.renderer = renderer;
    }

    /**
     * A loop request, its box already resolved (from {@code bbox} or a place).
     *
     * @param products {@code products}, or empty
     * @param past {@code past}, or null
     * @param future {@code future}, or null
     * @param bbox {@code [west, south, east, north]}, WGS84
     * @param frames frames wanted, or null
     * @param from window start, or null
     * @param to window end, or null
     * @param width image width, or null
     * @param style the style (single product only), or null
     * @param run the forecast run selector, or null
     */
    public record LoopRequest(List<String> products, String past, String future, double[] bbox, Integer frames,
            Instant from, Instant to, Integer width, String style, String run) {}

    /**
     * Builds the loop document ({@code ProductLoop}).
     *
     * @param excludedSources sources the caller may not read
     * @param base the URL base
     * @throws ai.foresignal.api.error.ForesignalException {@code 400 VALIDATION_FAILED},
     *     {@code UNKNOWN_STYLE} or {@code RUN_REQUIRED}; {@code 404 UNKNOWN_PRODUCT};
     *     {@code 503 PRODUCT_UNAVAILABLE}
     */
    public Map<String, Object> loop(LoopRequest request, Set<String> excludedSources, String base, Instant now) {
        boolean seamless = request.past() != null || request.future() != null;
        List<String> ids = request.products() == null ? List.of() : request.products();
        if (seamless && !ids.isEmpty()) {
            throw ValidationException.forField("products", "use either products or past/future, not both");
        }
        if (seamless && (request.past() == null || request.future() == null)) {
            throw ValidationException.forField(request.past() == null ? "past" : "future",
                    "a seamless loop needs both past and future");
        }
        if (!seamless && (ids.isEmpty() || ids.size() > 2)) {
            throw ValidationException.forField("products", "name one or two products, or past and future");
        }
        int wanted = request.frames() == null ? DEFAULT_FRAMES : request.frames();
        if (wanted < 1 || wanted > MAX_FRAMES) {
            throw ValidationException.forField("frames", "must be between 1 and " + MAX_FRAMES);
        }
        int width = request.width() == null ? DEFAULT_WIDTH : request.width();
        if (width < MIN_WIDTH || width > MAX_WIDTH) {
            throw ValidationException.forField("width", "must be between " + MIN_WIDTH + " and " + MAX_WIDTH);
        }
        if (request.from() != null && request.to() != null && request.from().isAfter(request.to())) {
            throw ValidationException.forField("from", "must not be after to");
        }
        double[] box = snap(request.bbox(), true);
        int[] size = size(box, width, MAX_WIDTH);
        String bbox = boxText(box);

        List<String> productIds = seamless ? List.of(request.past(), request.future()) : ids;
        List<ProductRegistry.Registered> products = new ArrayList<>();
        for (String id : productIds) {
            ProductRegistry.Registered product = catalog.visible(id, excludedSources);
            RasterRenderService.requireRendered(product.spec());
            catalog.requireAvailable(product);
            products.add(product);
        }
        if (seamless) {
            if (products.get(0).spec().isForecast()) {
                throw ValidationException.forField("past", "must be an observation product");
            }
            if (!products.get(1).spec().isForecast()) {
                throw ValidationException.forField("future", "must be a forecast product");
            }
        }

        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> runs = new LinkedHashMap<>();
        List<Map<String, Object>> attribution = new ArrayList<>();
        String legendUrl = null;
        int pastCount = 0;
        for (int k = 0; k < products.size(); k++) {
            ProductRegistry.Registered product = products.get(k);
            ProductSpec spec = product.spec();
            StyleRegistry.Resolved style = renderer.style(spec, products.size() == 1 ? request.style() : null);
            int quota;
            Instant from = request.from();
            Instant to = request.to();
            if (seamless) {
                quota = k == 0 ? wanted - wanted / 2 : wanted - pastCount;
                if (k == 0) {
                    to = to == null || to.isAfter(now) ? now : to;
                } else {
                    from = from == null || from.isBefore(now) ? now.plusNanos(1) : from;
                }
            } else {
                quota = Math.max(1, wanted / products.size());
            }
            List<RasterFrameRow> rows;
            if (spec.isForecast()) {
                Instant run = resolver.selectRun(spec, request.run() == null ? FrameResolver.RUN_LATEST : request.run(),
                        false);
                runs.put(spec.id(), FrameKeys.runIso(run));
                Instant start = from == null ? now : from;
                Instant end = to;
                rows = frames.runFrames(spec.id(), run, false).stream()
                        .filter(f -> !f.validTime().isBefore(start) && (end == null || !f.validTime().isAfter(end)))
                        .sorted(Comparator.comparing(RasterFrameRow::validTime))
                        .limit(quota).toList();
            } else {
                List<RasterFrameRow> newest = new ArrayList<>(frames.page(spec.id(), from,
                        to == null ? now : to, null, false, null, null, quota));
                Collections.reverse(newest);
                rows = newest;
            }
            if (k == 0) {
                pastCount = rows.size();
            }
            for (RasterFrameRow row : rows) {
                items.add(frame(base, spec, row, style, bbox, size, box));
            }
            if (legendUrl == null) {
                legendUrl = ProductUrls.legend(base, spec.id(), style.ref());
            }
            if (spec.attribution() != null) {
                Map<String, Object> a = ProductSpecJson.attribution(spec.attribution());
                if (!attribution.contains(a)) {
                    attribution.add(a);
                }
            }
        }
        if (!seamless && products.size() > 1) {
            items.sort(Comparator.comparing((Map<String, Object> m) -> (String) m.get("time"))
                    .thenComparing(m -> (String) m.get("productId")));
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("crs", "EPSG:3857");
        out.put("bounds", List.of(box[0], box[1], box[2], box[3]));
        out.put("coordinates", List.of(List.of(box[0], box[3]), List.of(box[2], box[3]), List.of(box[2], box[1]),
                List.of(box[0], box[1])));
        out.put("width", size[0]);
        out.put("height", size[1]);
        out.put("products", productIds);
        out.put("runs", runs);
        out.put("frames", items);
        out.put("legendUrl", legendUrl);
        out.put("attribution", attribution);
        out.put("serverTime", now.toString());
        return out;
    }

    private Map<String, Object> frame(String base, ProductSpec spec, RasterFrameRow row, StyleRegistry.Resolved style,
            String bbox, int[] size, double[] box) {
        Map<String, Object> f = new LinkedHashMap<>();
        f.put("productId", spec.id());
        f.put("time", row.validTime().toString());
        f.put("timeKey", row.timeKey());
        f.put("frameKey", row.frameKey());
        f.put("kind", spec.isForecast() ? "forecast" : "observed");
        f.put("run", row.referenceTime() == null ? null : FrameKeys.runIso(row.referenceTime()));
        f.put("step", row.forecastStep());
        f.put("url", ProductUrls.image(base, spec, row, style.ref(), bbox, size[0], size[1]));
        byte[] cached = renderer.cached(RasterRenderService.imageKey(spec, row, style,
                new RasterRenderService.ImageRequest(box[0], box[1], box[2], box[3], size[0], size[1], true, false)));
        f.put("bytes", cached == null ? null : cached.length);
        f.put("expiresAt", row.expiresAt() == null ? null : row.expiresAt().toString());
        return f;
    }

    /**
     * Snaps a box outwards to {@value #SNAP_DEGREES}° and clamps its latitudes (to WebMercator's range
     * for EPSG:3857), so nearby requests share one box, one index map and one cache entry.
     */
    public static double[] snap(double[] box, boolean mercator) {
        double limit = mercator ? Math.floor(WebMercator.MAX_LAT * 100) / 100 : 90.0;
        double west = Math.max(-180, floor(box[0]));
        double east = Math.min(180, ceil(box[2]));
        double south = Math.max(-limit, floor(box[1]));
        double north = Math.min(limit, ceil(box[3]));
        if (!(south < north) || west == east) {
            throw ValidationException.forField("bbox", "is empty after clamping to " + (mercator ? "WebMercator" : "WGS84"));
        }
        return new double[] {west, south, east, north};
    }

    /**
     * The pixel size of a box at {@code width}, keeping its aspect in the output projection; a box
     * taller than wide shrinks so neither side exceeds {@code maxSide}.
     */
    public static int[] size(double[] box, int width, int maxSide) {
        double east = box[2] < box[0] ? box[2] + 360 : box[2];
        double w = WebMercator.x(east) - WebMercator.x(box[0]);
        double h = WebMercator.y(box[3]) - WebMercator.y(box[1]);
        int height = (int) Math.max(1, Math.round(width * h / w));
        if (height > maxSide) {
            width = (int) Math.max(1, Math.round(width * (double) maxSide / height));
            height = maxSide;
        }
        return new int[] {width, height};
    }

    /** {@code w,s,e,n} without trailing zeros. */
    public static String boxText(double[] box) {
        return plain(box[0]) + "," + plain(box[1]) + "," + plain(box[2]) + "," + plain(box[3]);
    }

    private static String plain(double v) {
        return BigDecimal.valueOf(v).stripTrailingZeros().toPlainString();
    }

    private static double floor(double v) {
        return BigDecimal.valueOf(Math.floor(v / SNAP_DEGREES + 1e-9)).multiply(BigDecimal.valueOf(SNAP_DEGREES))
                .doubleValue();
    }

    private static double ceil(double v) {
        return BigDecimal.valueOf(Math.ceil(v / SNAP_DEGREES - 1e-9)).multiply(BigDecimal.valueOf(SNAP_DEGREES))
                .doubleValue();
    }
}
