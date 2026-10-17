package ai.foresignal.benchmark;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.benchmark.BenchmarkResults.Check;
import ai.foresignal.benchmark.BenchmarkResults.ComponentResult;
import ai.foresignal.benchmark.BenchmarkResults.ScenarioMetrics;
import ai.foresignal.benchmark.BenchmarkResults.ScenarioResult;
import ai.foresignal.benchmark.BenchmarkResults.SuiteParams;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@link BenchmarkScorer} 1.0.0 (prompt 123) on hand-built detector outputs against reference values (false-discovery
 * rate, recall, precision, delays, correct unknowns, duplicate inflation, pairwise linking, Brier, log score,
 * reliability, CORP, skill), and the threshold verdicts of {@link BenchmarkThresholds}.
 */
class BenchmarkScorerTest {

    @Test
    @DisplayName("FDR is V / max(R, 1); recall and precision divide by planted and discoveries (NaN without)")
    void rates() {
        assertThat(BenchmarkScorer.fdr(2, 10)).isCloseTo(0.2, within(1e-12));
        assertThat(BenchmarkScorer.fdr(0, 0)).isZero();
        assertThat(BenchmarkScorer.recall(3, 4)).isCloseTo(0.75, within(1e-12));
        assertThat(BenchmarkScorer.recall(0, 0)).isNaN();
        assertThat(BenchmarkScorer.precision(3, 5)).isCloseTo(0.6, within(1e-12));
        assertThat(BenchmarkScorer.precision(0, 0)).isNaN();
        assertThat(BenchmarkScorer.rate(1, 4)).isCloseTo(0.25, within(1e-12));
        assertThat(BenchmarkScorer.rate(1, 0)).isNaN();
        assertThat(BenchmarkScorer.correctUnknownShare(1, 20)).isCloseTo(0.95, within(1e-12));
        assertThat(BenchmarkScorer.correctUnknownShare(0, 0)).isNaN();
    }

    @Test
    @DisplayName("delays: median and type-7 90th percentile")
    void delays() {
        List<Double> d = List.of(10.0, 1.0, 3.0, 2.0, 4.0);
        assertThat(BenchmarkScorer.medianDelay(d)).isCloseTo(3.0, within(1e-12));
        // h = 4 · 0.9 = 3.6 → 4 + 0.6 · (10 − 4)
        assertThat(BenchmarkScorer.p90Delay(d)).isCloseTo(7.6, within(1e-12));
        assertThat(BenchmarkScorer.medianDelay(List.of())).isNaN();
    }

    @Test
    @DisplayName("duplicate inflation: added groups, plus one for a confidence rise and one for a quality rise")
    void duplicateInflation() {
        assertThat(BenchmarkScorer.duplicateInflation(1, 1, 0.5, 0.5, "weak", "weak")).isZero();
        assertThat(BenchmarkScorer.duplicateInflation(1, 3, 0.5, 0.5, "weak", "weak")).isEqualTo(2.0);
        assertThat(BenchmarkScorer.duplicateInflation(1, 1, 0.5, 0.6, "weak", "weak")).isEqualTo(1.0);
        assertThat(BenchmarkScorer.duplicateInflation(1, 1, 0.5, 0.5, "weak", "moderate")).isEqualTo(1.0);
        assertThat(BenchmarkScorer.duplicateInflation(2, 1, 0.6, 0.5, "moderate", "weak")).isZero();
    }

    @Test
    @DisplayName("pairwise linking precision and recall, and spurious merges")
    void pairs() {
        List<String> truth = List.of("a", "a", "b", "b");
        List<String> assigned = Arrays.asList("x", "x", "x", null);
        BenchmarkScorer.PairCounts p = BenchmarkScorer.pairs(truth, assigned);
        assertThat(p.truePairs()).isEqualTo(1);
        assertThat(p.falsePairs()).isEqualTo(2);
        assertThat(p.missedPairs()).isEqualTo(1);
        assertThat(p.precision()).isCloseTo(1.0 / 3, within(1e-12));
        assertThat(p.recall()).isCloseTo(0.5, within(1e-12));
        assertThat(p.plus(p).truePairs()).isEqualTo(2);
        assertThat(BenchmarkScorer.spuriousMerges(truth, assigned)).isEqualTo(1);
        assertThat(BenchmarkScorer.spuriousMerges(truth, List.of("x", "x", "y", "y"))).isZero();
        assertThat(BenchmarkScorer.pairs(truth, List.of("x", "x", "y", "y")).precision()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("forecasts: Brier, log score, reliability over populated bins, CORP miscalibration and skill")
    void forecasts() {
        assertThat(BenchmarkScorer.brier(new double[] {0.8, 0.2}, new boolean[] {true, false}))
                .isCloseTo(0.04, within(1e-12));
        assertThat(BenchmarkScorer.logScore(new double[] {0.8, 0.2}, new boolean[] {true, false}))
                .isCloseTo(-Math.log(0.8), within(1e-12));
        assertThat(BenchmarkScorer.brier(new double[0], new boolean[0])).isNaN();
        double[] p = new double[15];
        boolean[] o = new boolean[15];
        for (int i = 0; i < 10; i++) {
            p[i] = 0.15;
            o[i] = i < 2;
        }
        for (int i = 10; i < 15; i++) {
            p[i] = 0.75;
            o[i] = true;
        }
        // Bin [0.1, 0.2): mean 0.15, observed 0.2; the bin of 0.75 holds only 5 forecasts and is ignored.
        assertThat(BenchmarkScorer.reliabilityMaxDeviation(p, o, 10)).isCloseTo(0.05, within(1e-9));
        assertThat(BenchmarkScorer.populatedBins(p, o, 10)).isEqualTo(1);
        assertThat(BenchmarkScorer.reliabilityMaxDeviation(p, o, 100)).isNaN();
        double[] half = {0.5, 0.5, 0.5, 0.5};
        assertThat(BenchmarkScorer.corpMiscalibration(half, new boolean[] {true, false, true, false}))
                .isCloseTo(0.0, within(1e-12));
        assertThat(BenchmarkScorer.skill(0.18, 0.2)).isCloseTo(0.1, within(1e-12));
        assertThat(BenchmarkScorer.skill(Double.NaN, 0.2)).isNaN();
        assertThat(BenchmarkScorer.toDoubles(new boolean[] {true, false})).containsExactly(1.0, 0.0);
    }

    @Test
    @DisplayName("verdicts: le, ge, eq and within against pass and warn thresholds; not measurable warns")
    void verdicts() {
        assertThat(BenchmarkThresholds.verdict("le", 1, 2.0, 0.5)).isEqualTo(Verdict.PASS);
        assertThat(BenchmarkThresholds.verdict("le", 1, 2.0, 1.5)).isEqualTo(Verdict.WARN);
        assertThat(BenchmarkThresholds.verdict("le", 1, 2.0, 3)).isEqualTo(Verdict.FAIL);
        assertThat(BenchmarkThresholds.verdict("ge", 0.8, 0.6, 0.9)).isEqualTo(Verdict.PASS);
        assertThat(BenchmarkThresholds.verdict("ge", 0.8, 0.6, 0.7)).isEqualTo(Verdict.WARN);
        assertThat(BenchmarkThresholds.verdict("ge", 0.8, 0.6, 0.5)).isEqualTo(Verdict.FAIL);
        assertThat(BenchmarkThresholds.verdict("eq", 0, null, 0)).isEqualTo(Verdict.PASS);
        assertThat(BenchmarkThresholds.verdict("eq", 0, null, 1)).isEqualTo(Verdict.FAIL);
        assertThat(BenchmarkThresholds.verdict("eq", 0, 1.0, 1)).isEqualTo(Verdict.WARN);
        assertThat(BenchmarkThresholds.verdict("within", 0.05, 0.1, -0.04)).isEqualTo(Verdict.PASS);
        assertThat(BenchmarkThresholds.verdict("within", 0.05, 0.1, 0.07)).isEqualTo(Verdict.WARN);
        assertThat(BenchmarkThresholds.verdict("within", 0.05, 0.1, 0.2)).isEqualTo(Verdict.FAIL);
        BenchmarkThresholds.Rule rule = new BenchmarkThresholds.Rule("spike", "recall", "ge", 0.8, 0.6, "n");
        assertThat(BenchmarkThresholds.check(rule, "spike", null).status()).isEqualTo(Verdict.WARN);
        assertThat(BenchmarkThresholds.check(rule, "spike", Double.NaN).observed()).isNull();
        assertThat(Verdict.worst(List.of(Verdict.PASS, Verdict.WARN))).isEqualTo(Verdict.WARN);
        assertThat(Verdict.worst(List.of(Verdict.WARN, Verdict.FAIL, Verdict.PASS))).isEqualTo(Verdict.FAIL);
        assertThat(Verdict.worst(List.of())).isEqualTo(Verdict.PASS);
        assertThat(Verdict.fromWire("warn")).isEqualTo(Verdict.WARN);
        assertThat(Verdict.fromWire("unknown")).isNull();
    }

    @Test
    @DisplayName("thresholds: evaluate per scenario, merge into a component result with headline metrics")
    void evaluate() {
        Map<String, Object> doc = new LinkedHashMap<>();
        doc.put("version", "t1");
        doc.put("suite", Map.of("seed", 7, "replicates", 3, "minBinCount", 20));
        doc.put("components", Map.of("x", List.of(
                Map.of("scenario", "spike", "metric", "recall", "comparator", "ge", "pass", 0.8, "warn", 0.6),
                Map.of("scenario", "step-change", "metric", "recall", "comparator", "ge", "pass", 0.8),
                Map.of("scenario", "spike", "metric", "fdr", "comparator", "le", "pass", 0.1),
                Map.of("scenario", "spike", "metric", "bogus", "comparator", "nope", "pass", 1))));
        doc.put("gating", Map.of("llmEnsembleWeight", List.of("x"), "findingTypes", Map.of("change-point", "x")));
        BenchmarkThresholds t = BenchmarkThresholds.of(doc);
        assertThat(t.version()).isEqualTo("t1");
        SuiteParams params = t.suite(null);
        assertThat(params).isEqualTo(new SuiteParams(7, 3, 20));
        assertThat(t.suite(42L).seed()).isEqualTo(42);
        assertThat(t.rules("x")).hasSize(3);
        assertThat(t.rules("missing")).isEmpty();
        assertThat(t.dependencies("llmEnsembleWeight")).containsExactly("x");
        assertThat(t.componentOfFindingType("change-point")).isEqualTo("x");
        assertThat(t.componentOfFindingType("co-occurrence")).isNull();
        ScenarioResult spike = t.evaluate("x", new ScenarioMetrics("spike", null, 7L,
                Map.of("recall", 0.7, "fdr", 0.05), Map.of(), List.of()));
        assertThat(spike.checks()).extracting(Check::metric).containsExactly("recall", "fdr");
        assertThat(spike.status()).isEqualTo(Verdict.WARN);
        ScenarioResult step = t.evaluate("x", new ScenarioMetrics("step-change", null, 7L, Map.of("recall", 0.9),
                Map.of(), List.of()));
        assertThat(step.status()).isEqualTo(Verdict.PASS);
        ComponentResult result = new ComponentResult("x", "x 1.0.0", List.of(spike, step), List.of());
        assertThat(result.status()).isEqualTo(Verdict.WARN);
        assertThat(result.headline()).containsOnlyKeys("recall@spike", "recall@step-change", "fdr");
        Map<String, Object> view = result.toMap();
        assertThat(view.get("status")).isEqualTo("warn");
        assertThat((List<?>) view.get("scenarios")).hasSize(2);
        assertThat((List<?>) view.get("thresholds")).hasSize(3);
        assertThat(BenchmarkResults.numbers(Map.of("a", Double.NaN))).containsEntry("a", null);
    }

    @Test
    @DisplayName("the bundled thresholds cover every component, with the prompt's defaults")
    void bundled() {
        BenchmarkThresholds t = BenchmarkThresholds.bundled(JsonMapper.builder().build());
        for (String component : BenchmarkInputs.COMPONENTS) {
            assertThat(t.rules(component)).as(component).isNotEmpty();
        }
        assertThat(t.suite(null).replicates()).isEqualTo(10);
        List<String> found = new ArrayList<>();
        for (BenchmarkThresholds.Rule r : t.rules("change-point")) {
            found.add(r.scenario() + ":" + r.metric() + ":" + r.pass());
        }
        assertThat(found).contains("null-seasonal:falseAlarmRatio:1.5", "coverage-outage:correctUnknownShare:0.95",
                "step-change:recall:0.8");
        assertThat(t.rules("co-occurrence")).anyMatch(r -> r.metric().equals("fdr") && r.pass() == 0.1);
        assertThat(t.rules("evidence")).anyMatch(r -> r.metric().equals("duplicateInflation") && r.pass() == 0);
        assertThat(t.rules("precursor")).anyMatch(r -> r.scenario().equals("common-driver")
                && r.metric().equals("falseValidationRate") && r.pass() == 0.05);
        assertThat(t.rules("forecaster:base-rate")).anyMatch(r -> r.metric().equals("reliabilityMaxDeviation")
                && r.comparator().equals("within") && r.pass() == 0.05);
        assertThat(t.dependencies(BenchmarkGate.LLM_ENSEMBLE_WEIGHT)).isNotEmpty();
        assertThat(t.dependencies(BenchmarkGate.PROPOSER_AUTO_EXECUTION)).contains("change-point", "precursor");
        assertThat(t.componentOfFindingType("pre-event-signal")).isEqualTo("event-study");
    }
}
