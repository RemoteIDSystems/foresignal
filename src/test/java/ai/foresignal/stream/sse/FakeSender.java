package ai.foresignal.stream.sse;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** An {@link SseSender} that records frames, and can be told to fail like a dropped connection. */
final class FakeSender implements SseSender {

    final List<SseFrame> frames = new CopyOnWriteArrayList<>();
    volatile boolean completed;
    volatile boolean failing;

    @Override
    public void send(SseFrame frame) throws IOException {
        if (failing) {
            throw new IOException("connection reset");
        }
        frames.add(frame);
    }

    @Override
    public void complete() {
        completed = true;
    }

    /** Frames named {@code event}. */
    List<SseFrame> named(String event) {
        return frames.stream().filter(f -> f.event().equals(event)).toList();
    }

    /** Every event name, in order. */
    List<String> events() {
        return frames.stream().map(SseFrame::event).toList();
    }
}
