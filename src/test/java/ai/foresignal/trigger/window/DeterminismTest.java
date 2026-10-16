package ai.foresignal.trigger.window;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Invariant INT-1 for the prompt 100 analytic components: the baseline, the detector, the classifier,
 * the tails and the slot and bucket arithmetic are pure functions, so identical inputs give identical
 * outputs, run after run and whatever order the history arrives in. (The stored-input side of INT-1,
 * the membership read in a fixed order and bounded by the decision time, is asserted by
 * {@code LookAheadTest} against the database.)
 */
class DeterminismTest {

    private static final WindowSpec SPEC = new WindowSpec(Duration.ofHours(1), Duration.ofMinutes(5), "entity", null,
            500, null, null, Duration.ofHours(1), "hour-of-week", "both", "medium", 3, 8, 0.8);

    /** Twelve weeks of a fixed-seed Poisson-like history, with one outlier. */
    private static List<Integer> history() {
        Random random = new Random(42);
        List<Integer> counts = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            counts.add(8 + random.nextInt(5));
        }
        counts.set(5, 40);
        return counts;
    }

    private static List<Double> coverages(int n) {
        return new ArrayList<>(Collections.nCopies(n, 1.0));
    }

    @Test
    @DisplayName("the same history gives the same baseline every time, in any arrival order")
    void baselineIsDeterministicAndOrderInsensitive() {
        List<Integer> counts = history();
        Baseline first = Baseline.of(counts, coverages(counts.size()), "hour-of-week", Duration.ofHours(1));
        Baseline again = Baseline.of(new ArrayList<>(counts), coverages(counts.size()), "hour-of-week",
                Duration.ofHours(1));
        assertThat(again).isEqualTo(first);

        List<Integer> shuffled = new ArrayList<>(counts);
        Collections.shuffle(shuffled, new Random(7));
        Baseline reordered = Baseline.of(shuffled, coverages(counts.size()), "hour-of-week", Duration.ofHours(1));
        assertThat(reordered.mean()).isEqualTo(first.mean());
        assertThat(reordered.variance()).isCloseTo(first.variance(), org.assertj.core.data.Offset.offset(1e-9));
        assertThat(reordered.model()).isEqualTo(first.model());
        assertThat(reordered.weeks()).isEqualTo(first.weeks());
    }

    @Test
    @DisplayName("the detector returns identical results for identical inputs")
    void detectorIsDeterministic() {
        List<Integer> counts = history();
        Baseline baseline = Baseline.of(counts, coverages(counts.size()), "hour-of-week", Duration.ofHours(1));
        for (int observed : new int[] {0, 3, 10, 25, 60}) {
            for (double ratio : new double[] {1.0, 0.6, 0.3}) {
                AnomalyDetector.Result a = AnomalyDetector.evaluate(observed, baseline, ratio, true, SPEC);
                AnomalyDetector.Result b = AnomalyDetector.evaluate(observed, baseline, ratio, true, SPEC);
                assertThat(b).as("observed %d ratio %s", observed, ratio).isEqualTo(a);
            }
        }
    }

    @Test
    @DisplayName("the tails and the classifier are pure")
    void tailsAndClassifierArePure() {
        assertThat(PoissonTail.atLeast(42, 11.0)).isEqualTo(PoissonTail.atLeast(42, 11.0));
        assertThat(PoissonTail.atMost(2, 11.0)).isEqualTo(PoissonTail.atMost(2, 11.0));
        assertThat(NegBinTail.atLeast(30, 4.2, 0.3)).isEqualTo(NegBinTail.atLeast(30, 4.2, 0.3));
        AnomalyClassifier.Result a = AnomalyClassifier.classify(42, 11, 11, 1.0, 1.0, 2e-6, 1e-3, 3, "up", false, true);
        AnomalyClassifier.Result b = AnomalyClassifier.classify(42, 11, 11, 1.0, 1.0, 2e-6, 1e-3, 3, "up", false, true);
        assertThat(b).isEqualTo(a);
    }

    @Test
    @DisplayName("slots and bucket floors depend only on the instant (UTC), never on the host zone")
    void slotsAndBucketsArePure() {
        Instant mondayTen = Instant.parse("2026-10-12T10:17:00Z");
        assertThat(WindowEngine.slotOf(mondayTen, "hour-of-week")).isEqualTo(10);
        assertThat(WindowEngine.slotOf(mondayTen, "hour-of-day")).isEqualTo(10);
        assertThat(WindowEngine.slotOf(Instant.parse("2026-10-18T23:00:00Z"), "hour-of-week")).isEqualTo(167);
        assertThat(WindowEngine.floor(mondayTen, Duration.ofHours(1))).isEqualTo(Instant.parse("2026-10-12T10:00:00Z"));
        assertThat(WindowEngine.floor(mondayTen, Duration.ofMinutes(15)))
                .isEqualTo(Instant.parse("2026-10-12T10:15:00Z"));
    }

    @Test
    @DisplayName("the alert text is a pure function of the result: same numbers, same words")
    void messageIsPure() {
        Baseline baseline = Baseline.of(List.of(10, 11, 12, 9, 10, 11, 10, 9), coverages(8), "hour-of-week",
                Duration.ofHours(1));
        AnomalyDetector.Result result = AnomalyDetector.evaluate(42, baseline, 1.0, true, SPEC);
        assertThat(WindowEngine.anomalyMessage(result, baseline)).isEqualTo(WindowEngine.anomalyMessage(result, baseline))
                .startsWith("Unusual activity: 42 observed vs ~10 expected (p≈").endsWith("under a Poisson model)");
        assertThat(WindowEngine.scientific(2.04e-6)).isEqualTo("2e-6");
        assertThat(WindowEngine.scientific(0.00096)).isEqualTo("1e-3");
    }
}
