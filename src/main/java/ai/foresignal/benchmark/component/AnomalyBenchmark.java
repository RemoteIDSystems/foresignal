package ai.foresignal.benchmark.component;

import ai.foresignal.backtest.JobMonitor;
import ai.foresignal.benchmark.BenchmarkResults.ScenarioMetrics;
import ai.foresignal.benchmark.BenchmarkResults.SuiteParams;
import ai.foresignal.benchmark.BenchmarkScorer;
import ai.foresignal.benchmark.scenario.ScenarioGenerator;
import ai.foresignal.benchmark.scenario.ScenarioType;
import ai.foresignal.trigger.window.AnomalyDetector;
import ai.foresignal.trigger.window.Baseline;
import ai.foresignal.trigger.window.WindowSpec;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Benchmarks the 100 anomaly detector ({@code anomaly}, {@code poisson-tail}) on hourly counts with an
 * {@code hour-of-week} baseline of the previous up to twelve weeks' same-slot, coverage-ok, unflagged samples, at
 * medium sensitivity (α 1e-3), direction {@code both} and a minimum absolute delta of 3, exactly through
 * {@link Baseline#of} and {@link AnomalyDetector#evaluate}.
 *
 * <ul>
 *   <li>{@code null-seasonal}: alerts per evaluated hour against α ({@code falseAlarmRatio}).</li>
 *   <li>{@code spike}: recall of three-hour fourfold bursts and the delay to the first alert.</li>
 *   <li>{@code coverage-outage}: the share of outage hours never classified {@code activity-down}.</li>
 * </ul>
 */
@Component
public class AnomalyBenchmark implements ComponentBenchmark {

    /** The component name. */
    public static final String NAME = "anomaly";

    /** The rule the benchmark evaluates with. */
    static final WindowSpec SPEC = new WindowSpec(Duration.ofHours(1), null, null, null, 0, null, null,
            Duration.ofHours(1), "hour-of-week", "both", "medium", 3, 8, 0.8);

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String version() {
        return AnomalyDetector.NAME + " " + AnomalyDetector.VERSION;
    }

    @Override
    public List<ScenarioType> scenarios() {
        return List.of(ScenarioType.NULL_SEASONAL, ScenarioType.SPIKE, ScenarioType.COVERAGE_OUTAGE);
    }

    @Override
    public ScenarioMetrics run(ScenarioType scenario, SuiteParams params, JobMonitor monitor) {
        int replicates = params.replicates();
        long evaluated = 0;
        long fired = 0;
        int spikesFound = 0;
        List<Double> delays = new ArrayList<>();
        long outageHours = 0;
        long wrongDown = 0;
        Map<String, Object> truth = null;
        for (int r = 0; r < replicates; r++) {
            monitor.checkpoint((double) r / replicates);
            ScenarioGenerator.HourlyScenario sc = ScenarioGenerator.hourlyCounts(scenario, params.seed(), r);
            if (truth == null) {
                truth = new LinkedHashMap<>(sc.truth());
            }
            AnomalyDetector.Result[] results = evaluate(sc);
            int spike = sc.truth().get("spikeFromIndex") instanceof Number n ? n.intValue() : -1;
            int outage = sc.truth().get("outageFromIndex") instanceof Number n ? n.intValue() : -1;
            boolean found = false;
            for (int h = sc.evalFrom(); h < sc.counts().length; h++) {
                AnomalyDetector.Result res = results[h];
                evaluated++;
                if (res.fire()) {
                    fired++;
                }
                if (spike >= 0 && !found && h >= spike && h < spike + 3 && res.fire()) {
                    found = true;
                    delays.add((double) (h - spike));
                }
                if (outage >= 0 && h >= outage && h < outage + 12) {
                    outageHours++;
                    if ("activity-down".equals(res.classification())) {
                        wrongDown++;
                    }
                }
            }
            if (found) {
                spikesFound++;
            }
        }
        Map<String, Double> m = new LinkedHashMap<>();
        m.put("evaluatedBuckets", (double) evaluated);
        m.put("alerts", (double) fired);
        switch (scenario) {
            case NULL_SEASONAL -> {
                double far = BenchmarkScorer.rate(fired, evaluated);
                m.put("falseAlarmRate", far);
                m.put("configuredRate", SPEC.alpha());
                m.put("falseAlarmRatio", far / SPEC.alpha());
            }
            case SPIKE -> {
                m.put("recall", BenchmarkScorer.recall(spikesFound, replicates));
                m.put("delayMedianHours", BenchmarkScorer.medianDelay(delays));
                m.put("delayP90Hours", BenchmarkScorer.p90Delay(delays));
            }
            case COVERAGE_OUTAGE -> m.put("correctUnknownShare",
                    BenchmarkScorer.correctUnknownShare(wrongDown, outageHours));
            default -> {
                // no further metrics
            }
        }
        if (truth != null) {
            truth.put("replicates", replicates);
        }
        return new ScenarioMetrics(scenario.wire(), null, params.seed(), m, truth, List.of("Hourly buckets, "
                + ScenarioGenerator.HOURLY_HISTORY_WEEKS + " weeks of history, two weeks evaluated; rule "
                + SPEC.toMap() + "."));
    }

    /** Evaluates every hour from the evaluation start as the 100 engine does, flagged samples left out of baselines. */
    static AnomalyDetector.Result[] evaluate(ScenarioGenerator.HourlyScenario sc) {
        int n = sc.counts().length;
        AnomalyDetector.Result[] out = new AnomalyDetector.Result[n];
        boolean[] flagged = new boolean[n];
        for (int h = sc.evalFrom(); h < n; h++) {
            List<Integer> counts = new ArrayList<>();
            List<Double> coverages = new ArrayList<>();
            for (int k = 1; k <= 12 && h - 168 * k >= 0; k++) {
                int s = h - 168 * k;
                if (sc.coverage()[s] >= SPEC.coverageMin() && !flagged[s]) {
                    counts.add(sc.counts()[s]);
                    coverages.add(sc.coverage()[s]);
                }
            }
            Baseline baseline = Baseline.of(counts, coverages, SPEC.slotMode(), SPEC.bucket());
            double ratio = sc.coverage()[h];
            out[h] = AnomalyDetector.evaluate(sc.counts()[h], baseline, ratio, ratio >= SPEC.coverageMin(), SPEC);
            flagged[h] = out[h].fire();
        }
        return out;
    }
}
