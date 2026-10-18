package ai.foresignal.hypothesis;

import ai.foresignal.forecast.ForecastEntity;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.language.EstimativeLanguage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The API shapes of rolling hypotheses (prompt 125, {@code docs/OpenAPI.json}): {@code HypothesisSet},
 * {@code Hypothesis}, {@code HypothesisProbabilityState}, {@code HypothesisRevision}, {@code Indicator},
 * {@code KeyAssumption}, {@code AchEvidence}, {@code CollectionGap} and {@code LedgerQuestionBrief}.
 */
public final class HypothesisViews {

    private HypothesisViews() {}

    /** A {@code HypothesisProbabilityState}. */
    public static Map<String, Object> state(HypothesisEntity h) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("hypothesisId", h.getId());
        out.put("label", h.getLabel());
        out.put("probability", h.getProbability());
        out.put("estimativeTerm", h.getEstimativeTerm());
        out.put("confidence", h.getConfidence());
        return out;
    }

    /**
     * A {@code Hypothesis}.
     *
     * @param h the hypothesis
     * @param previous the probability state of the previous revision for this hypothesis, or null
     * @param sinceRevisionId the revision the change is measured from, or null
     */
    public static Map<String, Object> hypothesis(HypothesisEntity h, Map<String, Object> previous,
            String sinceRevisionId) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", h.getId());
        out.put("setId", h.getSetId());
        out.put("label", h.getLabel());
        out.put("statement", h.getStatement());
        out.put("type", h.getType());
        out.put("prior", h.getPrior());
        out.put("priorBasis", h.getPriorBasis());
        out.put("probability", h.getProbability());
        out.put("estimativeTerm", h.getEstimativeTerm());
        out.put("confidence", h.getConfidence());
        out.put("confidenceBasis", h.getConfidenceBasis());
        out.put("ledgerOutcome", h.getLedgerOutcome());
        out.put("status", h.getStatus());
        Map<String, Object> change = null;
        if (previous != null && previous.get("probability") instanceof Number before) {
            change = new LinkedHashMap<>();
            change.put("previousProbability", before.doubleValue());
            change.put("previousTerm", EstimativeLanguage.term(before.doubleValue()));
            change.put("delta", h.getProbability() - before.doubleValue());
            change.put("sinceRevisionId", sinceRevisionId);
        }
        out.put("change", change);
        out.put("linkedFindingIds", h.getLinkedFindingIds());
        out.put("coincidenceBaseline", h.getCoincidenceBaseline());
        out.put("alternatives", h.getAlternatives());
        out.put("probabilityCap", h.getProbabilityCap());
        out.put("capOverride", h.getCapOverride());
        out.put("version", h.getVersion());
        return out;
    }

    /** An {@code Indicator}, with the effective LR of each likelihood. */
    public static Map<String, Object> indicator(IndicatorEntity i) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", i.getId());
        out.put("setId", i.getSetId());
        out.put("description", i.getDescription());
        out.put("signal", i.getSignal());
        out.put("signalFamily", i.getSignalFamily());
        out.put("credibility", i.getCredibility());
        out.put("relevance", i.getRelevance());
        out.put("expectedBy", i.getExpectedBy() == null ? null : i.getExpectedBy().toString());
        Map<String, Object> lr = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : i.getLr().entrySet()) {
            Map<String, Object> likelihood = new LinkedHashMap<>(HypothesisInputs.asMap(e.getValue()));
            likelihood.put("effective", LikelihoodRatios.observed(HypothesisInputs.asMap(e.getValue())));
            lr.put(e.getKey(), likelihood);
        }
        out.put("lr", lr);
        out.put("absenceLr", i.getAbsenceLr());
        out.put("state", i.getState());
        out.put("watchRuleId", i.getWatchRuleId() == null ? null : i.getWatchRuleId().toString());
        out.put("pDetect", i.getPDetect());
        out.put("observedEvidence", i.getObservedEvidence());
        out.put("lastStateChangeAt", i.getLastStateChangeAt() == null ? null : i.getLastStateChangeAt().toString());
        out.put("origin", i.getOrigin());
        out.put("version", i.getVersion());
        return out;
    }

    /** A {@code KeyAssumption}. */
    public static Map<String, Object> assumption(KeyAssumptionEntity a) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", a.getId());
        out.put("setId", a.getSetId());
        out.put("text", a.getText());
        out.put("linchpin", a.isLinchpin());
        out.put("rationale", a.getRationale());
        out.put("wouldBeWrongIf", a.getWouldBeWrongIf());
        out.put("status", a.getStatus());
        out.put("indicatorIds", a.getIndicatorIds());
        out.put("reviewDue", a.getReviewDue() == null ? null : a.getReviewDue().toString());
        out.put("updatedBy", a.getUpdatedBy());
        out.put("updatedAt", a.getUpdatedAt().toString());
        return out;
    }

    /**
     * An {@code AchEvidence} row.
     *
     * @param e the row
     * @param nonDiagnostic whether it is rated identically for every hypothesis
     */
    public static Map<String, Object> evidence(AchEvidenceEntity e, boolean nonDiagnostic) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", e.getId());
        out.put("setId", e.getSetId());
        out.put("source", e.getSource());
        out.put("ref", e.getRef());
        out.put("refs", e.getRefs());
        out.put("evidenceGroupId", e.getEvidenceGroupId());
        out.put("description", e.getDescription());
        out.put("credibility", e.getCredibility());
        out.put("relevance", e.getRelevance());
        out.put("weight", AchMatrix.weight(e.getCredibility(), e.getRelevance()));
        out.put("ratings", e.getRatings());
        out.put("nonDiagnostic", nonDiagnostic);
        out.put("addedBy", e.getAddedBy());
        out.put("addedAt", e.getAddedAt().toString());
        out.put("removedAt", e.getRemovedAt() == null ? null : e.getRemovedAt().toString());
        out.put("evidenceItemId", null);
        out.put("provenanceChain", e.getProvenanceChain());
        out.put("caveats", e.getCaveats());
        return out;
    }

    /** A {@code HypothesisRevision}. */
    public static Map<String, Object> revision(HypothesisRevisionEntity r) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", r.getId());
        out.put("setId", r.getSetId());
        out.put("at", r.getAt().toString());
        out.put("cause", r.getCause());
        out.put("actor", r.getActor());
        out.put("asOfSeq", r.getAsOfSeq());
        out.put("before", r.getBefore());
        out.put("after", r.getAfter());
        List<Map<String, Object>> drivers = new ArrayList<>();
        for (Map<String, Object> d : r.getDrivers()) {
            Map<String, Object> copy = new LinkedHashMap<>(d);
            copy.remove("evidenceKey");
            copy.remove("evidence");
            drivers.add(copy);
        }
        out.put("drivers", drivers);
        out.put("citations", r.getCitations());
        out.put("rationale", r.getRationale());
        out.put("rationaleOrigin", r.getRationaleOrigin());
        out.put("bandChanged", r.isBandChanged());
        out.put("override", r.isOverride());
        out.put("ledgerForecastId", r.getLedgerForecastId());
        out.put("updaterVersion", r.getUpdaterVersion());
        return out;
    }

    /** A {@code CollectionGap}. */
    public static Map<String, Object> gap(CollectionGapEntity g) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", g.getId());
        out.put("setId", g.getSetId());
        out.put("indicatorId", g.getIndicatorId());
        out.put("potentialEig", g.getPotentialEig());
        out.put("actualEig", g.getActualEig());
        out.put("pDetect", g.getPDetect());
        out.put("recommendations", g.getRecommendations());
        out.put("createdAt", g.getCreatedAt().toString());
        out.put("updatedAt", g.getUpdatedAt().toString());
        return out;
    }

    /**
     * A {@code LedgerQuestionBrief}.
     *
     * @param q the question
     * @param latest the latest forecast per forecaster id
     */
    public static Map<String, Object> brief(ForecastQuestionEntity q, Map<String, ForecastEntity> latest) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", q.getId());
        out.put("ref", "question:" + q.getId());
        out.put("title", q.getTitle());
        out.put("type", q.getType());
        out.put("status", q.getStatus());
        out.put("closesAt", q.getClosesAt() == null ? null : q.getClosesAt().toString());
        out.put("resolveBy", q.getResolveBy() == null ? null : q.getResolveBy().toString());
        out.put("resolutionCriteria", q.getResolutionCriteria());
        ForecastEntity ensemble = latest.get("ensemble");
        out.put("probability", q.getEnsembleProbability());
        out.put("estimativeTerm", EstimativeLanguage.termOrNull(q.getEnsembleProbability()));
        out.put("distribution", ensemble == null ? null : ensemble.getDistribution());
        out.put("quantiles", ensemble == null ? null : ensemble.getQuantiles());
        out.put("issuer", "foresignal-ensemble");
        List<Map<String, Object>> forecasters = new ArrayList<>();
        for (ForecastEntity f : latest.values()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("forecasterId", f.getForecasterId());
            row.put("probability", f.getProbability());
            row.put("madeAt", f.getMadeAt().toString());
            row.put("knowledgeCutoff", f.getKnowledgeCutoff() == null ? null : f.getKnowledgeCutoff().toString());
            forecasters.add(row);
        }
        out.put("forecasters", forecasters);
        out.put("knowledgeCutoff", null);
        ForecastEntity baseRate = latest.get("base-rate");
        out.put("baseRate", baseRate == null ? null : baseRate.getProbability());
        out.put("updatedAt", q.getUpdatedAt() == null ? null : q.getUpdatedAt().toString());
        return out;
    }

    /** A page ({@code items}, {@code nextCursor}). */
    public static Map<String, Object> page(List<Map<String, Object>> items, String nextCursor) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("nextCursor", nextCursor);
        return out;
    }
}
