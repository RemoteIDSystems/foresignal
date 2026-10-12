package ai.foresignal.raster;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.RasterDtype;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Random;
import java.util.zip.CRC32C;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * The {@code .fsg} codec (prompt 042, Weather-Imagery-Design §2.3).
 *
 * <p>{@code src/test/resources/raster/golden-u8.fsg} was written independently of this codebase
 * (Python {@code struct}, a bitwise CRC32C checked against the standard "123456789" vector): a 4 × 2
 * u8 frame, codec none, scale 0.5, offset −33, nodata 0, payload {@code 00 01 02 42 64 C8 FF 0A}. The
 * encoder must reproduce it byte for byte.
 */
class ElgCodecTest {

    static final byte[] GOLDEN_PAYLOAD = {0, 1, 2, 66, 100, (byte) 200, (byte) 255, 10};
    static final FsgHeader GOLDEN_HEADER =
            new FsgHeader(RasterDtype.U8, FsgHeader.CODEC_NONE, 4, 2, 1, false, 0.5, -33.0, 0.0);

    static byte[] golden() throws IOException {
        try (InputStream in = ElgCodecTest.class.getResourceAsStream("/raster/golden-u8.fsg")) {
            assertThat(in).as("golden file on the classpath").isNotNull();
            return in.readAllBytes();
        }
    }

    @Test
    @DisplayName("the encoder reproduces the golden file byte for byte, header included")
    void goldenFile() throws IOException {
        byte[] golden = golden();
        byte[] encoded = ElgCodec.encode(GOLDEN_HEADER, GOLDEN_PAYLOAD);
        assertThat(encoded).isEqualTo(golden);
        assertThat(Arrays.copyOf(encoded, ElgCodec.HEADER_BYTES))
                .isEqualTo(ElgCodec.headerBytes(GOLDEN_HEADER, GOLDEN_PAYLOAD));
        ByteBuffer header = ByteBuffer.wrap(golden).order(ByteOrder.LITTLE_ENDIAN);
        assertThat(new String(golden, 0, 4, java.nio.charset.StandardCharsets.US_ASCII)).isEqualTo("FSRG");
        assertThat(header.getShort(4)).isEqualTo((short) 1);
        assertThat(golden[6]).isEqualTo((byte) 1);
        assertThat(golden[7]).isEqualTo((byte) 0);
        assertThat(header.getInt(8)).isEqualTo(4);
        assertThat(header.getInt(12)).isEqualTo(2);
        assertThat(header.getShort(16)).isEqualTo((short) 1);
        assertThat(header.getShort(18)).isZero();
        assertThat(header.getInt(20)).isZero();
        assertThat(header.getDouble(24)).isEqualTo(0.5);
        assertThat(header.getDouble(32)).isEqualTo(-33.0);
        assertThat(header.getDouble(40)).isEqualTo(0.0);
        assertThat(header.getLong(48)).isEqualTo(8);
        CRC32C crc = new CRC32C();
        crc.update(GOLDEN_PAYLOAD);
        assertThat(Integer.toUnsignedLong(header.getInt(56))).isEqualTo(crc.getValue());
        assertThat(header.getInt(60)).isZero();

        ElgCodec.Decoded decoded = ElgCodec.decode(golden);
        assertThat(decoded.header()).isEqualTo(GOLDEN_HEADER);
        assertThat(decoded.payload()).isEqualTo(GOLDEN_PAYLOAD);
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(RasterDtype.class)
    @DisplayName("every dtype round-trips with both codecs")
    void roundTripPerDtype(RasterDtype dtype) {
        Random random = new Random(42 + dtype.code());
        for (int codec : new int[] {FsgHeader.CODEC_NONE, FsgHeader.CODEC_DEFLATE}) {
            FsgHeader header = new FsgHeader(dtype, codec, 7, 5, 2, false, 0.25, 10.0, Double.NaN);
            byte[] payload = new byte[(int) header.decodedLength()];
            random.nextBytes(payload);
            byte[] file = ElgCodec.encode(header, payload);
            ElgCodec.Decoded decoded = ElgCodec.decode(file);
            assertThat(decoded.payload()).isEqualTo(payload);
            assertThat(decoded.header().dtype()).isEqualTo(dtype);
            assertThat(decoded.header().codec()).isEqualTo(codec);
            assertThat(decoded.header().bands()).isEqualTo(2);
            assertThat(Double.isNaN(decoded.header().nodata())).isTrue();
        }
    }

    @Test
    @DisplayName("deflate (codec 1) shrinks a compressible payload and still decodes")
    void deflateCompresses() {
        FsgHeader header = new FsgHeader(RasterDtype.U16, FsgHeader.CODEC_DEFLATE, 100, 100, 1, false, 1, 0, 0);
        byte[] payload = new byte[(int) header.decodedLength()];
        byte[] file = ElgCodec.encode(header, payload);
        assertThat(file.length).isLessThan(payload.length / 10);
        assertThat(ElgCodec.decode(file).payload()).isEqualTo(payload);
    }

    @Test
    @DisplayName("a CRC mismatch is rejected")
    void crcMismatch() throws IOException {
        byte[] file = golden();
        file[ElgCodec.HEADER_BYTES + 3] ^= 0x01;
        assertThatThrownBy(() -> ElgCodec.decode(file)).isInstanceOf(RasterFormatException.class)
                .hasMessageContaining("CRC32C");
    }

    @Test
    @DisplayName("a bad magic is rejected")
    void badMagic() throws IOException {
        byte[] file = golden();
        file[0] = 'E';
        assertThatThrownBy(() -> ElgCodec.decode(file)).isInstanceOf(RasterFormatException.class)
                .hasMessageContaining("magic");
    }

    @Test
    @DisplayName("non-zero reserved bytes at 20 or 60 are rejected")
    void reservedBytes() throws IOException {
        byte[] first = golden();
        first[21] = 1;
        assertThatThrownBy(() -> ElgCodec.decode(first)).isInstanceOf(RasterFormatException.class)
                .hasMessageContaining("Reserved");
        byte[] second = golden();
        second[63] = 1;
        assertThatThrownBy(() -> ElgCodec.decode(second)).isInstanceOf(RasterFormatException.class)
                .hasMessageContaining("Reserved");
    }

    @Test
    @DisplayName("an unknown version, dtype or flag bit is rejected")
    void unknownHeaderValues() throws IOException {
        byte[] version = golden();
        version[4] = 2;
        assertThatThrownBy(() -> ElgCodec.decode(version)).hasMessageContaining("version");
        byte[] dtype = golden();
        dtype[6] = 9;
        assertThatThrownBy(() -> ElgCodec.decode(dtype)).isInstanceOf(RasterFormatException.class);
        byte[] flags = golden();
        flags[18] = 2;
        assertThatThrownBy(() -> ElgCodec.decode(flags)).hasMessageContaining("flags");
    }

    @Test
    @DisplayName("the partial flag (bit 0) round-trips")
    void partialFlag() {
        FsgHeader header = new FsgHeader(RasterDtype.U8, FsgHeader.CODEC_NONE, 4, 2, 1, true, 0.5, -33.0, 0.0);
        byte[] file = ElgCodec.encode(header, GOLDEN_PAYLOAD);
        assertThat(file[18]).isEqualTo((byte) 1);
        assertThat(ElgCodec.decode(file).header().partial()).isTrue();
    }

    @Test
    @DisplayName("a truncated or overlong payload, or a short header, is rejected")
    void lengths() throws IOException {
        byte[] golden = golden();
        assertThatThrownBy(() -> ElgCodec.decode(Arrays.copyOf(golden, golden.length - 1)))
                .isInstanceOf(RasterFormatException.class);
        assertThatThrownBy(() -> ElgCodec.decode(Arrays.copyOf(golden, golden.length + 1)))
                .isInstanceOf(RasterFormatException.class);
        assertThatThrownBy(() -> ElgCodec.decode(Arrays.copyOf(golden, 20))).isInstanceOf(RasterFormatException.class);
        assertThatThrownBy(() -> ElgCodec.encode(GOLDEN_HEADER, new byte[7])).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("a deflate payload that inflates to the wrong length is rejected")
    void wrongInflatedLength() {
        byte[] stored = ElgCodec.deflate(new byte[10]);
        FsgHeader header = new FsgHeader(RasterDtype.U8, FsgHeader.CODEC_DEFLATE, 4, 2, 1, false, 1, 0, 0);
        byte[] head = ElgCodec.headerBytes(header, stored);
        byte[] file = new byte[head.length + stored.length];
        System.arraycopy(head, 0, file, 0, head.length);
        System.arraycopy(stored, 0, file, head.length, stored.length);
        assertThatThrownBy(() -> ElgCodec.decode(file)).isInstanceOf(RasterFormatException.class)
                .hasMessageContaining("inflates");
    }
}
