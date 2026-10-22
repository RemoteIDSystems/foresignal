package ai.foresignal.collect;

import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Where an adapter's work actually runs.
 *
 * <p>A seam rather than a hard-coded executor: the default ({@link SourceThreadFetchDispatcher}) runs
 * every fetch on a platform thread of its own, in the collect role, so collection never shares the
 * threads or the database pool the API serves requests with.
 */
public interface FetchDispatcher {

    /** Starts {@code task} for the source {@code descriptor} describes. */
    <T> Future<T> dispatch(SourceDescriptor descriptor, Callable<T> task);

    /**
     * Runs {@code task} and waits up to {@code timeout} for it.
     *
     * <p>On timeout the task is canceled with interruption and an {@link AdapterTimeoutException}
     * is thrown. A failure inside the task is rethrown as itself, not wrapped, so callers can tell
     * a refused budget from a broken upstream.
     *
     * @throws Exception whatever the task threw, or {@link AdapterTimeoutException}
     */
    default <T> T call(SourceDescriptor descriptor, Callable<T> task, Duration timeout) throws Exception {
        Future<T> future = dispatch(descriptor, task);
        try {
            return future.get(Math.max(1, timeout.toMillis()), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new AdapterTimeoutException(descriptor.id(), timeout);
        } catch (InterruptedException e) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            throw e;
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            if (cause instanceof Exception exception) {
                throw exception;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new IllegalStateException(cause);
        }
    }
}
