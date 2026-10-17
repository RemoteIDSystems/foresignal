package ai.foresignal.baserate.reference;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Weekly event counts of a reference family per region from {@code reference_history} (prompt 126): the data the
 * regional outlooks model and their ledger questions resolve on, so the forecast and the answer read the same
 * source.
 *
 * <p>One source per region, never a mix, so the same events are never counted twice: the first source of the
 * family's 121 priority (conflict: ACLED, UCDP, Foresignal; seismic: USGS, Foresignal; disaster: GDACS, Foresignal;
 * otherwise the longest coverage first) that has rows for the region. A source covers the weeks from its first to its
 * last {@code week_start} for the family (its imports and the own-history fold write whole weeks); a covered week
 * without a row counts zero. Counts for families with canonical events (116) are Foresignal's folded canonical
 * events, never raw records.
 */
@Component
public class ReferenceCounts {

    private final NamedParameterJdbcTemplate jdbc;

    /** Creates the reader. */
    public ReferenceCounts(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * The weeks a source covers for a family.
     *
     * @param source the source
     * @param first the first covered week (Monday)
     * @param last the last covered week (Monday)
     */
    public record Coverage(String source, LocalDate first, LocalDate last) {

        /** Weeks covered. */
        public long weeks() {
            return java.time.temporal.ChronoUnit.WEEKS.between(first, last) + 1;
        }

        /** True when every week of {@code [from, to)} is covered. */
        public boolean covers(LocalDate from, LocalDate to) {
            return !from.isBefore(first) && !to.minusWeeks(1).isAfter(last);
        }
    }

    /**
     * A region's weekly counts.
     *
     * @param family the reference family
     * @param region the 040 region code
     * @param source the source used
     * @param firstWeek the Monday of {@code counts[0]}
     * @param counts one count per week
     */
    public record Weekly(String family, String region, String source, LocalDate firstWeek, long[] counts) {

        /** The number of weeks. */
        public int size() {
            return counts.length;
        }

        /** The Monday of week {@code i}. */
        public LocalDate weekAt(int i) {
            return firstWeek.plusWeeks(i);
        }

        /** The index of the week starting on {@code monday}, or −1. */
        public int indexOf(LocalDate monday) {
            long i = java.time.temporal.ChronoUnit.WEEKS.between(firstWeek, monday);
            return i >= 0 && i < counts.length ? (int) i : -1;
        }
    }

    /**
     * A count over whole weeks.
     *
     * @param value the count, or null when no source covers the weeks
     * @param source the source used, or null
     * @param covered true when the source covers every week
     */
    public record Count(Long value, String source, boolean covered) {}

    /** Every source's coverage of a family, longest first. */
    public Map<String, Coverage> coverage(String family) {
        Map<String, Coverage> out = new LinkedHashMap<>();
        List<Coverage> rows = jdbc.query("SELECT source, min(week_start) AS first, max(week_start) AS last FROM"
                + " reference_history WHERE family = :family GROUP BY source", new MapSqlParameterSource("family",
                        family), (rs, i) -> new Coverage(rs.getString("source"), rs.getDate("first").toLocalDate(),
                                rs.getDate("last").toLocalDate()));
        rows.sort(Comparator.comparingLong(Coverage::weeks).reversed().thenComparing(Coverage::source));
        for (Coverage c : rows) {
            out.put(c.source(), c);
        }
        return out;
    }

    /** The source order of a family: its 121 priority, then the other sources by coverage. */
    static List<String> order(String family, Map<String, Coverage> coverage) {
        List<String> out = new ArrayList<>();
        List<String> priority = ReferenceFamilies.priority(family);
        if (priority != null) {
            for (String s : priority) {
                if (coverage.containsKey(s)) {
                    out.add(s);
                }
            }
        }
        for (String s : coverage.keySet()) {
            if (!out.contains(s)) {
                out.add(s);
            }
        }
        return out;
    }

    /**
     * The weekly counts of a family per region, each from one source, up to {@code until} (exclusive).
     *
     * @param regions the regions, or null for every region with rows
     */
    public Map<String, Weekly> weekly(String family, LocalDate until, Collection<String> regions) {
        Map<String, Coverage> coverage = coverage(family);
        if (coverage.isEmpty()) {
            return Map.of();
        }
        List<String> order = order(family, coverage);
        MapSqlParameterSource p = new MapSqlParameterSource("family", family).addValue("until", Date.valueOf(until));
        String where = "";
        if (regions != null) {
            if (regions.isEmpty()) {
                return Map.of();
            }
            where = " AND region_code IN (:regions)";
            p.addValue("regions", List.copyOf(regions));
        }
        Map<String, Map<String, TreeMap<LocalDate, Long>>> rows = new HashMap<>();
        jdbc.query("SELECT region_code, source, week_start, count FROM reference_history WHERE family = :family AND"
                + " week_start < :until" + where, p, rs -> {
                    rows.computeIfAbsent(rs.getString("region_code"), k -> new HashMap<>())
                            .computeIfAbsent(rs.getString("source"), k -> new TreeMap<>())
                            .merge(rs.getDate("week_start").toLocalDate(), rs.getLong("count"), Long::sum);
                });
        Map<String, Weekly> out = new TreeMap<>();
        for (Map.Entry<String, Map<String, TreeMap<LocalDate, Long>>> e : rows.entrySet()) {
            String source = null;
            for (String s : order) {
                if (e.getValue().containsKey(s)) {
                    source = s;
                    break;
                }
            }
            if (source == null) {
                continue;
            }
            Coverage c = coverage.get(source);
            LocalDate end = c.last().plusWeeks(1).isBefore(until) ? c.last().plusWeeks(1) : until;
            int n = (int) java.time.temporal.ChronoUnit.WEEKS.between(c.first(), end);
            if (n <= 0) {
                continue;
            }
            long[] counts = new long[n];
            for (Map.Entry<LocalDate, Long> w : e.getValue().get(source).entrySet()) {
                long i = java.time.temporal.ChronoUnit.WEEKS.between(c.first(), w.getKey());
                if (i >= 0 && i < n) {
                    counts[(int) i] += w.getValue();
                }
            }
            out.put(e.getKey(), new Weekly(family, e.getKey(), source, c.first(), counts));
        }
        return out;
    }

    /**
     * The count of a family in a region over the whole weeks {@code [fromWeek, toWeek)}, from the first source in
     * priority that has rows for the region (else the first that covers the weeks).
     */
    public Count count(String family, String region, LocalDate fromWeek, LocalDate toWeek) {
        Map<String, Coverage> coverage = coverage(family);
        if (coverage.isEmpty()) {
            return new Count(null, null, false);
        }
        List<String> order = order(family, coverage);
        List<String> withRows = jdbc.queryForList("SELECT DISTINCT source FROM reference_history WHERE family ="
                + " :family AND region_code = :region", new MapSqlParameterSource("family", family)
                        .addValue("region", region), String.class);
        String source = null;
        for (String s : order) {
            if (withRows.contains(s)) {
                source = s;
                break;
            }
        }
        if (source == null) {
            for (String s : order) {
                if (coverage.get(s).covers(fromWeek, toWeek)) {
                    source = s;
                    break;
                }
            }
        }
        if (source == null) {
            return new Count(null, null, false);
        }
        Long sum = jdbc.queryForObject("SELECT COALESCE(sum(count), 0) FROM reference_history WHERE family = :family"
                + " AND region_code = :region AND source = :source AND week_start >= :from AND week_start < :to",
                new MapSqlParameterSource("family", family).addValue("region", region).addValue("source", source)
                        .addValue("from", Date.valueOf(fromWeek)).addValue("to", Date.valueOf(toWeek)), Long.class);
        return new Count(sum == null ? 0 : sum, source, coverage.get(source).covers(fromWeek, toWeek));
    }

    /** The fatalities of a family in a region over {@code [from, to)} (null counts as 0), any source or one. */
    public double fatalities(String family, String region, LocalDate from, LocalDate to, String source) {
        MapSqlParameterSource p = new MapSqlParameterSource("family", family).addValue("region", region)
                .addValue("from", Date.valueOf(from)).addValue("to", Date.valueOf(to));
        String bySource = "";
        if (source != null) {
            bySource = " AND source = :source";
            p.addValue("source", source);
        }
        Double v = jdbc.queryForObject("SELECT COALESCE(sum(fatalities), 0) FROM reference_history WHERE family ="
                + " :family AND region_code = :region AND week_start >= :from AND week_start < :to" + bySource, p,
                Double.class);
        return v == null ? 0 : v;
    }

    /** The events of a family in a region over {@code [from, to)} from its chosen source. */
    public long events(String family, String region, LocalDate from, LocalDate to) {
        Count c = count(family, region, ReferenceFamilies.weekStart(from), ReferenceFamilies.weekCeil(to));
        return c.value() == null ? 0 : c.value();
    }
}
