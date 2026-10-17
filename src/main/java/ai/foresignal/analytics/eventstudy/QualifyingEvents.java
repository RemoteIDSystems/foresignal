package ai.foresignal.analytics.eventstudy;

import ai.foresignal.analytics.family.SignalFamilyRegistry;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * The qualifying events of live event studies (prompt 119, Intelligence-Design §5.3) from the typed tables: intelligence
 * events of severity warning or higher (by their start), situations turning active, and significant change points.
 * Used to keep placebo dates away from real events and to tell an unexplained move from an explained one.
 */
@Component
public class QualifyingEvents {

    private final NamedParameterJdbcTemplate jdbc;
    private final SignalFamilyRegistry families;
    private final JsonMapper json;

    /** Creates the component. */
    public QualifyingEvents(NamedParameterJdbcTemplate jdbc, SignalFamilyRegistry families, JsonMapper json) {
        this.jdbc = jdbc;
        this.families = families;
        this.json = json;
    }

    /**
     * One qualifying event.
     *
     * @param at when it happened (or became known as qualifying)
     * @param ref the object reference
     * @param title a title, or null
     * @param families its canonical signal families
     * @param regions its 040 regions
     */
    public record Qualifying(Instant at, String ref, String title, Set<String> families, List<String> regions) {

        /** True when it is in one of {@code regions} (always when {@code regions} is empty). */
        public boolean in(Collection<String> regionsWanted) {
            if (regionsWanted == null || regionsWanted.isEmpty()) {
                return true;
            }
            for (String r : regions) {
                if (regionsWanted.contains(r) || (r.length() > 2 && regionsWanted.contains(r.substring(0, 2)))) {
                    return true;
                }
            }
            return false;
        }
    }

    /** Every qualifying event in {@code [from, to]}, oldest first. */
    public List<Qualifying> between(Instant from, Instant to) {
        MapSqlParameterSource p = new MapSqlParameterSource().addValue("from", Timestamp.from(from))
                .addValue("to", Timestamp.from(to));
        List<Qualifying> out = new ArrayList<>();
        jdbc.query("SELECT e.id, e.start_time, e.title, e.region_tags::text AS tags, (SELECT string_agg(DISTINCT"
                + " m.signal_family, ',') FROM intelligence_event_member m WHERE m.event_id = e.id) AS families FROM"
                + " intelligence_event e WHERE e.severity IN ('warning', 'critical') AND e.merged_into IS NULL AND"
                + " e.status <> 'dismissed' AND e.start_time >= :from AND e.start_time <= :to", p, rs -> {
                    out.add(new Qualifying(rs.getTimestamp("start_time").toInstant(), "intel-event:" + rs.getString("id"),
                            rs.getString("title"), families.canonicalAll(csv(rs.getString("families"))),
                            jsonStrings(rs.getString("tags"))));
                });
        jdbc.query("SELECT s.id, l.at, s.title, array_to_string(s.region_codes, ',') AS regions,"
                + " array_to_string(s.signal_families, ',') AS families FROM situation_log l JOIN situation s ON s.id"
                + " = l.situation_id WHERE l.event = 'situation.activated' AND l.at >= :from AND l.at <= :to", p,
                rs -> {
                    out.add(new Qualifying(rs.getTimestamp("at").toInstant(), "situation:" + rs.getString("id"),
                            rs.getString("title"), families.canonicalAll(csv(rs.getString("families"))),
                            csv(rs.getString("regions"))));
                });
        jdbc.query("SELECT id, coalesce(window_start, detected_at) AS at, title, array_to_string(region_codes, ',') AS"
                + " regions, signal_family FROM analytic_finding WHERE finding_type = 'change-point' AND severity ="
                + " 'significant' AND status <> 'dismissed' AND detected_at >= :from AND detected_at <= :to", p,
                rs -> {
                    out.add(new Qualifying(rs.getTimestamp("at").toInstant(), "finding:" + rs.getString("id"),
                            rs.getString("title"), families.canonicalAll(csv(rs.getString("signal_family"))),
                            csv(rs.getString("regions"))));
                });
        out.sort(java.util.Comparator.comparing(Qualifying::at));
        return out;
    }

    /** The times of qualifying events of {@code family} in {@code regions}, for placebo exclusion. */
    public List<Instant> times(String family, Collection<String> regions, Instant from, Instant to) {
        List<Instant> out = new ArrayList<>();
        for (Qualifying q : between(from, to)) {
            if ((family == null || q.families().contains(family)) && q.in(regions)) {
                out.add(q.at());
            }
        }
        return out;
    }

    private List<String> jsonStrings(String text) {
        List<String> out = new ArrayList<>();
        if (text == null) {
            return out;
        }
        try {
            for (var node : json.readTree(text)) {
                if (node.isString()) {
                    out.add(node.asString());
                }
            }
        } catch (RuntimeException e) {
            return out;
        }
        return out;
    }

    private static List<String> csv(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return List.of(text.split(","));
    }
}
