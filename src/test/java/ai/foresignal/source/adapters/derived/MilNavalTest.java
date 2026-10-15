package ai.foresignal.source.adapters.derived;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.derive.Observation;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.PositionSource;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@code derived-mil-naval} classification (prompt 099). */
class MilNavalTest {

    private static Observation observation(String source, String kind, Map<String, Object> payload) {
        return new Observation(source, "id", kind, Instant.parse("2026-10-15T10:00:00Z"), Geo.point(-95, 32), payload,
                PositionSource.UNKNOWN);
    }

    @Test
    @DisplayName("the military sources and the readsb mil flag qualify")
    void military() {
        assertThat(MilNavalGenerator.military(observation("adsblol-mil", "aircraft", Map.of()))).isTrue();
        assertThat(MilNavalGenerator.military(observation("adsblol-area-1", "aircraft", Map.of("mil", true)))).isTrue();
        assertThat(MilNavalGenerator.military(observation("adsblol-area-1", "aircraft", Map.of("gs", 250)))).isFalse();
    }

    @Test
    @DisplayName("only AIS ship type 35 is naval")
    void naval() {
        assertThat(MilNavalGenerator.naval(observation("aisstream", "vessel", Map.of("shipType", 35)))).isTrue();
        assertThat(MilNavalGenerator.naval(observation("aisstream", "vessel", Map.of("shipType", 70)))).isFalse();
    }
}
