package ai.foresignal.briefing;

import ai.foresignal.config.StartupOrder;
import jakarta.annotation.PreDestroy;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Runs briefings when they are due (prompt 029, Architecture §22).
 *
 * <p>Every {@code foresignal.briefing.poll-interval} (30 s) one transaction selects enabled briefings
 * with {@code next_run_at <= now} and no run in progress, {@code FOR UPDATE SKIP LOCKED}, sets each
 * one's {@code next_run_at} to its next fire time <em>after now</em> and marks it running, then
 * commits. Each claimed briefing then runs on its own virtual thread. Because the next fire time is
 * computed from now, runs missed while Foresignal was down run once and are never replayed, and two
 * schedulers (two instances, or two ticks) never claim the same briefing.
 */
@Component
public class BriefingScheduler {

    private static final Logger log = LoggerFactory.getLogger(BriefingScheduler.class);

    private final BriefingRepository briefings;
    private final BriefingService service;
    private final BriefingProperties properties;
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactions;
    private final Clock clock;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private volatile boolean started;

    /** Creates the scheduler. */
    public BriefingScheduler(BriefingRepository briefings, BriefingService service, BriefingProperties properties,
            JdbcTemplate jdbc, PlatformTransactionManager transactionManager, Clock clock) {
        this.briefings = briefings;
        this.service = service;
        this.properties = properties;
        this.jdbc = jdbc;
        this.transactions = new TransactionTemplate(transactionManager);
        this.clock = clock;
    }

    /** Starts ticking once the application is ready, after the collector scheduler. */
    @EventListener(ApplicationReadyEvent.class)
    @Order(StartupOrder.BRIEFINGS)
    public void start() {
        started = true;
    }

    /** Stops running briefings. */
    @PreDestroy
    public void stop() {
        started = false;
        executor.shutdown();
    }

    /** The scheduled poll; does nothing when {@code foresignal.briefing.scheduled} is false. */
    @Scheduled(fixedDelayString = "${foresignal.briefing.poll-interval:30s}",
            initialDelayString = "${foresignal.briefing.poll-interval:30s}")
    public void poll() {
        if (!started || !properties.scheduled()) {
            return;
        }
        try {
            tick();
        } catch (RuntimeException e) {
            log.error("Briefing scheduler tick failed", e);
        }
    }

    /**
     * Claims every due briefing and starts its run.
     *
     * @return one future per started run (tests await them)
     */
    public List<Future<BriefingRunEntity>> tick() {
        Instant now = clock.instant();
        List<BriefingEntity> claimed = transactions.execute(status -> {
            List<UUID> ids = jdbc.queryForList("SELECT id FROM briefing WHERE enabled AND next_run_at <= ?"
                    + " AND (running_since IS NULL OR running_since < ?) ORDER BY next_run_at, id LIMIT ?"
                    + " FOR UPDATE SKIP LOCKED", UUID.class, Timestamp.from(now),
                    Timestamp.from(now.minus(properties.staleRun())), properties.batchSize());
            List<BriefingEntity> out = new ArrayList<>();
            for (UUID id : ids) {
                BriefingEntity briefing = briefings.findById(id).orElse(null);
                if (briefing == null) {
                    continue;
                }
                Instant next = null;
                try {
                    next = BriefingValidator.nextRun(briefing.getSchedule(), briefing.getTimezone(), now);
                } catch (IllegalArgumentException e) {
                    log.warn("Briefing {} has an unusable schedule '{}'; it is not rescheduled", id,
                            briefing.getSchedule());
                }
                jdbc.update("UPDATE briefing SET next_run_at = ?, running_since = ? WHERE id = ?",
                        next == null ? null : Timestamp.from(next), Timestamp.from(now), id);
                out.add(briefing);
            }
            return out;
        });
        List<Future<BriefingRunEntity>> futures = new ArrayList<>();
        for (BriefingEntity briefing : claimed == null ? List.<BriefingEntity>of() : claimed) {
            futures.add(executor.submit(() -> service.execute(briefing)));
        }
        return futures;
    }
}
