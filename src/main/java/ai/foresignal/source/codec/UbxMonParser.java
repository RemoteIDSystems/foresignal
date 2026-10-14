package ai.foresignal.source.codec;

import java.util.Optional;

/**
 * Decodes the two u-blox monitor messages the GNSS integrity sentinel reads (prompt 086), from frames gpsd passes
 * through verbatim with {@code "raw":2}. Read-only: nothing here builds or sends a UBX message.
 *
 * <p>A UBX frame is {@code 0xB5 0x62}, class, id, a little-endian payload length, the payload and a two-byte Fletcher
 * checksum over class, id, length and payload (u-blox interface description, "UBX frame structure").
 *
 * <ul>
 *   <li><strong>UBX-MON-HW</strong> (class {@code 0x0A}, id {@code 0x09}, 60 bytes): {@code flags} at offset 22,
 *       bits 2–3 {@code jammingState} (0 unknown, 1 OK, 2 warning, 3 critical); {@code jamInd} at offset 45, the CW
 *       jamming indicator 0–255.
 *   <li><strong>UBX-MON-RF</strong> (class {@code 0x0A}, id {@code 0x38}): {@code nBlocks} at offset 1, then 24-byte
 *       blocks from offset 4, each with {@code flags} at +1 (bits 0–1 {@code jammingState}) and {@code jamInd} at +16.
 *       The worst block is reported.
 * </ul>
 * Any other message, a bad checksum or a short payload is not a monitor reading.
 */
public final class UbxMonParser {

    /** The UBX sync characters. */
    public static final int SYNC1 = 0xB5;

    /** The second sync character. */
    public static final int SYNC2 = 0x62;

    /** The MON class. */
    public static final int CLASS_MON = 0x0A;

    /** MON-HW. */
    public static final int ID_MON_HW = 0x09;

    /** MON-RF. */
    public static final int ID_MON_RF = 0x38;

    /** {@code jammingState} "warning". */
    public static final int JAMMING_WARNING = 2;

    /** {@code jammingState} "critical". */
    public static final int JAMMING_CRITICAL = 3;

    /** The MON-HW payload length. */
    static final int MON_HW_LENGTH = 60;

    /** The MON-RF block length. */
    static final int MON_RF_BLOCK = 24;

    /**
     * One monitor reading.
     *
     * @param message {@code MON-HW} or {@code MON-RF}
     * @param jamIndicator the CW jamming indicator, 0–255, or null
     * @param jammingState 0 unknown, 1 OK, 2 warning, 3 critical, or null
     */
    public record Reading(String message, Integer jamIndicator, Integer jammingState) {

        /** True when the receiver itself reports jamming (warning or critical). */
        public boolean jammed() {
            return jammingState != null && jammingState >= JAMMING_WARNING;
        }
    }

    private UbxMonParser() {}

    /** The total length of the UBX frame at the start of {@code bytes} (from its header), or -1 when too short. */
    public static int frameLength(byte[] bytes, int offset, int available) {
        if (available < 6) {
            return -1;
        }
        int length = (bytes[offset + 4] & 0xFF) | ((bytes[offset + 5] & 0xFF) << 8);
        return 8 + length;
    }

    /** True when {@code frame} is one complete UBX frame with a valid checksum. */
    public static boolean valid(byte[] frame) {
        if (frame == null || frame.length < 8 || (frame[0] & 0xFF) != SYNC1 || (frame[1] & 0xFF) != SYNC2) {
            return false;
        }
        int length = (frame[4] & 0xFF) | ((frame[5] & 0xFF) << 8);
        if (frame.length != 8 + length) {
            return false;
        }
        int a = 0;
        int b = 0;
        for (int i = 2; i < 6 + length; i++) {
            a = (a + (frame[i] & 0xFF)) & 0xFF;
            b = (b + a) & 0xFF;
        }
        return (frame[6 + length] & 0xFF) == a && (frame[7 + length] & 0xFF) == b;
    }

    /** The monitor reading of {@code frame}, or empty for anything else. */
    public static Optional<Reading> parse(byte[] frame) {
        if (!valid(frame)) {
            return Optional.empty();
        }
        int cls = frame[2] & 0xFF;
        int id = frame[3] & 0xFF;
        int length = (frame[4] & 0xFF) | ((frame[5] & 0xFF) << 8);
        if (cls != CLASS_MON) {
            return Optional.empty();
        }
        if (id == ID_MON_HW) {
            if (length < MON_HW_LENGTH) {
                return Optional.empty();
            }
            int flags = frame[6 + 22] & 0xFF;
            int jamInd = frame[6 + 45] & 0xFF;
            return Optional.of(new Reading("MON-HW", jamInd, (flags >> 2) & 0x03));
        }
        if (id == ID_MON_RF) {
            if (length < 4) {
                return Optional.empty();
            }
            int blocks = frame[6 + 1] & 0xFF;
            if (blocks == 0 || length < 4 + blocks * MON_RF_BLOCK) {
                return Optional.empty();
            }
            int worstState = 0;
            int worstJam = 0;
            for (int i = 0; i < blocks; i++) {
                int base = 6 + 4 + i * MON_RF_BLOCK;
                worstState = Math.max(worstState, frame[base + 1] & 0x03);
                worstJam = Math.max(worstJam, frame[base + 16] & 0xFF);
            }
            return Optional.of(new Reading("MON-RF", worstJam, worstState));
        }
        return Optional.empty();
    }

    /**
     * Builds a UBX frame around {@code payload} with its checksum, for tests and fixtures only (Foresignal never sends
     * one).
     */
    public static byte[] frame(int cls, int id, byte[] payload) {
        byte[] out = new byte[8 + payload.length];
        out[0] = (byte) SYNC1;
        out[1] = (byte) SYNC2;
        out[2] = (byte) cls;
        out[3] = (byte) id;
        out[4] = (byte) (payload.length & 0xFF);
        out[5] = (byte) ((payload.length >> 8) & 0xFF);
        System.arraycopy(payload, 0, out, 6, payload.length);
        int a = 0;
        int b = 0;
        for (int i = 2; i < 6 + payload.length; i++) {
            a = (a + (out[i] & 0xFF)) & 0xFF;
            b = (b + a) & 0xFF;
        }
        out[6 + payload.length] = (byte) a;
        out[7 + payload.length] = (byte) b;
        return out;
    }
}
