package ai.foresignal.stream.sse;

import java.io.IOException;

/**
 * Where an {@link SseClient} writes its frames. Production wraps a Spring {@code SseEmitter}
 * ({@link EmitterSseSender}); unit tests record the frames.
 *
 * <p>Called only from the client's sender thread.
 */
public interface SseSender {

    /**
     * Writes one frame.
     *
     * @throws IOException when the connection is gone; the client then closes
     */
    void send(SseFrame frame) throws IOException;

    /** Ends the response normally. Idempotent. */
    void complete();
}
