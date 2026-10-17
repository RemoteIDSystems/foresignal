package ai.foresignal.forecast.resolve;

import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecastQuestionRepository;
import ai.foresignal.forecast.ForecastService;
import ai.foresignal.forecast.ForecastStatus;
import ai.foresignal.forecast.score.ScoringService;
import ai.foresignal.manifest.ManifestRequest;
import ai.foresignal.manifest.ManifestService;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * The resolution job (prompt 122 §3), run hourly by {@code LedgerScheduler} and on demand ({@link #runOnce}): closes
 * questions past {@code closes_at}, evaluates every open or closed machine-resolved question with
 * {@link ResolverEngine} (early, after release lags, annulled on coverage gaps or low detectability), raises
 * {@code forecast.resolution_needed} once for manual and {@code hypothesis-outcome} questions at {@code resolve_by},
 * and empties the scorecard cache after the batch. Each resolution pins its evidence, stores a 116 manifest and emits
 * {@code question.resolved}; scores follow through the ledger's hooks. Each question is handled in its own transaction,
 * so one failure does not stop the batch.
 */
@Service
public class ResolutionService {

    private static final Logger log = LoggerFactory.getLogger(ResolutionService.class);

    private final ForecastQuestionRepository questions;
    private final ForecastService ledger;
    private final ResolutionData data;
    private final ScoringService scoring;
    private final ObjectProvider<ManifestService> manifests;
    private final TransactionTemplate transactions;
    private final Clock clock;

    /** Creates the service. */
    public ResolutionService(ForecastQuestionRepository questions, ForecastService ledger, ResolutionData data,
            ScoringService scoring, ObjectProvider<ManifestService> manifests, TransactionTemplate transactions,
            Clock clock) {
        this.questions = questions;
        this.ledger = ledger;
        this.data = data;
        this.scoring = scoring;
        this.manifests = manifests;
        this.transactions = transactions;
        this.clock = clock;
    }

    /**
     * What one run did.
     *
     * @param closed questions closed
     * @param resolved questions resolved
     * @param annulled questions annulled
     * @param waiting questions still waiting
     * @param notified manual questions notified
     */
    public record RunResult(int closed, int resolved, int annulled, int waiting, int notified) {}

    /** Runs the job once. */
    public RunResult runOnce() {
        int closed = transactions.execute(status -> ledger.closeDue());
        int resolved = 0;
        int annulled = 0;
        int waiting = 0;
        int notified = 0;
        List<ForecastQuestionEntity> due = questions.findByStatusInOrderByResolveByAsc(List.of(ForecastStatus.OPEN,
                ForecastStatus.CLOSED));
        for (ForecastQuestionEntity candidate : due) {
            try {
                String outcome = transactions.execute(status -> handle(candidate.getId()));
                switch (outcome == null ? "" : outcome) {
                    case "resolved" -> resolved++;
                    case "annulled" -> annulled++;
                    case "notified" -> notified++;
                    default -> waiting++;
                }
            } catch (RuntimeException e) {
                log.warn("Resolving forecast question {} failed: {}", candidate.getId(), e.toString());
            }
        }
        scoring.invalidate();
        if (resolved + annulled + notified > 0) {
            log.info("Forecast resolution: {} closed, {} resolved, {} annulled, {} notified, {} waiting", closed,
                    resolved, annulled, notified, waiting);
        }
        return new RunResult(closed, resolved, annulled, waiting, notified);
    }

    /**
     * Handles one question in the current transaction.
     *
     * @return {@code resolved}, {@code annulled}, {@code notified} or {@code waiting}
     */
    public String handle(String questionId) {
        ForecastQuestionEntity q = questions.findById(questionId).orElse(null);
        if (q == null || q.finished()) {
            return "waiting";
        }
        Instant now = clock.instant();
        if (Set.of("manual", "hypothesis-outcome").contains(q.getResolverType())) {
            if (!now.isBefore(q.getResolveBy()) && q.getResolutionRequestedAt() == null) {
                ledger.requestResolution(q);
                return "notified";
            }
            return "waiting";
        }
        ResolverEngine.Verdict verdict = ResolverEngine.evaluate(q, now, data);
        switch (verdict.kind()) {
            case RESOLVE -> {
                Map<String, Object> resolution = new LinkedHashMap<>();
                resolution.put("outcome", verdict.outcome());
                resolution.put("value", verdict.value());
                resolution.put("resolvedAt", now.toString());
                resolution.put("resolvedBy", ResolverEngine.resolvedBy(q));
                resolution.put("early", verdict.early());
                resolution.put("asOf", verdict.asOf() == null ? null : verdict.asOf().toString());
                resolution.put("evidence", verdict.evidence());
                resolution.put("note", verdict.reason());
                resolution.put("caveats", verdict.caveats());
                resolution.put("manifestId", manifest(q, verdict));
                ledger.finishResolution(q, resolution);
                return "resolved";
            }
            case ANNUL -> {
                ledger.annul(q, verdict.annulCode(), verdict.reason(), ResolverEngine.resolvedBy(q));
                return "annulled";
            }
            default -> {
                return "waiting";
            }
        }
    }

    private String manifest(ForecastQuestionEntity q, ResolverEngine.Verdict verdict) {
        ManifestService service = manifests.getIfAvailable();
        if (service == null) {
            return null;
        }
        Map<String, Object> query = new LinkedHashMap<>();
        query.put("questionId", q.getId());
        query.put("asOf", verdict.asOf() == null ? null : verdict.asOf().toString());
        try {
            return service.record(ManifestRequest.of("resolver:" + q.getResolverType(), ResolverSpec.VERSION)
                    .config(q.getResolver()).inputs(verdict.evidence()).query(query));
        } catch (RuntimeException e) {
            log.warn("Could not record the resolution manifest of question {}: {}", q.getId(), e.toString());
            return null;
        }
    }
}
