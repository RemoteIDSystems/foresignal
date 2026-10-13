package ai.foresignal.raster.lightning;

import ai.foresignal.ingest.IngestStateReset;
import ai.foresignal.place.PlacesAoiChangedEvent;
import ai.foresignal.place.PlacesAoiService;
import ai.foresignal.raster.RasterPaths;
import ai.foresignal.raster.RasterProfile;
import ai.foresignal.raster.RasterSettings;
import ai.foresignal.source.spi.FlashBatch;
import ai.foresignal.source.spi.FlashSink;
import ai.foresignal.source.spi.Geo;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * The GLM flash store behind {@code ctx.rasters().flashes()} (prompt 048, Weather-Imagery-Design §2.4,
 * §2.6, §7.2): a compact daily {@link FlashArchive} of every quality-0 flash in the field of view, and
 * an in-memory {@link FlashRing} of the last 60 minutes inside the ring's scope.
 *
 * <p><b>Scope</b> ({@code foresignal.raster.glm-ring-scope}): {@code aoi} (default) keeps the flashes
 * inside the places-AOI geometry ({@link PlacesAoiService#aoi()}, the union of place buffers);
 * {@code conus} keeps the conterminous US box, and only the large profile may choose it (the small
 * profile logs a warning and stays on {@code aoi}). When the AOI changes ({@link PlacesAoiChangedEvent})
 * the ring is rebuilt from the archive with the new geometry.
 *
 * <p><b>Startup:</b> on {@link ApplicationReadyEvent} the ring is rebuilt from the tail of today's
 * archive and, within the first hour after UTC midnight, yesterday's.
 *
 * <p><b>Read API</b> (for 052 {@code wx-places} and 056): {@link #flashesWithin}, {@link #countWithin},
 * {@link #nearestKm} and {@link #newestFlashFileTime()}. They answer from the ring, under a read lock.
 *
 * <p><b>Retention:</b> {@code RasterRetentionStep} deletes archive files older than
 * {@value #ARCHIVE_DAYS} days ({@link #deleteArchivesBefore}); {@link #archiveBytes()} counts toward the
 * raster disk quota.
 */
@Component
public class FlashStore implements FlashSink, IngestStateReset {

    private static final Logger log = LoggerFactory.getLogger(FlashStore.class);

    /** Days of archive kept (Weather-Imagery-Design §2.4). */
    public static final int ARCHIVE_DAYS = 14;

    /** The ring scope property. */
    public static final String SCOPE_PROPERTY = "foresignal.raster.glm-ring-scope";

    /** The conterminous US box of {@code glm-ring-scope=conus}: {@code [minLon, minLat, maxLon, maxLat]}. */
    public static final double[] CONUS = {-125.0, 24.0, -66.5, 50.0};

    /** The AOI used when no places service is wired: the Texas box of {@code weather.defaultAoi}. */
    static final double[] DEFAULT_ENVELOPE = {-106.65, 25.84, -93.51, 36.50};

    /** The two ring scopes. */
    public enum Scope {

        /** The places-AOI geometry. */
        AOI,

        /** The conterminous US box (large profile only). */
        CONUS
    }

    private final FlashArchive archive;
    private final FlashRing ring = new FlashRing();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final PlacesAoiService aoi;
    private final Scope scope;
    private final Clock clock;
    private final GeometryFactory factory = Geo.factory();

    private volatile Instant newestFileTime;
    private long aoiVersion = Long.MIN_VALUE;
    private double[] envelope = DEFAULT_ENVELOPE.clone();
    private PreparedGeometry prepared;

    /**
     * Creates the store for Spring.
     *
     * @param paths the raster layout ({@code rasters/glm})
     * @param settings the resolved raster settings (for the profile)
     * @param aoi the places AOI, when available
     * @param scope the value of {@value #SCOPE_PROPERTY}
     * @param clock the clock
     */
    @Autowired
    public FlashStore(RasterPaths paths, RasterSettings settings, ObjectProvider<PlacesAoiService> aoi,
            @Value("${" + SCOPE_PROPERTY + ":aoi}") String scope, Clock clock) {
        this(new FlashArchive(paths.root().resolve("glm")), aoi == null ? null : aoi.getIfAvailable(),
                resolveScope(scope, settings == null ? RasterProfile.SMALL : settings.profile()), clock);
    }

    /**
     * Creates a store (tests).
     *
     * @param archive the archive
     * @param aoi the places AOI, or null for the default Texas box
     * @param scope the ring scope
     * @param clock the clock
     */
    public FlashStore(FlashArchive archive, PlacesAoiService aoi, Scope scope, Clock clock) {
        this.archive = archive;
        this.aoi = aoi;
        this.scope = scope == null ? Scope.AOI : scope;
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    /**
     * The scope named {@code value} for {@code profile}: {@code conus} only in the large profile.
     *
     * @throws IllegalArgumentException for a value other than {@code aoi} or {@code conus}
     */
    static Scope resolveScope(String value, RasterProfile profile) {
        String wanted = value == null || value.isBlank() ? "aoi" : value.strip().toLowerCase(Locale.ROOT);
        switch (wanted) {
            case "aoi" -> {
                return Scope.AOI;
            }
            case "conus" -> {
                if (profile == RasterProfile.LARGE) {
                    return Scope.CONUS;
                }
                log.warn("{}=conus needs the large profile (FORESIGNAL_PROFILE=large); the GLM ring stays on the"
                        + " places AOI", SCOPE_PROPERTY);
                return Scope.AOI;
            }
            default -> throw new IllegalArgumentException(SCOPE_PROPERTY + " must be aoi or conus, was '" + value + "'");
        }
    }

    /** The ring scope in force. */
    public Scope scope() {
        return scope;
    }

    /** The archive. */
    public FlashArchive archive() {
        return archive;
    }

    /** Rebuilds the ring from the archive once the application is ready. */
    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        rebuild();
    }

    /** Rebuilds the ring from the archive with the new AOI. */
    @EventListener
    public void onAoiChanged(PlacesAoiChangedEvent event) {
        if (scope == Scope.AOI) {
            rebuild();
        }
    }

    /**
     * Rebuilds the ring from the tail of today's archive (and yesterday's, within the first hour after
     * midnight) of every satellite, keeping the last 60 minutes inside the current scope.
     */
    public void rebuild() {
        Instant now = clock.instant();
        long toMs = now.toEpochMilli();
        long fromMs = now.minus(Duration.ofMinutes(FlashRing.WINDOW_MINUTES)).toEpochMilli();
        LocalDate today = LocalDate.ofInstant(now, ZoneOffset.UTC);
        LocalDate fromDay = LocalDate.ofInstant(Instant.ofEpochMilli(fromMs), ZoneOffset.UTC);
        lock.writeLock().lock();
        try {
            refreshAoi(true);
            ring.clear();
            int kept = 0;
            for (String satellite : archive.satellites()) {
                for (LocalDate day = fromDay; !day.isAfter(today); day = day.plusDays(1)) {
                    FlashBatch tail = archive.read(satellite, day, fromMs, toMs);
                    kept += addInScope(tail);
                }
            }
            log.info("GLM ring rebuilt from the archive: {} flash(es) in the {} scope", kept,
                    scope.name().toLowerCase(Locale.ROOT));
        } catch (RuntimeException e) {
            log.warn("Could not rebuild the GLM ring from the archive", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Forgets the in-memory state: the ring, the newest file time and the loaded AOI. The test database
     * cleaner calls it after truncating tables ({@link IngestStateReset}); the archive on disk is kept.
     */
    @Override
    public void resetState() {
        lock.writeLock().lock();
        try {
            ring.clear();
            newestFileTime = null;
            aoiVersion = Long.MIN_VALUE;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void append(String satellite, FlashBatch flashes, Instant fileTime) {
        lock.writeLock().lock();
        try {
            if (refreshAoi(false)) {
                // The AOI moved: the ring was filtered with the old geometry (the lock is reentrant).
                rebuild();
            }
            archive.append(satellite, flashes);
            addInScope(flashes);
            if (fileTime != null && (newestFileTime == null || fileTime.isAfter(newestFileTime))) {
                newestFileTime = fileTime;
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /** Adds the flashes inside the scope to the ring; returns how many were kept. Caller holds the write lock. */
    private int addInScope(FlashBatch flashes) {
        int kept = 0;
        for (int i = 0; i < flashes.size(); i++) {
            float lat = flashes.lat()[i];
            float lon = flashes.lon()[i];
            if (inScope(lon, lat) && ring.add(flashes.timesMs()[i], lat, lon, flashes.energyFj()[i])) {
                kept++;
            }
        }
        return kept;
    }

    /** True when {@code (lon, lat)} is inside the ring's scope. Caller holds a lock. */
    boolean inScope(double lon, double lat) {
        if (Double.isNaN(lon) || Double.isNaN(lat)) {
            return false;
        }
        if (scope == Scope.CONUS) {
            return lon >= CONUS[0] && lon <= CONUS[2] && lat >= CONUS[1] && lat <= CONUS[3];
        }
        if (lon < envelope[0] || lon > envelope[2] || lat < envelope[1] || lat > envelope[3]) {
            return false;
        }
        return prepared == null || prepared.covers(factory.createPoint(new Coordinate(lon, lat)));
    }

    /**
     * Picks up a new AOI; returns true when it changed after the first load. Caller holds the write lock.
     *
     * @param force reload even when the version is unchanged
     */
    private boolean refreshAoi(boolean force) {
        if (aoi == null) {
            if (prepared != null || force) {
                envelope = DEFAULT_ENVELOPE.clone();
                prepared = null;
            }
            return false;
        }
        long version = aoi.version();
        if (!force && version == aoiVersion) {
            return false;
        }
        boolean changed = aoiVersion != Long.MIN_VALUE && version != aoiVersion;
        aoiVersion = version;
        Geometry geometry = aoi.aoi();
        if (geometry == null || geometry.isEmpty()) {
            envelope = aoi.envelope();
            prepared = null;
        } else {
            Envelope e = geometry.getEnvelopeInternal();
            envelope = new double[] {e.getMinX(), e.getMinY(), e.getMaxX(), e.getMaxY()};
            prepared = PreparedGeometryFactory.prepare(geometry);
        }
        return changed;
    }

    @Override
    public FlashBatch between(Instant from, Instant to) {
        lock.readLock().lock();
        try {
            return ring.between(from.toEpochMilli(), to.toEpochMilli());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public FlashBatch flashesWithin(Point point, double radiusKm, Instant since) {
        double lat0 = point.getY();
        double lon0 = point.getX();
        FlashArchive.Growable out = new FlashArchive.Growable(64);
        lock.readLock().lock();
        try {
            ring.forEach(since.toEpochMilli(), Long.MAX_VALUE, (t, lat, lon, e) -> {
                if (Geo.haversineKm(lat0, lon0, lat, lon) <= radiusKm) {
                    out.add(t, lat, lon, e);
                }
            });
        } finally {
            lock.readLock().unlock();
        }
        return out.toBatch();
    }

    @Override
    public int countWithin(Geometry geometry, Instant since) {
        if (geometry == null || geometry.isEmpty()) {
            return 0;
        }
        PreparedGeometry area = PreparedGeometryFactory.prepare(geometry);
        Envelope box = geometry.getEnvelopeInternal();
        int[] count = new int[1];
        lock.readLock().lock();
        try {
            ring.forEach(since.toEpochMilli(), Long.MAX_VALUE, (t, lat, lon, e) -> {
                if (box.contains(lon, lat) && area.covers(factory.createPoint(new Coordinate(lon, lat)))) {
                    count[0]++;
                }
            });
        } finally {
            lock.readLock().unlock();
        }
        return count[0];
    }

    @Override
    public OptionalDouble nearestKm(Point point, Instant since) {
        double lat0 = point.getY();
        double lon0 = point.getX();
        double[] best = {Double.POSITIVE_INFINITY};
        lock.readLock().lock();
        try {
            ring.forEach(since.toEpochMilli(), Long.MAX_VALUE, (t, lat, lon, e) -> {
                double d = Geo.haversineKm(lat0, lon0, lat, lon);
                if (d < best[0]) {
                    best[0] = d;
                }
            });
        } finally {
            lock.readLock().unlock();
        }
        return Double.isInfinite(best[0]) ? OptionalDouble.empty() : OptionalDouble.of(best[0]);
    }

    @Override
    public Optional<Instant> newestFlashFileTime() {
        return Optional.ofNullable(newestFileTime);
    }

    /** The flashes in the ring now ({@code /v1/system} {@code rasters.glm.ringFlashes}). */
    public long ringFlashes() {
        lock.readLock().lock();
        try {
            return ring.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    /** The archive's bytes on disk ({@code rasters.glm.archiveBytes}); they count toward the raster quota. */
    public long archiveBytes() {
        return archive.totalBytes();
    }

    /**
     * Deletes archive files of days before {@code cutoff}'s UTC day.
     *
     * @return the bytes deleted
     */
    public long deleteArchivesBefore(Instant cutoff) {
        lock.writeLock().lock();
        try {
            return archive.deleteBefore(cutoff);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * A view for probes: reads answer from this store, {@link #append} writes nothing.
     */
    public FlashSink probingView() {
        FlashStore store = this;
        return new FlashSink() {
            @Override
            public void append(String satellite, FlashBatch flashes, Instant fileTime) {
                // A probe writes nothing.
            }

            @Override
            public FlashBatch between(Instant from, Instant to) {
                return store.between(from, to);
            }

            @Override
            public FlashBatch flashesWithin(Point point, double radiusKm, Instant since) {
                return store.flashesWithin(point, radiusKm, since);
            }

            @Override
            public int countWithin(Geometry geometry, Instant since) {
                return store.countWithin(geometry, since);
            }

            @Override
            public OptionalDouble nearestKm(Point point, Instant since) {
                return store.nearestKm(point, since);
            }

            @Override
            public Optional<Instant> newestFlashFileTime() {
                return store.newestFlashFileTime();
            }

            @Override
            public boolean probing() {
                return true;
            }
        };
    }
}
