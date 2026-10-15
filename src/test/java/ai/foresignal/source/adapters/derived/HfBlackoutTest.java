package ai.foresignal.source.adapters.derived;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.derive.Observation;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.PositionSource;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@code derived-hf-blackout} baseline math (prompt 099). */
class HfBlackoutTest {

    @Test
    @DisplayName("the median is the middle value, or the mean of the two middle values")
    void median() {
        assertThat(HfBlackoutGenerator.median(List.of(1.0, 3.0, 2.0))).isEqualTo(2.0);
        assertThat(HfBlackoutGenerator.median(List.of(1.0, 2.0, 3.0, 4.0))).isEqualTo(2.5);
        assertThat(HfBlackoutGenerator.median(List.of())).isNaN();
    }

    @Test
    @DisplayName("the path class is the sender's Maidenhead field and the DXCC")
    void path() {
        Observation spot = new Observation("pskreporter-ft8", "s|r|14074|1", "radio-spot", Instant.EPOCH, Geo.point(0, 0),
                Map.of("band", "20m", "senderLocator", "EM13", "dxcc", "291", "snr", -12), PositionSource.UNKNOWN);
        assertThat(HfBlackoutGenerator.pathClass(spot)).isEqualTo("EM:291");
    }
}
