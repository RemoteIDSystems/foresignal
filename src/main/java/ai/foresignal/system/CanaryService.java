package ai.foresignal.system;

import ai.foresignal.config.StartupOrder;
import ai.foresignal.ingest.FetchLogEntry;
import ai.foresignal.ingest.IngestQueue;
import ai.foresignal.ingest.IngestRecord;
import ai.foresignal.notify.SystemEvent;
import ai.foresignal.source.adapters.system.SystemCanaryAdapter;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.store.FetchTrigger;
import ai.foresignal.stream.ChangeBatch;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.stream.ChangeListener;
import ai.foresignal.stream.Subscription;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PreDestroy;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * The opt-in ingest canary (prompt 019): submits one synthetic record for {@code system-canary}
 * through the real ingest queue and waits up to 30 s for its change on the {@link ChangeBus},
 * recording {@code foresignal.canary.latency}. A failure publishes a {@code system.canary_failed}
 * notification and makes the watchdog ping its fail URL until the next success.
 */
@Component
public class CanaryService implements ChangeListener {

    private static final Logger log = LoggerFactory.getLogger(CanaryService.class);

    /** Latency timer. */
    public static final String LATENCY_METRIC = "foresignal.canary.latency";

    private final IngestQueue queue;
    private final ChangeBus changeBus;
    private final ApplicationEventPublisher events;
    private final WatchdogProperties properties;
    private final Clock clock;
    private final Timer latency;
    private final AtomicLong arrival = new AtomicLong();
    private volatile CompletableFuture<Long> pending;
    private volatile Subscription subscription;
    private volatile Boolean lastOk;
    private volatile Instant lastRunAt;

    /** Creates the service. */
    public CanaryService(IngestQueue queue, ChangeBus changeBus, ApplicationEventPublisher events,
            WatchdogProperties properties, MeterRegistry meters, Clock clock) {
        this.queue = queue;
        this.changeBus = changeBus;
        this.events = events;
        this.properties = properties;
        this.clock = clock;
        this.latency = Timer.builder(LATENCY_METRIC).description("Ingest canary round trip").register(meters);
    }

    /** Subscribes to the change bus. */
    @EventListener(ApplicationReadyEvent.class)
    @Order(StartupOrder.CANARY)
    public synchronized void start() {
        if (subscription == null) {
            subscription = changeBus.subscribe("canary", this, 16);
        }
    }

    /** Unsubscribes. */
    @PreDestroy
    public synchronized void stop() {
        if (subscription != null) {
            subscription.close();
            subscription = null;
        }
    }

    @Override
    public void onBatch(ChangeBatch batch) {
        CompletableFuture<Long> waiting = pending;
        if (waiting != null && SystemCanaryAdapter.ID.equals(batch.sourceId())) {
            waiting.complete(batch.toSeq());
        }
    }

    @Override
    public void onGap(long lastDeliveredSeq) {
        // A gap may have swallowed the canary's change; the run then times out and reports failure.
    }

    /** The last outcome: true, false, or null before the first run. */
    public Boolean lastOk() {
        return lastOk;
    }

    /** When the canary last ran, or null. */
    public Instant lastRunAt() {
        return lastRunAt;
    }

    /** Forgets the last outcome (the canary was disabled). */
    public void reset() {
        lastOk = null;
    }

    /**
     * Runs the canary once and waits for its change.
     *
     * @return true when the change arrived within the wait
     */
    public synchronized boolean runOnce() {
        start();
        Instant now = clock.instant();
        lastRunAt = now;
        long started = System.nanoTime();
        CompletableFuture<Long> waiting = new CompletableFuture<>();
        pending = waiting;
        try {
            NormalizedRecord record = NormalizedRecord.builder(SystemCanaryAdapter.EXTERNAL_ID, SystemCanaryAdapter.KIND)
                    .observedAt(now)
                    .put("nonce", UUID.randomUUID().toString())
                    .put("ranAt", now.toString())
                    .build();
            queue.submit(SystemCanaryAdapter.ID, FetchLogEntry.ok(SystemCanaryAdapter.ID, FetchTrigger.PUSH, now, now),
                    List.of(new IngestRecord(record, now, arrival.incrementAndGet())), properties.canaryWait());
            waiting.get(properties.canaryWait().toMillis(), TimeUnit.MILLISECONDS);
            latency.record(System.nanoTime() - started, TimeUnit.NANOSECONDS);
            lastOk = true;
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return fail("interrupted");
        } catch (Exception e) {
            return fail(e.getClass().getSimpleName() + (e.getMessage() == null ? "" : ": " + e.getMessage()));
        } finally {
            pending = null;
        }
    }

    private boolean fail(String reason) {
        lastOk = false;
        log.warn("The ingest canary failed: {}", reason);
        try {
            events.publishEvent(new SystemEvent("system.canary_failed", "critical", "The ingest canary failed",
                    "A synthetic record did not reach the change stream within "
                            + properties.canaryWait().toSeconds() + " s.",
                    "/v1/system", Map.of("reason", reason)));
        } catch (RuntimeException e) {
            log.warn("Could not publish the canary notification", e);
        }
        return false;
    }

    /** The configured wait. */
    public Duration canaryWait() {
        return properties.canaryWait();
    }
}
