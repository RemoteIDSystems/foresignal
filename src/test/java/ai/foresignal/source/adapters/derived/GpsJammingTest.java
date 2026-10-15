package ai.foresignal.source.adapters.derived;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@code derived-gps-jamming} thresholds (prompt 099). */
class GpsJammingTest {

    @Test
    @DisplayName("a cell needs three aircraft before a level is asserted")
    void minimumAircraft() {
        assertThat(GpsJammingGenerator.level(2, 2)).isEqualTo("low");
        assertThat(GpsJammingGenerator.level(3, 1)).isEqualTo("medium");
    }

    @Test
    @DisplayName("the boundaries are 30% medium and 50% high, inclusive")
    void boundaries() {
        assertThat(GpsJammingGenerator.level(10, 2)).isEqualTo("low");
        assertThat(GpsJammingGenerator.level(10, 3)).isEqualTo("medium");
        assertThat(GpsJammingGenerator.level(10, 4)).isEqualTo("medium");
        assertThat(GpsJammingGenerator.level(10, 5)).isEqualTo("high");
    }

    @Test
    @DisplayName("H3 and 1-degree keys are stable")
    void keys() {
        String h3 = GpsJammingGenerator.key(-95.5, 32.5, true);
        assertThat(h3).startsWith("h3:");
        assertThat(GpsJammingGenerator.key(-95.5, 32.5, false)).isEqualTo("deg:32:-96");
    }
}
