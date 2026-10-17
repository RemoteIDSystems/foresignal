package ai.foresignal.benchmark.component;

import ai.foresignal.analytics.changepoint.ChangePointDetector;
import ai.foresignal.analytics.changepoint.ChangePointService;
import ai.foresignal.analytics.math.CusumThresholds;
import ai.foresignal.analytics.series.SeriesData;
import ai.foresignal.analytics.series.SeriesFrequency;
import ai.foresignal.backtest.JobMonitor;
import ai.foresignal.benchmark.BenchmarkResults.ScenarioMetrics;
import ai.foresignal.benchmark.BenchmarkResults.SuiteParams;
import ai.foresignal.benchmark.BenchmarkScorer;
import ai.foresignal.benchmark.scenario.InMemorySeries;
import ai.foresignal.benchmark.scenario.ScenarioGenerator;
import ai.foresignal.benchmark.scenario.ScenarioType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Benchmarks the 119 change-point detector ({@code change-point}) as {@link ChangePointService} runs it: daily count
 * series of 400 days of history, decisions every {@value #STEP} days over the last
 * {@value ScenarioGenerator#EVAL_DAYS} days with {@code asOf} = the decision time (the in-memory series and
 * {@code Grid.knownBy}), alarms reported in the last seven buckets, each result classified by
 * {@link ChangePointService#classify} with the planted coverage standing in for 116 detectability. Detections of the
 * same change at later decisions count once.
 *
 * <ul>
 *   <li>{@code null-seasonal}: confirmed changes per evaluated bucket against the configured two-sided rate
 *       {@code 2 / targetArl} ({@code falseAlarmRatio}).</li>
 *   <li>{@code step-change}: recall of planted 2 and 3 SD shifts (right direction, change within a week before to two
 *       weeks after the plant), precision, and the delay from the plant to the alarm.</li>
 *   <li>{@code spike}: the share of replicates with a confirmed change within a week of a two-day burst.</li>
 *   <li>{@code coverage-outage}: the share of replicates without a confirmed downward change during an outage (gap
 *       variant) or low detectability (thinned reporting variant).</li>
 *   <li>{@code late-revision}: look-ahead violations (a value known after the decision in what the detector read,
 *       through the reader contract or through {@code knownBy} over everything stored); the detections a look-ahead
 *       reading would have made are reported beside them.</li>
 * </ul>
 */
@Component
public class ChangePointBenchmark implements ComponentBenchmark {

    /** The component name. */
    public static final String NAME = "change-point";

    /** Days between decisions (the detector reports alarms in the last seven buckets, so every alarm is seen). */
    public static final int STEP = 4;

    /** A detector: the real one, or a test double. */
    @FunctionalInterface
    public interface Detector {

        /** Detects as {@link ChangePointDetector#detect} does. */
        ChangePointDetector.Result detect(Instant[] starts, double[] x, boolean[] gap, boolean counts,
                ChangePointDetector.Params params, CusumThresholds thresholds);
    }

    private final Detector detector;

    /** The benchmark of the real detector. */
    public ChangePointBenchmark() {
        this(ChangePointDetector::detect);
    }

    /** The benchmark of {@code detector} (a test double). */
    public ChangePointBenchmark(Detector detector) {
        this.detector = detector;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String version() {
        return ChangePointDetector.CUSUM + " " + ChangePointDetector.VERSION;
    }

    @Override
    public List<ScenarioType> scenarios() {
        return List.of(ScenarioType.NULL_SEASONAL, ScenarioType.STEP_CHANGE, ScenarioType.SPIKE,
                ScenarioType.COVERAGE_OUTAGE, ScenarioType.LATE_REVISION);
    }

    /**
     * One reported change.
     *
     * @param change the bucket of the estimated change
     * @param alarm the alarm bucket
     * @param decision the decision bucket that first reported it
     * @param confirmed true when classified {@code confirmed}
     * @param direction +1 up, −1 down
     */
    record Detection(int change, int alarm, int decision, boolean confirmed, int direction) {}

    /** What one replay found. */
    record Replay(List<Detection> detections, int violations, int oracleDetections) {}

    @Override
    public ScenarioMetrics run(ScenarioType scenario, SuiteParams params, JobMonitor monitor) {
        CusumThresholds thresholds = new CusumThresholds();
        ChangePointDetector.Params p = ChangePointService.params(SeriesFrequency.DAY);
        int replicates = params.replicates();
        int confirmedNull = 0;
        int unknown = 0;
        int detections = 0;
        int truePositives = 0;
        int falsePositives = 0;
        List<Double> delays = new ArrayList<>();
        int spurious = 0;
        int wrongDown = 0;
        int violations = 0;
        int oracle = 0;
        Map<String, Object> truth = null;
        for (int r = 0; r < replicates; r++) {
            monitor.checkpoint((double) r / replicates);
            ScenarioGenerator.SeriesScenario sc = ScenarioGenerator.dailyCounts(scenario, params.seed(), r);
            if (truth == null) {
                truth = new LinkedHashMap<>(sc.truth());
            }
            Replay replay = replay(sc, p, thresholds, scenario == ScenarioType.LATE_REVISION);
            violations += replay.violations();
            oracle += replay.oracleDetections();
            detections += replay.detections().size();
            for (Detection d : replay.detections()) {
                if (!d.confirmed()) {
                    unknown++;
                }
            }
            switch (scenario) {
                case NULL_SEASONAL -> confirmedNull += (int) replay.detections().stream()
                        .filter(Detection::confirmed).count();
                case STEP_CHANGE -> {
                    int c = ((Number) sc.truth().get("changeIndex")).intValue();
                    int dir = "up".equals(sc.truth().get("direction")) ? 1 : -1;
                    boolean found = false;
                    for (Detection d : replay.detections()) {
                        if (!d.confirmed()) {
                            continue;
                        }
                        boolean hit = !found && d.direction() == dir && d.change() >= c - 7 && d.change() <= c + 14
                                && d.alarm() >= c;
                        if (hit) {
                            found = true;
                            truePositives++;
                            delays.add((d.alarm() - c) * 24.0);
                        } else {
                            falsePositives++;
                        }
                    }
                }
                case SPIKE -> {
                    @SuppressWarnings("unchecked")
                    int s = ((List<Number>) sc.truth().get("spikeIndexes")).get(0).intValue();
                    if (replay.detections().stream().anyMatch(d -> d.confirmed() && Math.abs(d.change() - s) <= 7)) {
                        spurious++;
                    }
                }
                case COVERAGE_OUTAGE -> {
                    int from = ((Number) sc.truth().get("outageFromIndex")).intValue();
                    int to = ((Number) sc.truth().get("outageToIndex")).intValue();
                    if (replay.detections().stream().anyMatch(d -> d.confirmed() && d.direction() < 0
                            && d.change() >= from - 3 && d.change() < to)) {
                        wrongDown++;
                    }
                }
                default -> {
                    // late-revision: counted through violations and oracle detections
                }
            }
        }
        Map<String, Double> m = new LinkedHashMap<>();
        m.put("detections", (double) detections);
        m.put("unknownDetections", (double) unknown);
        List<String> notes = new ArrayList<>();
        notes.add("Decisions every " + STEP + " days over the last " + ScenarioGenerator.EVAL_DAYS + " days of "
                + replicates + " replicate(s); 400 days of history, alarms in the last 7 buckets, as the nightly run.");
        switch (scenario) {
            case NULL_SEASONAL -> {
                double far = BenchmarkScorer.rate(confirmedNull, (long) replicates * ScenarioGenerator.EVAL_DAYS);
                double configured = 2.0 / p.targetArl();
                m.put("falseAlarmRate", far);
                m.put("configuredRate", configured);
                m.put("falseAlarmRatio", far / configured);
            }
            case STEP_CHANGE -> {
                m.put("recall", BenchmarkScorer.recall(truePositives, replicates));
                m.put("precision", BenchmarkScorer.precision(truePositives, truePositives + falsePositives));
                m.put("delayMedianHours", BenchmarkScorer.medianDelay(delays));
                m.put("delayP90Hours", BenchmarkScorer.p90Delay(delays));
            }
            case SPIKE -> m.put("spuriousChangeShare", BenchmarkScorer.rate(spurious, replicates));
            case COVERAGE_OUTAGE -> {
                m.put("correctUnknownShare", BenchmarkScorer.correctUnknownShare(wrongDown, replicates));
                notes.add("Detectability stand-in: a bucket below 0.8 coverage or detection probability fails the"
                        + " decline check, as 116 canSeeAbsence would.");
            }
            case LATE_REVISION -> {
                m.put("lookAheadViolations", (double) violations);
                m.put("oracleDetections", (double) oracle);
                notes.add("oracleDetections: confirmed changes in the revised span that a reading of every stored"
                        + " version, without asOf, would have reported.");
            }
            default -> {
                // no further metrics
            }
        }
        if (truth != null) {
            truth.put("replicates", replicates);
        }
        return new ScenarioMetrics(scenario.wire(), null, params.seed(), m, truth, notes);
    }

    /** Replays one series. */
    Replay replay(ScenarioGenerator.SeriesScenario sc, ChangePointDetector.Params p, CusumThresholds thresholds,
            boolean lookAheadChecks) {
        InMemorySeries s = sc.series();
        int history = ScenarioGenerator.HISTORY_DAYS;
        List<Detection> out = new ArrayList<>();
        int violations = 0;
        int oracle = 0;
        Integer revisedFrom = sc.truth().get("revisedFromIndex") instanceof Number n ? n.intValue() : null;
        Integer revisedTo = sc.truth().get("revisedToIndex") instanceof Number n ? n.intValue() : null;
        for (int d = sc.evalFrom(); d <= s.size(); d += STEP) {
            Instant decision = s.startOf(d);
            int from = d - history;
            SeriesData data = s.read(from, d, decision);
            SeriesData.Grid grid = data.grid().knownBy(decision);
            if (lookAheadChecks) {
                violations += lateValues(grid, decision);
                violations += lateValues(s.read(from, d, null).grid().knownBy(decision), decision);
                SeriesData.Grid all = s.read(from, d, null).grid();
                ChangePointDetector.Result o = detector.detect(all.starts(), all.values(), all.gap(), true, p,
                        thresholds);
                if (o.state() == ChangePointDetector.State.CONFIRMED && revisedFrom != null
                        && from + o.changeIndex() >= revisedFrom && from + o.changeIndex() < revisedTo) {
                    oracle++;
                }
            }
            ChangePointDetector.Result res = detector.detect(grid.starts(), grid.values(), grid.gap(), true, p,
                    thresholds);
            final int windowFrom = from;
            final int end = d;
            ChangePointService.Classification c = ChangePointService.classify(res, true, true, p.confirmPosterior(),
                    () -> s.canSeeDecline(windowFrom + res.changeIndex(), end));
            if (c == null || res.changeIndex() < 0) {
                continue;
            }
            int change = from + res.changeIndex();
            int alarm = res.alarmIndex() < 0 ? change : from + res.alarmIndex();
            int direction = res.direction();
            boolean seen = out.stream().anyMatch(x -> x.direction() == direction && Math.abs(x.change() - change) <= 3);
            if (!seen) {
                out.add(new Detection(change, alarm, d, c.confirmed(), direction));
            }
        }
        return new Replay(out, violations, oracle);
    }

    /** The covered values in a grid known only after the decision. */
    public static int lateValues(SeriesData.Grid grid, Instant decision) {
        int late = 0;
        for (int i = 0; i < grid.size(); i++) {
            if (!grid.gap()[i] && grid.knownAt()[i] != null && grid.knownAt()[i].isAfter(decision)) {
                late++;
            }
        }
        return late;
    }
}
