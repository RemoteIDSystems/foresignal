package ai.foresignal.trigger.window;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Negative-binomial tails and the overdispersion switch (prompt 100). */
class NegBinTailTest {

    @Test
    @DisplayName("at k = 0 the tail is p^r")
    void zero() {
        assertThat(NegBinTail.atMost(0, 5, 0.5)).isCloseTo(Math.pow(0.5, 5), org.assertj.core.data.Offset.offset(1e-12));
        assertThat(NegBinTail.atLeast(0, 5, 0.5)).isEqualTo(1);
    }

    @Test
    @DisplayName("the method of moments gives r = mu^2/(v - mu)")
    void moments() {
        double[] m = NegBinTail.moments(10, 25);
        assertThat(m[0]).isCloseTo(100.0 / 15.0, org.assertj.core.data.Offset.offset(1e-9));
        assertThat(m[1]).isCloseTo((100.0 / 15.0) / (100.0 / 15.0 + 10), org.assertj.core.data.Offset.offset(1e-9));
        assertThat(NegBinTail.moments(10, 10)).isNull();
    }

    @Test
    @DisplayName("the tails are monotone and bounded in [0, 1]")
    void monotone() {
        double[] m = NegBinTail.moments(10, 25);
        assertThat(NegBinTail.atMost(5, m[0], m[1])).isLessThan(NegBinTail.atMost(15, m[0], m[1]));
        assertThat(NegBinTail.atMost(15, m[0], m[1])).isBetween(0.0, 1.0);
    }
}
