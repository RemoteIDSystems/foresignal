package ai.foresignal.trigger;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.retention.RetentionState;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.settings.TriggerSettings;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.store.history.HistoryFidelity;
import ai.foresignal.store.query.ReadQueryRunner;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.RecordRow;
import ai.foresignal.store.query.SeqClockQuery;
import ai.foresignal.stream.ChangeBus;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * {@code testTrigger}: a backtest of a rule over stored versions (prompt 017). Creates nothing.
 *
 * <p>The range is {@code (fromSeq, toSeq]}, or {@code since}/{@code until} translated to seqs through
 * {@code seq_clock} ({@code S(T)}), by default the last 24 hours. It is evaluated in 50,000-seq chunks,
 * each statement under the read statement timeout, with the same SQL as the engine but without the
 * cooldown seed. Without {@code simulate}, every raw match counts. With it, {@link AlertPass} runs in
 * seq order from a fresh state, applying cooldown, active windows, hysteresis and the storm cap (on
 * {@code observed_at}); snoozes are not simulated.
 *
 * <p>When the tested range overlaps TRACK history thinned by downsampling (prompt 044), the result
 * carries the warning {@value #DOWNSAMPLED_RANGE}: match counts under-count there, while edge versions
 * (TRIGGER_EDGE and the other kept reasons) are preserved.
 */
@Service
public class TriggerBacktester {

    /** Seqs per backtest chunk. */
    public static final long CHUNK = 50_000;

    /** The default backtest window. */
    public static final Duration DEFAULT_WINDOW = Duration.ofHours(24);

    /** The most matches returned. */
    public static final int MAX_LIMIT = 1_000;

    /** The warning code for a range that overlaps downsampled history (prompt 044). */
    public static final String DOWNSAMPLED_RANGE = "DOWNSAMPLED_RANGE";

    /** Its message. */
    public static final String DOWNSAMPLED_RANGE_MESSAGE =
            "match-mode results over downsampled history under-count; edge reasons are preserved";

    private final TriggerRuleService rules;
    private final RuleSpecs specs;
    private final ReadQueryRunner reads;
    private final SeqClockQuery seqClock;
    private final RecordQueryService records;
    private final RetentionState retention;
    private final ChangeBus changeBus;
    private final AppSettingService settings;
    private final HistoryFidelity fidelity;
    private final Clock clock;

    /** Creates the backtester. */
    public TriggerBacktester(
            TriggerRuleService rules,
            RuleSpecs specs,
            ReadQueryRunner reads,
            SeqClockQuery seqClock,
            RecordQueryService records,
            RetentionState retention,
            ChangeBus changeBus,
            AppSettingService settings,
            HistoryFidelity fidelity,
            Clock clock) {
        this.rules = rules;
        this.specs = specs;
        this.reads = reads;
        this.seqClock = seqClock;
        this.records = records;
        this.retention = retention;
        this.changeBus = changeBus;
        this.settings = settings;
        this.fidelity = fidelity;
        this.clock = clock;
    }

    /**
     * What to test.
     *
     * @param fromSeq exclusive start seq, or null
     * @param toSeq inclusive end seq, or null
     * @param since start time, or null
     * @param until end time, or null
     * @param limit the most matches to return (1–1,000)
     * @param simulate run the full Java pass
     */
    public record Request(Long fromSeq, Long toSeq, Instant since, Instant until, int limit, boolean simulate) {}

    /**
     * A non-fatal finding about a backtest ({@code TriggerTestResult.warnings}).
     *
     * @param code a stable code, for example {@value #DOWNSAMPLED_RANGE}
     * @param message a human explanation
     */
    public record Warning(String code, String message) {}

    /**
     * The outcome.
     *
     * @param ruleId the rule
     * @param evaluatedFrom the start time when a time range was used, else null
     * @param matches the first {@code limit} matching versions
     * @param truncated true when more matched than were returned
     * @param fromSeq the exclusive start seq evaluated
     * @param toSeq the inclusive end seq evaluated
     * @param matchCount every raw match in the range
     * @param simulated true with {@code simulate}
     * @param wouldFire with {@code simulate}, the alerts that would be recorded, else null
     * @param wouldSuppress with {@code simulate}, the matches cooldown, windows and the cap would drop
     * @param truncatedByRetention true when {@code fromSeq} is below the retained floor
     * @param enrich true when the rule would enrich (no LLM is called)
     * @param warnings non-fatal findings, for example {@value #DOWNSAMPLED_RANGE}
     */
    public record Result(
            UUID ruleId,
            Instant evaluatedFrom,
            List<RecordRow> matches,
            boolean truncated,
            long fromSeq,
            long toSeq,
            long matchCount,
            boolean simulated,
            Long wouldFire,
            Long wouldSuppress,
            boolean truncatedByRetention,
            boolean enrich,
            List<Warning> warnings) {

        /** Copies the warnings. */
        public Result {
            warnings = warnings == null ? List.of() : List.copyOf(warnings);
        }
    }

    /**
     * Runs a backtest.
     *
     * @throws ai.foresignal.api.error.NotFoundException when the rule does not exist
     * @throws ValidationException when the range is not valid
     */
    public Result test(UUID ruleId, Request request) {
        RuleSpec rule = specs.compile(rules.load(ruleId));
        boolean seqRange = request.fromSeq() != null || request.toSeq() != null;
        boolean timeRange = request.since() != null || request.until() != null;
        if (seqRange && timeRange) {
            throw ValidationException.forField("fromSeq", "use fromSeq/toSeq or since/until, not both");
        }
        if (request.limit() < 1 || request.limit() > MAX_LIMIT) {
            throw ValidationException.forField("limit", "must be between 1 and " + MAX_LIMIT);
        }
        Instant now = clock.instant();
        Instant evaluatedFrom = null;
        long from;
        long to;
        if (seqRange) {
            to = request.toSeq() != null ? request.toSeq() : changeBus.lastPublishedSeq();
            from = request.fromSeq() != null ? request.fromSeq()
                    : Math.min(to, reads.read(t -> seqClock.seqAt(t, now.minus(DEFAULT_WINDOW))));
        } else {
            Instant since = request.since() != null ? request.since() : now.minus(DEFAULT_WINDOW);
            Instant until = request.until() != null ? request.until() : now;
            if (since.isAfter(until)) {
                throw ValidationException.forField("since", "must not be after until");
            }
            evaluatedFrom = since;
            from = reads.read(t -> seqClock.seqAt(t, since));
            to = reads.read(t -> seqClock.seqAt(t, until));
        }
        if (from > to) {
            throw ValidationException.forField("fromSeq", "must not be greater than toSeq");
        }
        int defaultCap = settings.getOrDefault(SettingKeys.TRIGGERS, TriggerSettings.class, TriggerSettings::defaults)
                .defaultMaxAlertsPerHour();
        List<Long> kept = new ArrayList<>();
        long matchCount = 0;
        long wouldFire = 0;
        long wouldSuppress = 0;
        AlertPass.State state = new AlertPass.State();
        state.seedStorm(List.of());
        for (long lo = from; lo < to; lo += CHUNK) {
            long hi = Math.min(to, lo + CHUNK);
            List<MatchRow> rows = matches(rule, lo, hi, now);
            for (MatchRow row : rows) {
                if (rule.hysteresis() && !row.edge().equals(rule.mode().wireValue())) {
                    // The opposite edge only drives the band's state; it is not a match.
                    continue;
                }
                matchCount++;
                if (kept.size() < request.limit()) {
                    kept.add(row.seq());
                }
            }
            if (request.simulate()) {
                AlertPass.Result pass = AlertPass.run(rule, rows, List.of(), state,
                        new AlertPass.Context(now, null, defaultCap, true, null));
                wouldFire += pass.fired();
                wouldSuppress += pass.cooldownSuppressed() + pass.windowSkipped() + pass.stormSuppressed();
                if (pass.disable()) {
                    break;
                }
            }
        }
        Map<Long, RecordRow> versions = records.versionsBySeq(kept);
        List<RecordRow> matches = new ArrayList<>();
        for (Long seq : kept) {
            RecordRow row = versions.get(seq);
            if (row != null) {
                matches.add(row);
            }
        }
        long floor = retention.lowestRetainedSeq(rule.sources());
        return new Result(ruleId, evaluatedFrom, matches, matchCount > kept.size(), from, to, matchCount,
                request.simulate(), request.simulate() ? wouldFire : null, request.simulate() ? wouldSuppress : null,
                from < floor, rule.enrich(), warnings(fidelity, from, to));
    }

    /**
     * The warnings of a backtest over {@code (from, to]}: {@value #DOWNSAMPLED_RANGE} when the range
     * overlaps downsampled TRACK history.
     */
    public static List<Warning> warnings(HistoryFidelity fidelity, long from, long to) {
        if (fidelity != null && to > from && fidelity.downsampled(HistoryTier.TRACK, from + 1, to)) {
            return List.of(new Warning(DOWNSAMPLED_RANGE, DOWNSAMPLED_RANGE_MESSAGE));
        }
        return List.of();
    }

    /** The {@code approach} condition evaluator (prompt 114); without one every approach is ambiguous. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private org.springframework.beans.factory.ObjectProvider<ai.foresignal.kinematics.ApproachEvaluator> approachEvaluator;

    /**
     * The matches of {@code rule} in {@code (lo, hi]}: the SQL statement, or for a rule with {@code approach}
     * conditions the candidate statement judged in Java (prompt 114).
     */
    private List<MatchRow> matches(RuleSpec rule, long lo, long hi, java.time.Instant now) {
        if (!rule.hasApproach()) {
            RuleSqlBuilder.RuleSql sql = RuleSqlBuilder.matches(rule, lo, hi, now, false);
            return reads.read(t -> t.query(sql.sql(), sql.params(), MatchRow.MAPPER));
        }
        RuleSqlBuilder.RuleSql sql = RuleSqlBuilder.approachCandidates(rule, lo, hi, now, null);
        List<ApproachMatches.Candidate> candidates =
                reads.read(t -> t.query(sql.sql(), sql.params(), ApproachMatches.MAPPER));
        ai.foresignal.kinematics.ApproachEvaluator evaluator =
                approachEvaluator == null ? null : approachEvaluator.getIfAvailable();
        return ApproachMatches.evaluate(rule, candidates,
                evaluator == null ? ApproachMatches.UNKNOWN : evaluator.session()::evaluate).matches();
    }
}
