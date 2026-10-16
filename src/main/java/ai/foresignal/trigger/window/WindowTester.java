package ai.foresignal.trigger.window;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.store.TriggerRuleEntity;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.trigger.RuleSpec;
import ai.foresignal.trigger.RuleSpecs;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * {@code testTrigger} for windowed rules (prompt 100 §5): evaluates a count, absence or anomaly rule
 * over a past range, at the rule's step instants (or anomaly buckets), from <strong>versions only</strong>
 * and only what was known at each instant. It creates nothing: no state, no samples, no alerts.
 *
 * <p>The result is marked approximate: an entity observed without a new version inside the window
 * (only touched, or thinned by a SAMPLED policy) is missed when the window is shorter than the
 * source's keyframe. Full backtests are prompt 103's.
 */
@Service
public class WindowTester {

    /** The reason every windowed test result carries. */
    public static final String APPROXIMATE_REASON = "versions only; entities observed without a new version inside"
            + " the window are missed when window < source keyframe";

    /** The most evaluations one test returns. */
    public static final int MAX_EVALUATIONS = 500;

    /** The default range when none is given. */
    public static final Duration DEFAULT_RANGE = Duration.ofHours(24);

    private final RuleSpecs specs;
    private final WindowMembership membership;
    private final CoverageGate gate;
    private final ActivitySampleRepository samples;
    private final ChangeBus changeBus;
    private final JdbcTemplate jdbc;
    private final Clock clock;

    /** Creates the tester. */
    public WindowTester(RuleSpecs specs, WindowMembership membership, CoverageGate gate,
            ActivitySampleRepository samples, ChangeBus changeBus, JdbcTemplate jdbc, Clock clock) {
        this.specs = specs;
        this.membership = membership;
        this.gate = gate;
        this.samples = samples;
        this.changeBus = changeBus;
        this.jdbc = jdbc;
        this.clock = clock;
    }

    /**
     * One evaluation.
     *
     * @param at the step instant (count, absence) or bucket end (anomaly)
     * @param groupKey the group
     * @param value the distinct count, or null when unknown
     * @param state {@code active}, {@code clear}, {@code unknown} or {@code learning}
     * @param reason why unknown or learning, or null
     */
    public record Evaluation(Instant at, String groupKey, Double value, String state, String reason) {}

    /**
     * A windowed test result.
     *
     * @param from the range start
     * @param to the range end
     * @param fromSeq the exclusive start seq
     * @param toSeq the inclusive end seq (the versions read)
     * @param evaluations the evaluations, oldest first
     * @param truncated true when more than {@value #MAX_EVALUATIONS} evaluations were due
     */
    public record Result(Instant from, Instant to, long fromSeq, long toSeq, List<Evaluation> evaluations,
            boolean truncated) {}

    /**
     * Tests a windowed rule.
     *
     * @param entity the stored rule (a windowed type)
     * @param since the range start, or null
     * @param until the range end, or null
     * @param fromSeq the exclusive start seq, or null
     * @param toSeq the inclusive end seq, or null
     * @return the evaluations
     * @throws ValidationException when both a seq and a time range are given, or the range is inverted
     */
    public Result test(TriggerRuleEntity entity, Instant since, Instant until, Long fromSeq, Long toSeq) {
        RuleType type = RuleType.fromWire(entity.getType());
        WindowSpec window = WindowSpec.fromStored(entity.getWindowSpec());
        if (!type.windowed() || window == null) {
            throw WindowSpecValidator.invalid("type", "is not a windowed type");
        }
        boolean seqRange = fromSeq != null || toSeq != null;
        if (seqRange && (since != null || until != null)) {
            throw ValidationException.forField("fromSeq", "use fromSeq/toSeq or since/until, not both");
        }
        Instant now = clock.instant();
        long watermark;
        long lowSeq;
        Instant from;
        Instant to;
        if (seqRange) {
            watermark = toSeq != null ? toSeq : changeBus.lastPublishedSeq();
            lowSeq = fromSeq != null ? fromSeq : 0;
            Instant[] bounds = ingestedBounds(lowSeq, watermark);
            from = bounds[0] != null ? bounds[0] : now.minus(DEFAULT_RANGE);
            to = bounds[1] != null ? bounds[1] : now;
        } else {
            watermark = changeBus.lastPublishedSeq();
            lowSeq = 0;
            to = until != null ? until : now;
            from = since != null ? since : to.minus(DEFAULT_RANGE);
        }
        if (from.isAfter(to)) {
            throw ValidationException.forField("since", "must not be after until");
        }
        RuleSpec spec = specs.compile(entity);
        List<Evaluation> out = new ArrayList<>();
        boolean truncated;
        if (type == RuleType.ANOMALY) {
            truncated = anomaly(spec, window, from, to, watermark, out);
        } else {
            truncated = steps(spec, type, window, from, to, watermark, out);
        }
        return new Result(from, to, lowSeq, watermark, out, truncated);
    }

    private boolean steps(RuleSpec spec, RuleType type, WindowSpec window, Instant from, Instant to, long watermark,
            List<Evaluation> out) {
        Instant at = WindowEngine.floor(from.plus(window.window()), window.step());
        if (at.isBefore(from.plus(window.window()))) {
            at = at.plus(window.step());
        }
        int evaluated = 0;
        while (!at.isAfter(to)) {
            if (evaluated >= MAX_EVALUATIONS) {
                return true;
            }
            Instant start = at.minus(window.window());
            WindowMembership.Membership result = membership.resolve(spec, window, start, at, watermark, false, at,
                    WindowMembership.ROW_CAP);
            if (result.capped()) {
                out.add(new Evaluation(at, "*", null, "unknown", "too many candidate rows; unknown"));
            } else if (type == RuleType.ABSENCE) {
                CoverageGate.Gate coverage = gate.evaluateAsOf(spec.sources(), spec.kinds(), start, at, window.window(),
                        window.coverageMin());
                int total = result.counts().values().stream().mapToInt(Integer::intValue).sum();
                out.add(coverage.ok()
                        ? new Evaluation(at, "*", (double) total, total == 0 ? "active" : "clear", null)
                        : new Evaluation(at, "*", null, "unknown", coverage.reason()));
            } else {
                Map<String, Integer> counts = new TreeMap<>(result.counts());
                if (window.groupBy() == null || window.groupBy().isBlank()) {
                    counts.putIfAbsent("*", 0);
                }
                for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                    int value = entry.getValue();
                    boolean active = ">=".equals(window.op()) ? value >= window.threshold() : value <= window.threshold();
                    out.add(new Evaluation(at, entry.getKey(), (double) value, active ? "active" : "clear", null));
                }
            }
            evaluated++;
            at = at.plus(window.step());
        }
        return false;
    }

    private boolean anomaly(RuleSpec spec, WindowSpec window, Instant from, Instant to, long watermark,
            List<Evaluation> out) {
        Duration bucket = window.bucket();
        int evaluated = 0;
        for (Instant start = WindowEngine.floor(from, bucket); !start.plus(bucket).isAfter(to); start = start.plus(bucket)) {
            if (evaluated >= MAX_EVALUATIONS) {
                return true;
            }
            Instant end = start.plus(bucket);
            WindowMembership.Membership result = membership.resolve(spec, window, start, end, watermark, false,
                    end.plus(WindowEngine.SETTLE), WindowMembership.ROW_CAP);
            CoverageGate.Gate coverage = gate.evaluateAsOf(spec.sources(), spec.kinds(), start, end, bucket,
                    window.coverageMin());
            double ratio = gate.coverageRatioAsOf(spec.sources(), spec.kinds(), start, end);
            Map<String, Integer> counts = new TreeMap<>(result.counts());
            if (window.groupBy() == null || window.groupBy().isBlank()) {
                counts.putIfAbsent("*", 0);
            }
            List<ActivitySampleEntity> history = samples
                    .findByRuleIdAndBucketStartGreaterThanEqualOrderByBucketStartAsc(spec.id(),
                            start.minus(WindowEngine.LOOKBACK));
            int slot = WindowEngine.slotOf(start, window.slotMode());
            for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                List<Integer> slotCounts = new ArrayList<>();
                List<Double> slotCoverages = new ArrayList<>();
                for (ActivitySampleEntity sample : history) {
                    if (sample.getGroupKey().equals(entry.getKey()) && sample.isCoverageOk() && !sample.isFlagged()
                            && sample.getBucketStart().isBefore(start)
                            && WindowEngine.slotOf(sample.getBucketStart(), window.slotMode()) == slot) {
                        slotCounts.add(sample.getCount());
                        slotCoverages.add(sample.getCoverageRatio() == null ? 1.0 : sample.getCoverageRatio());
                    }
                }
                Baseline baseline = Baseline.of(slotCounts, slotCoverages, window.slotMode(), bucket);
                AnomalyDetector.Result detector = AnomalyDetector.evaluate(entry.getValue(), baseline, ratio,
                        coverage.ok() && !result.capped(), window);
                String state = detector.fire() ? "active"
                        : "learning".equals(detector.classification()) ? "learning"
                        : "insufficient-coverage".equals(detector.classification())
                                || "coverage-drop".equals(detector.classification()) ? "unknown" : "clear";
                out.add(new Evaluation(end, entry.getKey(), (double) entry.getValue(), state,
                        "clear".equals(state) || "active".equals(state) ? null : detector.classification()));
            }
            evaluated++;
        }
        return false;
    }

    /** The ingestion times of the first and last version in {@code (fromSeq, toSeq]}. */
    private Instant[] ingestedBounds(long fromSeq, long toSeq) {
        return jdbc.queryForObject("SELECT min(ingested_at), max(ingested_at) FROM record_version"
                        + " WHERE seq > ? AND seq <= ?",
                (rs, n) -> {
                    Timestamp lo = rs.getTimestamp(1);
                    Timestamp hi = rs.getTimestamp(2);
                    return new Instant[] {lo == null ? null : lo.toInstant(), hi == null ? null : hi.toInstant()};
                }, fromSeq, toSeq);
    }
}
