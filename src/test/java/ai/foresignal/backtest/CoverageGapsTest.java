package ai.foresignal.backtest;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Coverage gaps from {@code fetch_log} rows (prompt 103, {@code COVERAGE_GAPS}). */
class CoverageGapsTest {

    private static final Instant T0 = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant END = T0.plus(Duration.ofHours(10));

    /** OK runs every 10 minutes over the whole range. */
    private static List<CoverageGaps.Run> steady() {
        List<CoverageGaps.Run> runs = new ArrayList<>();
        for (Instant t = T0; !t.isAfter(END); t = t.plus(Duration.ofMinutes(10))) {
            runs.add(new CoverageGaps.Run(t, "OK"));
        }
        return runs;
    }

    @Test
    @DisplayName("steady successful runs have no gaps")
    void steadyHasNoGaps() {
        assertThat(CoverageGaps.gaps(steady(), T0, END)).isEmpty();
        assertThat(CoverageGaps.fraction(CoverageGaps.gaps(steady(), T0, END), T0, END)).isZero();
    }

    @Test
    @DisplayName("a run of failures is a gap until the next success")
    void failures() {
        List<CoverageGaps.Run> runs = new ArrayList<>(steady());
        runs.replaceAll(r -> !r.startedAt().isBefore(T0.plus(Duration.ofHours(2)))
                && r.startedAt().isBefore(T0.plus(Duration.ofHours(4))) ? new CoverageGaps.Run(r.startedAt(), "FAILED")
                        : r);
        List<CoverageGaps.Gap> gaps = CoverageGaps.gaps(runs, T0, END);
        assertThat(gaps).containsExactly(new CoverageGaps.Gap(T0.plus(Duration.ofHours(2)), T0.plus(Duration.ofHours(4))));
        assertThat(CoverageGaps.fraction(gaps, T0, END)).isEqualTo(0.2);
    }

    @Test
    @DisplayName("refused runs at the end are a gap to the end of the range")
    void refusedAtEnd() {
        List<CoverageGaps.Run> runs = new ArrayList<>(steady());
        runs.replaceAll(r -> !r.startedAt().isBefore(T0.plus(Duration.ofHours(9))) ? new CoverageGaps.Run(
                r.startedAt(), "REFUSED") : r);
        assertThat(CoverageGaps.fraction(CoverageGaps.gaps(runs, T0, END), T0, END)).isEqualTo(0.1);
    }

    @Test
    @DisplayName("a silence longer than three typical intervals is a gap")
    void silence() {
        List<CoverageGaps.Run> runs = new ArrayList<>(steady());
        runs.removeIf(r -> r.startedAt().isAfter(T0.plus(Duration.ofHours(5)))
                && r.startedAt().isBefore(T0.plus(Duration.ofHours(6))));
        List<CoverageGaps.Gap> gaps = CoverageGaps.gaps(runs, T0, END);
        assertThat(gaps).containsExactly(new CoverageGaps.Gap(T0.plus(Duration.ofHours(5)), T0.plus(Duration.ofHours(6))));
    }

    @Test
    @DisplayName("no rows at all leave the whole range uncovered")
    void noRows() {
        assertThat(CoverageGaps.fraction(CoverageGaps.gaps(List.of(), T0, END), T0, END)).isEqualTo(1.0);
    }

    @Test
    @DisplayName("overlapping gaps merge before the fraction is taken")
    void merge() {
        List<CoverageGaps.Gap> gaps = List.of(new CoverageGaps.Gap(T0, T0.plus(Duration.ofHours(2))),
                new CoverageGaps.Gap(T0.plus(Duration.ofHours(1)), T0.plus(Duration.ofHours(3))));
        assertThat(CoverageGaps.merge(gaps)).hasSize(1);
        assertThat(CoverageGaps.fraction(gaps, T0, END)).isEqualTo(0.3);
    }
}
