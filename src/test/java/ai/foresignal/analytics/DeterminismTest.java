package ai.foresignal.analytics;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.analytics.eventstudy.EventStudyEngine;
import ai.foresignal.analytics.math.PlaceboSampler;
import ai.foresignal.analytics.series.SeriesData;
import ai.foresignal.analytics.series.SeriesFrequency;
import ai.foresignal.backtest.JobMonitor;
import ai.foresignal.backtest.PrecursorTester;
import ai.foresignal.benchmark.BenchmarkResults.ScenarioMetrics;
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
import ai.foresignal.benchmark.scenario.ScenarioGenerator;
import ai.foresignal.benchmark.scenario.ScenarioType;
import ai.foresignal.evidence.CanonicalRules;
import ai.foresignal.precursor.LeadLagCcf;
import ai.foresignal.precursor.PrecursorRules;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/**
 * Determinism of the intelligence components of prompts 100–123 (checkpoint 124, focus area 2; invariant INT-1):
 * every detector, scorer, miner, base-rate and analog component and forecaster, run twice on identical inputs and
 * seeds, gives identical output, including its permutation, placebo, surrogate and bootstrap statistics.
 *
 * <p>The components run in the pure mode the 123 benchmark harness uses (the same code the live paths call), over
 * every scenario they are benchmarked on; the engines with internal randomness are also compared field by field.
 * The windowed-rule detectors of prompt 100 have their own {@code ai.foresignal.trigger.window.DeterminismTest}.
 */
class DeterminismTest {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final SuiteParams ONE = new SuiteParams(1_240_001L, 1, 20);

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
    @DisplayName("every benchmarked component gives identical metrics twice on the same scenario and seed")
    void componentsAreDeterministic() {
        int compared = 0;
        for (ComponentBenchmark component : components()) {
            for (ScenarioType scenario : component.scenarios()) {
                ScenarioMetrics first = component.run(scenario, ONE, JobMonitor.NONE);
                ScenarioMetrics second = component.run(scenario, ONE, JobMonitor.NONE);
                assertThat(second.metrics()).as(component.name() + " on " + scenario.wire())
                        .isEqualTo(first.metrics());
                assertThat(second.truth()).as(component.name() + " truth on " + scenario.wire())
                        .isEqualTo(first.truth());
                compared++;
            }
        }
        assertThat(compared).as("component × scenario pairs compared").isGreaterThan(20);
    }

    @Test
    @DisplayName("the event-study engine repeats its placebo dates, placebo distribution and test statistics")
    void eventStudyPlaceboIsDeterministic() {
        ScenarioGenerator.MarketScenario sc = ScenarioGenerator.preEventMove(ONE.seed(), 0);
        Instant event = sc.trueEvents().get(2);
        SeriesData data = sc.series().read(0, sc.series().indexOf(event) + 1, event);
        SeriesData.Grid grid = data.grid();
        EventStudyEngine.Input input = new EventStudyEngine.Input(grid.starts(), grid.values(), SeriesFrequency.DAY,
                EventStudyEngine.Transform.of(false, null, "price"), EventStudyEngine.Windows.DAILY,
                List.of(new EventStudyEngine.Event(event, "e")), sc.trueEvents().subList(0, 2), 42L,
                PlaceboSampler.DEFAULT_COUNT, false);
        EventStudyEngine.Result first = EventStudyEngine.run(input);
        EventStudyEngine.Result second = EventStudyEngine.run(input);
        assertThat(first.placeboDates()).isPositive();
        assertThat(second).usingRecursiveComparison().withComparatorForType(Double::compare, Double.class).isEqualTo(first);
        EventStudyEngine.Result otherSeed = EventStudyEngine.run(new EventStudyEngine.Input(grid.starts(),
                grid.values(), SeriesFrequency.DAY, EventStudyEngine.Transform.of(false, null, "price"),
                EventStudyEngine.Windows.DAILY, List.of(new EventStudyEngine.Event(event, "e")),
                sc.trueEvents().subList(0, 2), 43L, 50, false));
        assertThat(otherSeed.placeboDates()).as("a different seed and count draws its own placebo sample")
                .isEqualTo(50);
    }

    @Test
    @DisplayName("the precursor tester repeats its circular-shift base rate and permutation p-value")
    void precursorShiftsAreDeterministic() {
        ScenarioGenerator.EventStreamScenario sc = ScenarioGenerator.eventStreams(ScenarioType.TRUE_PRECURSOR,
                ONE.seed(), 0);
        PrecursorTester.Request request = new PrecursorTester.Request(sc.antecedents(), sc.consequents(),
                Duration.ZERO, Duration.ofDays(3), PrecursorTester.Spatial.none(), Duration.ofDays(3),
                sc.rangeStart(), sc.rangeEnd(), 0.0, "determinism");
        PrecursorTester.Result first = PrecursorTester.test(request, PrecursorRules.MINING_SHIFTS);
        PrecursorTester.Result second = PrecursorTester.test(request, PrecursorRules.MINING_SHIFTS);
        assertThat(second).usingRecursiveComparison().withComparatorForType(Double::compare, Double.class).isEqualTo(first);
    }

    @Test
    @DisplayName("the lead–lag test repeats its block-bootstrap surrogates; the placebo sampler and bootstrap means too")
    void surrogatesAndBootstrapAreDeterministic() {
        ScenarioGenerator.SeriesPairScenario pair = ScenarioGenerator.seriesPair(ScenarioType.TRUE_PRECURSOR,
                ONE.seed(), 0);
        LeadLagCcf.Result first = LeadLagCcf.test(pair.antecedent(), pair.consequent(), 14, 7L);
        LeadLagCcf.Result second = LeadLagCcf.test(pair.antecedent(), pair.consequent(), 14, 7L);
        assertThat(second).usingRecursiveComparison().withComparatorForType(Double::compare, Double.class).isEqualTo(first);

        int[] eligible = new int[1000];
        for (int i = 0; i < eligible.length; i++) {
            eligible[i] = i * 3;
        }
        assertThat(PlaceboSampler.sample(eligible, 500, 99L)).containsExactly(PlaceboSampler.sample(eligible, 500, 99L));
        double[] values = {0.1, -0.4, 2.3, 0.0, 1.1, -1.7, 0.6};
        assertThat(PlaceboSampler.resampledMeans(values, 3, 200, 5L))
                .containsExactly(PlaceboSampler.resampledMeans(values, 3, 200, 5L));
    }
}
