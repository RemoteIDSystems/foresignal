package ai.foresignal.intel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * One resolved real-world outcome used to calibrate intelligence confidence (prompt 151, table
 * {@code intel_outcome}): an intel event the operator or agent resolved ({@code outcome = 1}) or dismissed
 * ({@code outcome = 0}), or a precursor-active finding whose forecast question resolved yes ({@code 1}) or no
 * ({@code 0}). {@code rawScore} is the uncalibrated {@link EventScorer} confidence (events) or the precursor's
 * stated probability (precursors) at the time the subject was scored; {@code (rawScore, outcome)} is the pair the
 * {@link IntelCalibrationService} fits a recalibration curve to. {@code (subjectKind, subjectId)} is unique, so
 * re-resolving the same subject updates its row rather than double-counting.
 */
@Entity
@Table(name = "intel_outcome")
public class IntelOutcomeEntity {

    /** An intel event resolved or dismissed. */
    public static final String KIND_EVENT = "intel-event";

    /** A precursor-active finding whose forecast question resolved. */
    public static final String KIND_PRECURSOR = "precursor-active";

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "scope", length = 128, nullable = false)
    private String scope;

    @Column(name = "subject_kind", length = 32, nullable = false)
    private String subjectKind;

    @Column(name = "subject_id", length = 64, nullable = false)
    private String subjectId;

    @Column(name = "raw_score", nullable = false)
    private double rawScore;

    @Column(name = "outcome", nullable = false)
    private double outcome;

    @Column(name = "resolved_at", nullable = false)
    private Instant resolvedAt;

    /** JPA. */
    protected IntelOutcomeEntity() {}

    /** Creates a resolved outcome. */
    public IntelOutcomeEntity(String id, String scope, String subjectKind, String subjectId, double rawScore,
            double outcome, Instant resolvedAt) {
        this.id = id;
        this.scope = scope;
        this.subjectKind = subjectKind;
        this.subjectId = subjectId;
        this.rawScore = rawScore;
        this.outcome = outcome;
        this.resolvedAt = resolvedAt;
    }

    public String getId() {
        return id;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getSubjectKind() {
        return subjectKind;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public double getRawScore() {
        return rawScore;
    }

    public void setRawScore(double rawScore) {
        this.rawScore = rawScore;
    }

    public double getOutcome() {
        return outcome;
    }

    public void setOutcome(double outcome) {
        this.outcome = outcome;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}
