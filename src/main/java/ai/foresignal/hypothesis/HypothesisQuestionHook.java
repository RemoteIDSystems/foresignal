package ai.foresignal.hypothesis;

import ai.foresignal.forecast.QuestionResolutionListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Connects 122 resolutions to rolling hypotheses (prompt 125 §2 and §5): a resolved question resolves the sets linked to
 * it (hypotheses confirmed or refuted) and observes or rules out the {@code question} indicators that watch it. The
 * ledger calls it after the resolving transaction committed, so the work runs in a new transaction of its own.
 */
@Component
public class HypothesisQuestionHook implements QuestionResolutionListener {

    private final HypothesisService service;
    private final IndicatorWatchService watch;
    private final TransactionTemplate fresh;

    /** Creates the hook. */
    public HypothesisQuestionHook(HypothesisService service, IndicatorWatchService watch,
            PlatformTransactionManager transactions) {
        this.service = service;
        this.watch = watch;
        this.fresh = new TransactionTemplate(transactions);
        this.fresh.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public void questionResolved(String questionId, String outcome) {
        fresh.executeWithoutResult(status -> {
            watch.questionResolved(questionId, outcome);
            service.questionResolved(questionId);
        });
    }
}
