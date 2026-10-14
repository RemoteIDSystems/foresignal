package ai.foresignal.source.formats.kiss;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Cuts a KISS byte stream into data frames (prompt 082; KISS as specified by Chepponis and Karn, 1987, and served by
 * Direwolf's {@code KISSPORT}).
 *
 * <ul>
 *   <li><strong>Framing:</strong> a frame lies between two {@code FEND} (0xC0) bytes; inside it {@code FESC TFEND}
 *       (0xDB 0xDC) stands for 0xC0 and {@code FESC TFESC} (0xDB 0xDD) for 0xDB. Back-to-back {@code FEND}s are
 *       empty frames and ignored. A {@code FESC} followed by anything else is a protocol error; that frame is
 *       dropped and counted.
 *   <li><strong>Type byte:</strong> the first byte of a frame: the high nibble is the TNC port, the low nibble the
 *       command. Only command 0 (data) is delivered; every other command (TXDELAY, persistence, SETHARDWARE, ...)
 *       is ignored and counted.
 *   <li><strong>Split reads:</strong> the decoder keeps a partial frame across {@link #feed} calls, so a frame
 *       split over several TCP reads (or several frames in one read) decodes the same.
 *   <li><strong>Limits:</strong> a frame longer than {@value #MAX_FRAME_BYTES} bytes is dropped whole and counted
 *       (an AX.25 UI frame is at most about 330 bytes).
 * </ul>
 * Not thread-safe: one decoder per connection.
 */
public final class KissFrameDecoder {

    /** Frame end. */
    public static final int FEND = 0xC0;

    /** Frame escape. */
    public static final int FESC = 0xDB;

    /** Transposed frame end. */
    public static final int TFEND = 0xDC;

    /** Transposed frame escape. */
    public static final int TFESC = 0xDD;

    /** The longest frame delivered, type byte included. */
    public static final int MAX_FRAME_BYTES = 2048;

    /**
     * One KISS data frame.
     *
     * @param port the TNC port, 0 to 15
     * @param data the frame's payload after the type byte (an AX.25 frame for a packet TNC)
     */
    public record Frame(int port, byte[] data) {

        /** Copies the payload. */
        public Frame {
            data = data == null ? new byte[0] : data.clone();
        }

        /** The payload, copied. */
        @Override
        public byte[] data() {
            return data.clone();
        }
    }

    private final ByteArrayOutputStream current = new ByteArrayOutputStream();
    private boolean inFrame;
    private boolean escaped;
    private boolean broken;
    private long nonData;
    private long malformed;
    private long oversize;

    /**
     * Feeds bytes and returns the data frames they complete, in order.
     *
     * @param bytes the bytes of one read
     * @return the completed data frames, possibly none
     */
    public List<Frame> feed(byte[] bytes) {
        return feed(bytes, 0, bytes == null ? 0 : bytes.length);
    }

    /**
     * Feeds {@code length} bytes from {@code offset} and returns the data frames they complete.
     */
    public List<Frame> feed(byte[] bytes, int offset, int length) {
        List<Frame> out = new ArrayList<>();
        if (bytes == null) {
            return out;
        }
        for (int i = offset; i < offset + length; i++) {
            int b = bytes[i] & 0xFF;
            if (b == FEND) {
                if (inFrame) {
                    finish(out);
                }
                inFrame = true;
                reset();
                continue;
            }
            if (!inFrame || broken) {
                // Bytes before the first FEND, or after an error inside a frame, wait for the next FEND.
                continue;
            }
            if (escaped) {
                escaped = false;
                if (b == TFEND) {
                    append(FEND);
                } else if (b == TFESC) {
                    append(FESC);
                } else {
                    malformed++;
                    broken = true;
                }
            } else if (b == FESC) {
                escaped = true;
            } else {
                append(b);
            }
        }
        return out;
    }

    private void append(int b) {
        if (current.size() >= MAX_FRAME_BYTES) {
            oversize++;
            broken = true;
            return;
        }
        current.write(b);
    }

    private void finish(List<Frame> out) {
        if (broken || current.size() == 0) {
            return;
        }
        if (escaped) {
            malformed++;
            return;
        }
        byte[] frame = current.toByteArray();
        int type = frame[0] & 0xFF;
        int command = type & 0x0F;
        if (command != 0) {
            nonData++;
            return;
        }
        byte[] data = new byte[frame.length - 1];
        System.arraycopy(frame, 1, data, 0, data.length);
        out.add(new Frame(type >> 4, data));
    }

    private void reset() {
        current.reset();
        escaped = false;
        broken = false;
    }

    /** Frames with a command other than data, ignored so far. */
    public long nonData() {
        return nonData;
    }

    /** Frames dropped for a bad escape so far. */
    public long malformed() {
        return malformed;
    }

    /** Frames dropped for exceeding {@value #MAX_FRAME_BYTES} bytes so far. */
    public long oversize() {
        return oversize;
    }

    /**
     * Encodes {@code data} as one KISS data frame on {@code port} (used by tests and fixture tools; Foresignal never
     * sends KISS to a TNC).
     */
    public static byte[] encode(int port, byte[] data) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(FEND);
        out.write((port & 0x0F) << 4);
        for (byte value : data) {
            int b = value & 0xFF;
            if (b == FEND) {
                out.write(FESC);
                out.write(TFEND);
            } else if (b == FESC) {
                out.write(FESC);
                out.write(TFESC);
            } else {
                out.write(b);
            }
        }
        out.write(FEND);
        return out.toByteArray();
    }
}
