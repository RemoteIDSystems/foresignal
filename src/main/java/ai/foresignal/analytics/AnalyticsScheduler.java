package ai.foresignal.analytics;

import ai.foresignal.analytics.changepoint.ChangePointService;
import ai.foresignal.analytics.cooccurrence.CoOccurrenceService;
import ai.foresignal.analytics.eventstudy.LiveEventStudyService;
import ai.foresignal.analytics.registry.AnalysisSeriesRegistrar;
import java.time.Clock;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * The discovery layer's schedule (prompt 119), all UTC: analysis-series auto-registration at 02:00, change points on
 * daily and weekly series at 03:00 (on the last closed day), on opt-in hourly series at :20 each hour, the
 * unexplained-move scan at 04:00, co-occurrence mining at 05:00, and the live event-study poll every minute.
 * {@code foresignal.analytics.scheduled=false} turns all of it off (tests call the services directly).
 */
@Component
public class AnalyticsScheduler {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsScheduler.class);

    private final AnalysisSeriesRegistrar registrar;
    private final ChangePointService changePoints;
    private final LiveEventStudyService live;
    private final CoOccurrenceService coOccurrence;
    private final Clock clock;
    private final boolean scheduled;

    /** Creates the scheduler. */
    public AnalyticsScheduler(AnalysisSeriesRegistrar registrar, ChangePointService changePoints,
            LiveEventStudyService live, CoOccurrenceService coOccurrence, Clock clock,
            @Value("${foresignal.analytics.scheduled:true}") boolean scheduled) {
        this.registrar = registrar;
        this.changePoints = changePoints;
        this.live = live;
        this.coOccurrence = coOccurrence;
        this.clock = clock;
        this.scheduled = scheduled;
    }

    /** Auto-registration at 02:00 UTC. */
    @Scheduled(cron = "0 0 2 * * *", zone = "UTC")
    public void register() {
        run("auto-registration", () -> log.info("Analysis series auto-registration: {}", registrar.run(now())));
    }

    /** Change points on daily and weekly series at 03:00 UTC. */
    @Scheduled(cron = "0 0 3 * * *", zone = "UTC")
    public void dailyChangePoints() {
        run("daily change points", () -> {
            Instant now = now();
            log.info("Change points (day): {}; (week): {}", changePoints.run("day", now), changePoints.run("week", now));
        });
    }

    /** Change points on hourly series at :20. */
    @Scheduled(cron = "0 20 * * * *", zone = "UTC")
    public void hourlyChangePoints() {
        run("hourly change points", () -> changePoints.run("hour", now()));
    }

    /** The unexplained-move scan at 04:00 UTC. */
    @Scheduled(cron = "0 0 4 * * *", zone = "UTC")
    public void unexplainedMoves() {
        run("unexplained moves", () -> log.info("Unexplained moves written: {}", live.scanMoves(now())));
    }

    /** Co-occurrence mining at 05:00 UTC. */
    @Scheduled(cron = "0 0 5 * * *", zone = "UTC")
    public void coOccurrence() {
        run("co-occurrence", () -> log.info("Co-occurrence: {}", coOccurrence.run(now())));
    }

    /** The live event-study poll. */
    @Scheduled(fixedDelayString = "PT1M", initialDelayString = "PT3M")
    public void livePoll() {
        run("live event studies", live::poll);
    }

    private Instant now() {
        return clock.instant();
    }

    private void run(String what, Runnable task) {
        if (!scheduled) {
            return;
        }
        try {
            task.run();
        } catch (RuntimeException e) {
            log.warn("The {} run failed: {}", what, e.toString());
        }
    }
}
