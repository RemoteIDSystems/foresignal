package ai.foresignal.cycle;

import ai.foresignal.analyst.AnalystProfileEntity;
import ai.foresignal.analyst.AnalystProfileRepository;
import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.config.StartupOrder;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Drives analysis cycle runs (prompt 128), after the 029/102 pattern: a 30-second poll claims due cycles with
 * {@code FOR UPDATE SKIP LOCKED}, computes the next fire time from now (a missed run runs once, as {@code missed},
 * and is never replayed), holds {@code running_since} so at most one run per cycle is in flight, and executes on a
 * virtual thread under a global limit of two. Manual runs and on-demand situation assessments take the same guarded
 * path ({@code CYCLE_RUN_IN_PROGRESS}, {@code LLM_NOT_CONFIGURED} only when roles need a provider).
 *
 * <p>The monthly maintenance belongs to the monthly cycle. The defaults are disabled, so when no monthly cycle is
 * enabled the maintenance still runs on the first of each month at 03:30 UTC (trigger {@code schedule}), keeping
 * base rates, precursors, ensemble weights, index weights and series methods current.
 */
@Component
public class CycleScheduler {

    private static final Logger log = LoggerFactory.getLogger(CycleScheduler.class);

    /** The global concurrent-run limit. */
    static final int GLOBAL_LIMIT = 2;

    /** How late a scheduled run may start before it counts as missed. */
    static final Duration MISSED_AFTER = Duration.ofMinutes(5);

    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactions;
    private final AnalysisCycleRepository cycles;
    private final CycleRunRepository runs;
    private final CycleRunner runner;
    private final CycleLlm llm;
    private final AnalystProfileRepository profiles;
    private final MonthlyMaintenance maintenance;
    private final Clock clock;
    private final boolean scheduled;

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final Semaphore global = new Semaphore(GLOBAL_LIMIT);
    private final AtomicBoolean started = new AtomicBoolean();
    private final Set<String> situationsRunning = ConcurrentHashMap.newKeySet();
    private volatile boolean enabled = true;

    /** Creates the scheduler. */
    public CycleScheduler(JdbcTemplate jdbc, TransactionTemplate transactions, AnalysisCycleRepository cycles,
            CycleRunRepository runs, CycleRunner runner, CycleLlm llm, AnalystProfileRepository profiles,
            MonthlyMaintenance maintenance, Clock clock,
            @Value("${foresignal.analytics.scheduled:true}") boolean scheduled) {
        this.jdbc = jdbc;
        this.transactions = transactions;
        this.cycles = cycles;
        this.runs = runs;
        this.runner = runner;
        this.llm = llm;
        this.profiles = profiles;
        this.maintenance = maintenance;
        this.clock = clock;
        this.scheduled = scheduled;
    }

    /** Starts once the application is ready. */
    @EventListener(ApplicationReadyEvent.class)
    @Order(StartupOrder.CYCLES + 1)
    public void start() {
        started.set(true);
    }

    /** Stops the scheduler. */
    @jakarta.annotation.PreDestroy
    public void stop() {
        started.set(false);
        executor.shutdown();
    }

    /** Polls for due cycles. */
    @Scheduled(fixedDelayString = "${foresignal.cycles.poll-interval:30s}",
            initialDelayString = "${foresignal.cycles.poll-interval:30s}")
    public void poll() {
        if (!started.get() || !enabled) {
            return;
        }
        try {
            tick();
        } catch (RuntimeException e) {
            log.warn("Cycle poll failed: {}", e.toString());
        }
    }

    /** Claims and launches every due cycle; returns the queued run ids. */
    public List<String> tick() {
        Instant now = clock.instant();
        List<String> queued = new ArrayList<>();
        for (Claim claim : claimDue(now)) {
            AnalysisCycleEntity cycle = cycles.findById(claim.cycleId()).orElse(null);
            if (cycle == null) {
                continue;
            }
            try {
                CycleRunEntity run = runner.queue(cycle, claim.trigger());
                queued.add(run.getId());
                executor.submit(() -> launch(cycle.getId(), run.getId()));
            } catch (RuntimeException e) {
                release(cycle.getId());
                log.warn("Could not queue cycle {}: {}", cycle.getId(), e.toString());
            }
        }
        return queued;
    }

    /** A claimed cycle and its trigger. */
    record Claim(String cycleId, String trigger) {}

    private List<Claim> claimDue(Instant now) {
        return transactions.execute(status -> {
            List<Claim> out = new ArrayList<>();
            jdbc.query("SELECT id, next_run_at FROM analysis_cycle WHERE enabled AND next_run_at IS NOT NULL"
                    + " AND next_run_at <= ? AND (running_since IS NULL OR running_since < ?)"
                    + " ORDER BY next_run_at, id LIMIT 20 FOR UPDATE SKIP LOCKED", rs -> {
                        Instant due = rs.getTimestamp("next_run_at").toInstant();
                        out.add(new Claim(rs.getString("id"), due.isBefore(now.minus(MISSED_AFTER)) ? "missed"
                                : "schedule"));
                    }, Timestamp.from(now), Timestamp.from(now.minus(CycleService.STALE)));
            for (Claim claim : out) {
                AnalysisCycleEntity cycle = cycles.findById(claim.cycleId()).orElse(null);
                Instant next = cycle == null ? null : CycleService.next(cycle, now);
                jdbc.update("UPDATE analysis_cycle SET next_run_at = ?, running_since = ? WHERE id = ?",
                        next == null ? null : Timestamp.from(next), Timestamp.from(now), claim.cycleId());
            }
            return out;
        });
    }

    private void launch(String cycleId, String runId) {
        try {
            global.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            release(cycleId);
            return;
        }
        try {
            runner.execute(runId);
        } catch (RuntimeException e) {
            log.warn("Cycle run {} failed: {}", runId, e.toString());
        } finally {
            global.release();
            release(cycleId);
        }
    }

    /**
     * A manual run ({@code POST /v1/analysis/cycles/{id}/run}, MCP {@code run_cycle}) through the guarded path.
     *
     * @return the queued run
     * @throws ConflictException 409 {@code CYCLE_RUN_IN_PROGRESS}, or {@code LLM_NOT_CONFIGURED} when the cycle has
     *     roles and no usable provider (a cycle without roles never needs one)
     */
    public CycleRunEntity runNow(AnalysisCycleEntity cycle) {
        if (!cycle.getRoles().isEmpty() && !llm.usable(llm.providerFor(profile(cycle)))) {
            throw new ConflictException(ErrorCodes.LLM_NOT_CONFIGURED, "The cycle runs LLM roles " + cycle.getRoles()
                    + " but no chat provider is configured; configure one or remove the roles for a digest-only run.");
        }
        Instant now = clock.instant();
        int claimed = jdbc.update("UPDATE analysis_cycle SET running_since = ? WHERE id = ?"
                + " AND (running_since IS NULL OR running_since < ?)", Timestamp.from(now), cycle.getId(),
                Timestamp.from(now.minus(CycleService.STALE)));
        if (claimed == 0) {
            throw new ConflictException(ErrorCodes.CYCLE_RUN_IN_PROGRESS, "Analysis cycle " + cycle.getId()
                    + " already has a run in progress.");
        }
        CycleRunEntity run;
        try {
            run = runner.queue(cycle, "manual");
        } catch (RuntimeException e) {
            release(cycle.getId());
            throw e;
        }
        String runId = run.getId();
        executor.submit(() -> launch(cycle.getId(), runId));
        return run;
    }

    /**
     * An on-demand situation assessment through the guarded path.
     *
     * @return the queued run
     * @throws ConflictException 409 {@code CYCLE_RUN_IN_PROGRESS} or {@code LLM_NOT_CONFIGURED}
     */
    public CycleRunEntity runSituation(String situationId, CycleInputs.SituationRequest request) {
        if (!request.roles().isEmpty() && !llm.usable(llm.providerFor(null))) {
            throw new ConflictException(ErrorCodes.LLM_NOT_CONFIGURED, "The assessment runs LLM roles "
                    + request.roles() + " but no chat provider is configured.");
        }
        if (!situationsRunning.add(situationId)
                || runs.countBySituationIdAndStatusIn(situationId, CycleRunner.ACTIVE) > 0) {
            situationsRunning.remove(situationId);
            throw new ConflictException(ErrorCodes.CYCLE_RUN_IN_PROGRESS, "Situation " + situationId
                    + " already has an assessment in progress.");
        }
        CycleRunEntity run;
        try {
            run = runner.queueSituation(situationId, request.roles(), request.effort());
        } catch (RuntimeException e) {
            situationsRunning.remove(situationId);
            throw e;
        }
        String runId = run.getId();
        executor.submit(() -> {
            try {
                global.acquire();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                situationsRunning.remove(situationId);
                return;
            }
            try {
                runner.execute(runId);
            } catch (RuntimeException e) {
                log.warn("Situation assessment {} failed: {}", runId, e.toString());
            } finally {
                global.release();
                situationsRunning.remove(situationId);
            }
        });
        return run;
    }

    /**
     * Starts the monthly maintenance now ({@code POST /v1/analysis/maintenance/run}) and runs it in the background.
     *
     * @throws ConflictException 409 {@code MAINTENANCE_IN_PROGRESS}
     */
    public MaintenanceRunEntity maintenanceNow(String trigger) {
        MaintenanceRunEntity run = maintenance.start(trigger, null);
        String provider = llm.providerFor(monthlyProfile());
        executor.submit(() -> {
            try {
                maintenance.execute(run.getId(), provider);
            } catch (RuntimeException e) {
                log.warn("Maintenance {} failed: {}", run.getId(), e.toString());
            }
        });
        return run;
    }

    /** The monthly maintenance when no monthly cycle is enabled (the 1st, 03:30 UTC). */
    @Scheduled(cron = "0 30 3 1 * *", zone = "UTC")
    public void monthlyFallback() {
        if (!scheduled || !started.get() || !enabled) {
            return;
        }
        if (!cycles.findByCadenceAndEnabledTrue(CycleVocabulary.MONTHLY).isEmpty()) {
            return;
        }
        try {
            maintenanceNow("schedule");
        } catch (ConflictException e) {
            log.info("Monthly maintenance skipped: {}", e.getMessage());
        } catch (RuntimeException e) {
            log.warn("Monthly maintenance could not start: {}", e.toString());
        }
    }

    private AnalystProfileEntity monthlyProfile() {
        for (AnalysisCycleEntity c : cycles.findByCadenceAndEnabledTrue(CycleVocabulary.MONTHLY)) {
            AnalystProfileEntity p = profile(c);
            if (p != null) {
                return p;
            }
        }
        return null;
    }

    private AnalystProfileEntity profile(AnalysisCycleEntity cycle) {
        return cycle.getAnalystProfileId() == null ? null : profiles.findById(cycle.getAnalystProfileId())
                .orElse(null);
    }

    private void release(String cycleId) {
        try {
            jdbc.update("UPDATE analysis_cycle SET running_since = NULL WHERE id = ?", cycleId);
        } catch (RuntimeException e) {
            log.warn("Could not release analysis cycle {}: {}", cycleId, e.toString());
        }
    }

    /** True while running. */
    public boolean running() {
        return started.get();
    }

    /** Enables or disables the scheduler (tests). */
    public void setEnabled(boolean value) {
        this.enabled = value;
    }
}
