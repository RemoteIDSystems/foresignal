package ai.foresignal.source.adapters.elexon;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.time.Year;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@code grid-frequency-gb} (prompt 093): the capped catch-up window and the attribution year. */
class GridFrequencyGbWindowTest {

    static final Instant NOW = Instant.parse("2026-10-14T12:00:00Z");

    @Test
    @DisplayName("after an outage the window starts at most an hour back; normally at the last sample")
    void window() {
        assertThat(GridFrequencyGbAdapter.windowStart(NOW.minus(Duration.ofDays(2)), NOW))
                .isEqualTo(NOW.minus(GridFrequencyGbAdapter.MAX_WINDOW));
        assertThat(GridFrequencyGbAdapter.windowStart(NOW.minusSeconds(120), NOW)).isEqualTo(NOW.minusSeconds(120));
        assertThat(GridFrequencyGbAdapter.windowStart(null, NOW)).isEqualTo(NOW.minus(GridFrequencyGbAdapter.FIRST_WINDOW));
    }

    @Test
    @DisplayName("the attribution names the year it is read in, not the year the class loaded")
    void attribution() {
        assertThat(GridFrequencyGbAdapter.attribution(Year.of(2031))).endsWith("database right 2031");
        assertThat(new GridFrequencyGbAdapter(null).descriptor().attribution())
                .endsWith("database right " + Year.now(ZoneOffset.UTC));
    }
}
