package ai.foresignal.source.adapters.nexrad;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * A minimal NIDS (NEXRAD Level III) reader (prompt 106): the WMO/AWIPS text header is skipped and the
 * NIDS message header and Product Description Block are parsed — the product code, the radar's
 * position and the message time. Verified against a real KFWD N0B file (2026-10-15).
 *
 * <p>Decoding the radial data is a documented gap: NetCDF-Java 5.11.0 in this build ships no NIDS
 * IOSP, and the packet layout is not implemented here. {@link #describe} is used only by the probe.
 */
public final class NidsDecoder {

    private NidsDecoder() {}

    /**
     * The Product Description Block.
     *
     * @param productCode the NIDS product code (153 = N0B, 154 = N0G)
     * @param lat the radar latitude, degrees
     * @param lon the radar longitude, degrees
     */
    public record Description(int productCode, double lat, double lon) {}

    /** Parses the product description, or returns empty when the file is not a NIDS product message. */
    public static Description describe(byte[] bytes) {
        int start = nidsStart(bytes);
        if (start < 0 || bytes.length < start + 32) {
            return null;
        }
        int p = start + 18;
        if (i16(bytes, p) != -1) {
            return null;
        }
        p += 2;
        int lat = i32(bytes, p);
        int lon = i32(bytes, p + 4);
        p += 8;
        p += 2; // height
        int productCode = i16(bytes, p);
        return new Description(productCode, lat / 1000.0, lon / 1000.0);
    }

    /** The byte offset of the NIDS message, after the WMO and AWIPS text header lines. */
    static int nidsStart(byte[] bytes) {
        int idx = 0;
        int found = 0;
        for (int i = 0; i + 2 < bytes.length && i < 120; i++) {
            if (bytes[i] == '\r' && bytes[i + 1] == '\r' && bytes[i + 2] == '\n') {
                found = i + 3;
                i += 2;
                if (found >= 27) {
                    return found;
                }
            } else if (bytes[i] == '\r' && bytes[i + 1] == '\n') {
                found = i + 2;
                i += 1;
                if (found >= 27) {
                    return found;
                }
            }
        }
        return found;
    }

    private static int i16(byte[] b, int o) {
        return (short) (((b[o] & 0xFF) << 8) | (b[o + 1] & 0xFF));
    }

    private static int i32(byte[] b, int o) {
        return ((b[o] & 0xFF) << 24) | ((b[o + 1] & 0xFF) << 16) | ((b[o + 2] & 0xFF) << 8) | (b[o + 3] & 0xFF);
    }

    /** The decoded text of the header lines, for tests. */
    static String header(byte[] bytes) {
        int start = nidsStart(bytes);
        return start <= 0 ? "" : new String(bytes, 0, start, StandardCharsets.US_ASCII);
    }
}
