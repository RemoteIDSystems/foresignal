package ai.foresignal.forecast;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * The scores of one forecaster on one resolved question (prompt 122, table {@code forecast_score}), computed by
 * {@code ForecastScorer} at {@code scorer_version}: Brier, log score, RPS (ordered), CRPS and pinball losses (numeric),
 * interval hits, the time-averaged Brier over {@code n_days}, the final probability and the observed outcome (1 or 0
 * for binary questions). Rows are recomputed only when the scorer version changes. The category, forecaster type and
 * resolution time are copied in for scorecard filters.
 */
@Entity
@Table(name = "forecast_score", indexes = {
    @Index(name = "forecast_score_key_idx", columnList = "question_id, forecaster_id", unique = true),
    @Index(name = "forecast_score_forecaster_idx", columnList = "forecaster_id, category, resolved_at")
})
public class ForecastScoreEntity {

    @Id
    @Column(name = "id", length = 160, nullable = false)
    private String id;

    @Column(name = "question_id", length = 26, nullable = false)
    private String questionId;

    @Column(name = "forecaster_id", length = 128, nullable = false)
    private String forecasterId;

    @Column(name = "forecaster_type", length = 32, nullable = false)
    private String forecasterType;

    @Column(name = "category", length = 64, nullable = false)
    private String category;

    @Column(name = "question_type", length = 16, nullable = false)
    private String questionType;

    @Column(name = "resolved_at", nullable = false)
    private Instant resolvedAt;

    @Column(name = "brier")
    private Double brier;

    @Column(name = "log_score")
    private Double logScore;

    @Column(name = "rps")
    private Double rps;

    @Column(name = "crps")
    private Double crps;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "pinball", columnDefinition = "jsonb")
    private Map<String, Double> pinball;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "interval_hits", columnDefinition = "jsonb")
    private Map<String, Boolean> intervalHits;

    /** Whether the outcome fell at or below each stored quantile (numeric calibration, prompt 152), or null. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "quantile_coverage", columnDefinition = "jsonb")
    private Map<String, Boolean> quantileCoverage;

    @Column(name = "time_averaged_brier")
    private Double timeAveragedBrier;

    @Column(name = "n_days", nullable = false)
    private int nDays;

    @Column(name = "n_forecasts", nullable = false)
    private int nForecasts;

    @Column(name = "hedged_forecasts", nullable = false)
    private int hedgedForecasts;

    @Column(name = "final_probability")
    private Double finalProbability;

    @Column(name = "observed")
    private Double observed;

    @Column(name = "scorer_version", length = 16, nullable = false)
    private String scorerVersion;

    @Column(name = "computed_at", nullable = false)
    private Instant computedAt;

    /** For JPA. */
    protected ForecastScoreEntity() {}

    /**
     * Creates a score row.
     *
     * @param questionId the question
     * @param forecasterId the forecaster
     */
    public ForecastScoreEntity(String questionId, String forecasterId) {
        this.id = key(questionId, forecasterId);
        this.questionId = questionId;
        this.forecasterId = forecasterId;
    }

    /** The row key of a question and forecaster. */
    public static String key(String questionId, String forecasterId) {
        return questionId + "|" + forecasterId;
    }

    /** The key. */
    public String getId() {
        return id;
    }

    /** The question. */
    public String getQuestionId() {
        return questionId;
    }

    /** The forecaster. */
    public String getForecasterId() {
        return forecasterId;
    }

    /** The forecaster type. */
    public String getForecasterType() {
        return forecasterType;
    }

    /** Sets the forecaster type. */
    public void setForecasterType(String value) {
        this.forecasterType = value;
    }

    /** The question's category. */
    public String getCategory() {
        return category;
    }

    /** Sets the category. */
    public void setCategory(String value) {
        this.category = value;
    }

    /** The question's type. */
    public String getQuestionType() {
        return questionType;
    }

    /** Sets the question type. */
    public void setQuestionType(String value) {
        this.questionType = value;
    }

    /** When the question resolved. */
    public Instant getResolvedAt() {
        return resolvedAt;
    }

    /** Sets the resolution time. */
    public void setResolvedAt(Instant value) {
        this.resolvedAt = value;
    }

    /** The Brier score (binary 0–1; multiple 0–2), or null. */
    public Double getBrier() {
        return brier;
    }

    /** Sets the Brier score. */
    public void setBrier(Double value) {
        this.brier = value;
    }

    /** The log score {@code −ln p(outcome)}, or null. */
    public Double getLogScore() {
        return logScore;
    }

    /** Sets the log score. */
    public void setLogScore(Double value) {
        this.logScore = value;
    }

    /** The ranked probability score (ordered), or null. */
    public Double getRps() {
        return rps;
    }

    /** Sets the RPS. */
    public void setRps(Double value) {
        this.rps = value;
    }

    /** The CRPS from quantiles (numeric), or null. */
    public Double getCrps() {
        return crps;
    }

    /** Sets the CRPS. */
    public void setCrps(Double value) {
        this.crps = value;
    }

    /** The pinball loss per quantile level (numeric), or null. */
    public Map<String, Double> getPinball() {
        return pinball;
    }

    /** Sets the pinball losses. */
    public void setPinball(Map<String, Double> value) {
        this.pinball = value == null ? null : new LinkedHashMap<>(value);
    }

    /** Whether the outcome fell inside p05–p95 and p25–p75 (numeric), or null. */
    public Map<String, Boolean> getIntervalHits() {
        return intervalHits;
    }

    /** Whether the outcome fell at or below each stored quantile (numeric calibration, prompt 152), or null. */
    public Map<String, Boolean> getQuantileCoverage() {
        return quantileCoverage;
    }

    /** Sets the per-quantile coverage indicators. */
    public void setQuantileCoverage(Map<String, Boolean> value) {
        this.quantileCoverage = value == null ? null : new LinkedHashMap<>(value);
    }

    /** Sets the interval hits. */
    public void setIntervalHits(Map<String, Boolean> value) {
        this.intervalHits = value == null ? null : new LinkedHashMap<>(value);
    }

    /** The time-averaged Brier score, or null. */
    public Double getTimeAveragedBrier() {
        return timeAveragedBrier;
    }

    /** Sets the time-averaged Brier score. */
    public void setTimeAveragedBrier(Double value) {
        this.timeAveragedBrier = value;
    }

    /** The days scored by the time-averaged Brier. */
    public int getNDays() {
        return nDays;
    }

    /** Sets the days scored. */
    public void setNDays(int value) {
        this.nDays = value;
    }

    /** How many forecasts the forecaster made on the question. */
    public int getNForecasts() {
        return nForecasts;
    }

    /** Sets how many forecasts were made. */
    public void setNForecasts(int value) {
        this.nForecasts = value;
    }

    /** How many of those forecasts (binary) were in [0.4, 0.6]. */
    public int getHedgedForecasts() {
        return hedgedForecasts;
    }

    /** Sets how many forecasts were hedged. */
    public void setHedgedForecasts(int value) {
        this.hedgedForecasts = value;
    }

    /** The final binary probability, or null. */
    public Double getFinalProbability() {
        return finalProbability;
    }

    /** Sets the final probability. */
    public void setFinalProbability(Double value) {
        this.finalProbability = value;
    }

    /** The observed binary outcome (1 or 0), or null. */
    public Double getObserved() {
        return observed;
    }

    /** Sets the observed outcome. */
    public void setObserved(Double value) {
        this.observed = value;
    }

    /** The scorer version. */
    public String getScorerVersion() {
        return scorerVersion;
    }

    /** Sets the scorer version. */
    public void setScorerVersion(String value) {
        this.scorerVersion = value;
    }

    /** When the scores were computed. */
    public Instant getComputedAt() {
        return computedAt;
    }

    /** Sets when the scores were computed. */
    public void setComputedAt(Instant value) {
        this.computedAt = value;
    }
}
