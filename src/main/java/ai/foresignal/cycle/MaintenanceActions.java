package ai.foresignal.cycle;

import java.time.Instant;
import java.util.Map;

/**
 * The jobs of the monthly maintenance (prompt 128 §5), one method per step, in the order {@link MonthlyMaintenance}
 * runs them. Each returns its result (job ids, counts) or throws; a step that does not apply returns {@code null}
 * from {@link #retrospectiveEvaluation} and is recorded as skipped. The default implementation
 * ({@link DefaultMaintenanceActions}) calls the services of prompts 116, 120–123 and 125–126.
 */
public interface MaintenanceActions {

    /** 116: observability re-estimation. */
    Map<String, Object> observabilityReestimation(Instant now);

    /** 123: null-case sampling and the historical benchmark suite. */
    Map<String, Object> benchmarkHistorical(Instant now);

    /** 121: base-rate refresh. */
    Map<String, Object> baseRateRefresh(Instant now);

    /** 120: precursor re-test. */
    Map<String, Object> precursorRetest(Instant now);

    /** 122: Platt refits (with the relative-Brier weights and the extremizing gate of the refit). */
    Map<String, Object> plattRefit(Instant now);

    /** 122: ensemble refresh with the new weights. */
    Map<String, Object> ensembleWeights(Instant now);

    /** 122: the extremizing check. */
    Map<String, Object> extremizingCheck(Instant now);

    /** 126: index weight fits (proposals) and outlook refits. */
    Map<String, Object> indexWeightFit(Instant now);

    /** 126: series-method re-selection. */
    Map<String, Object> seriesMethodReselection(Instant now);

    /** 125: archival proposals for hypothesis sets past their horizon. */
    Map<String, Object> hypothesisArchivalProposals(Instant now);

    /**
     * 122: retrospective evaluation of the {@code llm:*} forecasters over the past month, or null when the provider
     * has no {@code knowledgeCutoff}.
     *
     * @param providerId the monthly cycle profile's provider (else the chat provider), or null
     */
    Map<String, Object> retrospectiveEvaluation(Instant now, String providerId);
}
