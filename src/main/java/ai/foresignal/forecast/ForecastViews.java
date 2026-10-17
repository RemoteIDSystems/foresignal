package ai.foresignal.forecast;

import ai.foresignal.forecast.language.EstimativeLanguage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The JSON shapes of the forecast ledger (prompt 122): {@code ForecastQuestionSummary}, {@code ForecastQuestion},
 * {@code Forecast}, {@code Forecaster} and {@code ForecastScore}. Every probability is a number; its ICD 203 term is
 * derived by {@link EstimativeLanguage}.
 */
public final class ForecastViews {

    private ForecastViews() {}

    /** The links of a question ({@code QuestionLinks}). */
    public static Map<String, Object> links(ForecastQuestionEntity q) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("situationId", q.getSituationId());
        out.put("hypothesisSetId", q.getHypothesisSetId());
        out.put("precursorId", q.getPrecursorId());
        out.put("findingId", q.getFindingId());
        out.put("seriesRef", q.getSeriesRef());
        out.put("outlookId", q.getOutlookId());
        return out;
    }

    /** {@code ForecastQuestionSummary}. */
    public static Map<String, Object> summary(ForecastQuestionEntity q, long forecastCount) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", q.getId());
        out.put("title", q.getTitle());
        out.put("type", q.getType());
        out.put("status", q.getStatus());
        out.put("category", q.getCategory());
        out.put("tags", q.getTags());
        out.put("resolverType", q.getResolverType());
        out.put("opensAt", q.getOpensAt().toString());
        out.put("closesAt", q.getClosesAt().toString());
        out.put("resolveBy", q.getResolveBy().toString());
        out.put("origin", q.getOrigin());
        out.put("links", links(q));
        out.put("private", q.isPrivateQuestion());
        out.put("forecastCount", forecastCount);
        Double ensemble = Ledger.BINARY.equals(q.getType()) ? q.getEnsembleProbability() : null;
        out.put("ensembleProbability", ensemble);
        out.put("estimativeTerm", EstimativeLanguage.termOrNull(ensemble));
        Map<String, Object> resolution = q.getResolution();
        out.put("resolvedOutcome", resolution == null ? null : resolution.get("outcome"));
        out.put("resolvedValue", resolution == null ? null : resolution.get("value"));
        return out;
    }

    /**
     * {@code ForecastQuestion}.
     *
     * @param q the question
     * @param forecastCount how many forecasts it has
     * @param latest the latest forecast per forecaster, newest first
     * @param ensemble the latest ensemble forecast, or null
     * @param scores the scores once resolved
     */
    public static Map<String, Object> question(ForecastQuestionEntity q, long forecastCount,
            List<ForecastEntity> latest, ForecastEntity ensemble, List<ForecastScoreEntity> scores) {
        Map<String, Object> out = summary(q, forecastCount);
        out.put("outcomes", q.getOutcomes());
        out.put("resolutionCriteria", q.getResolutionCriteria());
        out.put("resolver", q.getResolver());
        out.put("resolution", q.getResolution());
        out.put("annulment", q.getAnnulment());
        out.put("dedupKey", q.getDedupKey());
        List<Map<String, Object>> latestViews = new ArrayList<>();
        for (ForecastEntity f : latest) {
            latestViews.add(forecast(f));
        }
        out.put("latestForecasts", latestViews);
        out.put("ensemble", ensemble == null ? null : forecast(ensemble));
        List<Map<String, Object>> scoreViews = new ArrayList<>();
        for (ForecastScoreEntity s : scores) {
            scoreViews.add(score(s));
        }
        out.put("scores", scoreViews);
        out.put("baselineNotes", q.getBaselineNotes());
        out.put("manifestId", q.getManifestId());
        out.put("createdAt", q.getCreatedAt().toString());
        out.put("updatedAt", q.getUpdatedAt().toString());
        return out;
    }

    /** {@code Forecast}. */
    public static Map<String, Object> forecast(ForecastEntity f) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", f.getId());
        out.put("questionId", f.getQuestionId());
        out.put("forecasterId", f.getForecasterId());
        out.put("forecasterType", f.getForecasterType());
        out.put("madeAt", f.getMadeAt().toString());
        out.put("asOfSeq", f.getAsOfSeq());
        out.put("knowledgeCutoff", f.getKnowledgeCutoff() == null ? null : f.getKnowledgeCutoff().toString());
        out.put("probability", f.getProbability());
        out.put("distribution", f.getDistribution());
        out.put("quantiles", f.getQuantiles());
        out.put("estimativeTerm", EstimativeLanguage.termOrNull(f.getProbability()));
        out.put("rationale", f.getRationale());
        out.put("claims", f.getClaims());
        out.put("citations", f.getCitations());
        out.put("inputs", f.getInputs());
        out.put("supersedesId", f.getSupersedesId());
        out.put("recalibrated", f.isRecalibrated());
        out.put("manifestId", f.getManifestId());
        return out;
    }

    /** {@code Forecaster}, with its headline track record when known. */
    public static Map<String, Object> forecaster(ForecasterEntity f, Map<String, Object> trackRecord) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", f.getId());
        out.put("type", f.getType());
        out.put("name", f.getName());
        out.put("config", f.getConfig());
        out.put("version", f.getVersion());
        out.put("active", f.isActive());
        out.put("trackRecord", trackRecord);
        out.put("createdAt", f.getCreatedAt().toString());
        return out;
    }

    /** {@code ForecastScore}. */
    public static Map<String, Object> score(ForecastScoreEntity s) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("questionId", s.getQuestionId());
        out.put("forecasterId", s.getForecasterId());
        out.put("brier", s.getBrier());
        out.put("logScore", s.getLogScore());
        out.put("rps", s.getRps());
        out.put("crps", s.getCrps());
        out.put("pinball", s.getPinball());
        out.put("intervalHits", s.getIntervalHits());
        out.put("timeAveragedBrier", s.getTimeAveragedBrier());
        out.put("nDays", s.getNDays());
        out.put("finalProbability", s.getFinalProbability());
        out.put("scorerVersion", s.getScorerVersion());
        out.put("computedAt", s.getComputedAt().toString());
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
