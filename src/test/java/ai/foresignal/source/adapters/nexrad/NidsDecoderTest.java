package ai.foresignal.source.adapters.nexrad;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The NIDS product-description reader (prompt 106). */
class NidsDecoderTest {

    private static byte[] nids(int productCode, double lat, double lon) {
        byte[] text = "SDUS54 KFWD 152300\r\r\nN0BFWS\r\r\n".getBytes(StandardCharsets.US_ASCII);
        ByteBuffer b = ByteBuffer.allocate(text.length + 66).order(ByteOrder.BIG_ENDIAN);
        b.put(text);
        b.putShort((short) -1);
        b.putInt((int) Math.round(lat * 1000));
        b.putInt((int) Math.round(lon * 1000));
        b.putShort((short) 776);
        b.putShort((short) productCode);
        b.putShort((short) 2);
        b.putShort((short) 212);
        b.putShort((short) 4785);
        b.putShort((short) 30);
        b.putShort((short) 20742);
        b.putInt(82842);
        b.put(new byte[20]);
        b.putShort((short) 0);
        b.putShort((short) 0);
        b.putInt(0);
        b.putInt(0);
        b.putInt(0);
        return b.array();
    }

    @Test
    @DisplayName("skips the WMO/AWIPS text and reads the product code and radar position")
    void describes() {
        NidsDecoder.Description d = NidsDecoder.describe(nids(153, 32.573, -97.431));
        assertThat(d).isNotNull();
        assertThat(d.productCode()).isEqualTo(153);
        assertThat(d.lat()).isCloseTo(32.573, org.assertj.core.data.Offset.offset(1e-6));
        assertThat(d.lon()).isCloseTo(-97.431, org.assertj.core.data.Offset.offset(1e-6));
    }

    @Test
    @DisplayName("reads N0G's product code too")
    void n0g() {
        assertThat(NidsDecoder.describe(nids(154, 32.0, -97.0)).productCode()).isEqualTo(154);
    }

    @Test
    @DisplayName("a non-NIDS file is refused")
    void refuses() {
        assertThat(NidsDecoder.describe(new byte[] {1, 2, 3})).isNull();
    }
}
