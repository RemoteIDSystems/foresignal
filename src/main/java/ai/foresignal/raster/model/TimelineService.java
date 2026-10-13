package ai.foresignal.raster.model;

import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.raster.ExternalCatalog;
import ai.foresignal.raster.FrameKeys;
import ai.foresignal.raster.FrameResolver;
import ai.foresignal.raster.ProductCatalogService;
import ai.foresignal.raster.ProductRegistry;
import ai.foresignal.raster.ProductUrls;
import ai.foresignal.raster.RasterFrameQueries;
import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.source.spi.ProductSpec;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.stereotype.Service;

/**
 * The cross-product timeline (prompt 051; Weather-Imagery-Design §6.2):
 * {@code GET /v1/products/timeline?products=a,b&from&to} and
 * {@code ?mode=seamless&past=mrms-reflectivity&future=hrrr-refc}.
 *
 * <ul>
 *   <li>Ticks are the union of the products' frame valid times inside the window, at most
 *       {@value #MAX_TICKS} (more is {@code 400 VALIDATION_FAILED}).
 *   <li>Per tick and product, the frame valid at or before the tick with the smallest gap, within
 *       the product's {@code maxGap}; a product with no such frame is absent from that tick.
 *   <li>A forecast product contributes the steps of exactly one run, its newest complete run, never a
 *       mix ({@code runs} names it), as the loops do (prompt 045).
 *   <li>An external catalog entry gets synthesized ticks every {@code timeDimension.period} up to
 *       {@code now − defaultLag}, marked {@code availability: unverified}; Foresignal never checks them.
 *   <li>Seamless mode: the past (observation) product up to now, the future (forecast) product after
 *       now; ticks are {@code observed} up to now and {@code forecast} after.
 *   <li>Products mode: a tick is {@code forecast} when every frame at it comes from a forecast
 *       product, else {@code observed}.
 *   <li>{@code nowIndex} is the tick nearest now.
 * </ul>
 * Templates are fully expanded (content-addressed tiles, the run pinned); clients build no time
 * strings.
 */
@Service
public class TimelineService {

    /** The most ticks a timeline holds. */
    public static final int MAX_TICKS = 500;

    /** Products per timeline. */
    public static final int MAX_PRODUCTS = 10;

    /** The default window before now. */
    public static final Duration DEFAULT_PAST = Duration.ofHours(6);

    /** The default window after now when a forecast product is named. */
    public static final Duration DEFAULT_FUTURE = Duration.ofHours(48);

    /** The default seamless window before now. */
    public static final Duration SEAMLESS_PAST = Duration.ofHours(3);

    /** The default seamless window after now. */
    public static final Duration SEAMLESS_FUTURE = Duration.ofHours(18);

    private final ProductCatalogService catalog;
    private final RasterFrameQueries frames;
    private final FrameResolver resolver;
    private final ExternalCatalog external;

    /** Creates the service. */
    public TimelineService(ProductCatalogService catalog, RasterFrameQueries frames, FrameResolver resolver,
            ExternalCatalog external) {
        this.catalog = catalog;
        this.frames = frames;
        this.resolver = resolver;
        this.external = external;
    }

    /**
     * A timeline request.
     *
     * @param products {@code products}, or empty
     * @param mode {@code seamless}, or null
     * @param past the seamless observed product, or null
     * @param future the seamless forecast product, or null
     * @param from window start, or null
     * @param to window end, or null
     */
    public record TimelineRequest(List<String> products, String mode, String past, String future, Instant from,
            Instant to) {}

    /** One candidate frame of one product. */
    record Candidate(Instant time, RasterFrameRow frame, String template) {}

    /**
     * Builds the timeline ({@code ProductTimeline}).
     *
     * @param excludedSources sources the caller may not read
     * @param base the URL base
     * @throws ForesignalException {@code 400 VALIDATION_FAILED}, {@code 404 UNKNOWN_PRODUCT},
     *     {@code 503 PRODUCT_UNAVAILABLE}
     */
    public Map<String, Object> timeline(TimelineRequest request, Set<String> excludedSources, String base,
            Instant now) {
        boolean seamless = "seamless".equals(request.mode()) || request.past() != null || request.future() != null;
        if (request.mode() != null && !"seamless".equals(request.mode())) {
            throw ValidationException.forField("mode", "must be seamless");
        }
        List<String> ids = request.products() == null ? List.of() : request.products();
        if (seamless && !ids.isEmpty()) {
            throw ValidationException.forField("products", "use either products or mode=seamless with past and future");
        }
        if (seamless && (request.past() == null || request.future() == null)) {
            throw ValidationException.forField(request.past() == null ? "past" : "future",
                    "a seamless timeline needs both past and future");
        }
        if (!seamless && ids.isEmpty()) {
            throw ValidationException.forField("products", "name at least one product, or use mode=seamless");
        }
        if (ids.size() > MAX_PRODUCTS) {
            throw ValidationException.forField("products", "at most " + MAX_PRODUCTS + " products");
        }
        List<String> productIds = seamless ? List.of(request.past(), request.future()) : ids;
        List<ProductRegistry.Registered> products = new ArrayList<>();
        for (String id : productIds) {
            ProductRegistry.Registered product = catalog.visible(id, excludedSources);
            catalog.requireAvailable(product);
            products.add(product);
        }
        if (seamless) {
            ProductSpec past = products.get(0).spec();
            if (past.isForecast() || past.isExternal()) {
                throw ValidationException.forField("past", "must be an observation product");
            }
            if (!products.get(1).spec().isForecast()) {
                throw ValidationException.forField("future", "must be a forecast product");
            }
        }
        boolean anyForecast = products.stream().anyMatch(p -> p.spec().isForecast());
        Instant from = request.from() != null ? request.from()
                : now.minus(seamless ? SEAMLESS_PAST : DEFAULT_PAST);
        Instant to = request.to() != null ? request.to()
                : seamless ? now.plus(SEAMLESS_FUTURE) : anyForecast ? now.plus(DEFAULT_FUTURE) : now;
        if (from.isAfter(to)) {
            throw ValidationException.forField("from", "must not be after to");
        }

        Map<String, Object> runs = new LinkedHashMap<>();
        Map<String, List<Candidate>> candidates = new LinkedHashMap<>();
        TreeSet<Instant> ticks = new TreeSet<>();
        for (int k = 0; k < products.size(); k++) {
            ProductRegistry.Registered product = products.get(k);
            ProductSpec spec = product.spec();
            Instant lo = from;
            Instant hi = to;
            if (seamless && k == 0) {
                hi = hi.isAfter(now) ? now : hi;
            } else if (seamless) {
                lo = lo.isAfter(now) ? lo : now.plusNanos(1);
            }
            List<Candidate> list = candidates(base, spec, lo, hi, now, runs);
            candidates.put(spec.id(), list);
            for (Candidate c : list) {
                if (!c.time().isBefore(lo) && !c.time().isAfter(hi)) {
                    ticks.add(c.time());
                }
                if (ticks.size() > MAX_TICKS) {
                    throw ValidationException.forField("to", "the window holds more than " + MAX_TICKS
                            + " ticks; narrow from/to or name fewer products");
                }
            }
        }

        List<Map<String, Object>> tickList = new ArrayList<>();
        Integer nowIndex = null;
        long best = Long.MAX_VALUE;
        for (Instant tick : ticks) {
            List<Map<String, Object>> chosen = new ArrayList<>();
            boolean allForecast = true;
            for (int k = 0; k < products.size(); k++) {
                ProductSpec spec = products.get(k).spec();
                if (seamless && (k == 0) == tick.isAfter(now)) {
                    continue;
                }
                Candidate c = choose(candidates.get(spec.id()), tick, maxGap(spec));
                if (c == null) {
                    continue;
                }
                chosen.add(frameEntry(spec, c));
                allForecast &= spec.isForecast();
            }
            if (chosen.isEmpty()) {
                continue;
            }
            Map<String, Object> t = new LinkedHashMap<>();
            t.put("time", tick.toString());
            boolean forecast = seamless ? tick.isAfter(now) : allForecast;
            t.put("kind", forecast ? "forecast" : "observed");
            t.put("frames", chosen);
            long gap = Math.abs(Duration.between(now, tick).toMillis());
            if (gap < best) {
                best = gap;
                nowIndex = tickList.size();
            }
            tickList.add(t);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("mode", seamless ? "seamless" : "products");
        out.put("products", productIds);
        out.put("from", from.toString());
        out.put("to", to.toString());
        out.put("runs", runs);
        out.put("nowIndex", nowIndex);
        out.put("ticks", tickList);
        out.put("serverTime", now.toString());
        return out;
    }

    /** The frames (or synthesized external ticks) of one product in {@code [lo − maxGap, hi]}. */
    private List<Candidate> candidates(String base, ProductSpec spec, Instant lo, Instant hi, Instant now,
            Map<String, Object> runs) {
        List<Candidate> out = new ArrayList<>();
        Duration gap = maxGap(spec);
        Instant start = gap == null ? lo : lo.minus(gap);
        if (spec.isExternal()) {
            return externalTicks(spec, start, hi, now);
        }
        if (spec.isForecast()) {
            Instant run;
            try {
                run = resolver.selectRun(spec, FrameResolver.RUN_LATEST, false);
            } catch (ForesignalException e) {
                return out;
            }
            runs.put(spec.id(), FrameKeys.runIso(run));
            for (RasterFrameRow f : frames.runFrames(spec.id(), run, false)) {
                if (!f.validTime().isBefore(start) && !f.validTime().isAfter(hi)) {
                    out.add(new Candidate(f.validTime(), f, ProductUrls.tiles(base, spec, f)));
                }
            }
            return out;
        }
        List<RasterFrameRow> rows = frames.page(spec.id(), start, hi, null, false, null, null, MAX_TICKS * 2 + 1);
        for (int k = rows.size() - 1; k >= 0; k--) {
            RasterFrameRow f = rows.get(k);
            out.add(new Candidate(f.validTime(), f, ProductUrls.tiles(base, spec, f)));
        }
        return out;
    }

    /** Synthesized ticks of an external entry: every period, aligned to the epoch, up to now − defaultLag. */
    List<Candidate> externalTicks(ProductSpec spec, Instant start, Instant hi, Instant now) {
        List<Candidate> out = new ArrayList<>();
        Map<String, Object> doc = external.find(spec.id()).map(ExternalCatalog.Entry::document).orElse(null);
        if (doc == null || !(doc.get("externalTemplate") instanceof String template)) {
            return out;
        }
        Map<?, ?> dim = doc.get("timeDimension") instanceof Map<?, ?> m ? m : null;
        Duration period = dim == null ? null : duration(dim.get("period"));
        if (period == null || period.isZero()) {
            return out;
        }
        Duration lag = dim.get("defaultLag") == null ? Duration.ZERO : duration(dim.get("defaultLag"));
        Instant newest = now.minus(lag == null ? Duration.ZERO : lag);
        Instant end = hi.isBefore(newest) ? hi : newest;
        long p = period.toMillis();
        long first = Math.floorDiv(start.toEpochMilli() + p - 1, p) * p;
        String format = dim.get("format") instanceof String f ? f : "YYYY-MM-DDThh:mm:ssZ";
        for (long t = first; t <= end.toEpochMilli(); t += p) {
            Instant time = Instant.ofEpochMilli(t);
            out.add(new Candidate(time, null, template.replace("{time}", formatTime(time, format))));
            if (out.size() > MAX_TICKS * 2) {
                throw ValidationException.forField("to", "the window holds more than " + MAX_TICKS
                        + " ticks of " + spec.id() + "; narrow from/to");
            }
        }
        return out;
    }

    /** A time in an external entry's {@code timeDimension.format}. */
    static String formatTime(Instant time, String format) {
        if ("YYYY-MM-DD".equals(format)) {
            return DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneOffset.UTC).format(time);
        }
        return DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ROOT).withZone(ZoneOffset.UTC)
                .format(time.truncatedTo(ChronoUnit.SECONDS));
    }

    /** The candidate at or before {@code tick} with the smallest gap, within {@code maxGap}; null when none. */
    static Candidate choose(List<Candidate> candidates, Instant tick, Duration maxGap) {
        Candidate best = null;
        for (Candidate c : candidates) {
            if (c.time().isAfter(tick)) {
                break;
            }
            best = c;
        }
        if (best == null) {
            return null;
        }
        if (maxGap != null && Duration.between(best.time(), tick).compareTo(maxGap) > 0) {
            return null;
        }
        return best;
    }

    private static Map<String, Object> frameEntry(ProductSpec spec, Candidate c) {
        Map<String, Object> f = new LinkedHashMap<>();
        f.put("productId", spec.id());
        RasterFrameRow row = c.frame();
        f.put("frameKey", row == null ? null : row.frameKey());
        f.put("time", c.time().toString());
        f.put("run", row == null || row.referenceTime() == null ? null : FrameKeys.runIso(row.referenceTime()));
        f.put("step", row == null ? null : row.forecastStep());
        f.put("tiles", c.template());
        f.put("availability", row == null ? "unverified" : "available");
        return f;
    }

    /** The product's gap tolerance: {@code maxGap}, else its cadence; null when it has neither. */
    static Duration maxGap(ProductSpec spec) {
        if (spec.temporal() == null) {
            return null;
        }
        return spec.temporal().maxGap() != null ? spec.temporal().maxGap() : spec.temporal().cadence();
    }

    private static Duration duration(Object value) {
        if (!(value instanceof String text)) {
            return null;
        }
        try {
            Duration d = Duration.parse(text);
            return d.isNegative() ? null : d;
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
