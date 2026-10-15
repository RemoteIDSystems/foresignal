package ai.foresignal.derive;

import ai.foresignal.collect.DefaultFetchContext;
import ai.foresignal.collect.FetchContextFactory;
import ai.foresignal.collect.FetchLogWriter;
import ai.foresignal.collect.HealthService;
import ai.foresignal.collect.HealthTracker;
import ai.foresignal.config.ForesignalVersion;
import ai.foresignal.config.StartupOrder;
import ai.foresignal.ingest.FetchLogEntry;
import ai.foresignal.ingest.IngestOutcome;
import ai.foresignal.ingest.IngestQueue;
import ai.foresignal.ingest.IngestRecord;
import ai.foresignal.place.PlaceChangedEvent;
import ai.foresignal.raster.RasterFrameCommittedEvent;
import ai.foresignal.raster.RasterFrameQueries;
import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.raster.RasterRunCompletedEvent;
import ai.foresignal.raster.sample.RasterReader;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceRegistryChangedEvent;
import ai.foresignal.source.SourceSettingsChangedEvent;
import ai.foresignal.source.spi.Coverage;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.FetchStatus;
import ai.foresignal.store.FetchTrigger;
import ai.foresignal.stream.ChangeBatch;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.stream.ChangeListener;
import ai.foresignal.stream.ChangeSummary;
import ai.foresignal.stream.Subscription;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Runs the enabled DERIVED sources (prompt 052; Weather-Imagery-Design §2.1 ⑥, §7.1).
 *
 * <p><b>One virtual thread per enabled derived source</b>, with a bounded inbox. Inputs:
 * <ul>
 *   <li><b>Change batches</b> from one {@link ChangeBus} subscription, filtered per source by its
 *       {@link DerivedInputs}; a source never receives its own changes. When a source's inbox already holds
 *       {@code inboxCapacity} batches, the batch is dropped and a reconcile is scheduled. A bus gap
 *       ({@link ChangeListener#onGap}) schedules a reconcile for every source with change inputs.
 *   <li><b>Frame events</b> ({@link RasterFrameCommittedEvent}, {@link RasterRunCompletedEvent}),
 *       coalesced per product: only the newest pending event of a product is kept.
 *   <li><b>Ticks</b> every {@link DerivedInputs#tick()}, checked every {@code tickResolution} against the
 *       injected clock; at most one tick is pending.
 *   <li><b>Reconciles</b> at start (startup and enable), after an overflow or a gap, and after a place edit
 *       for sources whose inputs ask for places.
 * </ul>
 * Pending work runs in that order: reconcile, change batches, frame events, the tick.
 *
 * <p><b>Each invocation is treated like a fetch</b>: {@code HealthTracker.recordAttempt}, then exactly one
 * {@code fetch_log} row with {@code principal=derived} (written by the writer when the invocation emitted
 * records, here otherwise), one {@code IngestBatch}, and {@code recordSuccess(IngestOutcome)} or
 * {@code recordFailure}. A derived source is enabled, parameterized, health-tracked and retained like any
 * other source; it has no schedule of its own beyond its tick.
 */
@Component
public class DerivedRunner implements ChangeListener, DisposableBean, ai.foresignal.ingest.IngestStateReset {

    private static final Logger log = LoggerFactory.getLogger(DerivedRunner.class);

    /** The {@code fetch_log.principal} of derived invocations. */
    public static final String PRINCIPAL = "derived";

    /** The change-bus subscriber name. */
    public static final String SUBSCRIBER = "derived-runtime";

    private final SourceRegistry registry;
    private final FetchContextFactory contexts;
    private final IngestQueue queue;
    private final FetchLogWriter fetchLog;
    private final HealthTracker health;
    private final ObjectProvider<HealthService> healthService;
    private final ChangeBus bus;
    private final RasterFrameQueries frames;
    private final ObjectProvider<RasterReader> rasters;
    private final DerivedRecords records;
    private final ObjectProvider<ObservationFeed> observations;
    private final ObjectProvider<CoverageService> coverage;
    private final DerivedMetrics metrics;
    private final DerivedPlaces places;
    private final ForesignalVersion version;
    private final JdbcTemplate jdbc;
    private final DerivedProperties properties;
    private final Clock clock;

    private final ConcurrentHashMap<String, Runner> runners = new ConcurrentHashMap<>();
    private final AtomicLong arrival = new AtomicLong();
    private volatile Subscription subscription;
    private volatile ScheduledExecutorService ticker;
    private volatile boolean started;

    /** Creates the runtime. */
    public DerivedRunner(SourceRegistry registry, FetchContextFactory contexts, IngestQueue queue,
            FetchLogWriter fetchLog, HealthTracker health, ObjectProvider<HealthService> healthService, ChangeBus bus,
            RasterFrameQueries frames, ObjectProvider<RasterReader> rasters, DerivedRecords records,
            ObjectProvider<ObservationFeed> observations, ObjectProvider<CoverageService> coverage,
            DerivedMetrics metrics, DerivedPlaces places, ForesignalVersion version, JdbcTemplate jdbc,
            DerivedProperties properties, Clock clock) {
        this.registry = registry;
        this.contexts = contexts;
        this.queue = queue;
        this.fetchLog = fetchLog;
        this.health = health;
        this.healthService = healthService;
        this.bus = bus;
        this.frames = frames;
        this.rasters = rasters;
        this.records = records;
        this.observations = observations;
        this.coverage = coverage;
        this.metrics = metrics;
        this.places = places;
        this.version = version;
        this.jdbc = jdbc;
        this.properties = properties;
        this.clock = clock;
    }

    // ------------------------------------------------------------------ lifecycle

    /** Starts the runtime once the application is ready, after the writer and the trigger engine. */
    @EventListener(ApplicationReadyEvent.class)
    @Order(StartupOrder.DERIVED)
    public void onApplicationReady() {
        if (properties.enabled()) {
            start();
        } else {
            log.info("The DERIVED runtime is disabled (foresignal.derive.enabled=false)");
        }
    }

    /** Subscribes to the change bus, starts the ticker and the runners of enabled sources. Idempotent. */
    public synchronized void start() {
        if (started) {
            return;
        }
        started = true;
        subscription = bus.subscribe(SUBSCRIBER, this, 1024);
        ticker = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "foresignal-derived-ticker");
            t.setDaemon(true);
            return t;
        });
        long period = Math.max(50, properties.tickResolution().toMillis());
        ticker.scheduleWithFixedDelay(this::tickDue, period, period, TimeUnit.MILLISECONDS);
        reconcileRunners();
    }

    /** Stops every runner and the subscriptions. Idempotent. */
    public synchronized void stop() {
        if (!started) {
            return;
        }
        started = false;
        if (subscription != null) {
            subscription.close();
        }
        if (ticker != null) {
            ticker.shutdownNow();
        }
        for (String id : new ArrayList<>(runners.keySet())) {
            stopRunner(id);
        }
    }

    @Override
    public void destroy() {
        stop();
    }

    /**
     * Schedules a reconcile of every running source after the store's state was reset (the test database
     * cleaner, {@link ai.foresignal.ingest.IngestStateReset}): each runner reseeds its emit ledger from the heads
     * that remain.
     */
    @Override
    public void resetState() {
        for (Runner runner : runners.values()) {
            runner.offerReconcile();
        }
    }

    /** True while the runtime runs. */
    public boolean isStarted() {
        return started;
    }

    /** The sources whose runners are running, ordered. */
    public Set<String> running() {
        return new TreeSet<>(runners.keySet());
    }

    /** Starts and stops runners to match the enabled derived sources with a generator. */
    public synchronized void reconcileRunners() {
        if (!started) {
            return;
        }
        Set<String> desired = new TreeSet<>();
        for (String id : jdbc.queryForList("select source_id from source_settings where enabled", String.class)) {
            Optional<SourceRegistry.Registration> registration = registry.find(id);
            if (registration.isPresent() && registration.get().descriptor().mode() == Mode.DERIVED
                    && registration.get().adapterIfPresent().orElse(null) instanceof DerivedGenerator) {
                desired.add(id);
            }
        }
        for (String id : new ArrayList<>(runners.keySet())) {
            if (!desired.contains(id)) {
                stopRunner(id);
            }
        }
        for (String id : desired) {
            if (!runners.containsKey(id)) {
                startRunner(id);
            }
        }
    }

    /** Restarts a source's runner when it was enabled, disabled or its parameters changed. */
    @EventListener
    public void onSettingsChanged(SourceSettingsChangedEvent event) {
        if (!started || !event.affectsScheduling()) {
            return;
        }
        synchronized (this) {
            if (runners.containsKey(event.sourceId()) && event.after() != null && event.after().enabled()) {
                stopRunner(event.sourceId());
            }
            reconcileRunners();
        }
    }

    /** Reconciles runners when a dynamic source appears or disappears. */
    @EventListener
    public void onRegistryChanged(SourceRegistryChangedEvent event) {
        reconcileRunners();
    }

    private void startRunner(String sourceId) {
        SourceRegistry.Registration registration = registry.get(sourceId);
        DerivedGenerator generator = (DerivedGenerator) registration.adapterIfPresent().orElseThrow();
        SourceDescriptor descriptor = registration.descriptor();
        DerivedInputs inputs;
        try {
            inputs = generator.inputs(contexts.forFetch(descriptor, null).params());
        } catch (RuntimeException e) {
            log.error("{}: could not read its derived inputs; not started", sourceId, e);
            return;
        }
        Runner runner = new Runner(descriptor, generator, inputs);
        runners.put(sourceId, runner);
        runner.thread = Thread.ofVirtual().name("foresignal-derived-" + sourceId).start(runner::loop);
        log.info("Started derived source {}", sourceId);
    }

    private void stopRunner(String sourceId) {
        Runner runner = runners.remove(sourceId);
        if (runner == null) {
            return;
        }
        runner.shutdown();
        try {
            runner.thread.join(5_000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("Stopped derived source {}", sourceId);
    }

    // ------------------------------------------------------------------ inputs

    @Override
    public void onBatch(ChangeBatch batch) {
        for (Runner runner : runners.values()) {
            if (!runner.inputs.wantsChanges() || runner.descriptor.id().equals(batch.sourceId())) {
                continue;
            }
            List<ChangeSummary> matching = new ArrayList<>();
            for (ChangeSummary change : batch.changes()) {
                if (runner.inputs.matchesChange(change.sourceId(), change.kind())) {
                    matching.add(change);
                }
            }
            if (!matching.isEmpty()) {
                runner.offerChanges(new ChangeBatch(batch.sourceId(), batch.batchId(), batch.ingestedAt(),
                        matching.get(0).seq(), matching.get(matching.size() - 1).seq(), matching));
            }
        }
    }

    @Override
    public void onGap(long lastDeliveredSeq) {
        for (Runner runner : runners.values()) {
            if (runner.inputs.wantsChanges()) {
                runner.offerReconcile();
            }
        }
    }

    /** Hands a stored frame to the sources that listen to its product. */
    @EventListener
    public void onFrameCommitted(RasterFrameCommittedEvent event) {
        List<Runner> targets = targets(event.productId());
        if (targets.isEmpty()) {
            return;
        }
        Instant now = clock.instant();
        Optional<RasterFrameRow> row = frames.find(event.productId(), event.frameId());
        Instant valid = row.map(RasterFrameRow::validTime).orElse(null);
        Instant ref = row.map(RasterFrameRow::referenceTime).orElse(null);
        Instant observed = valid == null || valid.isAfter(now) ? now : valid;
        RasterFrameEvent frameEvent = new RasterFrameEvent(event.productId(), event.frameId(), event.status(), valid, ref,
                false, observed.truncatedTo(ChronoUnit.MILLIS));
        targets.forEach(r -> r.offerFrame(frameEvent));
    }

    /** Hands a completed forecast run to the sources that listen to its product. */
    @EventListener
    public void onRunCompleted(RasterRunCompletedEvent event) {
        List<Runner> targets = targets(event.productId());
        if (targets.isEmpty()) {
            return;
        }
        Instant now = clock.instant().truncatedTo(ChronoUnit.MILLIS);
        RasterFrameEvent frameEvent = new RasterFrameEvent(event.productId(), null, "complete", null, event.run(), true,
                now);
        targets.forEach(r -> r.offerFrame(frameEvent));
    }

    /** Schedules a reconcile of the sources that follow places. */
    @EventListener
    public void onPlaceChanged(PlaceChangedEvent event) {
        for (Runner runner : runners.values()) {
            if (runner.inputs.places()) {
                runner.offerReconcile();
            }
        }
    }

    private List<Runner> targets(String productId) {
        List<Runner> out = new ArrayList<>();
        for (Runner runner : runners.values()) {
            if (runner.inputs.matchesProduct(productId)) {
                out.add(runner);
            }
        }
        return out;
    }

    /** Offers a tick to every runner whose tick is due. Runs on the ticker thread. */
    void tickDue() {
        try {
            Instant now = clock.instant();
            for (Runner runner : runners.values()) {
                Duration tick = runner.inputs.tick();
                if (tick == null) {
                    continue;
                }
                if (runner.nextTick == null || !now.isBefore(runner.nextTick)) {
                    runner.nextTick = now.plus(tick);
                    runner.offerTick(now);
                }
            }
        } catch (RuntimeException e) {
            log.warn("Derived tick check failed", e);
        }
    }

    // ------------------------------------------------------------------ test and operator hooks

    /** Offers a tick to {@code sourceId} now, whatever its schedule. */
    public void tickNow(String sourceId) {
        Runner runner = runners.get(sourceId);
        if (runner != null) {
            runner.offerTick(clock.instant());
        }
    }

    /** Schedules a reconcile of {@code sourceId}. */
    public void reconcileNow(String sourceId) {
        Runner runner = runners.get(sourceId);
        if (runner != null) {
            runner.offerReconcile();
        }
    }

    /**
     * Waits until {@code sourceId}'s inbox is empty and no invocation is running.
     *
     * @return true when idle within {@code timeout}; true also when the source is not running
     */
    public boolean awaitIdle(String sourceId, Duration timeout) {
        Runner runner = runners.get(sourceId);
        return runner == null || runner.awaitIdle(timeout);
    }

    /** Invocations of {@code sourceId} so far, or -1 when it is not running. */
    public long invocations(String sourceId) {
        Runner runner = runners.get(sourceId);
        return runner == null ? -1 : runner.invocations.get();
    }

    /** Inbox overflows of {@code sourceId} so far, or -1 when it is not running. */
    public long overflows(String sourceId) {
        Runner runner = runners.get(sourceId);
        return runner == null ? -1 : runner.overflows.get();
    }

    // ------------------------------------------------------------------ invocation

    private HealthStatus statusOf(String sourceId) {
        HealthService service = healthService.getIfAvailable();
        if (service == null) {
            return HealthStatus.IDLE;
        }
        return service.health(sourceId).status();
    }

    /** One hook call of one source. */
    @FunctionalInterface
    interface Hook {

        /** Calls the generator. */
        void call(DerivedContext ctx);
    }

    /** Runs one invocation of {@code runner} end to end. */
    void invoke(Runner runner, String what, Hook hook) {
        SourceDescriptor descriptor = runner.descriptor;
        String sourceId = descriptor.id();
        health.recordAttempt(sourceId);
        Instant startedAt = clock.instant().truncatedTo(ChronoUnit.MICROS);
        DefaultFetchContext fetch = contexts.forFetch(descriptor, null);
        Map<String, HealthStatus> statuses = new HashMap<>();
        DefaultDerivedContext ctx = new DefaultDerivedContext(descriptor, fetch.params(), fetch.state(),
                rasters.getIfAvailable(), records, observations.getIfAvailable(), coverage.getIfAvailable(),
                places, id -> statuses.computeIfAbsent(id, this::statusOf), clock, runner.ledger);
        try {
            long began = System.nanoTime();
            try {
                hook.call(ctx);
            } finally {
                metrics.eval(sourceId, Duration.ofNanos(System.nanoTime() - began));
                if (runner.generator instanceof StatefulGenerator stateful) {
                    metrics.stateSize(sourceId, stateful.stateSize());
                }
            }
        } catch (RuntimeException | Error e) {
            forget(runner, ctx.emitted());
            Instant now = clock.instant();
            String message = HealthTracker.truncate(what + ": " + health.errorMessage(sourceId, e));
            fetchLog.write(entry(descriptor, FetchStatus.FAILED, startedAt, now, message), 0);
            health.recordFailure(sourceId, e);
            log.warn("{}: {} failed: {}", sourceId, what, message);
            return;
        } finally {
            runner.invocations.incrementAndGet();
        }
        List<NormalizedRecord> emitted = ctx.emitted();
        Instant receivedAt = clock.instant();
        if (emitted.isEmpty()) {
            fetchLog.write(entry(descriptor, FetchStatus.OK, startedAt, receivedAt, null), 0);
            health.recordSuccess(sourceId, IngestOutcome.empty(), false, Coverage.none());
            return;
        }
        List<IngestRecord> stamped = new ArrayList<>(emitted.size());
        for (NormalizedRecord record : emitted) {
            stamped.add(new IngestRecord(record, receivedAt, arrival.incrementAndGet()));
        }
        try {
            IngestOutcome outcome = queue.submit(sourceId, entry(descriptor, FetchStatus.OK, startedAt, receivedAt, null),
                    stamped, properties.ingestTimeout()).get(properties.ingestTimeout().toMillis(), TimeUnit.MILLISECONDS);
            health.recordSuccess(sourceId, outcome, false, Coverage.none());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            forget(runner, emitted);
        } catch (Exception e) {
            forget(runner, emitted);
            String message = HealthTracker.truncate("ingest failed: " + health.errorMessage(sourceId, e));
            fetchLog.write(entry(descriptor, FetchStatus.FAILED, startedAt, clock.instant(), message), emitted.size());
            log.warn("{}: {} records of {} were not ingested: {}", sourceId, emitted.size(), what, message);
        }
    }

    private static void forget(Runner runner, List<NormalizedRecord> emitted) {
        for (NormalizedRecord r : emitted) {
            runner.ledger.forget(r.kind(), r.externalId());
        }
    }

    private FetchLogEntry entry(SourceDescriptor descriptor, FetchStatus status, Instant startedAt, Instant receivedAt,
            String error) {
        return new FetchLogEntry(descriptor.id(), FetchTrigger.SCHEDULED, status, startedAt, receivedAt, null, 0, null,
                null, null, null, version.version(), descriptor.licence(), PRINCIPAL, Coverage.none(), error);
    }

    // ------------------------------------------------------------------ the runner

    /** One derived source's inbox and thread. */
    final class Runner {

        final SourceDescriptor descriptor;
        final DerivedGenerator generator;
        final DerivedInputs inputs;
        final EmitLedger ledger = new EmitLedger();
        final AtomicLong invocations = new AtomicLong();
        final AtomicLong overflows = new AtomicLong();

        private final ReentrantLock lock = new ReentrantLock();
        private final Condition changed = lock.newCondition();
        private final ArrayDeque<ChangeBatch> changes = new ArrayDeque<>();
        private final LinkedHashMap<String, RasterFrameEvent> frameEvents = new LinkedHashMap<>();
        private Instant pendingTick;
        private boolean pendingReconcile = true;
        private boolean busy;
        private volatile boolean stopped;
        volatile Instant nextTick;
        Thread thread;

        Runner(SourceDescriptor descriptor, DerivedGenerator generator, DerivedInputs inputs) {
            this.descriptor = descriptor;
            this.generator = generator;
            this.inputs = inputs;
        }

        void offerChanges(ChangeBatch batch) {
            lock.lock();
            try {
                if (changes.size() >= properties.inboxCapacity()) {
                    overflows.incrementAndGet();
                    pendingReconcile = true;
                    changes.clear();
                    log.warn("{}: derived inbox overflowed; dropping change batches and reconciling", descriptor.id());
                } else {
                    changes.addLast(batch);
                }
                changed.signalAll();
            } finally {
                lock.unlock();
            }
        }

        void offerFrame(RasterFrameEvent event) {
            lock.lock();
            try {
                frameEvents.remove(event.productId());
                frameEvents.put(event.productId(), event);
                changed.signalAll();
            } finally {
                lock.unlock();
            }
        }

        void offerTick(Instant at) {
            lock.lock();
            try {
                pendingTick = at;
                changed.signalAll();
            } finally {
                lock.unlock();
            }
        }

        void offerReconcile() {
            lock.lock();
            try {
                pendingReconcile = true;
                changes.clear();
                changed.signalAll();
            } finally {
                lock.unlock();
            }
        }

        void shutdown() {
            stopped = true;
            lock.lock();
            try {
                changed.signalAll();
            } finally {
                lock.unlock();
            }
        }

        boolean awaitIdle(Duration timeout) {
            long deadline = System.nanoTime() + timeout.toNanos();
            lock.lock();
            try {
                while (busy || pendingReconcile || !changes.isEmpty() || !frameEvents.isEmpty() || pendingTick != null) {
                    long left = deadline - System.nanoTime();
                    if (left <= 0 || stopped) {
                        return !busy && !pendingReconcile && changes.isEmpty() && frameEvents.isEmpty()
                                && pendingTick == null;
                    }
                    changed.awaitNanos(Math.min(left, TimeUnit.MILLISECONDS.toNanos(50)));
                }
                return true;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            } finally {
                lock.unlock();
            }
        }

        void loop() {
            while (!stopped) {
                Work work;
                lock.lock();
                try {
                    while (!stopped && !pendingReconcile && changes.isEmpty() && frameEvents.isEmpty()
                            && pendingTick == null) {
                        changed.awaitNanos(TimeUnit.MILLISECONDS.toNanos(500));
                    }
                    if (stopped) {
                        return;
                    }
                    work = next();
                    busy = true;
                } catch (InterruptedException e) {
                    if (stopped) {
                        return;
                    }
                    continue;
                } finally {
                    lock.unlock();
                }
                try {
                    run(work);
                } catch (RuntimeException e) {
                    log.error("{}: derived invocation failed outside the generator", descriptor.id(), e);
                } finally {
                    lock.lock();
                    try {
                        busy = false;
                        changed.signalAll();
                    } finally {
                        lock.unlock();
                    }
                }
            }
        }

        /** The next piece of work; called holding the lock. */
        private Work next() {
            if (pendingReconcile) {
                pendingReconcile = false;
                return new Work(Work.Type.RECONCILE, null, null, null);
            }
            if (!changes.isEmpty()) {
                return new Work(Work.Type.CHANGES, changes.pollFirst(), null, null);
            }
            if (!frameEvents.isEmpty()) {
                String first = frameEvents.keySet().iterator().next();
                return new Work(Work.Type.FRAME, null, frameEvents.remove(first), null);
            }
            Instant at = pendingTick;
            pendingTick = null;
            return new Work(Work.Type.TICK, null, null, at);
        }

        private void run(Work work) {
            switch (work.type()) {
                case RECONCILE -> invoke(this, "reconcile", ctx -> {
                    List<HeadSnapshot> heads = records.heads(descriptor.id(), null);
                    ledger.seed(heads);
                    generator.reconcile(heads, ctx);
                });
                case CHANGES -> invoke(this, "changes", ctx -> generator.onChanges(work.batch(), ctx));
                case FRAME -> invoke(this, "frame " + work.frame().productId(), ctx -> generator.onFrame(work.frame(), ctx));
                case TICK -> invoke(this, "tick", ctx -> generator.onTick(work.tick(), ctx));
                default -> { }
            }
        }
    }

    /** One unit of runner work. */
    record Work(Type type, ChangeBatch batch, RasterFrameEvent frame, Instant tick) {

        /** The kinds of work. */
        enum Type {
            RECONCILE,
            CHANGES,
            FRAME,
            TICK
        }
    }
}
