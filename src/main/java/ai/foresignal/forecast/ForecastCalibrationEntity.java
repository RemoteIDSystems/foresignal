package ai.foresignal.forecast;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * The fitted track record of one forecaster in one category (prompt 122, table {@code forecast_calibration}): the
 * Platt recalibration {@code p' = σ(a · logit p + b)} once at least 50 forecasts have resolved, and the relative Brier
 * score against the per-question median over the last 100 resolved questions, from which ensemble weights are built.
 */
@Entity
@Table(name = "forecast_calibration")
public class ForecastCalibrationEntity {

    @Id
    @Column(name = "id", length = 200, nullable = false)
    private String id;

    @Column(name = "forecaster_id", length = 128, nullable = false)
    private String forecasterId;

    @Column(name = "category", length = 64, nullable = false)
    private String category;

    @Column(name = "platt_a")
    private Double plattA;

    @Column(name = "platt_b")
    private Double plattB;

    @Column(name = "platt_n", nullable = false)
    private int plattN;

    @Column(name = "platt_fitted_at")
    private Instant plattFittedAt;

    @Column(name = "rel_brier")
    private Double relBrier;

    @Column(name = "weight_n", nullable = false)
    private int weightN;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected ForecastCalibrationEntity() {}

    /**
     * Creates an empty row.
     *
     * @param forecasterId the forecaster
     * @param category the category
     * @param now the creation time
     */
    public ForecastCalibrationEntity(String forecasterId, String category, Instant now) {
        this.id = key(forecasterId, category);
        this.forecasterId = forecasterId;
        this.category = category;
        this.updatedAt = now;
    }

    /** The row key. */
    public static String key(String forecasterId, String category) {
        return forecasterId + "|" + category;
    }

    /** The key. */
    public String getId() {
        return id;
    }

    /** The forecaster. */
    public String getForecasterId() {
        return forecasterId;
    }

    /** The category. */
    public String getCategory() {
        return category;
    }

    /** The Platt slope, or null without a fit. */
    public Double getPlattA() {
        return plattA;
    }

    /** The Platt intercept, or null without a fit. */
    public Double getPlattB() {
        return plattB;
    }

    /** The resolved forecasts behind the Platt fit. */
    public int getPlattN() {
        return plattN;
    }

    /** When the Platt fit was made, or null. */
    public Instant getPlattFittedAt() {
        return plattFittedAt;
    }

    /** Records a Platt fit, or clears it with null coefficients. */
    public void setPlatt(Double a, Double b, int n, Instant at) {
        this.plattA = a;
        this.plattB = b;
        this.plattN = n;
        this.plattFittedAt = a == null ? null : at;
        this.updatedAt = at;
    }

    /** True when a Platt fit is present. */
    public boolean hasPlatt() {
        return plattA != null && plattB != null;
    }

    /** The mean Brier score minus the per-question median, or null. */
    public Double getRelBrier() {
        return relBrier;
    }

    /** The questions behind {@link #getRelBrier()}. */
    public int getWeightN() {
        return weightN;
    }

    /** Records the relative Brier score. */
    public void setRelBrier(Double value, int n, Instant at) {
        this.relBrier = value;
        this.weightN = n;
        this.updatedAt = at;
    }

    /** When the row last changed. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
