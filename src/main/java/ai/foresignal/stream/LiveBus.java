package ai.foresignal.stream;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Fans live updates and touches out to in-process subscribers (Core-Data-Model §10.1).
 *
 * <p>The writer publishes a {@link LiveBatch} after the batch's {@link ChangeBatch}, whenever the
 * batch wrote live updates or touches. {@link #publish} never blocks: each subscriber has a bounded
 * queue and its own virtual delivery thread. Live state is conflated rather than replayed, so on
 * overflow the batch is simply dropped for that subscriber, counted as
 * {@code foresignal.stream.live.dropped{subscriber}}, and the subscriber's
 * {@link LiveListener#onLiveDropped()} is called (once per overflow episode, after the queued
 * batches) so it can resync.
 */
@Component
public class LiveBus {

    private static final Logger log = LoggerFactory.getLogger(LiveBus.class);

    /** Counts live batches dropped for a full subscriber, tagged {@code subscriber}. */
    public static final String DROPPED_METRIC = "foresignal.stream.live.dropped";

    private final List<LiveSubscription> subscriptions = new CopyOnWriteArrayList<>();
    private final MeterRegistry meters;

    /** Creates the bus. */
    public LiveBus(MeterRegistry meters) {
        this.meters = Objects.requireNonNull(meters, "meters");
    }

    /**
     * Subscribes {@code listener}.
     *
     * @param name the subscriber's name, for its thread and metrics
     * @param listener receives batches on its own virtual thread
     * @param capacity how many batches may wait before further ones are dropped
     * @return the subscription; close it to unsubscribe
     */
    public Subscription subscribe(String name, LiveListener listener, int capacity) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(listener, "listener");
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be at least 1, got " + capacity);
        }
        LiveSubscription subscription = new LiveSubscription(name, listener, capacity);
        subscriptions.add(subscription);
        subscription.start();
        return subscription;
    }

    /** Publishes {@code batch} to every subscriber. Never blocks. */
    public void publish(LiveBatch batch) {
        Objects.requireNonNull(batch, "batch");
        for (LiveSubscription subscription : subscriptions) {
            subscription.offer(batch);
        }
    }

    /** The number of current subscribers. */
    public int subscriberCount() {
        return subscriptions.size();
    }

    private final class LiveSubscription implements Subscription {

        private final String name;
        private final LiveListener listener;
        private final BlockingQueue<LiveBatch> queue;
        private final Counter dropped;
        private volatile boolean closed;
        private volatile boolean dropPending;
        private Thread thread;

        LiveSubscription(String name, LiveListener listener, int capacity) {
            this.name = name;
            this.listener = listener;
            this.queue = new ArrayBlockingQueue<>(capacity);
            this.dropped = Counter.builder(DROPPED_METRIC).tag("subscriber", name).register(meters);
        }

        void start() {
            thread = Thread.ofVirtual().name("livebus-" + name).start(this::deliver);
        }

        void offer(LiveBatch batch) {
            if (closed) {
                return;
            }
            if (!queue.offer(batch)) {
                dropped.increment();
                dropPending = true;
            }
        }

        private void deliver() {
            while (!closed) {
                LiveBatch batch;
                try {
                    batch = queue.poll(100, TimeUnit.MILLISECONDS);
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
                        log.error("Live subscriber {} failed on a batch of {}", name, batch.sourceId(), e);
                    }
                }
                if (dropPending && queue.isEmpty() && !closed) {
                    dropPending = false;
                    try {
                        listener.onLiveDropped();
                    } catch (RuntimeException e) {
                        log.error("Live subscriber {} failed handling dropped live batches", name, e);
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
