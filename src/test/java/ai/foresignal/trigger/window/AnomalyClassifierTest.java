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

    @Test
    @DisplayName("a count below the slot mean with a 30% coverage fall is coverage-drop even when the gate failed")
    void coverageDropBeforeInsufficientCoverage() {
        AnomalyClassifier.Result result =
                AnomalyClassifier.classify(5, 5.0, 10.0, 0.5, 1.0, 0.5, 1e-3, 3, "both", false, false);
        assertThat(result.classification()).isEqualTo("coverage-drop");
        assertThat(result.fire()).isFalse();
        // Without a coverage fall, a failed gate is insufficient-coverage.
        assertThat(AnomalyClassifier.classify(5, 10.0, 10.0, 1.0, 1.0, 0.01, 1e-3, 3, "both", false, false)
                .classification()).isEqualTo("insufficient-coverage");
    }

    @Test
    @DisplayName("a 15-minute hour-of-week slot gets four samples a week; hour-of-day 7 x 4")
    void weeksFollowTheBucket() {
        List<Integer> sixteen = java.util.Collections.nCopies(16, 5);
        List<Double> coverage = java.util.Collections.nCopies(16, 1.0);
        assertThat(Baseline.of(sixteen, coverage, "hour-of-week", Duration.ofMinutes(15)).weeks()).isEqualTo(4.0);
        assertThat(Baseline.of(sixteen, coverage, "hour-of-week", Duration.ofHours(1)).weeks()).isEqualTo(16.0);
        assertThat(Baseline.of(java.util.Collections.nCopies(56, 5), java.util.Collections.nCopies(56, 1.0),
                "hour-of-day", Duration.ofMinutes(15)).weeks()).isEqualTo(2.0);
    }

    @Test
    @DisplayName("hour-of-day may start after 14 days, low-confidence until minWeeks; hour-of-week waits for minWeeks")
    void hourOfDayStartsAfterFourteenDays() {
        WindowSpec hourOfDay = new WindowSpec(Duration.ofHours(1), Duration.ofMinutes(5), "entity", null, 500, null,
                null, Duration.ofHours(1), "hour-of-day", "up", "medium", 3, 8, 0.8);
        Baseline fourteenDays = Baseline.of(java.util.Collections.nCopies(14, 10), java.util.Collections.nCopies(14, 1.0),
                "hour-of-day", Duration.ofHours(1));
        AnomalyDetector.Result result = AnomalyDetector.evaluate(40, fourteenDays, 1.0, true, hourOfDay);
        assertThat(result.classification()).isEqualTo("activity-up");
        assertThat(AnomalyDetector.lowConfidence(fourteenDays, hourOfDay)).isTrue();

        WindowSpec hourOfWeek = new WindowSpec(Duration.ofHours(1), Duration.ofMinutes(5), "entity", null, 500, null,
                null, Duration.ofHours(1), "hour-of-week", "up", "medium", 3, 8, 0.8);
        Baseline sevenWeeks = Baseline.of(java.util.Collections.nCopies(7, 10), java.util.Collections.nCopies(7, 1.0),
                "hour-of-week", Duration.ofHours(1));
        assertThat(AnomalyDetector.evaluate(40, sevenWeeks, 1.0, true, hourOfWeek).classification())
                .isEqualTo("learning");
        assertThat(AnomalyDetector.lowConfidence(sevenWeeks, hourOfWeek)).isFalse();
    }
}
