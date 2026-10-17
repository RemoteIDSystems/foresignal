package ai.foresignal.forecast;

import ai.foresignal.forecast.baseline.BaselineRunner;
import ai.foresignal.forecast.ensemble.EnsembleService;
import ai.foresignal.forecast.score.ScoringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The ledger's {@link LedgerHooks} (prompt 122): a new question gets its baseline forecasts on a virtual thread
 * (when background analytics run; tests call {@link BaselineRunner} directly), every non-ensemble forecast refreshes
 * the question's ensemble in the same transaction, and every resolution scores the question.
 */
@Component
public class LedgerCoordinator implements LedgerHooks {

    private static final Logger log = LoggerFactory.getLogger(LedgerCoordinator.class);

    private final ObjectProvider<BaselineRunner> baselines;
    private final ObjectProvider<EnsembleService> ensemble;
    private final ObjectProvider<ScoringService> scoring;
    private final boolean background;

    /** Creates the coordinator. */
    public LedgerCoordinator(ObjectProvider<BaselineRunner> baselines, ObjectProvider<EnsembleService> ensemble,
            ObjectProvider<ScoringService> scoring,
            @Value("${foresignal.analytics.scheduled:true}") boolean background) {
        this.baselines = baselines;
        this.ensemble = ensemble;
        this.scoring = scoring;
        this.background = background;
    }

    @Override
    public void questionOpened(String questionId) {
        BaselineRunner runner = baselines.getIfAvailable();
        if (!background || runner == null) {
            return;
        }
        Thread.ofVirtual().name("baselines-" + questionId).start(() -> {
            try {
                runner.runFor(questionId);
            } catch (RuntimeException e) {
                log.warn("Baselines of question {} failed: {}", questionId, e.toString());
            }
        });
    }

    @Override
    public void forecastRecorded(ForecastQuestionEntity question, ForecastEntity forecast) {
        EnsembleService service = ensemble.getIfAvailable();
        if (service != null) {
            service.refresh(question);
        }
    }

    @Override
    public void questionResolved(ForecastQuestionEntity question) {
        ScoringService service = scoring.getIfAvailable();
        if (service != null) {
            service.scoreQuestion(question);
        }
    }
}
