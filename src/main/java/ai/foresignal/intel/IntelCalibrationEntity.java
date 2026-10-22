package ai.foresignal.intel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * The fitted calibration of one scope (prompt 151, table {@code intel_calibration}): the CORP
 * (pool-adjacent-violators) recalibration curve over the scope's resolved outcomes, the 10-bin reliability table and
 * the mean Brier score, all from {@link ai.foresignal.forecast.score.Calibration}. {@code calibrated} is true once at
 * least {@link ai.foresignal.forecast.score.Calibration#CORP_MIN} outcomes have resolved; until then the raw
 * {@link EventScorer} score is reported unchanged and {@code calibrated} is false on the events themselves.
 *
 * <p>The scope groups outcomes that share a reliability curve: {@code "intel-event"} for intelligence events and
 * {@code "precursor-active"} for precursor findings.
 */
@Entity
@Table(name = "intel_calibration")
public class IntelCalibrationEntity {

    @Id
    @Column(name = "scope", length = 128, nullable = false)
    private String scope;

    @Column(name = "sample_n", nullable = false)
    private int sampleN;

    @Column(name = "calibrated", nullable = false)
    private boolean calibrated;

    @Column(name = "brier")
    private Double brier;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "curve")
    private List<Map<String, Object>> curve = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "bins")
    private List<Map<String, Object>> bins = new ArrayList<>();

    @Column(name = "fitted_at")
    private Instant fittedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** JPA. */
    protected IntelCalibrationEntity() {}

    /** Creates an empty calibration for a scope. */
    public IntelCalibrationEntity(String scope, Instant now) {
        this.scope = scope;
        this.updatedAt = now;
    }

    public String getScope() {
        return scope;
    }

    public int getSampleN() {
        return sampleN;
    }

    public boolean isCalibrated() {
        return calibrated;
    }

    public Double getBrier() {
        return brier;
    }

    public List<Map<String, Object>> getCurve() {
        return curve == null ? List.of() : curve;
    }

    public List<Map<String, Object>> getBins() {
        return bins == null ? List.of() : bins;
    }

    public Instant getFittedAt() {
        return fittedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** Records a fit: the curve, bins, Brier and sample size, and whether the scope is now calibrated. */
    public void setFit(List<Map<String, Object>> curve, List<Map<String, Object>> bins, Double brier, int sampleN,
            boolean calibrated, Instant now) {
        this.curve = curve == null ? new ArrayList<>() : curve;
        this.bins = bins == null ? new ArrayList<>() : bins;
        this.brier = brier;
        this.sampleN = sampleN;
        this.calibrated = calibrated;
        this.fittedAt = now;
        this.updatedAt = now;
    }
}
