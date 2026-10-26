package ai.foresignal.retention;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.ingest.IngestStateReset;
import ai.foresignal.notify.SystemEvent;
import ai.foresignal.settings.RetentionSettings;
import ai.foresignal.store.query.SeqClockQuery;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * The retention job (Core-Data-Model §13, prompt 019): hourly, 5 minutes after start, and on demand
 * through {@code POST /v1/settings/retention/run}. Deliberately not {@code @Transactional}: each
 * step commits in small units so no long transaction holds locks or bloats.
 *
 * <p>A run reads the clock and the settings once, runs every {@link RetentionStep} bean in order,
 * then publishes {@link RetentionStateStore} and returns a {@link RetentionRunResult}. One run at a
 * time: a second request while one is active is {@code 409 ALREADY_RUNNING}, and a scheduled tick
 * that finds one active is skipped.
 *
 * <p>Cap history (prompt R2-015): the run applies the settings {@link RetentionDbCap#tighten} returns, so a window
 * the database-size cap stepped down to stays in force; after the window pass {@link RetentionDbCap#enforce} may step
 * it down further through the {@link RetentionCapPruner} (the default re-runs the steps). The result reports both.
 *
 * <p>Consumers that released the clamp increment {@code foresignal.retention.consumer_lag} and
 * publish a {@code system.retention_consumer_lag} notification. Metrics:
 * {@code foresignal.retention.deleted{table}} and {@code foresignal.retention.duration}; one summary
 * log line per run.
 */
@Component
public class RetentionJob implements IngestStateReset {

    private static final Logger log = LoggerFactory.getLogger(RetentionJob.class);

    /** Counter of rows deleted, tagged {@code table}. */
    public static final String DELETED_METRIC = "foresignal.retention.deleted";

    /** Timer of whole runs. */
    public static final String DURATION_METRIC = "foresignal.retention.duration";

    /** Counter of clamp releases, tagged {@code consumer}. */
    public static final String CONSUMER_LAG_METRIC = "foresignal.retention.consumer_lag";

    private final List<RetentionStep> steps;
    private final List<RetentionConsumer> consumers;
    private final List<RetentionConsumerProvider> providers;
    private final RetentionResolver resolver;
    private final RetentionStateStore state;
    private final SeqClockQuery seqClock;
    private final NamedParameterJdbcTemplate jdbc;
    private final RetentionProperties properties;
    private final ApplicationEventPublisher events;
    private final MeterRegistry meters;
    private final Clock clock;
    private final Timer duration;
    private final AtomicBoolean running = new AtomicBoolean();
    private RetentionDbCap dbCap;
    private org.springframework.beans.factory.ObjectProvider<RetentionCapPruner> pruners;

    private volatile RetentionRunResult lastResult;
    private volatile List<RetentionRun.Lag> lastLags = List.of();

    /**
     * Creates the job.
     *
     * @param providers the providers of run-time consumers (prompt 131 chronicles), asked at the start of each run
     */
    public RetentionJob(List<RetentionStep> steps, List<RetentionConsumer> consumers,
            List<RetentionConsumerProvider> providers, RetentionResolver resolver,
            RetentionStateStore state, SeqClockQuery seqClock, NamedParameterJdbcTemplate jdbc,
            RetentionProperties properties, ApplicationEventPublisher events, MeterRegistry meters, Clock clock) {
        List<RetentionStep> ordered = new ArrayList<>(steps);
        ordered.sort(Comparator.comparingInt(RetentionStep::order));
        this.steps = List.copyOf(ordered);
        this.consumers = List.copyOf(consumers);
        this.providers = providers == null ? List.of() : List.copyOf(providers);
        this.resolver = resolver;
        this.state = state;
        this.seqClock = seqClock;
        this.jdbc = jdbc;
        this.properties = properties;
        this.events = events;
        this.meters = meters;
        this.clock = clock;
        this.duration = Timer.builder(DURATION_METRIC).description("Duration of retention runs").register(meters);
    }

    /**
     * Sets the database-size cap and the provider of the pruner it uses (prompt R2-015). The pruner is looked up at
     * each run, not here: prompt R2-016's archiving pruner depends on this job, so resolving it while this bean is
     * being created would be a circular reference.
     *
     * @param dbCap the cap
     * @param pruners the provider of a pruner bean, if any
     */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setDbCap(RetentionDbCap dbCap, org.springframework.beans.factory.ObjectProvider<RetentionCapPruner> pruners) {
        this.dbCap = dbCap;
        this.pruners = pruners;
    }

    /** The pruner bean, else {@link RetentionCapPruner#RERUN}. */
    private RetentionCapPruner pruner() {
        RetentionCapPruner provided = pruners == null ? null : pruners.getIfAvailable();
        return provided == null ? RetentionCapPruner.RERUN : provided;
    }

    /**
     * Takes the single-run guard for work that runs retention passes of its own (prompt R2-016: an archive run). The
     * guard is not bound to a thread, so the lease may be taken on a request thread and closed on a worker.
     *
     * @return the lease; close it to release the guard
     * @throws ConflictException {@code ALREADY_RUNNING} when a run is active
     */
    public Lease lease() {
        if (!running.compareAndSet(false, true)) {
            throw new ConflictException(ConflictException.ALREADY_RUNNING, "A retention run is already active.");
        }
        return new Lease();
    }

    /** The single-run guard held for passes outside {@link #run()}; see {@link #lease()}. */
    public final class Lease implements AutoCloseable {

        private final AtomicBoolean open = new AtomicBoolean(true);

        private Lease() {}

        /**
         * A pass not yet run: its plans ({@link RetentionPlanner}) are computed on first use and kept, so handing it
         * to {@link #execute} deletes exactly what they showed.
         *
         * @param now the pass's clock reading
         * @param settings the settings
         * @param sourceCaps the run-only cap of STANDARD windows per source id ({@link RetentionRun#capSources})
         * @return the run
         */
        public RetentionRun plan(Instant now, RetentionSettings settings, Map<String, Duration> sourceCaps) {
            RetentionRun run = newRun(now, settings);
            run.capSources(sourceCaps);
            return run;
        }

        /**
         * Runs every retention step on {@code run}.
         *
         * @param run a run from {@link #plan}
         * @return the same run
         */
        public RetentionRun execute(RetentionRun run) {
            if (!open.get()) {
                throw new IllegalStateException("The retention lease is closed");
            }
            return RetentionJob.this.execute(run);
        }

        /** Releases the guard; closing twice is harmless. */
        @Override
        public void close() {
            if (open.compareAndSet(true, false)) {
                running.set(false);
            }
        }
    }

    /** The hourly run; does nothing when {@code foresignal.retention.scheduled} is false. */
    @Scheduled(fixedDelayString = "${foresignal.retention.interval:1h}",
            initialDelayString = "${foresignal.retention.initial-delay:5m}")
    public void scheduled() {
        if (!properties.scheduled()) {
            return;
        }
        try {
            run();
        } catch (ConflictException e) {
            log.info("Skipping the scheduled retention run: one is already active");
        } catch (RuntimeException e) {
            log.error("The scheduled retention run failed", e);
        }
    }

    /** Forgets the last run's result and lags (the test cleaner truncates what they describe). */
    @Override
    public void resetState() {
        lastResult = null;
        lastLags = List.of();
    }

    /** True while a run is active. */
    public boolean isRunning() {
        return running.get();
    }

    /** The result of the last completed run, or null before the first. */
    public RetentionRunResult lastResult() {
        return lastResult;
    }

    /** Consumers that released the clamp in the last run (for {@code RETENTION_CONSUMER_LAG}). */
    public List<RetentionRun.Lag> lastLags() {
        return lastLags;
    }

    /**
     * The consumers in force: the registered beans in registration order, then those of every
     * {@link RetentionConsumerProvider} (one per active chronicle, prompt 131). A provider that fails contributes none.
     */
    public List<RetentionConsumer> consumers() {
        if (providers.isEmpty()) {
            return consumers;
        }
        List<RetentionConsumer> all = new ArrayList<>(consumers);
        for (RetentionConsumerProvider provider : providers) {
            try {
                all.addAll(provider.retentionConsumers());
            } catch (RuntimeException e) {
                log.warn("A retention consumer provider failed; its consumers do not clamp this run", e);
            }
        }
        return List.copyOf(all);
    }

    /**
     * Runs retention synchronously.
     *
     * @throws ConflictException {@code ALREADY_RUNNING} when a run is active
     */
    public RetentionRunResult run() {
        if (!running.compareAndSet(false, true)) {
            throw new ConflictException(ConflictException.ALREADY_RUNNING, "A retention run is already active.");
        }
        long start = System.nanoTime();
        try {
            Instant now = clock.instant();
            RetentionSettings configured = resolver.settings();
            RetentionSettings applied = dbCap == null ? configured : dbCap.tighten(configured);
            RetentionRun run = pass(now, applied);
            RetentionDbCap.Outcome capped = null;
            if (dbCap != null) {
                try {
                    capped = dbCap.enforce(configured, pruner(), passes());
                } catch (RuntimeException e) {
                    log.error("The retention size cap failed", e);
                    run.error("db-cap: " + e.getMessage());
                }
            }
            long elapsed = System.nanoTime() - start;
            duration.record(elapsed, TimeUnit.NANOSECONDS);
            RetentionRunResult result = result(run, Duration.ofNanos(elapsed),
                    applied.capWindow().map(w -> (int) w.toDays()).orElse(null), capped);
            lastResult = result;
            lastLags = run.lags();
            log.info("Retention run: {} versions, {} partitions, {} heads, {} fetch_log, {} seq_clock, {} alerts,"
                            + " {} usage, {} notifications deleted in {} ms; skipped {}; clamp {}; errors {}",
                    result.versionsDeleted(), result.partitionsDropped(), result.headsPurged(),
                    result.fetchLogDeleted(), result.seqClockDeleted(), result.alertsDeleted(),
                    result.usageRowsDeleted(), result.notificationsDeleted(), result.durationMs(),
                    result.skippedSources(), result.consumerClamp().active() ? result.consumerClamp().consumer() : "none",
                    run.errors().size());
            return result;
        } finally {
            running.set(false);
        }
    }

    /** One pass of every step under {@code settings}: publishes the floors, alarms on lags, counts deletions. */
    private RetentionRun pass(Instant now, RetentionSettings settings) {
        return execute(newRun(now, settings));
    }

    /**
     * A run that is planned but never executed, for read-only questions about what a pass would do (prompt R2-016:
     * an archive dry run). It takes no guard.
     *
     * @param now the run's clock reading
     * @param settings the settings
     * @return the run
     */
    public RetentionRun scratch(Instant now, RetentionSettings settings) {
        return newRun(now, settings);
    }

    /** The passes the size cap's pruner plans and runs: at the current time, inside this run's guard. */
    private RetentionCapPruner.Passes passes() {
        return new RetentionCapPruner.Passes() {
            @Override
            public RetentionRun plan(RetentionSettings settings) {
                return newRun(clock.instant(), settings);
            }

            @Override
            public RetentionRun execute(RetentionRun run) {
                return RetentionJob.this.execute(run);
            }
        };
    }

    /** A run not yet executed, with the consumers in force. */
    private RetentionRun newRun(Instant now, RetentionSettings settings) {
        return new RetentionRun(now, settings, jdbc, seqClock, consumers());
    }

    /** Runs every step on {@code run}: publishes the floors, alarms on lags, counts deletions. */
    private RetentionRun execute(RetentionRun run) {
        for (RetentionStep step : steps) {
            try {
                step.run(run);
            } catch (RuntimeException e) {
                log.error("Retention step {} failed; continuing with the next step", step.name(), e);
                run.error(step.name() + ": " + e.getMessage());
            }
        }
        state.publish(run);
        alarm(run);
        run.deletedByTable().forEach((table, count) ->
                meters.counter(DELETED_METRIC, "table", table).increment(count));
        return run;
    }

    private void alarm(RetentionRun run) {
        for (RetentionRun.Lag lag : run.lags()) {
            meters.counter(CONSUMER_LAG_METRIC, "consumer", lag.consumer()).increment();
            log.warn("Retention released the clamp of consumer {}: its cursor {} is older than twice the {} window",
                    lag.consumer(), lag.cursorSeq(), lag.window());
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("consumer", lag.consumer());
            data.put("cursorSeq", lag.cursorSeq());
            data.put("window", ai.foresignal.settings.Durations.format(lag.window()));
            try {
                events.publishEvent(new SystemEvent("system.retention_consumer_lag", "warning",
                        "Retention consumer " + lag.consumer() + " is lagging",
                        "Its cursor (seq " + lag.cursorSeq() + ") is older than twice the retention window, so"
                                + " retention deleted versions it has not processed.",
                        "/v1/system", data));
            } catch (RuntimeException e) {
                log.warn("Could not publish the consumer-lag notification", e);
            }
        }
    }

    private RetentionRunResult result(RetentionRun run, Duration elapsed, Integer capWindowDays,
            RetentionDbCap.Outcome dbCapOutcome) {
        Map<String, RetentionRunResult.PerSource> perSource = new LinkedHashMap<>();
        TreeSet<String> ids = new TreeSet<>(run.versionsBySource().keySet());
        ids.addAll(run.floors().keySet());
        for (String id : ids) {
            long floor = state.floorSeq(id);
            perSource.put(id, new RetentionRunResult.PerSource(
                    run.versionsBySource().getOrDefault(id, 0L), floor > 0 || run.floors().containsKey(id) ? floor : null));
        }
        return new RetentionRunResult(
                run.now(),
                elapsed.toMillis(),
                run.deletedFrom("record_version"),
                run.headsPurged(),
                run.deletedFrom("alert"),
                run.deletedFrom("source_usage"),
                run.partitionsDropped(),
                run.headsPurged(),
                run.deletedFrom("fetch_log"),
                run.deletedFrom("seq_clock"),
                run.deletedFrom("notification"),
                List.copyOf(run.skippedSources()),
                new RetentionRunResult.ConsumerClamp(run.clampActive(), run.clampConsumer(), run.clampCursor()),
                perSource,
                capWindowDays,
                dbCapOutcome);
    }
}
