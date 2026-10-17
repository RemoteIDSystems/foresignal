package ai.foresignal.trigger;

import ai.foresignal.stream.Subscription;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PreDestroy;
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
 * Fans alert lifecycle events out to in-process listeners (prompt 017): the alert stream now,
 * notification channels (018) and enrichment (030) later.
 *
 * <p>The same pattern as the {@code ChangeBus}: {@link #publish} never blocks; each listener has its
 * own bounded queue and virtual delivery thread, so a slow listener delays only itself. When a queue
 * is full the listener is marked lagging and drops everything until its queue drains, and then
 * {@link AlertListener#onGap} is called once, so it never sees an event from after the gap before it
 * has been told about the gap.
 */
@Component
public class AlertBus {

    private static final Logger log = LoggerFactory.getLogger(AlertBus.class);

    /** Counts events dropped for a lagging listener, tagged {@code subscriber}. */
    public static final String DROPPED_METRIC = "foresignal.alerts.bus.dropped";

    /** The default queue size per listener. */
    public static final int DEFAULT_QUEUE = 1_024;

    private final List<Listener> listeners = new CopyOnWriteArrayList<>();
    private final MeterRegistry meters;

    /** Creates the bus. */
    public AlertBus(MeterRegistry meters) {
        this.meters = Objects.requireNonNull(meters, "meters");
    }

    /**
     * Subscribes {@code listener}.
     *
     * @param name the listener's name, for its thread and metrics
     * @param listener receives events on its own virtual thread
     * @param queueCapacity how many events may wait before it is lagging
     * @return the subscription; close it to unsubscribe
     */
    public Subscription subscribe(String name, AlertListener listener, int queueCapacity) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(listener, "listener");
        if (queueCapacity < 1) {
            throw new IllegalArgumentException("queueCapacity must be at least 1, got " + queueCapacity);
        }
        Listener subscription = new Listener(name, listener, queueCapacity);
        listeners.add(subscription);
        subscription.start();
        return subscription;
    }

    /** Publishes {@code event} to every listener. Never blocks. */
    public void publish(AlertEvent event) {
        Objects.requireNonNull(event, "event");
        for (Listener listener : listeners) {
            listener.offer(event);
        }
    }

    /** The number of current listeners. */
    public int listenerCount() {
        return listeners.size();
    }

    /**
     * Closes every remaining listener and stops its delivery thread when the application context closes. A
     * delivery thread holds its listener, and through it the listener's beans, so one left running would keep a closed
     * context reachable (checkpoint 124: evicted test contexts accumulated until the heap was exhausted).
     */
    @PreDestroy
    public void closeAll() {
        for (Listener listener : listeners) {
            listener.close();
        }
    }

    private final class Listener implements Subscription {

        private final String name;
        private final AlertListener target;
        private final BlockingQueue<AlertEvent> queue;
        private final Counter dropped;
        private final Object monitor = new Object();
        private volatile boolean closed;
        private boolean lagging;
        private Thread thread;

        Listener(String name, AlertListener target, int capacity) {
            this.name = name;
            this.target = target;
            this.queue = new ArrayBlockingQueue<>(capacity);
            this.dropped = Counter.builder(DROPPED_METRIC).tag("subscriber", name).register(meters);
        }

        void start() {
            thread = Thread.ofVirtual().name("alertbus-" + name).start(this::deliver);
        }

        void offer(AlertEvent event) {
            if (closed) {
                return;
            }
            synchronized (monitor) {
                if (lagging || !queue.offer(event)) {
                    if (!lagging) {
                        log.warn("Alert listener {} is lagging; dropping events until it catches up", name);
                    }
                    lagging = true;
                    dropped.increment();
                }
            }
        }

        private void deliver() {
            while (!closed) {
                AlertEvent event;
                try {
                    event = queue.poll(250, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    if (closed) {
                        return;
                    }
                    continue;
                }
                if (event != null) {
                    try {
                        target.onAlert(event);
                    } catch (RuntimeException e) {
                        log.error("Alert listener {} failed on alert {}", name, event.alert().id(), e);
                    }
                }
                boolean gap = false;
                synchronized (monitor) {
                    if (lagging && queue.isEmpty()) {
                        lagging = false;
                        gap = true;
                    }
                }
                if (gap && !closed) {
                    try {
                        target.onGap();
                    } catch (RuntimeException e) {
                        log.error("Alert listener {} failed handling a gap", name, e);
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
            listeners.remove(this);
            queue.clear();
            if (thread != null) {
                thread.interrupt();
            }
        }
    }
}
