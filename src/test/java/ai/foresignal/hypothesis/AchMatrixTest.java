package ai.foresignal.hypothesis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.within;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The ACH arithmetic (prompt 125 §3): the LR → rating mapping, the weighted inconsistency score and the non-diagnostic
 * flag.
 */
class AchMatrixTest {

    @Test
    @DisplayName("LR → rating: ≥ 4 CC, ≥ 1.5 C, ≤ 0.25 II, ≤ 0.67 I, else N; NA without an LR")
    void ratings() {
        assertThat(LikelihoodRatios.rating(5.0)).isEqualTo("CC");
        assertThat(LikelihoodRatios.rating(4.0)).isEqualTo("CC");
        assertThat(LikelihoodRatios.rating(3.99)).isEqualTo("C");
        assertThat(LikelihoodRatios.rating(1.5)).isEqualTo("C");
        assertThat(LikelihoodRatios.rating(1.49)).isEqualTo("N");
        assertThat(LikelihoodRatios.rating(1.0)).isEqualTo("N");
        assertThat(LikelihoodRatios.rating(0.68)).isEqualTo("N");
        assertThat(LikelihoodRatios.rating(0.67)).isEqualTo("I");
        assertThat(LikelihoodRatios.rating(0.4)).isEqualTo("I");
        assertThat(LikelihoodRatios.rating(0.25)).isEqualTo("II");
        assertThat(LikelihoodRatios.rating(0.2)).isEqualTo("II");
        assertThat(LikelihoodRatios.rating(null)).isEqualTo("NA");
        assertThat(AchMatrix.ratingsFromLr(List.of("A", "B", "C"), Map.of("A", 5.0, "B", 0.4)))
                .containsExactly(entry("A", "CC"), entry("B", "I"), entry("C", "NA"));
    }

    @Test
    @DisplayName("weights are credibility × relevance with L 0.5, M 1, H 2")
    void weights() {
        assertThat(AchMatrix.weight("L", "L")).isEqualTo(0.25);
        assertThat(AchMatrix.weight("H", "M")).isEqualTo(2.0);
        assertThat(AchMatrix.weight("H", "H")).isEqualTo(4.0);
        assertThat(AchMatrix.inconsistency("II")).isEqualTo(2);
        assertThat(AchMatrix.inconsistency("I")).isEqualTo(1);
        assertThat(AchMatrix.inconsistency("CC")).isZero();
    }

    @Test
    @DisplayName("the inconsistency score is −Σ w·inc, and the least-inconsistent hypothesis has the highest score")
    void weightedInconsistency() {
        AchMatrix.Row strong = new AchMatrix.Row("r1", Map.of("A", "I", "B", "II", "C", "C"), "H", "H");
        AchMatrix.Row weak = new AchMatrix.Row("r2", Map.of("A", "N", "B", "I", "C", "II"), "L", "M");
        AchMatrix.Result r = AchMatrix.compute(List.of("A", "B", "C"), List.of(strong, weak));
        // r1 weighs 4: A −4, B −8; r2 weighs 0.5: B −0.5, C −1.
        assertThat(r.scores().get("A")).isCloseTo(-4.0, within(1e-12));
        assertThat(r.scores().get("B")).isCloseTo(-8.5, within(1e-12));
        assertThat(r.scores().get("C")).isCloseTo(-1.0, within(1e-12));
        assertThat(r.leastInconsistent()).isEqualTo("C");
        assertThat(r.weights()).containsEntry("r1", 4.0).containsEntry("r2", 0.5);
        assertThat(r.nonDiagnostic()).isEmpty();
    }

    @Test
    @DisplayName("a row rated identically for every hypothesis is non-diagnostic")
    void nonDiagnostic() {
        AchMatrix.Row same = new AchMatrix.Row("same", Map.of("A", "C", "B", "C"), "M", "M");
        AchMatrix.Row unrated = new AchMatrix.Row("unrated", Map.of(), "M", "M");
        AchMatrix.Row diagnostic = new AchMatrix.Row("diag", Map.of("A", "C", "B", "I"), "M", "M");
        AchMatrix.Result r = AchMatrix.compute(List.of("A", "B"), List.of(same, unrated, diagnostic));
        assertThat(r.nonDiagnostic()).containsExactly("same", "unrated");
        assertThat(r.scores().get("A")).isZero();
        assertThat(r.scores().get("B")).isCloseTo(-1.0, within(1e-12));
        assertThat(r.leastInconsistent()).isEqualTo("A");
        assertThat(AchMatrix.compute(List.of(), List.of(same)).leastInconsistent()).isNull();
    }
}
