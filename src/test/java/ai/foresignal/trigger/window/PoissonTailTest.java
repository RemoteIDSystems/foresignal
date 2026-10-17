package ai.foresignal.trigger.window;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Poisson tails against reference values (prompt 100). */
class PoissonTailTest {

    @Test
    @DisplayName("P(X ≥ k) and P(X ≤ k) match the closed form for lambda = 1")
    void lambdaOne() {
        // Closed form: P(X ≤ 4) = e^-1 (1 + 1 + 1/2 + 1/6 + 1/24) = 65 / (24e) = 0.99634015317.
        assertThat(PoissonTail.atMost(4, 1.0)).isCloseTo(0.9963401532, org.assertj.core.data.Offset.offset(1e-9));
        assertThat(PoissonTail.atLeast(5, 1.0)).isCloseTo(0.0036598468, org.assertj.core.data.Offset.offset(1e-9));
        assertThat(PoissonTail.atLeast(1, 1.0)).isCloseTo(0.6321205588, org.assertj.core.data.Offset.offset(1e-9));
        assertThat(PoissonTail.atMost(0, 1.0)).isCloseTo(0.3678794412, org.assertj.core.data.Offset.offset(1e-9));
    }

    @Test
    @DisplayName("a tiny upper tail under a larger lambda")
    void tails() {
        assertThat(PoissonTail.atLeast(42, 11.0)).isLessThan(1e-9);
        assertThat(PoissonTail.atLeast(0, 11.0)).isEqualTo(1);
        assertThat(PoissonTail.atMost(11, 11.0)).isCloseTo(0.579, org.assertj.core.data.Offset.offset(0.01));
    }

    @Test
    @DisplayName("the Anscombe z is zero at the mean and positive above it")
    void anscombe() {
        assertThat(PoissonTail.anscombe(11, 11)).isEqualTo(0);
        assertThat(PoissonTail.anscombe(20, 11)).isPositive();
        assertThat(PoissonTail.anscombe(3, 11)).isNegative();
    }
}
