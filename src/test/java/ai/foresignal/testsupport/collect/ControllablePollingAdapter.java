package ai.foresignal.testsupport.collect;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A polling adapter whose fetches run arbitrary scripted code.
 *
 * <p>{@code FakePollingAdapter} returns canned results; the collector tests also need a fetch that
 * blocks until released (timeouts, overlap, disable-in-flight) and one that really calls a stubbed
 * upstream through {@code ctx.http()} (traces, redaction, probes). Each scripted step receives the
 * context and may do either.
 */
public class ControllablePollingAdapter implements PollingAdapter {

    /** One scripted fetch. */
    @FunctionalInterface
    public interface Step {

        /** Runs the fetch. */
        FetchResult fetch(FetchContext ctx) throws Exception;
    }

    private final SourceDescriptor descriptor;
    private final Deque<Step> script = new ArrayDeque<>();
    private final List<FetchContext> contexts = new CopyOnWriteArrayList<>();
    private final AtomicInteger fetchCount = new AtomicInteger();
    private volatile Step fallback = ctx -> FetchResult.complete(List.of());

    /** An adapter for {@code descriptor}. */
    public ControllablePollingAdapter(SourceDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** Queues a step for a later fetch. */
    public synchronized ControllablePollingAdapter then(Step step) {
        script.addLast(step);
        return this;
    }

    /** What every fetch does once the script is exhausted. */
    public ControllablePollingAdapter fallback(Step step) {
        this.fallback = step;
        return this;
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        fetchCount.incrementAndGet();
        contexts.add(ctx);
        Step step;
        synchronized (this) {
            step = script.pollFirst();
        }
        if (step == null) {
            step = fallback;
        }
        try {
            return step.fetch(ctx);
        } catch (AdapterException | RuntimeException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AdapterException("Interrupted", e);
        } catch (Exception e) {
            throw new AdapterException("Scripted failure", e);
        }
    }

    /** Fetches so far. */
    public int fetchCount() {
        return fetchCount.get();
    }

    /** The most recent context, or null. */
    public FetchContext lastContext() {
        return contexts.isEmpty() ? null : contexts.get(contexts.size() - 1);
    }

    /** Clears the script, the contexts, the count and the fallback. */
    public synchronized void reset() {
        script.clear();
        contexts.clear();
        fetchCount.set(0);
        fallback = ctx -> FetchResult.complete(List.of());
    }
}
