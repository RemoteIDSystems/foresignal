package ai.foresignal.exercise;

import ai.foresignal.backtest.BacktestCaveat;
import ai.foresignal.backtest.BacktestCoverage;
import ai.foresignal.backtest.RuleBacktest;
import ai.foresignal.store.TriggerMode;
import ai.foresignal.store.TriggerRuleEntity;
import ai.foresignal.store.query.PgArrays;
import ai.foresignal.store.query.ReadQueryRunner;
import ai.foresignal.trigger.AlertPass;
import ai.foresignal.trigger.MatchRow;
import ai.foresignal.trigger.RuleSpec;
import ai.foresignal.trigger.RuleSpecs;
import ai.foresignal.trigger.window.RuleType;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;

/**
 * The evaluation half of an exercise replay (prompt 115): which alerts the selected rules would have raised in
 * a retained window, decided in <strong>run-local</strong> state so real cooldowns, rate counters and alerts are
 * never read or written.
 *
 * <p>It reuses the 103 rule backtest engine: {@link RuleBacktest#replayMatches} runs {@code RuleSqlBuilder} per
 * {@value RuleBacktest#CHUNK}-seq chunk with alert insertion disabled and no cooldown seed (enter and exit come
 * from the edge join on {@code prev_seq}, so a version whose predecessor was removed by retention fires nothing),
 * and {@link AlertPass} in simulate mode applies, in seq order, active periods and windows, the cooldown per
 * {@code (rule, entity)} or per rule on {@code observed_at}, and the storm cap ({@code max_alerts_per_hour}) on
 * {@code observed_at}, all held in a fresh {@link AlertPass.State} per rule.
 *
 * <ul>
 *   <li><b>Record rules</b> replay exactly.
 *   <li><b>Count and absence rules</b> replay at record level, as 103 does, with its {@code WINDOW_APPROXIMATE}
 *       caveat.
 *   <li><b>Anomaly rules</b> are replayed by 103 as bucket statistics, not as alert-producing matches, so they
 *       are listed in {@code skippedRules}, as is a rule that cannot be compiled.
 *   <li>A rule with an {@code approach} condition judges its target by the target's current state (the 114
 *       backtest rule), caveat {@code APPROACH_CURRENT_TARGET}.
 *   <li>Edge rules with versions in the window whose predecessor lies before the window start and is no longer
 *       retained get the caveat {@value #PREDECESSOR_OUTSIDE_WINDOW}: those versions fire nothing.
 * </ul>
 */
@Service
public class ExerciseReplay {

    /** The caveat of edge versions whose predecessor is outside the window and no longer retained. */
    public static final String PREDECESSOR_OUTSIDE_WINDOW = "PREDECESSOR_OUTSIDE_WINDOW";

    /** The caveat of approach rules judged against the target's current state. */
    public static final String APPROACH_CURRENT_TARGET = "APPROACH_CURRENT_TARGET";

    /** Seqs per {@code ingested_at} lookup. */
    static final int INGESTED_BATCH = 5_000;

    private final RuleBacktest backtest;
    private final RuleSpecs specs;
    private final ReadQueryRunner reads;

    /** Creates the replay. */
    public ExerciseReplay(RuleBacktest backtest, RuleSpecs specs, ReadQueryRunner reads) {
        this.backtest = backtest;
        this.specs = specs;
        this.reads = reads;
    }

    /**
     * One alert the replay would raise.
     *
     * @param rule the rule
     * @param row the matching version
     * @param ingestedAt the version's ingest time (pacing), or null when unknown
     */
    public record Match(RuleSpec rule, MatchRow row, Instant ingestedAt) {}

    /**
     * What the replay decided.
     *
     * @param matches the alerts to raise, in seq order
     * @param versionsScanned versions in the window
     * @param alertsByRule rule id to alerts
     * @param suppressedByCooldown matches dropped by the run-local cooldown
     * @param suppressedByRateCap matches dropped by the run-local storm cap
     * @param skippedRules {@code {ruleId, reason}} of rules that cannot be replayed
     * @param caveats {@code Caveat} objects
     */
    public record Plan(List<Match> matches, long versionsScanned, Map<String, Integer> alertsByRule,
            int suppressedByCooldown, int suppressedByRateCap, List<Map<String, Object>> skippedRules,
            List<Map<String, Object>> caveats) {}

    /**
     * Plans the replay of {@code rules} over {@code (fromSeq, toSeq]}.
     *
     * @param rules the stored rules
     * @param fromSeq exclusive lower bound
     * @param toSeq inclusive upper bound
     * @param canceled checked between chunks
     */
    public Plan plan(List<TriggerRuleEntity> rules, long fromSeq, long toSeq, BooleanSupplier canceled) {
        List<Match> matches = new ArrayList<>();
        Map<String, Integer> byRule = new LinkedHashMap<>();
        List<Map<String, Object>> skipped = new ArrayList<>();
        List<Map<String, Object>> caveats = new ArrayList<>();
        int cooldown = 0;
        int rateCap = 0;
        int defaultCap = backtest.defaultCap();
        for (TriggerRuleEntity entity : rules) {
            String ruleId = entity.getId().toString();
            RuleType type;
            try {
                type = RuleType.fromWire(entity.getType());
            } catch (IllegalArgumentException e) {
                skipped.add(skip(ruleId, "unknown rule type " + entity.getType()));
                continue;
            }
            if (type == RuleType.ANOMALY) {
                skipped.add(skip(ruleId, "anomaly rules are replayed by the backtest as bucket statistics, not as"
                        + " alerts"));
                continue;
            }
            RuleSpec rule;
            try {
                rule = specs.compile(entity);
            } catch (RuntimeException e) {
                skipped.add(skip(ruleId, "the rule cannot be compiled: " + e.getMessage()));
                continue;
            }
            if (type.windowed()) {
                caveats.add(caveat(BacktestCaveat.WINDOW_APPROXIMATE, "Rule " + ruleId + " (" + type.wire()
                        + ") replays at record level: alerts follow matching versions, not window transitions.",
                        ruleId, null));
            }
            if (rule.hasApproach()) {
                caveats.add(caveat(APPROACH_CURRENT_TARGET, "Rule " + ruleId + " judges its approach target by the"
                        + " target's current state, not its state at the time.", ruleId, null));
            }
            byRule.put(ruleId, 0);
            AlertPass.State state = new AlertPass.State();
            state.seedStorm(List.of());
            for (long lo = fromSeq; lo < toSeq; lo += RuleBacktest.CHUNK) {
                if (canceled.getAsBoolean()) {
                    break;
                }
                long hi = Math.min(toSeq, lo + RuleBacktest.CHUNK);
                List<MatchRow> rows = backtest.replayMatches(rule, lo, hi);
                if (rows == null || rows.isEmpty()) {
                    continue;
                }
                Instant decision = rows.getLast().observedAt();
                AlertPass.Result result = AlertPass.run(rule, rows, List.of(), state,
                        new AlertPass.Context(decision == null ? Instant.EPOCH : decision, null, defaultCap, true, null));
                cooldown += result.cooldownSuppressed();
                rateCap += result.stormSuppressed();
                for (AlertPass.Op op : result.ops()) {
                    if (op instanceof AlertPass.Fire fire && fire.suppressedReason() == null) {
                        matches.add(new Match(rule, fire.row(), null));
                        byRule.merge(ruleId, 1, Integer::sum);
                    }
                }
                if (result.disable()) {
                    // autoDisableOnStorm: the rule stops for the rest of the run (run-local; the real rule is untouched).
                    caveats.add(caveat("STORM_STOPPED", "Rule " + ruleId + " hit its storm cap with autoDisableOnStorm"
                            + " and replayed nothing after that.", ruleId, null));
                    break;
                }
            }
            if (rule.mode() != TriggerMode.MATCH) {
                long orphans = orphans(rule, fromSeq, toSeq);
                if (orphans > 0) {
                    caveats.add(caveat(PREDECESSOR_OUTSIDE_WINDOW, orphans + " version(s) of rule " + ruleId
                            + "'s sources have a predecessor before the window start that is no longer retained; they"
                            + " fire nothing.", ruleId, orphans));
                }
            }
        }
        matches.sort(Comparator.comparingLong((Match m) -> m.row().seq()).thenComparing(m -> m.rule().id()));
        List<Match> paced = withIngestedAt(matches);
        return new Plan(paced, versionsScanned(fromSeq, toSeq), byRule, cooldown, rateCap, skipped, caveats);
    }

    /** Versions in {@code (fromSeq, toSeq]}. */
    long versionsScanned(long fromSeq, long toSeq) {
        if (toSeq <= fromSeq) {
            return 0;
        }
        Long n = reads.read(BacktestCoverage.STATEMENT_TIMEOUT, t -> t.getJdbcTemplate().queryForObject(
                "SELECT count(*) FROM record_version WHERE seq > ? AND seq <= ?", Long.class, fromSeq, toSeq));
        return n == null ? 0 : n;
    }

    /**
     * Versions of the rule's sources and kinds in the window whose predecessor lies at or before the window start
     * and is gone (removed by retention).
     */
    long orphans(RuleSpec rule, long fromSeq, long toSeq) {
        MapSqlParameterSource p = new MapSqlParameterSource("lo", fromSeq).addValue("hi", toSeq);
        StringBuilder sql = new StringBuilder("SELECT count(*) FROM record_version v"
                + " LEFT JOIN record_version p ON p.seq = v.prev_seq"
                + " WHERE v.seq > :lo AND v.seq <= :hi AND v.prev_seq IS NOT NULL AND v.prev_seq <= :lo"
                + " AND p.seq IS NULL");
        if (!rule.sources().isEmpty()) {
            sql.append(" AND v.source_id = ANY(CAST(:sources AS text[]))");
            p.addValue("sources", PgArrays.text(new ArrayList<>(rule.sources())));
        }
        if (!rule.kinds().isEmpty()) {
            sql.append(" AND v.kind = ANY(CAST(:kinds AS text[]))");
            p.addValue("kinds", PgArrays.text(new ArrayList<>(rule.kinds())));
        }
        try {
            Long n = reads.read(BacktestCoverage.STATEMENT_TIMEOUT, t -> t.queryForObject(sql.toString(), p, Long.class));
            return n == null ? 0 : n;
        } catch (RuntimeException e) {
            return 0;
        }
    }

    /** The matches with their versions' {@code ingested_at}, read in batches. */
    private List<Match> withIngestedAt(List<Match> matches) {
        if (matches.isEmpty()) {
            return matches;
        }
        Map<Long, Instant> ingested = new HashMap<>();
        List<Long> seqs = matches.stream().map(m -> m.row().seq()).distinct().toList();
        for (int i = 0; i < seqs.size(); i += INGESTED_BATCH) {
            List<Long> batch = seqs.subList(i, Math.min(seqs.size(), i + INGESTED_BATCH));
            reads.read(BacktestCoverage.STATEMENT_TIMEOUT, t -> {
                t.query("SELECT seq, ingested_at FROM record_version WHERE seq = ANY(CAST(:seqs AS bigint[]))",
                        new MapSqlParameterSource("seqs", PgArrays.bigint(batch)),
                        rs -> {
                            OffsetDateTime at = rs.getObject(2, OffsetDateTime.class);
                            ingested.put(rs.getLong(1), at == null ? null : at.toInstant());
                        });
                return null;
            });
        }
        List<Match> out = new ArrayList<>(matches.size());
        for (Match m : matches) {
            out.add(new Match(m.rule(), m.row(), ingested.get(m.row().seq())));
        }
        return out;
    }

    private static Map<String, Object> skip(String ruleId, String reason) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ruleId", ruleId);
        out.put("reason", reason);
        return out;
    }

    /** A {@code Caveat} object. */
    static Map<String, Object> caveat(String code, String text, String ruleId, Long count) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("code", code);
        out.put("text", text);
        Map<String, Object> data = new LinkedHashMap<>();
        if (ruleId != null) {
            data.put("ruleId", ruleId);
        }
        if (count != null) {
            data.put("versions", count);
        }
        if (!data.isEmpty()) {
            out.put("data", data);
        }
        return out;
    }
}
