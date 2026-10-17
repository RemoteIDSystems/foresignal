package ai.foresignal.outlook;

import ai.foresignal.outlook.index.IndexFitService;
import ai.foresignal.outlook.index.RegionalIndexService;
import ai.foresignal.outlook.regional.RegionalOutlookService;
import ai.foresignal.outlook.series.SeriesForecastService;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * The schedule of prompt 126, all UTC: series forecasts (with method re-selection after 30 days and the exceedance
 * checks) nightly at 03:30, after the 03:00 change-point run; regional index values at 03:45; regional outlooks on
 * Mondays at 06:00 (families refitted first when their fits are older than 30 days); index weight fits on the 1st of
 * each month at 04:30 (prompt 128's monthly cycle is not built yet). {@code foresignal.analytics.scheduled=false}
 * turns all of it off (tests call the services directly).
 */
@Component
public class OutlookScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutlookScheduler.class);

    private final SeriesForecastService series;
    private final RegionalOutlookService outlooks;
    private final RegionalIndexService indices;
    private final IndexFitService fits;
    private final Clock clock;
    private final boolean scheduled;

    /** Creates the scheduler. */
    public OutlookScheduler(SeriesForecastService series, RegionalOutlookService outlooks,
            RegionalIndexService indices, IndexFitService fits, Clock clock,
            @Value("${foresignal.analytics.scheduled:true}") boolean scheduled) {
        this.series = series;
        this.outlooks = outlooks;
        this.indices = indices;
        this.fits = fits;
        this.clock = clock;
        this.scheduled = scheduled;
    }

    /** Nightly series forecasts at 03:30 UTC. */
    @Scheduled(cron = "0 30 3 * * *", zone = "UTC")
    public void seriesForecasts() {
        run("series forecasts", () -> log.info("Series forecasts: {}", series.runNightly(clock.instant())));
    }

    /** Daily index values at 03:45 UTC. */
    @Scheduled(cron = "0 45 3 * * *", zone = "UTC")
    public void indexValues() {
        run("regional indices", () -> log.info("Regional indices: {}", indices.computeDaily(clock.instant())));
    }

    /** Weekly outlooks on Mondays at 06:00 UTC. */
    @Scheduled(cron = "0 0 6 * * MON", zone = "UTC")
    public void weeklyOutlooks() {
        run("regional outlooks", () -> log.info("Regional outlooks: {}", outlooks.publishAll(clock.instant())));
    }

    /** Monthly index fits on the 1st at 04:30 UTC. */
    @Scheduled(cron = "0 30 4 1 * *", zone = "UTC")
    public void monthlyFits() {
        run("index fits", () -> log.info("Index fits submitted: {}", fits.submitMonthly().size()));
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
