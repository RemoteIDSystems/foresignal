package ai.foresignal.benchmark.component;

import ai.foresignal.analytics.cooccurrence.CoOccurrenceMiner;
import ai.foresignal.backtest.JobMonitor;
import ai.foresignal.benchmark.BenchmarkResults.ScenarioMetrics;
import ai.foresignal.benchmark.BenchmarkResults.SuiteParams;
import ai.foresignal.benchmark.BenchmarkScorer;
import ai.foresignal.benchmark.scenario.ScenarioGenerator;
import ai.foresignal.benchmark.scenario.ScenarioType;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.stereotype.Component;

/**
 * Benchmarks the 119 co-occurrence miner ({@code co-occurrence}) at its live false-discovery level q = 0.05: each of
 * the last 28 days is mined with the history before it (as the nightly run mines the last closed day).
 *
 * <ul>
 *   <li>{@code null-seasonal}: the false-discovery rate, under the global null the share of days with any discovery
 *       (BH controls it at q).</li>
 *   <li>{@code spike}: recall of the planted joint spikes, precision and the false-discovery rate of the
 *       discoveries.</li>
 * </ul>
 */
@Component
public class CoOccurrenceBenchmark implements ComponentBenchmark {

    /** The component name. */
    public static final String NAME = "co-occurrence";

    /** The live false-discovery level. */
    public static final double Q = 0.05;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String version() {
        return CoOccurrenceMiner.NAME + " " + CoOccurrenceMiner.VERSION;
    }

    @Override
    public List<ScenarioType> scenarios() {
        return List.of(ScenarioType.NULL_SEASONAL, ScenarioType.SPIKE);
    }

    @Override
    public ScenarioMetrics run(ScenarioType scenario, SuiteParams params, JobMonitor monitor) {
        int replicates = params.replicates();
        long days = 0;
        long daysWithDiscovery = 0;
        long discoveries = 0;
        long trueDiscoveries = 0;
        long planted = 0;
        Map<String, Object> truth = null;
        for (int r = 0; r < replicates; r++) {
            monitor.checkpoint((double) r / replicates);
            ScenarioGenerator.CoOccurrenceScenario sc = ScenarioGenerator.coOccurrence(scenario, params.seed(), r);
            if (truth == null) {
                truth = new LinkedHashMap<>(sc.truth());
            }
            planted += sc.planted().size();
            int length = sc.counts().values().iterator().next().values().iterator().next().length;
            for (int d = sc.evalFrom(); d < length; d++) {
                days++;
                int found = 0;
                for (CoOccurrenceMiner.Test t : CoOccurrenceMiner.mine(slice(sc.counts(), d + 1), Q)) {
                    if (!t.survives()) {
                        continue;
                    }
                    found++;
                    if (sc.planted().contains(d + "|" + t.region() + "|" + t.familyA() + "|" + t.familyB())) {
                        trueDiscoveries++;
                    }
                }
                discoveries += found;
                if (found > 0) {
                    daysWithDiscovery++;
                }
            }
        }
        Map<String, Double> m = new LinkedHashMap<>();
        m.put("minedDays", (double) days);
        m.put("discoveries", (double) discoveries);
        if (scenario == ScenarioType.NULL_SEASONAL) {
            m.put("fdr", BenchmarkScorer.rate(daysWithDiscovery, days));
        } else {
            m.put("recall", BenchmarkScorer.recall(trueDiscoveries, planted));
            m.put("precision", BenchmarkScorer.precision(trueDiscoveries, discoveries));
            m.put("fdr", BenchmarkScorer.fdr(discoveries - trueDiscoveries, discoveries));
        }
        m.put("q", Q);
        if (truth != null) {
            truth.put("replicates", replicates);
        }
        return new ScenarioMetrics(scenario.wire(), null, params.seed(), m, truth, List.of("Four regions of four"
                + " families; each of the last 28 days mined with the history before it at q = " + Q + "."));
    }

    /** The counts up to (excluding) day {@code end}, every day covered. */
    static Map<String, Map<String, CoOccurrenceMiner.Daily>> slice(Map<String, Map<String, double[]>> counts,
            int end) {
        Map<String, Map<String, CoOccurrenceMiner.Daily>> out = new TreeMap<>();
        counts.forEach((region, families) -> {
            Map<String, CoOccurrenceMiner.Daily> byFamily = new TreeMap<>();
            families.forEach((family, values) -> {
                boolean[] ok = new boolean[end];
                Arrays.fill(ok, true);
                byFamily.put(family, new CoOccurrenceMiner.Daily(Arrays.copyOf(values, end), ok));
            });
            out.put(region, byFamily);
        });
        return out;
    }
}
