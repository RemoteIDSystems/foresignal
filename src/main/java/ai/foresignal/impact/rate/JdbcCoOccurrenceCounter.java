package ai.foresignal.impact.rate;

import ai.foresignal.impact.ImpactPairs;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * The production {@link CoOccurrenceCounter} (prompt 127): reads {@code record_version} history and places versions in
 * admin1 regions by intersecting their stored geometry with {@code ref_admin1} (so a polygon counts in every region it
 * covers, not only the region of its representative point).
 *
 * <p>A hazard occurrence is a distinct (admin1 region, UTC day) with at least one non-tombstone version matching the
 * hazard class; its time is the first such version that day. It is followed by the impact when a version matching the
 * impact family intersects the same region after that time and within the lag.
 */
@Component
public class JdbcCoOccurrenceCounter implements CoOccurrenceCounter {

    private final NamedParameterJdbcTemplate jdbc;

    /** Creates the counter. */
    public JdbcCoOccurrenceCounter(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Counts count(ImpactPairs.HazardClass hazard, ImpactPairs.ImpactFamily family, int lagHours,
            Collection<String> countries, Instant from, Instant to) {
        if (countries.isEmpty() || !to.isAfter(from)) {
            return new Counts(0, 0);
        }
        MapSqlParameterSource p = new MapSqlParameterSource();
        String sql = sql(hazard, family, p);
        p.addValue("countries", new ArrayList<>(countries));
        p.addValue("from", Timestamp.from(from));
        p.addValue("to", Timestamp.from(to));
        p.addValue("lag", lagHours);
        return jdbc.queryForObject(sql, p, (rs, n) -> new Counts(rs.getLong("n"), rs.getLong("x")));
    }

    /** The statement (parameters {@code countries}, {@code from}, {@code to} and {@code lag} are added by the caller). */
    static String sql(ImpactPairs.HazardClass hazard, ImpactPairs.ImpactFamily family, MapSqlParameterSource p) {
        return "WITH hz AS (SELECT a.code AS admin1, CAST(v.observed_at AT TIME ZONE 'UTC' AS date) AS day,"
                + " min(v.observed_at) AS at FROM record_version v JOIN ref_admin1 a ON ST_Intersects(a.geom, v.geom)"
                + " WHERE v.tombstone = false AND v.geom IS NOT NULL AND v.observed_at >= :from AND v.observed_at < :to"
                + " AND a.iso2 IN (:countries)" + SelectorSql.where("v", hazard.selector(), "h", p)
                + " GROUP BY 1, 2)"
                + " SELECT count(*) AS n, count(*) FILTER (WHERE EXISTS (SELECT 1 FROM record_version i JOIN ref_admin1 b"
                + " ON b.code = hz.admin1 AND ST_Intersects(b.geom, i.geom) WHERE i.tombstone = false AND i.geom IS NOT"
                + " NULL AND i.observed_at > hz.at AND i.observed_at <= hz.at + make_interval(hours => :lag)"
                + SelectorSql.where("i", family.selector(), "f", p) + ")) AS x FROM hz";
    }
}
