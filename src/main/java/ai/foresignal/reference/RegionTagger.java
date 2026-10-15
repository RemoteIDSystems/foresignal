package ai.foresignal.reference;

import ai.foresignal.ingest.IngestStateReset;
import ai.foresignal.trigger.RuleSqlBuilder;
import java.time.Clock;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Turns a geometry into {@link RegionTags} without any online geocoder (prompt 040).
 *
 * <p>Tags are computed for the geometry's representative point (JTS {@code getInteriorPoint}, the
 * equivalent of {@code ST_PointOnSurface}, which lies on the geometry for areas and lines):
 *
 * <ul>
 *   <li><b>Country, admin-1, US county:</b> {@code ST_Covers} on {@code ref_country},
 *       {@code ref_admin1} and {@code ref_admin2}. A point outside every country but within
 *       {@code coastal-km} (5 km) of one is given the nearest (by distance on the simplified boundary).
 *   <li><b>Sea:</b> at sea {@code iso2} is null and {@code sea} is the Natural Earth marine polygon that
 *       covers the point.
 *   <li><b>Near:</b> the KNN nearest gazetteer place ({@code geog <-> point}, metres) with
 *       {@code population ≥ near-min-population}, with the distance and the bearing from the place to
 *       the point ({@code "12 km NNE of Austin, TX"}).
 * </ul>
 *
 * <p>Results are cached in an LRU of {@code cache-size} (50,000) entries keyed by the coordinates rounded
 * to 0.01°; the tags are those of the rounded point. When a reference table is empty its members are
 * null, and without {@code ref_country} the country comes from the bundled simplified polygons
 * ({@link BundledCountryPolygons}). Tagging never fails: a database error degrades to the bundled
 * country alone.
 *
 * <p>{@link #tagRecord} is lazy tagging (prompt 040 task 4): it reuses the stored row while the record
 * is within the retag distance of where it was tagged, and otherwise tags and stores it.
 */
@Component
public class RegionTagger implements IngestStateReset {

    private static final Logger log = LoggerFactory.getLogger(RegionTagger.class);

    /** The reference tables whose presence is tracked. */
    static final List<String> TABLES = List.of("ref_country", "ref_admin1", "ref_admin2", "ref_sea",
            "gazetteer_place");

    static final String POINT = "ST_SetSRID(ST_MakePoint(CAST(? AS double precision), CAST(? AS double precision)), 4326)";

    private final JdbcTemplate jdbc;
    private final CountryCentroids countries;
    private final BundledCountryPolygons bundled;
    private final RegionTagStore store;
    private final RegionTaggingService modes;
    private final ReferenceProperties properties;
    private final Clock clock;
    private final Map<Long, RegionTags> cache;
    private final Map<String, Boolean> loaded = new ConcurrentHashMap<>();

    /** Creates the tagger. */
    public RegionTagger(JdbcTemplate jdbc, CountryCentroids countries, BundledCountryPolygons bundled,
            RegionTagStore store, @Lazy RegionTaggingService modes, ReferenceProperties properties, Clock clock) {
        this.jdbc = jdbc;
        this.countries = countries;
        this.bundled = bundled;
        this.store = store;
        this.modes = modes;
        this.properties = properties;
        this.clock = clock;
        int size = properties.cacheSize();
        this.cache = Collections.synchronizedMap(new LinkedHashMap<>(1024, 0.75f, true) {
            private static final long serialVersionUID = 1L;

            @Override
            protected boolean removeEldestEntry(Map.Entry<Long, RegionTags> eldest) {
                return size() > size;
            }
        });
    }

    // ------------------------------------------------------------------------- tagging

    /** The tags of {@code geometry}'s representative point; {@link RegionTags#EMPTY} for null or empty. */
    public RegionTags tag(Geometry geometry) {
        Point point = representative(geometry);
        return point == null ? RegionTags.EMPTY : tag(point.getX(), point.getY());
    }

    /** The tags of {@code (lon, lat)} (cached by the point rounded to 0.01°). */
    public RegionTags tag(double lon, double lat) {
        if (!Double.isFinite(lon) || !Double.isFinite(lat) || Math.abs(lon) > 180 || Math.abs(lat) > 90) {
            return RegionTags.EMPTY;
        }
        double rLon = round(lon);
        double rLat = round(lat);
        long key = cacheKey(rLon, rLat);
        RegionTags cached = cache.get(key);
        if (cached != null) {
            return cached;
        }
        RegionTags tags = compute(rLon, rLat);
        cache.put(key, tags);
        return tags;
    }

    /**
     * Lazy tagging of a record from its current live geometry; null when the source's mode is
     * {@code off} or the record has no geometry.
     */
    public RegionTags tagRecord(String sourceId, String externalId) {
        return tagRecord(sourceId, externalId, null);
    }

    /**
     * Lazy tagging of a record at {@code geometry} (its live geometry when null): the stored row while the
     * record is within the retag distance of where it was tagged, otherwise fresh tags, stored. Null when
     * the source's mode is {@code off} or there is no geometry.
     */
    public RegionTags tagRecord(String sourceId, String externalId, Geometry geometry) {
        if (modes.mode(sourceId) == RegionTagMode.OFF) {
            return null;
        }
        Geometry g = geometry != null ? geometry : liveGeometry(sourceId, externalId);
        Point point = representative(g);
        if (point == null) {
            return store.find(sourceId, externalId).map(RegionTagStore.Stored::tags).orElse(null);
        }
        try {
            Optional<RegionTagStore.Stored> stored = store.find(sourceId, externalId);
            if (stored.isPresent()
                    && distanceKm(stored.get().lon(), stored.get().lat(), point.getX(), point.getY())
                            <= retagKm(sourceId)) {
                return stored.get().tags();
            }
            RegionTags tags = tag(point.getX(), point.getY());
            store.upsert(sourceId, externalId, tags, point.getX(), point.getY(), clock.instant());
            return tags;
        } catch (DataAccessException e) {
            log.warn("Could not tag {}:{}: {}", sourceId, externalId, e.getMessage());
            return tag(point.getX(), point.getY());
        }
    }

    /** The stored tags of a record, without tagging it. */
    public Optional<RegionTags> stored(String sourceId, String externalId) {
        return store.find(sourceId, externalId).map(RegionTagStore.Stored::tags);
    }

    /** The retag distance of {@code sourceId}: {@code track-retag-km} for TRACK sources, else {@code retag-km}. */
    public double retagKm(String sourceId) {
        return modes.track(sourceId) ? properties.trackRetagKm() : properties.retagKm();
    }

    /** True when {@code table} (one of {@link #TABLES}) holds rows. */
    public boolean loaded(String table) {
        return loaded.computeIfAbsent(table, this::probe);
    }

    /** True when the country boundaries are loaded (otherwise only the bundled country is known). */
    public boolean referenceLoaded() {
        return loaded("ref_country");
    }

    // ------------------------------------------------------------------------- cache

    /** Drops the cache and the table flags after a reference load. */
    @EventListener
    public void onReferenceChanged(ReferenceDataChangedEvent event) {
        resetState();
    }

    /** Drops the cache and the table flags (also after the test cleaner truncates). */
    @Override
    public void resetState() {
        cache.clear();
        loaded.clear();
    }

    /** The number of cached points. */
    public int cacheSize() {
        return cache.size();
    }

    private boolean probe(String table) {
        try {
            return Boolean.TRUE.equals(jdbc.queryForObject("SELECT EXISTS (SELECT 1 FROM " + table + ")",
                    Boolean.class));
        } catch (DataAccessException e) {
            return false;
        }
    }

    // ------------------------------------------------------------------------- computing

    RegionTags compute(double lon, double lat) {
        String iso2 = null;
        String countryName = null;
        String admin1 = null;
        String admin1Name = null;
        String admin2 = null;
        String countyName = null;
        String sea = null;
        RegionNear near = null;
        try {
            boolean coastal = false;
            if (loaded("ref_country")) {
                String[] country = first("SELECT iso2, name FROM ref_country WHERE geom && " + POINT
                        + " AND ST_Covers(geom, " + POINT + ") LIMIT 1", lon, lat, lon, lat);
                if (country == null && properties.coastalKm() > 0) {
                    country = nearest("ref_country", "iso2, name", null, lon, lat);
                    coastal = country != null;
                }
                if (country != null) {
                    iso2 = country[0];
                    countryName = countries.name(iso2) != null ? countries.name(iso2) : country[1];
                }
            } else {
                iso2 = bundled.iso2At(lon, lat);
                countryName = iso2 == null ? null : countries.name(iso2);
            }
            if (iso2 != null && loaded("ref_admin1")) {
                String[] region = first("SELECT code, name FROM ref_admin1 WHERE geom && " + POINT
                        + " AND ST_Covers(geom, " + POINT + ") LIMIT 1", lon, lat, lon, lat);
                if (region == null && coastal) {
                    region = nearest("ref_admin1", "code, name", iso2, lon, lat);
                }
                if (region != null) {
                    admin1 = region[0];
                    admin1Name = region[1];
                }
            }
            if ("US".equals(iso2) && loaded("ref_admin2")) {
                String[] county = first("SELECT fips5, name FROM ref_admin2 WHERE geom && " + POINT
                        + " AND ST_Covers(geom, " + POINT + ") LIMIT 1", lon, lat, lon, lat);
                if (county != null) {
                    admin2 = county[0];
                    countyName = county[1];
                }
            }
            if (iso2 == null && loaded("ref_sea")) {
                String[] area = first("SELECT name FROM ref_sea WHERE geom && " + POINT + " AND ST_Covers(geom, "
                        + POINT + ") LIMIT 1", lon, lat, lon, lat);
                sea = area == null ? null : area[0];
            }
            if (loaded("gazetteer_place")) {
                near = near(lon, lat);
            }
        } catch (DataAccessException e) {
            log.warn("Region tagging at {},{} degraded to the bundled country: {}", lon, lat, e.getMessage());
            String fallback = bundled.iso2At(lon, lat);
            return new RegionTags(fallback, null, null, null, null, fallback == null ? null : countries.name(fallback),
                    null, null);
        }
        return new RegionTags(iso2, admin1, admin2, sea, near, countryName, admin1Name, countyName);
    }

    private RegionNear near(double lon, double lat) {
        // The point is inlined (not a joined subquery) so the KNN ORDER BY uses the GiST index on geog.
        String point = POINT + "::geography";
        List<RegionNear> rows = jdbc.query("SELECT g.geonameid, g.name, g.iso2, g.admin1_code, g.population,"
                + " ST_Distance(g.geog, " + point + ") AS d, degrees(ST_Azimuth(g.geog, " + point + ")) AS az,"
                + " (SELECT a.code FROM ref_admin1 a WHERE a.gn_code = g.iso2 || '.' || g.admin1_code LIMIT 1) AS iso"
                + " FROM gazetteer_place g"
                + " WHERE COALESCE(g.population, 0) >= ? ORDER BY g.geog <-> " + point + " LIMIT 1",
                (rs, i) -> {
                    String iso2 = rs.getString("iso2");
                    String admin1Code = rs.getString("admin1_code");
                    long population = rs.getLong("population");
                    Long pop = rs.wasNull() ? null : population;
                    String iso = rs.getString("iso");
                    double az = rs.getDouble("az");
                    if (rs.wasNull()) {
                        az = 0;
                    }
                    return new RegionNear(rs.getLong("geonameid"), rs.getString("name"), iso2,
                            iso != null ? iso : admin1Code, rs.getDouble("d") / 1000.0, az, pop,
                            RegionTagStore.suffix(countries, iso2, admin1Code));
                },
                lon, lat, lon, lat, properties.nearMinPopulation(), lon, lat);
        return rows.isEmpty() ? null : rows.getFirst();
    }

    /** The nearest row of {@code table} within {@code coastal-km}, on the simplified boundary. */
    private String[] nearest(String table, String columns, String iso2, double lon, double lat) {
        double meters = properties.coastalKm() * 1000;
        double[] d = RuleSqlBuilder.expandDegrees(lat, meters);
        String shape = "COALESCE(geom_simple, geom)";
        String sql = "SELECT " + columns + " FROM " + table + " WHERE " + shape + " && ST_Expand(" + POINT
                + ", CAST(? AS double precision), CAST(? AS double precision))"
                + (iso2 == null ? "" : " AND iso2 = ?")
                + " AND ST_DWithin(" + shape + "::geography, " + POINT + "::geography, CAST(? AS double precision))"
                + " ORDER BY ST_Distance(" + shape + "::geography, " + POINT + "::geography) LIMIT 1";
        Object[] args = iso2 == null
                ? new Object[] {lon, lat, d[0], d[1], lon, lat, meters, lon, lat}
                : new Object[] {lon, lat, d[0], d[1], iso2, lon, lat, meters, lon, lat};
        return first(sql, args);
    }

    private String[] first(String sql, Object... args) {
        List<String[]> rows = jdbc.query(sql, (rs, i) -> {
            int n = rs.getMetaData().getColumnCount();
            String[] out = new String[n];
            for (int c = 0; c < n; c++) {
                out[c] = rs.getString(c + 1);
            }
            return out;
        }, args);
        return rows.isEmpty() ? null : rows.getFirst();
    }

    private Geometry liveGeometry(String sourceId, String externalId) {
        List<byte[]> rows = jdbc.query("SELECT ST_AsBinary(geom) FROM record_live WHERE source_id = ? AND external_id = ?",
                (rs, i) -> rs.getBytes(1), sourceId, externalId);
        if (rows.isEmpty() || rows.getFirst() == null) {
            return null;
        }
        try {
            return new WKBReader().read(rows.getFirst());
        } catch (ParseException e) {
            return null;
        }
    }

    // ------------------------------------------------------------------------- geometry helpers

    /** The representative point of {@code geometry} (on it for areas and lines), or null. */
    public static Point representative(Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) {
            return null;
        }
        if (geometry instanceof Point point) {
            return point;
        }
        Point point = geometry.getInteriorPoint();
        return point == null || point.isEmpty() ? geometry.getCentroid() : point;
    }

    /** Great-circle distance in kilometres (haversine, mean Earth radius). */
    public static double distanceKm(double lon1, double lat1, double lon2, double lat2) {
        double r = 6371.0088;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * r * Math.asin(Math.min(1, Math.sqrt(a)));
    }

    static double round(double value) {
        return Math.round(value * 100) / 100.0;
    }

    static long cacheKey(double lon, double lat) {
        long x = Math.round((lon + 180) * 100);
        long y = Math.round((lat + 90) * 100);
        return x * 100_000L + y;
    }
}
