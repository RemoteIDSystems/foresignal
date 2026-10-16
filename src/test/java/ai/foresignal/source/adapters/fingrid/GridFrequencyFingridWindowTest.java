package ai.foresignal.source.adapters.fingrid;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@code grid-frequency-fingrid} (prompt 093): the capped catch-up window after an outage. */
class GridFrequencyFingridWindowTest {

    static final Instant NOW = Instant.parse("2026-10-14T12:00:00Z");

    @Test
    @DisplayName("after an outage the window starts at most an hour back; normally at the last sample")
    void window() {
        assertThat(GridFrequencyFingridAdapter.windowStart(NOW.minus(Duration.ofDays(3)), NOW))
                .isEqualTo(NOW.minus(GridFrequencyFingridAdapter.MAX_WINDOW));
        assertThat(GridFrequencyFingridAdapter.windowStart(NOW.minusSeconds(180), NOW)).isEqualTo(NOW.minusSeconds(180));
        assertThat(GridFrequencyFingridAdapter.windowStart(null, NOW))
                .isEqualTo(NOW.minus(GridFrequencyFingridAdapter.FIRST_WINDOW));
    }
}
