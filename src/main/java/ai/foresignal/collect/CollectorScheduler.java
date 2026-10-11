package ai.foresignal.collect;

import ai.foresignal.config.StartupOrder;
import ai.foresignal.source.CredentialsAccess;
import ai.foresignal.source.SourceCredentialsChangedEvent;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceRegistryChangedEvent;
import ai.foresignal.source.SourceSettingsChangedEvent;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.StreamingAdapter;
import ai.foresignal.store.FetchTrigger;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Connects subscription to collection (Architecture §5.1; Product Charter FR-4, G1, NFR-6).
 *
 * <p><strong>The only things that cause an upstream request are an enabled source, a manual fetch,
 * or a probe.</strong> This scheduler polls exactly the enabled POLLING sources and hands the
 * enabled STREAMING sources to the {@link StreamingSupervisor}. PUSH sources receive data through
 * prompt 024's inbound endpoint and DERIVED sources are run by prompt 052's runtime; neither is
 * polled nor streamed here. The other two paths are {@link ManualFetchService} and
 * {@link ProbeService}, both operator-initiated.
 *
 * <p><strong>Timing.</strong> One platform thread drives all timing; every fetch runs on a virtual
 * thread. {@link #reconcile()} compares the enabled sources in {@code source_settings} with the
 * running set every 2 seconds, and immediately ({@link #reconcileNow()}) when settings, credentials
 * or the registry change, so an enable or disable applies within seconds:
 *
 * <ul>
 *   <li>newly enabled: the first run is immediate, then a fixed delay of {@code interval} measured
 *       from the end of the previous run;
 *   <li>interval changed: rescheduled;
 *   <li>disabled: the future is canceled; a run already in flight finishes, and the
 *       {@link FetchExecutor} records its result as DISCARDED.
 * </ul>
 *
 * <p>A source enabled while missing a required credential (credentials deleted after enabling) is
 * not scheduled until they are stored again. A run while the source is in backoff is skipped,
 * logged once per backoff window.
 *
 * <p><strong>Lifecycle.</strong> Starts on {@code ApplicationReadyEvent} at
 * {@link StartupOrder#SCHEDULER}, after the ingest writer and the trigger engine, and only when
 * {@code foresignal.collector.scheduled} is true. On shutdown, as a {@link SmartLifecycle} it stops
 * before any singleton is destroyed: it stops scheduling, waits up to {@code shutdownWait} (30 s)
 * for in-flight fetches, and flushes the streaming sinks; only then does the writer drain its queue.
 */
@Component
public class CollectorScheduler implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(CollectorScheduler.class);

    private final SourceRegistry registry;
    private final FetchExecutor executor;
    private final StreamingSupervisor supervisor;
    private final CredentialsAccess credentials;
    private final BackoffRegistry backoff;
    private final JdbcTemplate jdbc;
    private final CollectorProperties properties;

    /** Scheduled polling sources; mutated only on the timer thread. */
    private final Map<String, Entry> entries = new ConcurrentHashMap<>();

    /** The backoff deadline last logged per source, so a backoff window is logged once. */
    private final Map<String, Instant> backoffLogged = new ConcurrentHashMap<>();

    private final AtomicInteger inFlight = new AtomicInteger();

    private volatile boolean running;
    private ScheduledExecutorService timer;
    private ExecutorService fetchers;
    private ScheduledFuture<?> reconcileTask;

    /** Creates the scheduler. */
    public CollectorScheduler(
            SourceRegistry registry,
            FetchExecutor executor,
            StreamingSupervisor supervisor,
            CredentialsAccess credentials,
            BackoffRegistry backoff,
            JdbcTemplate jdbc,
            CollectorProperties properties) {
        this.registry = registry;
        this.executor = executor;
        this.supervisor = supervisor;
        this.credentials = credentials;
        this.backoff = backoff;
        this.jdbc = jdbc;
        this.properties = properties;
    }

    /** One scheduled polling source. */
    private static final class Entry {
        final String sourceId;
        volatile Duration interval;
        volatile boolean active = true;
        volatile boolean runInFlight;
        ScheduledFuture<?> next;

        Entry(String sourceId, Duration interval) {
            this.sourceId = sourceId;
            this.interval = interval;
        }
    }

    // ------------------------------------------------------------------ lifecycle

    /** Starts the scheduler once the application is ready, when scheduling is enabled. */
    @EventListener(ApplicationReadyEvent.class)
    @Order(StartupOrder.SCHEDULER)
    public void onApplicationReady() {
        if (properties.scheduled()) {
            start();
        } else {
            log.info("Collector scheduling is disabled (foresignal.collector.scheduled=false)");
        }
    }

    /** Starts scheduling. Idempotent. */
    @Override
    public synchronized void start() {
        if (running) {
            return;
        }
        timer = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "foresignal-collector");
            thread.setDaemon(true);
            return thread;
        });
        fetchers = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("foresignal-scheduled-", 0).factory());
        running = true;
        long period = Math.max(1, properties.reconcileInterval().toMillis());
        reconcileTask = timer.scheduleWithFixedDelay(this::reconcileSafely, 0, period, TimeUnit.MILLISECONDS);
        log.info("Collector scheduler started (reconcile every {})", properties.reconcileInterval());
    }

    /**
     * Stops scheduling, waits up to {@code shutdownWait} for in-flight fetches and flushes the
     * streaming sinks. Idempotent.
     */
    @Override
    public void stop() {
        ScheduledExecutorService stoppingTimer;
        ExecutorService stoppingFetchers;
        synchronized (this) {
            if (!running) {
                return;
            }
            running = false;
            stoppingTimer = timer;
            stoppingFetchers = fetchers;
            if (reconcileTask != null) {
                reconcileTask.cancel(false);
            }
        }
        long deadline = System.nanoTime() + properties.shutdownWait().toNanos();
        stoppingTimer.shutdownNow();
        try {
            stoppingTimer.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        entries.values().forEach(entry -> entry.active = false);
        entries.clear();
        while (inFlight.get() > 0 && System.nanoTime() < deadline) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        if (inFlight.get() > 0) {
            log.warn("{} fetches were still running when the collector stopped", inFlight.get());
        }
        supervisor.stopAll(Duration.ofNanos(Math.max(1, deadline - System.nanoTime())));
        stoppingFetchers.shutdown();
        log.info("Collector scheduler stopped");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    /** Started by the ready event rather than by the context refresh. */
    @Override
    public boolean isAutoStartup() {
        return false;
    }

    /** The collector's state, for {@code /v1/system}. */
    public SchedulerState state() {
        return new SchedulerState(
                running, new ArrayList<>(new TreeSet<>(entries.keySet())), executor.activeFetches(),
                supervisor.activeStreams());
    }

    // ------------------------------------------------------------------ events

    /** Reconciles at once when a change could start, stop or reschedule collection. */
    @EventListener
    public void onSettingsChanged(SourceSettingsChangedEvent event) {
        if (event.affectsScheduling()) {
            reconcileNow();
        }
    }

    /** Reconciles at once: a source may have gained or lost its required credentials. */
    @EventListener
    public void onCredentialsChanged(SourceCredentialsChangedEvent event) {
        reconcileNow();
    }

    /** Reconciles at once when a dynamic source appears or disappears. */
    @EventListener
    public void onRegistryChanged(SourceRegistryChangedEvent event) {
        reconcileNow();
    }

    /** Runs a reconcile on the timer thread now, rather than at the next tick. */
    public void reconcileNow() {
        ScheduledExecutorService current = timer;
        if (!running || current == null) {
            return;
        }
        try {
            current.execute(this::reconcileSafely);
        } catch (RejectedExecutionException e) {
            log.debug("Reconcile request after the scheduler stopped; ignored");
        }
    }

    // ------------------------------------------------------------------ reconcile (timer thread)

    private void reconcileSafely() {
        try {
            reconcile();
        } catch (RuntimeException e) {
            log.error("Collector reconcile failed; it will run again at the next tick", e);
        }
    }

    /** Compares the enabled sources with the running set and closes the gap. Runs on the timer thread. */
    void reconcile() {
        if (!running) {
            return;
        }
        Desired desired = desired();

        for (Entry entry : new ArrayList<>(entries.values())) {
            if (!desired.polling().containsKey(entry.sourceId)) {
                cancel(entry);
                log.info("Stopped scheduling {}", entry.sourceId);
            }
        }
        desired.polling().forEach((id, interval) -> {
            Entry entry = entries.get(id);
            if (entry == null) {
                Entry created = new Entry(id, interval);
                entries.put(id, created);
                schedule(created, Duration.ZERO);
                log.info("Scheduling {} every {}", id, interval);
            } else if (!entry.interval.equals(interval)) {
                entry.interval = interval;
                if (!entry.runInFlight) {
                    if (entry.next != null) {
                        entry.next.cancel(false);
                    }
                    schedule(entry, interval);
                }
                log.info("Rescheduled {} every {}", id, interval);
            }
        });

        supervisor.reconcile(desired.streams());
    }

    /** What should be running. */
    private record Desired(Map<String, Duration> polling, Set<String> streams) {}

    private Desired desired() {
        Map<String, Duration> polling = new LinkedHashMap<>();
        Set<String> streams = new TreeSet<>();
        List<Object[]> rows = jdbc.query(
                "select source_id, interval_seconds from source_settings where enabled",
                (rs, row) -> new Object[] {rs.getString(1), rs.getInt(2)});
        for (Object[] row : rows) {
            String id = (String) row[0];
            SourceRegistry.Registration registration = registry.find(id).orElse(null);
            if (registration == null) {
                continue;
            }
            SourceDescriptor descriptor = registration.descriptor();
            SourceAdapter adapter = registration.adapterIfPresent().orElse(null);
            if (descriptor.requiresCredentials() && !credentialsSet(id)) {
                continue;
            }
            if (descriptor.mode() == Mode.POLLING && adapter instanceof PollingAdapter) {
                polling.put(id, Duration.ofSeconds(Math.max(1, (Integer) row[1])));
            } else if (descriptor.mode() == Mode.STREAMING && adapter instanceof StreamingAdapter) {
                streams.add(id);
            }
            // PUSH and DERIVED: never polled nor streamed here.
        }
        return new Desired(polling, streams);
    }

    private boolean credentialsSet(String sourceId) {
        try {
            return credentials.isSet(sourceId);
        } catch (RuntimeException e) {
            log.warn("Could not check {}'s credentials; not scheduling it this tick", sourceId, e);
            return false;
        }
    }

    private void schedule(Entry entry, Duration delay) {
        if (!running || !entry.active) {
            return;
        }
        try {
            entry.next = timer.schedule(() -> launch(entry), Math.max(0, delay.toMillis()), TimeUnit.MILLISECONDS);
        } catch (RejectedExecutionException e) {
            log.debug("Could not schedule {}; the scheduler is stopping", entry.sourceId);
        }
    }

    private void cancel(Entry entry) {
        entry.active = false;
        if (entry.next != null) {
            entry.next.cancel(false);
        }
        entries.remove(entry.sourceId, entry);
        backoffLogged.remove(entry.sourceId);
    }

    /** Starts one run on a virtual thread. Timer thread. */
    private void launch(Entry entry) {
        if (!running || !entry.active) {
            return;
        }
        entry.runInFlight = true;
        inFlight.incrementAndGet();
        try {
            fetchers.execute(() -> runOnce(entry));
        } catch (RejectedExecutionException e) {
            entry.runInFlight = false;
            inFlight.decrementAndGet();
        }
    }

    /** One scheduled fetch. Virtual thread. */
    private void runOnce(Entry entry) {
        try {
            FetchOutcome outcome = executor.execute(entry.sourceId, FetchTrigger.SCHEDULED);
            if (FetchOutcome.SKIP_BACKOFF.equals(outcome.skipReason())) {
                logBackoffOnce(entry.sourceId, outcome.error());
            } else {
                backoffLogged.remove(entry.sourceId);
            }
        } catch (RuntimeException e) {
            log.error("Scheduled fetch of {} failed unexpectedly", entry.sourceId, e);
        } finally {
            inFlight.decrementAndGet();
            ScheduledExecutorService current = timer;
            try {
                current.execute(() -> afterRun(entry));
            } catch (RejectedExecutionException e) {
                entry.runInFlight = false;
            }
        }
    }

    /** Schedules the next run, measured from the end of this one. Timer thread. */
    private void afterRun(Entry entry) {
        entry.runInFlight = false;
        if (running && entry.active && entries.get(entry.sourceId) == entry) {
            schedule(entry, entry.interval);
        }
    }

    private void logBackoffOnce(String sourceId, String detail) {
        Instant until = null;
        try {
            until = backoff.backoffUntil(sourceId);
        } catch (RuntimeException e) {
            log.debug("Could not read {}'s backoff", sourceId, e);
        }
        Instant key = until == null ? Instant.EPOCH : until;
        Instant previous = backoffLogged.put(sourceId, key);
        if (!key.equals(previous)) {
            log.info("{}: skipping scheduled runs while in backoff ({})", sourceId, detail);
        }
    }
}
