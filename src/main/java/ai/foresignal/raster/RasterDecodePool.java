package ai.foresignal.raster;

import jakarta.annotation.PreDestroy;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Where heavy decodes and quantization run (Weather-Imagery-Design §2.2, Scheduler row): a bounded
 * pool of <em>platform</em> threads behind a semaphore, one permit in the small profile and two in
 * the large. Never virtual threads: a decode is CPU- and memory-heavy, and an unbounded number of
 * virtual threads decoding at once would exhaust the heap.
 *
 * <p>The calling fetch (a virtual thread) waits for its permit, hands the work to the pool and blocks
 * until it finishes, so at most {@code decodeConcurrency} decodes are ever in memory.
 */
@Component
public class RasterDecodePool {

    private final Semaphore permits;
    private final ExecutorService executor;
    private final int concurrency;

    /** Creates the pool sized by the profile's decode concurrency. */
    @Autowired
    public RasterDecodePool(RasterSettings settings) {
        this(settings.decodeConcurrency());
    }

    /** Creates a pool of {@code concurrency} threads and permits. */
    public RasterDecodePool(int concurrency) {
        this.concurrency = Math.max(1, concurrency);
        this.permits = new Semaphore(this.concurrency, true);
        AtomicInteger counter = new AtomicInteger();
        ThreadFactory threads = runnable -> {
            Thread thread = Thread.ofPlatform().daemon(true).name("raster-decode-" + counter.incrementAndGet())
                    .unstarted(runnable);
            return thread;
        };
        this.executor = new ThreadPoolExecutor(this.concurrency, this.concurrency, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(), threads);
    }

    /** The number of concurrent decodes. */
    public int concurrency() {
        return concurrency;
    }

    /**
     * Runs {@code work} on the pool and waits for it.
     *
     * @return what {@code work} returned
     * @throws Exception whatever {@code work} threw, unwrapped
     * @throws InterruptedException when the caller is interrupted while waiting
     */
    public <T> T run(Callable<T> work) throws Exception {
        permits.acquire();
        try {
            Future<T> future = executor.submit(work);
            try {
                return future.get();
            } catch (InterruptedException e) {
                future.cancel(true);
                throw e;
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof Exception exception) {
                    throw exception;
                }
                if (cause instanceof Error error) {
                    throw error;
                }
                throw e;
            }
        } finally {
            permits.release();
        }
    }

    /** Stops the pool. */
    @PreDestroy
    public void shutdown() {
        executor.shutdownNow();
    }
}
