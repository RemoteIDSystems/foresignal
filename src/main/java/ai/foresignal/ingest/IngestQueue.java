package ai.foresignal.ingest;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The bounded hand-off between producers (collector, sinks, push endpoints, retention) and the
 * single ingest writer (Core-Data-Model §6).
 *
 * <p>Capacity {@code foresignal.ingest.queue-capacity} (64). When the writer falls behind, or is
 * held by its clock guard or lock, the queue fills and {@link #submit} blocks its caller for up to
 * the given timeout before failing with {@link IngestBackpressureException}; that back-pressure is
 * what slows producers down. Gauge {@code foresignal.ingest.queue.depth}.
 */
@Component
public class IngestQueue {

    /** The depth gauge. */
    public static final String DEPTH_GAUGE = "foresignal.ingest.queue.depth";

    private final BlockingQueue<IngestCommand> queue;
    private final int capacity;

    /** The queue sized from {@link IngestProperties}. */
    @Autowired
    public IngestQueue(IngestProperties properties, MeterRegistry meters) {
        this(properties.queueCapacity(), meters);
    }

    /**
     * A queue of {@code capacity}.
     *
     * @param capacity the maximum number of waiting commands
     * @param meters where the depth gauge is registered
     */
    public IngestQueue(int capacity, MeterRegistry meters) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be positive, got " + capacity);
        }
        this.capacity = capacity;
        this.queue = new ArrayBlockingQueue<>(capacity);
        Gauge.builder(DEPTH_GAUGE, queue, BlockingQueue::size).register(meters);
    }

    /**
     * Submits one batch of records for {@code sourceId}.
     *
     * @param sourceId the source
     * @param log the batch's provenance
     * @param records the records, in arrival order
     * @param timeout how long to wait for room in the queue
     * @return completed by the writer with the outcome, or exceptionally if the batch failed
     * @throws IngestBackpressureException when the queue stayed full for the whole timeout
     */
    public CompletableFuture<IngestOutcome> submit(
            String sourceId, FetchLogEntry log, List<IngestRecord> records, Duration timeout) {
        return submit(sourceId, log, records, timeout, false);
    }

    /**
     * Submits one batch, forcing touches when {@code forceTouch} is set (prompt 094: the batches of a
     * complete-set reference run always advance {@code record_live.last_received_at}).
     */
    public CompletableFuture<IngestOutcome> submit(
            String sourceId, FetchLogEntry log, List<IngestRecord> records, Duration timeout, boolean forceTouch) {
        IngestBatch batch = new IngestBatch(sourceId, log, records, new CompletableFuture<>(), forceTouch);
        put(batch, timeout, "batch for " + sourceId);
        return batch.future();
    }

    /**
     * Submits a head purge (prompt 019's retention job).
     *
     * @param specs one entry per source
     * @return completed with the heads deleted per source once every spec is done
     * @throws IngestBackpressureException when the queue stayed full for 30 s
     */
    public CompletableFuture<PurgeOutcome> submitPurge(List<PurgeSpec> specs) {
        return submitPurge(specs, Duration.ofSeconds(30));
    }

    /**
     * Submits a head purge, waiting at most {@code timeout} for room.
     *
     * @throws IngestBackpressureException when the queue stayed full for the whole timeout
     */
    public CompletableFuture<PurgeOutcome> submitPurge(List<PurgeSpec> specs, Duration timeout) {
        HeadPurge purge = new HeadPurge(specs, new CompletableFuture<>());
        put(purge, timeout, "head purge");
        return purge.future();
    }

    /**
     * Submits an entity purge (prompt 039), waiting at most {@code timeout} for room.
     *
     * @param sourceId the source
     * @param externalId the entity
     * @return completed by the writer with the outcome
     * @throws IngestBackpressureException when the queue stayed full for the whole timeout
     */
    public CompletableFuture<EntityPurgeOutcome> submitEntityPurge(String sourceId, String externalId,
            Duration timeout) {
        EntityPurge purge = new EntityPurge(sourceId, externalId, new CompletableFuture<>());
        put(purge, timeout, "entity purge of " + sourceId);
        return purge.future();
    }

    /**
     * Submits a complete-set reconciliation (prompt 094), waiting at most {@code timeout} for room.
     *
     * @param sourceId the source
     * @param kinds the record kinds to reconcile
     * @param runStartedAt when the complete run started
     * @param maxAbsentRatio the descriptor's {@code reconcile.maxAbsentRatio}
     * @param allowShrink the operator's one-off override
     * @return completed by the writer with what the reconciliation did
     * @throws IngestBackpressureException when the queue stayed full for the whole timeout
     */
    public CompletableFuture<TombstoneOutcome> submitTombstoneAbsent(String sourceId, java.util.Set<String> kinds,
            java.time.Instant runStartedAt, double maxAbsentRatio, boolean allowShrink, Duration timeout) {
        TombstoneAbsent reconcile =
                new TombstoneAbsent(sourceId, kinds, runStartedAt, maxAbsentRatio, allowShrink, new CompletableFuture<>());
        put(reconcile, timeout, "complete-set reconciliation of " + sourceId);
        return reconcile.future();
    }

    /**
     * Enqueues {@code command} at the tail without waiting.
     *
     * @return false when the queue is full
     */
    public boolean offer(IngestCommand command) {
        return queue.offer(Objects.requireNonNull(command, "command"));
    }

    /**
     * The next command, waiting up to {@code timeout}; null when none arrived. Used only by the
     * writer thread.
     */
    public IngestCommand poll(Duration timeout) throws InterruptedException {
        return queue.poll(timeout.toNanos(), TimeUnit.NANOSECONDS);
    }

    /** Removes and returns every waiting command. */
    public List<IngestCommand> drain() {
        List<IngestCommand> drained = new ArrayList<>();
        queue.drainTo(drained);
        return drained;
    }

    /** Commands waiting. */
    public int depth() {
        return queue.size();
    }

    /** The configured capacity. */
    public int capacity() {
        return capacity;
    }

    private void put(IngestCommand command, Duration timeout, String what) {
        boolean accepted;
        try {
            accepted = queue.offer(command, Math.max(0, timeout.toNanos()), TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IngestBackpressureException("Interrupted while waiting to submit a " + what);
        }
        if (!accepted) {
            throw new IngestBackpressureException(
                    "The ingest queue stayed full (" + capacity + " commands) for " + timeout + "; " + what + " not accepted");
        }
    }
}
