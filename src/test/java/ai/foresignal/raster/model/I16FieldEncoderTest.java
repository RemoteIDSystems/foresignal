package ai.foresignal.raster.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The {@code .i16} field encoding round trips within one quantization step (prompt 108). */
class I16FieldEncoderTest {

    private static byte[] planar(float[] u, float[] v) {
        ByteBuffer buffer = ByteBuffer.allocate((u.length + v.length) * 4).order(ByteOrder.LITTLE_ENDIAN);
        buffer.asFloatBuffer().put(u).put(v);
        return buffer.array();
    }

    @Test
    @DisplayName("values round trip within one code step, per component")
    void roundTrip() {
        float[] u = {0f, 10f, -5f, 3f};
        float[] v = {100f, -100f, 200f, -200f};
        I16FieldEncoder.Result result = I16FieldEncoder.encode(planar(u, v), 2, 2);
        ByteBuffer in = ByteBuffer.wrap(result.bytes()).order(ByteOrder.LITTLE_ENDIAN);
        int cells = 4;
        for (int k = 0; k < cells; k++) {
            double decodedU = in.getShort(k * 2) * result.uScale() + result.uOffset();
            assertThat(decodedU).isCloseTo(u[k], org.assertj.core.data.Offset.offset(result.uScale()));
        }
        for (int k = 0; k < cells; k++) {
            double decodedV = in.getShort((cells + k) * 2) * result.vScale() + result.vOffset();
            assertThat(decodedV).isCloseTo(v[k], org.assertj.core.data.Offset.offset(result.vScale()));
        }
    }

    @Test
    @DisplayName("non-finite samples become the no-data code")
    void noData() {
        float[] u = {Float.NaN, 1f, 2f, 3f};
        float[] v = {1f, 2f, 3f, 4f};
        I16FieldEncoder.Result result = I16FieldEncoder.encode(planar(u, v), 2, 2);
        ByteBuffer in = ByteBuffer.wrap(result.bytes()).order(ByteOrder.LITTLE_ENDIAN);
        assertThat(in.getShort(0)).isEqualTo((short) I16FieldEncoder.NO_DATA);
    }
}
