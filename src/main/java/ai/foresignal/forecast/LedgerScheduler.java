package ai.foresignal.forecast;

import ai.foresignal.forecast.baseline.BaselineRunner;
import ai.foresignal.forecast.baseline.PrecursorQuestionOpener;
import ai.foresignal.forecast.ensemble.EnsembleService;
import ai.foresignal.forecast.resolve.ResolutionService;
import ai.foresignal.forecast.score.ScoringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * The ledger's schedule (prompt 122), UTC: precursor questions every 5 minutes, baseline re-forecasts every 30
 * minutes (each baseline at most daily per question), resolution hourly at :07, the ensemble's staleness refresh daily
 * at 04:20 and a re-score of rows from an older scorer version daily at 04:40. The monthly ensemble refit is a step of
 * prompt 128's monthly maintenance, which calls {@link EnsembleService#refit}.
 * {@code foresignal.analytics.scheduled=false} turns all of it off (tests call the services directly).
 */
@Component
public class LedgerScheduler {

    private static final Logger log = LoggerFactory.getLogger(LedgerScheduler.class);

    private final PrecursorQuestionOpener opener;
    private final BaselineRunner baselines;
    private final ResolutionService resolution;
    private final EnsembleService ensemble;
    private final ScoringService scoring;
    private final boolean scheduled;

    /** Creates the scheduler. */
    public LedgerScheduler(PrecursorQuestionOpener opener, BaselineRunner baselines, ResolutionService resolution,
            EnsembleService ensemble, ScoringService scoring,
            @Value("${foresignal.analytics.scheduled:true}") boolean scheduled) {
        this.opener = opener;
        this.baselines = baselines;
        this.resolution = resolution;
        this.ensemble = ensemble;
        this.scoring = scoring;
        this.scheduled = scheduled;
    }

    /** Opens questions from new precursor-active findings. */
    @Scheduled(fixedDelayString = "5m", initialDelayString = "2m")
    public void precursorQuestions() {
        run("precursor question scan", opener::scan);
    }

    /** Re-runs the baselines on open questions. */
    @Scheduled(fixedDelayString = "30m", initialDelayString = "3m")
    public void baselines() {
        run("baseline run", baselines::runAll);
    }

    /** Resolves due questions. */
    @Scheduled(cron = "0 7 * * * *", zone = "UTC")
    public void resolve() {
        run("forecast resolution", resolution::runOnce);
    }

    /** Refreshes ensembles so stale members drop out. */
    @Scheduled(cron = "0 20 4 * * *", zone = "UTC")
    public void ensembles() {
        run("ensemble refresh", ensemble::refreshAll);
    }

    /** Re-scores rows of an older scorer version. */
    @Scheduled(cron = "0 40 4 * * *", zone = "UTC")
    public void rescore() {
        run("forecast re-score", scoring::rescoreOutdated);
    }

    private void run(String what, java.util.function.Supplier<?> task) {
        if (!scheduled) {
            return;
        }
        try {
            task.get();
        } catch (RuntimeException e) {
            log.warn("The {} failed: {}", what, e.toString());
        }
    }
}
