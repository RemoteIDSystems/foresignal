package ai.foresignal.testsupport.adapters;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A polling adapter whose responses are scripted.
 *
 * <p>Shared by prompts 005–008, so the collector, budgets and health can be driven through exact
 * sequences — two successes then a failure then a 304 — without a network or a real upstream.
 *
 * <p>Thread-safe: the scheduler runs fetches on virtual threads, and a test that asserts on
 * {@link #fetchCount()} would otherwise race.
 */
public class FakePollingAdapter implements PollingAdapter {

    private final SourceDescriptor descriptor;
    private final Deque<Object> script = new ArrayDeque<>();
    private final List<FetchContext> contexts = new CopyOnWriteArrayList<>();
    private final AtomicInteger fetchCount = new AtomicInteger();

    /** What to return once the script runs out. Defaults to an empty complete result. */
    private volatile FetchResult fallback = FetchResult.complete(List.of());

    public FakePollingAdapter(SourceDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    /** A minimal valid polling descriptor for {@code id}. */
    public static FakePollingAdapter withId(String id) {
        return new FakePollingAdapter(defaultDescriptor(id).build());
    }

    /** A builder pre-filled so a test only states what it cares about. */
    public static SourceDescriptor.Builder defaultDescriptor(String id) {
        return SourceDescriptor.builder(id)
                .name("Fake " + id)
                .description("A scripted polling source for tests")
                .kind("fake-record")
                .interval(Duration.ofSeconds(10), Duration.ofSeconds(60))
                .defaultBudget(Budget.of(60, 10_000));
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** Queues a result to be returned by a later fetch. */
    public FakePollingAdapter scriptResult(FetchResult result) {
        script.addLast(result);
        return this;
    }

    /** Queues an exception to be thrown by a later fetch. */
    public FakePollingAdapter scriptFailure(Exception failure) {
        script.addLast(failure);
        return this;
    }

    /** Sets what is returned once the script is exhausted. */
    public FakePollingAdapter fallback(FetchResult result) {
        this.fallback = result;
        return this;
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        fetchCount.incrementAndGet();
        contexts.add(ctx);
        Object next = script.pollFirst();
        if (next == null) {
            return fallback;
        }
        if (next instanceof FetchResult result) {
            return result;
        }
        if (next instanceof AdapterException adapterException) {
            throw adapterException;
        }
        if (next instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        throw new AdapterException("Scripted failure", (Exception) next);
    }

    /** How many times {@link #fetch} has been called. */
    public int fetchCount() {
        return fetchCount.get();
    }

    /** The contexts each fetch was given, in order. */
    public List<FetchContext> contexts() {
        return List.copyOf(contexts);
    }

    /** The most recent context, or null. */
    public FetchContext lastContext() {
        return contexts.isEmpty() ? null : contexts.get(contexts.size() - 1);
    }

    /** True when every scripted response has been consumed. */
    public boolean scriptExhausted() {
        return script.isEmpty();
    }

    public void reset() {
        script.clear();
        contexts.clear();
        fetchCount.set(0);
    }
}
