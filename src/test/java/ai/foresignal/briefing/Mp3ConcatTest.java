package ai.foresignal.briefing;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link Mp3Concat} (prompt 141): MP3 parts joined with the tags between them removed. */
class Mp3ConcatTest {

    static final byte[] FRAMES_A = {(byte) 0xFF, (byte) 0xFB, 1, 2, 3};
    static final byte[] FRAMES_B = {(byte) 0xFF, (byte) 0xFB, 4, 5, 6};

    /** An ID3v2 tag of {@code size} body bytes (syncsafe), optionally with a footer. */
    static byte[] id3v2(int size, boolean footer) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.writeBytes(new byte[] {'I', 'D', '3', 4, 0, (byte) (footer ? 0x10 : 0),
                (byte) ((size >> 21) & 0x7F), (byte) ((size >> 14) & 0x7F), (byte) ((size >> 7) & 0x7F),
                (byte) (size & 0x7F)});
        out.writeBytes(new byte[size]);
        if (footer) {
            out.writeBytes(new byte[] {'3', 'D', 'I', 4, 0, 0x10, 0, 0, 0, 0});
        }
        return out.toByteArray();
    }

    static byte[] id3v1() {
        byte[] tag = new byte[128];
        tag[0] = 'T';
        tag[1] = 'A';
        tag[2] = 'G';
        return tag;
    }

    static byte[] join(byte[]... parts) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Arrays.stream(parts).forEach(out::writeBytes);
        return out.toByteArray();
    }

    @Test
    @DisplayName("the first part keeps its ID3v2 tag; later parts lose theirs; the last keeps its ID3v1 tag")
    void stripsTagsBetweenParts() {
        byte[] first = join(id3v2(20, false), FRAMES_A, id3v1());
        byte[] second = join(id3v2(300, true), FRAMES_B, id3v1());

        byte[] joined = Mp3Concat.concat(List.of(first, second));

        assertThat(joined).isEqualTo(join(id3v2(20, false), FRAMES_A, FRAMES_B, id3v1()));
    }

    @Test
    @DisplayName("parts without tags are concatenated byte for byte; one part is returned as is; none is empty")
    void plain() {
        assertThat(Mp3Concat.concat(List.of(FRAMES_A, FRAMES_B))).isEqualTo(join(FRAMES_A, FRAMES_B));
        assertThat(Mp3Concat.concat(List.of(FRAMES_A))).isSameAs(FRAMES_A);
        assertThat(Mp3Concat.concat(List.of())).isEmpty();
    }

    @Test
    @DisplayName("tag lengths: syncsafe size plus header and footer; a malformed or oversized tag counts as none")
    void tagLengths() {
        assertThat(Mp3Concat.id3v2Length(id3v2(0, false))).isEqualTo(10);
        assertThat(Mp3Concat.id3v2Length(id3v2(200, true))).isEqualTo(220);
        byte[] bad = id3v2(10, false);
        bad[9] = (byte) 0x80;
        assertThat(Mp3Concat.id3v2Length(bad)).isZero();
        byte[] truncated = Arrays.copyOf(id3v2(500, false), 50);
        assertThat(Mp3Concat.id3v2Length(truncated)).isZero();
        assertThat(Mp3Concat.id3v2Length(FRAMES_A)).isZero();
        assertThat(Mp3Concat.id3v1Length(join(FRAMES_A, id3v1()))).isEqualTo(128);
        assertThat(Mp3Concat.id3v1Length(FRAMES_A)).isZero();
    }
}
