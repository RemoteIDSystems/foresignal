package ai.foresignal.weather;

import ai.foresignal.raster.FrameKeys;
import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.raster.sample.AreaScan;
import ai.foresignal.raster.sample.FrameView;
import ai.foresignal.raster.sample.PlaceGridIndex;
import ai.foresignal.raster.sample.RasterReader;
import ai.foresignal.raster.sample.SampleService;
import ai.foresignal.raster.sample.SampleStatus;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.adapters.wx.WxRound;
import ai.foresignal.source.spi.FlashBatch;
import ai.foresignal.source.spi.FlashSink;
import ai.foresignal.source.spi.ProductSpec;
import jakarta.annotation.PreDestroy;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.util.AffineTransformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Computes the deterministic {@code context.weather} of a geometry at a time (prompt 056; Weather-Imagery-Design
 * §7.5, OpenAPI {@code WeatherContext}).
 *
 * <p><b>Inputs.</b> Only stored frames (through {@link RasterReader}), the GLM flash ring and current record heads
 * ({@link WeatherRecords}); never an upstream fetch (invariant SRC-6). The sections are computed in parallel on
 * virtual threads within a budget of {@value #BUDGET_MS} ms; a section that misses it is {@code unavailable} with
 * the text {@value #BUDGET_TEXT}.
 *
 * <p><b>Sections</b>, each {@code {status: ok|stale|no-coverage|unavailable, asOf?, statusText?, …}} with the
 * status rules of prompt 052 (an unknown, disabled or failing product, no frame or an unreadable one is
 * {@code unavailable}; no coverage at the geometry is {@code no-coverage}; a frame further from the time than the
 * product's maximum gap is {@code stale} with {@code asOf} = its valid time):
 * <ul>
 *   <li>{@code radar}: the {@code mrms-reflectivity} maximum within {@value #RADAR_KM} km of the geometry, and the
 *       nearest {@code radar-cell} ({@code wx-cells}) within {@value #CELL_SEARCH_KM} km with its {@code motion}
 *       and {@code meshMax};
 *   <li>{@code lightning}: GOES GLM flashes within {@value #LIGHTNING_KM} km over the 15 minutes before the time
 *       (from the 60-minute ring; stale when no GLM file ended within 2 minutes of the time). GLM's footprint over
 *       Texas is about 8–10 km and its detection efficiency is below 100%, so a zero count is advisory;
 *   <li>{@code precip}: the {@code mrms-precip-rate} rate at the geometry's representative point, plus the
 *       {@code mrms-qpe-1h} total there when that product is installed and current;
 *   <li>{@code warnings}: up to {@value #MAX_WARNINGS} active {@code nws-alert} records intersecting the geometry
 *       (event, severity, expires, id), each marked {@code untrusted} because its text is upstream data;
 *   <li>{@code wind}: the 10-m wind and gust of the newest complete HRRR run (else GFS) at the step nearest the
 *       time, with {@code run}, {@code validTime} and {@code step};
 *   <li>{@code smoke}: HRRR near-surface smoke (µg/m³) at the step nearest the time.
 * </ul>
 * A product whose source is disabled gives an {@code unavailable} section, never an omitted one. The
 * {@code summary} is {@link WeatherSummary#of}.
 */
@Service
public class WeatherContextService {

    private static final Logger log = LoggerFactory.getLogger(WeatherContextService.class);

    /** The computation budget in milliseconds. */
    public static final long BUDGET_MS = 500;

    /** The computation budget. */
    public static final Duration BUDGET = Duration.ofMillis(BUDGET_MS);

    /** The text of a section that missed the budget. */
    public static final String BUDGET_TEXT = "not computed within the 500 ms budget; this is not evidence of clear weather";

    /** MRMS reflectivity. */
    public static final String REFLECTIVITY = "mrms-reflectivity";

    /** MRMS precipitation rate. */
    public static final String PRECIP = "mrms-precip-rate";

    /** MRMS radar-only 1-hour QPE (prompt 053). */
    public static final String QPE_1H = "mrms-qpe-1h";

    /** HRRR 10-m wind. */
    public static final String HRRR_WIND = "hrrr";

    /** HRRR gusts. */
    public static final String HRRR_GUST = "hrrr-gust";

    /** GFS 10-m wind. */
    public static final String GFS_WIND = "gfs";

    /** GFS gusts. */
    public static final String GFS_GUST = "gfs-gust";

    /** HRRR near-surface smoke. */
    public static final String SMOKE = "hrrr-smoke";

    /** The GOES GLM source. */
    public static final String GLM = "goes-glm";

    /** The radar-cell source. */
    public static final String CELLS = "wx-cells";

    /** The radar-cell kind. */
    public static final String CELL_KIND = "radar-cell";

    /** The NWS alerts source. */
    public static final String NWS = "nws-alerts";

    /** The NWS alert kind. */
    public static final String NWS_KIND = "nws-alert";

    /** The radar radius, km. */
    public static final double RADAR_KM = 10;

    /** The lightning radius, km. */
    public static final double LIGHTNING_KM = 20;

    /** The radar-cell search radius, km. */
    public static final double CELL_SEARCH_KM = 100;

    /** The most NWS warnings listed. */
    public static final int MAX_WARNINGS = 5;

    /** The lightning window. */
    public static final Duration LIGHTNING_WINDOW = Duration.ofMinutes(15);

    /** The GLM maximum gap. */
    public static final Duration GLM_MAX_GAP = Duration.ofMinutes(2);

    /** What the GLM ring holds. */
    public static final Duration GLM_RING = Duration.ofMinutes(60);

    /** The gap of an MRMS product without a declared one. */
    public static final Duration MRMS_MAX_GAP = Duration.ofMinutes(6);

    /** The oldest model run still current. */
    public static final Duration RUN_MAX_AGE = Duration.ofHours(12);

    /** How far back an observation frame is looked for. */
    static final Duration LOOKBACK = Duration.ofHours(1);

    /** The products whose availability makes the context worth computing. */
    static final List<String> INPUT_PRODUCTS = List.of(REFLECTIVITY, PRECIP, HRRR_WIND, GFS_WIND, SMOKE);

    private final RasterReader rasters;
    private final Function<String, HealthStatus> health;
    private final java.util.function.Predicate<String> productEnabled;
    private final WeatherRecords records;
    private final Clock clock;
    private final Duration budget;
    private final ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor();

    /**
     * Creates the service on the stored frames.
     *
     * @param samples the frame reader and source health cache
     * @param registry the installed sources: a source that is not installed counts as disabled
     * @param records the record reads
     * @param clock the clock
     */
    @Autowired
    public WeatherContextService(SampleService samples, SourceRegistry registry,
            ai.foresignal.raster.ProductRegistry products, WeatherRecords records, Clock clock) {
        this(samples, id -> registry.contains(id) ? samples.sourceStatus(id) : HealthStatus.DISABLED,
                id -> products.find(id).map(p -> samples.sourceStatus(p.sourceId()) != HealthStatus.DISABLED)
                        .orElse(false),
                records, clock, BUDGET);
    }

    /**
     * Creates the service (tests).
     *
     * @param rasters the frame reader
     * @param health a source's health ({@code DISABLED} for an unknown source)
     * @param productEnabled whether a product's source is enabled
     * @param records the record reads
     * @param clock the clock
     * @param budget the computation budget
     */
    public WeatherContextService(RasterReader rasters, Function<String, HealthStatus> health,
            java.util.function.Predicate<String> productEnabled, WeatherRecords records, Clock clock, Duration budget) {
        this.rasters = rasters;
        this.health = health;
        this.productEnabled = productEnabled;
        this.records = records;
        this.clock = clock;
        this.budget = budget;
    }

    /** Stops the section threads. */
    @PreDestroy
    public void close() {
        pool.shutdownNow();
    }

    /** The current instant of the service's clock. */
    public Instant now() {
        return clock.instant();
    }

    /**
     * True when at least one weather input is installed and enabled (a radar, rain, model or smoke product, the
     * GLM source or the NWS alerts source), so a context can say something.
     */
    public boolean anyInputEnabled() {
        for (String product : INPUT_PRODUCTS) {
            if (unavailable(product).isEmpty()) {
                return true;
            }
        }
        return enabled(health.apply(GLM)) || enabled(health.apply(NWS));
    }

    /**
     * Why a product cannot give values now, or empty: {@link RasterReader#unavailableReason} (unknown, external,
     * disabled product, failing source), or its source is disabled.
     */
    Optional<String> unavailable(String productId) {
        Optional<String> reason = rasters.unavailableReason(productId);
        if (reason.isPresent()) {
            return reason;
        }
        return productEnabled.test(productId) ? Optional.empty()
                : Optional.of("the source of product " + productId + " is disabled");
    }

    private static boolean enabled(HealthStatus status) {
        return status != null && status != HealthStatus.DISABLED;
    }

    /**
     * The weather context of {@code geometry} at {@code at}.
     *
     * @param geometry a WGS84 geometry (point, line or polygon)
     * @param at the time judged (an alert's {@code observed_at}); null for now
     * @return the context; {@code unavailable} as a whole only when there is no geometry or no section could run
     */
    public WeatherContext compute(Geometry geometry, Instant at) {
        Instant t = at == null ? clock.instant() : at;
        if (geometry == null || geometry.isEmpty()) {
            return WeatherContext.unavailable(t, clock.instant());
        }
        Point center = geometry instanceof Point p ? p : geometry.getInteriorPoint();
        long deadline = System.nanoTime() + budget.toNanos();
        Map<String, Callable<Map<String, Object>>> tasks = new LinkedHashMap<>();
        tasks.put("radar", () -> radar(geometry, center, t));
        tasks.put("lightning", () -> lightning(geometry, center, t));
        tasks.put("precip", () -> precip(center, t));
        tasks.put("warnings", () -> warnings(geometry, t));
        tasks.put("wind", () -> wind(center, t));
        tasks.put("smoke", () -> smoke(center, t));
        Map<String, Future<Map<String, Object>>> futures = new LinkedHashMap<>();
        try {
            tasks.forEach((name, task) -> futures.put(name, pool.submit(task)));
        } catch (RejectedExecutionException e) {
            futures.values().forEach(f -> f.cancel(true));
            return WeatherContext.unavailable(t, clock.instant());
        }
        Map<String, Map<String, Object>> sections = new LinkedHashMap<>();
        for (Map.Entry<String, Future<Map<String, Object>>> entry : futures.entrySet()) {
            Future<Map<String, Object>> future = entry.getValue();
            long wait = Math.max(0, deadline - System.nanoTime());
            Map<String, Object> section;
            try {
                section = future.get(wait, TimeUnit.NANOSECONDS);
            } catch (TimeoutException e) {
                future.cancel(true);
                section = WeatherSections.unavailable(BUDGET_TEXT);
            } catch (ExecutionException e) {
                Throwable cause = e.getCause() == null ? e : e.getCause();
                log.debug("Weather section {} failed: {}", entry.getKey(), cause.toString());
                section = WeatherSections.unavailable("could not be computed (" + cause.getClass().getSimpleName()
                        + "); this is not evidence of clear weather");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                future.cancel(true);
                section = WeatherSections.unavailable(BUDGET_TEXT);
            }
            sections.put(entry.getKey(), section == null ? WeatherSections.unavailable(null) : section);
        }
        return new WeatherContext(WeatherSections.OK, t, clock.instant(), sections, WeatherSummary.of(sections, t));
    }

    // ------------------------------------------------------------------ radar

    Map<String, Object> radar(Geometry geometry, Point center, Instant t) {
        Optional<String> reason = unavailable(REFLECTIVITY);
        if (reason.isPresent()) {
            return WeatherSections.unavailable(reason.get() + "; this is not evidence of clear weather");
        }
        Choice choice = observation(REFLECTIVITY, t, MRMS_MAX_GAP);
        if (choice.frame() == null) {
            return choice.asOf() == null ? WeatherSections.unavailable(choice.text())
                    : WeatherSections.notOk(WeatherSections.STALE, choice.text(), choice.asOf());
        }
        Optional<FrameView> view = rasters.view(choice.frame());
        if (view.isEmpty()) {
            return WeatherSections.unavailable("frame " + choice.frame().frameKey() + " could not be read");
        }
        double max;
        boolean covered;
        if (geometry instanceof Point p) {
            PlaceGridIndex.Scan scan = PlaceGridIndex.scan(view.get(),
                    PlaceGridIndex.of(choice.frame().gridId(), view.get().grid(), p.getX(), p.getY()), new double[0],
                    RADAR_KM);
            covered = scan.radiusCells() > 0 && scan.coveredInRadius() * 2 >= scan.radiusCells();
            max = scan.maxInRadius();
        } else {
            AreaScan.Result result = AreaScan.scan(view.get(), bufferKm(geometry, RADAR_KM), new double[0], false);
            covered = result.cellsInArea() > 0 && result.coveredCells() * 2 >= result.cellsInArea();
            max = result.max();
        }
        if (!covered) {
            return WeatherSections.notOk(WeatherSections.NO_COVERAGE, null, null);
        }
        if (choice.stale()) {
            return WeatherSections.notOk(WeatherSections.STALE, choice.text(), choice.frame().validTime());
        }
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("maxDbz", WxRound.dbz(Double.isNaN(max) ? 0 : Math.max(0, max)));
        metrics.put("nearestCell", nearestCell(geometry));
        Map<String, Object> section = WeatherSections.ok(metrics);
        if (!section.containsKey("nearestCell")) {
            section.put("nearestCell", null);
        }
        return section;
    }

    /** The nearest current radar cell within {@value #CELL_SEARCH_KM} km, or null (none, or {@code wx-cells} down). */
    Map<String, Object> nearestCell(Geometry geometry) {
        HealthStatus status = health.apply(CELLS);
        if (status == HealthStatus.DISABLED || status == HealthStatus.FAILING) {
            return null;
        }
        List<WeatherRecords.Row> rows = records.nearby(CELLS, CELL_KIND, geometry, CELL_SEARCH_KM, 1);
        if (rows.isEmpty()) {
            return null;
        }
        WeatherRecords.Row row = rows.get(0);
        Map<String, Object> cell = new LinkedHashMap<>();
        cell.put("id", row.externalId());
        cell.put("distanceKm", WxRound.km(row.distanceKm() == null ? Double.NaN : row.distanceKm()));
        if (row.payload().get("motion") instanceof Map<?, ?> motion) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("speedKmh", motion.get("speedKmh"));
            m.put("bearingDeg", motion.get("bearingDeg"));
            m.put("method", motion.get("method"));
            cell.put("motion", m);
        }
        cell.put("meshMaxMm", row.payload().get("meshMax") instanceof Number n ? n : null);
        return cell;
    }

    // ------------------------------------------------------------------ lightning

    Map<String, Object> lightning(Geometry geometry, Point center, Instant t) {
        HealthStatus status = health.apply(GLM);
        if (status == HealthStatus.DISABLED) {
            return WeatherSections.unavailable("the goes-glm source is disabled or not installed; this is not"
                    + " evidence of clear weather");
        }
        if (status == HealthStatus.FAILING) {
            return WeatherSections.unavailable("the goes-glm source is failing; this is not evidence of clear weather");
        }
        FlashSink flashes;
        try {
            flashes = rasters.flashes();
        } catch (UnsupportedOperationException e) {
            return WeatherSections.unavailable("no GLM flash store is installed; this is not evidence of clear weather");
        }
        Optional<Instant> newest = flashes.newestFlashFileTime();
        if (newest.isEmpty()) {
            return WeatherSections.unavailable("no GLM file has been processed yet; this is not evidence of clear"
                    + " weather");
        }
        if (Duration.between(newest.get(), t).compareTo(GLM_MAX_GAP) > 0) {
            return WeatherSections.notOk(WeatherSections.STALE, "the newest GLM file ends "
                    + minutes(Duration.between(newest.get(), t)) + " min before the time; the maximum gap is 2 min",
                    newest.get());
        }
        Instant since = t.minus(LIGHTNING_WINDOW);
        if (since.isBefore(clock.instant().minus(GLM_RING))) {
            return WeatherSections.unavailable("the window is older than the 60-minute GLM ring; this is not evidence"
                    + " of clear weather");
        }
        int count;
        if (geometry instanceof Point p) {
            FlashBatch batch = flashes.flashesWithin(p, LIGHTNING_KM, since);
            long until = t.toEpochMilli();
            count = 0;
            for (long ms : batch.timesMs()) {
                if (ms <= until) {
                    count++;
                }
            }
        } else {
            count = flashes.countWithin(bufferKm(geometry, LIGHTNING_KM), since);
        }
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("flashes20km15m", count);
        return WeatherSections.ok(metrics);
    }

    // ------------------------------------------------------------------ precipitation

    Map<String, Object> precip(Point center, Instant t) {
        Optional<String> reason = unavailable(PRECIP);
        if (reason.isPresent()) {
            return WeatherSections.unavailable(reason.get() + "; this is not evidence of clear weather");
        }
        Choice choice = observation(PRECIP, t, MRMS_MAX_GAP);
        if (choice.frame() == null) {
            return choice.asOf() == null ? WeatherSections.unavailable(choice.text())
                    : WeatherSections.notOk(WeatherSections.STALE, choice.text(), choice.asOf());
        }
        Optional<FrameView> view = rasters.view(choice.frame());
        if (view.isEmpty()) {
            return WeatherSections.unavailable("frame " + choice.frame().frameKey() + " could not be read");
        }
        FrameView.Sample sample = view.get().sample(center.getX(), center.getY());
        if (sample.kind() == FrameView.Kind.NO_COVERAGE) {
            return WeatherSections.notOk(WeatherSections.NO_COVERAGE, null, null);
        }
        if (sample.kind() == FrameView.Kind.NO_DATA) {
            return WeatherSections.unavailable(SampleStatus.NO_DATA.defaultText());
        }
        if (choice.stale()) {
            return WeatherSections.notOk(WeatherSections.STALE, choice.text(), choice.frame().validTime());
        }
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("rateMmH", round1(Math.max(0, sample.value())));
        metrics.put("qpe1hMm", qpe1h(center, t));
        Map<String, Object> section = WeatherSections.ok(metrics);
        if (!section.containsKey("qpe1hMm")) {
            section.put("qpe1hMm", null);
        }
        return section;
    }

    /** The current 1-hour QPE at the point, or null when the product is missing, stale or uncovered. */
    Double qpe1h(Point center, Instant t) {
        if (unavailable(QPE_1H).isPresent()) {
            return null;
        }
        Choice choice = observation(QPE_1H, t, Duration.ofMinutes(30));
        if (choice.frame() == null || choice.stale()) {
            return null;
        }
        Optional<FrameView> view = rasters.view(choice.frame());
        if (view.isEmpty()) {
            return null;
        }
        FrameView.Sample sample = view.get().sample(center.getX(), center.getY());
        return sample.kind() == FrameView.Kind.VALUE ? round1(Math.max(0, sample.value())) : null;
    }

    // ------------------------------------------------------------------ warnings

    Map<String, Object> warnings(Geometry geometry, Instant t) {
        HealthStatus status = health.apply(NWS);
        if (status == HealthStatus.DISABLED) {
            return WeatherSections.unavailable("the nws-alerts source is disabled or not installed; this is not"
                    + " evidence that no warning is in effect");
        }
        if (status == HealthStatus.FAILING) {
            return WeatherSections.notOk(WeatherSections.STALE, "the nws-alerts source is failing; active warnings may"
                    + " be missing", null);
        }
        List<WeatherRecords.Row> rows = new ArrayList<>(records.intersecting(NWS, NWS_KIND, geometry, t, 50));
        rows.removeIf(r -> {
            Instant expires = instant(r.payload().get("expires"));
            return expires != null && !expires.isAfter(t);
        });
        rows.sort(Comparator.comparingInt((WeatherRecords.Row r) -> severityRank(r.payload().get("severity")))
                .thenComparing(r -> {
                    Instant e = instant(r.payload().get("expires"));
                    return e == null ? Instant.MAX : e;
                })
                .thenComparing(WeatherRecords.Row::externalId));
        List<Map<String, Object>> items = new ArrayList<>();
        for (WeatherRecords.Row row : rows) {
            if (items.size() >= MAX_WARNINGS) {
                break;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", row.externalId());
            item.put("event", text(row.payload().get("event")));
            item.put("severity", text(row.payload().get("severity")));
            Instant expires = instant(row.payload().get("expires"));
            item.put("expires", expires == null ? null : expires.toString());
            item.put("untrusted", true);
            items.add(item);
        }
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("items", items);
        return WeatherSections.ok(metrics);
    }

    /** CAP severity order: Extreme first, Unknown and anything else last. */
    static int severityRank(Object severity) {
        String s = severity == null ? "" : severity.toString();
        return switch (s) {
            case "Extreme" -> 0;
            case "Severe" -> 1;
            case "Moderate" -> 2;
            case "Minor" -> 3;
            default -> 4;
        };
    }

    // ------------------------------------------------------------------ models

    Map<String, Object> wind(Point center, Instant t) {
        String[][] providers = {{HRRR_WIND, HRRR_GUST}, {GFS_WIND, GFS_GUST}};
        boolean noCoverage = false;
        Map<String, Object> stale = null;
        String reason = null;
        for (String[] provider : providers) {
            Optional<String> unavailable = unavailable(provider[0]);
            if (unavailable.isPresent()) {
                reason = reason == null ? unavailable.get() : reason;
                continue;
            }
            Step step = forecastStep(provider[0], t);
            if (step == null) {
                reason = reason == null ? "product " + provider[0] + " has no complete forecast run" : reason;
                continue;
            }
            Optional<FrameView> view = rasters.view(step.frame());
            if (view.isEmpty() || view.get().bands() < 2) {
                continue;
            }
            FrameView.Sample sample = view.get().sample(center.getX(), center.getY());
            if (sample.kind() == FrameView.Kind.NO_COVERAGE) {
                noCoverage = true;
                continue;
            }
            if (sample.kind() != FrameView.Kind.VALUE || Double.isNaN(sample.second())) {
                continue;
            }
            if (step.stale()) {
                stale = stale == null ? WeatherSections.notOk(WeatherSections.STALE, step.text(),
                        step.frame().referenceTime()) : stale;
                continue;
            }
            Map<String, Object> derived = SampleService.derived(sample.value(), sample.second());
            Map<String, Object> metrics = new LinkedHashMap<>();
            metrics.put("productId", provider[0]);
            metrics.put("speedMs", derived.get("speedMs"));
            metrics.put("gustMs", gust(provider[1], center, t));
            metrics.put("fromDeg", derived.get("fromDeg"));
            metrics.put("run", FrameKeys.runIso(step.frame().referenceTime()));
            metrics.put("validTime", step.frame().validTime().toString());
            metrics.put("step", step.frame().forecastStep());
            Map<String, Object> section = WeatherSections.ok(metrics);
            if (!section.containsKey("gustMs")) {
                section.put("gustMs", null);
            }
            return section;
        }
        if (stale != null) {
            return stale;
        }
        if (noCoverage) {
            return WeatherSections.notOk(WeatherSections.NO_COVERAGE, null, null);
        }
        return WeatherSections.unavailable((reason == null ? "no GFS or HRRR wind run is available" : reason)
                + "; this is not evidence of calm weather");
    }

    /** The gust at the step nearest {@code t} of the newest complete run, or null. */
    Double gust(String productId, Point center, Instant t) {
        if (unavailable(productId).isPresent()) {
            return null;
        }
        Step step = forecastStep(productId, t);
        if (step == null || step.stale()) {
            return null;
        }
        Optional<FrameView> view = rasters.view(step.frame());
        if (view.isEmpty()) {
            return null;
        }
        FrameView.Sample sample = view.get().sample(center.getX(), center.getY());
        return sample.kind() == FrameView.Kind.VALUE ? round1(sample.value()) : null;
    }

    Map<String, Object> smoke(Point center, Instant t) {
        Optional<String> reason = unavailable(SMOKE);
        if (reason.isPresent()) {
            return WeatherSections.unavailable(reason.get() + "; this is not evidence of clear air");
        }
        Step step = forecastStep(SMOKE, t);
        if (step == null) {
            return WeatherSections.unavailable("product " + SMOKE + " has no complete forecast run");
        }
        Optional<FrameView> view = rasters.view(step.frame());
        if (view.isEmpty()) {
            return WeatherSections.unavailable("frame " + step.frame().frameKey() + " could not be read");
        }
        FrameView.Sample sample = view.get().sample(center.getX(), center.getY());
        if (sample.kind() == FrameView.Kind.NO_COVERAGE) {
            return WeatherSections.notOk(WeatherSections.NO_COVERAGE, null, null);
        }
        if (sample.kind() != FrameView.Kind.VALUE) {
            return WeatherSections.unavailable(SampleStatus.NO_DATA.defaultText());
        }
        if (step.stale()) {
            return WeatherSections.notOk(WeatherSections.STALE, step.text(), step.frame().referenceTime());
        }
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("pm25UgM3", round1(Math.max(0, sample.value())));
        metrics.put("run", FrameKeys.runIso(step.frame().referenceTime()));
        metrics.put("validTime", step.frame().validTime().toString());
        metrics.put("step", step.frame().forecastStep());
        return WeatherSections.ok(metrics);
    }

    // ------------------------------------------------------------------ frame choice

    /**
     * An observation frame for a time.
     *
     * @param frame the frame, or null when none can be used
     * @param stale true when the frame is further from the time than the gap
     * @param text the explanation of a stale or missing frame
     * @param asOf the valid time of the newest frame when only an old one exists, else null
     */
    record Choice(RasterFrameRow frame, boolean stale, String text, Instant asOf) {}

    /**
     * The frame valid at or before {@code t} within {@link #LOOKBACK}, or the newest frame when it is valid after
     * {@code t} and nearer; stale beyond the product's maximum gap ({@code fallbackGap} when it declares none).
     */
    Choice observation(String productId, Instant t, Duration fallbackGap) {
        ProductSpec spec = rasters.product(productId).orElse(null);
        Duration gap = spec == null ? null : SampleService.maxGap(spec);
        if (gap == null) {
            gap = fallbackGap;
        }
        List<RasterFrameRow> before = rasters.frames(productId, t.minus(LOOKBACK), t);
        RasterFrameRow best = before.isEmpty() ? null : before.get(before.size() - 1);
        Optional<RasterFrameRow> newest = rasters.newestFrame(productId);
        if (newest.isPresent() && newest.get().validTime().isAfter(t)
                && (best == null || distance(newest.get(), t).compareTo(distance(best, t)) < 0)) {
            best = newest.get();
        }
        if (best == null) {
            if (newest.isPresent()) {
                return new Choice(null, true, "the newest frame of " + productId + " is "
                        + minutes(Duration.between(newest.get().validTime(), t).abs()) + " min from the time",
                        newest.get().validTime());
            }
            return new Choice(null, false, "product " + productId + " has no frame; this is not evidence of clear"
                    + " weather", null);
        }
        Duration d = distance(best, t);
        if (d.compareTo(gap) > 0) {
            return new Choice(best, true, "the frame is " + minutes(d) + " min from the time; the product's maximum"
                    + " gap is " + minutes(gap) + " min", null);
        }
        return new Choice(best, false, null, null);
    }

    /**
     * A forecast step for a time.
     *
     * @param frame the step nearest the time in the newest complete run
     * @param stale true when the run is older than {@link #RUN_MAX_AGE} at the time, or the step is further from it
     *     than the product's maximum gap
     * @param text the explanation of a stale step
     */
    record Step(RasterFrameRow frame, boolean stale, String text) {}

    /** The step of the newest complete run nearest {@code t}, or null without a run. */
    Step forecastStep(String productId, Instant t) {
        Optional<Instant> run = rasters.latestCompleteRun(productId);
        if (run.isEmpty()) {
            return null;
        }
        List<RasterFrameRow> steps = rasters.runFrames(productId, run.get());
        if (steps.isEmpty()) {
            return null;
        }
        RasterFrameRow nearest = steps.stream()
                .min(Comparator.comparingLong((RasterFrameRow f) -> Math.abs(Duration.between(t, f.validTime()).toMillis()))
                        .thenComparing(RasterFrameRow::validTime))
                .orElseThrow();
        ProductSpec spec = rasters.product(productId).orElse(null);
        Duration gap = spec == null ? null : SampleService.maxGap(spec);
        Duration age = Duration.between(run.get(), t);
        if (age.compareTo(RUN_MAX_AGE) > 0) {
            return new Step(nearest, true, "the newest complete run of " + productId + " is "
                    + age.toHours() + " h old at the time; runs older than 12 h are not current");
        }
        Duration d = distance(nearest, t);
        if (gap != null && d.compareTo(gap) > 0) {
            return new Step(nearest, true, "the nearest step is " + minutes(d) + " min from the time; the product's"
                    + " maximum gap is " + minutes(gap) + " min");
        }
        return new Step(nearest, false, null);
    }

    // ------------------------------------------------------------------ helpers

    private static Duration distance(RasterFrameRow frame, Instant t) {
        return Duration.between(frame.validTime(), t).abs();
    }

    private static long minutes(Duration d) {
        return Math.round(d.toSeconds() / 60.0);
    }

    static Double round1(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) {
            return null;
        }
        return Math.round(v * 10) / 10.0;
    }

    private static String text(Object value) {
        return value == null ? null : value.toString();
    }

    private static Instant instant(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Instant.parse(value.toString());
        } catch (RuntimeException e) {
            try {
                return java.time.OffsetDateTime.parse(value.toString()).toInstant();
            } catch (RuntimeException inner) {
                return null;
            }
        }
    }

    /**
     * {@code geometry} buffered by {@code km}, on a local equirectangular plane around its centroid (accurate to a
     * few percent over the radii used here).
     */
    public static Geometry bufferKm(Geometry geometry, double km) {
        double lat0 = geometry.getCentroid().getY();
        double kmPerDegLon = Math.max(1.0, 111.32 * Math.cos(Math.toRadians(lat0)));
        double kmPerDegLat = 110.57;
        AffineTransformation toKm = AffineTransformation.scaleInstance(kmPerDegLon, kmPerDegLat);
        AffineTransformation toDeg = AffineTransformation.scaleInstance(1 / kmPerDegLon, 1 / kmPerDegLat);
        Geometry buffered = toDeg.transform(toKm.transform(geometry).buffer(km, 16));
        buffered.setSRID(geometry.getSRID() == 0 ? 4326 : geometry.getSRID());
        return buffered;
    }
}
