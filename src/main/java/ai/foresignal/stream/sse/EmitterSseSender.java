package ai.foresignal.stream.sse;

import java.io.IOException;
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
