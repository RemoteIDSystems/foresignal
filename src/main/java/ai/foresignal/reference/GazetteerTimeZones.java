package ai.foresignal.reference;

import ai.foresignal.ingest.IngestStateReset;
import ai.foresignal.source.spi.PlaceTimeZones;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * The nearest populated place's IANA timezone, from 040's {@code gazetteer_place} table (prompt 095), for
 * adapters that must convert a local wall-clock time with no offset into an instant (iNaturalist
 * {@code observed_on}, eBird {@code obsDt}).
 *
 * <p>A KNN query on the GiST index finds the closest place with a timezone in meters. It makes no network call
 * and never geocodes. Lookups are cached on coordinates rounded to 0.05°; the cache is dropped when reference
 * data changes. Until the reference datasets are loaded the table is empty and every lookup misses, which
 * callers treat as "no zone" and fall back to UTC.
 */
@Component
public class GazetteerTimeZones implements PlaceTimeZones, IngestStateReset {

    private static final Logger log = LoggerFactory.getLogger(GazetteerTimeZones.class);

    /** The nearest place with a timezone. */
    static final String QUERY = "SELECT timezone FROM gazetteer_place WHERE timezone IS NOT NULL"
            + " ORDER BY geog <-> (ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography) LIMIT 1";

    /** The coordinate rounding used as the cache key (about 5 km). */
    static final double CACHE_DEGREES = 0.05;

    /** The most lookups kept in the cache. */
    static final int MAX_CACHED = 20_000;

    private final JdbcTemplate jdbc;
    private final Map<String, Optional<ZoneId>> cache = new ConcurrentHashMap<>();

    /** Creates the resolver. */
    public GazetteerTimeZones(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<ZoneId> byPoint(double lon, double lat) {
        if (Double.isNaN(lon) || Double.isNaN(lat) || lon < -180 || lon > 180 || lat < -90 || lat > 90) {
            return Optional.empty();
        }
        String key = rounded(lon) + "," + rounded(lat);
        Optional<ZoneId> cached = cache.get(key);
        if (cached != null) {
            return cached;
        }
        Optional<ZoneId> resolved = load(lon, lat);
        if (cache.size() >= MAX_CACHED) {
            cache.clear();
        }
        cache.put(key, resolved);
        return resolved;
    }

    private Optional<ZoneId> load(double lon, double lat) {
        try {
            String zone = jdbc.queryForObject(QUERY, String.class, lon, lat);
            if (zone == null || zone.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(ZoneId.of(zone.strip()));
        } catch (DataAccessException e) {
            log.debug("Could not read gazetteer_place timezone: {}", e.getMessage());
            return Optional.empty();
        } catch (DateTimeException e) {
            return Optional.empty();
        }
    }

    private static String rounded(double value) {
        return String.format(java.util.Locale.ROOT, "%.2f", Math.round(value / CACHE_DEGREES) * CACHE_DEGREES);
    }

    /** Drops the cache after a reference load. */
    @EventListener
    public void onReferenceChanged(ReferenceDataChangedEvent event) {
        cache.clear();
    }

    /** Drops the cache (also after the test cleaner truncates). */
    @Override
    public void resetState() {
        cache.clear();
    }
}
