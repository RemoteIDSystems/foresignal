package ai.foresignal.social.correlation;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.backtest.JobMonitor;
import ai.foresignal.benchmark.BenchmarkResults.ScenarioMetrics;
import ai.foresignal.benchmark.BenchmarkResults.SuiteParams;
import ai.foresignal.benchmark.BenchmarkThresholds;
import ai.foresignal.benchmark.component.SocialPhysicalBenchmark;
import ai.foresignal.benchmark.scenario.ScenarioType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The 123 social scenarios of prompt 137 ({@link SocialPhysicalBenchmark}): a planted narrative with no physical event
 * is unobserved under adequate coverage and insufficient-coverage under thin coverage; a planted event with organic
 * discussion is corroborated with a positive lead and no attention gap; silence draws an attention gap; a reused
 * image is contradicted; a cue loop never self-corroborates.
 */
class SocialBenchmarkScenariosTest {

    static final SocialPhysicalBenchmark BENCH = new SocialPhysicalBenchmark();
    static final SuiteParams PARAMS = new SuiteParams(BenchmarkThresholds.DEFAULT_SEED, 10, 20);

    static ScenarioMetrics run(ScenarioType type) {
        return BENCH.run(type, PARAMS, JobMonitor.NONE);
    }

    @Test
    @DisplayName("the component covers the six social scenarios")
    void scenarios() {
        assertThat(BENCH.scenarios()).containsExactlyElementsOf(ScenarioType.SOCIAL);
        assertThat(BENCH.name()).isEqualTo("social-physical");
    }

    @Test
    @DisplayName("planted narrative: unobserved with coverage, insufficient-coverage without")
    void plantedNarrative() {
        assertThat(run(ScenarioType.SOCIAL_PLANTED_COVERED).metrics()).containsEntry("correctStatusShare", 1.0);
        assertThat(run(ScenarioType.SOCIAL_PLANTED_THIN).metrics()).containsEntry("correctStatusShare", 1.0);
    }

    @Test
    @DisplayName("planted event: corroborated with discussion (positive lead, no gap), a gap with silence")
    void plantedEvent() {
        ScenarioMetrics discussed = run(ScenarioType.SOCIAL_EVENT_DISCUSSED);
        assertThat(discussed.metrics()).containsEntry("correctStatusShare", 1.0);
        assertThat(discussed.metrics().get("falseGapShare")).isLessThanOrEqualTo(0.05);
        assertThat(discussed.metrics().get("meanLeadSeconds")).isPositive();
        assertThat(run(ScenarioType.SOCIAL_EVENT_SILENT).metrics().get("gapRecall")).isGreaterThanOrEqualTo(0.95);
    }

    @Test
    @DisplayName("reused image: contradicted; cue loop: every trigger-only match is caveated")
    void reuseAndCueLoop() {
        assertThat(run(ScenarioType.SOCIAL_REUSED_IMAGE).metrics()).containsEntry("correctStatusShare", 1.0);
        assertThat(run(ScenarioType.SOCIAL_CUE_LOOP).metrics()).containsEntry("uncaveatedTriggerMatches", 0.0);
    }

    @Test
    @DisplayName("the same seed gives the same metrics")
    void deterministic() {
        for (ScenarioType t : ScenarioType.SOCIAL) {
            assertThat(run(t).metrics()).isEqualTo(run(t).metrics());
        }
    }
}
