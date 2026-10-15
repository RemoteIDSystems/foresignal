package ai.foresignal.trigger.window;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Anomaly classification and baselines (prompt 100). */
class AnomalyClassifierTest {

    @Test
    @DisplayName("a significant rise is activity-up; a significant fall is activity-down")
    void activity() {
        AnomalyClassifier.Result up = AnomalyClassifier.classify(42, 11, 1.0, 1.0, 2e-6, 1e-3, 3, "up", false, true);
        assertThat(up.classification()).isEqualTo("activity-up");
        assertThat(up.fire()).isTrue();
        AnomalyClassifier.Result down =
                AnomalyClassifier.classify(1, 11, 1.0, 1.0, 1e-6, 1e-3, 3, "down", false, true);
        assertThat(down.classification()).isEqualTo("activity-down");
        assertThat(down.fire()).isTrue();
    }

    @Test
    @DisplayName("a count drop with a coverage drop is coverage-drop, and never alerts")
    void coverageDrop() {
        AnomalyClassifier.Result result =
                AnomalyClassifier.classify(2, 11, 0.5, 1.0, 1e-6, 1e-3, 3, "both", false, true);
        assertThat(result.classification()).isEqualTo("coverage-drop");
        assertThat(result.fire()).isFalse();
    }

    @Test
    @DisplayName("learning and insufficient coverage never alert")
    void unknown() {
        assertThat(AnomalyClassifier.classify(50, 11, 1.0, 1.0, 1e-9, 1e-3, 3, "up", true, true).classification())
                .isEqualTo("learning");
        assertThat(AnomalyClassifier.classify(50, 11, 0.4, 1.0, 1e-9, 1e-3, 3, "up", false, false).classification())
                .isEqualTo("insufficient-coverage");
    }

    @Test
    @DisplayName("minAbsoluteDelta gates a statistically significant but tiny change")
    void minDelta() {
        AnomalyClassifier.Result result =
                AnomalyClassifier.classify(12, 10.6, 1.0, 1.0, 1e-6, 1e-3, 3, "up", false, true);
        assertThat(result.classification()).isEqualTo("normal");
        assertThat(result.fire()).isFalse();
    }

    @Test
    @DisplayName("a baseline winsorizes at p99 and switches to the negative binomial when overdispersed")
    void baseline() {
        List<Integer> counts = List.of(10, 10, 11, 10, 12, 9, 10, 11, 10, 500);
        Baseline poisson = Baseline.of(counts, List.of(1.0), "hour-of-week", Duration.ofHours(1));
        assertThat(poisson.mean()).isLessThan(20);
        Baseline overdispersed = Baseline.of(List.of(1, 1, 1, 1, 60, 1, 1, 1), List.of(1.0), "hour-of-week",
                Duration.ofHours(1));
        assertThat(overdispersed.model()).isEqualTo("nb");
    }
}
