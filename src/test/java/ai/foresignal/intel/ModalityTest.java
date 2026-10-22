package ai.foresignal.intel;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.evidence.Originator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The sensing modality derived from the originator class (prompt 154). */
class ModalityTest {

    @Test
    @DisplayName("each originator class maps to its modality")
    void mapsClasses() {
        assertThat(Modality.of("sensor-network")).isEqualTo(Modality.SENSOR);
        assertThat(Modality.of("imagery")).isEqualTo(Modality.IMAGERY);
        assertThat(Modality.of("agency")).isEqualTo(Modality.OFFICIAL);
        assertThat(Modality.of("social")).isEqualTo(Modality.SOCIAL);
        assertThat(Modality.of("media")).isEqualTo(Modality.MEDIA);
        assertThat(Modality.of("crowd")).isEqualTo(Modality.CROWD);
    }

    @Test
    @DisplayName("an unmapped or null class is other")
    void unmapped() {
        assertThat(Modality.of("aggregator")).isEqualTo(Modality.OTHER);
        assertThat(Modality.of(null)).isEqualTo(Modality.OTHER);
    }

    @Test
    @DisplayName("imagery is a recognized originator class")
    void imageryIsAClass() {
        assertThat(Originator.CLASSES).contains("imagery");
    }
}
