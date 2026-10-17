package ai.foresignal.manifest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The replay comparison (prompt 116, MAN-1): equal outputs are {@code identical} (numbers within a relative
 * 1e-9), anything else {@code differs} with a summary naming the differing paths.
 */
class ManifestReplayCheckTest {

    @Test
    @DisplayName("equal outputs are identical, numeric noise below the tolerance included")
    void identical() {
        Map<String, Object> stored = Map.of("observed", 42, "expected", 11.000000000001, "classification", "activity-up",
                "items", List.of(1, 2));
        Map<String, Object> replayed = Map.of("observed", 42L, "expected", 11.0, "classification", "activity-up",
                "items", List.of(1, 2));
        assertThat(ManifestReplayCheck.compare(stored, replayed)).isEqualTo(ReplayOutcome.identical());
    }

    @Test
    @DisplayName("a changed value, a missing member or a different list length differs, with the paths named")
    void differs() {
        ReplayOutcome outcome = ManifestReplayCheck.compare(
                Map.of("classification", "activity-up", "pValue", 1e-6, "items", List.of(1, 2)),
                Map.of("classification", "normal", "items", List.of(1)));
        assertThat(outcome.outcome()).isEqualTo(ReplayOutcome.DIFFERS);
        assertThat(outcome.diffSummary()).contains("$.classification: activity-up → normal").contains("$.pValue")
                .contains("$.items: 2 items → 1 items");
    }

    @Test
    @DisplayName("the summary lists at most ten paths and counts the rest")
    void boundedSummary() {
        java.util.Map<String, Object> a = new java.util.TreeMap<>();
        java.util.Map<String, Object> b = new java.util.TreeMap<>();
        for (int i = 0; i < 15; i++) {
            a.put("k" + (100 + i), i);
            b.put("k" + (100 + i), i + 1);
        }
        assertThat(ManifestReplayCheck.compare(a, b).diffSummary()).endsWith("and 5 more");
    }

    @Test
    @DisplayName("not-replayable outcomes carry their reason; LLM manifests the fixed message")
    void notReplayable() {
        ReplayOutcome llm = ReplayOutcome.notReplayable(ReplayOutcome.LLM_OUTPUT, ReplayOutcome.LLM_MESSAGE);
        assertThat(llm.outcome()).isEqualTo(ReplayOutcome.NOT_REPLAYABLE);
        assertThat(llm.message()).isEqualTo("inputs recorded; model output not reproducible");
    }
}
