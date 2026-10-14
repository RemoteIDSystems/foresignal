package ai.foresignal.source.codec;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Cuts gpsd's stream into JSON lines and UBX frames when {@code ubxMonitor} asks gpsd for {@code "raw":2} (prompt 086).
 *
 * <p>With {@code "raw":2} gpsd passes the receiver's binary packets through unaltered between its JSON objects
 * (https://gpsd.gitlab.io/gpsd/gpsd_json.html, WATCH {@code raw}). A UBX payload may contain {@code 0x0A}, so line
 * framing cannot be used; the transport delivers raw chunks and this splitter finds the frames:
 *
 * <ul>
 *   <li>{@code 0xB5 0x62} starts a UBX frame whose length comes from its header; the frame is delivered whole, and a
 *       frame with a bad checksum is dropped and counted;
 *   <li>{@code {} starts a JSON line ending in LF (a trailing CR is removed);
 *   <li>anything else (NMEA sentences gpsd also passes through, partial garbage) is skipped up to the next line end or
 *       sync character and counted.
 * </ul>
 * At most {@value #MAX_BUFFER} bytes are held; a longer unfinished frame is discarded and counted. Not thread-safe; one
 * splitter per connection.
 */
public final class GpsdRawSplitter {

    /** The most bytes held while a frame is incomplete. */
    public static final int MAX_BUFFER = 64 * 1024;

    /** One complete frame. */
    public sealed interface Frame permits JsonLine, Ubx {}

    /**
     * A JSON line without its terminator.
     *
     * @param bytes the line
     */
    public record JsonLine(byte[] bytes) implements Frame {}

    /**
     * A UBX frame with a valid checksum, sync characters included.
     *
     * @param bytes the frame
     */
    public record Ubx(byte[] bytes) implements Frame {}

    private byte[] buffer = new byte[0];
    private long skipped;

    /** Appends {@code chunk} and returns every frame it completes, in order. */
    public List<Frame> feed(byte[] chunk) {
        if (chunk != null && chunk.length > 0) {
            ByteArrayOutputStream joined = new ByteArrayOutputStream(buffer.length + chunk.length);
            joined.writeBytes(buffer);
            joined.writeBytes(chunk);
            buffer = joined.toByteArray();
        }
        List<Frame> out = new ArrayList<>();
        int pos = 0;
        while (pos < buffer.length) {
            int b = buffer[pos] & 0xFF;
            if (b == UbxMonParser.SYNC1) {
                if (buffer.length - pos < 2) {
                    break;
                }
                if ((buffer[pos + 1] & 0xFF) != UbxMonParser.SYNC2) {
                    pos++;
                    skipped++;
                    continue;
                }
                int length = UbxMonParser.frameLength(buffer, pos, buffer.length - pos);
                if (length < 0 || buffer.length - pos < length) {
                    if (length > MAX_BUFFER) {
                        pos += 2;
                        skipped++;
                        continue;
                    }
                    break;
                }
                byte[] frame = Arrays.copyOfRange(buffer, pos, pos + length);
                if (UbxMonParser.valid(frame)) {
                    out.add(new Ubx(frame));
                    pos += length;
                } else {
                    skipped++;
                    pos += 2;
                }
            } else if (b == '{') {
                int end = indexOf(buffer, pos, (byte) '\n');
                if (end < 0) {
                    break;
                }
                int stop = end > pos && buffer[end - 1] == '\r' ? end - 1 : end;
                out.add(new JsonLine(Arrays.copyOfRange(buffer, pos, stop)));
                pos = end + 1;
            } else if (b == '\n' || b == '\r') {
                pos++;
            } else {
                int next = pos + 1;
                while (next < buffer.length && buffer[next] != '\n' && (buffer[next] & 0xFF) != UbxMonParser.SYNC1
                        && buffer[next] != '{') {
                    next++;
                }
                skipped++;
                pos = next;
            }
        }
        buffer = Arrays.copyOfRange(buffer, pos, buffer.length);
        if (buffer.length > MAX_BUFFER) {
            buffer = new byte[0];
            skipped++;
        }
        return out;
    }

    /** Frames and garbage runs skipped so far. */
    public long skipped() {
        return skipped;
    }

    private static int indexOf(byte[] bytes, int from, byte value) {
        for (int i = from; i < bytes.length; i++) {
            if (bytes[i] == value) {
                return i;
            }
        }
        return -1;
    }
}
