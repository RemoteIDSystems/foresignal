package ai.foresignal.briefing;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Joins MP3 files made from consecutive chunks of one text (prompt 141). MPEG audio frames are self-contained,
 * so the files are concatenated byte for byte, except that metadata tags between the parts are removed: the
 * ID3v2 tag at the start of every part after the first, and the 128-byte ID3v1 tag at the end of every part
 * before the last. Players then see one continuous stream.
 */
public final class Mp3Concat {

    /** The size of an ID3v2 header. */
    static final int ID3V2_HEADER = 10;

    /** The size of an ID3v1 tag. */
    static final int ID3V1_SIZE = 128;

    private Mp3Concat() {}

    /** The parts joined into one MP3; a single part is returned unchanged. */
    public static byte[] concat(List<byte[]> parts) {
        if (parts.isEmpty()) {
            return new byte[0];
        }
        if (parts.size() == 1) {
            return parts.getFirst();
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int i = 0; i < parts.size(); i++) {
            byte[] part = parts.get(i);
            int from = i == 0 ? 0 : id3v2Length(part);
            int to = i == parts.size() - 1 ? part.length : part.length - id3v1Length(part);
            if (to > from) {
                out.write(part, from, to - from);
            }
        }
        return out.toByteArray();
    }

    /**
     * The length of the ID3v2 tag at the start of {@code mp3} (header, syncsafe size, and the footer when flagged),
     * or 0 when there is none or it does not fit.
     */
    static int id3v2Length(byte[] mp3) {
        if (mp3.length < ID3V2_HEADER || mp3[0] != 'I' || mp3[1] != 'D' || mp3[2] != '3') {
            return 0;
        }
        int size = 0;
        for (int i = 6; i < 10; i++) {
            int b = mp3[i] & 0xFF;
            if ((b & 0x80) != 0) {
                return 0; // not a syncsafe integer: not a valid tag
            }
            size = (size << 7) | b;
        }
        boolean footer = (mp3[5] & 0x10) != 0;
        long total = (long) ID3V2_HEADER + size + (footer ? ID3V2_HEADER : 0);
        return total > mp3.length ? 0 : (int) total;
    }

    /** {@value #ID3V1_SIZE} when {@code mp3} ends with an ID3v1 tag ({@code TAG}), else 0. */
    static int id3v1Length(byte[] mp3) {
        int at = mp3.length - ID3V1_SIZE;
        if (at < 0 || mp3[at] != 'T' || mp3[at + 1] != 'A' || mp3[at + 2] != 'G') {
            return 0;
        }
        return ID3V1_SIZE;
    }
}
