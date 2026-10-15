package ai.foresignal.source.adapters.derived;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@code derived-dark-vessel-match} reachability (prompt 099). */
class DarkVesselMatchTest {

    @Test
    @DisplayName("the reachable radius grows with the gap and is capped at 200 km")
    void reachable() {
        Instant start = Instant.parse("2026-10-15T10:00:00Z");
        assertThat(DarkVesselMatchGenerator.reachableKm(start, start.plusSeconds(3600))).isGreaterThan(40);
        assertThat(DarkVesselMatchGenerator.reachableKm(start, start.plusSeconds(30 * 24 * 3600)))
                .isEqualTo(DarkVesselMatchGenerator.REACH_KM);
    }
}
