package ai.foresignal.source.adapters.wx;

import ai.foresignal.derive.DerivedContext;
import ai.foresignal.derive.DerivedGenerator;
import ai.foresignal.derive.DerivedInputs;
import ai.foresignal.derive.HeadSnapshot;
import ai.foresignal.raster.GridDef;
import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.raster.model.EcmwfDefinition;
import ai.foresignal.raster.sample.CellAreas;
import ai.foresignal.raster.sample.FrameView;
import ai.foresignal.raster.sample.GridWindow;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.ValidatedParams;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Component;

/**
 * {@code wx-forecast-areas} (prompt 105; Weather-Imagery-Design §7.1): the polygon where a configured
 * model field is forecast to cross a threshold inside the places AOI, as {@code forecast-area}
 * records with externalId {@code {model}:{field}:{op}{value}:{aoi}} (for example
 * {@code ifs:gust:>=25:aoi}).
 *
 * <p>Parameter {@code thresholds}: a list of {@code {product}:{op}:{value}[:{horizonHours}]} items,
 * where {@code product} is a model scalar product ({@code gfs-gust}, {@code ifs-t2m},
 * {@code ifs-precip-24h}, …), {@code op} is {@code >=} or {@code <=}, {@code value} is in the
 * product's units, and {@code horizonHours} (default 120, at most 120) is how far ahead the run is
 * examined. Defaults: gust ≥ 20 and ≥ 25 m/s, 24-hour precipitation ≥ 50 mm, and 2 m temperature
 * ≥ 313.15 K (40 °C) and ≤ 263.15 K (−10 °C), for GFS, HRRR and IFS where the product exists.
 *
 * <p>On each completed run the steps within the horizon are thresholded, their masks are unioned into
 * one polygon per definition (3×3 opening, run-rectangle union, JTS simplify at 0.02°), and the
 * record carries {@code validFrom} (the first exceeding valid time) and {@code validUntil} (the last
 * exceeding valid time plus the step). It emits only on a change of the quantized payload (the
 * volatile {@code run} excluded) or of the geometry: a new geometry replaces the stored one only when
 * their symmetric difference is over 10% of their union. It is tombstoned when the exceedance
 * disappears in a newer run, and a model's run counts as stale only past its own cadence, lag and
 * ingest allowance ({@link ModelRunAge}). IFS- and AIFS-derived areas carry the ECMWF CC BY 4.0
 * "changes made" attribution in {@code attribution}. {@code reconcile} restores state and tombstones
 * areas whose definition left the config.
 */
@Component
public class WxForecastAreasGenerator implements DerivedGenerator {

    /** The source id. */
    public static final String ID = "wx-forecast-areas";

    /** The record kind. */
    public static final String KIND = "forecast-area";

    /** The {@code thresholds} parameter. */
    public static final String THRESHOLDS_PARAM = "thresholds";

    /** The default tick, minutes. */
    public static final Duration DEFAULT_INTERVAL = Duration.ofMinutes(10);

    /** The largest horizon, hours. */
    public static final int MAX_HORIZON_HOURS = 120;

    /** The default horizon, hours. */
    public static final int DEFAULT_HORIZON_HOURS = 120;

    /** The geometry quantum and simplification tolerance, degrees. */
    public static final double QUANTUM_DEG = 0.02;

    /** The symmetric-difference fraction above which the geometry counts as changed. */
    public static final double GEOMETRY_CHANGE = 0.10;

    /**
     * The maximum run age of a model {@link ModelRunAge} does not know; known models use their own cadence,
     * lag and ingest allowance (prompt 105 post-review).
     */
    public static final Duration RUN_MAX_AGE = Duration.ofHours(12);

    /** The payload key left out of the significant content (and declared volatile): the run of the forecast. */
    static final String VOLATILE_RUN = "run";

    /** The default thresholds. */
    public static final List<String> DEFAULT_THRESHOLDS = List.of(
            "gfs-gust:>=:20", "gfs-gust:>=:25",
            "hrrr-gust:>=:20", "hrrr-gust:>=:25",
            "ifs-gust:>=:20", "ifs-gust:>=:25",
            "ifs-precip-24h:>=:50",
            "gfs-t2m:>=:313.15", "gfs-t2m:<=:263.15",
            "ifs-t2m:>=:313.15", "ifs-t2m:<=:263.15");

    private static final Pattern ITEM = Pattern.compile(
            "^([a-z0-9-]+):(>=|<=):(-?\\d+(?:\\.\\d+)?)(?::(\\d{1,3}))?$");

    private static final int[] OPEN3X3 = {-1, 0, 1};

    private final SourceDescriptor descriptor = buildDescriptor();
    private final Map<String, AreaState> areas = new LinkedHashMap<>();

    /** Creates the generator. */
    public WxForecastAreasGenerator() {}

    /**
     * One configured threshold area.
     *
     * @param productId a model scalar product
     * @param op {@code >=} or {@code <=}
     * @param value the threshold in the product's units
     * @param horizonHours how far ahead the run is examined
     */
    public record AreaDef(String productId, String op, double value, int horizonHours) {

        /** The model prefix ({@code gfs}, {@code hrrr}, {@code ifs}). */
        public String model() {
            int dash = productId.indexOf('-');
            return dash < 0 ? productId : productId.substring(0, dash);
        }

        /** The field (the product id without its model prefix). */
        public String field() {
            int dash = productId.indexOf('-');
            return dash < 0 ? productId : productId.substring(dash + 1);
        }

        /** The horizon. */
        public Duration horizon() {
            return Duration.ofHours(horizonHours);
        }

        /** The externalId {@code {model}:{field}:{op}{value}:{aoi}}. */
        public String externalId() {
            return model() + ":" + field() + ":" + op + number(value) + ":aoi";
        }

        /** True when {@code v} satisfies the threshold. */
        public boolean matches(double v) {
            return ">=".equals(op) ? v >= value : v <= value;
        }

        /** The most extreme value of a scan: the maximum for {@code >=}, the minimum for {@code <=}. */
        public boolean moreExtreme(double candidate, double current) {
            return ">=".equals(op) ? candidate > current : candidate < current;
        }

        /** Parses {@code product:op:value[:horizon]}, or empty when malformed. */
        public static Optional<AreaDef> parse(String item) {
            Matcher m = ITEM.matcher(item == null ? "" : item.strip());
            if (!m.matches()) {
                return Optional.empty();
            }
            int horizon = m.group(4) == null ? DEFAULT_HORIZON_HOURS : Integer.parseInt(m.group(4));
            if (horizon < 1 || horizon > MAX_HORIZON_HOURS) {
                return Optional.empty();
            }
            return Optional.of(new AreaDef(m.group(1), m.group(2), Double.parseDouble(m.group(3)), horizon));
        }
    }

    /** One area's state. */
    static final class AreaState {

        final AreaDef def;
        WxSection section;
        Geometry geometry;
        Instant validFrom;
        Instant validUntil;
        Instant peakAt;
        Instant run;
        /** True while a live (non-tombstoned) record exists for this area. */
        boolean exists;
        /** The canonical significant content (payload without {@code run}, plus geometry) last emitted. */
        String lastSignificant;

        AreaState(AreaDef def, WxSection section, Geometry geometry) {
            this.def = def;
            this.section = section;
            this.geometry = geometry;
        }
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** The descriptor. */
    static SourceDescriptor buildDescriptor() {
        return SourceDescriptor.builder(ID)
                .name("Forecast threshold areas (derived)")
                .description("The polygon where a model field is forecast to cross a threshold inside the places AOI,"
                        + " over a horizon of up to 120 h. Automated, not a warning.")
                .kind(KIND)
                .attribution("Derived by Foresignal from NOAA GFS/HRRR and ECMWF IFS. IFS-derived areas: "
                        + EcmwfDefinition.CHANGES_MADE)
                .licence("derived (see inputs)")
                .mode(Mode.DERIVED)
                .defaultBudget(Budget.of(1, 1))
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .recordTtl(null)
                .zeroResultsNormal(true)
                .defaultRetention(Duration.ofDays(7))
                .kindRetention(KIND, Duration.ofDays(7))
                .volatileFields(VOLATILE_RUN)
                .param(ParamSpec.optional(THRESHOLDS_PARAM, ParamType.STRING_LIST,
                        "product:op:value[:horizonHours] items; op >= or <=; horizon at most 120",
                        DEFAULT_THRESHOLDS))
                .build();
    }

    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        for (String item : items(params)) {
            if (AreaDef.parse(item).isEmpty()) {
                problems.put(THRESHOLDS_PARAM,
                        "'" + item + "' is not product:op:value[:horizon] with op >= or <= and horizon at most 120");
                break;
            }
        }
        return problems;
    }

    @Override
    public DerivedInputs inputs(ValidatedParams params) {
        Set<String> products = new LinkedHashSet<>();
        definitions(params).forEach(d -> products.add(d.productId()));
        return new DerivedInputs(Set.of(), Set.of(), products, DEFAULT_INTERVAL, true);
    }

    /** The configured thresholds. */
    static List<AreaDef> definitions(ValidatedParams params) {
        List<AreaDef> out = new ArrayList<>();
        for (String item : items(params)) {
            AreaDef.parse(item).ifPresent(d -> {
                if (out.stream().noneMatch(o -> o.externalId().equals(d.externalId()))) {
                    out.add(d);
                }
            });
        }
        return out;
    }

    private static List<String> items(ValidatedParams params) {
        List<String> raw = params == null || !params.has(THRESHOLDS_PARAM) ? DEFAULT_THRESHOLDS
                : params.stringList(THRESHOLDS_PARAM);
        return raw == null || raw.isEmpty() ? DEFAULT_THRESHOLDS : raw;
    }

    @Override
    public void reconcile(List<HeadSnapshot> ownHeads, DerivedContext ctx) {
        areas.clear();
        List<AreaDef> defs = definitions(ctx.params());
        Map<String, HeadSnapshot> heads = new LinkedHashMap<>();
        for (HeadSnapshot head : ownHeads) {
            if (!head.tombstone() && KIND.equals(head.kind())) {
                heads.put(head.externalId(), head);
            }
        }
        for (AreaDef def : defs) {
            HeadSnapshot head = heads.remove(def.externalId());
            AreaState state = new AreaState(def, head == null ? new WxSection()
                    : WxSection.restore(sectionOf(head.payload()), head.observedAt()),
                    head == null ? null : head.geometry());
            if (head != null) {
                state.exists = true;
                state.validFrom = instant(head.payload().get("firstExceedAt"));
                state.validUntil = instant(head.payload().get("validUntil"));
                state.peakAt = instant(head.payload().get("peakAt"));
                state.run = instant(head.payload().get(VOLATILE_RUN));
                state.lastSignificant = significant(head.payload(), head.geometry());
            }
            areas.put(def.externalId(), state);
        }
        for (String gone : heads.keySet()) {
            ctx.tombstone(KIND, gone);
        }
        compute(ctx, ctx.now());
    }

    @Override
    public void onFrame(ai.foresignal.derive.RasterFrameEvent event, DerivedContext ctx) {
        compute(ctx, ctx.now());
    }

    @Override
    public void onTick(Instant now, DerivedContext ctx) {
        compute(ctx, now);
    }

    /** The section part of a stored payload: status, asOf and the quantized metrics. */
    private static Map<String, Object> sectionOf(Map<String, Object> payload) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (String key : List.of("status", "asOf", "peakValue", "areaKm2")) {
            if (payload.containsKey(key)) {
                m.put(key, payload.get(key));
            }
        }
        return m;
    }

    private static Instant instant(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Instant.parse(value.toString());
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** Computes every area and emits the changed ones. */
    void compute(DerivedContext ctx, Instant now) {
        Geometry aoi = ctx.places().aoi();
        Map<String, FrameView> views = new LinkedHashMap<>();
        Map<String, Optional<Instant>> runs = new LinkedHashMap<>();
        for (AreaState area : new ArrayList<>(areas.values())) {
            AreaDef def = area.def;
            if (aoi == null || aoi.isEmpty()) {
                notOk(ctx, area, WxSection.UNAVAILABLE, now);
                continue;
            }
            Optional<Instant> run = runs.computeIfAbsent(def.productId(),
                    p -> ctx.rasters().unavailableReason(p).isPresent() ? Optional.empty()
                            : ctx.rasters().latestCompleteRun(p));
            if (run.isEmpty() || Duration.between(run.get(), now).compareTo(maxRunAge(def)) > 0) {
                notOk(ctx, area, run.isEmpty() ? WxSection.UNAVAILABLE : WxSection.STALE, now);
                continue;
            }
            computeRun(ctx, area, def, run.get(), aoi, views, now);
        }
    }

    /**
     * The maximum run age of an area's model (prompt 105 post-review): the model's cadence, lag and ingest
     * allowance ({@link ModelRunAge}), so IFS is not called stale for most of every day.
     */
    static Duration maxRunAge(AreaDef def) {
        return ModelRunAge.of(def.productId());
    }

    /**
     * Marks an area's input not ok. An area with a live record keeps its last values with the status and
     * {@code asOf} (unknown is not clear); an area without a record emits nothing, because there is no
     * exceedance to qualify.
     */
    private void notOk(DerivedContext ctx, AreaState area, String status, Instant now) {
        area.section.notOk(status);
        if (area.exists) {
            emit(ctx, area, now);
        }
    }

    private void computeRun(DerivedContext ctx, AreaState area, AreaDef def, Instant run, Geometry aoi,
            Map<String, FrameView> views, Instant now) {
        Instant horizonEnd = run.plus(def.horizon());
        List<RasterFrameRow> frames = ctx.rasters().runFrames(def.productId(), run);
        if (frames.isEmpty()) {
            notOk(ctx, area, WxSection.UNAVAILABLE, now);
            return;
        }
        ProductSpec spec = ctx.rasters().product(def.productId()).orElse(null);
        Duration step = spec == null || spec.temporal() == null || spec.temporal().cadence() == null
                ? Duration.ofHours(3) : spec.temporal().cadence();
        Geometry union = null;
        double peak = Double.NaN;
        Instant peakAt = null;
        Instant firstExceed = null;
        Instant lastExceed = null;
        double[] envelope = envelope(aoi);
        for (RasterFrameRow frame : frames) {
            if (frame.validTime().isAfter(horizonEnd)) {
                continue;
            }
            FrameView view = views.computeIfAbsent(def.productId() + "|" + frame.frameId(),
                    k -> ctx.rasters().view(frame).orElse(null));
            if (view == null) {
                continue;
            }
            GridWindow window = GridWindow.of(view.grid(), envelope[0], envelope[1], envelope[2], envelope[3]);
            int[] cells = mask(view, window, aoi, def, envelope);
            if (cells.length == 0) {
                continue;
            }
            Geometry mask = MaskPolygons.of(view.grid(), open(view, window, cells), QUANTUM_DEG);
            if (mask != null) {
                union = union == null ? mask : union.union(mask);
            }
            double stepPeak = peakOf(view, cells, def);
            if (Double.isNaN(peak) || def.moreExtreme(stepPeak, peak)) {
                peak = stepPeak;
                peakAt = frame.validTime();
            }
            if (firstExceed == null || frame.validTime().isBefore(firstExceed)) {
                firstExceed = frame.validTime();
            }
            if (lastExceed == null || frame.validTime().isAfter(lastExceed)) {
                lastExceed = frame.validTime();
            }
        }
        if (union == null || union.isEmpty() || firstExceed == null) {
            // The exceedance is gone in this run: the area ends with a tombstone (prompt 105 §2), never a
            // record without geometry. A later exceedance starts a fresh record.
            if (area.exists) {
                ctx.tombstone(KIND, def.externalId());
            }
            areas.put(def.externalId(), new AreaState(def, new WxSection(), null));
            return;
        }
        // The 10% rule: a new geometry replaces the stored one only when their symmetric difference is over
        // 10% of their union; otherwise the stored geometry (and its area) are kept, so small run-to-run
        // wobble writes no version.
        Geometry kept = geometryChanged(area.geometry, union) ? union : area.geometry;
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("peakValue", WxRound.whole(peak));
        m.put("areaKm2", WxRound.sig2(CellAreas.geometryAreaKm2(kept)));
        area.section.good(m, run);
        area.geometry = kept;
        area.validFrom = firstExceed;
        area.validUntil = lastExceed.plus(step);
        area.peakAt = peakAt;
        area.run = run;
        emit(ctx, area, run);
    }

    /**
     * Emits an area when its significant content changed. The significant content is the payload without
     * {@code run} (also declared volatile) plus the geometry, so a newer run that forecasts the same area
     * writes no version.
     */
    private void emit(DerivedContext ctx, AreaState area, Instant observedAt) {
        AreaDef def = area.def;
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", def.model());
        payload.put("field", def.field());
        Map<String, Object> threshold = new LinkedHashMap<>();
        threshold.put("op", def.op());
        threshold.put("value", number(def.value()));
        payload.put("threshold", threshold);
        payload.putAll(area.section.toMap());
        payload.remove("statusText");
        if (area.run != null) {
            payload.put(VOLATILE_RUN, area.run.toString());
        }
        if (area.peakAt != null) {
            payload.put("peakAt", area.peakAt.toString());
        }
        if (area.validFrom != null) {
            payload.put("firstExceedAt", area.validFrom.toString());
            if (area.validUntil != null) {
                payload.put("validUntil", area.validUntil.toString());
            }
        }
        String attribution = EcmwfDefinition.changesMadeFor(def.model());
        if (attribution != null) {
            payload.put("attribution", attribution);
        }
        payload.put("automated", true);
        payload.put("notAWarning", true);
        String significant = significant(payload, area.geometry);
        if (significant.equals(area.lastSignificant)) {
            return;
        }
        NormalizedRecord.Builder builder = NormalizedRecord.builder(def.externalId(), KIND)
                .observedAt(observedAt)
                .geometry(area.geometry)
                .putAll(payload);
        if (area.validFrom != null) {
            builder.validFrom(area.validFrom);
        }
        if (area.validUntil != null) {
            builder.validUntil(area.validUntil);
        }
        ctx.emit(builder.build());
        area.exists = true;
        area.lastSignificant = significant;
    }

    /** The canonical significant content: the payload without {@code run}, plus the geometry's WKT. */
    static String significant(Map<String, Object> payload, Geometry geometry) {
        Map<String, Object> copy = new LinkedHashMap<>(payload);
        copy.remove(VOLATILE_RUN);
        return ai.foresignal.ingest.CanonicalJson.write(copy) + "|"
                + (geometry == null ? "" : ai.foresignal.ingest.ContentHasher.wkt(geometry));
    }

    /** True when the symmetric difference of the two geometries is over 10% of their union. */
    static boolean geometryChanged(Geometry previous, Geometry current) {
        if (previous == null || current == null || previous.isEmpty() || current.isEmpty()) {
            return previous != current;
        }
        try {
            double union = previous.union(current).getArea();
            if (union <= 0) {
                return true;
            }
            return previous.symDifference(current).getArea() / union > GEOMETRY_CHANGE;
        } catch (RuntimeException e) {
            return true;
        }
    }

    /** The AOI's envelope {west, south, east, north}. */
    private static double[] envelope(Geometry aoi) {
        var e = aoi.getEnvelopeInternal();
        return new double[] {e.getMinX(), e.getMinY(), e.getMaxX(), e.getMaxY()};
    }

    /** The cells of {@code window} whose centre is in {@code aoi} and which satisfy the threshold. */
    private static int[] mask(FrameView view, GridWindow window, Geometry aoi, AreaDef def, double[] envelope) {
        List<Integer> cells = new ArrayList<>();
        int nx = view.nx();
        for (int r = window.r0(); r <= window.r1(); r++) {
            for (int c = window.c0(); c <= window.c1(); c++) {
                int cell = r * nx + c;
                double code = view.code(cell, 0);
                if (view.classify(code) != FrameView.Kind.VALUE) {
                    continue;
                }
                double[] ll = view.grid().indexToLonLat(c, r);
                if (Double.isNaN(ll[0]) || !aoi.covers(ai.foresignal.source.spi.Geo.point(ll[0], ll[1]))) {
                    continue;
                }
                if (def.matches(view.value(cell, 0))) {
                    cells.add(cell);
                }
            }
        }
        int[] out = new int[cells.size()];
        for (int i = 0; i < out.length; i++) {
            out[i] = cells.get(i);
        }
        return out;
    }

    /**
     * The most extreme value of the masked cells: the maximum for a {@code >=} threshold, the minimum for a
     * {@code <=} threshold (the coldest point of a freeze area).
     */
    static double peakOf(FrameView view, int[] cells, AreaDef def) {
        double peak = Double.NaN;
        for (int cell : cells) {
            double v = view.value(cell, 0);
            if (Double.isNaN(peak) || def.moreExtreme(v, peak)) {
                peak = v;
            }
        }
        return peak;
    }

    /** A 3×3 opening over the mask, keeping the original cell indices. */
    private static int[] open(FrameView view, GridWindow window, int[] cells) {
        Set<Integer> set = new LinkedHashSet<>();
        for (int c : cells) {
            set.add(c);
        }
        int nx = view.nx();
        Set<Integer> kept = new LinkedHashSet<>();
        for (int cell : cells) {
            int r = cell / nx;
            int c = cell % nx;
            if (r <= window.r0() || r >= window.r1() || c <= window.c0() || c >= window.c1()) {
                continue;
            }
            boolean all = true;
            for (int dr : OPEN3X3) {
                for (int dc : OPEN3X3) {
                    if (!set.contains((r + dr) * nx + (c + dc))) {
                        all = false;
                        break;
                    }
                }
                if (!all) {
                    break;
                }
            }
            if (all) {
                kept.add(cell);
            }
        }
        // Dilate back: keep any cell that neighbours a surviving cell.
        Set<Integer> out = new LinkedHashSet<>();
        for (int cell : kept) {
            int r = cell / nx;
            int c = cell % nx;
            for (int dr : OPEN3X3) {
                for (int dc : OPEN3X3) {
                    out.add((r + dr) * nx + (c + dc));
                }
            }
        }
        out.retainAll(set);
        int[] result = new int[out.size()];
        int i = 0;
        for (int cell : out) {
            result[i++] = cell;
        }
        return result;
    }

    /** The current areas (tests). */
    Map<String, AreaState> states() {
        return areas;
    }

    /** {@code 50} for 50.0, {@code 2.5} for 2.5. */
    static String number(double v) {
        return v == Math.rint(v) ? Long.toString((long) v) : Double.toString(v);
    }

    /** The area of a geometry in km², for tests. */
    static double areaKm2(Geometry geometry) {
        return geometry == null ? 0 : CellAreas.geometryAreaKm2(geometry);
    }
}
