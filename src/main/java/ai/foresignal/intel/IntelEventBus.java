package ai.foresignal.intel;

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
 * Fans intelligence-event stream events out to in-process listeners (prompt 101), the same pattern as
 * {@code AlertBus}: {@link #publish} never blocks, each listener has its own bounded queue and virtual
 * delivery thread, and a lagging listener is told about the gap once it catches up.
 */
@Component
public class IntelEventBus {

    private static final Logger log = LoggerFactory.getLogger(IntelEventBus.class);

    /** Counts events dropped for a lagging listener. */
    public static final String DROPPED_METRIC = "foresignal.intel.bus.dropped";

    /** The default queue size per listener. */
    public static final int DEFAULT_QUEUE = 1_024;

    private final List<Listener> listeners = new CopyOnWriteArrayList<>();
    private final MeterRegistry meters;

    /** Creates the bus. */
    public IntelEventBus(MeterRegistry meters) {
        this.meters = Objects.requireNonNull(meters, "meters");
    }

    /** Subscribes {@code listener}. */
    public Subscription subscribe(String name, IntelListener listener, int queueCapacity) {
        Listener subscription = new Listener(name, listener, Math.max(1, queueCapacity));
        listeners.add(subscription);
        subscription.start();
        return subscription;
    }

    /** Publishes {@code event} to every listener. Never blocks. */
    public void publish(IntelEvent event) {
        Objects.requireNonNull(event, "event");
        for (Listener listener : listeners) {
            listener.offer(event);
        }
    }

    /** The number of current listeners. */
    public int listenerCount() {
        return listeners.size();
    }

    private final class Listener implements Subscription {

        private final String name;
        private final IntelListener target;
        private final BlockingQueue<IntelEvent> queue;
        private final Counter dropped;
        private final Object monitor = new Object();
        private volatile boolean closed;
        private boolean lagging;
        private Thread thread;

        Listener(String name, IntelListener target, int capacity) {
            this.name = name;
            this.target = target;
            this.queue = new ArrayBlockingQueue<>(capacity);
            this.dropped = Counter.builder(DROPPED_METRIC).tag("subscriber", name).register(meters);
        }

        void start() {
            thread = Thread.ofVirtual().name("intelbus-" + name).start(this::deliver);
        }

        void offer(IntelEvent event) {
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
                IntelEvent event;
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
                        log.error("Intel listener {} failed on event {}", name, event.eventId(), e);
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
