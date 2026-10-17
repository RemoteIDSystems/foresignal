package ai.foresignal.situation;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * The situation layer's periodic work (prompt 118): the daily snapshot job at 00:10 UTC for the previous UTC day,
 * the lifecycle sweep every 15 minutes (open → dormant after {@code dormancyWindow}, dormant → closed after 90
 * days) and the retry of pending briefs every 5 minutes. {@code foresignal.situations.scheduled=false} turns all
 * three off (tests drive {@link SituationSnapshots#snapshot}, {@link SituationService#sweep} and
 * {@link SituationBriefService#retryPending} directly).
 */
@Component
public class SituationScheduler {

    private static final Logger log = LoggerFactory.getLogger(SituationScheduler.class);

    private final SituationSnapshots snapshots;
    private final SituationService service;
    private final SituationBriefService briefs;
    private final SituationSettings settings;
    private final Clock clock;
    private final boolean scheduled;

    /** Creates the scheduler. */
    public SituationScheduler(SituationSnapshots snapshots, SituationService service, SituationBriefService briefs,
            SituationSettings settings, Clock clock,
            @Value("${foresignal.situations.scheduled:true}") boolean scheduled) {
        this.snapshots = snapshots;
        this.service = service;
        this.briefs = briefs;
        this.settings = settings;
        this.clock = clock;
        this.scheduled = scheduled;
    }

    /** Writes yesterday's snapshots at 00:10 UTC. */
    @Scheduled(cron = "0 10 0 * * *", zone = "UTC")
    public void dailySnapshots() {
        if (!scheduled) {
            return;
        }
        LocalDate yesterday = clock.instant().atZone(ZoneOffset.UTC).toLocalDate().minusDays(1);
        try {
            snapshots.snapshot(yesterday);
        } catch (RuntimeException e) {
            log.warn("Situation snapshots for {} failed: {}", yesterday, e.toString());
        }
    }

    /** Applies the clock's lifecycle transitions. */
    @Scheduled(fixedDelayString = "PT15M", initialDelayString = "PT2M")
    public void sweep() {
        if (!scheduled) {
            return;
        }
        try {
            service.sweep(clock.instant(), settings.dormancyWindow());
        } catch (RuntimeException e) {
            log.warn("Situation lifecycle sweep failed: {}", e.toString());
        }
    }

    /** Retries pending briefs. */
    @Scheduled(fixedDelayString = "PT5M", initialDelayString = "PT3M")
    public void briefs() {
        if (!scheduled) {
            return;
        }
        try {
            briefs.retryPending();
        } catch (RuntimeException e) {
            log.warn("Situation brief retry failed: {}", e.toString());
        }
    }
}
