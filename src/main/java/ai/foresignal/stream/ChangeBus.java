package ai.foresignal.stream;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Fans committed change batches out to in-process subscribers (Core-Data-Model §10.1).
 *
 * <p>The ingest writer publishes one {@link ChangeBatch} per committed batch that persisted at
 * least one version, in commit order (which with a single writer is seq order). Subscribers are
 * the SSE hub (prompt 015), the trigger engine (017) and the embedding worker (027).
 *
 * <p><strong>{@link #publish} never blocks.</strong> Each subscriber has its own bounded queue and
 * its own virtual delivery thread, so a slow subscriber delays nobody else. When a subscriber's
 * queue is full the batch is dropped for that subscriber and it is marked <em>lagging</em>: every
 * further batch is dropped too until its queue drains, and then {@link ChangeListener#onGap} is
 * called once with the last seq it received, so it can catch up from the database. Dropping
 * everything while lagging keeps delivery in order: a subscriber never sees a batch from after the
 * gap before it has been told about the gap.
 *
 * <p>{@link #lastPublishedSeq()} is the watermark read models use (prompt 014's
 * {@code watermarkSeq}): every version with a seq at or below it has been published. It starts at
 * {@code max(record_version.seq)} and the writer resets it whenever it reloads its state.
 */
@Component
public class ChangeBus {

    private static final Logger log = LoggerFactory.getLogger(ChangeBus.class);

    /** Counts batches dropped for a lagging subscriber, tagged {@code subscriber}. */
    public static final String DROPPED_METRIC = "foresignal.stream.change.dropped";

    private static final long UNSET = Long.MIN_VALUE;

    private final List<ChangeSubscription> subscriptions = new CopyOnWriteArrayList<>();
    private final AtomicLong watermark = new AtomicLong(UNSET);
    private final LongSupplier initialWatermark;
    private final MeterRegistry meters;

    /** The bus, with the watermark initialized lazily from {@code record_version}. */
    @Autowired
    public ChangeBus(JdbcTemplate jdbc, MeterRegistry meters) {
        this(
                () -> {
                    Long max = jdbc.queryForObject("SELECT coalesce(max(seq), 0) FROM record_version", Long.class);
                    return max == null ? 0L : max;
                },
                meters);
    }

    /**
     * A bus whose initial watermark comes from {@code initialWatermark}, read on first use.
     *
     * @param initialWatermark supplies the starting watermark
     * @param meters where drop counters are registered
     */
    public ChangeBus(LongSupplier initialWatermark, MeterRegistry meters) {
        this.initialWatermark = Objects.requireNonNull(initialWatermark, "initialWatermark");
        this.meters = Objects.requireNonNull(meters, "meters");
    }

    /**
     * Subscribes {@code listener}.
     *
     * @param name the subscriber's name, for its thread and metrics
     * @param listener receives batches and gaps on its own virtual thread
     * @param queueCapacity how many batches may wait for the listener before it is lagging
     * @return the subscription; close it to unsubscribe
     */
    public Subscription subscribe(String name, ChangeListener listener, int queueCapacity) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(listener, "listener");
        if (queueCapacity < 1) {
            throw new IllegalArgumentException("queueCapacity must be at least 1, got " + queueCapacity);
        }
        ChangeSubscription subscription = new ChangeSubscription(name, listener, queueCapacity, lastPublishedSeq());
        subscriptions.add(subscription);
        subscription.start();
        return subscription;
    }

    /**
     * Publishes {@code batch} to every subscriber and advances the watermark. Never blocks.
     *
     * @param batch a committed batch
     */
    public void publish(ChangeBatch batch) {
        Objects.requireNonNull(batch, "batch");
        lastPublishedSeq();
        watermark.accumulateAndGet(batch.toSeq(), Math::max);
        for (ChangeSubscription subscription : subscriptions) {
            subscription.offer(batch);
        }
    }

    /** Every seq at or below this has been published (or was committed before the bus started). */
    public long lastPublishedSeq() {
        long current = watermark.get();
        if (current != UNSET) {
            return current;
        }
        long initial = initialWatermark.getAsLong();
        watermark.compareAndSet(UNSET, initial);
        return watermark.get();
    }

    /**
     * Sets the watermark, for example after the history tables were truncated.
     *
     * @param seq the new watermark
     */
    public void resetWatermark(long seq) {
        watermark.set(seq);
    }

    /** The number of current subscribers. */
    public int subscriberCount() {
        return subscriptions.size();
    }

    private final class ChangeSubscription implements Subscription {

        private final String name;
        private final ChangeListener listener;
        private final BlockingQueue<ChangeBatch> queue;
        private final Counter dropped;
        private final Object monitor = new Object();
        private volatile boolean closed;
        private boolean lagging;
        private long lastDeliveredSeq;
        private Thread thread;

        ChangeSubscription(String name, ChangeListener listener, int capacity, long startSeq) {
            this.name = name;
            this.listener = listener;
            this.queue = new ArrayBlockingQueue<>(capacity);
            this.lastDeliveredSeq = startSeq;
            this.dropped = Counter.builder(DROPPED_METRIC).tag("subscriber", name).register(meters);
        }

        void start() {
            thread = Thread.ofVirtual().name("changebus-" + name).start(this::deliver);
        }

        void offer(ChangeBatch batch) {
            if (closed) {
                return;
            }
            synchronized (monitor) {
                if (lagging || !queue.offer(batch)) {
                    if (!lagging) {
                        log.warn("Change subscriber {} is lagging; dropping batches until it catches up", name);
                    }
                    lagging = true;
                    dropped.increment();
                }
            }
        }

        private void deliver() {
            while (!closed) {
                ChangeBatch batch;
                try {
                    batch = queue.poll(250, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    if (closed) {
                        return;
                    }
                    continue;
                }
                if (batch != null) {
                    try {
                        listener.onBatch(batch);
                    } catch (RuntimeException e) {
                        log.error("Change subscriber {} failed on batch {}..{}", name, batch.fromSeq(), batch.toSeq(), e);
                    }
                    lastDeliveredSeq = Math.max(lastDeliveredSeq, batch.toSeq());
                }
                long gapAt = Long.MIN_VALUE;
                synchronized (monitor) {
                    if (lagging && queue.isEmpty()) {
                        lagging = false;
                        gapAt = lastDeliveredSeq;
                    }
                }
                if (gapAt != Long.MIN_VALUE && !closed) {
                    try {
                        listener.onGap(gapAt);
                    } catch (RuntimeException e) {
                        log.error("Change subscriber {} failed handling a gap after seq {}", name, gapAt, e);
                    }
                }
            }
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;
            subscriptions.remove(this);
            queue.clear();
            if (thread != null) {
                thread.interrupt();
            }
        }
    }
}
