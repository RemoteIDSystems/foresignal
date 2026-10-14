package ai.foresignal.source.formats.kiss;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link KissFrameDecoder} (prompt 082): escapes, split frames, the port nibble and non-data commands. */
class KissFrameDecoderTest {

    private static byte[] bytes(int... values) {
        byte[] out = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            out[i] = (byte) values[i];
        }
        return out;
    }

    @Test
    @DisplayName("FESC TFEND and FESC TFESC unescape to 0xC0 and 0xDB")
    void escapes() {
        List<KissFrameDecoder.Frame> frames = new KissFrameDecoder().feed(
                bytes(0xC0, 0x00, 0x41, 0xDB, 0xDC, 0x42, 0xDB, 0xDD, 0x43, 0xC0));

        assertThat(frames).singleElement().satisfies(f -> {
            assertThat(f.port()).isZero();
            assertThat(f.data()).containsExactly(bytes(0x41, 0xC0, 0x42, 0xDB, 0x43));
        });
    }

    @Test
    @DisplayName("a frame split over several reads, byte by byte, decodes the same as in one read")
    void splitFrames() {
        byte[] stream = new byte[0];
        stream = concat(stream, KissFrameDecoder.encode(0, "hello".getBytes()));
        stream = concat(stream, KissFrameDecoder.encode(2, bytes(0xC0, 0xDB, 0x01)));
        KissFrameDecoder decoder = new KissFrameDecoder();
        List<KissFrameDecoder.Frame> frames = new ArrayList<>();
        for (byte b : stream) {
            frames.addAll(decoder.feed(new byte[] {b}));
        }

        assertThat(frames).hasSize(2);
        assertThat(frames.get(0).data()).containsExactly("hello".getBytes());
        assertThat(frames.get(1).port()).isEqualTo(2);
        assertThat(frames.get(1).data()).containsExactly(bytes(0xC0, 0xDB, 0x01));
        assertThat(new KissFrameDecoder().feed(stream)).hasSize(2);
    }

    @Test
    @DisplayName("a non-zero command (TXDELAY, persistence, SETHARDWARE) is ignored and counted")
    void nonDataIgnored() {
        KissFrameDecoder decoder = new KissFrameDecoder();
        List<KissFrameDecoder.Frame> frames = decoder.feed(bytes(0xC0, 0x01, 0x32, 0xC0, 0xC0, 0x16, 0x05, 0xC0,
                0xC0, 0x06, 0x01, 0x02, 0xC0));

        assertThat(frames).isEmpty();
        assertThat(decoder.nonData()).isEqualTo(3);
    }

    @Test
    @DisplayName("empty frames, bytes before the first FEND and a bad escape are dropped")
    void junk() {
        KissFrameDecoder decoder = new KissFrameDecoder();
        List<KissFrameDecoder.Frame> frames = decoder.feed(bytes(0x55, 0x66, 0xC0, 0xC0, 0xC0, 0x00, 0xDB, 0x01, 0x02,
                0xC0, 0x00, 0x7A, 0xC0));

        assertThat(frames).singleElement().satisfies(f -> assertThat(f.data()).containsExactly(bytes(0x7A)));
        assertThat(decoder.malformed()).isEqualTo(1);
    }

    @Test
    @DisplayName("a frame over the size cap is dropped whole and counted; the next frame decodes")
    void oversize() {
        byte[] big = new byte[KissFrameDecoder.MAX_FRAME_BYTES + 10];
        Arrays.fill(big, (byte) 0x41);
        KissFrameDecoder decoder = new KissFrameDecoder();
        List<KissFrameDecoder.Frame> frames = decoder.feed(concat(KissFrameDecoder.encode(0, big),
                KissFrameDecoder.encode(0, bytes(0x42))));

        assertThat(frames).singleElement().satisfies(f -> assertThat(f.data()).containsExactly(bytes(0x42)));
        assertThat(decoder.oversize()).isEqualTo(1);
    }

    @Test
    @DisplayName("encode escapes FEND and FESC and sets the port nibble")
    void encode() {
        assertThat(KissFrameDecoder.encode(1, bytes(0xC0, 0xDB))).containsExactly(bytes(0xC0, 0x10, 0xDB, 0xDC, 0xDB,
                0xDD, 0xC0));
    }

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] out = Arrays.copyOf(a, a.length + b.length);
        System.arraycopy(b, 0, out, a.length, b.length);
        return out;
    }
}
