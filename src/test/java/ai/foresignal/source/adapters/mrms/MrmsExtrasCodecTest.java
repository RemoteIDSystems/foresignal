package ai.foresignal.source.adapters.mrms;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The MRMS lightning-probability, VIL and echo-top code spaces (prompt 106). */
class MrmsExtrasCodecTest {

    @Test
    @DisplayName("VIL is u16 at 0.01 kg/m², missing above the cap")
    void vil() {
        assertThat(MrmsCodec.vil(0)).isZero();
        assertThat(MrmsCodec.vil(2.5f)).isEqualTo(250);
        assertThat(MrmsCodec.vil(80f)).isEqualTo(8000);
        assertThat(MrmsCodec.vil(Float.NaN)).isEqualTo(MrmsCodec.U16_MISSING);
        assertThat(MrmsCodec.vil(-3f)).isEqualTo(MrmsCodec.U16_MISSING);
    }

    @Test
    @DisplayName("echo top is u16 at 0.1 km")
    void echoTop() {
        assertThat(MrmsCodec.echoTop(12f)).isEqualTo(120);
        assertThat(MrmsCodec.echoTop(0f)).isZero();
        assertThat(MrmsCodec.echoTop(-1f)).isEqualTo(MrmsCodec.U16_MISSING);
    }

    @Test
    @DisplayName("lightning probability is u8 percent, sentinels to no data")
    void lightningProbability() {
        assertThat(MrmsCodec.lightningProbability(0f)).isZero();
        assertThat(MrmsCodec.lightningProbability(50f)).isEqualTo(50);
        assertThat(MrmsCodec.lightningProbability(300f)).isEqualTo(254);
        assertThat(MrmsCodec.lightningProbability(-999f)).isEqualTo(MrmsCodec.LTNG_PROB_NO_DATA);
        assertThat(MrmsCodec.lightningProbability(Float.NaN)).isEqualTo(MrmsCodec.LTNG_PROB_NO_DATA);
    }

    @Test
    @DisplayName("the products are in the descriptor")
    void products() {
        MrmsAdapter adapter = new MrmsAdapter(null, "");
        assertThat(adapter.descriptor().products()).extracting(ai.foresignal.source.spi.ProductSpec::id)
                .contains("mrms-lightning-prob", "mrms-vil", "mrms-echo-top-50");
    }
}
