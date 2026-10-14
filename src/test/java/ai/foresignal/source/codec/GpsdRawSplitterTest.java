package ai.foresignal.source.codec;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link GpsdRawSplitter} (prompt 086): JSON lines and UBX frames interleaved in gpsd's {@code "raw":2} stream, a UBX
 * payload containing newlines, frames split across chunks, NMEA and garbage skipped, bad checksums dropped.
 */
class GpsdRawSplitterTest {

    @Test
    @DisplayName("JSON lines and UBX frames come out in order, whole, across chunk boundaries")
    void interleaved() {
        byte[] ubx = UbxMonParserTest.monHw(2, 10);
        ubx[10] = '\n';
        ubx = UbxMonParser.frame(0x0A, 0x09, java.util.Arrays.copyOfRange(ubx, 6, ubx.length - 2));
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.writeBytes("{\"class\":\"TPV\",\"mode\":3}\r\n".getBytes(StandardCharsets.US_ASCII));
        stream.writeBytes("$GPGGA,1,2,3*00\r\n".getBytes(StandardCharsets.US_ASCII));
        stream.writeBytes(ubx);
        stream.writeBytes("{\"class\":\"SKY\"}\n".getBytes(StandardCharsets.US_ASCII));
        byte[] all = stream.toByteArray();

        GpsdRawSplitter splitter = new GpsdRawSplitter();
        List<GpsdRawSplitter.Frame> frames = new ArrayList<>();
        for (int i = 0; i < all.length; i += 7) {
            frames.addAll(splitter.feed(java.util.Arrays.copyOfRange(all, i, Math.min(all.length, i + 7))));
        }
        assertThat(frames).hasSize(3);
        assertThat(frames.get(0)).isInstanceOf(GpsdRawSplitter.JsonLine.class);
        assertThat(new String(((GpsdRawSplitter.JsonLine) frames.get(0)).bytes(), StandardCharsets.US_ASCII))
                .isEqualTo("{\"class\":\"TPV\",\"mode\":3}");
        assertThat(frames.get(1)).isInstanceOf(GpsdRawSplitter.Ubx.class);
        assertThat(((GpsdRawSplitter.Ubx) frames.get(1)).bytes()).isEqualTo(ubx);
        assertThat(frames.get(2)).isInstanceOf(GpsdRawSplitter.JsonLine.class);
        assertThat(splitter.skipped()).isPositive();
    }

    @Test
    @DisplayName("a UBX frame with a bad checksum is dropped and the stream recovers")
    void badChecksum() {
        byte[] ubx = UbxMonParserTest.monRf(3, 200);
        ubx[ubx.length - 2] ^= 0x7F;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.writeBytes(ubx);
        stream.writeBytes("{\"class\":\"TPV\"}\n".getBytes(StandardCharsets.US_ASCII));
        GpsdRawSplitter splitter = new GpsdRawSplitter();
        List<GpsdRawSplitter.Frame> frames = splitter.feed(stream.toByteArray());
        assertThat(frames).singleElement().isInstanceOf(GpsdRawSplitter.JsonLine.class);
        assertThat(splitter.skipped()).isPositive();
        assertThat(splitter.feed(null)).isEmpty();
    }
}
