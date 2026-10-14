package ai.foresignal.notify.mesh;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * LoRa time on air (prompt 084). The reference numbers below were computed by hand with the formula of Semtech's
 * SX1276 datasheet §4.1.1.7 / AN1200.13 (explicit header, CRC on); the Semtech LoRa Calculator implements the same
 * formula. SF7/125 kHz/4/5 with an 8-symbol preamble and 10 bytes is the commonly quoted 41.2 ms.
 *
 * <ul>
 *   <li>SF7, 125 kHz, 4/5, preamble 8, 10 bytes: Tsym 1.024 ms; preamble 12.25 × 1.024 = 12.544 ms; payload symbols
 *       8 + ceil((80 − 28 + 28 + 16) / 28) × 5 = 28 → 28.672 ms; total <strong>41.216 ms</strong>.
 *   <li>LongFast (SF11, 250 kHz, 4/5, preamble 16), 132 bytes (100 + 32 overhead): Tsym 8.192 ms (no low-data-rate
 *       optimization); preamble 20.25 × 8.192 = 165.888 ms; payload 8 + ceil(1056 / 44) × 5 = 128 symbols →
 *       1048.576 ms; total <strong>1214.464 ms</strong>.
 *   <li>LongFast, 92 bytes (60 + 32): 93 symbols → <strong>927.744 ms</strong> (the Operator Guide's worked example).
 *   <li>SF12, 125 kHz, 4/5, preamble 8, 10 bytes: Tsym 32.768 ms, so DE = 1; payload 8 + ceil(80/40) × 5 = 18
 *       symbols; total (12.25 + 18) × 32.768 = <strong>991.232 ms</strong>.
 * </ul>
 */
class LoRaAirtimeTest {

    @Test
    @DisplayName("SF7/125 kHz, 10 bytes: 41.216 ms")
    void sf7() {
        LoRaAirtime sf7 = new LoRaAirtime(7, 125_000, 5, 8);
        assertThat(sf7.symbolSeconds()).isCloseTo(0.001024, within(1e-12));
        assertThat(sf7.payloadSymbols(10)).isEqualTo(28);
        assertThat(sf7.timeOnAirSeconds(10)).isCloseTo(0.041216, within(1e-9));
        assertThat(sf7.lowDataRateOptimize()).isFalse();
    }

    @Test
    @DisplayName("LongFast: 132 bytes take 1214.464 ms, a 60-byte Meshtastic line 927.744 ms")
    void longFast() {
        LoRaAirtime lf = LoRaAirtime.MESHTASTIC_LONGFAST;
        assertThat(lf.symbolSeconds()).isCloseTo(0.008192, within(1e-12));
        assertThat(lf.lowDataRateOptimize()).isFalse();
        assertThat(lf.payloadSymbols(132)).isEqualTo(128);
        assertThat(lf.timeOnAirSeconds(132)).isCloseTo(1.214464, within(1e-9));
        assertThat(lf.messageSeconds("meshtastic", 100)).isCloseTo(1.214464, within(1e-9));
        assertThat(lf.messageSeconds("meshtastic", 60)).isCloseTo(0.927744, within(1e-9));
        assertThat(lf.messageSeconds("meshcore", 108)).as("MeshCore adds 24 bytes")
                .isCloseTo(lf.timeOnAirSeconds(132), within(1e-12));
    }

    @Test
    @DisplayName("SF12/125 kHz turns low-data-rate optimization on: 991.232 ms for 10 bytes")
    void lowDataRate() {
        LoRaAirtime sf12 = new LoRaAirtime(12, 125_000, 5, 8);
        assertThat(sf12.lowDataRateOptimize()).isTrue();
        assertThat(sf12.payloadSymbols(10)).isEqualTo(18);
        assertThat(sf12.timeOnAirSeconds(10)).isCloseTo(0.991232, within(1e-9));
    }

    @Test
    @DisplayName("presets: LongFast, MediumFast and custom; bad settings are refused")
    void presets() {
        assertThat(LoRaAirtime.preset("meshtastic-longfast", null)).isEqualTo(LoRaAirtime.MESHTASTIC_LONGFAST);
        assertThat(LoRaAirtime.preset("meshtastic-mediumfast", null)).isEqualTo(new LoRaAirtime(9, 250_000, 5, 16));
        assertThat(LoRaAirtime.preset("custom", Map.of("spreadingFactor", 10, "bandwidthKhz", 250, "codingRate", 5,
                "preambleSymbols", 8))).isEqualTo(new LoRaAirtime(10, 250_000, 5, 8));
        assertThatThrownBy(() -> LoRaAirtime.preset("custom", null)).hasMessageContaining("airtimeCustom");
        assertThatThrownBy(() -> LoRaAirtime.preset("shortturbo", null)).hasMessageContaining("must be one of");
        assertThatThrownBy(() -> new LoRaAirtime(13, 125_000, 5, 8)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new LoRaAirtime(7, 125_000, 9, 8)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> LoRaAirtime.preset("custom", Map.of("spreadingFactor", 10.5, "bandwidthKhz", 250,
                "codingRate", 5, "preambleSymbols", 8))).isInstanceOf(IllegalArgumentException.class);
    }
}
