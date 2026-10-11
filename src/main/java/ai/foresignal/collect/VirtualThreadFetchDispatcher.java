package ai.foresignal.collect;

import ai.foresignal.source.spi.SourceDescriptor;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

/**
 * The default {@link FetchDispatcher}: one virtual thread per fetch.
 *
 * <p>A fetch spends nearly all its time waiting on an upstream, so a virtual thread costs almost
 * nothing while it waits, and a hanging adapter cannot starve the others (Product Charter NFR-5).
 */
@Component
public class VirtualThreadFetchDispatcher implements FetchDispatcher, DisposableBean {

    private final ExecutorService executor =
            Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("foresignal-fetch-", 0).factory());

    @Override
    public <T> Future<T> dispatch(SourceDescriptor descriptor, Callable<T> task) {
        return executor.submit(task);
    }

    /** Interrupts any fetch still running. */
    @Override
    public void destroy() {
        executor.shutdownNow();
    }
}
