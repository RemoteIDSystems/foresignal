package ai.foresignal.analytics.finding;

import ai.foresignal.stream.Subscription;
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
 * Fans finding stream events out to in-process listeners (prompt 119), the pattern of {@code IntelEventBus}:
 * {@link #publish} never blocks, each listener has its own bounded queue and virtual delivery thread, and a lagging
 * listener is told about the gap once it catches up.
 */
@Component
public class FindingEventBus {

    private static final Logger log = LoggerFactory.getLogger(FindingEventBus.class);

    /** Counts events dropped for a lagging listener. */
    public static final String DROPPED_METRIC = "foresignal.analytics.bus.dropped";

    /** The default queue size per listener. */
    public static final int DEFAULT_QUEUE = 1_024;

    private final List<Listener> listeners = new CopyOnWriteArrayList<>();
    private final MeterRegistry meters;

    /** Creates the bus. */
    public FindingEventBus(MeterRegistry meters) {
        this.meters = Objects.requireNonNull(meters, "meters");
    }

    /** Subscribes {@code listener}. */
    public Subscription subscribe(String name, FindingListener listener, int queueCapacity) {
        Listener subscription = new Listener(name, listener, Math.max(1, queueCapacity));
        listeners.add(subscription);
        subscription.start();
        return subscription;
    }

    /** Publishes {@code event} to every listener. Never blocks. */
    public void publish(FindingEvent event) {
        Objects.requireNonNull(event, "event");
        for (Listener listener : listeners) {
            listener.offer(event);
        }
    }

    private final class Listener implements Subscription {

        private final String name;
        private final FindingListener target;
        private final BlockingQueue<FindingEvent> queue;
        private final Counter dropped;
        private final Object monitor = new Object();
        private volatile boolean closed;
        private boolean lagging;
        private Thread thread;

        Listener(String name, FindingListener target, int capacity) {
            this.name = name;
            this.target = target;
            this.queue = new ArrayBlockingQueue<>(capacity);
            this.dropped = Counter.builder(DROPPED_METRIC).tag("subscriber", name).register(meters);
        }

        void start() {
            thread = Thread.ofVirtual().name("findingbus-" + name).start(this::deliver);
        }

        void offer(FindingEvent event) {
            if (closed) {
                return;
            }
            synchronized (monitor) {
                if (lagging || !queue.offer(event)) {
                    lagging = true;
                    dropped.increment();
                }
            }
        }

        private void deliver() {
            while (!closed) {
                FindingEvent event;
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
                        target.onEvent(event);
                    } catch (RuntimeException e) {
                        log.error("Finding listener {} failed on {}", name, event.findingId(), e);
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
                    target.onGap();
                }
            }
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public void close() {
            closed = true;
            listeners.remove(this);
            queue.clear();
            if (thread != null) {
                thread.interrupt();
            }
        }
    }
}
