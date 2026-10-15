package ai.foresignal.source.adapters.derived;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@code derived-deception} implied speed (prompt 099). */
class DeceptionTest {

    @Test
    @DisplayName("implied speed is distance over time in knots")
    void implied() {
        double knots = DeceptionGenerator.impliedKnots(32.0, -95.0, 32.1, -95.0, 60);
        assertThat(knots).isGreaterThan(300);
        assertThat(DeceptionGenerator.impliedKnots(32.0, -95.0, 32.0, -95.0, 60)).isZero();
        assertThat(DeceptionGenerator.impliedKnots(32.0, -95.0, 32.1, -95.0, 0)).isZero();
    }
}
