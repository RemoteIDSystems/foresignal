package ai.foresignal.source.adapters.derived;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@code derived-holding} turn math (prompt 099). */
class HoldingPatternTest {

    private static HoldingPatternGenerator.Point point(double track) {
        return new HoldingPatternGenerator.Point(Instant.parse("2026-10-15T10:00:00Z"), -95, 32, 10000, track);
    }

    @Test
    @DisplayName("a full circle exceeds 300 degrees; a straight line does not")
    void turns() {
        assertThat(HoldingPatternGenerator.cumulativeTurn(List.of(point(0), point(90), point(180), point(270), point(0))))
                .isEqualTo(360);
        assertThat(HoldingPatternGenerator.cumulativeTurn(List.of(point(0), point(1), point(2)))).isLessThan(300);
    }

    @Test
    @DisplayName("wrap keeps deltas in [-180, 180]")
    void wrapping() {
        assertThat(HoldingPatternGenerator.wrap(350)).isEqualTo(-10);
        assertThat(HoldingPatternGenerator.wrap(-350)).isEqualTo(10);
    }
}
