package ai.foresignal.outlook.regional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Region adjacency for the regional outlooks (prompt 126): two countries are neighbors when their 040 boundaries
 * ({@code ref_country}, simplified geometry where loaded) lie within {@value #TOLERANCE_DEGREES}° of each other; two
 * admin-1 regions of the same country likewise ({@code ref_admin1}). 040 stores boundaries but no adjacency table, so
 * adjacency is derived from them here and cached for {@value #CACHE_HOURS} hours. Without loaded reference data a
 * region has no neighbors and the outlook model leaves the neighbor term out.
 */
@Component
public class RegionNeighbors {

    private static final Logger log = LoggerFactory.getLogger(RegionNeighbors.class);

    /** The distance under which two boundaries count as adjacent, degrees. */
    public static final double TOLERANCE_DEGREES = 0.05;

    /** Hours a computed adjacency is kept. */
    public static final int CACHE_HOURS = 24;

    private final NamedParameterJdbcTemplate jdbc;
    private final Clock clock;
    private volatile Map<String, List<String>> countries;
    private volatile Map<String, List<String>> admin1;
    private volatile Instant computedAt;

    /** Creates the component. */
    public RegionNeighbors(NamedParameterJdbcTemplate jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    /** The neighbors of a country or admin-1 region (empty when unknown). */
    public List<String> of(String region) {
        refresh();
        Map<String, List<String>> map = region != null && region.contains("-") ? admin1 : countries;
        return map == null ? List.of() : map.getOrDefault(region, List.of());
    }

    /** Drops the cache (tests, reference reloads). */
    public synchronized void reset() {
        countries = null;
        admin1 = null;
        computedAt = null;
    }

    private synchronized void refresh() {
        Instant now = clock.instant();
        if (computedAt != null && computedAt.isAfter(now.minus(Duration.ofHours(CACHE_HOURS)))) {
            return;
        }
        countries = pairs("SELECT a.iso2 AS a, b.iso2 AS b FROM ref_country a JOIN ref_country b ON a.iso2 < b.iso2"
                + " AND ST_DWithin(COALESCE(a.geom_simple, a.geom), COALESCE(b.geom_simple, b.geom), :tol)"
                + " WHERE a.iso2 IS NOT NULL AND b.iso2 IS NOT NULL");
        admin1 = pairs("SELECT a.code AS a, b.code AS b FROM ref_admin1 a JOIN ref_admin1 b ON a.iso2 = b.iso2 AND"
                + " a.code < b.code AND ST_DWithin(a.geom, b.geom, :tol)");
        computedAt = now;
    }

    private Map<String, List<String>> pairs(String sql) {
        Map<String, List<String>> out = new HashMap<>();
        try {
            jdbc.query(sql, new MapSqlParameterSource("tol", TOLERANCE_DEGREES), rs -> {
                String a = rs.getString("a");
                String b = rs.getString("b");
                out.computeIfAbsent(a, k -> new ArrayList<>()).add(b);
                out.computeIfAbsent(b, k -> new ArrayList<>()).add(a);
            });
        } catch (RuntimeException e) {
            log.warn("Region adjacency could not be computed: {}", e.toString());
        }
        return out;
    }
}
