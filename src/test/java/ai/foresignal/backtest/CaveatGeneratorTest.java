package ai.foresignal.backtest;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The backtest caveat generator (prompt 103). */
class CaveatGeneratorTest {

    private static List<String> codes(CaveatGenerator.Inputs inputs) {
        return CaveatGenerator.generate(inputs).stream().map(BacktestCaveat::code).toList();
    }

    @Test
    @DisplayName("NOT_GROUND_TRUTH is always present")
    void always() {
        assertThat(codes(new CaveatGenerator.Inputs(false, false, false, false, false, false, false, 100)))
                .containsExactly(BacktestCaveat.NOT_GROUND_TRUTH);
    }

    @Test
    @DisplayName("every applicable caveat is emitted, and SMALL_SAMPLE at n < 10")
    void every() {
        List<String> codes = codes(new CaveatGenerator.Inputs(true, true, true, true, true, true, true, 3));
        assertThat(codes).contains(BacktestCaveat.RANGE_CLAMPED_BY_RETENTION, BacktestCaveat.DOWNSAMPLED_RANGE,
                BacktestCaveat.SAMPLED_SOURCE, BacktestCaveat.COVERAGE_GAPS, BacktestCaveat.DEFINITION_DIFFERS,
                BacktestCaveat.ENTITY_RESOLUTION_CHANGED, BacktestCaveat.WINDOW_APPROXIMATE,
                BacktestCaveat.SMALL_SAMPLE, BacktestCaveat.NOT_GROUND_TRUTH);
    }

    @Test
    @DisplayName("the stored form carries the code and text")
    void stored() {
        var stored = CaveatGenerator.stored(
                CaveatGenerator.generate(new CaveatGenerator.Inputs(false, false, false, false, false, false, false, 100)));
        assertThat(stored).hasSize(1);
        assertThat(stored.get(0)).containsEntry("code", BacktestCaveat.NOT_GROUND_TRUTH);
        assertThat(stored.get(0).get("text")).isNotNull();
    }
}
