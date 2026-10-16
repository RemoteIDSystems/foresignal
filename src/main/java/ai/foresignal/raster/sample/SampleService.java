package ai.foresignal.raster.sample;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.collect.HealthService;
import ai.foresignal.raster.FrameKeys;
import ai.foresignal.raster.FrameResolver;
import ai.foresignal.raster.FrameStore;
import ai.foresignal.raster.GridDef;
import ai.foresignal.raster.ProductRegistry;
import ai.foresignal.raster.ProductStatusTracker;
import ai.foresignal.raster.RasterFrameQueries;
import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.raster.lightning.FlashStore;
import ai.foresignal.raster.model.VectorTileService;
import ai.foresignal.raster.render.RasterRenderService;
import ai.foresignal.raster.render.UnitConversion;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.spi.FlashSink;
import ai.foresignal.source.spi.ProductSpec;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Point, series and frame resolution for sampling (prompt 052; Weather-Imagery-Design §6.5), and the
 * read-only {@link RasterReader} the DERIVED generators get.
 *
 * <p><b>Status</b> of each value, decided in this order:
 * <ol>
 *   <li>{@code unavailable}: the product is unknown to sampling (external), disabled, its source is
 *       failing, it has no frame, or the frame expired (the file is gone or past {@code expiresAt});
 *   <li>{@code no-coverage}: the point is off the grid, or the nearest cell holds a code meaning no
 *       coverage (MRMS code 0), with the text "outside product coverage; this is not evidence of clear
 *       weather";
 *   <li>{@code no-data}: inside coverage, but the sample is the {@code nodata} code;
 *   <li>{@code stale}: the frame is older than the product's {@code maxGap} relative to the requested
 *       time;
 *   <li>{@code ok}.
 * </ol>
 * A non-ok value carries {@code statusText} and no {@code value} (unknown is never clear).
 *
 * <p><b>Time.</b> {@code latest} is the newest frame (judged against now); {@code now}, an ISO time and
 * a past offset choose the observation frame valid at or before the time; a future offset, and any time
 * on a forecast product, uses the newest complete forecast run (or the pinned {@code run}), taking the
 * step valid at or before the time, or the first step when the time precedes the run.
 *
 * <p>Sampling never triggers an upstream fetch: it reads stored frames only.
 */
@Service
public class SampleService implements RasterReader, ai.foresignal.ingest.IngestStateReset {

    /** The most steps a series returns. */
    public static final int MAX_SERIES_STEPS = 500;

    /** The most points of one {@code POST /v1/sample}. */
    public static final int MAX_POINTS = 100;

    /** The most products of one sample request. */
    public static final int MAX_PRODUCTS = 20;

    /** The most points of one track sample (prompt 106). */
    public static final int MAX_TRACK_POINTS = 500;

    private static final String[] COMPASS = {"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW",
        "WSW", "W", "WNW", "NW", "NNW"};

    private final ProductRegistry registry;
    private final ProductStatusTracker status;
    private final RasterFrameQueries frames;
    private final FrameStore store;
    private final FrameResolver resolver;
    private final RasterRenderService renderer;
    private final ObjectProvider<HealthService> health;
    private final ObjectProvider<FlashStore> flashStore;
    private final Clock clock;
    private final Cache<String, HealthStatus> healthCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofSeconds(10)).maximumSize(2_000).build();

    /** Creates the service. */
    public SampleService(ProductRegistry registry, ProductStatusTracker status, RasterFrameQueries frames,
            FrameStore store, FrameResolver resolver, RasterRenderService renderer, ObjectProvider<HealthService> health,
            ObjectProvider<FlashStore> flashStore, Clock clock) {
        this.registry = registry;
        this.status = status;
        this.frames = frames;
        this.store = store;
        this.resolver = resolver;
        this.renderer = renderer;
        this.health = health;
        this.flashStore = flashStore;
        this.clock = clock;
    }

    // ------------------------------------------------------------------ RasterReader

    @Override
    public Optional<ProductSpec> product(String productId) {
        return registry.find(productId).map(ProductRegistry.Registered::spec);
    }

    @Override
    public Optional<String> unavailableReason(String productId) {
        Optional<ProductRegistry.Registered> product = registry.find(productId);
        if (product.isEmpty()) {
            return Optional.of("product " + productId + " is not installed");
        }
        return unavailableReason(product.get());
    }

    /** {@link #unavailableReason(String)} of a registered product. */
    public Optional<String> unavailableReason(ProductRegistry.Registered product) {
        if (product.spec().isExternal()) {
            return Optional.of("product " + product.id() + " is an external catalog entry with no frames");
        }
        Optional<ProductStatusTracker.Report> disabled = status.disabled(product.id());
        if (disabled.isPresent()) {
            return Optional.of("product " + product.id() + " is disabled: " + disabled.get().reason());
        }
        if (sourceStatus(product.sourceId()) == HealthStatus.FAILING) {
            return Optional.of("source " + product.sourceId() + " is failing");
        }
        return Optional.empty();
    }

    /** The cached health status of a source; {@code IDLE} when unknown. */
    public HealthStatus sourceStatus(String sourceId) {
        HealthStatus cached = healthCache.getIfPresent(sourceId);
        if (cached != null) {
            return cached;
        }
        HealthStatus s = HealthStatus.IDLE;
        HealthService service = health == null ? null : health.getIfAvailable();
        if (service != null) {
            try {
                s = service.health(sourceId).status();
            } catch (RuntimeException e) {
                s = HealthStatus.IDLE;
            }
        }
        healthCache.put(sourceId, s);
        return s;
    }

    /** Forgets cached source health (tests). */
    public void invalidateHealth() {
        healthCache.invalidateAll();
    }

    /** Forgets cached source health when the store's state is reset ({@link ai.foresignal.ingest.IngestStateReset}). */
    @Override
    public void resetState() {
        invalidateHealth();
    }

    @Override
    public Optional<RasterFrameRow> newestFrame(String productId) {
        return frames.newest(productId, false);
    }

    @Override
    public List<RasterFrameRow> frames(String productId, Instant from, Instant to) {
        List<RasterFrameRow> rows = new ArrayList<>(frames.page(productId, from, to, null, false, null, null,
                MAX_SERIES_STEPS + 1));
        rows.removeIf(RasterFrameRow::forecast);
        Collections.reverse(rows);
        return rows;
    }

    @Override
    public Optional<Instant> latestCompleteRun(String productId) {
        return frames.runs(productId).stream().filter(RasterFrameQueries.RunSummary::complete).findFirst()
                .map(RasterFrameQueries.RunSummary::run);
    }

    @Override
    public List<RasterFrameRow> runFrames(String productId, Instant run) {
        return frames.runFrames(productId, run, false);
    }

    @Override
    public Optional<FrameView> view(RasterFrameRow frame) {
        if (frame == null || !store.exists(frame.path())) {
            return Optional.empty();
        }
        Optional<ProductSpec> spec = product(frame.productId());
        if (spec.isEmpty()) {
            return Optional.empty();
        }
        try {
            GridDef grid = renderer.projection(frame.gridId());
            return Optional.of(new FrameView(spec.get(), frame, grid, store.read(frame.path())));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    @Override
    public FlashSink flashes() {
        FlashStore s = flashStore == null ? null : flashStore.getIfAvailable();
        if (s == null) {
            throw new UnsupportedOperationException("No GLM flash store is installed");
        }
        return s;
    }

    // ------------------------------------------------------------------ frame choice

    /**
     * The frame a time expression names for a product, with the status the choice implies.
     *
     * @param run the forecast run selector ({@code latest}, {@code previous}, a run time), or null for the
     *     newest complete run
     */
    public FrameChoice choose(ProductRegistry.Registered product, TimeSpec time, String run, Instant now) {
        Optional<String> reason = unavailableReason(product);
        if (reason.isPresent()) {
            return FrameChoice.unavailable(reason.get());
        }
        ProductSpec spec = product.spec();
        Instant target = time.target(now);
        Duration maxGap = maxGap(spec);
        RasterFrameRow frame;
        if (spec.isForecast()) {
            Instant selected;
            try {
                selected = resolver.selectRun(spec, run == null || run.isBlank() ? FrameResolver.RUN_LATEST : run, false);
            } catch (ForesignalException e) {
                if (ErrorCodes.VALIDATION_FAILED.equals(e.getCode())) {
                    throw e;
                }
                return FrameChoice.unavailable("product " + spec.id() + " has no complete forecast run"
                        + (run == null ? "" : " " + run));
            }
            List<RasterFrameRow> steps = frames.runFrames(spec.id(), selected, false);
            if (steps.isEmpty()) {
                return FrameChoice.unavailable("product " + spec.id() + " has no frame in run " + FrameKeys.runIso(selected));
            }
            frame = time.kind() == TimeSpec.Kind.LATEST ? nearest(steps, now) : atOrBefore(steps, target);
        } else {
            if (time.kind() != TimeSpec.Kind.LATEST && time.future(now)) {
                return FrameChoice.unavailable("product " + spec.id() + " is an observation product; it has no frames"
                        + " after now");
            }
            Optional<RasterFrameRow> found = time.kind() == TimeSpec.Kind.LATEST ? frames.newest(spec.id(), false)
                    : frames.newestAtOrBefore(spec.id(), target, false);
            if (found.isEmpty()) {
                return FrameChoice.unavailable("product " + spec.id() + " has no frame at or before " + target);
            }
            frame = found.get();
        }
        if ((frame.expiresAt() != null && frame.expiresAt().isBefore(now)) || !store.exists(frame.path())) {
            return FrameChoice.unavailable("frame " + frame.frameKey() + " of " + spec.id() + " expired");
        }
        if (maxGap != null && Duration.between(frame.validTime(), target).abs().compareTo(maxGap) > 0) {
            return FrameChoice.stale(frame, "the frame is " + minutes(Duration.between(frame.validTime(), target).abs())
                    + " min from the requested time; the product's maximum gap is " + minutes(maxGap) + " min");
        }
        return FrameChoice.current(frame);
    }

    /** The product's {@code maxGap}, else three cadences, else null. */
    public static Duration maxGap(ProductSpec spec) {
        if (spec.temporal() == null) {
            return null;
        }
        if (spec.temporal().maxGap() != null) {
            return spec.temporal().maxGap();
        }
        return spec.temporal().cadence() == null ? null : spec.temporal().cadence().multipliedBy(3);
    }

    private static RasterFrameRow nearest(List<RasterFrameRow> steps, Instant now) {
        return steps.stream().min(Comparator.comparingLong((RasterFrameRow f) ->
                Math.abs(Duration.between(now, f.validTime()).toMillis())).thenComparing(RasterFrameRow::validTime))
                .orElseThrow();
    }

    private static RasterFrameRow atOrBefore(List<RasterFrameRow> steps, Instant target) {
        RasterFrameRow best = null;
        for (RasterFrameRow f : steps) {
            if (!f.validTime().isAfter(target)) {
                best = f;
            }
        }
        return best == null ? steps.get(0) : best;
    }

    // ------------------------------------------------------------------ point samples

    /**
     * A sample point.
     *
     * @param lon longitude
     * @param lat latitude
     * @param placeId the place sampled, or null for coordinates
     * @param zone the time zone of {@code localTime}: the place's, else UTC
     */
    public record Point(double lon, double lat, String placeId, ZoneId zone) {

        /** Defaults the zone to UTC. */
        public Point {
            zone = zone == null ? ZoneOffset.UTC : zone;
        }
    }

    /**
     * The {@code SampleResponse} of one point.
     *
     * @param products product ids, each visible to the caller
     * @param excludedSources sources the caller may not read: their products are {@code 404 UNKNOWN_PRODUCT}
     */
    public Map<String, Object> sample(Point point, List<String> products, TimeSpec time, String run, boolean imperial,
            Set<String> excludedSources) {
        Instant now = clock.instant();
        List<ProductRegistry.Registered> resolved = resolveProducts(products, excludedSources);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("lon", point.lon());
        out.put("lat", point.lat());
        out.put("placeId", point.placeId());
        out.put("timezone", point.zone().getId());
        out.put("time", time.text());
        out.put("serverTime", now.toString());
        List<Map<String, Object>> values = new ArrayList<>();
        for (ProductRegistry.Registered product : resolved) {
            values.add(value(product, point, time, run, imperial, now).toMap());
        }
        out.put("values", values);
        Map<String, Object> disagreement = disagreement(values);
        if (disagreement != null) {
            out.put("disagreement", disagreement);
        }
        return out;
    }

    /** One point of a track sample (prompt 106). */
    public record TrackPoint(double lon, double lat, Instant time) {}

    /**
     * Samples each point at its own time (prompt 106, {@code POST /v1/sample/track}): one value per product
     * per point, each with its status. A {@code maxGap} marks a value stale when the chosen frame is older
     * than it; future times use the newest complete run (or the pinned {@code run}).
     *
     * @return {@code {products, points:[{lon,lat,time,status,values[]}], serverTime}}
     */
    public Map<String, Object> sampleTrack(List<TrackPoint> points, List<String> products, Duration maxGap,
            String run, boolean imperial, Set<String> excludedSources) {
        if (points == null || points.isEmpty()) {
            throw ValidationException.forField("points", "name at least one point");
        }
        if (points.size() > MAX_TRACK_POINTS) {
            throw ValidationException.forField("points", "at most " + MAX_TRACK_POINTS + " points per track");
        }
        Instant now = clock.instant();
        List<ProductRegistry.Registered> resolved = resolveProducts(products, excludedSources);
        List<Map<String, Object>> items = new ArrayList<>();
        for (TrackPoint p : points) {
            checkPoint(p.lon(), p.lat());
            Point point = new Point(p.lon(), p.lat(), null, ZoneOffset.UTC);
            TimeSpec time = TimeSpec.at(p.time());
            List<Map<String, Object>> values = new ArrayList<>();
            for (ProductRegistry.Registered product : resolved) {
                Map<String, Object> value = new LinkedHashMap<>(value(product, point, time, run, imperial, now).toMap());
                applyMaxGap(value, maxGap, p.time());
                values.add(value);
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("lon", p.lon());
            item.put("lat", p.lat());
            item.put("time", p.time().toString());
            item.put("status", worst(values));
            item.put("values", values);
            items.add(item);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("products", resolved.stream().map(ProductRegistry.Registered::id).toList());
        out.put("points", items);
        out.put("serverTime", now.toString());
        return out;
    }

    /** Marks a value stale when the chosen frame is older than {@code maxGap} from the point's time. */
    static void applyMaxGap(Map<String, Object> value, Duration maxGap, Instant at) {
        if (maxGap == null || !SampleStatus.OK.wireValue().equals(value.get("status"))) {
            return;
        }
        Object validTime = value.get("validTime");
        if (validTime == null) {
            return;
        }
        try {
            Instant v = Instant.parse(validTime.toString());
            if (Duration.between(v, at).compareTo(maxGap) > 0) {
                value.put("status", SampleStatus.STALE.wireValue());
                value.put("statusText", SampleStatus.STALE.defaultText());
            }
        } catch (RuntimeException e) {
            // an unparseable valid time leaves the status
        }
    }

    /** The worst status among a point's values, for the per-point status. */
    static String worst(List<Map<String, Object>> values) {
        int worst = 0;
        String wire = SampleStatus.OK.wireValue();
        for (Map<String, Object> v : values) {
            int rank = rank(String.valueOf(v.get("status")));
            if (rank > worst) {
                worst = rank;
                wire = String.valueOf(v.get("status"));
            }
        }
        return wire;
    }

    private static int rank(String status) {
        return switch (status) {
            case "unavailable" -> 4;
            case "no-coverage" -> 3;
            case "no-data" -> 2;
            case "stale", "stale-position" -> 1;
            default -> 0;
        };
    }

    /**
     * The model-disagreement block of a sample (prompt 105): when two or more model products (gust, 24-hour
     * precipitation or 2 m temperature) are sampled together, their spread over the fields that disagree.
     * Null when fewer than two models or none disagree.
     */
    static Map<String, Object> disagreement(List<Map<String, Object>> values) {
        Map<String, Map<String, Double>> fields = new LinkedHashMap<>();
        for (Map<String, Object> v : values) {
            Object productId = v.get("productId");
            Object value = v.get("value");
            if (!(productId instanceof String id) || !(value instanceof Number n) || !"ok".equals(v.get("status"))) {
                continue;
            }
            String field = disagreementField(id);
            if (field == null) {
                continue;
            }
            int dash = id.indexOf('-');
            String model = dash < 0 ? id : id.substring(0, dash);
            fields.computeIfAbsent(field, k -> new LinkedHashMap<>()).put(model, n.doubleValue());
        }
        return ModelDisagreement.compute(fields, Map.of());
    }

    /** The disagreement field of a model product id, or null when it is not one. */
    static String disagreementField(String productId) {
        if (productId.endsWith("-gust")) {
            return "gust";
        }
        if (productId.endsWith("-t2m")) {
            return "t2mMax";
        }
        if ("ifs-precip-24h".equals(productId)) {
            return "precip24h";
        }
        return null;
    }

    /**
     * The products named, in order, without duplicates.
     *
     * @throws ForesignalException {@code 404 UNKNOWN_PRODUCT}; {@code 400} for too many
     */
    public List<ProductRegistry.Registered> resolveProducts(List<String> products, Set<String> excludedSources) {
        if (products == null || products.isEmpty()) {
            throw ValidationException.forField("products", "name at least one product");
        }
        List<String> ids = products.stream().distinct().toList();
        if (ids.size() > MAX_PRODUCTS) {
            throw ValidationException.forField("products", "at most " + MAX_PRODUCTS + " products per request");
        }
        List<ProductRegistry.Registered> out = new ArrayList<>();
        for (String id : ids) {
            ProductRegistry.Registered product = registry.find(id).orElseThrow(() -> ProductRegistry.unknownProduct(id));
            if (excludedSources != null && excludedSources.contains(product.sourceId())) {
                throw ProductRegistry.unknownProduct(id);
            }
            out.add(product);
        }
        return out;
    }

    /** One product's value at a point. */
    public SampleValue value(ProductRegistry.Registered product, Point point, TimeSpec time, String run,
            boolean imperial, Instant now) {
        ProductSpec spec = product.spec();
        FrameChoice choice = choose(product, time, run, now);
        if (!choice.hasFrame()) {
            return SampleValue.unknown(spec, SampleStatus.UNAVAILABLE, choice.text(), imperial);
        }
        Optional<FrameView> view = view(choice.frame());
        if (view.isEmpty()) {
            return SampleValue.unknown(spec, SampleStatus.UNAVAILABLE, "frame " + choice.frame().frameKey()
                    + " could not be read", imperial);
        }
        return valueOf(view.get(), point, choice.status(), choice.text(), imperial, now);
    }

    /**
     * The value of a frame at a point.
     *
     * @param choiceStatus {@code stale} or null, from the frame choice
     */
    public SampleValue valueOf(FrameView view, Point point, SampleStatus choiceStatus, String choiceText,
            boolean imperial, Instant now) {
        ProductSpec spec = view.spec();
        RasterFrameRow frame = view.frame();
        FrameView.Sample sample = view.sample(point.lon(), point.lat());
        SampleStatus st;
        String text;
        switch (sample.kind()) {
            case NO_COVERAGE -> {
                st = SampleStatus.NO_COVERAGE;
                text = SampleStatus.NO_COVERAGE.defaultText();
            }
            case NO_DATA -> {
                st = SampleStatus.NO_DATA;
                text = SampleStatus.NO_DATA.defaultText();
            }
            default -> {
                st = choiceStatus == null ? SampleStatus.OK : choiceStatus;
                text = choiceStatus == null ? null : choiceText;
            }
        }
        UnitConversion conversion = UnitConversion.of(spec.units(), imperial);
        boolean wind = isWind(spec, view);
        Double value = null;
        Double u = null;
        Double v = null;
        Map<String, Object> derived = null;
        String label = null;
        if (st == SampleStatus.OK) {
            if (wind) {
                u = round(conversion.apply(sample.value()), 2);
                v = round(conversion.apply(sample.second()), 2);
                derived = derived(sample.value(), sample.second());
                label = derived.get("speedMs") + " m/s from " + derived.get("compass");
            } else {
                value = round(conversion.apply(sample.value()), 3);
                label = sample.meaning() != null ? humanize(sample.meaning())
                        : UnitConversion.formatReading(value) + (conversion.label() == null ? "" : " " + conversion.label());
            }
        }
        Instant validTime = frame == null ? null : frame.validTime();
        return new SampleValue(spec.id(), value, u, v, derived, conversion.units(), label,
                frame == null ? null : frame.frameKey(), validTime,
                validTime == null ? null : validTime.atZone(point.zone()).toOffsetDateTime().truncatedTo(ChronoUnit.SECONDS).toString(),
                validTime == null ? null : ageMinutes(validTime, now),
                frame == null || frame.referenceTime() == null ? null : FrameKeys.runIso(frame.referenceTime()),
                frame == null ? null : frame.forecastStep(), sample.method(),
                spec.spatial() == null ? null : spec.spatial().coverage(), st, st.ok() ? null : (text == null
                        ? st.defaultText() : text), attribution(spec));
    }

    /** True for a u/v field product: two or more bands and {@code kind=field}. */
    static boolean isWind(ProductSpec spec, FrameView view) {
        return ProductSpec.KIND_FIELD.equals(spec.kind()) && view.bands() >= 2;
    }

    /** {@code {speedMs, fromDeg, compass}} of a u/v vector (m/s), reusing the barb maths (prompt 051). */
    public static Map<String, Object> derived(double u, double v) {
        double speed = Math.hypot(u, v);
        int from = VectorTileService.fromDeg(u, v);
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("speedMs", Math.round(speed * 10) / 10.0);
        d.put("fromDeg", from);
        d.put("compass", compass(from));
        return d;
    }

    /** The 16-point compass name of a direction in degrees. */
    public static String compass(double deg) {
        int index = (int) Math.floor(((deg % 360 + 360) % 360) / 22.5 + 0.5) % 16;
        return COMPASS[index];
    }

    /** Minutes from {@code validTime} to {@code now}, to 0.1 (negative for a future step). */
    public static double ageMinutes(Instant validTime, Instant now) {
        return Math.round(Duration.between(validTime, now).toMillis() / 6_000.0) / 10.0;
    }

    /** The structured attribution of a product. */
    public static Map<String, Object> attribution(ProductSpec spec) {
        Map<String, Object> a = new LinkedHashMap<>();
        ProductSpec.Attribution at = spec.attribution();
        a.put("text", at == null || at.text() == null ? spec.title() : at.text());
        a.put("html", at == null ? null : at.html());
        a.put("url", at == null ? null : at.url());
        a.put("licence", at == null || at.licence() == null ? "unknown" : at.licence());
        a.put("licenceUrl", at == null ? null : at.licenceUrl());
        a.put("requiredDisplay", at != null && at.requiredDisplay());
        return a;
    }

    private static String humanize(String meaning) {
        StringBuilder out = new StringBuilder();
        for (char c : meaning.toCharArray()) {
            if (Character.isUpperCase(c)) {
                out.append(' ').append(Character.toLowerCase(c));
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    static Double round(double v, int decimals) {
        if (Double.isNaN(v) || Double.isInfinite(v)) {
            return null;
        }
        double f = Math.pow(10, decimals);
        return Math.round(v * f) / f;
    }

    private static long minutes(Duration d) {
        return Math.round(d.toSeconds() / 60.0);
    }

    // ------------------------------------------------------------------ series

    /**
     * The {@code Series} of one product at a point: observed frames for an observation product, the steps of
     * one forecast run for a forecast product, each with its status.
     *
     * @param from the start, or null (observations: now − 6 h; forecasts: the run's first step)
     * @param to the end, or null (observations: now; forecasts: the run's last step)
     * @throws ValidationException when {@code from} is after {@code to}, or the range holds more than
     *     {@value #MAX_SERIES_STEPS} steps
     */
    public Map<String, Object> series(ProductRegistry.Registered product, Point point, Instant from, Instant to,
            String run, boolean imperial) {
        Instant now = clock.instant();
        ProductSpec spec = product.spec();
        if (spec.isExternal()) {
            throw ValidationException.forField("productId", "an external product has no frames to sample");
        }
        if (from != null && to != null && from.isAfter(to)) {
            throw ValidationException.forField("from", "must not be after to");
        }
        List<RasterFrameRow> rows;
        String runIso = null;
        if (spec.isForecast()) {
            Instant selected;
            try {
                selected = resolver.selectRun(spec, run == null || run.isBlank() ? FrameResolver.RUN_LATEST : run, false);
                runIso = FrameKeys.runIso(selected);
                rows = new ArrayList<>(frames.runFrames(spec.id(), selected, false));
            } catch (ForesignalException e) {
                if (ErrorCodes.VALIDATION_FAILED.equals(e.getCode())) {
                    throw e;
                }
                rows = new ArrayList<>();
            }
            Instant f = from;
            Instant t = to;
            rows.removeIf(r -> (f != null && r.validTime().isBefore(f)) || (t != null && r.validTime().isAfter(t)));
        } else {
            Instant f = from == null ? now.minus(Duration.ofHours(6)) : from;
            Instant t = to == null ? now : (to.isAfter(now) ? now : to);
            rows = f.isAfter(t) ? new ArrayList<>() : frames(spec.id(), f, t);
        }
        if (rows.size() > MAX_SERIES_STEPS) {
            throw ValidationException.forField("from", "the range holds more than " + MAX_SERIES_STEPS
                    + " steps; narrow from and to");
        }
        String disabled = status.disabled(product.id()).map(r -> "product " + product.id() + " is disabled: " + r.reason())
                .orElse(null);
        UnitConversion conversion = UnitConversion.of(spec.units(), imperial);
        List<Map<String, Object>> items = new ArrayList<>();
        for (RasterFrameRow row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("time", row.validTime().toString());
            item.put("kind", row.forecast() ? "forecast" : "observed");
            if (disabled != null) {
                seriesUnknown(item, row, SampleStatus.UNAVAILABLE, disabled);
                items.add(item);
                continue;
            }
            Optional<FrameView> view = view(row);
            if (view.isEmpty()) {
                seriesUnknown(item, row, SampleStatus.UNAVAILABLE, "frame " + row.frameKey() + " expired");
                items.add(item);
                continue;
            }
            SampleValue value = valueOf(view.get(), point, null, null, imperial, now);
            item.put("value", value.value());
            item.put("u", value.u());
            item.put("v", value.v());
            item.put("derived", value.derived());
            item.put("frameKey", row.frameKey());
            item.put("run", value.run());
            item.put("step", row.forecastStep());
            item.put("status", value.status().wireValue());
            item.put("statusText", value.statusText());
            items.add(item);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("productId", spec.id());
        out.put("lon", point.lon());
        out.put("lat", point.lat());
        out.put("placeId", point.placeId());
        out.put("timezone", point.zone().getId());
        out.put("units", conversion.units());
        out.put("label", conversion.label());
        out.put("from", (rows.isEmpty() ? (from == null ? now : from) : rows.get(0).validTime()).toString());
        out.put("to", (rows.isEmpty() ? (to == null ? now : to) : rows.get(rows.size() - 1).validTime()).toString());
        out.put("run", runIso);
        out.put("items", items);
        out.put("attribution", attribution(spec));
        out.put("serverTime", now.toString());
        return out;
    }

    private static void seriesUnknown(Map<String, Object> item, RasterFrameRow row, SampleStatus st, String text) {
        item.put("value", null);
        item.put("frameKey", row.frameKey());
        item.put("run", row.referenceTime() == null ? null : FrameKeys.runIso(row.referenceTime()));
        item.put("step", row.forecastStep());
        item.put("status", st.wireValue());
        item.put("statusText", text);
    }

    /** The current instant of the service's clock. */
    public Instant now() {
        return clock.instant();
    }

    /**
     * A product that must exist and be visible.
     *
     * @throws ForesignalException {@code 404 UNKNOWN_PRODUCT}
     */
    public ProductRegistry.Registered visible(String productId, Set<String> excludedSources) {
        ProductRegistry.Registered product = registry.find(productId)
                .orElseThrow(() -> ProductRegistry.unknownProduct(productId));
        if (excludedSources != null && excludedSources.contains(product.sourceId())) {
            throw ProductRegistry.unknownProduct(productId);
        }
        return product;
    }

    /** {@code 400} for a point outside WGS84 ranges. */
    public static void checkPoint(double lon, double lat) {
        if (Double.isNaN(lon) || lon < -180 || lon > 180) {
            throw ValidationException.forField("lon", "must be between -180 and 180");
        }
        if (Double.isNaN(lat) || lat < -90 || lat > 90) {
            throw ValidationException.forField("lat", "must be between -90 and 90");
        }
    }

    /** {@code 404 UNKNOWN_PLACE}. */
    public static ForesignalException unknownPlace(String id) {
        return new ForesignalException(HttpStatus.NOT_FOUND, ErrorCodes.UNKNOWN_PLACE, "No place has the id '" + id + "'.");
    }
}
