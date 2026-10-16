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
import ai.foresignal.source.spi.FlashBatch;
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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code wx-storm-tracks} (prompt 105; Weather-Imagery-Design §7.2): TITAN-lite storm tracking as
 * {@code storm-track} records (externalId {@code t-{ulid}}), plus the {@code storm-swath} union along
 * each track.
 *
 * <p>Per MRMS reflectivity frame, inside the places AOI (CONUS opt-in on the large profile), cells
 * are detected by 052's segmentation and associated to active tracks with {@link TitanLiteTracker}
 * (IoU of advected polygons, Hungarian assignment, merges and splits). Each track carries its current
 * cell summary, a 12-position history, motion ({@value TitanLiteTracker#VERSION}, least squares over the
 * newest five positions once three or more exist, else {@code centroid-v1}), a max-dBZ trend, a GLM
 * lightning-jump flag ({@link LightningJump}) and the +15/+30/+60-minute nowcast polygons. It emits only
 * when a quantized field or the geometry changes: the {@code history} ring (whose timestamps move every
 * frame) is left out of that comparison and declared volatile. It is tombstoned 30 minutes after it ends.
 * A {@code storm-swath} record (externalId = the track id) is written every 10 minutes while active and
 * when the track ends, whether by a merge or by missed frames. {@code reconcile} restores each track's
 * history, motion, links and stored swath, so a restart extends the stored union rather than replacing it.
 */
@Component
public class WxStormTracksGenerator implements DerivedGenerator {

    /** The source id. */
    public static final String ID = "wx-storm-tracks";

    /** The {@code storm-track} kind. */
    public static final String TRACK_KIND = "storm-track";

    /** The {@code storm-swath} kind. */
    public static final String SWATH_KIND = "storm-swath";

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

    /** The swath rewrite interval. */
    public static final Duration SWATH_INTERVAL = Duration.ofMinutes(10);

    /** How long after a track ends its records are tombstoned. */
    public static final Duration TOMBSTONE_AFTER = Duration.ofMinutes(30);

    /** The nowcast leads, minutes. */
    public static final int[] NOWCAST_LEADS = {15, 30, 60};

    /** The history ring size. */
    public static final int HISTORY = 12;

    /** The positions used for the least-squares motion. */
    public static final int MOTION_POSITIONS = 5;

    /** The GLM polygon buffer, degrees (about 5 km). */
    public static final double FLASH_BUFFER_DEG = 0.045;

    /** The lightning jump's advisory lifetime. */
    public static final Duration JUMP_LIFETIME = Duration.ofMinutes(10);

    /**
     * The payload key left out of a track's significant content (and declared volatile): the position
     * history, whose timestamps change every frame.
     */
    static final String VOLATILE_HISTORY = "history";

    private static final GeometryFactory GEOMETRY = new GeometryFactory();

    private final boolean large;
    private final SourceDescriptor descriptor = buildDescriptor();
    private final Map<String, Track> tracks = new LinkedHashMap<>();
    private Instant lastFrame;

    /**
     * Creates the generator.
     *
     * @param profile {@code foresignal.profile}: {@code scope=conus} is honored only on {@code large}
     */
    @Autowired
    public WxStormTracksGenerator(@Value("${foresignal.profile:small}") String profile) {
        this.large = RasterProfile.parse(profile) == RasterProfile.LARGE;
    }

    /** Creates a generator for tests (small profile). */
    WxStormTracksGenerator() {
        this("small");
    }

    /** One tracked storm. */
    static final class Track {

        final String id;
        final Instant firstSeen;
        Instant lastSeen;
        Geometry geometry;
        double lon;
        double lat;
        double maxDbz;
        double areaKm2;
        Double lastMesh;
        double speedKmh;
        double bearingDeg;
        String motionMethod;
        int missed;
        boolean ended;
        String mergedInto;
        String parentTrackId;
        String trend = "steady";
        Double lastEmittedMaxDbz;
        double flashRate;
        Map<String, Object> lightningJump;
        Instant jumpUntil;
        Geometry swath;
        Instant swathAt;
        Geometry nowcast;
        /** The canonical significant content (payload without {@code history}, plus geometry) last emitted. */
        String lastSignificant;
        /** The last {@value #HISTORY} positions {@code {epochMs, lon, lat}}, oldest first. */
        final ArrayDeque<double[]> positions = new ArrayDeque<>();

        Track(String id, Instant firstSeen, Instant time, double lon, double lat, Geometry geometry) {
            this.id = id;
            this.firstSeen = firstSeen;
            this.lastSeen = time;
            this.lon = lon;
            this.lat = lat;
            this.geometry = geometry;
            add(time, lon, lat);
        }

        void add(Instant time, double lon, double lat) {
            positions.addLast(new double[] {time.toEpochMilli(), lon, lat});
            while (positions.size() > HISTORY) {
                positions.removeFirst();
            }
        }

        /** The newest {@value #MOTION_POSITIONS} positions, oldest first: the least-squares motion window. */
        List<double[]> motionWindow() {
            List<double[]> all = new ArrayList<>(positions);
            return all.subList(Math.max(0, all.size() - MOTION_POSITIONS), all.size());
        }

        /** The last position {@code {epochMs, lon, lat}}. */
        double[] last() {
            return positions.getLast();
        }
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** The descriptor. */
    static SourceDescriptor buildDescriptor() {
        return SourceDescriptor.builder(ID)
                .name("Storm tracks (derived)")
                .description("TITAN-lite storm tracking: storm-track records with motion, trend, nowcast and a GLM"
                        + " lightning-jump flag, and storm-swath unions, from MRMS reflectivity inside the places AOI."
                        + " Automated, not a warning.")
                .kind(TRACK_KIND)
                .kind(SWATH_KIND)
                .attribution("Derived by Foresignal from NOAA MRMS and GOES-19 GLM")
                .licence("derived (see inputs)")
                .mode(Mode.DERIVED)
                .defaultBudget(Budget.of(1, 1))
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .recordTtl(null)
                .zeroResultsNormal(true)
                .defaultRetention(Duration.ofDays(3))
                .kindRetention(TRACK_KIND, Duration.ofDays(3))
                .kindRetention(SWATH_KIND, Duration.ofDays(3))
                .volatileFields(VOLATILE_HISTORY)
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
        Map<String, HeadSnapshot> swaths = new HashMap<>();
        for (HeadSnapshot head : ownHeads) {
            if (!head.tombstone() && SWATH_KIND.equals(head.kind()) && head.geometry() != null
                    && !head.geometry().isEmpty()) {
                swaths.put(head.externalId(), head);
            }
        }
        for (HeadSnapshot head : ownHeads) {
            if (head.tombstone() || !TRACK_KIND.equals(head.kind()) || head.geometry() == null
                    || head.geometry().isEmpty()) {
                continue;
            }
            Track track = restore(head, swaths.get(head.externalId()), ctx.now());
            tracks.put(track.id, track);
            if (lastFrame == null || track.lastSeen.isAfter(lastFrame)) {
                lastFrame = track.lastSeen;
            }
        }
    }

    /**
     * Rebuilds a track from its stored head (and its swath head, when there is one): the current polygon, the
     * position history (so least-squares motion continues), motion, trend, links, MESH, lightning jump and the
     * stored swath union, which later frames extend rather than replace.
     */
    static Track restore(HeadSnapshot head, HeadSnapshot swath, Instant now) {
        Map<String, Object> payload = head.payload();
        Geometry current = head.geometry() instanceof GeometryCollection gc && gc.getNumGeometries() > 0
                ? gc.getGeometryN(0) : head.geometry();
        Point c = current.getCentroid();
        Instant at = head.observedAt() == null ? now : head.observedAt();
        List<double[]> history = new ArrayList<>();
        if (payload.get(VOLATILE_HISTORY) instanceof List<?> list) {
            for (Object o : list) {
                if (o instanceof Map<?, ?> m && m.get("lon") instanceof Number lon && m.get("lat") instanceof Number lat) {
                    Instant t = parseInstant(m.get("time"));
                    if (t != null) {
                        history.add(new double[] {t.toEpochMilli(), lon.doubleValue(), lat.doubleValue()});
                    }
                }
            }
        }
        Instant firstSeen = history.isEmpty() ? at : Instant.ofEpochMilli((long) history.get(0)[0]);
        if (swath != null && swath.observedAt() != null && swath.payload().get("durationMin") instanceof Number d) {
            Instant fromSwath = swath.observedAt().minus(Duration.ofMinutes(d.longValue()));
            if (fromSwath.isBefore(firstSeen)) {
                firstSeen = fromSwath;
            }
        }
        Track track = new Track(head.externalId(), firstSeen, at, c.getX(), c.getY(), current);
        if (!history.isEmpty()) {
            track.positions.clear();
            history.forEach(track.positions::addLast);
            while (track.positions.size() > HISTORY) {
                track.positions.removeFirst();
            }
            double[] last = track.positions.getLast();
            if ((long) last[0] < at.toEpochMilli()) {
                track.add(at, c.getX(), c.getY());
            }
        }
        track.lastSeen = at;
        if (payload.get("cell") instanceof Map<?, ?> cell) {
            if (cell.get("maxDbz") instanceof Number n) {
                track.maxDbz = n.doubleValue();
                track.lastEmittedMaxDbz = n.doubleValue();
            }
            if (cell.get("areaKm2") instanceof Number n) {
                track.areaKm2 = n.doubleValue();
            }
            if (cell.get("meshMaxMm") instanceof Number n) {
                track.lastMesh = n.doubleValue();
            }
            if (cell.get("flashRate") instanceof Number n) {
                track.flashRate = n.doubleValue();
            }
        }
        if (payload.get("motion") instanceof Map<?, ?> motion) {
            if (motion.get("speedKmh") instanceof Number n) {
                track.speedKmh = n.doubleValue();
            }
            if (motion.get("bearingDeg") instanceof Number n) {
                track.bearingDeg = n.doubleValue();
            }
            if (motion.get("method") != null) {
                track.motionMethod = motion.get("method").toString();
            }
        }
        if (payload.get("trend") instanceof Map<?, ?> trend && trend.get("maxDbz") != null) {
            track.trend = trend.get("maxDbz").toString();
        }
        if (payload.get("lightningJump") instanceof Map<?, ?> jump) {
            Map<String, Object> copy = new LinkedHashMap<>();
            jump.forEach((k, v) -> copy.put(String.valueOf(k), v));
            track.lightningJump = copy;
            Instant jumpAt = parseInstant(jump.get("at"));
            track.jumpUntil = jumpAt == null ? at.plus(JUMP_LIFETIME) : jumpAt.plus(JUMP_LIFETIME);
        }
        track.ended = "ended".equals(payload.get("status"));
        track.mergedInto = payload.get("mergedInto") == null ? null : payload.get("mergedInto").toString();
        track.parentTrackId = payload.get("parentTrackId") == null ? null : payload.get("parentTrackId").toString();
        track.nowcast = head.geometry();
        track.lastSignificant = significant(payload, head.geometry());
        if (swath != null) {
            track.swath = swath.geometry();
            track.swathAt = swath.observedAt();
        }
        return track;
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

    @Override
    public void onTick(Instant now, DerivedContext ctx) {
        for (String id : new ArrayList<>(tracks.keySet())) {
            Track track = tracks.get(id);
            if (track.ended && Duration.between(track.lastSeen, now).compareTo(TOMBSTONE_AFTER) > 0) {
                tracks.remove(id);
                ctx.tombstone(TRACK_KIND, id);
                ctx.tombstone(SWATH_KIND, id);
            }
        }
    }

    /** Processes one reflectivity frame: detect, associate, emit and expire. */
    void process(DerivedContext ctx, FrameView view, Instant validTime) {
        boolean conus = large && "conus".equals(ctx.params().optionalString(SCOPE_PARAM).orElse("aoi"));
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
            if (aoi == null || aoi.covers(GEOMETRY.createPoint(new Coordinate(cell.lon(), cell.lat())))) {
                detected.add(cell);
            }
        }
        List<TitanLiteTracker.Cell> cells = new ArrayList<>();
        for (RadarCellTracker.Cell cell : detected) {
            Geometry geometry = MaskPolygons.of(view.grid(), cell.pixels(), QUANTUM_DEG);
            cells.add(new TitanLiteTracker.Cell(geometry == null ? GEOMETRY.createPoint(
                    new Coordinate(cell.lon(), cell.lat())) : geometry, cell.lon(), cell.lat(), cell.areaKm2(),
                    cell.maxDbz()));
        }
        List<TitanLiteTracker.Previous> previous = new ArrayList<>();
        List<Track> previousTracks = new ArrayList<>();
        for (Track track : tracks.values()) {
            if (track.ended || track.geometry == null || track.geometry.isEmpty()) {
                continue;
            }
            previous.add(new TitanLiteTracker.Previous(track.id, track.geometry, track.lon, track.lat, track.lastSeen,
                    track.speedKmh, track.bearingDeg));
            previousTracks.add(track);
        }
        TitanLiteTracker.Result result = TitanLiteTracker.associate(previous, cells, validTime);
        if (result.greedy()) {
            ctx.log().warn("wx-storm-tracks: association over {} tracks fell back to greedy", previous.size());
        }
        FrameView mesh = meshView(ctx, view, validTime);
        FlashSink flashes = flashes(ctx);
        Set<String> seen = new HashSet<>();
        Map<Integer, String> trackOfCell = new HashMap<>();
        for (TitanLiteTracker.Assignment a : result.assignments()) {
            Track track = previousTracks.get(a.previous());
            updateTrack(track, cells.get(a.cell()), detected.get(a.cell()), view, mesh, flashes, validTime, true);
            seen.add(track.id);
            trackOfCell.put(a.cell(), track.id);
        }
        for (TitanLiteTracker.Merge merge : result.merges()) {
            Track ended = previousTracks.get(merge.endedPrevious());
            Track into = previousTracks.get(merge.intoPrevious());
            ended.ended = true;
            ended.mergedInto = into.id;
            seen.add(ended.id);
            emitTrack(ctx, ended, validTime);
            writeSwath(ctx, ended, validTime, true);
        }
        for (int cell : result.unmatchedCells()) {
            String id = "t-" + CellIds.ulid(validTime);
            Geometry geometry = cells.get(cell).geometry();
            Track track = new Track(id, validTime, validTime, cells.get(cell).lon(), cells.get(cell).lat(), geometry);
            updateTrack(track, cells.get(cell), detected.get(cell), view, mesh, flashes, validTime, false);
            tracks.put(id, track);
            seen.add(id);
        }
        for (TitanLiteTracker.Split split : result.splits()) {
            Track parent = previousTracks.get(split.parentPrevious());
            String id = "t-" + CellIds.ulid(validTime);
            Geometry geometry = cells.get(split.childCell()).geometry();
            Track child = new Track(id, validTime, validTime, cells.get(split.childCell()).lon(),
                    cells.get(split.childCell()).lat(), geometry);
            child.parentTrackId = parent.id;
            updateTrack(child, cells.get(split.childCell()), detected.get(split.childCell()), view, mesh, flashes,
                    validTime, false);
            tracks.put(id, child);
            seen.add(id);
        }
        for (int prev : result.unmatchedPrevious()) {
            Track track = previousTracks.get(prev);
            track.missed++;
            if (track.missed >= TitanLiteTracker.MISSED_FRAMES) {
                track.ended = true;
                seen.add(track.id);
                emitTrack(ctx, track, validTime);
                writeSwath(ctx, track, validTime, true);
            }
        }
        for (Track track : tracks.values()) {
            if (!track.ended && seen.contains(track.id)) {
                emitTrack(ctx, track, validTime);
                writeSwath(ctx, track, validTime, false);
            }
        }
        lastFrame = validTime;
    }

    private void updateTrack(Track track, TitanLiteTracker.Cell cell, RadarCellTracker.Cell detected, FrameView view,
            FrameView mesh, FlashSink flashes, Instant validTime, boolean matched) {
        track.missed = 0;
        track.lastSeen = validTime;
        track.geometry = cell.geometry();
        track.lon = cell.lon();
        track.lat = cell.lat();
        track.areaKm2 = cell.areaKm2();
        track.add(validTime, cell.lon(), cell.lat());
        track.maxDbz = cell.maxDbz();
        double[] motion = motion(track);
        if (motion != null) {
            track.speedKmh = motion[0];
            track.bearingDeg = motion[1];
            track.motionMethod = motion[2] == 1 ? TitanLiteTracker.VERSION : "centroid-v1";
        }
        track.trend = trend(track.lastEmittedMaxDbz, track.maxDbz);
        if (track.swath == null) {
            track.swath = cell.geometry();
        } else {
            track.swath = track.swath.union(cell.geometry());
        }
        track.flashRate = flashes == null ? 0 : flashes.countWithin(cell.geometry(), validTime.minusSeconds(300)) / 5.0;
        track.nowcast = nowcastGeometry(track);
        if (flashes != null) {
            List<Instant> times = flashTimes(flashes, cell.geometry(), validTime);
            LightningJump.detect(validTime, times).ifPresent(jump -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("at", jump.at().toString());
                m.put("sigma", WxRound.sig2(jump.sigma()));
                m.put("rateFpm", WxRound.whole(jump.rateFpm()));
                m.put("method", LightningJump.METHOD);
                track.lightningJump = m;
                track.jumpUntil = validTime.plus(JUMP_LIFETIME);
            });
        }
        if (track.lightningJump != null && track.jumpUntil != null && validTime.isAfter(track.jumpUntil)) {
            track.lightningJump = null;
        }
        if (mesh != null) {
            double max = Double.NaN;
            for (int pixel : detected.pixels()) {
                double v = mesh.value(pixel, 0);
                if (!Double.isNaN(v) && (Double.isNaN(max) || v > max)) {
                    max = v;
                }
            }
            track.lastMesh = Double.isNaN(max) ? null : Math.max(0, max);
        }
    }

    private void emitTrack(DerivedContext ctx, Track track, Instant validTime) {
        Map<String, Object> payload = new LinkedHashMap<>();
        Map<String, Object> cell = new LinkedHashMap<>();
        cell.put("maxDbz", WxRound.dbz(track.maxDbz));
        cell.put("areaKm2", WxRound.sig2(track.areaKm2));
        cell.put("flashRate", WxRound.whole(track.flashRate));
        if (track.lastMesh != null) {
            cell.put("meshMaxMm", WxRound.whole(track.lastMesh));
        }
        payload.put("cell", cell);
        List<Map<String, Object>> history = new ArrayList<>();
        double[] positions = track.positions.stream().flatMapToDouble(p -> Arrays.stream(p)).toArray();
        int count = positions.length / 3;
        for (int i = Math.max(0, count - HISTORY); i < count; i++) {
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("time", Instant.ofEpochMilli((long) positions[i * 3]).toString());
            p.put("lon", Math.round(positions[i * 3 + 1] * 1e4) / 1e4);
            p.put("lat", Math.round(positions[i * 3 + 2] * 1e4) / 1e4);
            history.add(p);
        }
        payload.put(VOLATILE_HISTORY, history);
        if (track.motionMethod != null) {
            Map<String, Object> motion = new LinkedHashMap<>();
            motion.put("speedKmh", WxRound.speedKmh(track.speedKmh));
            motion.put("bearingDeg", WxRound.bearing10(track.bearingDeg));
            motion.put("method", track.motionMethod);
            payload.put("motion", motion);
        }
        Map<String, Object> trend = new LinkedHashMap<>();
        trend.put("maxDbz", track.trend);
        payload.put("trend", trend);
        if (track.lightningJump != null) {
            payload.put("lightningJump", track.lightningJump);
        }
        List<Map<String, Object>> nowcast = new ArrayList<>();
        for (int lead : NOWCAST_LEADS) {
            Map<String, Object> n = new LinkedHashMap<>();
            n.put("leadMinutes", lead);
            n.put("method", "linear-advection");
            nowcast.add(n);
        }
        payload.put("nowcast", nowcast);
        payload.put("status", track.ended ? "ended" : "active");
        if (track.mergedInto != null) {
            payload.put("mergedInto", track.mergedInto);
        }
        if (track.parentTrackId != null) {
            payload.put("parentTrackId", track.parentTrackId);
        }
        Map<String, Object> detector = new LinkedHashMap<>();
        detector.put("name", "titan-lite");
        detector.put("version", TitanLiteTracker.VERSION);
        payload.put("detector", detector);
        payload.put("automated", true);
        payload.put("notAWarning", true);
        Geometry geometry = nowcastGeometry(track);
        String significant = significant(payload, geometry);
        if (significant.equals(track.lastSignificant)) {
            // Only the history moved: no version (prompt 105: emit on change of quantized fields).
            return;
        }
        track.lastSignificant = significant;
        track.lastEmittedMaxDbz = track.maxDbz;
        ctx.emit(NormalizedRecord.builder(track.id, TRACK_KIND)
                .observedAt(validTime)
                .eventTime(track.firstSeen)
                .geometry(geometry)
                .putAll(payload)
                .build());
    }

    /** A track's significant content: the payload without {@code history}, plus the geometry's WKT. */
    static String significant(Map<String, Object> payload, Geometry geometry) {
        Map<String, Object> copy = new LinkedHashMap<>(payload);
        copy.remove(VOLATILE_HISTORY);
        return ai.foresignal.ingest.CanonicalJson.write(copy) + "|"
                + (geometry == null ? "" : ai.foresignal.ingest.ContentHasher.wkt(geometry));
    }

    private void writeSwath(DerivedContext ctx, Track track, Instant validTime, boolean force) {
        if (track.swath == null) {
            return;
        }
        if (!force && track.swathAt != null && Duration.between(track.swathAt, validTime).compareTo(SWATH_INTERVAL) < 0) {
            return;
        }
        track.swathAt = validTime;
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("trackId", track.id);
        if (track.lastMesh != null) {
            payload.put("meshMaxMm", WxRound.whole(track.lastMesh));
        }
        Instant until = track.ended ? track.lastSeen : validTime;
        payload.put("durationMin", WxRound.whole(Math.max(0, Duration.between(track.firstSeen, until).toMinutes())));
        payload.put("status", track.ended ? "ended" : "active");
        payload.put("automated", true);
        payload.put("notAWarning", true);
        ctx.emit(NormalizedRecord.builder(track.id, SWATH_KIND)
                .observedAt(validTime)
                .geometry(track.swath)
                .putAll(payload)
                .build());
    }

    /** A GeometryCollection of the current polygon and the nowcast polygons. */
    private static Geometry nowcastGeometry(Track track) {
        List<Geometry> parts = new ArrayList<>();
        if (track.geometry != null) {
            parts.add(track.geometry);
        }
        double[] destination = track.positions.isEmpty() ? null : track.last();
        if (destination != null) {
            for (int lead : NOWCAST_LEADS) {
                Geometry n = TitanLiteTracker.advect(track.geometry, track.lon, track.lat, track.speedKmh,
                        track.bearingDeg, Duration.ofMinutes(lead));
                if (n != null) {
                    parts.add(n);
                }
            }
        }
        if (parts.isEmpty()) {
            return null;
        }
        return GEOMETRY.createGeometryCollection(parts.toArray(Geometry[]::new));
    }

    /** The flash times inside the cell polygon buffered by 5 km in the last 14 minutes. */
    private static List<Instant> flashTimes(FlashSink flashes, Geometry geometry, Instant validTime) {
        List<Instant> times = new ArrayList<>();
        Geometry buffered = geometry.buffer(FLASH_BUFFER_DEG);
        Instant from = validTime.minus(Duration.ofMinutes(14));
        try {
            FlashBatch batch = flashes.between(from, validTime);
            for (int i = 0; i < batch.size(); i++) {
                Point p = GEOMETRY.createPoint(new Coordinate(batch.lon()[i], batch.lat()[i]));
                if (buffered.covers(p)) {
                    times.add(batch.time(i));
                }
            }
        } catch (RuntimeException e) {
            return times;
        }
        return times;
    }

    /** {@code {speedKmh, bearingDeg, method}}; method 1 for the least-squares fit, 0 for centroid-v1. */
    static double[] motion(Track track) {
        List<double[]> window = track.motionWindow();
        int n = window.size();
        if (n < 2) {
            return null;
        }
        if (n < 3) {
            return centroid(points(window));
        }
        double[] origin = track.last();
        double cos = Math.cos(Math.toRadians(origin[2]));
        double[] t = new double[n];
        double[] x = new double[n];
        double[] y = new double[n];
        int i = 0;
        for (double[] p : window) {
            t[i] = (p[0] - origin[0]) / 1000.0;
            x[i] = (p[1] - origin[1]) * 111.32 * cos;
            y[i] = (p[2] - origin[2]) * 110.57;
            i++;
        }
        double tm = Arrays.stream(t).average().orElse(0);
        double xm = Arrays.stream(x).average().orElse(0);
        double ym = Arrays.stream(y).average().orElse(0);
        double stt = 0;
        double stx = 0;
        double sty = 0;
        for (int k = 0; k < n; k++) {
            stt += (t[k] - tm) * (t[k] - tm);
            stx += (t[k] - tm) * (x[k] - xm);
            sty += (t[k] - tm) * (y[k] - ym);
        }
        if (stt <= 0) {
            return centroid(points(window));
        }
        double speed = Math.hypot(stx / stt * 3600, sty / stt * 3600);
        double bearing = (Math.toDegrees(Math.atan2(stx / stt, sty / stt)) + 360) % 360;
        return new double[] {speed, bearing, 1};
    }

    private static double[][] points(java.util.Collection<double[]> positions) {
        double[][] out = new double[positions.size()][];
        int i = 0;
        for (double[] p : positions) {
            out[i++] = p;
        }
        return out;
    }

    private static double[] centroid(double[][] points) {
        double[] a = points[points.length - 2];
        double[] b = points[points.length - 1];
        double hours = (b[0] - a[0]) / 3_600_000.0;
        if (hours <= 0) {
            return null;
        }
        double cos = Math.cos(Math.toRadians((a[2] + b[2]) / 2));
        double dx = (b[1] - a[1]) * 111.32 * cos;
        double dy = (b[2] - a[2]) * 110.57;
        double speed = Math.hypot(dx, dy) / hours;
        double bearing = (Math.toDegrees(Math.atan2(dx, dy)) + 360) % 360;
        return new double[] {speed, bearing, 0};
    }

    private static String trend(Double previous, double current) {
        if (previous == null) {
            return "steady";
        }
        double delta = current - previous;
        if (delta > 2.5) {
            return "rising";
        }
        if (delta < -2.5) {
            return "falling";
        }
        return "steady";
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
    Map<String, Track> tracks() {
        return tracks;
    }
}
