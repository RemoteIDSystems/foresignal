package ai.foresignal.precursor;

import ai.foresignal.api.error.ConflictException;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * The precursor schedule (prompt 120), all UTC: mining at 01:30 as a 103 job ({@code precursor-mining}, skipped while
 * another run is queued or running), the live watch every minute, and the series-antecedent check at 06:15 (after the
 * day's series have been published and registered). The monthly re-test is triggered by prompt 128's monthly cycle or
 * by {@code POST /v1/precursors/retest}. {@code foresignal.analytics.scheduled=false} turns all of it off (tests call
 * the services directly).
 */
@Component
public class PrecursorScheduler {

    private static final Logger log = LoggerFactory.getLogger(PrecursorScheduler.class);

    private final PrecursorService service;
    private final PrecursorLiveWatch watch;
    private final Clock clock;
    private final boolean scheduled;

    /** Creates the scheduler. */
    public PrecursorScheduler(PrecursorService service, PrecursorLiveWatch watch, Clock clock,
            @Value("${foresignal.analytics.scheduled:true}") boolean scheduled) {
        this.service = service;
        this.watch = watch;
        this.clock = clock;
        this.scheduled = scheduled;
    }

    /** Nightly mining at 01:30 UTC. */
    @Scheduled(cron = "0 30 1 * * *", zone = "UTC")
    public void mine() {
        run("precursor mining", () -> {
            try {
                log.info("Precursor mining queued as job {}", service.submit("mine", "schedule", null).getId());
            } catch (ConflictException e) {
                log.info("Precursor mining skipped: {}", e.getMessage());
            }
        });
    }

    /** The live watch. */
    @Scheduled(fixedDelayString = "PT1M", initialDelayString = "PT4M")
    public void poll() {
        run("precursor live watch", watch::poll);
    }

    /** The series-antecedent check at 06:15 UTC. */
    @Scheduled(cron = "0 15 6 * * *", zone = "UTC")
    public void series() {
        run("precursor series check", () -> log.info("Series precursors active: {}", watch.scanSeries(clock.instant())));
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
