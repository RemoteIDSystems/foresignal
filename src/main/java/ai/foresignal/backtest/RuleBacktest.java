package ai.foresignal.backtest;

import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.settings.TriggerSettings;
import ai.foresignal.store.TriggerRuleEntity;
import ai.foresignal.store.query.ReadQueryRunner;
import ai.foresignal.trigger.AlertPass;
import ai.foresignal.trigger.MatchRow;
import ai.foresignal.trigger.RuleCondition;
import ai.foresignal.trigger.RuleSpec;
import ai.foresignal.trigger.RuleSqlBuilder;
import ai.foresignal.trigger.TriggerRuleService;
import ai.foresignal.trigger.window.RuleType;
import ai.foresignal.trigger.window.WindowSpec;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;

/**
 * A rule backtest (prompt 103 §3).
 *
 * <p><strong>Record rules</strong> (and, as a record-level approximation, count and absence rules):
 * replays {@link RuleSqlBuilder} per 50k-seq chunk, each chunk in its own read transaction with a
 * 60 s statement timeout, through the live engine's {@link AlertPass} in simulate mode, so
 * {@code wouldFire} reproduces the alerts the engine fired. The job is checked for cancellation and
 * its progress recorded between chunks.
 *
 * <p><strong>No look-ahead for forecast-horizon rules.</strong> The live engine evaluates
 * {@code valid_from ≤ now + horizon} at the moment it processes a version, which is that version's
 * ingest time. The replay therefore runs each chunk's statement at the chunk's latest ingest time
 * (a superset) and then keeps only rows with {@code valid_from ≤ ingested_at + horizon} for their
 * own {@code ingested_at}; it never uses the wall clock of the replay.
 *
 * <p><strong>Anomaly rules</strong> are replayed bucket by bucket from the stored counts with
 * {@link AnomalyReplay} (baselines from earlier buckets only).
 *
 * <p>Reports {@code wouldFire}, a per-day histogram, the top entities, sample hits, the actual
 * alerts in range and their overlap by seq, and optional variants side by side.
 */
@Service
public class RuleBacktest {

    /** Seqs per chunk. */
    public static final long CHUNK = 50_000;

    /** The most samples kept. */
    public static final int MAX_SAMPLES = 200;

    /** The most entities kept. */
    public static final int MAX_ENTITIES = 20;

    /** The most variants. */
    public static final int MAX_VARIANTS = 5;

    /** The most fired seqs remembered for the overlap; beyond it the overlap is estimated from counts. */
    public static final int MAX_OVERLAP_SEQS = 1_000_000;

    private final TriggerRuleService rules;
    private final ai.foresignal.trigger.RuleSpecs specs;
    private final ReadQueryRunner reads;
    private final AppSettingService settings;

    /** Creates the service. */
    public RuleBacktest(TriggerRuleService rules, ai.foresignal.trigger.RuleSpecs specs, ReadQueryRunner reads,
            AppSettingService settings) {
        this.rules = rules;
        this.specs = specs;
        this.reads = reads;
        this.settings = settings;
    }

    /** Runs a rule backtest over {@code (fromSeq, toSeq]} with optional variants and no job monitor. */
    public Map<String, Object> run(UUID ruleId, long fromSeq, long toSeq, List<Map<String, Object>> variants) {
        return run(ruleId, fromSeq, toSeq, null, null, variants, JobMonitor.NONE);
    }

    /**
     * Runs a rule backtest.
     *
     * @param ruleId the rule
     * @param fromSeq exclusive lower seq bound
     * @param toSeq inclusive upper seq bound
     * @param rangeFrom the start of the range in time (anomaly rules), or null
     * @param rangeTo the end of the range in time (anomaly rules), or null
     * @param variants up to {@value #MAX_VARIANTS} variants
     * @param monitor the job's monitor
     */
    public Map<String, Object> run(UUID ruleId, long fromSeq, long toSeq, Instant rangeFrom, Instant rangeTo,
            List<Map<String, Object>> variants, JobMonitor monitor) {
        TriggerRuleEntity entity = rules.load(ruleId);
        RuleType type = RuleType.fromWire(entity.getType());
        List<Map<String, Object>> chosen = variants == null ? List.of()
                : variants.subList(0, Math.min(variants.size(), MAX_VARIANTS));
        if (type == RuleType.ANOMALY) {
            return anomaly(entity, rangeFrom, rangeTo, chosen, monitor);
        }
        RuleSpec rule = specs.compile(entity);
        int passes = 1 + chosen.size();
        Result base = evaluate(rule, fromSeq, toSeq, monitor, 0, passes, true);
        Map<String, Object> result = base.toMap();
        result.put("ruleType", type.wire());
        if (type.windowed()) {
            result.put("windowedReplay", "record-level approximation: versions matching the rule's filter,"
                    + " not window states");
        }
        Set<Long> actualSeqs = actualAlertSeqs(ruleId, fromSeq, toSeq);
        long actual = actualSeqs.size();
        result.put("actual", actual(actual, alertsBefore(ruleId, fromSeq, toSeq, entity.getUpdatedAt())));
        if (base.firedSeqs != null) {
            long both = 0;
            for (Long seq : actualSeqs) {
                if (base.firedSeqs.contains(seq)) {
                    both++;
                }
            }
            result.put("overlap", Map.of("both", both, "onlyBacktest", base.fired - both,
                    "onlyActual", actual - both));
        } else {
            long both = Math.min(base.fired, actual);
            result.put("overlap", Map.of("both", both, "onlyBacktest", base.fired - both,
                    "onlyActual", actual - both, "estimated", true));
        }
        if (!chosen.isEmpty()) {
            List<Map<String, Object>> variantResults = new ArrayList<>();
            for (int i = 0; i < chosen.size(); i++) {
                Map<String, Object> variant = chosen.get(i);
                RuleSpec patched = patch(rule, variant);
                Map<String, Object> entry = evaluate(patched, fromSeq, toSeq, monitor, i + 1, passes, false).toMap();
                identify(entry, i, variant);
                variantResults.add(entry);
            }
            result.put("variants", variantResults);
        }
        return result;
    }

    private Result evaluate(RuleSpec rule, long fromSeq, long toSeq, JobMonitor monitor, int pass, int passes,
            boolean rememberSeqs) {
        int defaultCap = settings.getOrDefault(SettingKeys.TRIGGERS, TriggerSettings.class, TriggerSettings::defaults)
                .defaultMaxAlertsPerHour();
        AlertPass.State state = new AlertPass.State();
        state.seedStorm(List.of());
        long fired = 0;
        long suppressed = 0;
        Map<String, Integer> byDay = new TreeMap<>();
        Map<String, Integer> byEntity = new LinkedHashMap<>();
        List<Map<String, Object>> samples = new ArrayList<>();
        Set<Long> firedSeqs = rememberSeqs ? new HashSet<>() : null;
        long span = Math.max(1, toSeq - fromSeq);
        for (long lo = fromSeq; lo < toSeq; lo += CHUNK) {
            monitor.checkpoint((pass + (double) (lo - fromSeq) / span) / passes);
            long hi = Math.min(toSeq, lo + CHUNK);
            List<MatchRow> rows = matches(rule, lo, hi);
            if (rows == null) {
                continue;
            }
            Instant decision = rows.isEmpty() ? null : rows.getLast().observedAt();
            AlertPass.Result result = AlertPass.run(rule, rows, List.of(), state,
                    new AlertPass.Context(decision == null ? Instant.EPOCH : decision, null, defaultCap, true, null));
            suppressed += result.cooldownSuppressed() + result.windowSkipped() + result.stormSuppressed();
            for (AlertPass.Op op : result.ops()) {
                if (op instanceof AlertPass.Fire fire && fire.suppressedReason() == null) {
                    fired++;
                    MatchRow row = fire.row();
                    if (firedSeqs != null) {
                        if (firedSeqs.size() < MAX_OVERLAP_SEQS) {
                            firedSeqs.add(row.seq());
                        } else {
                            firedSeqs = null;
                        }
                    }
                    String day = row.observedAt() == null ? "unknown"
                            : row.observedAt().atZone(ZoneOffset.UTC).toLocalDate().toString();
                    byDay.merge(day, 1, Integer::sum);
                    byEntity.merge(row.sourceId() + ":" + row.externalId(), 1, Integer::sum);
                    if (samples.size() < MAX_SAMPLES) {
                        samples.add(Map.of("seq", row.seq(), "source", row.sourceId(), "externalId",
                                row.externalId(), "observedAt", String.valueOf(row.observedAt()),
                                "edge", row.edge() == null ? "match" : row.edge()));
                    }
                }
            }
            if (result.disable()) {
                break;
            }
        }
        List<Map<String, Object>> top = byEntity.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(MAX_ENTITIES)
                .map(e -> Map.<String, Object>of("entity", e.getKey(), "count", e.getValue()))
                .toList();
        return new Result(fired, suppressed, byDay, top, samples, firedSeqs);
    }

    /**
     * The match rows of one chunk {@code (lo, hi]}, as the live engine would have selected them when
     * it processed each version (see the class comment on forecast-horizon rules).
     */
    private List<MatchRow> matches(RuleSpec rule, long lo, long hi) {
        Duration horizon = rule.forecastHorizon();
        return reads.read(BacktestCoverage.STATEMENT_TIMEOUT, t -> {
            Instant evalTime = Instant.EPOCH;
            if (horizon != null) {
                OffsetDateTime latest = t.getJdbcTemplate().queryForObject(
                        "SELECT max(ingested_at) FROM record_version WHERE seq > ? AND seq <= ?",
                        OffsetDateTime.class, lo, hi);
                if (latest == null) {
                    return List.<MatchRow>of();
                }
                evalTime = latest.toInstant();
            }
            RuleSqlBuilder.RuleSql sql = RuleSqlBuilder.matches(rule, lo, hi, evalTime, false);
            List<MatchRow> rows = t.query(sql.sql(), sql.params(), MatchRow.MAPPER);
            if (horizon == null || rows.isEmpty()) {
                return rows;
            }
            List<Long> seqs = rows.stream().filter(r -> r.validFrom() != null).map(MatchRow::seq).toList();
            Map<Long, Instant> ingested = new HashMap<>();
            if (!seqs.isEmpty()) {
                t.query("SELECT seq, ingested_at FROM record_version WHERE seq IN (:seqs)",
                        new MapSqlParameterSource("seqs", seqs),
                        (rs, n) -> ingested.put(rs.getLong(1), rs.getObject(2, OffsetDateTime.class).toInstant()));
            }
            return keepKnownAtIngest(rows, ingested, horizon);
        });
    }

    /**
     * The rows the live engine would have seen: {@code valid_from} unset, or at most
     * {@code horizon} after the version's own ingest time.
     */
    static List<MatchRow> keepKnownAtIngest(List<MatchRow> rows, Map<Long, Instant> ingestedAt, Duration horizon) {
        List<MatchRow> out = new ArrayList<>();
        for (MatchRow row : rows) {
            Instant ingested = ingestedAt.get(row.seq());
            if (row.validFrom() == null || (ingested != null && !row.validFrom().isAfter(ingested.plus(horizon)))) {
                out.add(row);
            }
        }
        return out;
    }

    /** The non-suppressed alerts of the rule in the seq range that fired before {@code updatedAt}. */
    private long alertsBefore(UUID ruleId, long fromSeq, long toSeq, Instant updatedAt) {
        if (updatedAt == null) {
            return 0;
        }
        return reads.read(BacktestCoverage.STATEMENT_TIMEOUT, t -> {
            Long n = t.getJdbcTemplate().queryForObject("SELECT count(*) FROM alert WHERE rule_id = ? AND seq > ?"
                    + " AND seq <= ? AND suppressed = false AND fired_at < ?",
                    Long.class, ruleId, fromSeq, toSeq, Timestamp.from(updatedAt));
            return n == null ? 0L : n;
        });
    }

    /**
     * The {@code actual} block of {@code RuleBacktestResult}: the alerts the rule really fired in range,
     * and whether they are comparable with the backtest. Alerts carry no rule version, so they are
     * comparable when the rule is unchanged since every one of them fired.
     *
     * @param alerts the alerts (or anomaly observations) the rule really produced in range
     * @param firedBeforeLastUpdate how many of them predate the rule's last update
     * @return {@code {alerts, comparable}}
     */
    static Map<String, Object> actual(long alerts, long firedBeforeLastUpdate) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("alerts", alerts);
        out.put("comparable", firedBeforeLastUpdate == 0);
        return out;
    }

    /**
     * The {@code perDay} array of {@code RuleBacktestResult}: one {@code {date, count}} per UTC day, in
     * date order.
     *
     * @param byDay counts keyed by ISO date
     * @return the array
     */
    static List<Map<String, Object>> perDay(Map<String, Integer> byDay) {
        List<Map<String, Object>> out = new ArrayList<>();
        new TreeMap<>(byDay).forEach((date, count) -> {
            Map<String, Object> day = new LinkedHashMap<>();
            day.put("date", date);
            day.put("count", count);
            out.add(day);
        });
        return out;
    }

    /** Marks a variant result with its position, the patch it ran and its name. */
    private static void identify(Map<String, Object> entry, int index, Map<String, Object> variant) {
        entry.put("index", index);
        entry.put("patch", variant);
        entry.put("name", String.valueOf(variant.getOrDefault("name", "variant")));
    }

    private Set<Long> actualAlertSeqs(UUID ruleId, long fromSeq, long toSeq) {
        List<Long> seqs = reads.read(BacktestCoverage.STATEMENT_TIMEOUT, t -> t.getJdbcTemplate().queryForList(
                "SELECT seq FROM alert WHERE rule_id = ? AND seq > ? AND seq <= ? AND suppressed = false",
                Long.class, ruleId, fromSeq, toSeq));
        return new HashSet<>(seqs);
    }

    /** An anomaly rule replayed from its stored bucket counts. */
    private Map<String, Object> anomaly(TriggerRuleEntity entity, Instant rangeFrom, Instant rangeTo,
            List<Map<String, Object>> variants, JobMonitor monitor) {
        WindowSpec spec = WindowSpec.fromStored(entity.getWindowSpec());
        if (spec == null || rangeFrom == null || rangeTo == null) {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("ruleType", "anomaly");
            out.put("wouldFire", 0L);
            out.put("note", "The rule has no window specification or the range has no time bounds.");
            return out;
        }
        monitor.checkpoint(0);
        List<AnomalyReplay.Sample> samples = reads.read(BacktestCoverage.STATEMENT_TIMEOUT,
                t -> t.getJdbcTemplate().query(
                        "SELECT group_key, bucket_start, count, coverage_ratio, coverage_ok, flagged FROM activity_sample"
                                + " WHERE rule_id = ? AND bucket_start >= ? AND bucket_start < ?"
                                + " ORDER BY bucket_start, group_key",
                        (rs, n) -> new AnomalyReplay.Sample(rs.getString(1), rs.getTimestamp(2).toInstant(),
                                rs.getInt(3), rs.getObject(4) == null ? null : rs.getDouble(4), rs.getBoolean(5),
                                rs.getBoolean(6)),
                        entity.getId(), Timestamp.from(rangeFrom.minus(AnomalyReplay.LOOKBACK)),
                        Timestamp.from(rangeTo)));
        monitor.checkpoint(0.5 / (1 + variants.size()));
        Map<String, Object> result = AnomalyReplay.replay(samples, spec, rangeFrom, rangeTo).toMap(spec.bucket());
        long actual = reads.read(BacktestCoverage.STATEMENT_TIMEOUT, t -> {
            Long n = t.getJdbcTemplate().queryForObject("SELECT count(*) FROM anomaly_observation WHERE rule_id = ?"
                    + " AND bucket_start >= ? AND bucket_start < ? AND classification IN ('activity-up','activity-down')",
                    Long.class, entity.getId(), Timestamp.from(rangeFrom), Timestamp.from(rangeTo));
            return n == null ? 0L : n;
        });
        Instant updatedAt = entity.getUpdatedAt();
        long before = updatedAt == null ? 0 : reads.read(BacktestCoverage.STATEMENT_TIMEOUT, t -> {
            Long n = t.getJdbcTemplate().queryForObject("SELECT count(*) FROM anomaly_observation WHERE rule_id = ?"
                    + " AND bucket_start >= ? AND bucket_start < ? AND bucket_start < ?"
                    + " AND classification IN ('activity-up','activity-down')",
                    Long.class, entity.getId(), Timestamp.from(rangeFrom), Timestamp.from(rangeTo),
                    Timestamp.from(updatedAt));
            return n == null ? 0L : n;
        });
        result.put("actual", actual(actual, before));
        if (!variants.isEmpty()) {
            List<Map<String, Object>> variantResults = new ArrayList<>();
            for (int i = 0; i < variants.size(); i++) {
                monitor.checkpoint((i + 1.0) / (1 + variants.size()));
                Map<String, Object> variant = variants.get(i);
                Map<String, Object> entry = AnomalyReplay.replay(samples, patchWindow(spec, variant), rangeFrom,
                        rangeTo).toMap(spec.bucket());
                identify(entry, i, variant);
                variantResults.add(entry);
            }
            result.put("variants", variantResults);
        }
        return result;
    }

    /**
     * A window specification with a variant's {@code window} fields merged in. Only detector
     * parameters take effect: the bucket and the grouping are kept, since the stored counts were
     * computed with them.
     */
    @SuppressWarnings("unchecked")
    static WindowSpec patchWindow(WindowSpec spec, Map<String, Object> variant) {
        if (!(variant.get("window") instanceof Map<?, ?> raw)) {
            return spec;
        }
        Map<String, Object> merged = new LinkedHashMap<>(spec.toMap());
        merged.putAll((Map<String, Object>) raw);
        merged.put("bucket", spec.bucket() == null ? null : spec.bucket().toString());
        merged.put("slotMode", spec.slotMode());
        merged.put("groupBy", spec.groupBy());
        merged.put("window", spec.window() == null ? null : spec.window().toString());
        merged.put("step", spec.step() == null ? null : spec.step().toString());
        return WindowSpec.fromStored(merged);
    }

    /** Applies a partial variant (replacing conditions) to a rule spec. */
    @SuppressWarnings("unchecked")
    static RuleSpec patch(RuleSpec rule, Map<String, Object> variant) {
        if (!(variant.get("conditions") instanceof List<?> raw)) {
            return rule;
        }
        List<RuleCondition> conditions = new ArrayList<>();
        for (Object item : raw) {
            if (item instanceof Map<?, ?> map) {
                Map<String, Object> condition = (Map<String, Object>) map;
                String path = String.valueOf(condition.get("path"));
                String op = String.valueOf(condition.get("op"));
                OperatorHolder holder = OperatorHolder.of(op);
                if (holder.operator != null) {
                    conditions.add(new RuleCondition(path, holder.operator, condition.get("value")));
                }
            }
        }
        return rule.withConditions(conditions);
    }

    private static final class Result {

        final long fired;
        final long suppressed;
        final Map<String, Integer> byDay;
        final List<Map<String, Object>> topEntities;
        final List<Map<String, Object>> samples;
        final Set<Long> firedSeqs;

        Result(long fired, long suppressed, Map<String, Integer> byDay, List<Map<String, Object>> topEntities,
                List<Map<String, Object>> samples, Set<Long> firedSeqs) {
            this.fired = fired;
            this.suppressed = suppressed;
            this.byDay = byDay;
            this.topEntities = topEntities;
            this.samples = samples;
            this.firedSeqs = firedSeqs;
        }

        Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("wouldFire", fired);
            out.put("wouldSuppress", suppressed);
            out.put("perDay", perDay(byDay));
            out.put("topEntities", topEntities);
            out.put("samples", samples);
            return out;
        }
    }

    private record OperatorHolder(ai.foresignal.trigger.Operator operator) {

        static OperatorHolder of(String name) {
            return new OperatorHolder(ai.foresignal.trigger.Operators.builtIns().stream()
                    .filter(o -> o.name().equals(name)).findFirst().orElse(null));
        }
    }
}
