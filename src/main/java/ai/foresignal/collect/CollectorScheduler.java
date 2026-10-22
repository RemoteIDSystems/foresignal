package ai.foresignal.collect;

import ai.foresignal.config.StartupOrder;
import ai.foresignal.isolation.WorkRole;
import ai.foresignal.isolation.WorkThreads;
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
import java.util.HashMap;
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
 * <p><strong>Timing.</strong> One platform thread drives all timing. Every scheduled source has a
 * platform thread of its own ({@code foresignal-source-<id>}), in the {@link WorkRole#COLLECT} role, on
 * which its runs execute one after another; so a slow or stuck source holds only its own thread, and
 * collection draws only on the collect database pool, never on what the API needs. {@link #reconcile()} compares the enabled sources in {@code source_settings} with the
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

    /** True once {@link #start()} has run; see {@link #isAutoStartup()}. */
    private volatile boolean startedOnce;
    private ScheduledExecutorService timer;
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

    /** One scheduled polling source, with the thread its runs execute on. */
    private static final class Entry {
        final String sourceId;
        /** The source's own thread; one run at a time ({@link #runInFlight}). */
        final ExecutorService thread;
        volatile Duration interval;
        volatile boolean active = true;
        volatile boolean runInFlight;
        ScheduledFuture<?> next;

        Entry(String sourceId, Duration interval) {
            this.sourceId = sourceId;
            this.interval = interval;
            this.thread = Executors.newSingleThreadExecutor(
                    WorkThreads.named(WorkRole.COLLECT, "foresignal-source-" + sourceId));
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
        timer = Executors.newSingleThreadScheduledExecutor(WorkThreads.named(WorkRole.COLLECT, "foresignal-collector"));
        running = true;
        startedOnce = true;
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
        synchronized (this) {
            if (!running) {
                return;
            }
            running = false;
            stoppingTimer = timer;
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
        List<Entry> stopping = new ArrayList<>(entries.values());
        stopping.forEach(entry -> entry.active = false);
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
        stopping.forEach(entry -> entry.thread.shutdown());
        log.info("Collector scheduler stopped");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * False until the ready event has started the scheduler, true afterwards.
     *
     * <p>False at first, so the context refresh does not start the scheduler ahead of the
     * {@link StartupOrder} sequence; the ready event does. True afterwards, because a context that
     * is paused and restarted ({@code ConfigurableApplicationContext.pause()}/{@code restart()}, as
     * Spring's test context cache does on every context switch, or a checkpoint/restore) stops every
     * lifecycle bean but restarts only auto-startup ones. With a constant {@code false} the
     * scheduler stayed stopped after a restart (checkpoint 012, {@code NoClientTriggeredFetchIT}).
     */
    @Override
    public boolean isAutoStartup() {
        return startedOnce;
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
        if (paramsChangedWhileEnabled(event)) {
            // 063: a running stream applies new parameters in place (StreamHandle.update) or restarts.
            onTimer(() -> supervisor.paramsChanged(event.sourceId()));
        }
    }

    /**
     * Reconciles at once: a source may have gained or lost its required credentials. A stream that is
     * already running restarts with the new credentials (063), instead of waiting out the hourly retry of a
     * rejected key.
     */
    @EventListener
    public void onCredentialsChanged(SourceCredentialsChangedEvent event) {
        reconcileNow();
        if (event.present()) {
            onTimer(() -> supervisor.restart(event.sourceId()));
        }
    }

    private static boolean paramsChangedWhileEnabled(SourceSettingsChangedEvent event) {
        return event.before() != null
                && event.before().enabled()
                && event.after().enabled()
                && !java.util.Objects.equals(event.before().params(), event.after().params());
    }

    /** Runs {@code task} on the timer thread, after any reconcile already queued; a no-op when stopped. */
    private void onTimer(Runnable task) {
        ScheduledExecutorService current = timer;
        if (!running || current == null) {
            return;
        }
        try {
            current.execute(() -> {
                try {
                    task.run();
                } catch (RuntimeException e) {
                    log.warn("A streaming change failed", e);
                }
            });
        } catch (RejectedExecutionException e) {
            log.debug("The collector is stopping; a streaming change was not applied");
        }
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
        Instant now = Instant.now();
        int[] dueNow = {0};
        desired.polling().forEach((id, interval) -> {
            Entry entry = entries.get(id);
            if (entry == null) {
                Entry created = new Entry(id, interval);
                entries.put(id, created);
                Duration delay = firstDelay(desired.lastAttempts().get(id), interval, now, dueNow[0]);
                if (delay.compareTo(STAGGER.multipliedBy(dueNow[0])) <= 0) {
                    dueNow[0]++;
                }
                schedule(created, delay);
                log.info("Scheduling {} every {}, first run in {}", id, interval, delay);
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

    /**
     * The delay before a newly scheduled source's first run: the rest of its interval since its last attempt, so a
     * restart does not re-fetch what was just fetched (CelesTrak allows one download per update, 2026-10-22). A
     * source that is due, or never ran, starts {@code dueBefore} × {@link #STAGGER} from now, so the hundreds due
     * at once after a restart do not all start in the same second (1.7% of that burst timed out, against 0.27%
     * for the rest of the day).
     */
    static Duration firstDelay(Instant lastAttempt, Duration interval, Instant now, int dueBefore) {
        Duration stagger = STAGGER.multipliedBy(dueBefore);
        if (lastAttempt == null) {
            return stagger;
        }
        Duration remaining = Duration.between(now, lastAttempt.plus(interval));
        return remaining.compareTo(stagger) > 0 ? remaining : stagger;
    }

    /** The spacing between the first runs of sources that are due at the same time. */
    static final Duration STAGGER = Duration.ofMillis(100);

    /** What should be running, with each polling source's last attempt (absent when it never ran). */
    private record Desired(Map<String, Duration> polling, Set<String> streams, Map<String, Instant> lastAttempts) {}

    private Desired desired() {
        Map<String, Duration> polling = new LinkedHashMap<>();
        Set<String> streams = new TreeSet<>();
        Map<String, Instant> lastAttempts = new HashMap<>();
        List<Object[]> rows = jdbc.query(
                "select s.source_id, s.interval_seconds, h.last_attempt_at from source_settings s"
                        + " left join source_health h on h.source_id = s.source_id where s.enabled",
                (rs, row) -> {
                    java.sql.Timestamp last = rs.getTimestamp(3);
                    return new Object[] {rs.getString(1), rs.getInt(2), last == null ? null : last.toInstant()};
                });
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
                if (row[2] != null) {
                    lastAttempts.put(id, (Instant) row[2]);
                }
            } else if (descriptor.mode() == Mode.STREAMING && adapter instanceof StreamingAdapter) {
                streams.add(id);
            }
            // PUSH and DERIVED: never polled nor streamed here.
        }
        return new Desired(polling, streams, lastAttempts);
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
        // A run in flight finishes (FetchExecutor records it DISCARDED); then the thread ends.
        entry.thread.shutdown();
    }

    /** Starts one run on the source's own thread. Timer thread. */
    private void launch(Entry entry) {
        if (!running || !entry.active) {
            return;
        }
        entry.runInFlight = true;
        inFlight.incrementAndGet();
        try {
            entry.thread.execute(() -> runOnce(entry));
        } catch (RejectedExecutionException e) {
            entry.runInFlight = false;
            inFlight.decrementAndGet();
        }
    }

    /** One scheduled fetch. The source's own thread. */
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
