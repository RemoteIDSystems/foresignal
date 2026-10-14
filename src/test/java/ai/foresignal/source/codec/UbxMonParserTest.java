package ai.foresignal.source.codec;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link UbxMonParser} (prompt 086): UBX-MON-HW's {@code jammingState} and {@code jamInd}, UBX-MON-RF's worst block, and
 * nothing for a bad checksum, a short payload or another message.
 */
class UbxMonParserTest {

    /** A MON-HW frame with {@code flags} and {@code jamInd}. */
    static byte[] monHw(int jammingState, int jamInd) {
        byte[] payload = new byte[UbxMonParser.MON_HW_LENGTH];
        payload[22] = (byte) ((jammingState & 0x03) << 2 | 0x01);
        payload[45] = (byte) jamInd;
        return UbxMonParser.frame(UbxMonParser.CLASS_MON, UbxMonParser.ID_MON_HW, payload);
    }

    /** A MON-RF frame with one block per pair of (jammingState, jamInd). */
    static byte[] monRf(int... stateAndJam) {
        int blocks = stateAndJam.length / 2;
        byte[] payload = new byte[4 + blocks * UbxMonParser.MON_RF_BLOCK];
        payload[1] = (byte) blocks;
        for (int i = 0; i < blocks; i++) {
            int base = 4 + i * UbxMonParser.MON_RF_BLOCK;
            payload[base] = (byte) i;
            payload[base + 1] = (byte) (stateAndJam[2 * i] & 0x03);
            payload[base + 16] = (byte) stateAndJam[2 * i + 1];
        }
        return UbxMonParser.frame(UbxMonParser.CLASS_MON, UbxMonParser.ID_MON_RF, payload);
    }

    @Test
    @DisplayName("MON-HW: jammingState from flags bits 2-3 and jamInd at offset 45")
    void monHwReading() {
        UbxMonParser.Reading r = UbxMonParser.parse(monHw(3, 201)).orElseThrow();
        assertThat(r.message()).isEqualTo("MON-HW");
        assertThat(r.jammingState()).isEqualTo(3);
        assertThat(r.jamIndicator()).isEqualTo(201);
        assertThat(r.jammed()).isTrue();
        assertThat(UbxMonParser.parse(monHw(1, 12)).orElseThrow().jammed()).isFalse();
    }

    @Test
    @DisplayName("MON-RF: the worst state and the highest jamInd over the blocks")
    void monRfReading() {
        UbxMonParser.Reading r = UbxMonParser.parse(monRf(1, 30, 2, 90)).orElseThrow();
        assertThat(r.message()).isEqualTo("MON-RF");
        assertThat(r.jammingState()).isEqualTo(UbxMonParser.JAMMING_WARNING);
        assertThat(r.jamIndicator()).isEqualTo(90);
    }

    @Test
    @DisplayName("a bad checksum, a short payload, zero blocks, another class or id: no reading")
    void rejects() {
        byte[] bad = monHw(2, 5);
        bad[bad.length - 1] ^= 0x01;
        assertThat(UbxMonParser.valid(bad)).isFalse();
        assertThat(UbxMonParser.parse(bad)).isEmpty();
        assertThat(UbxMonParser.parse(UbxMonParser.frame(0x0A, 0x09, new byte[10]))).isEmpty();
        assertThat(UbxMonParser.parse(UbxMonParser.frame(0x0A, 0x38, new byte[4]))).isEmpty();
        assertThat(UbxMonParser.parse(UbxMonParser.frame(0x01, 0x07, new byte[92]))).isEmpty();
        assertThat(UbxMonParser.parse(UbxMonParser.frame(0x0A, 0x04, new byte[4]))).isEmpty();
        assertThat(UbxMonParser.parse(new byte[] {1, 2, 3})).isEmpty();
        assertThat(UbxMonParser.parse(null)).isEmpty();
        assertThat(UbxMonParser.frameLength(new byte[3], 0, 3)).isEqualTo(-1);
    }
}
