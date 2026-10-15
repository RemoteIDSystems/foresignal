package ai.foresignal.backtest;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The precursor tester (prompt 103). */
class PrecursorTesterTest {

    private static final Instant T0 = Instant.parse("2026-01-01T00:00:00Z");

    /** {@code n} occurrences one hour apart starting at T0, all with entity {@code e}. */
    private static List<PrecursorTester.Occurrence> hourly(int n, Duration offset, String entity) {
        List<PrecursorTester.Occurrence> out = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            out.add(new PrecursorTester.Occurrence(T0.plus(Duration.ofHours(i)).plus(offset), -95.0, 32.0, "US", entity));
        }
        return out;
    }

    private static PrecursorTester.Result run(List<PrecursorTester.Occurrence> a,
            List<PrecursorTester.Occurrence> c) {
        return PrecursorTester.test(new PrecursorTester.Request(a, c, Duration.ZERO, Duration.ofHours(2),
                PrecursorTester.Spatial.sameEntity(), Duration.ZERO, T0, T0.plus(Duration.ofHours(40)), 0.0, "seed"));
    }

    @Test
    @DisplayName("planted precedence (deterministic) is notable")
    void planted() {
        var result = run(hourly(20, Duration.ZERO, "x"), hourly(20, Duration.ofMinutes(30), "x"));
        assertThat(result.verdict()).isEqualTo("notable");
        assertThat(result.hitRate()).isEqualTo(1.0);
        assertThat(result.insufficient()).isFalse();
    }

    @Test
    @DisplayName("independent processes are no-evidence")
    void independent() {
        // Consequents with a different entity and a spatial sameEntity rule -> no hits.
        var result = run(hourly(20, Duration.ZERO, "x"), hourly(20, Duration.ofMinutes(30), "y"));
        assertThat(result.hitRate()).isZero();
        assertThat(result.verdict()).isEqualTo("no-evidence-of-precedence");
    }

    @Test
    @DisplayName("deterministic shifts give the same p-value twice")
    void deterministic() {
        var a = run(hourly(20, Duration.ZERO, "x"), hourly(20, Duration.ofMinutes(30), "x"));
        var b = run(hourly(20, Duration.ZERO, "x"), hourly(20, Duration.ofMinutes(30), "x"));
        assertThat(a.pValue()).isEqualTo(b.pValue());
        assertThat(a.baseRate()).isEqualTo(b.baseRate());
    }

    @Test
    @DisplayName("symmetric co-occurrence flags reverse")
    void symmetric() {
        List<PrecursorTester.Occurrence> both = hourly(20, Duration.ofMinutes(20), "x");
        var result = PrecursorTester.test(new PrecursorTester.Request(both, both, Duration.ZERO, Duration.ofMinutes(30),
                PrecursorTester.Spatial.sameEntity(), Duration.ZERO, T0, T0.plus(Duration.ofHours(40)), 0.0, "sym"));
        assertThat(result.coOccurrence()).isTrue();
    }

    @Test
    @DisplayName("fewer than ten antecedents is insufficient")
    void insufficient() {
        var result = run(hourly(5, Duration.ZERO, "x"), hourly(5, Duration.ofMinutes(30), "x"));
        assertThat(result.verdict()).isEqualTo("insufficient");
    }

    @Test
    @DisplayName("Wilson bounds behave at the edges")
    void wilson() {
        assertThat(Wilson.interval(0, 0)).containsExactly(0, 1);
        assertThat(Wilson.interval(10, 10)[1]).isEqualTo(1.0);
        double[] mid = Wilson.interval(5, 10);
        assertThat(mid[0]).isLessThan(0.5).isGreaterThan(0);
        assertThat(mid[1]).isGreaterThan(0.5).isLessThan(1);
    }
}
