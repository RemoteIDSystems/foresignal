package ai.foresignal.source.adapters.wx;

import ai.foresignal.derive.DerivedContext;
import ai.foresignal.derive.DerivedGenerator;
import ai.foresignal.derive.DerivedInputs;
import ai.foresignal.derive.HeadSnapshot;
import ai.foresignal.place.PlaceView;
import ai.foresignal.raster.sample.AreaScan;
import ai.foresignal.raster.sample.FrameView;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.ValidatedParams;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
 * {@code wx-areas} (prompt 052; Weather-Imagery-Design §7.2): the area above a threshold inside the places
 * AOI or a place, as {@code wx-area} records with externalId {@code {field}:{threshold}:{aoi}} (for example
 * {@code refl:50:aoi}, {@code mesh:25:home}).
 *
 * <p>Parameter {@code areas}: a list of {@code field:threshold:aoi} items, {@code field} one of
 * {@code refl} (MRMS reflectivity, dBZ), {@code mesh} (MRMS 60-minute MESH, mm), {@code precip} (MRMS
 * precipitation rate, mm/h), and from prompt 053 {@code rotation} (MRMS 60-minute rotation track, s⁻¹),
 * {@code qpe1h} (MRMS radar-only 1-hour precipitation, mm) or {@code ffg} (1-hour rainfall as a percentage of
 * flash-flood guidance), {@code aoi} a place id or {@code aoi}; default {@code refl:50:aoi} and
 * {@code mesh:25:aoi}. Parameter {@code interval}: the tick, 6 to 10 minutes, default 8.
 *
 * <p>Payload: {@code field}, {@code threshold}, {@code aoi}, {@code status}, {@code areaKm2} (2 significant
 * figures), {@code maxValue} (reflectivity to 0.5 dBZ, rotation to 0.001 s⁻¹, else whole units) and
 * {@code coveragePct} (integer),
 * plus {@code automated} and {@code notAWarning}. Geometry: the thresholded mask's polygons, simplified and
 * snapped to about 1 km, or null when nothing reaches the threshold. Staleness, failing inputs and lack of
 * coverage keep the previous values (geometry included) with a non-ok status and {@code asOf}. An area whose
 * place was deleted, or that left the {@code areas} parameter, is tombstoned.
 */
@Component
public class WxAreasGenerator implements DerivedGenerator {

    /** The source id. */
    public static final String ID = "wx-areas";

    /** The record kind. */
    public static final String KIND = "wx-area";

    /** The {@code areas} parameter. */
    public static final String AREAS_PARAM = "areas";

    /** The {@code interval} parameter. */
    public static final String INTERVAL_PARAM = "interval";

    /** The default tick. */
    public static final Duration DEFAULT_INTERVAL = Duration.ofMinutes(8);

    /** The default areas. */
    public static final List<String> DEFAULT_AREAS = List.of("refl:50:aoi", "mesh:25:aoi");

    /** The MRMS maximum gap of the 2-minute products. */
    public static final Duration MAX_GAP = Duration.ofMinutes(6);

    /** The maximum gap of the products taken every 10 minutes by default (QPE 1 h, FFG ratio; prompt 053). */
    public static final Duration TEN_MINUTE_MAX_GAP = Duration.ofMinutes(30);

    /** Mask geometry quantum, degrees (about 1 km). */
    public static final double QUANTUM_DEG = 0.01;

    /** The coverage below which an area is {@code no-coverage}, percent. */
    public static final double MIN_COVERAGE_PCT = 50;

    private static final Pattern ITEM = Pattern.compile("^(refl|mesh|precip|rotation|qpe1h|ffg):(\\d+(?:\\.\\d+)?):([a-z0-9]+(?:-[a-z0-9]+)*)$");

    private final SourceDescriptor descriptor = buildDescriptor();
    private final Map<String, AreaState> areas = new LinkedHashMap<>();

    /** Creates the generator. */
    public WxAreasGenerator() {}

    /**
     * One configured area.
     *
     * @param field {@code refl}, {@code mesh}, {@code precip}, {@code rotation}, {@code qpe1h} or {@code ffg}
     * @param threshold the threshold in the field's units
     * @param aoi a place id or {@code aoi}
     */
    public record AreaDef(String field, double threshold, String aoi) {

        /** The externalId {@code {field}:{threshold}:{aoi}}, the threshold without a needless fraction. */
        public String externalId() {
            return field + ":" + number(threshold) + ":" + aoi;
        }

        /** The product of the field. */
        public String productId() {
            return switch (field) {
                case "refl" -> "mrms-reflectivity";
                case "mesh" -> "mrms-mesh-60m";
                case "rotation" -> "mrms-rotation-60m";
                case "qpe1h" -> "mrms-qpe-1h";
                case "ffg" -> "mrms-ffg-ratio-1h";
                default -> "mrms-precip-rate";
            };
        }

        /** The input's maximum gap: 30 minutes for the 10-minute products, else 6 minutes. */
        public Duration maxGap() {
            return "qpe1h".equals(field) || "ffg".equals(field) ? TEN_MINUTE_MAX_GAP : MAX_GAP;
        }

        /** {@code maxValue} rounded for the field. */
        Object roundMax(double max) {
            return switch (field) {
                case "refl" -> WxRound.dbz(max);
                case "rotation" -> Math.round(max * 1000.0) / 1000.0;
                default -> WxRound.whole(max);
            };
        }

        /** Parses {@code field:threshold:aoi}, or empty when malformed. */
        public static Optional<AreaDef> parse(String item) {
            Matcher m = ITEM.matcher(item == null ? "" : item.strip().toLowerCase(Locale.ROOT));
            if (!m.matches()) {
                return Optional.empty();
            }
            return Optional.of(new AreaDef(m.group(1), Double.parseDouble(m.group(2)), m.group(3)));
        }
    }

    /** One area's state. */
    static final class AreaState {

        final AreaDef def;
        final WxSection section;
        Geometry geometry;

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
                .name("Weather areas (derived)")
                .description("The area of MRMS reflectivity, hail, rain rate, rotation, 1-hour rainfall or flash-flood"
                        + " guidance ratio above a threshold inside the places AOI or a place, every few minutes. Unknown is never clear. Automated, not a warning.")
                .kind(KIND)
                .attribution("Derived by Foresignal from NOAA MRMS")
                .licence("derived (see inputs)")
                .mode(Mode.DERIVED)
                .defaultBudget(Budget.of(1, 1))
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .recordTtl(null)
                .zeroResultsNormal(true)
                .defaultRetention(Duration.ofDays(7))
                .kindRetention(KIND, Duration.ofDays(7))
                .param(ParamSpec.optional(AREAS_PARAM, ParamType.STRING_LIST,
                        "field:threshold:aoi items; field refl, mesh, precip, rotation, qpe1h or ffg; aoi a place id or aoi",
                        DEFAULT_AREAS))
                .param(ParamSpec.optional(INTERVAL_PARAM, ParamType.DURATION, "How often areas are computed (6 to 10 min)",
                        "8m"))
                .build();
    }

    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        for (String item : items(params)) {
            if (AreaDef.parse(item).isEmpty()) {
                problems.put(AREAS_PARAM, "'" + item + "' is not field:threshold:aoi with field refl, mesh, precip, rotation,"
                        + " qpe1h or ffg");
                break;
            }
        }
        Duration interval = interval(params);
        if (interval.compareTo(Duration.ofMinutes(6)) < 0 || interval.compareTo(Duration.ofMinutes(10)) > 0) {
            problems.put(INTERVAL_PARAM, "must be between 6 and 10 minutes");
        }
        return problems;
    }

    @Override
    public DerivedInputs inputs(ValidatedParams params) {
        Duration interval = interval(params);
        if (interval.compareTo(Duration.ofMinutes(6)) < 0 || interval.compareTo(Duration.ofMinutes(10)) > 0) {
            interval = DEFAULT_INTERVAL;
        }
        return new DerivedInputs(Set.of(), Set.of(), Set.of(), interval, true);
    }

    /** The configured areas. */
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
        List<String> raw = params == null || !params.has(AREAS_PARAM) ? DEFAULT_AREAS : params.stringList(AREAS_PARAM);
        return raw == null || raw.isEmpty() ? DEFAULT_AREAS : raw;
    }

    private static Duration interval(ValidatedParams params) {
        if (params == null || !params.has(INTERVAL_PARAM)) {
            return DEFAULT_INTERVAL;
        }
        Duration d = params.duration(INTERVAL_PARAM);
        return d == null ? DEFAULT_INTERVAL : d;
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
            WxSection section = head == null ? new WxSection() : WxSection.restore(metricsOf(head.payload()),
                    head.observedAt());
            areas.put(def.externalId(), new AreaState(def, section, head == null ? null : head.geometry()));
        }
        for (String gone : heads.keySet()) {
            ctx.tombstone(KIND, gone);
        }
        compute(ctx, ctx.now());
    }

    @Override
    public void onTick(Instant now, DerivedContext ctx) {
        compute(ctx, now);
    }

    /** The section part of a stored payload. */
    private static Map<String, Object> metricsOf(Map<String, Object> payload) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (String key : List.of("status", "asOf", "areaKm2", "maxValue", "coveragePct")) {
            if (payload.containsKey(key)) {
                m.put(key, payload.get(key));
            }
        }
        return m;
    }

    /** Computes every area and emits the changed ones. */
    void compute(DerivedContext ctx, Instant now) {
        Map<String, FrameView> views = new LinkedHashMap<>();
        Map<String, WxInput> inputs = new LinkedHashMap<>();
        for (AreaState area : new ArrayList<>(areas.values())) {
            AreaDef def = area.def;
            Geometry geometry;
            if ("aoi".equals(def.aoi())) {
                geometry = ctx.places().aoi();
            } else {
                Optional<PlaceView> place = ctx.places().find(def.aoi());
                if (place.isEmpty()) {
                    areas.remove(def.externalId());
                    ctx.tombstone(KIND, def.externalId());
                    continue;
                }
                geometry = place.get().area();
            }
            WxInput in = inputs.computeIfAbsent(def.productId(), p -> WxInput.observation(ctx, p, def.maxGap(), now));
            if (!in.ok() || geometry == null || geometry.isEmpty()) {
                area.section.notOk(in.ok() ? WxSection.UNAVAILABLE : in.status());
                emit(ctx, area, now);
                continue;
            }
            FrameView view = views.computeIfAbsent(def.productId(), p -> ctx.rasters().view(in.frame()).orElse(null));
            if (view == null) {
                area.section.notOk(WxSection.UNAVAILABLE);
                emit(ctx, area, now);
                continue;
            }
            AreaScan.Result result = AreaScan.scan(view, geometry, new double[] {def.threshold()}, true);
            if (result.cellsInArea() == 0 || result.coveragePct() < MIN_COVERAGE_PCT) {
                area.section.notOk(WxSection.NO_COVERAGE);
                emit(ctx, area, in.frame().validTime());
                continue;
            }
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("areaKm2", WxRound.sig2(result.areaAboveKm2()[0]));
            double max = Double.isNaN(result.max()) ? 0 : Math.max(0, result.max());
            m.put("maxValue", def.roundMax(max));
            m.put("coveragePct", (int) Math.round(result.coveragePct()));
            area.section.good(m, in.frame().validTime());
            area.geometry = MaskPolygons.of(view.grid(), result.maskCells(), QUANTUM_DEG);
            emit(ctx, area, in.frame().validTime());
        }
    }

    private void emit(DerivedContext ctx, AreaState area, Instant observedAt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("field", area.def.field());
        payload.put("threshold", area.def.threshold() == Math.rint(area.def.threshold())
                ? (Object) (long) area.def.threshold() : area.def.threshold());
        payload.put("aoi", area.def.aoi());
        payload.putAll(area.section.toMap());
        payload.remove("statusText");
        payload.put("automated", true);
        payload.put("notAWarning", true);
        ctx.emit(NormalizedRecord.builder(area.def.externalId(), KIND)
                .observedAt(observedAt)
                .geometry(area.geometry)
                .putAll(payload)
                .build());
    }

    /** The current areas (tests). */
    Map<String, AreaState> states() {
        return areas;
    }

    /** {@code 50} for 50.0, {@code 2.5} for 2.5. */
    static String number(double v) {
        return v == Math.rint(v) ? Long.toString((long) v) : Double.toString(v);
    }
}
