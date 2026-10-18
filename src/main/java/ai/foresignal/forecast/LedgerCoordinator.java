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
 * the question's ensemble in the same transaction, and every resolution scores the question and, once it commits,
 * tells the {@link QuestionResolutionListener}s (prompt 125: hypothesis sets and question indicators).
 */
@Component
public class LedgerCoordinator implements LedgerHooks {

    private static final Logger log = LoggerFactory.getLogger(LedgerCoordinator.class);

    private final ObjectProvider<BaselineRunner> baselines;
    private final ObjectProvider<EnsembleService> ensemble;
    private final ObjectProvider<ScoringService> scoring;
    private final boolean background;
    private ObjectProvider<QuestionResolutionListener> resolutionListeners;

    /** Creates the coordinator. */
    public LedgerCoordinator(ObjectProvider<BaselineRunner> baselines, ObjectProvider<EnsembleService> ensemble,
            ObjectProvider<ScoringService> scoring,
            @Value("${foresignal.analytics.scheduled:true}") boolean background) {
        this.baselines = baselines;
        this.ensemble = ensemble;
        this.scoring = scoring;
        this.background = background;
    }

    /** Injects the resolution listeners (prompt 125); optional. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    void setResolutionListeners(ObjectProvider<QuestionResolutionListener> resolutionListeners) {
        this.resolutionListeners = resolutionListeners;
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
        if (resolutionListeners != null) {
            String id = question.getId();
            Object outcome = question.getResolution() == null ? null : question.getResolution().get("outcome");
            String label = outcome == null ? null : outcome.toString();
            ForecastEvents.afterCommit(() -> resolutionListeners.orderedStream().forEach(listener -> {
                try {
                    listener.questionResolved(id, label);
                } catch (RuntimeException e) {
                    log.warn("A resolution listener failed on question {}: {}", id, e.toString());
                }
            }));
        }
    }

    /** Tells the resolution listeners, once the annulment commits, that the question was annulled (prompt 142). */
    @Override
    public void questionAnnulled(ForecastQuestionEntity question) {
        if (resolutionListeners == null) {
            return;
        }
        String id = question.getId();
        ForecastEvents.afterCommit(() -> resolutionListeners.orderedStream().forEach(listener -> {
            try {
                listener.questionAnnulled(id);
            } catch (RuntimeException e) {
                log.warn("A resolution listener failed on annulled question {}: {}", id, e.toString());
            }
        }));
    }
}
