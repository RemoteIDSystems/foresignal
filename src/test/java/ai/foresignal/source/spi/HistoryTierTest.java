package ai.foresignal.source.spi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/** The stored codes of {@link HistoryTier}, which are also the list-partition keys. */
class HistoryTierTest {

    @Test
    void codesMatchThePartitionKeys() {
        assertThat(HistoryTier.STANDARD.code()).isEqualTo((short) 0);
        assertThat(HistoryTier.TRACK.code()).isEqualTo((short) 1);
    }

    @Test
    void codesRoundTrip() {
        for (HistoryTier tier : HistoryTier.values()) {
            assertThat(HistoryTier.fromCode(tier.code())).isEqualTo(tier);
        }
    }

    @Test
    void unknownCodeIsRejected() {
        assertThatThrownBy(() -> HistoryTier.fromCode((short) 2)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void wireValueIsLowercase() {
        assertThat(HistoryTier.TRACK.wireValue()).isEqualTo("track");
    }
}
