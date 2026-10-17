package ai.foresignal.benchmark.component;

import ai.foresignal.backtest.JobMonitor;
import ai.foresignal.benchmark.BenchmarkResults.ScenarioMetrics;
import ai.foresignal.benchmark.BenchmarkResults.SuiteParams;
import ai.foresignal.benchmark.BenchmarkScorer;
import ai.foresignal.benchmark.scenario.ScenarioGenerator;
import ai.foresignal.benchmark.scenario.ScenarioType;
import ai.foresignal.situation.LinkerWeights;
import ai.foresignal.situation.SituationLinker;
import ai.foresignal.situation.SituationScorer;
import ai.foresignal.situation.SituationSettings;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Benchmarks the 118 situation linker ({@code situation-linker}) in the isolated mode of its dry run
 * ({@link SituationLinker#simulate}: the same procedure from an empty state, in memory) at the default link threshold
 * and windows. Every input is assigned the situation it joined or seeded; pairwise precision and recall against the
 * planted storylines, and spurious merges (situations mixing storylines or unrelated inputs), are reported.
 *
 * <ul>
 *   <li>{@code spike}: five planted storylines (bursts of related inputs) among unrelated background inputs.</li>
 *   <li>{@code null-seasonal}: unrelated inputs only; any merge is spurious.</li>
 * </ul>
 */
@Component
public class SituationLinkerBenchmark implements ComponentBenchmark {

    /** The component name. */
    public static final String NAME = "situation-linker";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String version() {
        return SituationScorer.NAME + " " + SituationScorer.VERSION;
    }

    @Override
    public List<ScenarioType> scenarios() {
        return List.of(ScenarioType.SPIKE, ScenarioType.NULL_SEASONAL);
    }

    @Override
    public ScenarioMetrics run(ScenarioType scenario, SuiteParams params, JobMonitor monitor) {
        int replicates = params.replicates();
        BenchmarkScorer.PairCounts pairs = new BenchmarkScorer.PairCounts(0, 0, 0);
        int spurious = 0;
        int situations = 0;
        Map<String, Object> truth = null;
        for (int r = 0; r < replicates; r++) {
            monitor.checkpoint((double) r / replicates);
            ScenarioGenerator.StorylineScenario sc = ScenarioGenerator.storylines(scenario, params.seed(), r);
            if (truth == null) {
                truth = new LinkedHashMap<>(sc.truth());
            }
            List<SituationLinker.Decision> decisions = SituationLinker.simulate(sc.inputs(),
                    LinkerWeights.DEFAULT.linkThreshold(), SituationSettings.DORMANCY_WINDOW,
                    SituationSettings.REOPEN_WINDOW);
            List<String> assigned = new ArrayList<>();
            java.util.Set<String> distinct = new java.util.HashSet<>();
            for (SituationLinker.Decision d : decisions) {
                assigned.add(d.situationId());
                if (d.situationId() != null) {
                    distinct.add(d.situationId());
                }
            }
            situations += distinct.size();
            pairs = pairs.plus(BenchmarkScorer.pairs(sc.truthGroup(), assigned));
            spurious += BenchmarkScorer.spuriousMerges(sc.truthGroup(), assigned);
        }
        Map<String, Double> m = new LinkedHashMap<>();
        if (scenario == ScenarioType.SPIKE) {
            m.put("linkingPrecision", pairs.precision());
            m.put("linkingRecall", pairs.recall());
        }
        m.put("spuriousMerges", (double) spurious);
        m.put("situations", (double) situations);
        m.put("linkThreshold", LinkerWeights.DEFAULT.linkThreshold());
        if (truth != null) {
            truth.put("replicates", replicates);
        }
        return new ScenarioMetrics(scenario.wire(), null, params.seed(), m, truth, List.of("Isolated dry run from an"
                + " empty state at the default threshold, dormancy and reopen windows; text similarity off."));
    }
}
