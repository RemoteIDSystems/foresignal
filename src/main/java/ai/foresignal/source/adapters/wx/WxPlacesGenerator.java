package ai.foresignal.source.adapters.wx;

import ai.foresignal.derive.DerivedContext;
import ai.foresignal.derive.DerivedGenerator;
import ai.foresignal.derive.DerivedInputs;
import ai.foresignal.derive.HeadSnapshot;
import ai.foresignal.derive.RasterFrameEvent;
import ai.foresignal.place.PlaceView;
import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.raster.sample.FrameView;
import ai.foresignal.raster.sample.PlaceGridIndex;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FlashSink;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.stream.ChangeBatch;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.TreeMap;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

/**
 * {@code wx-places} (prompt 052; Weather-Imagery-Design §7.2): one {@code place-weather} record per place,
 * externalId {@code place:{id}}, written only when its quantized payload changes.
 *
 * <p><b>Inputs:</b> frame events of {@code mrms-reflectivity}, {@code mrms-mesh-60m},
 * {@code mrms-precip-rate}, {@code hrrr-smoke} and the {@code gfs*} products; {@code radar-cell} changes of
 * {@code wx-cells}; place edits (reconcile); and a 60 s tick that judges staleness and reads the GLM ring
 * (prompt 048).
 *
 * <p><b>Sections</b> ({@link PlaceWeatherState}):
 * <ul>
 *   <li>{@code radar}: the reflectivity maximum within the place radius (0.5 dBZ) and the distance to the
 *       nearest 40, 50 and 60 dBZ cells within 100 km ({@value WxRound#NONE} for none), plus the 10-minute
 *       rolling minimum {@code nearest50Km10m}; stale after 6 min;
 *   <li>{@code hail}: the MESH maximum within the radius and the nearest ≥ 25 mm cell; stale after 6 min;
 *   <li>{@code precip}: the precipitation rate at the place, 1 mm/h; stale after 6 min; and (prompt 053) the
 *       maxima within the radius of the MRMS 1-hour QPE ({@code qpe1hMmMax}, 1 mm), 24-hour QPE
 *       ({@code qpe24hMmMax}, 1 mm) and flash-flood guidance ratio ({@code ffgRatio1hPctMax}, 1 %), each judged
 *       against its own product's gap (30 min, 150 min, 30 min) and merged by {@link PlaceWeatherState};
 *   <li>{@code lightning}: from the GLM ring, the nearest flash in 15 and 30 minutes and the flashes within
 *       15 km in 15 minutes; stale when no GLM file was processed for 2 minutes. GLM's footprint over Texas
 *       is about 8–10 km and its detection efficiency is below 100%: GLM-only rules should use 15 km or
 *       more, and all-clears are advisory;
 *   <li>{@code cell}: the ETA of a ≥ 50 dBZ core ({@link CellEta}); unavailable while {@code wx-cells} is
 *       disabled or failing, and as unknown as the radar section otherwise;
 *   <li>{@code smoke}: the HRRR near-surface smoke maximum over the next 24 h of the newest complete run;
 *   <li>{@code forecast.gfs}: the GFS gust maximum and precipitation over the next 24 h;
 *   <li>{@code forecast.nws} (prompt 053): {@code popMax24h}, {@code qpf24hMm} and {@code gustMaxMs24h} from the
 *       place's {@code nws-forecast} head ({@code nws-gridpoints}); {@code stale} when its
 *       {@code forecastUpdated} is older than 6 h, {@code unavailable} while that source is disabled or failing or
 *       has no forecast for the place.
 * </ul>
 * Distances are measured from the place's representative point. The record has no geometry: a place's
 * position is sensitive (prompt 036) and must not reach read tokens through the records API.
 *
 * <p><b>Lifecycle:</b> a deleted place's record is tombstoned, which may fire {@code exit} rules (a tombstone
 * after a matching version is an exit, Core-Data-Model §11); records are never tombstoned because input is
 * stale. {@code reconcile} restores every section from the heads, tombstones the heads of deleted places and
 * rebuilds the rolling radar window from the last 10 minutes of frames.
 */
@Component
public class WxPlacesGenerator implements DerivedGenerator {

    /** The source id. */
    public static final String ID = "wx-places";

    /** The record kind. */
    public static final String KIND = "place-weather";

    /** MRMS reflectivity. */
    public static final String REFLECTIVITY = "mrms-reflectivity";

    /** MRMS 60-minute MESH. */
    public static final String MESH = "mrms-mesh-60m";

    /** MRMS precipitation rate. */
    public static final String PRECIP = "mrms-precip-rate";

    /** HRRR near-surface smoke. */
    public static final String SMOKE = "hrrr-smoke";

    /** GFS gusts. */
    public static final String GFS_GUST = "gfs-gust";

    /** GFS accumulated precipitation (run total). */
    public static final String GFS_APCP = "gfs-apcp";

    /** The radar-cell source. */
    public static final String CELLS = "wx-cells";

    /** The GLM source. */
    public static final String GLM = "goes-glm";

    /** MRMS radar-only 1-hour QPE (prompt 053). */
    public static final String QPE_1H = "mrms-qpe-1h";

    /** MRMS multi-sensor 24-hour QPE (prompt 053). */
    public static final String QPE_24H = "mrms-qpe-24h";

    /** MRMS 1-hour flash-flood guidance ratio (prompt 053). */
    public static final String FFG = "mrms-ffg-ratio-1h";

    /** The NWS gridpoint forecast source (prompt 053). */
    public static final String NWS = "nws-gridpoints";

    /** The NWS forecast kind (prompt 053). */
    public static final String NWS_KIND = "nws-forecast";

    /** The gap of the 10-minute MRMS products (QPE 1 h, FFG ratio). */
    public static final Duration TEN_MINUTE_MAX_GAP = Duration.ofMinutes(30);

    /** The gap of the hourly 24-hour QPE (files arrive about 59 minutes after their valid time). */
    public static final Duration QPE_24H_MAX_GAP = Duration.ofMinutes(150);

    /** The age after which an NWS forecast is stale. */
    public static final Duration NWS_MAX_AGE = Duration.ofHours(6);

    /** MRMS maximum gap. */
    public static final Duration MRMS_MAX_GAP = Duration.ofMinutes(6);

    /** GLM maximum gap. */
    public static final Duration GLM_MAX_GAP = Duration.ofMinutes(2);

    /** The oldest model run still current. */
    public static final Duration RUN_MAX_AGE = Duration.ofHours(12);

    /** The staleness tick. */
    public static final Duration TICK = Duration.ofSeconds(60);

    /** The forecast window. */
    public static final Duration FORECAST_WINDOW = Duration.ofHours(24);

    private static final double[] RADAR_THRESHOLDS = {40, 50, 60};
    private static final double[] HAIL_THRESHOLDS = {25};
    private static final double[] NO_THRESHOLDS = {};

    private final SourceDescriptor descriptor = buildDescriptor();
    private final TreeMap<String, PlaceWeatherState> places = new TreeMap<>();

    /** Creates the generator. */
    public WxPlacesGenerator() {}

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** The descriptor. */
    static SourceDescriptor buildDescriptor() {
        return SourceDescriptor.builder(ID)
                .name("Place weather (derived)")
                .description("Automated weather near each place, from MRMS radar and rainfall, GOES GLM lightning,"
                        + " wx-cells storm cells, HRRR smoke, GFS forecasts and the NWS gridpoint forecast. Sections that are stale, failing or without coverage"
                        + " keep their previous values with a non-ok status; unknown is never clear. Not a warning.")
                .kind(KIND)
                .attribution("Derived by Foresignal from NOAA MRMS, GOES-19 GLM, HRRR, GFS and NWS forecasts")
                .licence("derived (see inputs)")
                .mode(Mode.DERIVED)
                .defaultBudget(Budget.of(1, 1))
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .recordTtl(null)
                .zeroResultsNormal(true)
                .defaultRetention(Duration.ofDays(14))
                .kindRetention(KIND, Duration.ofDays(14))
                .titleField("place")
                .build();
    }

    @Override
    public DerivedInputs inputs(ValidatedParams params) {
        return new DerivedInputs(Set.of(CELLS, NWS), Set.of(WxCellsGenerator.KIND, NWS_KIND),
                Set.of(REFLECTIVITY, MESH, PRECIP, QPE_1H, QPE_24H, FFG, SMOKE, "gfs", GFS_GUST, "gfs-t2m", GFS_APCP),
                TICK, true);
    }

    // ------------------------------------------------------------------ hooks

    @Override
    public void reconcile(List<HeadSnapshot> ownHeads, DerivedContext ctx) {
        Instant now = ctx.now();
        places.clear();
        Map<String, HeadSnapshot> heads = new HashMap<>();
        for (HeadSnapshot head : ownHeads) {
            String placeId = PlaceWeatherState.placeIdOf(head.externalId());
            if (placeId == null || head.tombstone() || !KIND.equals(head.kind())) {
                continue;
            }
            if (ctx.places().find(placeId).isEmpty()) {
                ctx.tombstone(KIND, head.externalId());
            } else {
                heads.put(placeId, head);
            }
        }
        for (PlaceView place : ctx.places().all()) {
            double[] point = point(place);
            HeadSnapshot head = heads.get(place.id());
            places.put(place.id(), head == null
                    ? new PlaceWeatherState(place.id(), place.name(), radius(place), point[0], point[1], place.version())
                    : PlaceWeatherState.restore(place.id(), place.name(), radius(place), point[0], point[1],
                            place.version(), head.payload(), head.observedAt()));
        }
        rebuildRadarWindow(ctx, now);
        refresh(ctx, now, true);
        emitAll(ctx, now);
    }

    @Override
    public void onFrame(RasterFrameEvent event, DerivedContext ctx) {
        Instant now = ctx.now();
        syncPlaces(ctx);
        switch (event.productId()) {
            case REFLECTIVITY -> {
                updateRadar(ctx, now, false);
                updateCells(ctx, now);
            }
            case MESH -> updateHail(ctx, now, false);
            case PRECIP -> updatePrecip(ctx, now, false);
            case QPE_1H, QPE_24H, FFG -> updatePrecipPart(ctx, now, false, event.productId());
            case SMOKE -> updateSmoke(ctx, now, event.complete());
            case GFS_GUST, GFS_APCP -> updateGfs(ctx, now, event.complete());
            default -> {
                // gfs winds and t2m feed the forecast series, not the record.
            }
        }
        emitAll(ctx, event.observedAt());
    }

    @Override
    public void onChanges(ChangeBatch batch, DerivedContext ctx) {
        syncPlaces(ctx);
        updateCells(ctx, ctx.now());
        updateNws(ctx, ctx.now());
        emitAll(ctx, batch.ingestedAt() == null ? ctx.now() : batch.ingestedAt());
    }

    @Override
    public void onTick(Instant now, DerivedContext ctx) {
        syncPlaces(ctx);
        refresh(ctx, now, false);
        emitAll(ctx, now);
    }

    // ------------------------------------------------------------------ places

    /** Follows place edits: new places start fresh, deleted ones are tombstoned, moved ones reset. */
    void syncPlaces(DerivedContext ctx) {
        Map<String, PlaceView> current = new LinkedHashMap<>();
        for (PlaceView p : ctx.places().all()) {
            current.put(p.id(), p);
        }
        for (String id : new ArrayList<>(places.keySet())) {
            if (!current.containsKey(id)) {
                places.remove(id);
                ctx.tombstone(KIND, PlaceWeatherState.externalId(id));
            }
        }
        for (PlaceView p : current.values()) {
            double[] point = point(p);
            PlaceWeatherState st = places.get(p.id());
            if (st == null) {
                places.put(p.id(), new PlaceWeatherState(p.id(), p.name(), radius(p), point[0], point[1], p.version()));
                continue;
            }
            if (st.version != p.version()) {
                st.version = p.version();
                st.name = p.name();
                st.radiusKm = radius(p);
                if (st.lon != point[0] || st.lat != point[1]) {
                    st.lon = point[0];
                    st.lat = point[1];
                    st.moved();
                }
                st.radarFrame = null;
                st.hailFrame = null;
                st.precipFrame = null;
                st.qpe1hFrame = null;
                st.qpe24hFrame = null;
                st.ffgFrame = null;
            }
        }
    }

    /** The place's representative point {@code {lon, lat}}. */
    static double[] point(PlaceView place) {
        Geometry g = place.geometry();
        if (g == null || g.isEmpty()) {
            g = place.area();
        }
        Point p = g instanceof Point point ? point : g.getInteriorPoint();
        return new double[] {p.getX(), p.getY()};
    }

    /** The place's radius: its own, else 20 km. */
    static double radius(PlaceView place) {
        return place.radiusKm() == null || place.radiusKm() <= 0 ? 20.0 : place.radiusKm();
    }

    /** The current states (tests). */
    Map<String, PlaceWeatherState> states() {
        return places;
    }

    // ------------------------------------------------------------------ sections

    /** Re-evaluates every section; {@code force} recomputes even when the input did not change. */
    void refresh(DerivedContext ctx, Instant now, boolean force) {
        updateRadar(ctx, now, force);
        updateHail(ctx, now, force);
        updatePrecip(ctx, now, force);
        updatePrecipPart(ctx, now, force, QPE_1H);
        updatePrecipPart(ctx, now, force, QPE_24H);
        updatePrecipPart(ctx, now, force, FFG);
        updateLightning(ctx, now);
        updateCells(ctx, now);
        updateSmoke(ctx, now, force);
        updateGfs(ctx, now, force);
        updateNws(ctx, now);
    }

    /** The precip part of a prompt 053 product. */
    static WxSection part(PlaceWeatherState p, String productId) {
        return switch (productId) {
            case QPE_1H -> p.qpe1h;
            case QPE_24H -> p.qpe24h;
            default -> p.ffg;
        };
    }

    private static String partFrame(PlaceWeatherState p, String productId) {
        return switch (productId) {
            case QPE_1H -> p.qpe1hFrame;
            case QPE_24H -> p.qpe24hFrame;
            default -> p.ffgFrame;
        };
    }

    private static void partFrame(PlaceWeatherState p, String productId, String frameId) {
        switch (productId) {
            case QPE_1H -> p.qpe1hFrame = frameId;
            case QPE_24H -> p.qpe24hFrame = frameId;
            default -> p.ffgFrame = frameId;
        }
    }

    /** The payload key of a prompt 053 product's maximum within the radius. */
    static String partKey(String productId) {
        return switch (productId) {
            case QPE_1H -> PlaceWeatherState.QPE_1H_KEY;
            case QPE_24H -> PlaceWeatherState.QPE_24H_KEY;
            default -> PlaceWeatherState.FFG_KEY;
        };
    }

    /** The gap of a prompt 053 product. */
    static Duration partGap(String productId) {
        return QPE_24H.equals(productId) ? QPE_24H_MAX_GAP : TEN_MINUTE_MAX_GAP;
    }

    /**
     * One prompt 053 precip input (QPE 1 h, QPE 24 h or the FFG ratio): the maximum within each place's radius,
     * whole units. A product that is disabled or not installed leaves the section until it returns.
     */
    void updatePrecipPart(DerivedContext ctx, Instant now, boolean force, String productId) {
        Optional<String> reason = ctx.rasters().unavailableReason(productId);
        if (reason.isPresent() && (reason.get().contains(" is disabled") || reason.get().contains("not installed"))) {
            places.values().forEach(p -> {
                part(p, productId).clear();
                partFrame(p, productId, null);
            });
            return;
        }
        WxInput in = WxInput.observation(ctx, productId, partGap(productId), now);
        if (!in.ok()) {
            places.values().forEach(p -> part(p, productId).notOk(in.status()));
            return;
        }
        RasterFrameRow frame = in.frame();
        List<PlaceWeatherState> todo = places.values().stream()
                .filter(p -> force || !frame.frameId().equals(partFrame(p, productId)) || !part(p, productId).ok())
                .toList();
        if (todo.isEmpty()) {
            return;
        }
        Optional<FrameView> view = ctx.rasters().view(frame);
        if (view.isEmpty()) {
            todo.forEach(p -> part(p, productId).notOk(WxSection.UNAVAILABLE));
            return;
        }
        for (PlaceWeatherState p : todo) {
            PlaceGridIndex.Scan scan = PlaceGridIndex.scan(view.get(),
                    PlaceGridIndex.of(frame.gridId(), view.get().grid(), p.lon, p.lat), NO_THRESHOLDS, p.radiusKm);
            partFrame(p, productId, frame.frameId());
            if (!PlaceWeatherState.covered(scan)) {
                part(p, productId).notOk(WxSection.NO_COVERAGE);
                continue;
            }
            double max = Double.isNaN(scan.maxInRadius()) ? 0 : Math.max(0, scan.maxInRadius());
            Map<String, Object> m = new LinkedHashMap<>();
            m.put(partKey(productId), WxRound.whole(max));
            part(p, productId).good(m, frame.validTime());
        }
    }

    /**
     * {@code forecast.nws} from the places' {@code nws-forecast} heads: unavailable while {@value #NWS} is disabled
     * or failing or a place has no forecast; stale when the forecast is older than {@link #NWS_MAX_AGE} (values
     * kept, {@code asOf} = its {@code forecastUpdated}).
     */
    void updateNws(DerivedContext ctx, Instant now) {
        HealthStatus status = ctx.sourceStatus(NWS);
        if (status == HealthStatus.DISABLED || status == HealthStatus.FAILING) {
            places.values().forEach(p -> p.nws.notOk(WxSection.UNAVAILABLE));
            return;
        }
        Map<String, HeadSnapshot> heads = new HashMap<>();
        for (HeadSnapshot head : ctx.records().current(List.of(NWS), List.of(NWS_KIND), 5_000)) {
            if (!head.tombstone()) {
                heads.put(head.externalId(), head);
            }
        }
        for (PlaceWeatherState p : places.values()) {
            HeadSnapshot head = heads.get(PlaceWeatherState.externalId(p.placeId));
            if (head == null) {
                p.nws.notOk(WxSection.UNAVAILABLE);
                continue;
            }
            Instant updated = parseInstant(head.payload().get("forecastUpdated"));
            if (updated == null) {
                updated = head.observedAt();
            }
            Map<String, Object> m = new LinkedHashMap<>();
            for (String key : List.of("popMax24h", "qpf24hMm", "gustMaxMs24h")) {
                Object v = head.payload().get(key);
                if (v != null) {
                    m.put(key, v);
                }
            }
            p.nws.good(m, updated);
            if (updated != null && Duration.between(updated, now).compareTo(NWS_MAX_AGE) > 0) {
                p.nws.notOk(WxSection.STALE);
            }
        }
    }

    private static Instant parseInstant(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Instant.parse(value.toString());
        } catch (RuntimeException e) {
            return null;
        }
    }

    void updateRadar(DerivedContext ctx, Instant now, boolean force) {
        WxInput in = WxInput.observation(ctx, REFLECTIVITY, MRMS_MAX_GAP, now);
        if (!in.ok()) {
            places.values().forEach(p -> p.radar.notOk(in.status()));
            return;
        }
        RasterFrameRow frame = in.frame();
        List<PlaceWeatherState> todo = places.values().stream()
                .filter(p -> force || !frame.frameId().equals(p.radarFrame) || !p.radar.ok()).toList();
        if (todo.isEmpty()) {
            return;
        }
        Optional<FrameView> view = ctx.rasters().view(frame);
        if (view.isEmpty()) {
            todo.forEach(p -> p.radar.notOk(WxSection.UNAVAILABLE));
            return;
        }
        for (PlaceWeatherState p : todo) {
            PlaceGridIndex.Scan scan = PlaceGridIndex.scan(view.get(),
                    PlaceGridIndex.of(frame.gridId(), view.get().grid(), p.lon, p.lat), RADAR_THRESHOLDS, p.radiusKm);
            p.radarFrame = frame.frameId();
            if (!PlaceWeatherState.covered(scan)) {
                p.radar.notOk(WxSection.NO_COVERAGE);
                continue;
            }
            double n10 = p.addRadar(frame.validTime(), WxRound.km(scan.nearestKm()[1]));
            p.radar.good(PlaceWeatherState.radarMetrics(scan, n10), frame.validTime());
        }
    }

    void updateHail(DerivedContext ctx, Instant now, boolean force) {
        WxInput in = WxInput.observation(ctx, MESH, MRMS_MAX_GAP, now);
        if (!in.ok()) {
            places.values().forEach(p -> p.hail.notOk(in.status()));
            return;
        }
        RasterFrameRow frame = in.frame();
        List<PlaceWeatherState> todo = places.values().stream()
                .filter(p -> force || !frame.frameId().equals(p.hailFrame) || !p.hail.ok()).toList();
        if (todo.isEmpty()) {
            return;
        }
        Optional<FrameView> view = ctx.rasters().view(frame);
        if (view.isEmpty()) {
            todo.forEach(p -> p.hail.notOk(WxSection.UNAVAILABLE));
            return;
        }
        for (PlaceWeatherState p : todo) {
            PlaceGridIndex.Scan scan = PlaceGridIndex.scan(view.get(),
                    PlaceGridIndex.of(frame.gridId(), view.get().grid(), p.lon, p.lat), HAIL_THRESHOLDS, p.radiusKm);
            p.hailFrame = frame.frameId();
            if (!PlaceWeatherState.covered(scan)) {
                p.hail.notOk(WxSection.NO_COVERAGE);
                continue;
            }
            p.hail.good(PlaceWeatherState.hailMetrics(scan), frame.validTime());
        }
    }

    void updatePrecip(DerivedContext ctx, Instant now, boolean force) {
        WxInput in = WxInput.observation(ctx, PRECIP, MRMS_MAX_GAP, now);
        if (!in.ok()) {
            places.values().forEach(p -> p.precip.notOk(in.status()));
            return;
        }
        RasterFrameRow frame = in.frame();
        List<PlaceWeatherState> todo = places.values().stream()
                .filter(p -> force || !frame.frameId().equals(p.precipFrame) || !p.precip.ok()).toList();
        if (todo.isEmpty()) {
            return;
        }
        Optional<FrameView> view = ctx.rasters().view(frame);
        if (view.isEmpty()) {
            todo.forEach(p -> p.precip.notOk(WxSection.UNAVAILABLE));
            return;
        }
        for (PlaceWeatherState p : todo) {
            FrameView.Sample s = view.get().sample(p.lon, p.lat);
            p.precipFrame = frame.frameId();
            if (s.kind() != FrameView.Kind.VALUE) {
                p.precip.notOk(WxSection.NO_COVERAGE);
                continue;
            }
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("rateMmH", WxRound.whole(Math.max(0, s.value())));
            p.precip.good(m, frame.validTime());
        }
    }

    void updateLightning(DerivedContext ctx, Instant now) {
        if (ctx.sourceStatus(GLM) == HealthStatus.FAILING) {
            places.values().forEach(p -> p.lightning.notOk(WxSection.UNAVAILABLE));
            return;
        }
        FlashSink flashes;
        try {
            flashes = ctx.rasters().flashes();
        } catch (UnsupportedOperationException e) {
            places.values().forEach(p -> p.lightning.notOk(WxSection.UNAVAILABLE));
            return;
        }
        Optional<Instant> newest = flashes.newestFlashFileTime();
        if (newest.isEmpty()) {
            places.values().forEach(p -> p.lightning.notOk(WxSection.UNAVAILABLE));
            return;
        }
        if (Duration.between(newest.get(), now).compareTo(GLM_MAX_GAP) > 0) {
            places.values().forEach(p -> p.lightning.notOk(WxSection.STALE));
            return;
        }
        for (PlaceWeatherState p : places.values()) {
            p.lightning.good(lightningMetrics(flashes, p.lon, p.lat, now), newest.get());
        }
    }

    /** The lightning metrics of a point from the ring. */
    static Map<String, Object> lightningMetrics(FlashSink flashes, double lon, double lat, Instant now) {
        Point point = Geo.point(lon, lat);
        OptionalDouble n15 = flashes.nearestKm(point, now.minus(Duration.ofMinutes(15)));
        OptionalDouble n30 = flashes.nearestKm(point, now.minus(Duration.ofMinutes(30)));
        int count = flashes.flashesWithin(point, 15, now.minus(Duration.ofMinutes(15))).size();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("nearestKm15m", WxRound.km(n15.isPresent() ? n15.getAsDouble() : Double.NaN));
        m.put("nearestKm30m", WxRound.km(n30.isPresent() ? n30.getAsDouble() : Double.NaN));
        m.put("count15km15m", count);
        return m;
    }

    void updateCells(DerivedContext ctx, Instant now) {
        HealthStatus cells = ctx.sourceStatus(CELLS);
        if (cells == HealthStatus.DISABLED || cells == HealthStatus.FAILING) {
            places.values().forEach(p -> p.cell.notOk(WxSection.UNAVAILABLE));
            return;
        }
        List<HeadSnapshot> current = null;
        for (PlaceWeatherState p : places.values()) {
            if (!p.radar.ok()) {
                p.cell.notOk(p.radar.status());
                continue;
            }
            if (current == null) {
                current = ctx.records().current(List.of(CELLS), List.of(WxCellsGenerator.KIND), 5_000);
            }
            p.cell.good(CellEta.metrics(current, p.lon, p.lat, p.radiusKm, now), now);
        }
    }

    void updateSmoke(DerivedContext ctx, Instant now, boolean force) {
        WxInput in = WxInput.forecast(ctx, SMOKE, RUN_MAX_AGE, now);
        if (!in.ok()) {
            places.values().forEach(p -> p.smoke.notOk(in.status()));
            return;
        }
        List<PlaceWeatherState> todo = places.values().stream()
                .filter(p -> force || !in.run().equals(p.smokeRun)).toList();
        if (todo.isEmpty()) {
            return;
        }
        double[] max = windowMax(ctx, SMOKE, in.run(), now, todo);
        for (int i = 0; i < todo.size(); i++) {
            PlaceWeatherState p = todo.get(i);
            p.smokeRun = in.run();
            if (Double.isNaN(max[i])) {
                p.smoke.notOk(WxSection.NO_COVERAGE);
                continue;
            }
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("hrrrPm25Max24h", WxRound.whole(Math.max(0, max[i])));
            p.smoke.good(m, in.run());
        }
    }

    void updateGfs(DerivedContext ctx, Instant now, boolean force) {
        WxInput gust = WxInput.forecast(ctx, GFS_GUST, RUN_MAX_AGE, now);
        if (!gust.ok()) {
            places.values().forEach(p -> p.gfs.notOk(gust.status()));
            return;
        }
        List<PlaceWeatherState> todo = places.values().stream()
                .filter(p -> force || !gust.run().equals(p.gfsRun)).toList();
        if (todo.isEmpty()) {
            return;
        }
        double[] gustMax = windowMax(ctx, GFS_GUST, gust.run(), now, todo);
        WxInput apcp = WxInput.forecast(ctx, GFS_APCP, RUN_MAX_AGE, now);
        double[] precip = apcp.ok() ? precip24h(ctx, apcp.run(), now, todo) : null;
        for (int i = 0; i < todo.size(); i++) {
            PlaceWeatherState p = todo.get(i);
            p.gfsRun = gust.run();
            if (Double.isNaN(gustMax[i])) {
                p.gfs.notOk(WxSection.NO_COVERAGE);
                continue;
            }
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("gustMax24h", WxRound.whole(gustMax[i]));
            if (precip != null && !Double.isNaN(precip[i])) {
                m.put("precip24hMm", WxRound.whole(Math.max(0, precip[i])));
            }
            p.gfs.good(m, gust.run());
        }
    }

    /** Per place, the maximum of a forecast product over the next 24 h of a run (NaN off the grid). */
    static double[] windowMax(DerivedContext ctx, String productId, Instant run, Instant now,
            List<PlaceWeatherState> todo) {
        double[] max = new double[todo.size()];
        java.util.Arrays.fill(max, Double.NaN);
        List<RasterFrameRow> steps = window(ctx.rasters().runFrames(productId, run), now);
        for (RasterFrameRow step : steps) {
            Optional<FrameView> view = ctx.rasters().view(step);
            if (view.isEmpty()) {
                continue;
            }
            for (int i = 0; i < todo.size(); i++) {
                FrameView.Sample s = view.get().sample(todo.get(i).lon, todo.get(i).lat);
                if (s.kind() == FrameView.Kind.VALUE && (Double.isNaN(max[i]) || s.value() > max[i])) {
                    max[i] = s.value();
                }
            }
        }
        return max;
    }

    /**
     * Per place, the GFS precipitation of the next 24 h: the run total at the step nearest now + 24 h minus the
     * run total at the step at or before now (zero before the first step).
     */
    static double[] precip24h(DerivedContext ctx, Instant run, Instant now, List<PlaceWeatherState> todo) {
        double[] out = new double[todo.size()];
        java.util.Arrays.fill(out, Double.NaN);
        List<RasterFrameRow> steps = ctx.rasters().runFrames(GFS_APCP, run);
        if (steps.isEmpty()) {
            return out;
        }
        RasterFrameRow start = null;
        RasterFrameRow end = null;
        Instant until = now.plus(FORECAST_WINDOW);
        for (RasterFrameRow s : steps) {
            if (!s.validTime().isAfter(now)) {
                start = s;
            }
            if (!s.validTime().isAfter(until)) {
                end = s;
            }
        }
        if (end == null) {
            return out;
        }
        Optional<FrameView> endView = ctx.rasters().view(end);
        Optional<FrameView> startView = start == null ? Optional.empty() : ctx.rasters().view(start);
        if (endView.isEmpty()) {
            return out;
        }
        for (int i = 0; i < todo.size(); i++) {
            FrameView.Sample e = endView.get().sample(todo.get(i).lon, todo.get(i).lat);
            if (e.kind() != FrameView.Kind.VALUE) {
                continue;
            }
            double base = 0;
            if (startView.isPresent()) {
                FrameView.Sample b = startView.get().sample(todo.get(i).lon, todo.get(i).lat);
                base = b.kind() == FrameView.Kind.VALUE ? b.value() : 0;
            }
            out[i] = e.value() - base;
        }
        return out;
    }

    /** The steps valid in {@code [now − 1 h, now + 24 h]}; all steps when none is. */
    static List<RasterFrameRow> window(List<RasterFrameRow> steps, Instant now) {
        Instant from = now.minus(Duration.ofHours(1));
        Instant to = now.plus(FORECAST_WINDOW);
        List<RasterFrameRow> out = steps.stream()
                .filter(s -> !s.validTime().isBefore(from) && !s.validTime().isAfter(to)).toList();
        return out.isEmpty() ? steps : out;
    }

    /** Rebuilds each place's rolling radar window from the reflectivity frames of the last 10 minutes. */
    void rebuildRadarWindow(DerivedContext ctx, Instant now) {
        if (ctx.rasters().product(REFLECTIVITY).isEmpty()) {
            return;
        }
        List<RasterFrameRow> recent = ctx.rasters().frames(REFLECTIVITY, now.minus(PlaceWeatherState.RADAR_WINDOW), now);
        for (RasterFrameRow frame : recent) {
            Optional<FrameView> view = ctx.rasters().view(frame);
            if (view.isEmpty()) {
                continue;
            }
            for (PlaceWeatherState p : places.values()) {
                PlaceGridIndex.Scan scan = PlaceGridIndex.scan(view.get(),
                        PlaceGridIndex.of(frame.gridId(), view.get().grid(), p.lon, p.lat), RADAR_THRESHOLDS,
                        p.radiusKm);
                if (PlaceWeatherState.covered(scan)) {
                    p.addRadar(frame.validTime(), WxRound.km(scan.nearestKm()[1]));
                }
            }
        }
    }

    /** Emits every place's payload; the runtime drops the unchanged ones. */
    void emitAll(DerivedContext ctx, Instant observedAt) {
        for (PlaceWeatherState p : places.values()) {
            ctx.emit(NormalizedRecord.builder(PlaceWeatherState.externalId(p.placeId), KIND)
                    .observedAt(observedAt)
                    .putAll(p.payload())
                    .build());
        }
    }
}
