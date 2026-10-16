package ai.foresignal.export;

import jakarta.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * The one bounded worker pool of exports, snapshots and packs (prompt 111): tasks run on virtual
 * threads, at most {@code exports.maxConcurrent} (default 1) at a time; the rest wait their turn.
 *
 * <p>{@link #submit} defers a task submitted inside a transaction until that transaction commits,
 * so the worker always finds the job row it was given (a task started before the commit could read
 * no row and leave the job {@code QUEUED} forever). A rolled-back transaction submits nothing.
 */
@Component
public class ExportWorker {

    private static final Logger log = LoggerFactory.getLogger(ExportWorker.class);

    private final ExecutorService executor = Executors.newThreadPerTaskExecutor(
            Thread.ofVirtual().name("export-worker-", 0).factory());
    private final Semaphore permits;
    private final AtomicInteger waiting = new AtomicInteger();
    private final AtomicInteger running = new AtomicInteger();

    /** Creates the worker with {@code exports.maxConcurrent} permits. */
    public ExportWorker(ExportProperties properties) {
        this.permits = new Semaphore(properties.maxConcurrent(), true);
    }

    /** Runs {@code task} on the pool, after the current transaction commits when one is active. */
    public void submit(Runnable task) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    enqueue(task);
                }
            });
        } else {
            enqueue(task);
        }
    }

    private void enqueue(Runnable task) {
        waiting.incrementAndGet();
        executor.submit(() -> {
            try {
                permits.acquire();
            } catch (InterruptedException e) {
                waiting.decrementAndGet();
                Thread.currentThread().interrupt();
                return;
            }
            waiting.decrementAndGet();
            running.incrementAndGet();
            try {
                task.run();
            } catch (RuntimeException e) {
                log.warn("An export task failed outside its own error handling", e);
            } finally {
                running.decrementAndGet();
                permits.release();
            }
        });
    }

    /** Tasks waiting for a permit. */
    public int waiting() {
        return waiting.get();
    }

    /** Tasks running now. */
    public int running() {
        return running.get();
    }

    /** Stops accepting tasks; running tasks see their thread interrupted. */
    @PreDestroy
    public void shutdown() {
        executor.shutdownNow();
    }
}
