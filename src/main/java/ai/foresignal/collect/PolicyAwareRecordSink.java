package ai.foresignal.collect;

import ai.foresignal.ingest.FetchLogEntry;
import ai.foresignal.ingest.IngestBackpressureException;
import ai.foresignal.ingest.IngestOutcome;
import ai.foresignal.ingest.IngestRecord;
import ai.foresignal.source.spi.Coverage;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.RecordSink;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.FetchStatus;
import ai.foresignal.store.FetchTrigger;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The record sink of one streaming source: buffers what the adapter hands over and flushes it to
 * the ingest writer in bounded batches (Core-Data-Model §7). Replaces the pre-v1.2 coalescing sink.
 *
 * <p><strong>Stamping.</strong> {@code receivedAt} is stamped on {@link #accept}, with a per-source
 * arrival number, so the writer's ordering and the RECEIVED-anchored TTLs see when Foresignal
 * actually received each message, not when its batch was flushed.
 *
 * <p><strong>One outstanding batch.</strong> A flush is due once the oldest buffered record has
 * waited {@code flushInterval} (2 s) or the buffer holds {@code flushMaxEntries} (2,000) entries,
 * but it only happens once the previous batch's future has completed; records keep buffering in
 * the meantime. That bounds how far a stream can run ahead of the writer to one batch in flight
 * plus one buffer.
 *
 * <p><strong>By policy.</strong>
 *
 * <ul>
 *   <li>SAMPLED and LATEST keep one entry per {@code externalId}: the greatest {@code observedAt}
 *       wins ({@code receivedAt} under {@link ObservedAtMode#RECEIVED}), and a tie goes to the later
 *       arrival. Those policies only ever persist from the newest state, so the older observations
 *       would be decided against and dropped by the writer anyway.
 *   <li>ALL keeps every record in arrival order, capped at {@code allBufferMax} (50,000). At the cap
 *       {@code accept} blocks for up to {@code acceptBlockTimeout} (30 s), which back-pressures the
 *       adapter, and then drops the record and counts {@code foresignal.stream.records.dropped}.
 *       Nothing is coalesced for an ALL source: every revision is information.
 * </ul>
 *
 * <p><strong>Documented limit.</strong> Under SAMPLED and LATEST, trigger edges and samples have
 * <em>flush resolution</em> for streams (2 s at best, longer while a batch is in flight) and
 * <em>poll resolution</em> for polling sources: a value that crosses a threshold and comes back
 * within one flush window is never seen by the writer.
 *
 * <p>Each flush is one batch and so one {@code fetch_log} row ({@code trigger=STREAM}, written by the
 * writer); the {@link FlushListener} hears the outcome, which the supervisor records in health
 * ({@code last_data_at}).
 */
public class PolicyAwareRecordSink implements RecordSink {

    private static final Logger log = LoggerFactory.getLogger(PolicyAwareRecordSink.class);

    /** The counter of records dropped at the ALL cap or after close. */
    public static final String DROPPED_COUNTER = "foresignal.stream.records.dropped";

    /** Hands a batch to the ingest writer. */
    @FunctionalInterface
    public interface BatchSubmitter {

        /**
         * Submits one batch.
         *
         * @throws IngestBackpressureException when the queue stayed full for {@code timeout}
         */
        CompletableFuture<IngestOutcome> submit(
                String sourceId, FetchLogEntry log, List<IngestRecord> records, Duration timeout);
    }

    /** Hears how each flush ended. */
    public interface FlushListener {

        /** The writer ingested a flush of {@code records} records. */
        void flushed(FetchLogEntry log, int records, IngestOutcome outcome);

        /** A flush of {@code records} records could not be ingested. */
        void failed(FetchLogEntry log, int records, Throwable error);
    }

    private final String sourceId;
    private final SourceDescriptor descriptor;
    private final boolean keyed;
    private final boolean observedIsReceived;
    private final BatchSubmitter submitter;
    private final FlushListener listener;
    private final Clock clock;
    private final CollectorProperties.Stream settings;
    private final String adapterVersion;
    private final Counter dropped;

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition changed = lock.newCondition();
    private final Condition notFull = lock.newCondition();

    private final List<IngestRecord> ordered = new ArrayList<>();
    private final LinkedHashMap<String, IngestRecord> latest = new LinkedHashMap<>();

    private long arrival;
    private long windowStartNanos = -1;
    private Instant windowStartedAt;
    private CompletableFuture<?> outstanding = CompletableFuture.completedFuture(null);
    private boolean closing;
    private boolean finished;
    private Thread flusher;
    private long flushes;

    /**
     * Creates a sink; call {@link #start()} before handing it to an adapter.
     *
     * @param policy the source's effective ingest policy, which picks keyed or ordered buffering
     * @param adapterVersion snapshotted on each flush's {@code fetch_log} row
     */
    public PolicyAwareRecordSink(
            SourceDescriptor descriptor,
            IngestPolicy.PolicyMode policy,
            BatchSubmitter submitter,
            FlushListener listener,
            Clock clock,
            CollectorProperties.Stream settings,
            String adapterVersion,
            MeterRegistry meters) {
        this.sourceId = descriptor.id();
        this.descriptor = descriptor;
        this.keyed = policy == IngestPolicy.PolicyMode.SAMPLED || policy == IngestPolicy.PolicyMode.LATEST;
        this.observedIsReceived = descriptor.observedAtMode() == ObservedAtMode.RECEIVED;
        this.submitter = submitter;
        this.listener = listener;
        this.clock = clock;
        this.settings = settings;
        this.adapterVersion = adapterVersion;
        this.dropped = Counter.builder(DROPPED_COUNTER)
                .description("Stream records dropped at the ALL buffer cap or after the sink closed")
                .tag("source", sourceId)
                .register(meters);
    }

    /** Starts the flusher thread. Idempotent. */
    public void start() {
        lock.lock();
        try {
            if (flusher == null) {
                flusher = Thread.ofVirtual().name("foresignal-sink-" + sourceId).start(this::flushLoop);
            }
        } finally {
            lock.unlock();
        }
    }

    /** True when the buffer is keyed by external id (SAMPLED or LATEST). */
    public boolean isKeyed() {
        return keyed;
    }

    /**
     * Buffers one record, stamping {@code receivedAt}.
     *
     * <p>For an ALL source at the cap this blocks for up to {@code acceptBlockTimeout}, then drops
     * the record. After {@link #flushAndClose} the record is dropped.
     */
    @Override
    public void accept(NormalizedRecord record) {
        Instant receivedAt = clock.instant();
        lock.lock();
        try {
            if (closing) {
                dropped.increment();
                return;
            }
            IngestRecord stamped = new IngestRecord(record, receivedAt, ++arrival);
            if (keyed) {
                merge(stamped);
            } else {
                if (ordered.size() >= settings.allBufferMax() && !awaitRoom()) {
                    dropped.increment();
                    log.warn("{}: stream buffer full at {} records for {}; dropped a record", sourceId,
                            settings.allBufferMax(), settings.acceptBlockTimeout());
                    return;
                }
                ordered.add(stamped);
            }
            boolean windowOpened = false;
            if (windowStartNanos < 0) {
                windowStartNanos = System.nanoTime();
                windowStartedAt = receivedAt;
                windowOpened = true;
            }
            if (windowOpened || size() >= settings.flushMaxEntries()) {
                changed.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }

    /** Waits for room under the cap; false when the wait timed out, the sink closed or was interrupted. */
    private boolean awaitRoom() {
        long nanos = settings.acceptBlockTimeout().toNanos();
        try {
            while (ordered.size() >= settings.allBufferMax() && !closing) {
                if (nanos <= 0) {
                    return false;
                }
                nanos = notFull.awaitNanos(nanos);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        return !closing;
    }

    /** Latest-wins by observed time, ties to the later arrival. Caller holds the lock. */
    private void merge(IngestRecord candidate) {
        latest.merge(candidate.record().externalId(), candidate, (current, incoming) ->
                replaces(incoming, current) ? incoming : current);
    }

    /** True when {@code incoming} should replace {@code current} in a keyed buffer. */
    boolean replaces(IngestRecord incoming, IngestRecord current) {
        Instant a = observedTime(incoming);
        Instant b = observedTime(current);
        int cmp = compareNullsFirst(a, b);
        return cmp > 0 || (cmp == 0 && incoming.arrival() > current.arrival());
    }

    private Instant observedTime(IngestRecord record) {
        return observedIsReceived ? record.receivedAt() : record.record().observedAt();
    }

    private static int compareNullsFirst(Instant a, Instant b) {
        if (a == null) {
            return b == null ? 0 : -1;
        }
        return b == null ? 1 : a.compareTo(b);
    }

    private int size() {
        return keyed ? latest.size() : ordered.size();
    }

    // ------------------------------------------------------------------ flushing

    private void flushLoop() {
        while (true) {
            List<IngestRecord> batch;
            Instant windowStart;
            lock.lock();
            try {
                if (!awaitDue()) {
                    finished = true;
                    changed.signalAll();
                    return;
                }
                batch = drain();
                windowStart = windowStartedAt;
                windowStartNanos = -1;
                windowStartedAt = null;
                notFull.signalAll();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                finished = true;
                changed.signalAll();
                return;
            } finally {
                lock.unlock();
            }
            submit(batch, windowStart);
        }
    }

    /**
     * Waits until a flush is due; false when the sink is closing and everything has been flushed.
     * Caller holds the lock.
     */
    private boolean awaitDue() throws InterruptedException {
        long intervalNanos = settings.flushInterval().toNanos();
        while (true) {
            boolean idle = outstanding.isDone();
            int size = size();
            if (closing && size == 0 && idle) {
                return false;
            }
            if (idle && size > 0) {
                long age = System.nanoTime() - windowStartNanos;
                if (closing || size >= settings.flushMaxEntries() || age >= intervalNanos) {
                    return true;
                }
                changed.awaitNanos(Math.min(intervalNanos - age, intervalNanos));
            } else {
                // Woken by the first record of a window, the size threshold, the outstanding
                // batch completing, or close; the timeout only guards against a lost signal.
                changed.awaitNanos(intervalNanos);
            }
        }
    }

    /** Takes the buffer's contents in arrival order. Caller holds the lock. */
    private List<IngestRecord> drain() {
        List<IngestRecord> batch;
        if (keyed) {
            batch = new ArrayList<>(latest.values());
            batch.sort((x, y) -> Long.compare(x.arrival(), y.arrival()));
            latest.clear();
        } else {
            batch = new ArrayList<>(ordered);
            ordered.clear();
        }
        return batch;
    }

    private void submit(List<IngestRecord> batch, Instant windowStart) {
        FetchLogEntry entry = new FetchLogEntry(
                sourceId,
                FetchTrigger.STREAM,
                FetchStatus.OK,
                windowStart == null ? clock.instant() : windowStart,
                clock.instant(),
                null,
                0,
                null,
                null,
                null,
                null,
                adapterVersion,
                descriptor.licence(),
                null,
                Coverage.none(),
                null);
        CompletableFuture<IngestOutcome> future;
        try {
            future = submitter.submit(sourceId, entry, batch, settings.submitTimeout());
        } catch (IngestBackpressureException e) {
            log.warn("{}: the ingest queue is full; keeping {} stream records buffered", sourceId, batch.size());
            restore(batch);
            // Wait one interval before trying again rather than spinning against a full queue.
            setOutstanding(CompletableFuture.runAsync(
                    () -> {}, CompletableFuture.delayedExecutor(settings.flushInterval().toMillis(), TimeUnit.MILLISECONDS)));
            return;
        } catch (RuntimeException e) {
            notifyFailed(entry, batch.size(), e);
            return;
        }
        setOutstanding(future);
        future.whenComplete((outcome, error) -> {
            if (error == null) {
                try {
                    listener.flushed(entry, batch.size(), outcome);
                } catch (RuntimeException e) {
                    log.warn("{}: the flush listener failed", sourceId, e);
                }
            } else {
                notifyFailed(entry, batch.size(), error);
            }
        });
    }

    private void notifyFailed(FetchLogEntry entry, int records, Throwable error) {
        try {
            listener.failed(entry, records, error);
        } catch (RuntimeException e) {
            log.warn("{}: the flush listener failed", sourceId, e);
        }
    }

    /** Makes {@code future} the outstanding batch and wakes the flusher when it completes. */
    private void setOutstanding(CompletableFuture<?> future) {
        lock.lock();
        try {
            outstanding = future;
            flushes++;
        } finally {
            lock.unlock();
        }
        future.whenComplete((value, error) -> {
            lock.lock();
            try {
                changed.signalAll();
            } finally {
                lock.unlock();
            }
        });
    }

    /** Puts a batch the queue refused back in front of anything buffered since. */
    private void restore(List<IngestRecord> batch) {
        lock.lock();
        try {
            if (keyed) {
                for (IngestRecord record : batch) {
                    merge(record);
                }
            } else {
                ordered.addAll(0, batch);
            }
            if (windowStartNanos < 0 && !batch.isEmpty()) {
                windowStartNanos = System.nanoTime() - settings.flushInterval().toNanos();
                windowStartedAt = batch.get(0).receivedAt();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Stops accepting, flushes everything buffered (still one batch at a time) and waits for the
     * last batch.
     *
     * @return true when everything was flushed and acknowledged within {@code timeout}
     */
    public boolean flushAndClose(Duration timeout) {
        long deadline = System.nanoTime() + timeout.toNanos();
        Thread thread;
        lock.lock();
        try {
            closing = true;
            changed.signalAll();
            notFull.signalAll();
            thread = flusher;
        } finally {
            lock.unlock();
        }
        if (thread == null) {
            // Never started: flush inline so nothing buffered is lost.
            lock.lock();
            try {
                flusher = Thread.currentThread();
            } finally {
                lock.unlock();
            }
            flushLoop();
        } else {
            try {
                thread.join(Math.max(1, TimeUnit.NANOSECONDS.toMillis(deadline - System.nanoTime())));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        CompletableFuture<?> last;
        lock.lock();
        try {
            last = outstanding;
        } finally {
            lock.unlock();
        }
        try {
            last.get(Math.max(1, TimeUnit.NANOSECONDS.toMillis(deadline - System.nanoTime())), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            // A failed last batch has been reported to the listener; what matters here is that it
            // is no longer outstanding.
            return isFinished();
        }
        return isFinished();
    }

    /** True once the sink has closed and flushed everything. */
    public boolean isFinished() {
        lock.lock();
        try {
            return finished && size() == 0;
        } finally {
            lock.unlock();
        }
    }

    /** Entries currently buffered. */
    public int buffered() {
        lock.lock();
        try {
            return size();
        } finally {
            lock.unlock();
        }
    }

    /** True while a batch is in flight. */
    public boolean hasOutstanding() {
        lock.lock();
        try {
            return !outstanding.isDone();
        } finally {
            lock.unlock();
        }
    }

    /** Batches handed to the submitter so far, including refused ones. */
    public long flushes() {
        lock.lock();
        try {
            return flushes;
        } finally {
            lock.unlock();
        }
    }

    /** Records dropped so far. */
    public long droppedCount() {
        return (long) dropped.count();
    }

    /** A view of the keyed buffer, for tests. */
    Map<String, IngestRecord> keyedSnapshot() {
        lock.lock();
        try {
            return Map.copyOf(latest);
        } finally {
            lock.unlock();
        }
    }
}
