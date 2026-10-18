package ai.foresignal.social.correlation;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Links narratives to 118 situations by place, family and time (prompt 137): a non-private, unmerged, open situation
 * that shares a 040 region (or its country) with the narrative's claims, carries one of the claims' signal families,
 * and was active within {@value #SLACK_HOURS} hours of the narrative's span. Linked narratives are the situation's
 * social evidence, counted separately from its physical corroboration; the link never changes the situation itself.
 */
@Component
public class NarrativeSituationLinker {

    /** The time slack around the narrative's span. */
    public static final int SLACK_HOURS = 24;

    /** The most situations linked per narrative. */
    static final int LIMIT = 20;

    private final JdbcTemplate jdbc;

    /** Creates the linker. */
    public NarrativeSituationLinker(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** The region codes a narrative matches: its own and their countries. */
    static String[] regions(List<String> regions) {
        Set<String> out = new LinkedHashSet<>();
        for (String r : regions) {
            if (r != null && !r.isBlank()) {
                out.add(r);
                if (r.length() > 2) {
                    out.add(r.substring(0, 2));
                }
            }
        }
        return out.toArray(String[]::new);
    }

    /**
     * The situations a narrative links to.
     *
     * @param regions the narrative's and its claims' regions
     * @param families its claims' signal families (none: no link)
     * @param first its earliest post
     * @param last its latest post
     */
    public List<String> situations(List<String> regions, List<String> families, Instant first, Instant last) {
        String[] r = regions(regions);
        if (r.length == 0 || families.isEmpty() || first == null) {
            return List.of();
        }
        Instant to = (last == null ? first : last).plus(Duration.ofHours(SLACK_HOURS));
        Instant from = first.minus(Duration.ofHours(SLACK_HOURS));
        return new ArrayList<>(jdbc.queryForList("SELECT id FROM situation WHERE merged_into IS NULL AND NOT private"
                + " AND status <> 'closed' AND region_codes && ?::text[] AND signal_families && ?::text[] AND started_at <= ? AND"
                + " last_activity_at >= ? ORDER BY last_activity_at DESC, id LIMIT ?", String.class, r,
                families.toArray(String[]::new), Timestamp.from(to), Timestamp.from(from), LIMIT));
    }
}
