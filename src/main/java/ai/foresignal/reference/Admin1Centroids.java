package ai.foresignal.reference;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * First-level region (admin-1) label points by country and name, read from 040's Natural Earth admin-1 table
 * ({@code ref_admin1}), added by prompt 068 for the IODA outage source.
 *
 * <p>{@code ref_admin1} holds Natural Earth 10m admin-1 v5.1.2 as the reference loader swapped it in (prompt 040). A
 * lookup reads one country's regions once, as {@code ST_PointOnSurface} of each region's simplified polygon (a
 * point always inside the region, like the country table's label points), and caches them in memory. It makes no
 * network call and never geocodes. The cache is dropped when reference data changes. Until the reference datasets
 * are loaded the table is empty and every lookup misses, which callers treat as "no geometry".
 *
 * <p>Names match through {@link CountryCentroids#normalize}: case-, accent- and punctuation-insensitive, so
 * "Espírito Santo" matches "Espirito Santo". There is no crosswalk from upstream region codes (IODA's NetAcuity
 * ids) to Natural Earth codes, so matching is by (country, name) only.
 */
@Component
public class Admin1Centroids {

    private static final Logger log = LoggerFactory.getLogger(Admin1Centroids.class);

    /** One country's regions with their label points, as stored. */
    static final String QUERY = "SELECT code, iso2, name, ST_X(p) AS lon, ST_Y(p) AS lat FROM (SELECT code, iso2, name,"
            + " ST_PointOnSurface(coalesce(geom_simple, geom)) AS p FROM ref_admin1 WHERE iso2 = ?) s";

    /**
     * One region's label point.
     *
     * @param code the region code (ISO 3166-2 where Natural Earth has one, else {@code adm1_code})
     * @param iso2 the country
     * @param name the Natural Earth name
     * @param lon the label point's longitude
     * @param lat the label point's latitude
     */
    public record Centroid(String code, String iso2, String name, double lon, double lat) {}

    private final Function<String, List<Centroid>> loader;
    private final Map<String, Map<String, Centroid>> cache = new ConcurrentHashMap<>();

    /**
     * Reads {@code ref_admin1} through {@code jdbc}.
     *
     * @param jdbc the application database
     */
    @Autowired
    public Admin1Centroids(JdbcTemplate jdbc) {
        this(iso2 -> jdbc.query(QUERY, (rs, i) -> new Centroid(rs.getString("code"), rs.getString("iso2"),
                rs.getString("name"), rs.getDouble("lon"), rs.getDouble("lat")), iso2));
    }

    /**
     * Reads regions through {@code loader} (tests).
     *
     * @param loader one country's regions by ISO2; may throw {@link DataAccessException}
     */
    public Admin1Centroids(Function<String, List<Centroid>> loader) {
        this.loader = loader;
    }

    /**
     * The region of country {@code iso2} named {@code name}.
     *
     * @param iso2 ISO 3166-1 alpha-2, case-insensitive
     * @param name the region name in any case, with or without accents
     * @return the region, or empty when the country has no such region, the reference data is not loaded, or the
     *     database could not be read
     */
    public Optional<Centroid> byName(String iso2, String name) {
        if (iso2 == null || iso2.isBlank() || name == null || name.isBlank()) {
            return Optional.empty();
        }
        Map<String, Centroid> regions = regions(iso2.strip().toUpperCase(Locale.ROOT));
        return Optional.ofNullable(regions.get(CountryCentroids.normalize(name)));
    }

    /** Drops the cache after reference datasets were swapped in. */
    @EventListener
    public void onReferenceChanged(ReferenceDataChangedEvent event) {
        cache.clear();
    }

    private Map<String, Centroid> regions(String iso2) {
        Map<String, Centroid> cached = cache.get(iso2);
        if (cached != null) {
            return cached;
        }
        List<Centroid> rows;
        try {
            rows = loader.apply(iso2);
        } catch (DataAccessException e) {
            // Not cached: the next lookup tries again.
            log.debug("Could not read ref_admin1 for {}: {}", iso2, e.getMessage());
            return Map.of();
        }
        Map<String, Centroid> byName = new HashMap<>();
        for (Centroid c : rows == null ? List.<Centroid>of() : rows) {
            if (c.name() != null && !c.name().isBlank()) {
                byName.putIfAbsent(CountryCentroids.normalize(c.name()), c);
            }
        }
        Map<String, Centroid> frozen = Map.copyOf(byName);
        cache.put(iso2, frozen);
        return frozen;
    }
}
