package ai.foresignal.collect;

import ai.foresignal.isolation.WorkRole;
import ai.foresignal.isolation.WorkThreads;
import ai.foresignal.source.spi.SourceDescriptor;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

/**
 * The default {@link FetchDispatcher}: every fetch on a platform thread of its own, named
 * {@code foresignal-fetch-<source id>}, in the {@link WorkRole#COLLECT} role.
 *
 * <p>A fetch blocks on an upstream and on the database. On a platform thread that blocks nobody else: a hanging
 * adapter cannot starve the others (Product Charter NFR-5), and no number of fetches can take the threads or the
 * database connections the API serves requests with. On timeout {@link FetchDispatcher#call} interrupts the thread.
 */
@Component
public class SourceThreadFetchDispatcher implements FetchDispatcher, DisposableBean {

    /** Fetches still running, interrupted at shutdown. */
    private final Set<Thread> running = ConcurrentHashMap.newKeySet();

    private volatile boolean destroyed;

    @Override
    public <T> Future<T> dispatch(SourceDescriptor descriptor, Callable<T> task) {
        if (destroyed) {
            throw new RejectedExecutionException("the fetch dispatcher has shut down");
        }
        FutureTask<T> future = new FutureTask<>(task);
        Thread thread = WorkThreads.unstarted(WorkRole.COLLECT, "foresignal-fetch-" + descriptor.id(), () -> {
            try {
                future.run();
            } finally {
                running.remove(Thread.currentThread());
            }
        });
        running.add(thread);
        thread.start();
        return future;
    }

    /** Interrupts any fetch still running. */
    @Override
    public void destroy() {
        destroyed = true;
        running.forEach(Thread::interrupt);
    }
}
