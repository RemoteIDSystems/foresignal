package ai.foresignal.forecast.score;

import ai.foresignal.forecast.ForecastCalibrationEntity;
import ai.foresignal.forecast.ForecastCalibrationRepository;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecastQuestionRepository;
import ai.foresignal.forecast.ForecastRepository;
import ai.foresignal.forecast.ForecastScoreEntity;
import ai.foresignal.forecast.ForecastScoreRepository;
import ai.foresignal.forecast.ForecastStatus;
import ai.foresignal.forecast.ForecasterEntity;
import ai.foresignal.forecast.ForecasterRegistry;
import ai.foresignal.forecast.ensemble.EnsembleMath;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Stores scores and serves scorecards (prompt 122): scores a resolved question with {@link QuestionScorer}
 * ({@code ForecastScorer} {@value ForecastScorer#VERSION}), re-scores rows of an older scorer version, and builds
 * scorecards, calibration reports and comparisons with {@link Scorecards}. Scorecards are cached and the cache is
 * emptied after every resolution batch.
 */
@Service
public class ScoringService {

    private final ForecastScoreRepository scores;
    private final ForecastRepository forecasts;
    private final ForecastQuestionRepository questions;
    private final ForecastCalibrationRepository calibrations;
    private final ForecasterRegistry forecasters;
    private final Clock clock;
    private final Map<String, Map<String, Object>> cache = new ConcurrentHashMap<>();

    /** Creates the service. */
    public ScoringService(ForecastScoreRepository scores, ForecastRepository forecasts,
            ForecastQuestionRepository questions, ForecastCalibrationRepository calibrations,
            ForecasterRegistry forecasters, Clock clock) {
        this.scores = scores;
        this.forecasts = forecasts;
        this.questions = questions;
        this.calibrations = calibrations;
        this.forecasters = forecasters;
        this.clock = clock;
    }

    /** Scores a resolved question, replacing its rows; returns them. Annulled questions get none (LED-1). */
    @Transactional
    public List<ForecastScoreEntity> scoreQuestion(ForecastQuestionEntity q) {
        List<ForecastScoreEntity> existing = scores.findByQuestionId(q.getId());
        scores.deleteAll(existing);
        scores.flush();
        List<ForecastScoreEntity> rows = ForecastStatus.RESOLVED.equals(q.getStatus())
                ? QuestionScorer.score(q, forecasts.findByQuestionIdOrderByMadeAtAscIdAsc(q.getId()), clock.instant())
                : List.of();
        scores.saveAll(rows);
        invalidate();
        return rows;
    }

    /** Re-scores the questions whose rows were computed by another scorer version; returns how many. */
    @Transactional
    public int rescoreOutdated() {
        Set<String> outdated = new LinkedHashSet<>();
        for (ForecastScoreEntity s : scores.findAll()) {
            if (!ForecastScorer.VERSION.equals(s.getScorerVersion())) {
                outdated.add(s.getQuestionId());
            }
        }
        int n = 0;
        for (String id : outdated) {
            ForecastQuestionEntity q = questions.findById(id).orElse(null);
            if (q != null) {
                scoreQuestion(q);
                n++;
            }
        }
        return n;
    }

    /** Empties the scorecard cache. */
    public void invalidate() {
        cache.clear();
    }

    /** Every score row, oldest resolution first. */
    public List<ForecastScoreEntity> rows() {
        return scores.findAllByOrderByResolvedAtAsc();
    }

    /**
     * The scorecard ({@code ForecastScorecard}), cached until the next resolution batch.
     *
     * @param forecasterId one forecaster (must exist), or null
     */
    public Map<String, Object> scorecard(String forecasterId, String category, Instant from, Instant to) {
        if (forecasterId != null) {
            forecasters.get(forecasterId);
        }
        String key = "card|" + forecasterId + "|" + category + "|" + from + "|" + to;
        return cache.computeIfAbsent(key, k -> Scorecards.scorecard(rows(), new Scorecards.Filter(forecasterId,
                category, from, to), false, calibrationMap(), category == null ? Map.of() : weights(category),
                clock.instant()));
    }

    /** The calibration report ({@code ForecastCalibration}). */
    public Map<String, Object> calibration(String forecasterId, String category) {
        if (forecasterId != null) {
            forecasters.get(forecasterId);
        }
        String key = "cal|" + forecasterId + "|" + category;
        return cache.computeIfAbsent(key, k -> Scorecards.calibration(rows(), new Scorecards.Filter(forecasterId,
                category, null, null), clock.instant()));
    }

    /** The comparison of two forecasters ({@code ForecasterComparison}). */
    public Map<String, Object> compare(String a, String b, String category) {
        forecasters.get(a);
        forecasters.get(b);
        String key = "cmp|" + a + "|" + b + "|" + category;
        return cache.computeIfAbsent(key, k -> Scorecards.compare(rows(), a, b, category, clock.instant()));
    }

    /** The headline track record of each forecaster. */
    public Map<String, Map<String, Object>> trackRecords() {
        Map<String, List<ForecastScoreEntity>> byForecaster = new LinkedHashMap<>();
        for (ForecastScoreEntity s : rows()) {
            byForecaster.computeIfAbsent(s.getForecasterId(), k -> new ArrayList<>()).add(s);
        }
        Map<String, Map<String, Object>> out = new LinkedHashMap<>();
        byForecaster.forEach((id, list) -> out.put(id, Scorecards.trackRecord(list)));
        return out;
    }

    /** The Platt fits and relative Brier scores by {@code forecaster|category}. */
    public Map<String, ForecastCalibrationEntity> calibrationMap() {
        Map<String, ForecastCalibrationEntity> out = new LinkedHashMap<>();
        for (ForecastCalibrationEntity c : calibrations.findAll()) {
            out.put(c.getId(), c);
        }
        return out;
    }

    /**
     * The ensemble weights of the active non-ensemble forecasters with a track record in {@code category}, from
     * their relative Brier scores ({@link EnsembleMath#weights}).
     */
    public Map<String, Double> weights(String category) {
        List<String> ids = new ArrayList<>();
        List<EnsembleMath.Track> tracks = new ArrayList<>();
        for (ForecastCalibrationEntity c : calibrations.findByCategory(category)) {
            ForecasterEntity f = forecasters.find(c.getForecasterId()).orElse(null);
            if (f == null || !f.isActive() || ForecasterRegistry.ENSEMBLE.equals(f.getId())) {
                continue;
            }
            ids.add(c.getForecasterId());
            tracks.add(new EnsembleMath.Track(Objects.requireNonNullElse(c.getRelBrier(), 0.0), c.getWeightN()));
        }
        double[] w = EnsembleMath.weights(tracks, EnsembleMath.ETA, EnsembleMath.SHRINK);
        Map<String, Double> out = new LinkedHashMap<>();
        for (int i = 0; i < ids.size(); i++) {
            out.put(ids.get(i), w[i]);
        }
        return out;
    }
}
