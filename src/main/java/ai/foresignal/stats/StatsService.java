package ai.foresignal.stats;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.BboxFilter;
import ai.foresignal.api.support.TimeField;
import ai.foresignal.retention.RetentionState;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.restrict.RestrictionClamp;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.RetentionSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.history.AsOfResolver;
import ai.foresignal.store.query.ReadQueryRunner;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.SeqClockQuery;
import ai.foresignal.store.query.SqlParams;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.locationtech.jts.geom.Envelope;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

/**
 * The stats API's engine (prompt 038): time series, grids, top-N and the period totals the briefing
 * digest compares against.
 *
 * <p><b>Backends.</b> {@code timeField=ingested} reads {@code activity_rollup} for the buckets the
 * rollup has closed (between its start and its cursor, and inside the rollups' retention) and raw
 * {@code record_version} for the rest (the open hour, or anything the rollup does not hold); the other
 * axes read raw history and need {@code from ≥ retainedFrom}; {@code measure=alerts} reads
 * {@code alert.fired_at}. Sources the rollup skips (private without {@code statsIncludePrivate}, or
 * {@code statsEnabled=false}) and requests whose filter needs the payload (view conditions) are read
 * raw. The response says {@code rollup}, {@code raw} or {@code mixed}.
 *
 * <p><b>Accuracy.</b> Without a spatial filter the rollup's per-{@code (source, kind)} totals rows are
 * read, so every measure is exact. With a bbox, a restricted region or a place filter, rollup rows are
 * selected by their cell centre (cell resolution, about 40 km at the default {@code cellRes} 4), and
 * {@code entities} sums per-cell distinct counts, so an entity that crossed cells counts once per cell.
 *
 * <p>Every query runs with {@code SET LOCAL statement_timeout} = {@code foresignal.stats.queryTimeout}
 * (10 s); a timeout is {@code 503 QUERY_TIMEOUT} with {@code Retry-After}.
 */
@Service
public class StatsService {

    /** The most buckets of a time series. */
    public static final int MAX_BUCKETS = 2_000;

    /** The most cells of a grid. */
    public static final int MAX_CELLS = 10_000;

    /** The most items of a top-N. */
    public static final int MAX_TOP = 100;

    /** The longest range {@code bucket=auto} serves hourly. */
    public static final Duration AUTO_HOURLY_RANGE = Duration.ofDays(7);

    /** The longest range of a raw grid. */
    public static final Duration MAX_RAW_GRID_RANGE = Duration.ofDays(7);

    /** The most hexagons a raw grid may generate over its bbox before counting. */
    public static final long MAX_GENERATED_HEXAGONS = 2_000_000L;

    /** The internal grouping by source and kind (the digest). */
    public static final String GROUP_PAIR = "pair";

    /** The separator of {@link #GROUP_PAIR} keys. */
    public static final char PAIR_SEPARATOR = '\u001f';

    /** The {@code groupBy} values the API accepts. */
    public static final List<String> GROUP_BY_VALUES = List.of("none", "source", "kind");

    /** Web Mercator's latitude limit. */
    static final double MERCATOR_LIMIT = 85.05112878;

    private static final String ROLLUP_GEOMETRY =
            "(CASE WHEN r.cell > 0 THEN ST_SetSRID(ST_MakePoint(r.cell_lon, r.cell_lat), 4326) END)";

    private final ReadQueryRunner runner;
    private final SeqClockQuery seqClock;
    private final RollupJob rollup;
    private final StatsSettingsService statsSettings;
    private final RetentionState retention;
    private final SourceRegistry registry;
    private final AsOfResolver asOf;
    private final AppSettingService settings;
    private final H3Cells cells;
    private final StatsProperties properties;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the service. */
    public StatsService(ReadQueryRunner runner, SeqClockQuery seqClock, RollupJob rollup,
            StatsSettingsService statsSettings, RetentionState retention, SourceRegistry registry, AsOfResolver asOf,
            AppSettingService settings, H3Cells cells, StatsProperties properties, JsonMapper json, Clock clock) {
        this.runner = runner;
        this.seqClock = seqClock;
        this.rollup = rollup;
        this.statsSettings = statsSettings;
        this.retention = retention;
        this.registry = registry;
        this.asOf = asOf;
        this.settings = settings;
        this.cells = cells;
        this.properties = properties;
        this.json = json;
        this.clock = clock;
    }

    // =====================================================================================
    // Time series
    // =====================================================================================

    /**
     * {@code getStatsTimeseries}.
     *
     * @param sources requested source ids (empty: by kind, else every visible source)
     * @param kinds requested kinds
     * @param bbox spatial filter, or null
     * @param from start (required)
     * @param to end (required, after {@code from})
     * @param bucket {@code 1h}, {@code 1d} or {@code auto} (null)
     * @param measure the measure
     * @param groupBy {@code none}, {@code source} or {@code kind}
     * @param timeField the time axis
     * @param excluded sources the caller may not see
     * @param restriction the caller's restriction
     * @throws ValidationException {@code STATS_TOO_LARGE} above {@value #MAX_BUCKETS} buckets
     */
    public StatsSeries timeseries(List<String> sources, List<String> kinds, BboxFilter bbox, Instant from, Instant to,
            String bucket, StatsMeasure measure, String groupBy, TimeField timeField, Set<String> excluded,
            ReadRestriction restriction) {
        range(from, to);
        RollupResolution resolution = bucket(bucket, from, to);
        Instant start = resolution.floor(from);
        Instant end = resolution.ceil(to);
        long count = Duration.between(start, end).dividedBy(resolution.width());
        if (count > MAX_BUCKETS) {
            throw new ValidationException(ErrorCodes.STATS_TOO_LARGE, "The series would have " + count
                    + " buckets, more than " + MAX_BUCKETS + "; use bucket=1d or a shorter range.", List.of());
        }
        String group = groupBy == null ? "none" : groupBy;
        return series(new StatsQuery(involved(sources, kinds, excluded), kinds, bbox, restriction, start, end,
                resolution, measure, group, timeField), properties.queryTimeout());
    }

    /** {@code 1h}, {@code 1d}, or {@code auto}: hourly up to {@link #AUTO_HOURLY_RANGE}, else daily. */
    static RollupResolution bucket(String raw, Instant from, Instant to) {
        if (raw == null || raw.isBlank() || "auto".equalsIgnoreCase(raw.strip())) {
            return Duration.between(from, to).compareTo(AUTO_HOURLY_RANGE) <= 0 ? RollupResolution.HOUR
                    : RollupResolution.DAY;
        }
        return switch (raw.strip()) {
            case "1h" -> RollupResolution.HOUR;
            case "1d" -> RollupResolution.DAY;
            default -> throw ValidationException.forField("bucket", "must be 1h, 1d or auto");
        };
    }

    /** The series of a validated query, with an explicit statement timeout (tests inject a tiny one). */
    StatsSeries series(StatsQuery q, Duration timeout) {
        Instant now = clock.instant();
        Instant retainedFrom = retainedFrom(q.sources(), now);
        Map<Instant, Map<String, Long>> values = new HashMap<>();
        String backend = "raw";
        if (!q.sources().isEmpty()) {
            if (q.measure() == StatsMeasure.ALERTS) {
                runner.read(timeout, jdbc -> {
                    alerts(jdbc, q, false, values);
                    return null;
                });
            } else if (q.timeField() != TimeField.INGESTED) {
                if (retainedFrom != null && q.from().isBefore(retainedFrom)) {
                    throw ValidationException.forField("from", "timeField=" + q.timeField().wireValue()
                            + " reads raw history, which starts at " + retainedFrom
                            + " for these sources; use a later from or timeField=ingested");
                }
                runner.read(timeout, jdbc -> {
                    raw(jdbc, q, q.sources(), q.from(), q.to(), false, values);
                    return null;
                });
            } else {
                Plan plan = plan(q, q.bucket(), q.from(), q.to(), now);
                runner.read(timeout, jdbc -> {
                    execute(jdbc, q, plan, false, values);
                    return null;
                });
                backend = plan.backend();
            }
        }
        return toSeries(q, values, backend, retainedFrom);
    }

    private static StatsSeries toSeries(StatsQuery q, Map<Instant, Map<String, Long>> values, String backend,
            Instant retainedFrom) {
        List<Instant> buckets = new ArrayList<>();
        for (Instant t = q.from(); t.isBefore(q.to()); t = t.plus(q.bucket().width())) {
            buckets.add(t);
        }
        List<StatsSeries.Series> series = new ArrayList<>();
        if ("none".equals(q.groupBy())) {
            List<StatsSeries.Point> points = new ArrayList<>(buckets.size());
            for (Instant t : buckets) {
                long total = 0;
                for (long v : values.getOrDefault(t, Map.of()).values()) {
                    total += v;
                }
                points.add(new StatsSeries.Point(t, total));
            }
            series.add(new StatsSeries.Series(null, points));
        } else {
            Set<String> keys = new TreeSet<>();
            values.values().forEach(m -> keys.addAll(m.keySet()));
            for (String key : keys) {
                List<StatsSeries.Point> points = new ArrayList<>(buckets.size());
                for (Instant t : buckets) {
                    points.add(new StatsSeries.Point(t, values.getOrDefault(t, Map.of()).getOrDefault(key, 0L)));
                }
                series.add(new StatsSeries.Series(key, points));
            }
        }
        String groupBy = GROUP_PAIR.equals(q.groupBy()) ? "none" : q.groupBy();
        return new StatsSeries(q.from(), q.to(), q.bucket() == RollupResolution.DAY ? "1d" : "1h", q.measure().wire(),
                groupBy, q.timeField().wireValue(), backend, series, retainedFrom);
    }

    // =====================================================================================
    // Period totals (the briefing digest)
    // =====================================================================================

    /**
     * Totals of an arbitrary period on the ingested axis.
     *
     * @param values group key to measure to value
     * @param backend {@code rollup}, {@code raw} or {@code mixed}
     * @param complete false when a raw part of the period lies below the retention floor of one of
     *     its sources, so the totals are incomplete
     */
    public record Totals(Map<String, Map<StatsMeasure, Long>> values, String backend, boolean complete) {}

    /**
     * Totals over {@code [from, to)} on the ingested axis, grouped by {@code groupBy} (including
     * {@link #GROUP_PAIR}): whole closed hours from the rollup, the edges from raw rows (exact to the
     * batch through {@code seq_clock}).
     *
     * @param sources the involved sources (empty for every registered source)
     * @param measures the additive measures wanted ({@link StatsMeasure#ALERTS} not allowed)
     */
    public Totals totals(List<String> sources, List<String> kinds, BboxFilter bbox, ReadRestriction restriction,
            Instant from, Instant to, String groupBy, List<StatsMeasure> measures) {
        List<String> involved = sources == null || sources.isEmpty() ? List.copyOf(new TreeSet<>(registry.ids()))
                : List.copyOf(new TreeSet<>(sources));
        Map<String, Map<StatsMeasure, Long>> out = new TreeMap<>();
        if (involved.isEmpty() || !from.isBefore(to)) {
            return new Totals(out, "raw", true);
        }
        Instant now = clock.instant();
        boolean[] complete = {true};
        String[] backend = {"raw"};
        runner.read(properties.queryTimeout(), jdbc -> {
            for (StatsMeasure measure : measures) {
                StatsQuery q = new StatsQuery(involved, kinds, bbox, restriction, from, to, RollupResolution.HOUR,
                        measure, groupBy, TimeField.INGESTED);
                Plan plan = plan(q, RollupResolution.HOUR, from, to, now);
                backend[0] = plan.backend();
                Map<Instant, Map<String, Long>> values = new HashMap<>();
                execute(jdbc, q, plan, true, values);
                values.getOrDefault(from, Map.of()).forEach((key, value) ->
                        out.computeIfAbsent(key, k -> new LinkedHashMap<>()).merge(measure, value, Long::sum));
                if (measure == measures.getFirst()) {
                    complete[0] = rawRetained(jdbc, plan);
                }
            }
            return null;
        });
        return new Totals(out, backend[0], complete[0]);
    }

    /** True when every raw range of {@code plan} lies at or above the retention floor of its sources. */
    private boolean rawRetained(NamedParameterJdbcTemplate jdbc, Plan plan) {
        for (RawPart part : plan.raw()) {
            long lo = seqClock.lastSeqBefore(jdbc, part.from());
            for (String id : part.sources()) {
                if (lo + 1 < retention.lowestRetainedSeq(Set.of(id))) {
                    return false;
                }
            }
        }
        return true;
    }

    // =====================================================================================
    // Plans: which part comes from the rollup, which from raw history
    // =====================================================================================

    /** One raw range over some sources. */
    record RawPart(List<String> sources, Instant from, Instant to) {}

    /**
     * How a query over {@code [from, to)} is served.
     *
     * @param rollupSources the sources read from the rollup over {@code [rollupFrom, rollupTo)}
     * @param resolution the rollup resolution read
     * @param rollupFrom the rollup range start, or null when the rollup is not used
     * @param rollupTo the rollup range end, or null
     * @param raw the raw parts
     */
    record Plan(List<String> rollupSources, RollupResolution resolution, Instant rollupFrom, Instant rollupTo,
            List<RawPart> raw) {

        /** {@code rollup}, {@code raw} or {@code mixed}. */
        String backend() {
            boolean usesRollup = rollupFrom != null;
            if (usesRollup && raw.isEmpty()) {
                return "rollup";
            }
            return usesRollup ? "mixed" : "raw";
        }
    }

    /**
     * The plan of {@code q} over {@code [from, to)}: the rollup serves the whole {@code resolution}
     * buckets inside {@code [max(start, retention start), cursor end)} for the rolled-up sources; raw
     * history serves the rest.
     */
    Plan plan(StatsQuery q, RollupResolution resolution, Instant from, Instant to, Instant now) {
        List<String> rolled = new ArrayList<>();
        List<String> rawOnly = new ArrayList<>();
        RollupCursor cursor = rollup.cursor();
        Set<String> rolledUp = cursor != null && q.rollupUsable() ? Set.copyOf(statsSettings.rolledUpSources())
                : Set.of();
        for (String id : q.sources()) {
            (rolledUp.contains(id) ? rolled : rawOnly).add(id);
        }
        Instant rs = null;
        Instant re = null;
        if (!rolled.isEmpty()) {
            Instant start = latest(resolution.ceil(from), cursor.start(resolution),
                    resolution.ceil(now.minus(window(resolution))));
            Instant end = earliest(resolution.floor(to), cursor.end(resolution));
            if (start.isBefore(end)) {
                rs = start;
                re = end;
            }
        }
        List<RawPart> raw = new ArrayList<>();
        if (!rawOnly.isEmpty()) {
            raw.add(new RawPart(List.copyOf(rawOnly), from, to));
        }
        if (!rolled.isEmpty()) {
            if (rs == null) {
                raw.add(new RawPart(List.copyOf(rolled), from, to));
            } else {
                if (from.isBefore(rs)) {
                    raw.add(new RawPart(List.copyOf(rolled), from, rs));
                }
                if (re.isBefore(to)) {
                    raw.add(new RawPart(List.copyOf(rolled), re, to));
                }
            }
        }
        return new Plan(rs == null ? List.of() : List.copyOf(rolled), resolution, rs, re, List.copyOf(raw));
    }

    private void execute(NamedParameterJdbcTemplate jdbc, StatsQuery q, Plan plan, boolean single,
            Map<Instant, Map<String, Long>> values) {
        if (plan.rollupFrom() != null) {
            rollup(jdbc, q, plan.rollupSources(), plan.resolution(), plan.rollupFrom(), plan.rollupTo(), single, values);
        }
        for (RawPart part : plan.raw()) {
            raw(jdbc, q, part.sources(), part.from(), part.to(), single, values);
        }
    }

    /** The rollups' retention window of {@code resolution}. */
    private Duration window(RollupResolution resolution) {
        RetentionSettings.Stats stats = settings.getOrDefault(SettingKeys.RETENTION, RetentionSettings.class,
                RetentionSettings::defaults).stats();
        return resolution == RollupResolution.DAY ? stats.daily() : stats.hourly();
    }

    // =====================================================================================
    // SQL: rollup, raw, alerts
    // =====================================================================================

    private static String group(String groupBy, String sourceColumn, String kindColumn) {
        return switch (groupBy) {
            case "source" -> sourceColumn;
            case "kind" -> kindColumn;
            case GROUP_PAIR -> sourceColumn + " || chr(31) || " + kindColumn;
            default -> "CAST(NULL AS text)";
        };
    }

    private void rollup(NamedParameterJdbcTemplate jdbc, StatsQuery q, List<String> sources,
            RollupResolution resolution, Instant from, Instant to, boolean single,
            Map<Instant, Map<String, Long>> values) {
        SqlParams p = new SqlParams();
        List<String> where = rollupWhere(q, sources, resolution, from, to, p);
        String g = group(q.groupBy(), "r.source_id", "r.kind");
        String sql = single
                ? "SELECT " + g + " AS g, sum(r." + q.measure().column() + ") AS n FROM activity_rollup r WHERE "
                        + String.join(" AND ", where) + " GROUP BY 1"
                : "SELECT r.bucket_start AS t, " + g + " AS g, sum(r." + q.measure().column()
                        + ") AS n FROM activity_rollup r WHERE " + String.join(" AND ", where) + " GROUP BY 1, 2";
        collect(jdbc, sql, p, single ? q.from() : null, values);
    }

    /** The rollup filters: resolution, range, sources, kinds, the cell mode, bbox and restriction. */
    static List<String> rollupWhere(StatsQuery q, Collection<String> sources, RollupResolution resolution,
            Instant from, Instant to, SqlParams p) {
        List<String> where = new ArrayList<>();
        where.add("r.resolution = " + p.add(resolution.code()));
        where.add("r.bucket_start >= " + p.instant(from) + " AND r.bucket_start < " + p.instant(to));
        where.add("r.source_id = ANY(" + p.textArray(sources) + ")");
        if (!q.kinds().isEmpty()) {
            where.add("r.kind = ANY(" + p.textArray(q.kinds()) + ")");
        }
        if (q.spatial()) {
            where.add("r.cell <> " + H3Cells.ALL_CELLS);
            if (q.bbox() != null) {
                where.add(RecordQueryService.bboxClause(ROLLUP_GEOMETRY, q.bbox(), p));
            }
        } else {
            where.add("r.cell = " + H3Cells.ALL_CELLS);
        }
        RestrictionClamp.sqlPredicate("r.source_id", "r.kind", ROLLUP_GEOMETRY, null, q.restriction(), p)
                .ifPresent(where::add);
        return where;
    }

    private void raw(NamedParameterJdbcTemplate jdbc, StatsQuery q, List<String> sources, Instant from, Instant to,
            boolean single, Map<Instant, Map<String, Long>> values) {
        if (sources.isEmpty() || !from.isBefore(to)) {
            return;
        }
        SqlParams p = new SqlParams();
        List<String> where = rawWhere(q, sources, p);
        String axis = switch (q.timeField()) {
            case OBSERVED -> "v.observed_at";
            case EFFECTIVE -> "COALESCE(v.event_time, v.observed_at)";
            case EVENT -> "v.event_time";
            case INGESTED -> "v.ingested_at";
        };
        if (q.timeField() == TimeField.INGESTED) {
            long lo = seqClock.lastSeqBefore(jdbc, from);
            long hi = seqClock.lastSeqBefore(jdbc, to);
            if (hi <= lo) {
                return;
            }
            where.add("v.seq > " + p.bigint(lo) + " AND v.seq <= " + p.bigint(hi));
        } else {
            where.add(axis + " >= " + p.instant(from) + " AND " + axis + " < " + p.instant(to));
        }
        String g = group(q.groupBy(), "v.source_id", "v.kind");
        String sql = single
                ? "SELECT " + g + " AS g, " + q.measure().rawSql() + " AS n FROM record_version v WHERE "
                        + String.join(" AND ", where) + " GROUP BY 1"
                : "SELECT date_bin(CAST(" + p.add(q.bucket().width().toMillis() + " milliseconds") + " AS interval), "
                        + axis + ", " + p.instant(q.from()) + ") AS t, " + g + " AS g, " + q.measure().rawSql()
                        + " AS n FROM record_version v WHERE " + String.join(" AND ", where) + " GROUP BY 1, 2";
        collect(jdbc, sql, p, single ? q.from() : null, values);
    }

    /** The raw filters: sources, kinds, routine versions excluded, bbox and restriction (with the payload). */
    static List<String> rawWhere(StatsQuery q, Collection<String> sources, SqlParams p) {
        List<String> where = new ArrayList<>();
        where.add("v.source_id = ANY(" + p.textArray(sources) + ")");
        if (!q.kinds().isEmpty()) {
            where.add("v.kind = ANY(" + p.textArray(q.kinds()) + ")");
        }
        where.add("v.persist_reason NOT IN (" + RollupComputer.SKIPPED_REASONS + ")");
        if (q.bbox() != null) {
            where.add(RecordQueryService.bboxClause("v.geom", q.bbox(), p));
        }
        RestrictionClamp.sqlPredicate("v.source_id", "v.kind", "v.geom", "v.payload", q.restriction(), p)
                .ifPresent(where::add);
        if (q.restriction().regionFilter() != null) {
            // country=, admin1=, county= (prompt 040): the entity's current tags in record_region.
            where.add(q.restriction().regionFilter().sql("v.source_id", "v.external_id", p::textArray));
        }
        return where;
    }

    private void alerts(NamedParameterJdbcTemplate jdbc, StatsQuery q, boolean single,
            Map<Instant, Map<String, Long>> values) {
        SqlParams p = new SqlParams();
        List<String> where = alertWhere(q, q.from(), q.to(), p);
        String g = group(q.groupBy(), "a.source_id", "a.kind");
        String sql = single
                ? "SELECT " + g + " AS g, count(*) AS n FROM alert a WHERE " + String.join(" AND ", where)
                        + " GROUP BY 1"
                : "SELECT date_bin(CAST(" + p.add(q.bucket().width().toMillis() + " milliseconds")
                        + " AS interval), a.fired_at, " + p.instant(q.from()) + ") AS t, " + g
                        + " AS g, count(*) AS n FROM alert a WHERE " + String.join(" AND ", where) + " GROUP BY 1, 2";
        collect(jdbc, sql, p, single ? q.from() : null, values);
    }

    /** Alerts fired in {@code [from, to)} under the source, kind, bbox and restriction filters; suppressed ones excluded. */
    static List<String> alertWhere(StatsQuery q, Instant from, Instant to, SqlParams p) {
        List<String> where = new ArrayList<>();
        where.add("a.fired_at >= " + p.instant(from) + " AND a.fired_at < " + p.instant(to));
        where.add("NOT a.suppressed");
        where.add("a.source_id = ANY(" + p.textArray(q.sources()) + ")");
        if (!q.kinds().isEmpty()) {
            where.add("a.kind = ANY(" + p.textArray(q.kinds()) + ")");
        }
        if (q.bbox() != null) {
            where.add(RecordQueryService.bboxClause(RestrictionClamp.ALERT_GEOMETRY_SQL, q.bbox(), p));
        }
        RestrictionClamp.sqlPredicate("a.source_id", "a.kind", RestrictionClamp.ALERT_GEOMETRY_SQL,
                "(a.record_snapshot -> 'payload')", q.restriction(), p).ifPresent(where::add);
        if (q.restriction().regionFilter() != null) {
            // country=, admin1=, county= (prompt 040): the region snapshot taken when the alert fired.
            where.add(q.restriction().regionFilter().jsonSql("a.region", p::textArray));
        }
        return where;
    }

    private static void collect(NamedParameterJdbcTemplate jdbc, String sql, SqlParams p, Instant single,
            Map<Instant, Map<String, Long>> values) {
        jdbc.query(sql, p.source(), rs -> {
            Instant t = single != null ? single : rs.getObject("t", OffsetDateTime.class).toInstant();
            String g = rs.getString("g");
            values.computeIfAbsent(t, k -> new HashMap<>()).merge(g == null ? "" : g, rs.getLong("n"), Long::sum);
        });
    }

    // =====================================================================================
    // Grid
    // =====================================================================================

    /**
     * {@code getStatsGrid}.
     *
     * @param sources requested sources
     * @param kinds requested kinds
     * @param bbox the area (required)
     * @param from start
     * @param to end
     * @param measure the measure
     * @param res the H3 resolution (≤ {@code cellRes}; null for {@code cellRes})
     * @param mode {@code rollup} (default) or {@code raw}
     * @param sizeKm the raw hexagon size (raw mode)
     * @param excluded sources the caller may not see
     * @param restriction the caller's restriction
     * @throws ValidationException {@code STATS_TOO_LARGE} above {@value #MAX_CELLS} cells
     */
    public StatsGrid grid(List<String> sources, List<String> kinds, BboxFilter bbox, Instant from, Instant to,
            StatsMeasure measure, Integer res, String mode, Double sizeKm, Set<String> excluded,
            ReadRestriction restriction) {
        return grid(sources, kinds, bbox, from, to, measure, res, mode, sizeKm, excluded, restriction,
                properties.queryTimeout());
    }

    StatsGrid grid(List<String> sources, List<String> kinds, BboxFilter bbox, Instant from, Instant to,
            StatsMeasure measure, Integer res, String mode, Double sizeKm, Set<String> excluded,
            ReadRestriction restriction, Duration timeout) {
        if (bbox == null) {
            throw ValidationException.forField("bbox", "is required");
        }
        range(from, to);
        List<String> involved = involved(sources, kinds, excluded);
        if ("raw".equals(mode)) {
            return rawGrid(involved, kinds, bbox, from, to, measure, sizeKm, restriction, timeout);
        }
        if (measure == StatsMeasure.ALERTS) {
            throw ValidationException.forField("measure", "alerts are not rolled up; use mode=raw");
        }
        int resolution = res == null ? properties.cellRes() : res;
        if (resolution < 0 || resolution > properties.cellRes()) {
            throw ValidationException.forField("res", "must be between 0 and cellRes (" + properties.cellRes() + ")");
        }
        RollupResolution bucket = Duration.between(from, to).compareTo(AUTO_HOURLY_RANGE) > 0 ? RollupResolution.DAY
                : RollupResolution.HOUR;
        Map<Long, Long> byCell = new HashMap<>();
        RollupCursor cursor = rollup.cursor();
        List<String> rolled = new ArrayList<>(involved);
        rolled.retainAll(statsSettings.rolledUpSources());
        StatsQuery q = new StatsQuery(rolled, kinds, bbox, restriction, from, to, bucket, measure, "none",
                TimeField.INGESTED);
        if (cursor != null && !rolled.isEmpty() && q.rollupUsable()) {
            Instant start = latest(bucket.floor(from), cursor.start(bucket));
            Instant end = earliest(bucket.ceil(to), cursor.end(bucket));
            if (start.isBefore(end)) {
                runner.read(timeout, jdbc -> {
                    SqlParams p = new SqlParams();
                    List<String> where = rollupWhere(q, rolled, bucket, start, end, p);
                    where.add("r.cell > 0");
                    jdbc.query("SELECT r.cell, sum(r." + measure.column() + ") AS n FROM activity_rollup r WHERE "
                            + String.join(" AND ", where) + " GROUP BY r.cell", p.source(), rs -> {
                                long parent = cells.parent(rs.getLong(1), resolution);
                                byCell.merge(parent, rs.getLong(2), Long::sum);
                            });
                    return null;
                });
            }
        }
        if (byCell.size() > MAX_CELLS) {
            throw tooManyCells(byCell.size());
        }
        List<StatsGrid.Cell> out = new ArrayList<>(byCell.size());
        new TreeMap<>(byCell).forEach((cell, value) -> out.add(new StatsGrid.Cell(cells.address(cell),
                cells.boundary(cell), value)));
        return new StatsGrid("h3", resolution, null, measure.wire(), "rollup", out);
    }

    private StatsGrid rawGrid(List<String> involved, List<String> kinds, BboxFilter bbox, Instant from, Instant to,
            StatsMeasure measure, Double sizeKm, ReadRestriction restriction, Duration timeout) {
        if (sizeKm == null || !(sizeKm > 0)) {
            throw ValidationException.forField("sizeKm", "is required in mode=raw and must be positive");
        }
        if (Duration.between(from, to).compareTo(MAX_RAW_GRID_RANGE) > 0) {
            throw ValidationException.forField("to", "a raw grid covers at most 7 days");
        }
        double centerLat = 0;
        double minLat = Double.POSITIVE_INFINITY;
        double maxLat = Double.NEGATIVE_INFINITY;
        for (Envelope e : bbox.envelopes()) {
            minLat = Math.min(minLat, e.getMinY());
            maxLat = Math.max(maxLat, e.getMaxY());
        }
        centerLat = Math.max(-MERCATOR_LIMIT, Math.min(MERCATOR_LIMIT, (minLat + maxLat) / 2));
        // A ground size at the bbox's centre latitude, as a Web Mercator size: hexagons stay regular on
        // the map (no distortion at 60°N) and are sizeKm across the ground at that latitude.
        double size = sizeKm * 1000 / Math.cos(Math.toRadians(centerLat));
        double hexArea = 1.5 * Math.sqrt(3) * size * size;
        double generated = 0;
        for (Envelope e : bbox.envelopes()) {
            generated += mercatorArea(e) / hexArea;
        }
        if (generated > MAX_GENERATED_HEXAGONS) {
            throw new ValidationException(ErrorCodes.STATS_TOO_LARGE, "The bbox holds about " + (long) generated
                    + " hexagons of " + sizeKm + " km; use a larger sizeKm or a smaller bbox.", List.of());
        }
        Map<String, StatsGrid.Cell> byHex = new TreeMap<>();
        if (!involved.isEmpty()) {
            StatsQuery q = new StatsQuery(involved, kinds, null, restriction, from, to, RollupResolution.HOUR, measure,
                    "none", TimeField.INGESTED);
            runner.read(timeout, jdbc -> {
                for (Envelope e : bbox.envelopes()) {
                    SqlParams p = new SqlParams();
                    String points = rawPoints(jdbc, q, e, p);
                    if (points == null) {
                        continue;
                    }
                    String envelope = "ST_Transform(ST_MakeEnvelope(" + p.dbl(e.getMinX()) + ", "
                            + p.dbl(Math.max(-MERCATOR_LIMIT, e.getMinY())) + ", " + p.dbl(e.getMaxX()) + ", "
                            + p.dbl(Math.min(MERCATOR_LIMIT, e.getMaxY())) + ", 4326), 3857)";
                    String measureSql = measure == StatsMeasure.ALERTS ? "count(*)"
                            : measure.rawSql().replace("v.", "pts.");
                    String sql = "WITH pts AS (" + points + "), hex AS (SELECT h.geom, h.i, h.j FROM ST_HexagonGrid("
                            + p.dbl(size) + ", " + envelope + ") h)"
                            + " SELECT hex.i, hex.j, ST_AsGeoJSON(ST_Transform(hex.geom, 4326), 7) AS g, " + measureSql
                            + " AS n FROM hex JOIN pts ON ST_Intersects(hex.geom, pts.p)"
                            + " GROUP BY hex.i, hex.j, hex.geom";
                    jdbc.query(sql, p.source(), rs -> {
                        String id = rs.getInt(1) + "," + rs.getInt(2);
                        long n = rs.getLong(4);
                        StatsGrid.Cell existing = byHex.get(id);
                        if (existing == null) {
                            byHex.put(id, new StatsGrid.Cell(id, geometry(rs.getString(3)), n));
                        } else {
                            byHex.put(id, new StatsGrid.Cell(id, existing.geometry(), existing.value() + n));
                        }
                    });
                }
                return null;
            });
        }
        if (byHex.size() > MAX_CELLS) {
            throw tooManyCells(byHex.size());
        }
        return new StatsGrid("hex3857", null, sizeKm, measure.wire(), "raw", List.copyOf(byHex.values()));
    }

    /** The points of a raw grid in EPSG:3857: version centroids by seq range, or alert geometries. */
    private String rawPoints(NamedParameterJdbcTemplate jdbc, StatsQuery q, Envelope e, SqlParams p) {
        BboxFilter box = new BboxFilter(List.of(e));
        if (q.measure() == StatsMeasure.ALERTS) {
            List<String> where = alertWhere(new StatsQuery(q.sources(), q.kinds(), box, q.restriction(), q.from(),
                    q.to(), q.bucket(), q.measure(), "none", TimeField.INGESTED), q.from(), q.to(), p);
            return "SELECT ST_Transform(ST_Centroid(" + RestrictionClamp.ALERT_GEOMETRY_SQL + "), 3857) AS p"
                    + " FROM alert a WHERE " + String.join(" AND ", where);
        }
        long lo = seqClock.lastSeqBefore(jdbc, q.from());
        long hi = seqClock.lastSeqBefore(jdbc, q.to());
        if (hi <= lo) {
            return null;
        }
        List<String> where = rawWhere(new StatsQuery(q.sources(), q.kinds(), box, q.restriction(), q.from(), q.to(),
                q.bucket(), q.measure(), "none", TimeField.INGESTED), q.sources(), p);
        where.add("v.seq > " + p.bigint(lo) + " AND v.seq <= " + p.bigint(hi));
        where.add("ST_Y(ST_Centroid(v.geom)) BETWEEN " + -MERCATOR_LIMIT + " AND " + MERCATOR_LIMIT);
        return "SELECT ST_Transform(ST_Centroid(v.geom), 3857) AS p, v.source_id, v.external_id, v.persist_reason"
                + " FROM record_version v WHERE " + String.join(" AND ", where);
    }

    /** The area of {@code e} (WGS84) in square Web Mercator meters. */
    static double mercatorArea(Envelope e) {
        double r = 6378137.0;
        double width = Math.toRadians(e.getMaxX() - e.getMinX()) * r;
        double y1 = mercatorY(Math.max(-MERCATOR_LIMIT, e.getMinY()), r);
        double y2 = mercatorY(Math.min(MERCATOR_LIMIT, e.getMaxY()), r);
        return Math.abs(width * (y2 - y1));
    }

    private static double mercatorY(double lat, double r) {
        return r * Math.log(Math.tan(Math.PI / 4 + Math.toRadians(lat) / 2));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> geometry(String geoJson) {
        return json.readValue(geoJson, Map.class);
    }

    private static ValidationException tooManyCells(int n) {
        return new ValidationException(ErrorCodes.STATS_TOO_LARGE, "The grid would have " + n + " cells, more than "
                + MAX_CELLS + "; use a coarser res, a larger sizeKm or a smaller bbox.", List.of());
    }

    // =====================================================================================
    // Top
    // =====================================================================================

    /**
     * {@code getStatsTop}.
     *
     * @param by {@code source}, {@code kind}, {@code cell} or {@code entity}
     * @param measure the measure
     * @param from start
     * @param to end
     * @param limit 1–{@value #MAX_TOP}
     * @param sources requested sources
     * @param kinds requested kinds
     * @param excluded sources the caller may not see
     * @param restriction the caller's restriction
     */
    public StatsTop top(String by, StatsMeasure measure, Instant from, Instant to, int limit, List<String> sources,
            List<String> kinds, Set<String> excluded, ReadRestriction restriction) {
        range(from, to);
        List<String> involved = involved(sources, kinds, excluded);
        Instant now = clock.instant();
        if (involved.isEmpty()) {
            return new StatsTop(by, measure.wire(), from, to, "raw", List.of());
        }
        StatsQuery base = new StatsQuery(involved, kinds, null, restriction, from, to, RollupResolution.HOUR, measure,
                "none", TimeField.INGESTED);
        if ("entity".equals(by)) {
            return topEntities(base, limit, now);
        }
        if (measure == StatsMeasure.ALERTS) {
            if ("cell".equals(by)) {
                throw ValidationException.forField("by", "alerts are ranked by source, kind or entity");
            }
            Map<Instant, Map<String, Long>> values = new HashMap<>();
            runner.read(properties.queryTimeout(), jdbc -> {
                alerts(jdbc, new StatsQuery(involved, kinds, null, restriction, from, to, RollupResolution.HOUR,
                        measure, by, TimeField.INGESTED), true, values);
                return null;
            });
            return new StatsTop(by, measure.wire(), from, to, "raw", rank(by, values.getOrDefault(from, Map.of()), limit));
        }
        if ("cell".equals(by)) {
            return topCells(base, limit, now);
        }
        StatsQuery q = new StatsQuery(involved, kinds, null, restriction, from, to, RollupResolution.HOUR, measure, by,
                TimeField.INGESTED);
        Plan plan = plan(q, RollupResolution.HOUR, from, to, now);
        Map<Instant, Map<String, Long>> values = new HashMap<>();
        runner.read(properties.queryTimeout(), jdbc -> {
            execute(jdbc, q, plan, true, values);
            return null;
        });
        return new StatsTop(by, measure.wire(), from, to, plan.backend(),
                rank(by, values.getOrDefault(from, Map.of()), limit));
    }

    private static List<StatsTop.Item> rank(String by, Map<String, Long> values, int limit) {
        List<Map.Entry<String, Long>> entries = new ArrayList<>(values.entrySet());
        entries.removeIf(e -> e.getValue() == 0);
        entries.sort(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()));
        List<StatsTop.Item> out = new ArrayList<>();
        for (Map.Entry<String, Long> e : entries.subList(0, Math.min(limit, entries.size()))) {
            out.add(new StatsTop.Item(e.getKey(), "source".equals(by) ? e.getKey() : null,
                    "kind".equals(by) ? e.getKey() : null, null, null, null, e.getValue()));
        }
        return out;
    }

    private StatsTop topCells(StatsQuery base, int limit, Instant now) {
        if (!base.rollupUsable()) {
            throw ValidationException.forField("by", "by=cell cannot be combined with a view or watchlist filter,"
                    + " whose conditions need the payload, or with a region filter (the rollup has no regions until"
                    + " prompt 119)");
        }
        StatsQuery q = new StatsQuery(base.sources(), base.kinds(), null, base.restriction(), base.from(), base.to(),
                RollupResolution.HOUR, base.measure(), "none", TimeField.INGESTED);
        Plan plan = plan(q, RollupResolution.HOUR, base.from(), base.to(), now);
        Map<Long, Long> byCell = new HashMap<>();
        runner.read(properties.queryTimeout(), jdbc -> {
            if (plan.rollupFrom() != null) {
                SqlParams p = new SqlParams();
                List<String> where = rollupWhere(q, plan.rollupSources(), RollupResolution.HOUR,
                        plan.rollupFrom(), plan.rollupTo(), p);
                where.removeIf(w -> w.equals("r.cell = " + H3Cells.ALL_CELLS));
                where.add("r.cell > 0");
                jdbc.query("SELECT r.cell, sum(r." + q.measure().column() + ") FROM activity_rollup r WHERE "
                        + String.join(" AND ", where) + " GROUP BY r.cell", p.source(),
                        rs -> {
                            byCell.merge(rs.getLong(1), rs.getLong(2), Long::sum);
                        });
            }
            RollupComputer computer = new RollupComputer(cells, properties.cellRes());
            for (RawPart part : plan.raw()) {
                long lo = seqClock.lastSeqBefore(jdbc, part.from());
                long hi = seqClock.lastSeqBefore(jdbc, part.to());
                for (RollupComputer.Row row : computer.compute(jdbc, lo, hi, part.sources())) {
                    // Raw rows judged at cell resolution, like the rollup rows they complete.
                    if (row.cell() > 0 && (q.kinds().isEmpty() || q.kinds().contains(row.kind()))
                            && RestrictionClamp.test(q.restriction(), row.sourceId(), row.kind(),
                                    point(row.lon(), row.lat()))) {
                        byCell.merge(row.cell(), value(row, q.measure()), Long::sum);
                    }
                }
            }
            return null;
        });
        List<Map.Entry<Long, Long>> entries = new ArrayList<>(byCell.entrySet());
        entries.removeIf(e -> e.getValue() == 0);
        entries.sort(Map.Entry.<Long, Long>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()));
        List<StatsTop.Item> items = new ArrayList<>();
        for (Map.Entry<Long, Long> e : entries.subList(0, Math.min(limit, entries.size()))) {
            String address = cells.address(e.getKey());
            items.add(new StatsTop.Item(address, null, null, address, null, null, e.getValue()));
        }
        return new StatsTop("cell", q.measure().wire(), q.from(), q.to(), plan.backend(), items);
    }

    private static org.locationtech.jts.geom.Geometry point(Double lon, Double lat) {
        if (lon == null || lat == null) {
            return null;
        }
        return new org.locationtech.jts.geom.GeometryFactory(new org.locationtech.jts.geom.PrecisionModel(), 4326)
                .createPoint(new org.locationtech.jts.geom.Coordinate(lon, lat));
    }

    private static long value(RollupComputer.Row row, StatsMeasure measure) {
        return switch (measure) {
            case VERSIONS -> row.versions();
            case ENTITIES -> row.entities();
            case FIRSTS -> row.firsts();
            case TOMBSTONES -> row.tombstones();
            case EDGES -> row.edges();
            case CHANGES -> row.changes();
            case ALERTS -> 0;
        };
    }

    private StatsTop topEntities(StatsQuery q, int limit, Instant now) {
        Instant retainedFrom = retainedFrom(q.sources(), now);
        if (retainedFrom != null && q.from().isBefore(retainedFrom)) {
            throw ValidationException.forField("from", "by=entity reads raw history, which starts at " + retainedFrom
                    + " for these sources");
        }
        List<StatsTop.Item> items = runner.read(properties.queryTimeout(), jdbc -> {
            SqlParams p = new SqlParams();
            String sql;
            if (q.measure() == StatsMeasure.ALERTS) {
                List<String> where = alertWhere(q, q.from(), q.to(), p);
                sql = "SELECT a.source_id, a.external_id, count(*) AS n FROM alert a WHERE "
                        + String.join(" AND ", where) + " GROUP BY 1, 2";
            } else {
                long lo = seqClock.lastSeqBefore(jdbc, q.from());
                long hi = seqClock.lastSeqBefore(jdbc, q.to());
                if (hi <= lo) {
                    return List.<StatsTop.Item>of();
                }
                List<String> where = rawWhere(q, q.sources(), p);
                where.add("v.seq > " + p.bigint(lo) + " AND v.seq <= " + p.bigint(hi));
                String measure = q.measure() == StatsMeasure.ENTITIES ? "count(DISTINCT v.external_id)"
                        : q.measure().rawSql();
                sql = "SELECT v.source_id, v.external_id, " + measure + " AS n FROM record_version v WHERE "
                        + String.join(" AND ", where) + " GROUP BY 1, 2";
            }
            String ranked = "SELECT x.source_id, x.external_id, x.n, h.title FROM (" + sql + ") x"
                    + " LEFT JOIN record_head h ON h.source_id = x.source_id AND h.external_id = x.external_id"
                    + " WHERE x.n > 0 ORDER BY x.n DESC, x.source_id, x.external_id LIMIT " + p.bigint(limit);
            return jdbc.query(ranked, p.source(), (rs, i) -> new StatsTop.Item(
                    rs.getString(1) + ":" + rs.getString(2), rs.getString(1), null, null, rs.getString(2),
                    rs.getString(4), rs.getLong(3)));
        });
        return new StatsTop("entity", q.measure().wire(), q.from(), q.to(), "raw", items);
    }

    // =====================================================================================
    // Glance
    // =====================================================================================

    /**
     * Versions ingested at or after {@code since} (HEARTBEAT and KEYFRAME excluded) of the sources the
     * caller may see, from raw history (the glance's {@code activity.versionsLastHour}).
     */
    public long versionsSince(Instant since, Set<String> excluded, ReadRestriction restriction) {
        List<String> involved = involved(List.of(), List.of(), excluded);
        if (involved.isEmpty()) {
            return 0;
        }
        StatsQuery q = new StatsQuery(involved, List.of(), null, restriction, since, clock.instant(),
                RollupResolution.HOUR, StatsMeasure.VERSIONS, "none", TimeField.INGESTED);
        return runner.read(properties.queryTimeout(), jdbc -> {
            SqlParams p = new SqlParams();
            List<String> where = rawWhere(q, involved, p);
            where.add("v.seq > " + p.bigint(seqClock.lastSeqBefore(jdbc, since)));
            Long n = jdbc.queryForObject("SELECT count(*) FROM record_version v WHERE " + String.join(" AND ", where),
                    p.source(), Long.class);
            return n == null ? 0L : n;
        });
    }

    // =====================================================================================
    // Helpers
    // =====================================================================================

    private static void range(Instant from, Instant to) {
        if (from == null || to == null) {
            throw ValidationException.forField(from == null ? "from" : "to", "is required");
        }
        if (!from.isBefore(to)) {
            throw ValidationException.forField("to", "must be after from");
        }
    }

    /**
     * The involved sources: the named ones (404 when unknown), else those declaring a named kind, else
     * every registered source; the caller's excluded sources removed; sorted.
     */
    public List<String> involved(List<String> sources, List<String> kinds, Set<String> excluded) {
        Set<String> exclude = excluded == null ? Set.of() : excluded;
        if ((sources != null && !sources.isEmpty()) || (kinds != null && !kinds.isEmpty())) {
            return asOf.involved(sources, kinds, exclude).stream().map(SourceDescriptor::id).toList();
        }
        Set<String> all = new TreeSet<>(registry.ids());
        all.removeAll(exclude);
        return List.copyOf(all);
    }

    /** The latest {@code now − window} over {@code sources}: where raw history is complete for all of them. */
    public Instant retainedFrom(Collection<String> sources, Instant now) {
        Instant out = null;
        for (String id : sources) {
            Duration window = retention.window(id);
            if (window != null) {
                Instant start = now.minus(window);
                if (out == null || start.isAfter(out)) {
                    out = start;
                }
            }
        }
        return out;
    }

    private static Instant latest(Instant... values) {
        return java.util.Arrays.stream(values).max(Comparator.naturalOrder()).orElseThrow();
    }

    private static Instant earliest(Instant... values) {
        return java.util.Arrays.stream(values).min(Comparator.naturalOrder()).orElseThrow();
    }
}
