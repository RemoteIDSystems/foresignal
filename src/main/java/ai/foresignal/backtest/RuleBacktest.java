package ai.foresignal.backtest;

import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.settings.TriggerSettings;
import ai.foresignal.store.query.ReadQueryRunner;
import ai.foresignal.trigger.AlertPass;
import ai.foresignal.trigger.MatchRow;
import ai.foresignal.trigger.RuleCondition;
import ai.foresignal.trigger.RuleSpec;
import ai.foresignal.trigger.RuleSqlBuilder;
import ai.foresignal.trigger.TriggerRuleService;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * A record-rule backtest (prompt 103): replays {@link RuleSqlBuilder} per 50k-seq chunk with the
 * live engine's {@link AlertPass} in simulate mode, so {@code wouldFire} reproduces the alerts the
 * engine fired. Reports a per-day histogram, the top entities, sample hits, the actual alerts in
 * range and their overlap, and optional threshold variants side by side.
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

    private final TriggerRuleService rules;
    private final ai.foresignal.trigger.RuleSpecs specs;
    private final ReadQueryRunner reads;
    private final AppSettingService settings;
    private final JdbcTemplate jdbc;
    private final Clock clock;

    /** Creates the service. */
    public RuleBacktest(TriggerRuleService rules, ai.foresignal.trigger.RuleSpecs specs, ReadQueryRunner reads,
            AppSettingService settings, JdbcTemplate jdbc, Clock clock) {
        this.rules = rules;
        this.specs = specs;
        this.reads = reads;
        this.settings = settings;
        this.jdbc = jdbc;
        this.clock = clock;
    }

    /** Runs a rule backtest over {@code (fromSeq, toSeq]} with optional variants. */
    public Map<String, Object> run(UUID ruleId, long fromSeq, long toSeq, List<Map<String, Object>> variants) {
        RuleSpec rule = specs.compile(rules.load(ruleId));
        Instant now = clock.instant();
        Result base = evaluate(rule, fromSeq, toSeq, now);
        Map<String, Object> result = base.toMap();
        long actual = actualAlerts(ruleId, fromSeq, toSeq);
        result.put("actual", actual);
        result.put("overlap", Map.of("both", Math.min(base.fired, actual),
                "onlyBacktest", Math.max(0, base.fired - actual), "onlyActual", Math.max(0, actual - base.fired)));
        if (variants != null && !variants.isEmpty()) {
            List<Map<String, Object>> variantResults = new ArrayList<>();
            for (Map<String, Object> variant : variants.subList(0, Math.min(variants.size(), MAX_VARIANTS))) {
                RuleSpec patched = patch(rule, variant);
                Result variantResult = evaluate(patched, fromSeq, toSeq, now);
                Map<String, Object> entry = variantResult.toMap();
                entry.put("name", String.valueOf(variant.getOrDefault("name", "variant")));
                variantResults.add(entry);
            }
            result.put("variants", variantResults);
        }
        return result;
    }

    private Result evaluate(RuleSpec rule, long fromSeq, long toSeq, Instant now) {
        int defaultCap = settings.getOrDefault(SettingKeys.TRIGGERS, TriggerSettings.class, TriggerSettings::defaults)
                .defaultMaxAlertsPerHour();
        AlertPass.State state = new AlertPass.State();
        state.seedStorm(List.of());
        long fired = 0;
        long suppressed = 0;
        Map<String, Integer> byDay = new TreeMap<>();
        Map<String, Integer> byEntity = new LinkedHashMap<>();
        List<Map<String, Object>> samples = new ArrayList<>();
        for (long lo = fromSeq; lo < toSeq; lo += CHUNK) {
            long hi = Math.min(toSeq, lo + CHUNK);
            RuleSqlBuilder.RuleSql sql = RuleSqlBuilder.matches(rule, lo, hi, now, false);
            List<MatchRow> rows = reads.read(t -> t.query(sql.sql(), sql.params(), MatchRow.MAPPER));
            AlertPass.Result pass = AlertPass.run(rule, rows, List.of(), state,
                    new AlertPass.Context(now, null, defaultCap, true, null));
            suppressed += pass.cooldownSuppressed() + pass.windowSkipped() + pass.stormSuppressed();
            for (AlertPass.Op op : pass.ops()) {
                if (op instanceof AlertPass.Fire fire && fire.suppressedReason() == null) {
                    fired++;
                    MatchRow row = fire.row();
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
            if (pass.disable()) {
                break;
            }
        }
        List<Map<String, Object>> top = byEntity.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(MAX_ENTITIES)
                .map(e -> Map.<String, Object>of("entity", e.getKey(), "count", e.getValue()))
                .toList();
        return new Result(fired, suppressed, byDay, top, samples);
    }

    private long actualAlerts(UUID ruleId, long fromSeq, long toSeq) {
        Long n = jdbc.queryForObject("select count(*) from alert where rule_id = ? and seq > ? and seq <= ?",
                Long.class, ruleId, fromSeq, toSeq);
        return n == null ? 0 : n;
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

    private record Result(long fired, long suppressed, Map<String, Integer> byDay, List<Map<String, Object>> topEntities,
            List<Map<String, Object>> samples) {

        Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("wouldFire", fired);
            out.put("wouldSuppress", suppressed);
            out.put("perDay", byDay);
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
