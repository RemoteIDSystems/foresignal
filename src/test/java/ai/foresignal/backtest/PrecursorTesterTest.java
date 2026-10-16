package ai.foresignal.backtest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The precursor tester (prompt 103). */
class PrecursorTesterTest {

    private static final Instant T0 = Instant.parse("2026-01-01T00:00:00Z");

    /** Irregular gaps (hours) so that no circular shift lines the series up with itself. */
    private static final int[] GAPS = {3, 7, 2, 5, 11, 4, 6, 9, 3, 8, 5, 2, 10, 4, 7, 3, 6, 12, 5, 4};

    /** The end of the range: six hours after the last irregular occurrence. */
    private static final Instant END = T0.plus(Duration.ofHours(122));

    /** {@code n} occurrences at the irregular times plus {@code offset}, all with entity {@code e}. */
    private static List<PrecursorTester.Occurrence> irregular(int n, Duration offset, String entity) {
        List<PrecursorTester.Occurrence> out = new ArrayList<>();
        long hours = 0;
        for (int i = 0; i < n; i++) {
            hours += GAPS[i % GAPS.length];
            out.add(new PrecursorTester.Occurrence(T0.plus(Duration.ofHours(hours)).plus(offset), -95.0, 32.0, "US",
                    entity));
        }
        return out;
    }

    private static PrecursorTester.Result run(List<PrecursorTester.Occurrence> a,
            List<PrecursorTester.Occurrence> c, Duration maxLag, double gaps) {
        return PrecursorTester.test(new PrecursorTester.Request(a, c, Duration.ZERO, maxLag,
                PrecursorTester.Spatial.sameEntity(), Duration.ZERO, T0, END, gaps, "seed"));
    }

    @Test
    @DisplayName("planted precedence (deterministic) is notable, and one-way precedence is not co-occurrence")
    void planted() {
        var result = run(irregular(20, Duration.ZERO, "x"), irregular(20, Duration.ofMinutes(30), "x"),
                Duration.ofHours(1), 0);
        assertThat(result.verdict()).isEqualTo("notable");
        assertThat(result.hitRate()).isEqualTo(1.0);
        assertThat(result.lift()).isGreaterThanOrEqualTo(PrecursorTester.NOTABLE_LIFT);
        assertThat(result.lagMedian()).isEqualTo(30);
        assertThat(result.insufficient()).isFalse();
        assertThat(result.coOccurrence()).isFalse();
        assertThat(result.reverse().hits()).isZero();
        assertThat(result.note()).isEqualTo("association in stored data; not causation");
    }

    @Test
    @DisplayName("independent processes are no-evidence")
    void independent() {
        // Consequents with a different entity and a spatial sameEntity rule -> no hits.
        var result = run(irregular(20, Duration.ZERO, "x"), irregular(20, Duration.ofMinutes(30), "y"),
                Duration.ofHours(1), 0);
        assertThat(result.hitRate()).isZero();
        assertThat(result.verdict()).isEqualTo("no-evidence-of-precedence");
    }

    @Test
    @DisplayName("deterministic shifts give the same p-value and base rate twice")
    void deterministic() {
        var a = run(irregular(20, Duration.ZERO, "x"), irregular(20, Duration.ofMinutes(30), "x"),
                Duration.ofHours(1), 0);
        var b = run(irregular(20, Duration.ZERO, "x"), irregular(20, Duration.ofMinutes(30), "x"),
                Duration.ofHours(1), 0);
        assertThat(a.pValue()).isEqualTo(b.pValue());
        assertThat(a.baseRate()).isEqualTo(b.baseRate());
        assertThat(a.seed()).isEqualTo(b.seed()).isEqualTo(PrecursorTester.seed("seed"));
    }

    @Test
    @DisplayName("symmetric co-occurrence flags reverse, without recursing")
    void symmetric() {
        List<PrecursorTester.Occurrence> both = irregular(20, Duration.ofMinutes(20), "x");
        assertThatCode(() -> run(both, both, Duration.ofMinutes(30), 0)).doesNotThrowAnyException();
        var result = run(both, both, Duration.ofMinutes(30), 0);
        assertThat(result.verdict()).isEqualTo("notable");
        assertThat(result.reverse().strong()).isTrue();
        assertThat(result.coOccurrence()).isTrue();
    }

    @Test
    @DisplayName("fewer than ten antecedents is insufficient")
    void insufficient() {
        var result = run(irregular(5, Duration.ZERO, "x"), irregular(5, Duration.ofMinutes(30), "x"),
                Duration.ofHours(1), 0);
        assertThat(result.verdict()).isEqualTo("insufficient");
    }

    @Test
    @DisplayName("coverage gaps over 30% of the range make the verdict insufficient")
    void coverageGaps() {
        var result = run(irregular(20, Duration.ZERO, "x"), irregular(20, Duration.ofMinutes(30), "x"),
                Duration.ofHours(1), 0.31);
        assertThat(result.insufficient()).isTrue();
        assertThat(result.verdict()).isEqualTo("insufficient");
    }

    @Test
    @DisplayName("a circular shift wraps around the range instead of leaving it")
    void circularShiftWraps() {
        long span = Duration.ofHours(10).toSeconds();
        Instant late = T0.plus(Duration.ofHours(9));
        Instant moved = PrecursorTester.circularShift(late, Duration.ofHours(3).toSeconds(), T0, span);
        assertThat(moved).isEqualTo(T0.plus(Duration.ofHours(2)));
        assertThat(PrecursorTester.circularShift(T0, Duration.ofHours(3).toSeconds(), T0, span))
                .isEqualTo(T0.plus(Duration.ofHours(3)));
        assertThat(PrecursorTester.circularShift(T0.plus(Duration.ofHours(4)), span, T0, span))
                .isEqualTo(T0.plus(Duration.ofHours(4)));
    }

    @Test
    @DisplayName("sameRegion compares the country or the first-level region")
    void sameRegion() {
        var a = new PrecursorTester.Occurrence(T0, null, null, "US", "US-TX", Set.of());
        var c = new PrecursorTester.Occurrence(T0, null, null, "US", "US-OK", Set.of());
        assertThat(PrecursorTester.spatially(a, c, PrecursorTester.Spatial.sameRegion("country"))).isTrue();
        assertThat(PrecursorTester.spatially(a, c, PrecursorTester.Spatial.sameRegion("admin1"))).isFalse();
        var unknown = new PrecursorTester.Occurrence(T0, null, null, null, null, Set.of());
        assertThat(PrecursorTester.spatially(a, unknown, PrecursorTester.Spatial.sameRegion())).isFalse();
    }

    @Test
    @DisplayName("sameEntity matches when the entity sets share a member")
    void sameEntity() {
        var event = new PrecursorTester.Occurrence(T0, null, null, null, null, Set.of("ais:1", "ais:2"));
        var alert = new PrecursorTester.Occurrence(T0, null, null, null, "ais:2");
        var other = new PrecursorTester.Occurrence(T0, null, null, null, "ais:3");
        assertThat(PrecursorTester.spatially(event, alert, PrecursorTester.Spatial.sameEntity())).isTrue();
        assertThat(PrecursorTester.spatially(event, other, PrecursorTester.Spatial.sameEntity())).isFalse();
    }

    @Test
    @DisplayName("withinKm uses the great-circle distance")
    void withinKm() {
        var dallas = new PrecursorTester.Occurrence(T0, -96.8, 32.78, null, null);
        var fortWorth = new PrecursorTester.Occurrence(T0, -97.33, 32.75, null, null);
        assertThat(PrecursorTester.spatially(dallas, fortWorth, PrecursorTester.Spatial.withinKm(100))).isTrue();
        assertThat(PrecursorTester.spatially(dallas, fortWorth, PrecursorTester.Spatial.withinKm(10))).isFalse();
    }

    @Test
    @DisplayName("the dedup window merges close antecedents")
    void dedup() {
        List<PrecursorTester.Occurrence> close = List.of(
                new PrecursorTester.Occurrence(T0, null, null, null, "x"),
                new PrecursorTester.Occurrence(T0.plusSeconds(60), null, null, null, "x"),
                new PrecursorTester.Occurrence(T0.plusSeconds(7200), null, null, null, "x"));
        assertThat(PrecursorTester.dedup(PrecursorTester.sort(close), Duration.ofHours(1))).hasSize(2);
        assertThat(PrecursorTester.dedup(PrecursorTester.sort(close), Duration.ZERO)).hasSize(3);
    }

    @Test
    @DisplayName("verdict thresholds are the documented constants")
    void verdicts() {
        assertThat(PrecursorTester.verdict(false, 0.5, 2.0, 0.05)).isEqualTo("notable");
        assertThat(PrecursorTester.verdict(false, 0.5, 1.5, 0.10)).isEqualTo("weak");
        assertThat(PrecursorTester.verdict(false, 0.5, 1.4, 0.10)).isEqualTo("no-evidence-of-precedence");
        assertThat(PrecursorTester.verdict(true, 1.0, 9, 0.001)).isEqualTo("insufficient");
    }

    @Test
    @DisplayName("Wilson bounds against reference values")
    void wilson() {
        assertThat(Wilson.interval(0, 0)).containsExactly(0, 1);
        assertThat(Wilson.interval(10, 10)[1]).isEqualTo(1.0);
        double[] mid = Wilson.interval(5, 10);
        // Reference: Wilson 95% for 5/10 is [0.2366, 0.7634].
        assertThat(mid[0]).isBetween(0.2365, 0.2367);
        assertThat(mid[1]).isBetween(0.7633, 0.7635);
    }
}
