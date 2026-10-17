package ai.foresignal.benchmark;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.analytics.changepoint.ChangePointDetector;
import ai.foresignal.analytics.finding.FindingDraft;
import ai.foresignal.analytics.finding.FindingService;
import ai.foresignal.backtest.JobMonitor;
import ai.foresignal.benchmark.BenchmarkResults.ComponentResult;
import ai.foresignal.benchmark.BenchmarkResults.ScenarioResult;
import ai.foresignal.benchmark.BenchmarkResults.SuiteParams;
import ai.foresignal.benchmark.component.ChangePointBenchmark;
import ai.foresignal.benchmark.component.ComponentBenchmark;
import ai.foresignal.forecast.ForecastEntity;
import ai.foresignal.forecast.ForecastInputs;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecastService;
import ai.foresignal.forecast.ForecasterEntity;
import ai.foresignal.forecast.ForecasterRegistry;
import ai.foresignal.security.Ulids;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.json.JsonMapper;

/**
 * A synthetic run over the real 116/118–122 components (prompt 123): results for every scenario of every component;
 * a deliberately broken detector double yields {@code fail}; and the gate (BEN-2) degrades 128's proposer
 * auto-execution (a stubbed consumer), the LLM forecasters' weight in the real 122 ensemble and adds
 * {@code BENCHMARK_FAIL} to new findings while a dependency fails, and lifts all of it after a passing run.
 */
@IntegrationTest
class BenchmarkRunIT {

    static final Instant NOW = Instant.parse("2026-10-17T12:00:00Z");

    @Autowired BenchmarkService service;
    @Autowired BenchmarkThresholds thresholds;
    @Autowired BenchmarkGate gate;
    @Autowired BenchmarkRunRepository runs;
    @Autowired FindingService findings;
    @Autowired ForecastService ledger;
    @Autowired ForecasterRegistry registry;
    @Autowired MutableClock clock;
    @Autowired JsonMapper json;

    @BeforeEach
    void setUp() {
        clock.setInstant(NOW);
        registry.registerBuiltIns();
        gate.invalidate();
    }

    @Test
    @DisplayName("a synthetic run over the real components produces results for every scenario of every component")
    void everyScenario() {
        List<ComponentResult> results = service.execute("synthetic", new LinkedHashSet<>(BenchmarkInputs.COMPONENTS),
                new SuiteParams(BenchmarkThresholds.DEFAULT_SEED, 1, 20), JobMonitor.NONE);
        assertThat(results).extracting(ComponentResult::component)
                .containsExactlyElementsOf(BenchmarkInputs.COMPONENTS);
        for (ComponentBenchmark c : service.components()) {
            ComponentResult r = results.stream().filter(x -> x.component().equals(c.name())).findFirst()
                    .orElseThrow();
            assertThat(r.scenarios()).extracting(s -> s.metrics().scenario())
                    .containsExactlyElementsOf(c.scenarios().stream().map(s -> s.wire()).toList());
            assertThat(r.componentVersion()).isEqualTo(c.version());
            for (ScenarioResult s : r.scenarios()) {
                assertThat(s.metrics().metrics()).as(c.name() + " " + s.metrics().scenario()).isNotEmpty();
                assertThat(s.checks()).as(c.name() + " " + s.metrics().scenario()).isNotEmpty();
            }
            assertThat(r.toMap()).containsKeys("component", "componentVersion", "status", "metrics", "thresholds",
                    "scenarios");
        }
        List<String> covered = new ArrayList<>();
        for (ComponentResult r : results) {
            for (ScenarioResult s : r.scenarios()) {
                covered.add(s.metrics().scenario());
            }
        }
        for (var scenario : ai.foresignal.benchmark.scenario.ScenarioType.SYNTHETIC) {
            assertThat(covered).as("scenario " + scenario.wire()).contains(scenario.wire());
        }
    }

    @Test
    @DisplayName("a deliberately broken detector (a test double that always fires) yields fail")
    void brokenDetector() {
        ChangePointBenchmark broken = new ChangePointBenchmark((starts, x, gap, counts, p, t) -> {
            int n = x.length;
            return new ChangePointDetector.Result(ChangePointDetector.State.CONFIRMED, -1, n - 3, n - 1,
                    starts[n - 3], starts[n - 1], 20, 10, -0.5, 0.99, 5, "POISSON", 0, n);
        });
        ComponentResult result = SyntheticSuite.run(List.of(broken), new SuiteParams(BenchmarkThresholds.DEFAULT_SEED,
                1, 20), thresholds, JobMonitor.NONE, 0, 1).get(0);
        assertThat(result.status()).isEqualTo(Verdict.FAIL);
        ScenarioResult nullData = result.scenarios().stream().filter(s -> s.metrics().scenario()
                .equals("null-seasonal")).findFirst().orElseThrow();
        assertThat(nullData.status()).isEqualTo(Verdict.FAIL);
        assertThat(nullData.metrics().metrics().get("falseAlarmRatio")).isGreaterThan(3.0);
        // Its downward "changes" during the outage are still classified unknown: classification is not the double's.
        ScenarioResult outage = result.scenarios().stream().filter(s -> s.metrics().scenario()
                .equals("coverage-outage")).findFirst().orElseThrow();
        assertThat(outage.metrics().metrics().get("unknownDetections")).isPositive();
    }

    /** A stand-in for prompt 128's proposer: it executes a proposal only when the gate allows it. */
    record StubProposer(BenchmarkGate gate) {

        String decide(List<String> dependsOn) {
            return gate.proposerAutoExecutionAllowed(dependsOn) ? "executed" : "pending";
        }
    }

    BenchmarkRunEntity storeRun(String status, Instant finishedAt) {
        BenchmarkRunEntity run = new BenchmarkRunEntity(Ulids.next(finishedAt), "synthetic", finishedAt);
        run.setSuiteVersion("bench-scenarios 1.0.0");
        run.setScorerVersion("BenchmarkScorer 1.0.0");
        run.setTrigger("manual");
        List<Map<String, Object>> results = new ArrayList<>();
        for (ComponentBenchmark c : service.components()) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("component", c.name());
            r.put("componentVersion", c.version());
            r.put("status", status);
            results.add(r);
        }
        run.setResults(results);
        run.setStatus(status);
        run.setStartedAt(finishedAt);
        run.setFinishedAt(finishedAt);
        BenchmarkRunEntity saved = runs.save(run);
        gate.invalidate();
        return saved;
    }

    FindingDraft changePointDraft(String scope) {
        return new FindingDraft("change-point", "Change point: bench " + scope, "notable", null, List.of(),
                NOW.minus(Duration.ofDays(3)), NOW, NOW, List.of("series:bench/" + scope), List.of(), 0,
                Map.of("effect", 0.5), Map.of(), List.of(), ChangePointDetector.CUSUM, ChangePointDetector.VERSION,
                Map.of(), "A benchmark test finding.", false, null, List.of(), List.of(), "series:bench/" + scope);
    }

    @Test
    @DisplayName("gating: fail degrades proposer auto-execution, LLM ensemble weight and adds BENCHMARK_FAIL;"
            + " a passing run lifts it")
    void gating() throws Exception {
        StubProposer proposer = new StubProposer(gate);
        // Never benchmarked: unknown is not pass or warn, so autonomy waits for the first run.
        assertThat(gate.status("change-point")).isEqualTo("unknown");
        assertThat(proposer.decide(List.of("change-point"))).isEqualTo("pending");
        assertThat(gate.findingCaveat("change-point")).isNull();

        storeRun("fail", NOW.minus(Duration.ofHours(1)));
        assertThat(gate.status("change-point")).isEqualTo("fail");
        assertThat(proposer.decide(List.of("change-point"))).isEqualTo("pending");
        assertThat(proposer.decide(null)).isEqualTo("pending");
        assertThat(gate.llmEnsembleWeightAllowed()).isFalse();
        FindingService.Outcome failed = findings.record(changePointDraft("a"));
        assertThat(failed.finding().getCaveats()).anyMatch(c -> BenchmarkGate.BENCHMARK_FAIL.equals(c.get("code")));
        Map<String, Object> status = gate.view();
        assertThat(status.get("overall")).isEqualTo("fail");
        assertThat(String.valueOf(status.get("components"))).contains("degraded", BenchmarkGate.BENCHMARK_FAIL);

        ForecasterEntity llm = registry.ensure("llm:bench", "llm", "Benchmark LLM", "1.0.0", Map.of());
        ForecasterEntity operator = registry.ensure("operator:bench", "operator", "Bench operator", "1.0.0",
                Map.of());
        ForecastQuestionEntity q = ledger.openQuestion(ForecastInputs.question(json.readTree(question()), NOW),
                "agent", "test").question();
        ledger.record(q, operator, ForecastInputs.ForecastValue.binary(0.3), "operator view", List.of(), List.of(),
                Map.of(), null, false);
        clock.setInstant(NOW.plusSeconds(1));
        ledger.record(q, llm, ForecastInputs.ForecastValue.binary(0.9), "model view", List.of(), List.of(), Map.of(),
                null, false);
        assertThat(memberIds(q)).contains("operator:bench").doesNotContain("llm:bench");

        storeRun("pass", NOW.plusSeconds(2));
        clock.setInstant(NOW.plusSeconds(3));
        assertThat(gate.status("change-point")).isEqualTo("pass");
        assertThat(proposer.decide(List.of("change-point"))).isEqualTo("executed");
        assertThat(gate.llmEnsembleWeightAllowed()).isTrue();
        ledger.record(q, llm, ForecastInputs.ForecastValue.binary(0.8), "model view, updated", List.of(), List.of(),
                Map.of(), null, false);
        assertThat(memberIds(q)).contains("operator:bench", "llm:bench");
        FindingService.Outcome passed = findings.record(changePointDraft("b"));
        assertThat(passed.finding().getCaveats()).noneMatch(c -> BenchmarkGate.BENCHMARK_FAIL.equals(c.get("code")));
        assertThat(gate.view().get("overall")).isEqualTo("pass");
    }

    List<String> memberIds(ForecastQuestionEntity q) {
        ForecastEntity ensemble = ledger.latestByForecaster(q.getId()).get(ForecasterRegistry.ENSEMBLE);
        assertThat(ensemble).as("an ensemble forecast exists").isNotNull();
        List<String> out = new ArrayList<>();
        for (Object m : (List<?>) ensemble.getInputs().get("members")) {
            out.add(String.valueOf(((Map<?, ?>) m).get("forecasterId")));
        }
        return out;
    }

    static String question() {
        return "{\"title\":\"Will the benchmark hold?\",\"type\":\"binary\",\"resolutionCriteria\":\"Yes if it"
                + " holds.\",\"resolver\":{\"type\":\"manual\"},\"closesAt\":\"2026-10-24T00:00:00Z\","
                + "\"resolveBy\":\"2026-10-31T00:00:00Z\",\"category\":\"conflict\"}";
    }
}
