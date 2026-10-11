package ai.foresignal.testsupport.adapters;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.RecordSink;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.StreamContext;
import ai.foresignal.source.spi.StreamHandle;
import ai.foresignal.source.spi.StreamingAdapter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * A streaming adapter that pushes scripted records to the sink when started, and lets a test push
 * more afterwards.
 *
 * <p>Used by prompts 005–008 to exercise the streaming supervisor's start, reconnect and close
 * paths, including the case where the sink blocks on a full queue.
 */
public class FakeStreamingAdapter implements StreamingAdapter {

    private final SourceDescriptor descriptor;
    private final List<NormalizedRecord> onStart = new CopyOnWriteArrayList<>();
    private final List<Handle> handles = new CopyOnWriteArrayList<>();
    private final AtomicInteger startCount = new AtomicInteger();

    private volatile AdapterException startFailure;
    private volatile RecordSink sink;

    public FakeStreamingAdapter(SourceDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    public static FakeStreamingAdapter withId(String id) {
        return new FakeStreamingAdapter(defaultDescriptor(id).build());
    }

    /** A minimal valid streaming descriptor. Streams have no poll interval. */
    public static SourceDescriptor.Builder defaultDescriptor(String id) {
        return SourceDescriptor.builder(id)
                .name("Fake streaming " + id)
                .description("A scripted streaming source for tests")
                .kind("fake-record")
                .mode(Mode.STREAMING)
                .defaultBudget(Budget.of(10, 1_000))
                .recordTtl(Duration.ofMinutes(30));
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** Records pushed as soon as the stream starts. */
    public FakeStreamingAdapter scriptOnStart(NormalizedRecord... records) {
        onStart.addAll(List.of(records));
        return this;
    }

    /** Makes the next {@link #start} throw. */
    public FakeStreamingAdapter failOnStart(AdapterException failure) {
        this.startFailure = failure;
        return this;
    }

    @Override
    public StreamHandle start(StreamContext ctx, RecordSink sink) throws AdapterException {
        startCount.incrementAndGet();
        if (startFailure != null) {
            AdapterException failure = startFailure;
            startFailure = null;
            throw failure;
        }
        this.sink = sink;
        onStart.forEach(sink::accept);
        Handle handle = new Handle();
        handles.add(handle);
        return handle;
    }

    /** Pushes a record as if the upstream had sent it. */
    public void emit(NormalizedRecord record) {
        RecordSink current = sink;
        if (current == null) {
            throw new IllegalStateException("The stream has not been started");
        }
        current.accept(record);
    }

    public int startCount() {
        return startCount.get();
    }

    /** How many handles are still open. */
    public long openHandles() {
        return handles.stream().filter(Handle::isRunning).count();
    }

    /** True when every handle handed out has been closed. */
    public boolean allClosed() {
        return handles.stream().noneMatch(Handle::isRunning);
    }

    public List<StreamHandle> handles() {
        return new ArrayList<>(handles);
    }

    public void reset() {
        handles.forEach(Handle::close);
        handles.clear();
        onStart.clear();
        startCount.set(0);
        sink = null;
        startFailure = null;
    }

    /** The most recently started handle, or null. */
    public Handle lastHandle() {
        return handles.isEmpty() ? null : handles.get(handles.size() - 1);
    }

    /**
     * Tracks whether it was closed, and tolerates being closed twice. {@link #drop} simulates the
     * upstream ending the connection, which notifies the {@link #onClose} callback.
     */
    public static final class Handle implements StreamHandle {

        private final AtomicBoolean running = new AtomicBoolean(true);
        private final AtomicInteger closeCount = new AtomicInteger();
        private final List<Consumer<Throwable>> closeCallbacks = new CopyOnWriteArrayList<>();

        @Override
        public void close() {
            closeCount.incrementAndGet();
            running.set(false);
        }

        @Override
        public void onClose(Consumer<Throwable> callback) {
            closeCallbacks.add(callback);
        }

        /** Ends the stream as the upstream would; {@code error} is null for a clean close. */
        public void drop(Throwable error) {
            running.set(false);
            closeCallbacks.forEach(callback -> callback.accept(error));
        }

        @Override
        public boolean isRunning() {
            return running.get();
        }

        /** How many times close() was called; the supervisor may call it more than once. */
        public int closeCount() {
            return closeCount.get();
        }
    }
}
