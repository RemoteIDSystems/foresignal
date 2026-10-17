package ai.foresignal.baserate.reference;

import ai.foresignal.intel.IntelVisibility;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.store.query.PgArrays;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Folds Foresignal's own history into {@code reference_history} with {@code source=foresignal} (prompt 121), nightly,
 * so the reference set keeps growing. Own history counts <strong>116 canonical events</strong>, never raw records:
 * one row per canonical family (bare 119 family, e.g. {@code conflict}, {@code fire}), region and complete ISO week,
 * plus {@code seismic:m4.5}, {@code seismic:m5} and {@code seismic:m6} from the events' magnitude band value. Merged
 * events and events seen only by private sources are left out. Each event counts in every 040 region code it carries
 * ({@code ZZ} when none).
 *
 * <p>The first run folds everything since the oldest canonical event (at most {@value #MAX_WEEKS} weeks back); later
 * runs re-fold the last {@value #REFOLD_WEEKS} weeks before the last folded week, so late-linked events are counted.
 * The current, incomplete week is never folded.
 */
@Component
public class OwnHistoryFold {

    /** Weeks re-folded behind the last folded week. */
    public static final int REFOLD_WEEKS = 4;

    /** The longest first fold. */
    public static final int MAX_WEEKS = 520;

    private static final Logger log = LoggerFactory.getLogger(OwnHistoryFold.class);

    private final NamedParameterJdbcTemplate jdbc;
    private final ReferenceHistoryWriter writer;
    private final AppSettingService settings;
    private final ObjectProvider<IntelVisibility> visibility;
    private final SourceRegistry registry;
    private final Clock clock;

    /** Creates the fold. */
    public OwnHistoryFold(NamedParameterJdbcTemplate jdbc, ReferenceHistoryWriter writer, AppSettingService settings,
            ObjectProvider<IntelVisibility> visibility, SourceRegistry registry, Clock clock) {
        this.jdbc = jdbc;
        this.writer = writer;
        this.settings = settings;
        this.visibility = visibility;
        this.registry = registry;
        this.clock = clock;
    }

    /**
     * Folds the weeks due.
     *
     * @return the weekly rows written
     */
    public int fold() {
        LocalDate end = ReferenceFamilies.weekStart(LocalDate.ofInstant(clock.instant(), ZoneOffset.UTC));
        LocalDate start = settings.get(SettingKeys.BASERATE_FOLD_WEEK, String.class).map(LocalDate::parse)
                .map(last -> last.minusWeeks(REFOLD_WEEKS)).orElse(null);
        if (start == null) {
            LocalDate oldest = jdbc.query("SELECT min(event_time) FROM canonical_event", new MapSqlParameterSource(),
                    rs -> rs.next() && rs.getTimestamp(1) != null
                            ? LocalDate.ofInstant(rs.getTimestamp(1).toInstant(), ZoneOffset.UTC) : null);
            if (oldest == null) {
                return 0;
            }
            start = ReferenceFamilies.weekStart(oldest);
        }
        if (start.isBefore(end.minusWeeks(MAX_WEEKS))) {
            start = end.minusWeeks(MAX_WEEKS);
        }
        if (!start.isBefore(end)) {
            return 0;
        }
        List<WeeklyCounts.Row> rows = rows(start, end);
        int written = writer.replace(ReferenceFamilies.FORESIGNAL, null, start, end, List.of(), rows);
        settings.put(SettingKeys.BASERATE_FOLD_WEEK, end.toString());
        log.info("Own history folded: {} weekly rows for {} to {}", written, start, end);
        return written;
    }

    /** The weekly rows of canonical events in {@code [start, end)}. */
    public List<WeeklyCounts.Row> rows(LocalDate start, LocalDate end) {
        List<String> privateSources = new ArrayList<>();
        IntelVisibility v = visibility.getIfAvailable();
        if (v != null) {
            for (String id : registry.ids()) {
                if (v.isPrivateSource(id)) {
                    privateSources.add(id);
                }
            }
        }
        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("from", Timestamp.from(start.atStartOfDay(ZoneOffset.UTC).toInstant()))
                .addValue("to", Timestamp.from(end.atStartOfDay(ZoneOffset.UTC).toInstant()));
        String privateClause = "";
        if (!privateSources.isEmpty()) {
            privateClause = " AND NOT (coalesce(e.source_ids, ARRAY[]::text[]) <@ CAST(:private AS text[]))";
            p.addValue("private", PgArrays.text(privateSources));
        }
        List<WeeklyCounts.Row> out = new ArrayList<>();
        jdbc.query("SELECT e.family, CAST(date_trunc('week', e.event_time AT TIME ZONE 'UTC') AS date) AS week, r.code,"
                + " count(*) AS n, count(*) FILTER (WHERE e.band_value >= 4.5) AS m45, count(*) FILTER (WHERE"
                + " e.band_value >= 5.0) AS m5, count(*) FILTER (WHERE e.band_value >= 6.0) AS m6 FROM canonical_event e"
                + " CROSS JOIN LATERAL unnest(CASE WHEN e.region_codes IS NULL OR cardinality(e.region_codes) = 0 THEN"
                + " ARRAY['ZZ']::text[] ELSE e.region_codes END) AS r(code) WHERE e.status <> 'merged' AND"
                + " e.event_time >= :from AND e.event_time < :to" + privateClause + " GROUP BY 1, 2, 3", p, rs -> {
                    String family = rs.getString("family");
                    String region = rs.getString("code");
                    Date week = rs.getDate("week");
                    if (family == null || region == null || week == null) {
                        return;
                    }
                    LocalDate day = week.toLocalDate();
                    out.add(new WeeklyCounts.Row(family, region, day, rs.getLong("n"), null));
                    if ("seismic".equals(family)) {
                        long[] bands = {rs.getLong("m45"), rs.getLong("m5"), rs.getLong("m6")};
                        for (int i = 0; i < bands.length; i++) {
                            if (bands[i] > 0) {
                                out.add(new WeeklyCounts.Row(ReferenceFamilies.SEISMIC.get(i), region, day, bands[i],
                                        null));
                            }
                        }
                    }
                });
        return out;
    }
}
