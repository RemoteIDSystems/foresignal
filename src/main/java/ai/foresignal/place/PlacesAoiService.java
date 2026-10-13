package ai.foresignal.place;

import ai.foresignal.collect.HealthRule;
import ai.foresignal.collect.HealthRuleContext;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.spi.AreaOfInterest;
import ai.foresignal.collect.HealthVerdict;
import ai.foresignal.ingest.IngestStateReset;
import ai.foresignal.trigger.Antimeridian;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.util.GeometryFixer;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * The places area of interest (prompt 036; Weather-Imagery-Design §5.1): what weather sources (047,
 * 048, 049, 052) and render warming (045) limit themselves to.
 *
 * <ul>
 *   <li>{@link #aoi()}: the union over places of each place buffered to at least
 *       {@code max(radiusKm, weather.aoiBufferKm)} (default 150 km): a point place is its point
 *       buffered by that radius, a polygon or route its area buffered by {@code aoiBufferKm}; all on
 *       the ellipsoid, fixed and split at the antimeridian.
 *   <li>{@link #envelope()}: its bounding box, for upstream filters.
 *   <li>{@link #version()}: bumped on every recomputation, published as {@link PlacesAoiChangedEvent}.
 * </ul>
 *
 * <p>With no places the AOI is {@code weather.defaultAoi} (the Texas box by default), and every
 * AOI-consuming source gets a health warning through the 006 {@link HealthRule} SPI. The AOI is
 * cached and recomputed on {@link PlaceChangedEvent}. Adapters see it through the SPI type
 * {@link AreaOfInterest} (prompt 047: {@code mrms-probsevere} and {@code awc-metars}).
 *
 * <p>Prompt 056: {@code weather.defaultAoi} and {@code weather.aoiBufferKm} are app settings
 * ({@code PUT /v1/settings/weather}); until one is stored they come from {@link WeatherAoiProperties}. Changing
 * either recomputes the AOI ({@link ai.foresignal.weather.WeatherSettingsChangedEvent}).
 */
@Service
public class PlacesAoiService implements HealthRule, IngestStateReset, AreaOfInterest {

    private static final GeometryFactory WGS84 = new GeometryFactory(new PrecisionModel(), 4326);

    static final String UNION_SQL = """
            SELECT ST_AsBinary(ST_Union(ST_Buffer(
                     (CASE WHEN shape = 'point' THEN geom ELSE area END)::geography,
                     (CASE WHEN shape = 'point' THEN GREATEST(COALESCE(radius_km, 0), ?) ELSE ? END) * 1000.0
                   )::geometry)) AS aoi, count(*) AS n
              FROM place""";

    /**
     * One computed AOI.
     *
     * @param version the version
     * @param geometry the AOI (a MultiPolygon; the default box when there are no places)
     * @param envelope {@code [minLon, minLat, maxLon, maxLat]}
     * @param placeCount the places it covers
     * @param bufferKm {@code weather.aoiBufferKm}
     * @param isDefault true when there are no places
     */
    public record Aoi(long version, MultiPolygon geometry, double[] envelope, int placeCount, double bufferKm,
            boolean isDefault) {}

    private final JdbcTemplate jdbc;
    private final WeatherAoiProperties properties;
    private final ApplicationEventPublisher events;
    private final AtomicLong versions = new AtomicLong();
    private final Set<String> consumers = ConcurrentHashMap.newKeySet();
    private volatile Aoi cached;

    /** The stored weather settings (prompt 056); null in hand-built unit tests, which use the properties. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private ai.foresignal.weather.WeatherSettingsService weatherSettings;

    /** Creates the service. */
    public PlacesAoiService(JdbcTemplate jdbc, WeatherAoiProperties properties, ApplicationEventPublisher events) {
        this.jdbc = jdbc;
        this.properties = properties == null ? WeatherAoiProperties.defaults() : properties;
        this.events = events;
        this.consumers.addAll(this.properties.aoiSources());
    }

    /** The current AOI, computed on first use after a change. */
    public Aoi current() {
        Aoi aoi = cached;
        if (aoi == null) {
            aoi = compute();
            cached = aoi;
        }
        return aoi;
    }

    /** The AOI geometry. */
    public MultiPolygon aoi() {
        return current().geometry();
    }

    /** The AOI's {@code [minLon, minLat, maxLon, maxLat]}. */
    public double[] envelope() {
        return current().envelope().clone();
    }

    /** The AOI version. */
    public long version() {
        return current().version();
    }

    /** Registers {@code sourceId} as an AOI consumer (it gets the default-AOI health warning). */
    public void registerConsumer(String sourceId) {
        consumers.add(sourceId);
    }

    /** True when {@code sourceId} consumes the AOI. */
    public boolean consumes(String sourceId) {
        return consumers.contains(sourceId);
    }

    /** Recomputes after a place changed and publishes {@link PlacesAoiChangedEvent}. */
    @EventListener
    public void onPlaceChanged(PlaceChangedEvent event) {
        Aoi aoi = compute();
        cached = aoi;
        events.publishEvent(new PlacesAoiChangedEvent(aoi.version()));
    }

    /**
     * Recomputes after the weather settings changed {@code defaultAoi} or {@code aoiBufferKm} (prompt 056) and
     * publishes {@link PlacesAoiChangedEvent}.
     */
    @EventListener
    public void onWeatherSettingsChanged(ai.foresignal.weather.WeatherSettingsChangedEvent event) {
        if (!event.aoiChanged()) {
            return;
        }
        Aoi aoi = compute();
        cached = aoi;
        events.publishEvent(new PlacesAoiChangedEvent(aoi.version()));
    }

    /** The effective {@code weather.aoiBufferKm}: the stored setting, else the property. */
    double bufferKm() {
        ai.foresignal.weather.WeatherSettings stored = weatherSettings == null ? null : weatherSettings.get();
        return stored == null ? properties.aoiBufferKm() : stored.aoiBufferKm();
    }

    /** The effective {@code weather.defaultAoi} text: the stored setting, else the property. */
    String defaultAoi() {
        ai.foresignal.weather.WeatherSettings stored = weatherSettings == null ? null : weatherSettings.get();
        return stored == null ? properties.defaultAoi() : stored.defaultAoi();
    }

    /** The effective default box {@code [minLon, minLat, maxLon, maxLat]}. */
    double[] defaultBox() {
        ai.foresignal.weather.WeatherSettings stored = weatherSettings == null ? null : weatherSettings.get();
        return stored == null ? properties.defaultBox() : stored.defaultBox();
    }

    /** Forgets the cached AOI (the test cleaner truncated the places). */
    @Override
    public void resetState() {
        cached = null;
    }

    /** Computes the AOI from the database. */
    Aoi compute() {
        double buffer = bufferKm();
        long version = versions.incrementAndGet();
        Object[] row = jdbc.queryForObject(UNION_SQL, (rs, i) -> new Object[] {rs.getBytes("aoi"), rs.getLong("n")},
                buffer, buffer);
        long count = row == null ? 0 : (Long) row[1];
        byte[] wkb = row == null ? null : (byte[]) row[0];
        if (count == 0 || wkb == null) {
            double[] box = defaultBox();
            MultiPolygon geometry = Antimeridian.toMulti(WGS84.toGeometry(new Envelope(box[0], box[2], box[1], box[3])));
            return new Aoi(version, geometry, box, 0, buffer, true);
        }
        MultiPolygon geometry = Antimeridian.split(GeometryFixer.fix(read(wkb)));
        Envelope e = geometry.getEnvelopeInternal();
        return new Aoi(version, geometry, new double[] {e.getMinX(), e.getMinY(), e.getMaxX(), e.getMaxY()},
                (int) count, buffer, false);
    }

    @Override
    public Optional<HealthVerdict> evaluate(HealthRuleContext context) {
        if (!consumes(context.sourceId())) {
            return Optional.empty();
        }
        Aoi aoi = current();
        if (!aoi.isDefault()) {
            return Optional.of(HealthVerdict.healthy(null));
        }
        return Optional.of(new HealthVerdict(HealthStatus.DEGRADED,
                "No places are defined, so the default area of interest (" + defaultAoi()
                        + ") is used; add places with POST /v1/places", null));
    }

    private static Geometry read(byte[] wkb) {
        try {
            Geometry g = new WKBReader(WGS84).read(wkb);
            g.setSRID(4326);
            return g;
        } catch (ParseException e) {
            throw new IllegalStateException("PostGIS returned unreadable WKB: " + e.getMessage(), e);
        }
    }

    /** The default box as a polygon, for tests. */
    static Geometry box(double[] b) {
        return WGS84.createPolygon(new Coordinate[] {new Coordinate(b[0], b[1]), new Coordinate(b[2], b[1]),
            new Coordinate(b[2], b[3]), new Coordinate(b[0], b[3]), new Coordinate(b[0], b[1])});
    }

    /** The consumers, for the health view and tests. */
    public List<String> consumers() {
        return consumers.stream().sorted().toList();
    }
}
