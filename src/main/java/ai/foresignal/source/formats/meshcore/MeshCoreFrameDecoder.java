package ai.foresignal.source.formats.meshcore;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Reassembles the radio-to-app frames of the MeshCore WiFi companion firmware's TCP stream (prompt 083): each frame is
 * {@code '>'}, a uint16 little-endian length and the frame. Reads may split or join frames arbitrarily.
 *
 * <ul>
 *   <li>A frame longer than {@link MeshCoreProtocol#MAX_FRAME_SIZE} is skipped whole (its bytes are consumed and
 *       counted), as the firmware does with oversize app frames.
 *   <li>A byte where an envelope marker should be is skipped and counted until a {@code '>'} appears, so the decoder
 *       resynchronizes after garbage.
 *   <li>An empty frame (length 0) is ignored.
 * </ul>
 *
 * <p>Not thread-safe: one decoder per connection, fed from its read loop.
 */
public final class MeshCoreFrameDecoder {

    private final ByteArrayOutputStream pending = new ByteArrayOutputStream();
    private int skipRemaining;
    private long oversize;
    private long junk;

    /**
     * Feeds one read and returns the frames it completes, without their envelopes.
     *
     * @param bytes the bytes read
     * @return the completed frames, in order
     */
    public List<byte[]> feed(byte[] bytes) {
        return feed(bytes, 0, bytes == null ? 0 : bytes.length);
    }

    /** {@link #feed(byte[])} on a slice. */
    public List<byte[]> feed(byte[] bytes, int offset, int length) {
        List<byte[]> frames = new ArrayList<>();
        int i = offset;
        int end = offset + length;
        while (i < end && skipRemaining > 0) {
            int n = Math.min(skipRemaining, end - i);
            skipRemaining -= n;
            i += n;
        }
        if (i < end) {
            pending.write(bytes, i, end - i);
        }
        byte[] buffer = pending.toByteArray();
        int pos = 0;
        while (pos < buffer.length) {
            if (buffer[pos] != MeshCoreProtocol.RADIO_TO_APP) {
                junk++;
                pos++;
                continue;
            }
            if (buffer.length - pos < 3) {
                break;
            }
            int len = (buffer[pos + 1] & 0xFF) | (buffer[pos + 2] & 0xFF) << 8;
            if (len > MeshCoreProtocol.MAX_FRAME_SIZE) {
                oversize++;
                int available = buffer.length - pos - 3;
                if (available >= len) {
                    pos += 3 + len;
                } else {
                    skipRemaining = len - available;
                    pos = buffer.length;
                }
                continue;
            }
            if (buffer.length - pos - 3 < len) {
                break;
            }
            if (len > 0) {
                byte[] frame = new byte[len];
                System.arraycopy(buffer, pos + 3, frame, 0, len);
                frames.add(frame);
            }
            pos += 3 + len;
        }
        pending.reset();
        if (pos < buffer.length) {
            pending.write(buffer, pos, buffer.length - pos);
        }
        return frames;
    }

    /** Frames skipped because they were longer than {@link MeshCoreProtocol#MAX_FRAME_SIZE}. */
    public long oversize() {
        return oversize;
    }

    /** Bytes skipped while looking for an envelope marker. */
    public long junk() {
        return junk;
    }

    /** Bytes buffered towards an incomplete frame (tests). */
    public int buffered() {
        return pending.size();
    }
}
