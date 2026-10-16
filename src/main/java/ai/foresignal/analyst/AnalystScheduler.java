package ai.foresignal.analyst;

import ai.foresignal.config.StartupOrder;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Drives the analyst runs (prompt 102), after the briefing scheduler's pattern: a 30-second poll
 * claims due profiles with {@code FOR UPDATE SKIP LOCKED}, computes the next fire time from now (a
 * missed run runs once and is never replayed), marks {@code running_since} so at most one run per
 * profile is in flight, and executes on a virtual thread under a global limit of two.
 */
@Component
public class AnalystScheduler {

    private static final Logger log = LoggerFactory.getLogger(AnalystScheduler.class);

    /** The global concurrent-run limit. */
    static final int GLOBAL_LIMIT = 2;

    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactions;
    private final AnalystProfileRepository profiles;
    private final AnalystRunner runner;
    private final Clock clock;

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final Semaphore global = new Semaphore(GLOBAL_LIMIT);
    private final AtomicBoolean started = new AtomicBoolean();
    private volatile boolean enabled = true;

    /** Creates the scheduler. */
    public AnalystScheduler(JdbcTemplate jdbc, TransactionTemplate transactions, AnalystProfileRepository profiles,
            AnalystRunner runner, Clock clock) {
        this.jdbc = jdbc;
        this.transactions = transactions;
        this.profiles = profiles;
        this.runner = runner;
        this.clock = clock;
    }

    /** Starts once the application is ready. */
    @EventListener(ApplicationReadyEvent.class)
    @Order(StartupOrder.ANALYST)
    public void start() {
        started.set(true);
    }

    /** Stops the scheduler. */
    @jakarta.annotation.PreDestroy
    public void stop() {
        started.set(false);
        executor.shutdown();
    }

    /** Polls for due profiles. */
    @Scheduled(fixedDelayString = "${foresignal.analyst.poll-interval:30s}",
            initialDelayString = "${foresignal.analyst.poll-interval:30s}")
    public void poll() {
        if (!started.get() || !enabled) {
            return;
        }
        try {
            tick();
        } catch (RuntimeException e) {
            log.warn("Analyst poll failed: {}", e.toString());
        }
    }

    /** Claims and launches each due profile; returns the claimed profile ids. */
    public List<String> tick() {
        Instant now = clock.instant();
        List<String> due = claimDue(now);
        for (String id : due) {
            executor.submit(() -> launch(id));
        }
        return due;
    }

    private List<String> claimDue(Instant now) {
        return transactions.execute(status -> {
            List<String> ids = jdbc.queryForList(
                    "SELECT id FROM analyst_profile WHERE enabled AND next_run_at IS NOT NULL AND next_run_at <= ?"
                            + " AND (running_since IS NULL OR running_since < ?) ORDER BY next_run_at, id LIMIT 20"
                            + " FOR UPDATE SKIP LOCKED",
                    String.class, java.sql.Timestamp.from(now), java.sql.Timestamp.from(now.minus(Duration.ofMinutes(30))));
            for (String id : ids) {
                AnalystProfileEntity profile = profiles.findById(id).orElse(null);
                if (profile == null) {
                    continue;
                }
                Instant next = AnalystSchedule.of(profile.getSchedule()).next(now);
                jdbc.update("UPDATE analyst_profile SET next_run_at = ?, running_since = ? WHERE id = ?",
                        java.sql.Timestamp.from(next), java.sql.Timestamp.from(now), id);
            }
            return ids;
        });
    }

    private void launch(String id) {
        try {
            global.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            release(id);
            return;
        }
        try {
            profiles.findById(id).ifPresent(runner::run);
        } finally {
            global.release();
        }
    }

    /**
     * A manual run ({@code POST /v1/analyst/profiles/{id}/run}, MCP {@code run_analyst}) through the same
     * guarded path as scheduled runs: the profile is claimed atomically ({@code running_since}, so at most
     * one run per profile), the run is queued, and it executes on the scheduler's executor under the global
     * limit of {@value #GLOBAL_LIMIT}. The daily run cap ({@code maxRunsPerDay}) is enforced by the runner.
     *
     * @return the queued run
     * @throws ai.foresignal.api.error.ConflictException {@code 409 ANALYST_RUN_IN_PROGRESS} when the profile
     *     is already running
     */
    public AnalystRunEntity runNow(AnalystProfileEntity profile) {
        Instant now = clock.instant();
        int claimed = jdbc.update("UPDATE analyst_profile SET running_since = ? WHERE id = ?"
                + " AND (running_since IS NULL OR running_since < ?)", java.sql.Timestamp.from(now), profile.getId(),
                java.sql.Timestamp.from(now.minus(STALE)));
        if (claimed == 0) {
            throw new ai.foresignal.api.error.ConflictException(ai.foresignal.api.error.ErrorCodes.ANALYST_RUN_IN_PROGRESS,
                    "Analyst profile " + profile.getId() + " is already running.");
        }
        AnalystRunEntity run;
        try {
            run = runner.queued(profile);
        } catch (RuntimeException e) {
            release(profile.getId());
            throw e;
        }
        String runId = run.getId();
        executor.submit(() -> {
            try {
                global.acquire();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                release(profile.getId());
                return;
            }
            try {
                profiles.findById(profile.getId()).ifPresentOrElse(p -> runner.executeQueued(p, runId),
                        () -> release(profile.getId()));
            } finally {
                global.release();
            }
        });
        return run;
    }

    /** How long a {@code running_since} claim is honored before it is treated as stale. */
    static final Duration STALE = Duration.ofMinutes(30);

    private void release(String id) {
        try {
            jdbc.update("UPDATE analyst_profile SET running_since = NULL WHERE id = ?", id);
        } catch (RuntimeException e) {
            log.warn("Could not release analyst profile {}: {}", id, e.toString());
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
