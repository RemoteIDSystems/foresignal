package ai.foresignal.stats;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.restrict.ReadRestriction;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Stats answered from the region rollups (prompt 119): {@code /v1/stats/timeseries} with {@code groupBy=region} or a
 * {@code region=} filter, and {@code /v1/stats/top} with {@code by=region} or a {@code region=} filter.
 *
 * <p>Reads the per-source region rows (resolutions 2 and 3, never the cross-source {@code *} rows, which include no
 * per-caller filtering), summing the measure over the matching sources, kinds and regions. The caller's excluded
 * sources and source and kind restrictions apply; a caller with a spatial restriction gets nothing (region rows have
 * no geometry, so they cannot be clipped: fail closed). Distinct entities summed over several sources or kinds can
 * count one entity more than once; per-source per-kind series are exact.
 */
@Component
public class RegionStatsService {

    /** The group keys of the region timeseries. */
    public static final List<String> GROUP_BY_VALUES = List.of("none", "source", "kind", "region");

    private static final Pattern REGION = RegionRollupComputer.REGION_CODE;

    private final NamedParameterJdbcTemplate jdbc;

    /** Creates the service. */
    public RegionStatsService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Validates region codes ({@code 400} for anything that is not a 040 code). */
    public static List<String> validRegions(List<String> regions) {
        List<String> out = new ArrayList<>();
        for (String region : regions == null ? List.<String>of() : regions) {
            String code = region.strip();
            if (!REGION.matcher(code).matches()) {
                throw ValidationException.forField("region", "'" + region + "' is not a 040 region code");
            }
            out.add(code);
        }
        return out;
    }

    /**
     * The region timeseries.
     *
     * @param bucket {@code 1h}, {@code 1d} or {@code auto}
     * @param groupBy {@code none}, {@code source}, {@code kind} or {@code region}
     */
    public StatsSeries timeseries(List<String> sources, List<String> kinds, List<String> regions, Instant from,
            Instant to, String bucket, StatsMeasure measure, String groupBy, Set<String> excluded,
            ReadRestriction restriction) {
        if (!from.isBefore(to)) {
            throw ValidationException.forField("to", "must be after from");
        }
        RollupResolution width = StatsService.bucket(bucket, from, to);
        RegionRollupResolution resolution = RegionRollupResolution.of(width);
        Instant start = width.floor(from);
        Instant end = width.ceil(to);
        long count = Duration.between(start, end).dividedBy(width.width());
        if (count > StatsService.MAX_BUCKETS) {
            throw new ValidationException(ErrorCodes.STATS_TOO_LARGE, "The series would have " + count
                    + " buckets, more than " + StatsService.MAX_BUCKETS + "; use bucket=1d or a shorter range.",
                    List.of());
        }
        String group = groupBy == null ? "none" : groupBy;
        String column = groupColumn(group);
        Map<String, Map<Instant, Long>> values = new TreeMap<>(java.util.Comparator.nullsFirst(String::compareTo));
        if (!spatiallyRestricted(restriction)) {
            MapSqlParameterSource params = new MapSqlParameterSource();
            String where = where(sources, kinds, regions, excluded, restriction, resolution, start, end, params);
            String sql = "SELECT bucket_start, " + (column == null ? "NULL" : column) + " AS k, sum("
                    + measure.column() + ") AS v FROM activity_rollup WHERE " + where + " GROUP BY bucket_start, k";
            jdbc.query(sql, params, rs -> {
                values.computeIfAbsent(rs.getString("k"), k -> new TreeMap<>())
                        .put(rs.getTimestamp("bucket_start").toInstant(), rs.getLong("v"));
            });
        }
        if (values.isEmpty() && column == null) {
            values.put(null, new TreeMap<>());
        }
        List<StatsSeries.Series> series = new ArrayList<>();
        for (Map.Entry<String, Map<Instant, Long>> e : values.entrySet()) {
            List<StatsSeries.Point> points = new ArrayList<>();
            for (Instant t = start; t.isBefore(end); t = t.plus(width.width())) {
                points.add(new StatsSeries.Point(t, e.getValue().getOrDefault(t, 0L)));
            }
            series.add(new StatsSeries.Series(e.getKey(), points));
        }
        return new StatsSeries(start, end, width == RollupResolution.DAY ? "1d" : "1h", measure.wire(), group,
                "ingested", "rollup", series, null);
    }

    /** The top regions (or sources or kinds) over region rows. */
    public StatsTop top(String by, StatsMeasure measure, Instant from, Instant to, int limit, List<String> sources,
            List<String> kinds, List<String> regions, Set<String> excluded, ReadRestriction restriction) {
        if (!from.isBefore(to)) {
            throw ValidationException.forField("to", "must be after from");
        }
        String column = groupColumn(by);
        if (column == null) {
            throw ValidationException.forField("by", "must be source, kind or region with region rollups");
        }
        boolean daily = Duration.between(from, to).compareTo(StatsService.AUTO_HOURLY_RANGE) > 0;
        RegionRollupResolution resolution = daily ? RegionRollupResolution.DAY : RegionRollupResolution.HOUR;
        Instant start = daily ? RollupResolution.DAY.floor(from) : RollupResolution.HOUR.floor(from);
        Instant end = daily ? RollupResolution.DAY.ceil(to) : RollupResolution.HOUR.ceil(to);
        List<StatsTop.Item> items = new ArrayList<>();
        if (!spatiallyRestricted(restriction)) {
            MapSqlParameterSource params = new MapSqlParameterSource();
            String where = where(sources, kinds, regions, excluded, restriction, resolution, start, end, params);
            params.addValue("limit", limit);
            jdbc.query("SELECT " + column + " AS k, sum(" + measure.column() + ") AS v FROM activity_rollup WHERE "
                    + where + " GROUP BY k ORDER BY v DESC, k LIMIT :limit", params, rs -> {
                        String key = rs.getString("k");
                        items.add(new StatsTop.Item(key, "source".equals(by) ? key : null,
                                "kind".equals(by) ? key : null, null, null, null, rs.getLong("v")));
                    });
        }
        return new StatsTop(by, measure.wire(), from, to, "rollup", items);
    }

    /** True when the caller is clipped spatially (region rows cannot honor it). */
    public static boolean spatiallyRestricted(ReadRestriction restriction) {
        return restriction != null && (restriction.region() != null || restriction.filter() != null
                || restriction.regionFilter() != null);
    }

    private static String groupColumn(String group) {
        return switch (group == null ? "none" : group) {
            case "source" -> "source_id";
            case "kind" -> "kind";
            case "region" -> "region_code";
            case "none" -> null;
            default -> throw ValidationException.forField("groupBy", "must be one of " + GROUP_BY_VALUES);
        };
    }

    private static String where(Collection<String> sources, Collection<String> kinds, Collection<String> regions,
            Set<String> excluded, ReadRestriction restriction, RegionRollupResolution resolution, Instant start,
            Instant end, MapSqlParameterSource params) {
        List<String> clauses = new ArrayList<>();
        clauses.add("resolution = :resolution");
        clauses.add("bucket_start >= :from AND bucket_start < :to");
        clauses.add("source_id <> '" + RegionRollupComputer.ALL_SOURCES + "'");
        params.addValue("resolution", resolution.code());
        params.addValue("from", Timestamp.from(start));
        params.addValue("to", Timestamp.from(end));
        if (sources != null && !sources.isEmpty()) {
            clauses.add("source_id IN (:sources)");
            params.addValue("sources", List.copyOf(sources));
        }
        if (kinds != null && !kinds.isEmpty()) {
            clauses.add("kind IN (:kinds)");
            params.addValue("kinds", List.copyOf(kinds));
        }
        if (regions != null && !regions.isEmpty()) {
            clauses.add("region_code IN (:regions)");
            params.addValue("regions", List.copyOf(regions));
        }
        if (excluded != null && !excluded.isEmpty()) {
            clauses.add("source_id NOT IN (:excluded)");
            params.addValue("excluded", List.copyOf(excluded));
        }
        if (restriction != null && restriction.sources() != null && !restriction.sources().isEmpty()) {
            clauses.add("source_id IN (:restrictedSources)");
            params.addValue("restrictedSources", List.copyOf(restriction.sources()));
        }
        if (restriction != null && restriction.kinds() != null && !restriction.kinds().isEmpty()) {
            clauses.add("kind IN (:restrictedKinds)");
            params.addValue("restrictedKinds", List.copyOf(restriction.kinds()));
        }
        return String.join(" AND ", clauses);
    }

    /** The region values of one {@code (source, kind)} per bucket, for the series reader (internal, unfiltered). */
    public Map<Instant, Long> values(String sourceId, String kind, String region, RegionRollupResolution resolution,
            Instant from, Instant to, StatsMeasure measure) {
        Map<Instant, Long> out = new LinkedHashMap<>();
        jdbc.query("SELECT bucket_start, " + measure.column() + " AS v FROM activity_rollup WHERE resolution ="
                        + " :resolution AND source_id = :source AND kind = :kind AND region_code = :region AND"
                        + " bucket_start >= :from AND bucket_start < :to ORDER BY bucket_start",
                new MapSqlParameterSource().addValue("resolution", resolution.code()).addValue("source", sourceId)
                        .addValue("kind", kind).addValue("region", region).addValue("from", Timestamp.from(from))
                        .addValue("to", Timestamp.from(to)),
                rs -> {
                    out.put(rs.getTimestamp("bucket_start").toInstant(), rs.getLong("v"));
                });
        return out;
    }
}
