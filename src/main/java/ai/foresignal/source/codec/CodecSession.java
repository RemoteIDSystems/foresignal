package ai.foresignal.source.codec;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.time.Instant;
import java.util.List;

/**
 * The state of one connection's decoding (prompt 069): a session is created when a connection opens and
 * dropped when it closes, so nothing from a previous connection leaks into the next.
 *
 * <p>Pure apart from its own state: no I/O, no clock (time comes in as {@code receivedAt}), no shared
 * mutable state. Called from one thread at a time.
 */
public interface CodecSession {

    /**
     * Decodes one frame.
     *
     * @param frame one frame as the codec's framing cut it
     * @param receivedAt when the transport read it
     * @return the records the frame completes, possibly none
     * @throws UpstreamFormatException when the stream as a whole is unusable (for example an unsupported
     *     protocol version); the connection is then closed. A single malformed frame is skipped and counted
     *     instead
     */
    List<NormalizedRecord> onFrame(byte[] frame, Instant receivedAt);

    /** Frames skipped as malformed since the session opened. */
    default long skipped() {
        return 0;
    }
}
