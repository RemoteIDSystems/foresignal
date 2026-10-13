package ai.foresignal.source.spi;

import java.time.Instant;

/**
 * Receives the frames of a TCP connection opened through {@link SocketTransport#tcp} (prompt 069).
 *
 * <p>Called on the connection's own virtual thread, one frame at a time and in order. It may block, for
 * example in {@link RecordSink#accept} when the ingest queue is full: the transport then simply stops
 * reading, which is the back-pressure the socket's receive window passes on to the sender.
 */
@FunctionalInterface
public interface FrameHandler {

    /**
     * Handles one frame.
     *
     * @param frame the frame's bytes (a line without its terminator, or a raw chunk); owned by the handler
     * @param receivedAt when the transport read it, from the context clock
     * @throws Exception to close the connection; the stream then reports the failure and the supervisor
     *     reconnects on its ladder
     */
    void onFrame(byte[] frame, Instant receivedAt) throws Exception;
}
