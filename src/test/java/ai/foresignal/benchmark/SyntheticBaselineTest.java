package ai.foresignal.benchmark;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.backtest.JobMonitor;
import ai.foresignal.benchmark.BenchmarkResults.ComponentResult;
import ai.foresignal.benchmark.BenchmarkResults.ScenarioResult;
import ai.foresignal.benchmark.BenchmarkResults.SuiteParams;
import ai.foresignal.benchmark.component.AnomalyBenchmark;
import ai.foresignal.benchmark.component.CanonicalLinkingBenchmark;
import ai.foresignal.benchmark.component.ChangePointBenchmark;
import ai.foresignal.benchmark.component.CoOccurrenceBenchmark;
import ai.foresignal.benchmark.component.ComponentBenchmark;
import ai.foresignal.benchmark.component.EventStudyBenchmark;
import ai.foresignal.benchmark.component.EvidenceBenchmark;
import ai.foresignal.benchmark.component.ForecasterBenchmark;
import ai.foresignal.benchmark.component.PrecursorBenchmark;
import ai.foresignal.benchmark.component.SituationLinkerBenchmark;
import ai.foresignal.evidence.CanonicalRules;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * The 123 synthetic benchmark at its bundled parameters (seed, replicates and minimum bin count from
 * {@code intel/benchmark-thresholds.json}), over every component in its pure mode (checkpoint 124, focus area 9).
 *
 * <p>Every component must be {@code pass} or {@code warn}. The results are written to
 * {@code build/reports/benchmark/synthetic-baseline.json}: they are the baseline checkpoint 139 compares against.
 */
class SyntheticBaselineTest {

    static final JsonMapper JSON = JsonMapper.builder().enable(SerializationFeature.INDENT_OUTPUT).build();

    /** Every component benchmark in its pure mode, as the 123 harness registers them. */
    static List<ComponentBenchmark> components() {
        List<ComponentBenchmark> out = new ArrayList<>();
        out.add(new ChangePointBenchmark());
        out.add(new AnomalyBenchmark());
        out.add(new EventStudyBenchmark());
        out.add(new CoOccurrenceBenchmark());
        out.add(new EvidenceBenchmark(JSON));
        out.add(new CanonicalLinkingBenchmark(new CanonicalRules(JSON)));
        out.add(new PrecursorBenchmark());
        out.add(new SituationLinkerBenchmark());
        for (ForecasterBenchmark.Kind kind : ForecasterBenchmark.Kind.values()) {
            out.add(new ForecasterBenchmark(kind));
        }
        return out;
    }

    @Test
    @DisplayName("the synthetic suite at the bundled parameters: every component passes or warns; the results are kept")
    void syntheticSuiteHasNoFail() throws Exception {
        BenchmarkThresholds thresholds = BenchmarkThresholds.bundled(JSON);
        SuiteParams params = thresholds.suite(null);
        List<ComponentResult> results = SyntheticSuite.run(components(), params, thresholds,
                JobMonitor.NONE, 0, 1);

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("params", params.toMap());
        List<Map<String, Object>> components = new ArrayList<>();
        List<String> failing = new ArrayList<>();
        for (ComponentResult result : results) {
            components.add(result.toMap());
            if (result.status() == Verdict.FAIL) {
                List<String> checks = new ArrayList<>();
                for (ScenarioResult scenario : result.scenarios()) {
                    scenario.checks().stream().filter(c -> c.status() == Verdict.FAIL)
                            .forEach(c -> checks.add(scenario.metrics().scenario() + ":" + c.metric() + "="
                                    + c.observed() + " (" + c.comparator() + " " + c.threshold() + ")"));
                }
                failing.add(result.component() + " " + checks);
            }
        }
        report.put("components", components);
        Path out = Path.of("build/reports/benchmark/synthetic-baseline.json");
        Files.createDirectories(out.getParent());
        Files.writeString(out, JSON.writeValueAsString(report));

        assertThat(results).extracting(ComponentResult::component)
                .containsExactlyInAnyOrderElementsOf(BenchmarkInputs.COMPONENTS);
        assertThat(failing).as("components that fail the synthetic benchmark").isEmpty();
    }
}
