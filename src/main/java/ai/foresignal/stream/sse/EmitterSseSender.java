package ai.foresignal.stream.sse;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * An {@link SseSender} over a Spring {@link SseEmitter}. The id line is written only when the frame
 * has one; data is the frame's JSON text, written as-is ({@code text/plain} picks the string
 * converter, so the JSON is not quoted again).
 */
public final class EmitterSseSender implements SseSender {

    private final SseEmitter emitter;
    private final AtomicBoolean completed = new AtomicBoolean();

    /**
     * A stream emitter with no timeout whose response is committed as soon as Spring initializes it.
     *
     * <p>Spring commits the response (status and headers) only on the first write, so a stream with
     * nothing to say would leave its client without even the headers until the first heartbeat
     * (prompt 022). The emitter therefore starts with the SSE comment line {@code :open}, which
     * every EventSource ignores; Spring holds it until the emitter is initialized and then writes
     * and flushes it. Call this before {@code SseHub.open}, so no other thread writes yet.
     */
    public static SseEmitter newCommittingEmitter() {
        SseEmitter emitter = new SseEmitter(0L);
        try {
            emitter.send(SseEmitter.event().comment("open"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return emitter;
    }

    /** Wraps {@code emitter}. */
    public EmitterSseSender(SseEmitter emitter) {
        this.emitter = emitter;
    }

    @Override
    public void send(SseFrame frame) throws IOException {
        SseEmitter.SseEventBuilder event = SseEmitter.event();
        if (frame.id() != null) {
            event.id(frame.id());
        }
        event.name(frame.event()).data(frame.data(), MediaType.TEXT_PLAIN);
        emitter.send(event);
    }

    @Override
    public void complete() {
        if (completed.compareAndSet(false, true)) {
            try {
                emitter.complete();
            } catch (RuntimeException e) {
                // The connection is already gone; nothing to end.
            }
        }
    }
}
