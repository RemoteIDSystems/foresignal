package ai.foresignal.forecast.score;

import ai.foresignal.forecast.ForecastEntity;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecastScoreEntity;
import ai.foresignal.forecast.ForecasterRegistry;
import ai.foresignal.forecast.JsonValues;
import ai.foresignal.forecast.Ledger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Scores every forecaster on one resolved question with {@link ForecastScorer} (prompt 122). Pure: the question, its
 * forecasts and the time. A forecaster's final forecast is its newest made by the time the question stopped being
 * open (it closed or resolved, whichever came first); its time-averaged Brier score runs from {@code opens_at} to that
 * time, with the {@code base-rate} forecaster's standing forecast filling the days before its first forecast.
 * Annulled or unresolved questions are never scored (LED-1).
 */
public final class QuestionScorer {

    private QuestionScorer() {}

    /** The time a question stopped being open: the earlier of {@code closes_at} and its resolution. */
    public static Instant endOf(ForecastQuestionEntity q) {
        Instant end = q.getClosesAt();
        Map<String, Object> resolution = q.getResolution();
        if (resolution != null && resolution.get("resolvedAt") != null) {
            Instant resolved = JsonValues.instant(resolution.get("resolvedAt").toString());
            if (resolved.isBefore(end)) {
                end = resolved;
            }
        }
        return end;
    }

    /**
     * The score rows of a resolved question, one per forecaster with a forecast made before its end.
     *
     * @param q the question (status {@code resolved})
     * @param forecasts its forecasts
     * @param now the computation time
     * @return the rows (empty for an unresolved or annulled question)
     */
    public static List<ForecastScoreEntity> score(ForecastQuestionEntity q, List<ForecastEntity> forecasts,
            Instant now) {
        List<ForecastScoreEntity> out = new ArrayList<>();
        Map<String, Object> resolution = q.getResolution();
        if (!"resolved".equals(q.getStatus()) || resolution == null) {
            return out;
        }
        Instant end = endOf(q);
        Instant resolvedAt = resolution.get("resolvedAt") == null ? now
                : JsonValues.instant(resolution.get("resolvedAt").toString());
        Map<String, List<ForecastEntity>> byForecaster = new LinkedHashMap<>();
        for (ForecastEntity f : forecasts) {
            if (!f.getMadeAt().isAfter(end)) {
                byForecaster.computeIfAbsent(f.getForecasterId(), k -> new ArrayList<>()).add(f);
            }
        }
        for (List<ForecastEntity> list : byForecaster.values()) {
            list.sort((a, b) -> a.getMadeAt().equals(b.getMadeAt()) ? a.getId().compareTo(b.getId())
                    : a.getMadeAt().compareTo(b.getMadeAt()));
        }
        List<ForecastEntity> fill = byForecaster.getOrDefault(ForecasterRegistry.BASE_RATE, List.of());
        String outcome = resolution.get("outcome") == null ? null : resolution.get("outcome").toString();
        Double value = resolution.get("value") instanceof Number n ? n.doubleValue() : null;
        for (Map.Entry<String, List<ForecastEntity>> e : byForecaster.entrySet()) {
            List<ForecastEntity> own = e.getValue();
            ForecastEntity last = own.get(own.size() - 1);
            ForecastScoreEntity s = new ForecastScoreEntity(q.getId(), e.getKey());
            s.setForecasterType(last.getForecasterType());
            s.setCategory(q.getCategory());
            s.setQuestionType(q.getType());
            s.setResolvedAt(resolvedAt);
            s.setNForecasts(own.size());
            s.setScorerVersion(ForecastScorer.VERSION);
            s.setComputedAt(now);
            List<ForecastEntity> fillFor = ForecasterRegistry.BASE_RATE.equals(e.getKey()) ? List.of() : fill;
            switch (q.getType()) {
                case Ledger.BINARY -> binary(s, own, fillFor, q, end, "yes".equals(outcome));
                case Ledger.MULTIPLE, Ledger.ORDERED -> categorical(s, own, fillFor, q, end, outcome);
                case Ledger.NUMERIC -> numeric(s, last, value);
                default -> {
                    // Unknown types are not scored.
                }
            }
            out.add(s);
        }
        return out;
    }

    private static void binary(ForecastScoreEntity s, List<ForecastEntity> own, List<ForecastEntity> fill,
            ForecastQuestionEntity q, Instant end, boolean yes) {
        ForecastEntity last = own.get(own.size() - 1);
        if (last.getProbability() == null) {
            return;
        }
        double p = last.getProbability();
        s.setBrier(ForecastScorer.brier(p, yes));
        s.setLogScore(ForecastScorer.logScore(p, yes));
        s.setFinalProbability(p);
        s.setObserved(yes ? 1.0 : 0.0);
        int hedged = 0;
        for (ForecastEntity f : own) {
            if (f.getProbability() != null && Skill.hedged(f.getProbability())) {
                hedged++;
            }
        }
        s.setHedgedForecasts(hedged);
        ForecastScorer.TimeAveraged t = ForecastScorer.timeAveraged(timed(own, true), timed(fill, true),
                q.getOpensAt(), end, (Double prob) -> ForecastScorer.brier(prob, yes));
        s.setTimeAveragedBrier(Double.isNaN(t.score()) ? null : t.score());
        s.setNDays(t.nDays());
    }

    private static void categorical(ForecastScoreEntity s, List<ForecastEntity> own, List<ForecastEntity> fill,
            ForecastQuestionEntity q, Instant end, String outcome) {
        ForecastEntity last = own.get(own.size() - 1);
        Map<String, Double> d = last.getDistribution();
        if (d == null || outcome == null) {
            return;
        }
        List<String> labels = q.labels();
        s.setBrier(ForecastScorer.multiBrier(d, outcome, labels));
        s.setLogScore(ForecastScorer.logScore(d.getOrDefault(outcome, 0.0)));
        if (Ledger.ORDERED.equals(q.getType())) {
            s.setRps(ForecastScorer.rps(d, outcome, labels));
        }
        List<ForecastScorer.Timed<Map<String, Double>>> mine = new ArrayList<>();
        for (ForecastEntity f : own) {
            if (f.getDistribution() != null) {
                mine.add(new ForecastScorer.Timed<>(f.getMadeAt(), f.getDistribution()));
            }
        }
        List<ForecastScorer.Timed<Map<String, Double>>> base = new ArrayList<>();
        for (ForecastEntity f : fill) {
            if (f.getDistribution() != null) {
                base.add(new ForecastScorer.Timed<>(f.getMadeAt(), f.getDistribution()));
            }
        }
        ForecastScorer.TimeAveraged t = ForecastScorer.timeAveraged(mine, base, q.getOpensAt(), end,
                dist -> ForecastScorer.multiBrier(dist, outcome, labels));
        s.setTimeAveragedBrier(Double.isNaN(t.score()) ? null : t.score());
        s.setNDays(t.nDays());
    }

    private static void numeric(ForecastScoreEntity s, ForecastEntity last, Double value) {
        Map<String, Double> quantiles = last.getQuantiles();
        if (quantiles == null || value == null) {
            return;
        }
        s.setCrps(ForecastScorer.crps(quantiles, value));
        s.setPinball(ForecastScorer.pinball(quantiles, value));
        s.setIntervalHits(ForecastScorer.intervalHits(quantiles, value));
        s.setNDays(0);
    }

    private static List<ForecastScorer.Timed<Double>> timed(List<ForecastEntity> list, boolean binary) {
        List<ForecastScorer.Timed<Double>> out = new ArrayList<>();
        for (ForecastEntity f : list) {
            if (binary && f.getProbability() != null) {
                out.add(new ForecastScorer.Timed<>(f.getMadeAt(), f.getProbability()));
            }
        }
        return out;
    }
}
