package ai.foresignal.collab;

import java.io.ByteArrayOutputStream;
import java.util.Set;

/**
 * Removes metadata from uploaded images without decoding them (prompt 130): EXIF (with its GPS position), XMP, IPTC
 * and comments. JPEG loses its {@code APP1} (EXIF, XMP), {@code APP13} (IPTC) and comment segments up to the start of
 * scan; PNG loses its {@code eXIf}, {@code tEXt}, {@code zTXt}, {@code iTXt} and {@code tIME} chunks. Pixels, color
 * profiles and every other segment are copied byte for byte. A file whose structure cannot be followed is refused
 * rather than stored with its metadata.
 */
public final class ExifStripper {

    private static final Set<String> PNG_DROP = Set.of("eXIf", "tEXt", "zTXt", "iTXt", "tIME");

    private ExifStripper() {}

    /**
     * The stripped image.
     *
     * @param bytes a PNG or JPEG
     * @param kind its kind
     * @return the bytes without metadata
     * @throws IllegalArgumentException when the structure is broken
     */
    public static byte[] strip(byte[] bytes, UploadTypes.Kind kind) {
        return switch (kind) {
            case JPEG -> jpeg(bytes);
            case PNG -> png(bytes);
            default -> bytes;
        };
    }

    /** Strips a JPEG's APP1, APP13 and COM segments before the start of scan. */
    static byte[] jpeg(byte[] in) {
        ByteArrayOutputStream out = new ByteArrayOutputStream(in.length);
        if (in.length < 4 || u(in, 0) != 0xFF || u(in, 1) != 0xD8) {
            throw new IllegalArgumentException("not a JPEG");
        }
        out.write(0xFF);
        out.write(0xD8);
        int i = 2;
        while (i < in.length) {
            if (u(in, i) != 0xFF) {
                throw new IllegalArgumentException("JPEG marker expected at " + i);
            }
            int marker = u(in, i + 1);
            if (marker == 0xFF) {
                i++;
                continue;
            }
            if (marker == 0xD9) {
                out.write(in, i, in.length - i);
                return out.toByteArray();
            }
            if (marker == 0x01 || (marker >= 0xD0 && marker <= 0xD7)) {
                out.write(in, i, 2);
                i += 2;
                continue;
            }
            if (i + 3 >= in.length) {
                throw new IllegalArgumentException("truncated JPEG segment");
            }
            int length = (u(in, i + 2) << 8) | u(in, i + 3);
            if (length < 2 || i + 2 + length > in.length) {
                throw new IllegalArgumentException("bad JPEG segment length");
            }
            if (marker == 0xDA) {
                out.write(in, i, in.length - i);
                return out.toByteArray();
            }
            boolean drop = marker == 0xE1 || marker == 0xED || marker == 0xFE;
            if (!drop) {
                out.write(in, i, 2 + length);
            }
            i += 2 + length;
        }
        return out.toByteArray();
    }

    /** Strips a PNG's text, time and EXIF chunks. */
    static byte[] png(byte[] in) {
        if (in.length < 8 || u(in, 0) != 0x89 || in[1] != 'P' || in[2] != 'N' || in[3] != 'G') {
            throw new IllegalArgumentException("not a PNG");
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream(in.length);
        out.write(in, 0, 8);
        int i = 8;
        while (i < in.length) {
            if (i + 8 > in.length) {
                throw new IllegalArgumentException("truncated PNG chunk");
            }
            long length = ((long) u(in, i) << 24) | (u(in, i + 1) << 16) | (u(in, i + 2) << 8) | u(in, i + 3);
            String type = new String(in, i + 4, 4, java.nio.charset.StandardCharsets.ISO_8859_1);
            long total = 12 + length;
            if (length < 0 || i + total > in.length) {
                throw new IllegalArgumentException("bad PNG chunk length");
            }
            if (!PNG_DROP.contains(type)) {
                out.write(in, i, (int) total);
            }
            i += (int) total;
            if ("IEND".equals(type)) {
                break;
            }
        }
        return out.toByteArray();
    }

    private static int u(byte[] b, int i) {
        return i < b.length ? b[i] & 0xff : -1;
    }
}
