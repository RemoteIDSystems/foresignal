package ai.foresignal.source.adapters.derived;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@code derived-chokepoint} helpers (prompt 099). */
class ChokepointTest {

    @Test
    @DisplayName("counts band to 0,1,3,5,10,20,50,100")
    void bands() {
        assertThat(ChokepointGenerator.band(0)).isZero();
        assertThat(ChokepointGenerator.band(2)).isEqualTo(1);
        assertThat(ChokepointGenerator.band(4)).isEqualTo(3);
        assertThat(ChokepointGenerator.band(60)).isEqualTo(50);
        assertThat(ChokepointGenerator.band(400)).isEqualTo(100);
    }

    @Test
    @DisplayName("containment uses the per-chokepoint radius")
    void containing() {
        List<ReferenceData.Chokepoint> chokepoints = List.of(new ReferenceData.Chokepoint("hormuz", "Hormuz", 56.5, 26.5, 60));
        assertThat(ChokepointGenerator.containing(chokepoints, 56.6, 26.6)).isNotNull();
        assertThat(ChokepointGenerator.containing(chokepoints, 0, 0)).isNull();
    }

    @Test
    @DisplayName("wrap keeps course deltas in [-180, 180]")
    void wrapping() {
        assertThat(ChokepointGenerator.wrap(190)).isEqualTo(-170);
    }
}
