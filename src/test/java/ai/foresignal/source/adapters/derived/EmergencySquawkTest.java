package ai.foresignal.source.adapters.derived;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@code derived-emergency-squawk} evidence gate (prompt 099). */
class EmergencySquawkTest {

    @Test
    @DisplayName("two observations ten seconds apart are enough; one is not")
    void confirmation() {
        EmergencySquawkGenerator.Active state = new EmergencySquawkGenerator.Active();
        state.observations.add(Instant.parse("2026-10-15T10:00:00Z"));
        assertThat(EmergencySquawkGenerator.enoughEvidence(state)).isFalse();
        state.observations.add(Instant.parse("2026-10-15T10:00:15Z"));
        assertThat(EmergencySquawkGenerator.enoughEvidence(state)).isTrue();
    }

    @Test
    @DisplayName("two independent sources are enough on their own")
    void twoSources() {
        EmergencySquawkGenerator.Active state = new EmergencySquawkGenerator.Active();
        state.sources.add("adsblol-area-1");
        state.sources.add("adsbfi-region-1");
        assertThat(EmergencySquawkGenerator.enoughEvidence(state)).isTrue();
    }

    @Test
    @DisplayName("the known squawks map to their meanings")
    void meanings() {
        assertThat(EmergencySquawkGenerator.MEANINGS).containsEntry("7500", "unlawful interference")
                .containsEntry("7700", "emergency");
    }
}
