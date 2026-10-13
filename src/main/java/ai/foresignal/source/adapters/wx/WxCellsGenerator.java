package ai.foresignal.source.adapters.wx;

import ai.foresignal.derive.DerivedContext;
import ai.foresignal.derive.DerivedGenerator;
import ai.foresignal.derive.DerivedInputs;
import ai.foresignal.derive.HeadSnapshot;
import ai.foresignal.derive.RasterFrameEvent;
import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.raster.RasterProfile;
import ai.foresignal.raster.sample.FrameView;
import ai.foresignal.raster.sample.GridWindow;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FlashSink;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.ValidatedParams;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code wx-cells} (prompt 052; Weather-Imagery-Design §7.2): tracked storm cells as {@code radar-cell}
 * records, externalId {@code c-{ulid}}, from each MRMS reflectivity frame ({@link RadarCellTracker}).
 *
 * <p><b>Scope:</b> the places AOI (the default); {@code scope=conus} scans the whole grid, honored only on
 * the {@code large} profile (on {@code small} it falls back to the AOI with a log warning).
 *
 * <p><b>Per frame</b> (complete, no older than 6 minutes; an older one is skipped without tombstoning):
 * detect the cells, keep those whose centroid lies in the AOI, match them to the previous frame's tracks
 * ({@code centroid-v1}), give unmatched cells new ids, and tombstone the tracks that found no cell. Properties:
 * {@code maxDbz} (0.5 dBZ), {@code areaKm2} (2 significant figures), {@code meshMax} (whole mm from the
 * current MESH frame, omitted when none), {@code glmFlashes5m} (flashes inside the cell in the last 5 minutes,
 * as a bucket lower bound: 0, 1, 3, 5, 10, 20, 50, 100, 200) and {@code motion{speedKmh (5 km/h),
 * bearingDeg (10°), method: centroid-v1}} once a cell has been matched. Geometry: the cell's pixels by
 * run-rectangle union, snapped to about 500 m. {@code observedAt} is the frame's valid time; {@code eventTime}
 * is when the cell was first seen.
 *
 * <p>{@code reconcile} restores the tracks from the heads (a restart keeps the ids), and tombstones nothing:
 * the next processed frame decides which cells vanished.
 */
@Component
public class WxCellsGenerator implements DerivedGenerator {

    /** The source id. */
    public static final String ID = "wx-cells";

    /** The record kind. */
    public static final String KIND = "radar-cell";

    /** The {@code scope} parameter. */
    public static final String SCOPE_PARAM = "scope";

    /** The reflectivity product. */
    public static final String REFLECTIVITY = "mrms-reflectivity";

    /** The MESH product. */
    public static final String MESH = "mrms-mesh-60m";

    /** The oldest reflectivity frame processed. */
    public static final Duration MAX_GAP = Duration.ofMinutes(6);

    /** Geometry quantum, degrees (about 500 m). */
    public static final double QUANTUM_DEG = 0.005;

    private static final int[] FLASH_BUCKETS = {0, 1, 3, 5, 10, 20, 50, 100, 200};

    private final boolean large;
    private final SourceDescriptor descriptor = buildDescriptor();
    private final Map<String, RadarCellTracker.Track> tracks = new LinkedHashMap<>();
    private Instant lastFrame;

    /**
     * Creates the generator.
     *
     * @param profile {@code foresignal.profile}: {@code scope=conus} is honored only on {@code large}
     */
    @Autowired
    public WxCellsGenerator(@Value("${foresignal.profile:small}") String profile) {
        this.large = RasterProfile.parse(profile) == RasterProfile.LARGE;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** The descriptor. */
    static SourceDescriptor buildDescriptor() {
        return SourceDescriptor.builder(ID)
                .name("Radar storm cells (derived)")
                .description("Storm cells (40 dBZ areas split at 50 dBZ cores) tracked across MRMS reflectivity"
                        + " frames inside the places AOI, with simple centroid motion. Automated, not a warning.")
                .kind(KIND)
                .attribution("Derived by Foresignal from NOAA MRMS and GOES-19 GLM")
                .licence("derived (see inputs)")
                .mode(Mode.DERIVED)
                .defaultBudget(Budget.of(1, 1))
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .recordTtl(null)
                .zeroResultsNormal(true)
                .defaultRetention(Duration.ofDays(3))
                .kindRetention(KIND, Duration.ofDays(3))
                .param(ParamSpec.enumerated(SCOPE_PARAM,
                        "aoi (the places AOI) or conus (the whole grid; large profile only)", "aoi",
                        List.of("aoi", "conus")))
                .build();
    }

    @Override
    public DerivedInputs inputs(ValidatedParams params) {
        return new DerivedInputs(Set.of(), Set.of(), Set.of(REFLECTIVITY), null, true);
    }

    @Override
    public void reconcile(List<HeadSnapshot> ownHeads, DerivedContext ctx) {
        tracks.clear();
        lastFrame = null;
        for (HeadSnapshot head : ownHeads) {
            if (head.tombstone() || !KIND.equals(head.kind()) || head.geometry() == null || head.geometry().isEmpty()) {
                continue;
            }
            Point c = head.geometry().getCentroid();
            Instant at = head.observedAt() == null ? ctx.now() : head.observedAt();
            tracks.put(head.externalId(), new RadarCellTracker.Track(head.externalId(), at, at, c.getX(), c.getY()));
            if (lastFrame == null || at.isAfter(lastFrame)) {
                lastFrame = at;
            }
        }
    }

    @Override
    public void onFrame(RasterFrameEvent event, DerivedContext ctx) {
        if (!REFLECTIVITY.equals(event.productId()) || !event.complete()) {
            return;
        }
        Instant now = ctx.now();
        WxInput in = WxInput.observation(ctx, REFLECTIVITY, MAX_GAP, now);
        if (!in.ok()) {
            return;
        }
        RasterFrameRow frame = in.frame();
        if (lastFrame != null && !frame.validTime().isAfter(lastFrame)) {
            return;
        }
        Optional<FrameView> view = ctx.rasters().view(frame);
        if (view.isEmpty()) {
            return;
        }
        process(ctx, view.get(), frame.validTime());
    }

    /** Processes one reflectivity frame: detect, match, emit and tombstone. */
    void process(DerivedContext ctx, FrameView view, Instant validTime) {
        boolean conus = large && "conus".equals(ctx.params().optionalString(SCOPE_PARAM).orElse("aoi"));
        if (!large && "conus".equals(ctx.params().optionalString(SCOPE_PARAM).orElse("aoi"))) {
            ctx.log().warn("wx-cells scope=conus needs the large profile; using the places AOI");
        }
        GridWindow window;
        PreparedGeometry aoi = null;
        if (conus) {
            window = new GridWindow(0, 0, view.nx() - 1, view.ny() - 1);
        } else {
            double[] env = ctx.places().aoiEnvelope();
            window = GridWindow.of(view.grid(), env[0], env[1], env[2], env[3]);
            MultiPolygon area = ctx.places().aoi();
            aoi = area == null || area.isEmpty() ? null : PreparedGeometryFactory.prepare(area);
        }
        List<RadarCellTracker.Cell> detected = new ArrayList<>();
        for (RadarCellTracker.Cell cell : RadarCellTracker.detect(view, window)) {
            if (aoi == null || aoi.covers(ai.foresignal.source.spi.Geo.factory()
                    .createPoint(new Coordinate(cell.lon(), cell.lat())))) {
                detected.add(cell);
            }
        }
        Map<Integer, RadarCellTracker.Track> matches = RadarCellTracker.match(detected, tracks.values(), validTime);
        FrameView mesh = meshView(ctx, view, validTime);
        FlashSink flashes = flashes(ctx);
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < detected.size(); i++) {
            RadarCellTracker.Cell cell = detected.get(i);
            RadarCellTracker.Track track = matches.get(i);
            if (track == null) {
                String id = CellIds.cellId(validTime);
                track = new RadarCellTracker.Track(id, validTime, validTime, cell.lon(), cell.lat());
                tracks.put(id, track);
            } else {
                track.add(validTime, cell.lon(), cell.lat());
            }
            seen.add(track.id());
            Geometry geometry = MaskPolygons.of(view.grid(), cell.pixels(), QUANTUM_DEG);
            ctx.emit(NormalizedRecord.builder(track.id(), KIND)
                    .observedAt(validTime)
                    .eventTime(track.firstSeen())
                    .geometry(geometry)
                    .putAll(payload(cell, track, mesh, flashes, geometry, validTime))
                    .build());
        }
        for (String id : new ArrayList<>(tracks.keySet())) {
            if (!seen.contains(id)) {
                tracks.remove(id);
                ctx.tombstone(KIND, id);
            }
        }
        lastFrame = validTime;
    }

    /** A cell's payload. */
    static Map<String, Object> payload(RadarCellTracker.Cell cell, RadarCellTracker.Track track, FrameView mesh,
            FlashSink flashes, Geometry geometry, Instant validTime) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("maxDbz", WxRound.dbz(cell.maxDbz()));
        p.put("areaKm2", WxRound.sig2(cell.areaKm2()));
        if (mesh != null) {
            double max = Double.NaN;
            for (int pixel : cell.pixels()) {
                double v = mesh.value(pixel, 0);
                if (!Double.isNaN(v) && (Double.isNaN(max) || v > max)) {
                    max = v;
                }
            }
            if (!Double.isNaN(max)) {
                p.put("meshMax", WxRound.whole(Math.max(0, max)));
            }
        }
        int count = 0;
        if (flashes != null && geometry != null) {
            count = flashes.countWithin(geometry, validTime.minus(Duration.ofMinutes(5)));
        }
        p.put("glmFlashes5m", bucket(count));
        double[] motion = RadarCellTracker.motion(track);
        if (motion != null) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("speedKmh", WxRound.speedKmh(motion[0]));
            m.put("bearingDeg", WxRound.bearing10(motion[1]));
            m.put("method", "centroid-v1");
            p.put("motion", m);
        }
        p.put("automated", true);
        p.put("notAWarning", true);
        return p;
    }

    /** The bucket lower bound of a flash count. */
    static int bucket(int count) {
        int out = 0;
        for (int b : FLASH_BUCKETS) {
            if (count >= b) {
                out = b;
            }
        }
        return out;
    }

    /** The current MESH frame on the same grid, or null. */
    private static FrameView meshView(DerivedContext ctx, FrameView reflectivity, Instant validTime) {
        WxInput in = WxInput.observation(ctx, MESH, MAX_GAP, ctx.now());
        if (in.frame() == null || Duration.between(in.frame().validTime(), validTime).abs().compareTo(MAX_GAP) > 0) {
            return null;
        }
        Optional<FrameView> view = ctx.rasters().view(in.frame());
        if (view.isEmpty() || view.get().nx() != reflectivity.nx() || view.get().ny() != reflectivity.ny()) {
            return null;
        }
        return view.get();
    }

    private static FlashSink flashes(DerivedContext ctx) {
        try {
            return ctx.rasters().flashes();
        } catch (UnsupportedOperationException e) {
            return null;
        }
    }

    /** The current tracks (tests). */
    Map<String, RadarCellTracker.Track> tracks() {
        return tracks;
    }
}
