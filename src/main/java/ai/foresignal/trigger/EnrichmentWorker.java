package ai.foresignal.trigger;

import ai.foresignal.config.StartupOrder;
import ai.foresignal.stream.Subscription;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Enriches alerts as they fire (prompt 030, Architecture §23): an {@link AlertBus} listener in front
 * of a bounded queue ({@link EnrichmentProperties#queueCapacity()}, 256) drained by a small pool of
 * virtual threads ({@link EnrichmentProperties#concurrency()}, 4).
 *
 * <p>Only {@link AlertEvent#FIRED} events of unsuppressed alerts are considered; each is admitted by
 * {@link EnrichmentService#admit} (the rule's {@code enrich} flag, {@code llm.enrichmentEnabled}, a
 * chat provider) before it takes a queue slot. When the queue is full the alert is dropped and
 * {@value #DROPPED_METRIC} counted: it is delivered without a summary. Enrichment never delays
 * delivery beyond each channel's {@code wait_for_enrichment_seconds}; it runs beside the notification
 * router, never in front of it.
 */
@Component
public class EnrichmentWorker implements AlertListener {

    private static final Logger log = LoggerFactory.getLogger(EnrichmentWorker.class);

    /** Alerts dropped because the queue was full. */
    public static final String DROPPED_METRIC = "foresignal.enrichment.dropped";

    /** Reads exercise alert contexts (prompt 115). */
    private static final tools.jackson.databind.json.JsonMapper EXERCISE_JSON =
            tools.jackson.databind.json.JsonMapper.builder().build();

    private final AlertBus bus;
    private final EnrichmentService service;
    private final EnrichmentProperties properties;
    private final BlockingQueue<AlertView> queue;
    private final Counter dropped;
    private final AtomicInteger busy = new AtomicInteger();
    private final List<Thread> threads = new ArrayList<>();
    private volatile Subscription subscription;
    private volatile boolean running;

    /** Creates the worker. */
    public EnrichmentWorker(AlertBus bus, EnrichmentService service, EnrichmentProperties properties,
            MeterRegistry meters) {
        this.bus = bus;
        this.service = service;
        this.properties = properties;
        this.queue = new ArrayBlockingQueue<>(properties.queueCapacity());
        this.dropped = Counter.builder(DROPPED_METRIC).register(meters);
    }

    /**
     * Starts the worker at startup, before the trigger engine fires alerts, unless
     * {@code foresignal.enrichment.auto-start} is off (tests).
     */
    @EventListener(ApplicationReadyEvent.class)
    @Order(StartupOrder.ENRICHMENT)
    public void onReady() {
        if (properties.autoStart()) {
            start();
        }
    }

    /** Starts the pool and subscribes to the alert bus; idempotent. */
    public synchronized void start() {
        if (running) {
            return;
        }
        running = true;
        for (int i = 0; i < properties.concurrency(); i++) {
            threads.add(Thread.ofVirtual().name("enrichment-" + i).start(this::drain));
        }
        subscription = bus.subscribe("enrichment", this, AlertBus.DEFAULT_QUEUE);
    }

    /** True while the worker is subscribed. */
    public boolean running() {
        return running;
    }

    /** Unsubscribes and stops the pool; queued alerts are abandoned. */
    @PreDestroy
    public synchronized void stop() {
        running = false;
        if (subscription != null) {
            subscription.close();
            subscription = null;
        }
        for (Thread thread : threads) {
            thread.interrupt();
        }
        threads.clear();
        queue.clear();
    }

    @Override
    public void onAlert(AlertEvent event) {
        if (!AlertEvent.FIRED.equals(event.type()) || event.suppressed()) {
            return;
        }
        if (event.alert().exercise() && !ai.foresignal.exercise.ExerciseMarking.enrichAllowed(event.alert(), EXERCISE_JSON)) {
            // Exercise alerts (prompt 115) are enriched only when their run set enrich=true.
            return;
        }
        submit(event.alert());
    }

    @Override
    public void onGap() {
        log.warn("The enrichment worker missed alert events (its bus queue overflowed); those alerts are not enriched");
    }

    /**
     * Admits and queues {@code alert}.
     *
     * @return true when it was queued; false when it was not admitted or the queue was full
     */
    public boolean submit(AlertView alert) {
        if (!service.admit(alert)) {
            return false;
        }
        if (!queue.offer(alert)) {
            dropped.increment();
            log.warn("Enrichment queue full ({}): alert {} is delivered without a summary", properties.queueCapacity(),
                    alert.id());
            return false;
        }
        return true;
    }

    /** Alerts waiting plus alerts being enriched. */
    public int backlog() {
        return queue.size() + busy.get();
    }

    private void drain() {
        while (running) {
            AlertView alert;
            try {
                alert = queue.poll(250, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                if (!running) {
                    return;
                }
                continue;
            }
            if (alert == null) {
                continue;
            }
            busy.incrementAndGet();
            try {
                service.run(alert);
            } catch (RuntimeException e) {
                log.error("Enrichment of alert {} failed unexpectedly", alert.id(), e);
            } finally {
                busy.decrementAndGet();
            }
        }
    }
}
