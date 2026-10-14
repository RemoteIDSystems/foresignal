package ai.foresignal.source.formats.kiss;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Decodes an AX.25 UI frame (AX.25 2.2 §3 and §6) into TNC2 monitor text {@code SRC>DEST,DIGI1*,DIGI2:info}, the form
 * APRS-IS carries and the APRS parser reads (prompt 082). Pure.
 *
 * <ul>
 *   <li><strong>Addresses:</strong> destination, source, then up to 8 digipeaters, 7 bytes each: six characters
 *       shifted left one bit (upper-case letters, digits and space padding), then the SSID byte (SSID in bits 1 to 4,
 *       the H bit 0x80 meaning "has been repeated" on a digipeater, and the extension bit 0x01 marking the last
 *       address). The digipeater path marks the last repeated digipeater with {@code *}, as TNC2 does.
 *   <li><strong>UI only:</strong> the control byte must be 0x03 (UI, P/F clear) and the PID 0xF0 (no layer 3).
 *       Anything else is a valid AX.25 frame that is not APRS ({@link Result.NotUi}).
 *   <li><strong>Not AX.25:</strong> a frame whose address field does not decode (a character outside the shifted
 *       set, no extension bit within 10 addresses, or too short) is {@link Result.NotAx25}: for example a MeshCore
 *       KISS modem, which carries raw MeshCore packets rather than AX.25.
 * </ul>
 * The FCS is not present: a KISS TNC strips it after checking it.
 */
public final class Ax25UiDecoder {

    /** The UI control byte. */
    public static final int CONTROL_UI = 0x03;

    /** The "no layer 3" protocol identifier, which APRS uses. */
    public static final int PID_NO_LAYER3 = 0xF0;

    /** The most digipeater addresses AX.25 allows. */
    public static final int MAX_DIGIPEATERS = 8;

    private static final int ADDRESS_BYTES = 7;

    private Ax25UiDecoder() {}

    /** The outcome of decoding one frame. */
    public sealed interface Result permits Result.Ui, Result.NotUi, Result.NotAx25 {

        /**
         * A UI frame.
         *
         * @param source the source address with SSID ({@code N0CALL-9}; {@code -0} is left out)
         * @param destination the destination address with SSID
         * @param digipeaters the path, the last repeated one marked {@code *}
         * @param info the information field
         */
        record Ui(String source, String destination, List<String> digipeaters, byte[] info) implements Result {

            /** Copies the lists. */
            public Ui {
                digipeaters = List.copyOf(digipeaters);
                info = info.clone();
            }

            /** The information field, copied. */
            @Override
            public byte[] info() {
                return info.clone();
            }

            /** The information field as text (UTF-8, malformed bytes replaced). */
            public String infoText() {
                return new String(info, StandardCharsets.UTF_8);
            }

            /** The frame as one TNC2 monitor line. */
            public String tnc2() {
                StringBuilder line = new StringBuilder(source).append('>').append(destination);
                for (String digi : digipeaters) {
                    line.append(',').append(digi);
                }
                return line.append(':').append(infoText()).toString();
            }
        }

        /**
         * A valid AX.25 frame that is not an APRS UI frame.
         *
         * @param control the control byte
         * @param pid the protocol identifier, or -1 when the frame has none
         */
        record NotUi(int control, int pid) implements Result {}

        /**
         * Not an AX.25 frame at all.
         *
         * @param why what did not decode
         */
        record NotAx25(String why) implements Result {}
    }

    /**
     * Decodes one frame (the payload of a KISS data frame).
     *
     * @param frame the AX.25 frame without FCS
     * @return the decoded UI frame, or why it is not one
     */
    public static Result decode(byte[] frame) {
        if (frame == null || frame.length < 2 * ADDRESS_BYTES + 1) {
            return new Result.NotAx25("too short for two addresses and a control byte");
        }
        List<String> addresses = new ArrayList<>();
        List<Boolean> repeated = new ArrayList<>();
        int offset = 0;
        boolean last = false;
        while (!last) {
            if (addresses.size() >= 2 + MAX_DIGIPEATERS) {
                return new Result.NotAx25("no address extension bit within " + (2 + MAX_DIGIPEATERS) + " addresses");
            }
            if (offset + ADDRESS_BYTES > frame.length) {
                return new Result.NotAx25("the address field runs past the end of the frame");
            }
            String call = callsign(frame, offset);
            if (call == null) {
                return new Result.NotAx25("address " + (addresses.size() + 1) + " is not shifted ASCII");
            }
            int ssidByte = frame[offset + 6] & 0xFF;
            int ssid = (ssidByte >> 1) & 0x0F;
            addresses.add(ssid == 0 ? call : call + "-" + ssid);
            repeated.add((ssidByte & 0x80) != 0);
            last = (ssidByte & 0x01) != 0;
            offset += ADDRESS_BYTES;
            if (addresses.size() == 1 && last) {
                return new Result.NotAx25("the extension bit ends the address field after one address");
            }
        }
        if (offset >= frame.length) {
            return new Result.NotAx25("no control byte");
        }
        int control = frame[offset] & 0xFF;
        if (control != CONTROL_UI) {
            return new Result.NotUi(control, -1);
        }
        if (offset + 1 >= frame.length) {
            return new Result.NotUi(control, -1);
        }
        int pid = frame[offset + 1] & 0xFF;
        if (pid != PID_NO_LAYER3) {
            return new Result.NotUi(control, pid);
        }
        int lastRepeated = -1;
        for (int i = 2; i < addresses.size(); i++) {
            if (repeated.get(i)) {
                lastRepeated = i;
            }
        }
        List<String> path = new ArrayList<>();
        for (int i = 2; i < addresses.size(); i++) {
            path.add(i == lastRepeated ? addresses.get(i) + "*" : addresses.get(i));
        }
        byte[] info = new byte[frame.length - offset - 2];
        System.arraycopy(frame, offset + 2, info, 0, info.length);
        return new Result.Ui(addresses.get(1), addresses.get(0), path, info);
    }

    /** The six shifted characters at {@code offset}, trailing spaces removed, or null when not valid. */
    private static String callsign(byte[] frame, int offset) {
        StringBuilder call = new StringBuilder(6);
        boolean padding = false;
        for (int i = 0; i < 6; i++) {
            int raw = frame[offset + i] & 0xFF;
            if ((raw & 0x01) != 0) {
                return null;
            }
            char c = (char) (raw >> 1);
            if (c == ' ') {
                padding = true;
                continue;
            }
            if (padding || !((c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9'))) {
                return null;
            }
            call.append(c);
        }
        return call.isEmpty() ? null : call.toString();
    }

    /**
     * Encodes a UI frame (used by tests and fixture tools; Foresignal never transmits).
     *
     * @param source the source call with optional {@code -SSID}
     * @param destination the destination call with optional {@code -SSID}
     * @param digipeaters the path; a trailing {@code *} sets the H bit on that and every earlier digipeater
     * @param info the information field
     */
    public static byte[] encode(String source, String destination, List<String> digipeaters, byte[] info) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<String> path = digipeaters == null ? List.of() : digipeaters;
        int lastRepeated = -1;
        for (int i = 0; i < path.size(); i++) {
            if (path.get(i).endsWith("*")) {
                lastRepeated = i;
            }
        }
        writeAddress(out, destination, false, false);
        writeAddress(out, source, false, path.isEmpty());
        for (int i = 0; i < path.size(); i++) {
            String digi = path.get(i).endsWith("*") ? path.get(i).substring(0, path.get(i).length() - 1) : path.get(i);
            writeAddress(out, digi, i <= lastRepeated, i == path.size() - 1);
        }
        out.write(CONTROL_UI);
        out.write(PID_NO_LAYER3);
        out.writeBytes(info);
        return out.toByteArray();
    }

    private static void writeAddress(ByteArrayOutputStream out, String address, boolean hBit, boolean last) {
        String call = address;
        int ssid = 0;
        int dash = address.indexOf('-');
        if (dash >= 0) {
            call = address.substring(0, dash);
            ssid = Integer.parseInt(address.substring(dash + 1));
        }
        String padded = (call + "      ").substring(0, 6);
        for (int i = 0; i < 6; i++) {
            out.write(padded.charAt(i) << 1);
        }
        out.write(0x60 | (ssid & 0x0F) << 1 | (hBit ? 0x80 : 0) | (last ? 0x01 : 0));
    }
}
