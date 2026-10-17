package ai.foresignal.baserate;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.baserate.reference.OwnHistoryFold;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * The base-rate schedule (prompt 121), UTC: the own-history fold nightly at 02:15, and the automatic-rate refresh on
 * the first of each month at 03:30 (until prompt 128's monthly cycle calls {@link BaseRateService#submitRefresh}
 * itself). {@code foresignal.analytics.scheduled=false} turns both off (tests call the services directly).
 */
@Component
public class BaseRateScheduler {

    private static final Logger log = LoggerFactory.getLogger(BaseRateScheduler.class);

    private final OwnHistoryFold fold;
    private final BaseRateService service;
    private final boolean scheduled;

    /** Creates the scheduler. */
    public BaseRateScheduler(OwnHistoryFold fold, BaseRateService service,
            @Value("${foresignal.analytics.scheduled:true}") boolean scheduled) {
        this.fold = fold;
        this.service = service;
        this.scheduled = scheduled;
    }

    /** The nightly own-history fold. */
    @Scheduled(cron = "0 15 2 * * *", zone = "UTC")
    public void fold() {
        run("own-history fold", fold::fold);
    }

    /** The monthly refresh. */
    @Scheduled(cron = "0 30 3 1 * *", zone = "UTC")
    public void monthly() {
        run("monthly base-rate refresh", () -> {
            try {
                log.info("Base-rate refresh queued as job {}", service.submitRefresh("schedule", null).getId());
            } catch (ConflictException e) {
                log.info("Base-rate refresh skipped: {}", e.getMessage());
            }
        });
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
