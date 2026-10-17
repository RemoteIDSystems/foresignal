package ai.foresignal.benchmark.component;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.analytics.changepoint.ChangePointDetector;
import ai.foresignal.backtest.JobMonitor;
import ai.foresignal.benchmark.BenchmarkInputs;
import ai.foresignal.benchmark.BenchmarkResults.ComponentResult;
import ai.foresignal.benchmark.BenchmarkResults.ScenarioMetrics;
import ai.foresignal.benchmark.BenchmarkResults.SuiteParams;
import ai.foresignal.benchmark.BenchmarkThresholds;
import ai.foresignal.benchmark.SyntheticSuite;
import ai.foresignal.benchmark.Verdict;
import ai.foresignal.benchmark.scenario.ScenarioType;
import ai.foresignal.evidence.CanonicalRules;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/**
 * The component benchmarks (prompt 123) in their pure mode with one replicate: every component measures every one of
 * its scenarios with the metrics its thresholds check, results are deterministic per seed, the properties planted
 * to hold by construction hold, and a broken detector double fails.
 */
class ComponentBenchmarksTest {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final SuiteParams ONE = new SuiteParams(1_230_001L, 1, 20);

    static List<ComponentBenchmark> all() {
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
    @DisplayName("every component is in the API's list, and every scenario reports the metrics its thresholds check")
    void everyScenarioMeasured() {
        BenchmarkThresholds thresholds = BenchmarkThresholds.bundled(JSON);
        List<ComponentBenchmark> components = all();
        assertThat(components).extracting(ComponentBenchmark::name)
                .containsExactlyInAnyOrderElementsOf(BenchmarkInputs.COMPONENTS);
        for (ComponentBenchmark c : components) {
            assertThat(c.version()).as(c.name()).isNotBlank();
            for (ScenarioType scenario : c.scenarios()) {
                ScenarioMetrics m = c.run(scenario, ONE, JobMonitor.NONE);
                assertThat(m.scenario()).isEqualTo(scenario.wire());
                assertThat(m.seed()).isEqualTo(ONE.seed());
                assertThat(m.truth()).as(c.name() + " " + scenario.wire()).isNotEmpty();
                for (BenchmarkThresholds.Rule rule : thresholds.rules(c.name())) {
                    if (scenario.wire().equals(rule.scenario())) {
                        assertThat(m.metrics()).as(c.name() + " " + scenario.wire()).containsKey(rule.metric());
                    }
                }
            }
        }
    }

    @Test
    @DisplayName("results are identical for the same seed")
    void deterministic() {
        for (ComponentBenchmark c : List.of(new EvidenceBenchmark(JSON), new CanonicalLinkingBenchmark(
                new CanonicalRules(JSON)), new SituationLinkerBenchmark(), new CoOccurrenceBenchmark(),
                new ForecasterBenchmark(ForecasterBenchmark.Kind.BASE_RATE), new AnomalyBenchmark())) {
            for (ScenarioType s : c.scenarios()) {
                assertThat(c.run(s, ONE, JobMonitor.NONE).metrics()).as(c.name() + " " + s.wire())
                        .isEqualTo(c.run(s, ONE, JobMonitor.NONE).metrics());
            }
        }
    }

    @Test
    @DisplayName("duplicate echoes inflate nothing, and planted multi-source events link exactly (by construction)")
    void constructedProperties() {
        EvidenceBenchmark evidence = new EvidenceBenchmark(JSON);
        ScenarioMetrics echo = evidence.run(ScenarioType.DUPLICATE_ECHO, new SuiteParams(1_230_001L, 3, 20),
                JobMonitor.NONE);
        assertThat(echo.metrics().get("duplicateInflation")).isZero();
        assertThat(echo.metrics().get("wrongGroupCountShare")).isZero();
        assertThat(echo.metrics().get("meanGroupsAfter")).isEqualTo(echo.metrics().get("meanGroupsBefore"));
        ScenarioMetrics linking = new CanonicalLinkingBenchmark(new CanonicalRules(JSON))
                .run(ScenarioType.DUPLICATE_ECHO, ONE, JobMonitor.NONE);
        assertThat(linking.metrics().get("linkingPrecision")).isEqualTo(1.0);
        assertThat(linking.metrics().get("linkingRecall")).isEqualTo(1.0);
        assertThat(linking.metrics().get("spuriousMerges")).isZero();
    }

    @Test
    @DisplayName("the change-point replay never reads a value known after the decision")
    void noLookAhead() {
        ScenarioMetrics m = new ChangePointBenchmark().run(ScenarioType.LATE_REVISION, ONE, JobMonitor.NONE);
        assertThat(m.metrics().get("lookAheadViolations")).isZero();
    }

    @Test
    @DisplayName("a broken detector (fires on every run) fails its null-data threshold")
    void brokenDetectorFails() {
        ChangePointBenchmark broken = new ChangePointBenchmark(ComponentBenchmarksTest::alwaysFires);
        ComponentResult result = SyntheticSuite.run(List.of(broken), ONE, BenchmarkThresholds.bundled(JSON),
                JobMonitor.NONE, 0, 1).get(0);
        assertThat(result.status()).isEqualTo(Verdict.FAIL);
        assertThat(result.scenarios().get(0).metrics().metrics().get("falseAlarmRatio")).isGreaterThan(3.0);
    }

    @Test
    @DisplayName("a component that throws gets a failing scenario naming the error")
    void throwingComponent() {
        ComponentBenchmark failing = new ComponentBenchmark() {
            @Override
            public String name() {
                return "change-point";
            }

            @Override
            public String version() {
                return "broken 0.0.0";
            }

            @Override
            public List<ScenarioType> scenarios() {
                return List.of(ScenarioType.NULL_SEASONAL);
            }

            @Override
            public ScenarioMetrics run(ScenarioType scenario, SuiteParams params, JobMonitor monitor) {
                throw new IllegalStateException("boom");
            }
        };
        ComponentResult result = SyntheticSuite.run(List.of(failing), ONE, BenchmarkThresholds.bundled(JSON),
                JobMonitor.NONE, 0, 1).get(0);
        assertThat(result.status()).isEqualTo(Verdict.FAIL);
        assertThat(result.scenarios().get(0).metrics().notes()).anyMatch(n -> n.contains("boom"));
        Map<String, Object> view = result.toMap();
        assertThat(view.get("componentVersion")).isEqualTo("broken 0.0.0");
    }

    /** A detector double that reports a confirmed upward change at the last bucket of every window. */
    static ChangePointDetector.Result alwaysFires(Instant[] starts, double[] x, boolean[] gap, boolean counts,
            ChangePointDetector.Params p, ai.foresignal.analytics.math.CusumThresholds t) {
        int n = x.length;
        int change = n - 3;
        return new ChangePointDetector.Result(ChangePointDetector.State.CONFIRMED, 1, change, n - 1, starts[change],
                starts[n - 1], 10, 20, 1.0, 0.99, 5, "POISSON", 0, n);
    }
}
