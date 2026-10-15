package ai.foresignal.source.adapters.passes;

import ai.foresignal.derive.DerivedContext;
import ai.foresignal.derive.DerivedGenerator;
import ai.foresignal.derive.DerivedInputs;
import ai.foresignal.derive.HeadSnapshot;
import ai.foresignal.place.PlaceView;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.space.GroundTracks;
import ai.foresignal.space.OmmElements;
import ai.foresignal.space.Observer;
import ai.foresignal.space.PassPredictor;
import ai.foresignal.space.PredictedPass;
import ai.foresignal.stream.ChangeBatch;
import ai.foresignal.stream.ChangeSummary;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

/**
 * {@code satellite-passes} (prompt 064): predicted passes of watched satellites over the operator's places,
 * computed with Orekit ({@link PassPredictor}) from the {@code celestrak-gp} heads. DERIVED (prompt 052): no
 * upstream requests.
 *
 * <ul>
 *   <li><b>Inputs:</b> changes of {@code celestrak-gp} {@code satellite} records (a watched catalog number
 *       recomputes that satellite), place edits (reconcile) and a {@value #TICK_MINUTES}-minute tick; every
 *       satellite is recomputed at most every {@value #RECOMPUTE_MINUTES} minutes on the tick.
 *   <li><b>Records:</b> kind {@code satellite-pass}, {@code externalId} = {@code place:{placeId}:{norad}:{aosEpochMinute}},
 *       {@code eventTime} = AOS, {@code observedAt} = receipt. The payload's times are rounded to
 *       {@value #TIME_ROUNDING_SECONDS} s, elevations to 0.1° and azimuths to 1°, and the ground-track geometry
 *       to 0.01°, so an element refresh does not churn versions (the runtime's emit-on-change drops an identical
 *       emit).
 *   <li><b>Lifecycle:</b> a pass is tombstoned at LOS + 10 min, and when a recomputation with fresh elements no
 *       longer finds it; missing elements never tombstone anything (Weather-Imagery-Design §7.1). Reconcile
 *       tombstones the passes of deleted places and those past LOS + 10 min.
 *   <li><b>Alerts:</b> {@code validFrom} = AOS, so a rule's {@code forecastHorizon} (which matches
 *       {@code valid_from} on new versions only) can see it, and {@code imminent} turns true
 *       {@value #IMMINENT_MINUTES} minutes before AOS (on the tick), which makes the one new version near AOS such
 *       a rule needs; a plain {@code imminent == true} enter rule works too.
 *   <li><b>Places:</b> point places, or the ids in {@code places}. A sensitive place (prompt 036) is used only when
 *       it is named in {@code places}; its records carry no position of the place itself (the geometry is the
 *       satellite's ground track), but mark the source private (prompt 039) to keep them from read tokens.
 * </ul>
 */
@Component
public class SatellitePassesGenerator implements DerivedGenerator {

    /** The source id. */
    public static final String ID = "satellite-passes";

    /** The record kind. */
    public static final String KIND = "satellite-pass";

    /** The elements source. */
    public static final String ELEMENTS_SOURCE = "celestrak-gp";

    /** The elements kind. */
    public static final String ELEMENTS_KIND = "satellite";

    /** The watched catalog numbers. */
    public static final String NORAD_PARAM = "norad";

    /** The place ids. */
    public static final String PLACES_PARAM = "places";

    /** The minimum elevation. */
    public static final String MIN_ELEVATION_PARAM = "minElevationDeg";

    /** The prediction horizon. */
    public static final String HORIZON_PARAM = "horizon";

    /** The per-place cap. */
    public static final String MAX_PASSES_PARAM = "maxPassesPerPlace";

    /** ISS, METEOR-M2 3 and METEOR-M2 4. */
    public static final List<String> DEFAULT_NORAD = List.of("25544", "57166", "59051");

    /** The tick period, minutes. */
    public static final int TICK_MINUTES = 5;

    /** How often every satellite is recomputed on the tick, minutes. */
    public static final int RECOMPUTE_MINUTES = 30;

    /** How long after LOS a pass is tombstoned. */
    public static final Duration TOMBSTONE_AFTER_LOS = Duration.ofMinutes(10);

    /** Payload time rounding, seconds. */
    public static final int TIME_ROUNDING_SECONDS = 10;

    /** How long before AOS a pass becomes {@code imminent}, minutes. */
    public static final int IMMINENT_MINUTES = 15;

    /** The retention of {@code satellite-pass} versions. */
    public static final Duration RETENTION = Duration.ofDays(14);

    private final SourceDescriptor descriptor = buildDescriptor();

    /** The LOS of every live pass this source holds, by externalId. */
    private final Map<String, Instant> live = new HashMap<>();

    /** The last record emitted per live pass this run, so the tick can flag it imminent without recomputing. */
    private final Map<String, NormalizedRecord> lastEmitted = new HashMap<>();

    private Instant lastFullRecompute;

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public DerivedInputs inputs(ValidatedParams params) {
        return new DerivedInputs(Set.of(ELEMENTS_SOURCE), Set.of(ELEMENTS_KIND), Set.of(),
                Duration.ofMinutes(TICK_MINUTES), true);
    }

    /** Catalog numbers are 1 to 9 digits; the elevation 0 to 89°; the horizon 1 h to 10 days. */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        for (String n : norads(params)) {
            if (!n.matches("\\d{1,9}")) {
                problems.put(NORAD_PARAM, "'" + n + "' is not a catalog number of 1 to 9 digits");
                break;
            }
        }
        Double el = params.number(MIN_ELEVATION_PARAM);
        if (el != null && (el < 0 || el >= 90)) {
            problems.put(MIN_ELEVATION_PARAM, "must be 0 to 89, but was " + el);
        }
        Duration horizon = params.duration(HORIZON_PARAM);
        if (horizon != null && (horizon.compareTo(Duration.ofHours(1)) < 0 || horizon.compareTo(Duration.ofDays(10)) > 0)) {
            problems.put(HORIZON_PARAM, "must be 1 h to 10 days, but was " + horizon);
        }
        Integer max = params.integer(MAX_PASSES_PARAM);
        if (max != null && (max < 1 || max > 1000)) {
            problems.put(MAX_PASSES_PARAM, "must be 1 to 1000, but was " + max);
        }
        return problems;
    }

    // ------------------------------------------------------------------ hooks

    @Override
    public void reconcile(List<HeadSnapshot> ownHeads, DerivedContext ctx) {
        Instant now = ctx.now();
        live.clear();
        lastEmitted.clear();
        for (HeadSnapshot head : ownHeads) {
            if (head.tombstone() || !KIND.equals(head.kind())) {
                continue;
            }
            String placeId = placeIdOf(head.externalId());
            Instant los = instant(head.payload().get("los"));
            if (placeId == null || ctx.places().find(placeId).isEmpty()
                    || (los != null && !los.plus(TOMBSTONE_AFTER_LOS).isAfter(now))) {
                ctx.tombstone(KIND, head.externalId());
            } else {
                live.put(head.externalId(), los == null ? now : los);
            }
        }
        recompute(ctx, null, now);
        lastFullRecompute = now;
    }

    @Override
    public void onChanges(ChangeBatch batch, DerivedContext ctx) {
        Set<String> watched = new LinkedHashSet<>(norads(ctx.params()));
        Set<String> changed = new LinkedHashSet<>();
        for (ChangeSummary change : batch.changes()) {
            if (ELEMENTS_KIND.equals(change.kind()) && watched.contains(change.externalId())) {
                changed.add(change.externalId());
            }
        }
        if (!changed.isEmpty()) {
            recompute(ctx, changed, ctx.now());
        }
    }

    @Override
    public void onTick(Instant now, DerivedContext ctx) {
        expire(ctx, now);
        flagImminent(ctx, now);
        if (lastFullRecompute == null
                || !lastFullRecompute.plus(Duration.ofMinutes(RECOMPUTE_MINUTES)).isAfter(now)) {
            recompute(ctx, null, now);
            lastFullRecompute = now;
        }
    }

    // ------------------------------------------------------------------ work

    /** Tombstones the passes whose LOS + 10 min has passed. */
    void expire(DerivedContext ctx, Instant now) {
        List<String> done = new ArrayList<>();
        live.forEach((id, los) -> {
            if (!los.plus(TOMBSTONE_AFTER_LOS).isAfter(now)) {
                done.add(id);
            }
        });
        for (String id : done) {
            ctx.tombstone(KIND, id);
            live.remove(id);
            lastEmitted.remove(id);
        }
    }

    /** Re-emits the passes that entered the imminent window since they were last emitted. */
    void flagImminent(DerivedContext ctx, Instant now) {
        for (Map.Entry<String, NormalizedRecord> e : new ArrayList<>(lastEmitted.entrySet())) {
            NormalizedRecord r = e.getValue();
            if (Boolean.TRUE.equals(r.payload().get("imminent")) || r.eventTime() == null) {
                continue;
            }
            Instant los = live.get(e.getKey());
            if (los != null && imminent(r.eventTime(), los, now)) {
                Map<String, Object> payload = new LinkedHashMap<>(r.payload());
                payload.put("imminent", true);
                NormalizedRecord flagged = new NormalizedRecord(r.externalId(), r.kind(), now, r.eventTime(),
                        r.geometry(), r.validUntil(), r.validFrom(), payload, false, r.quality(), r.observers());
                ctx.emit(flagged);
                lastEmitted.put(e.getKey(), flagged);
            }
        }
    }

    /** True from {@value #IMMINENT_MINUTES} minutes before AOS until LOS. */
    static boolean imminent(Instant aos, Instant los, Instant now) {
        return !now.isBefore(aos.minus(Duration.ofMinutes(IMMINENT_MINUTES))) && now.isBefore(los);
    }

    /**
     * Recomputes the passes of {@code only} (null: every watched satellite) over every chosen place, emits them
     * and tombstones the future passes of those satellites that are no longer predicted.
     */
    void recompute(DerivedContext ctx, Set<String> only, Instant now) {
        ValidatedParams params = ctx.params();
        double minEl = Optional.ofNullable(params.number(MIN_ELEVATION_PARAM)).orElse(10.0);
        Duration horizon = Optional.ofNullable(params.duration(HORIZON_PARAM)).orElse(Duration.ofHours(48));
        int maxPerPlace = params.integer(MAX_PASSES_PARAM, 100);
        Map<String, OmmElements> elements = new TreeMap<>();
        for (String norad : norads(params)) {
            if (only != null && !only.contains(norad)) {
                continue;
            }
            ctx.records().get(ELEMENTS_SOURCE, norad)
                    .filter(h -> !h.tombstone())
                    .flatMap(h -> parse(h, ctx))
                    .ifPresent(e -> elements.put(norad, e));
        }
        if (elements.isEmpty()) {
            return;
        }
        Set<String> failed = new LinkedHashSet<>();
        for (PlaceView place : places(ctx, params)) {
            Observer observer = observer(place);
            List<Candidate> candidates = new ArrayList<>();
            for (Map.Entry<String, OmmElements> e : elements.entrySet()) {
                if (failed.contains(e.getKey())) {
                    continue;
                }
                try {
                    for (PredictedPass pass : PassPredictor.predict(e.getValue(), observer, now, horizon, minEl)) {
                        candidates.add(new Candidate(e.getKey(), e.getValue(), pass));
                    }
                } catch (IllegalArgumentException ex) {
                    ctx.log().warn("{}: NORAD {} could not be propagated for {}: {}", ctx.sourceId(), e.getKey(),
                            place.id(), ex.getMessage());
                    failed.add(e.getKey());
                }
            }
            candidates.sort((a, b) -> a.pass().aos().compareTo(b.pass().aos()));
            Set<String> emitted = new LinkedHashSet<>();
            for (Candidate c : candidates.subList(0, Math.min(maxPerPlace, candidates.size()))) {
                NormalizedRecord record = record(place, c.norad(), c.elements(), c.pass(), now);
                ctx.emit(record);
                emitted.add(record.externalId());
                live.put(record.externalId(), round(c.pass().los()));
                lastEmitted.put(record.externalId(), record);
            }
            // A future pass of a recomputed satellite that is no longer predicted is gone.
            for (String id : new ArrayList<>(live.keySet())) {
                String[] parts = id.split(":");
                if (parts.length != 4 || !parts[1].equals(place.id()) || !elements.containsKey(parts[2])
                        || failed.contains(parts[2]) || emitted.contains(id)) {
                    continue;
                }
                long aosMinute = Long.parseLong(parts[3]);
                if (Instant.ofEpochSecond(aosMinute * 60).isAfter(now)) {
                    ctx.tombstone(KIND, id);
                    live.remove(id);
                    lastEmitted.remove(id);
                }
            }
        }
    }

    /** One candidate pass. */
    private record Candidate(String norad, OmmElements elements, PredictedPass pass) {}

    /**
     * The record of one pass.
     *
     * @param now the computation time, the record's {@code observedAt} (the source is RECEIVED-mode)
     */
    static NormalizedRecord record(PlaceView place, String norad, OmmElements e, PredictedPass pass, Instant now) {
        Instant aos = round(pass.aos());
        String id = "place:" + place.id() + ":" + norad + ":" + Math.floorDiv(aos.getEpochSecond(), 60);
        Instant los = round(pass.los());
        NormalizedRecord.Builder b = NormalizedRecord.builder(id, KIND)
                .observedAt(now)
                .eventTime(aos)
                .validFrom(aos)
                .put("satellite", e.objectName())
                .put("norad", Long.parseLong(norad))
                .put("place", place.id())
                .put("status", "predicted")
                .put("aos", aos.toString())
                .put("tca", round(pass.tca()).toString())
                .put("los", los.toString())
                .put("maxElevationDeg", Math.round(pass.maxElevationDeg() * 10) / 10.0)
                .put("aosAzimuthDeg", (double) Math.round(pass.aosAzimuthDeg()) % 360)
                .put("losAzimuthDeg", (double) Math.round(pass.losAzimuthDeg()) % 360)
                .put("imminent", imminent(aos, los, now));
        Geometry track = GroundTracks.geometry(pass.groundTrack());
        if (track != null) {
            b.geometry(track);
        }
        return b.build();
    }

    /** {@code t} rounded to the nearest {@value #TIME_ROUNDING_SECONDS} seconds. */
    static Instant round(Instant t) {
        // Round the whole instant to the nearest 10 s (34.9 s -> 30 s, 35.0 s -> 40 s).
        long rounded = Math.round(t.toEpochMilli() / (TIME_ROUNDING_SECONDS * 1000.0)) * TIME_ROUNDING_SECONDS;
        return Instant.ofEpochSecond(rounded);
    }

    /** The place id of {@code place:{placeId}:{norad}:{minute}}, or null. */
    static String placeIdOf(String externalId) {
        String[] parts = externalId == null ? new String[0] : externalId.split(":");
        return parts.length == 4 && "place".equals(parts[0]) ? parts[1] : null;
    }

    /** The places passes are computed for: the named ones, else every non-sensitive point place. */
    static List<PlaceView> places(DerivedContext ctx, ValidatedParams params) {
        List<String> named = params.has(PLACES_PARAM) ? params.stringList(PLACES_PARAM) : List.of();
        if (named != null && !named.isEmpty()) {
            List<PlaceView> out = new ArrayList<>();
            for (String id : named) {
                ctx.places().find(id.strip()).ifPresent(out::add);
            }
            return out;
        }
        return ctx.places().all().stream()
                .filter(p -> "point".equals(p.shape()) && !p.sensitive())
                .toList();
    }

    /** The observer of a place: its point (or interior point) and an {@code altitudeM} attribute, else 0 m. */
    static Observer observer(PlaceView place) {
        Geometry g = place.geometry() == null || place.geometry().isEmpty() ? place.area() : place.geometry();
        Point p = g instanceof Point point ? point : g.getInteriorPoint();
        Object alt = place.attributes().get("altitudeM");
        double altM = alt instanceof Number n && Double.isFinite(n.doubleValue()) ? n.doubleValue() : 0.0;
        return new Observer(p.getY(), p.getX(), altM);
    }

    private static List<String> norads(ValidatedParams params) {
        List<String> list = params.has(NORAD_PARAM) ? params.stringList(NORAD_PARAM) : null;
        return (list == null ? DEFAULT_NORAD : list).stream().map(String::strip).filter(s -> !s.isEmpty()).toList();
    }

    private static Optional<OmmElements> parse(HeadSnapshot head, DerivedContext ctx) {
        try {
            return Optional.of(OmmElements.fromPayload(head.payload()));
        } catch (IllegalArgumentException e) {
            ctx.log().debug("{}: unreadable elements for {}: {}", ctx.sourceId(), head.externalId(), e.getMessage());
            return Optional.empty();
        }
    }

    private static Instant instant(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Instant.parse(value.toString());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    static SourceDescriptor buildDescriptor() {
        return SourceDescriptor.builder(ID)
                .name("Satellite passes (derived)")
                .description("Predicted passes of watched satellites (default ISS, METEOR-M2 3 and METEOR-M2 4) over"
                        + " your point places for the next 48 h, computed with Orekit SGP4 from celestrak-gp elements."
                        + " Status predicted; tombstoned 10 minutes after LOS. A trigger with forecastHorizon on"
                        + " eventTime gives \"pass in 10 minutes\" alerts.")
                .kind(KIND)
                .mode(Mode.DERIVED)
                .attribution("Derived by Foresignal from CelesTrak GP element sets (Orekit SGP4)")
                .licence("derived (see inputs)")
                .defaultBudget(Budget.of(1, 1))
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.RECEIVED)
                .recordTtl(null)
                .zeroResultsNormal(true)
                .kindRetention(KIND, RETENTION)
                .titleField("satellite")
                .searchFields("satellite", "place")
                .tileProperties("satellite", "norad", "place", "aos", "los", "maxElevationDeg")
                .kindDisplay(KIND, new KindDisplay("{satellite} pass, max el {maxElevationDeg}°", "antenna", "#7048E8", null))
                .param(ParamSpec.optional(NORAD_PARAM, ParamType.STRING_LIST,
                        "Catalog numbers to predict (default ISS, METEOR-M2 3, METEOR-M2 4)", DEFAULT_NORAD))
                .param(ParamSpec.optional(PLACES_PARAM, ParamType.STRING_LIST,
                        "Place ids; empty means every non-sensitive point place", List.of()))
                .param(ParamSpec.optional(MIN_ELEVATION_PARAM, ParamType.NUMBER,
                        "The elevation a pass must rise above, degrees", 10))
                .param(ParamSpec.optional(HORIZON_PARAM, ParamType.DURATION, "How far ahead to predict", "48h"))
                .param(ParamSpec.optional(MAX_PASSES_PARAM, ParamType.INTEGER, "The most passes kept per place", 100))
                .build();
    }
}
