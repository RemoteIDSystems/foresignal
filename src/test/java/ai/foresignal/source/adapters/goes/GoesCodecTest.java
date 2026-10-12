package ai.foresignal.source.adapters.goes;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** GOES IR quantization: u16, scale 0.01 K, offset 150 K (prompt 046). */
class GoesCodecTest {

    @Test
    @DisplayName("code = round((K - 150) x 100); NaN is 65535; out-of-range values clamp")
    void codes() {
        assertThat(GoesCodec.brightness(150f)).isZero();
        assertThat(GoesCodec.brightness(297.3322f)).isEqualTo(14_733);
        assertThat(GoesCodec.brightness(190.53f)).isEqualTo(4_053);
        assertThat(GoesCodec.brightness(100f)).isZero();
        assertThat(GoesCodec.brightness(Float.NaN)).isEqualTo(65_535);
        assertThat(GoesCodec.brightness(Float.POSITIVE_INFINITY)).isEqualTo(65_535);
        assertThat(GoesCodec.brightness(900f)).isEqualTo(65_534);
        assertThat(GoesCodec.brightness(new float[] {150f, Float.NaN})).containsExactly(0, 65_535);
        int code = GoesCodec.brightness(273.15f);
        assertThat(code * GoesCodec.SCALE_K + GoesCodec.OFFSET_K).isCloseTo(273.15, org.assertj.core.data.Offset.offset(0.005));
    }
}
