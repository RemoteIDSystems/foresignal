package ai.foresignal.baserate.analog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * What happened after a past situation (prompt 121, table {@code situation_outcome}), for closed or dormant situations
 * and any older than 30 days: its duration, peak severity, the signal families active in its regions within 7 and 30
 * days of its reference time (day 7 of its life, or its end when shorter), and the outcome of a related hypothesis set
 * when one resolved.
 */
@Entity
@Table(name = "situation_outcome")
public class SituationOutcomeEntity {

    @Id
    @Column(name = "situation_id", length = 26, nullable = false)
    private String situationId;

    @Column(name = "duration_days")
    private Double durationDays;

    @Column(name = "peak_severity", length = 16)
    private String peakSeverity;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "follow_on_7d", columnDefinition = "text[]")
    private String[] followOn7d = new String[0];

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "follow_on_30d", columnDefinition = "text[]")
    private String[] followOn30d = new String[0];

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "hypothesis_outcome", columnDefinition = "jsonb")
    private Map<String, Object> hypothesisOutcome;

    @Column(name = "reference_at", nullable = false)
    private Instant referenceAt;

    @Column(name = "is_final", nullable = false)
    private boolean finalOutcome;

    @Column(name = "computed_at", nullable = false)
    private Instant computedAt;

    /** For JPA. */
    protected SituationOutcomeEntity() {}

    /**
     * Creates an outcome.
     *
     * @param situationId the situation
     */
    public SituationOutcomeEntity(String situationId) {
        this.situationId = situationId;
    }

    /** The situation. */
    public String getSituationId() {
        return situationId;
    }

    /** The duration in days, or null. */
    public Double getDurationDays() {
        return durationDays;
    }

    /** Sets the duration. */
    public void setDurationDays(Double value) {
        this.durationDays = value;
    }

    /** The peak severity, or null. */
    public String getPeakSeverity() {
        return peakSeverity;
    }

    /** Sets the peak severity. */
    public void setPeakSeverity(String value) {
        this.peakSeverity = value;
    }

    /** The families active within 7 days of the reference time. */
    public List<String> getFollowOn7d() {
        return followOn7d == null ? List.of() : Arrays.asList(followOn7d);
    }

    /** Sets the 7-day families. */
    public void setFollowOn7d(List<String> value) {
        this.followOn7d = value == null ? new String[0] : value.toArray(String[]::new);
    }

    /** The families active within 30 days of the reference time. */
    public List<String> getFollowOn30d() {
        return followOn30d == null ? List.of() : Arrays.asList(followOn30d);
    }

    /** Sets the 30-day families. */
    public void setFollowOn30d(List<String> value) {
        this.followOn30d = value == null ? new String[0] : value.toArray(String[]::new);
    }

    /** The resolved related hypothesis set ({@code {setId, outcome}}), or null. */
    public Map<String, Object> getHypothesisOutcome() {
        return hypothesisOutcome;
    }

    /** Sets the hypothesis outcome. */
    public void setHypothesisOutcome(Map<String, Object> value) {
        this.hypothesisOutcome = value;
    }

    /** The time follow-on windows start from. */
    public Instant getReferenceAt() {
        return referenceAt;
    }

    /** Sets the reference time. */
    public void setReferenceAt(Instant value) {
        this.referenceAt = value;
    }

    /** True once the situation is closed and its 30-day window has passed (never recomputed). */
    public boolean isFinalOutcome() {
        return finalOutcome;
    }

    /** Sets whether the outcome is final. */
    public void setFinalOutcome(boolean value) {
        this.finalOutcome = value;
    }

    /** When it was computed. */
    public Instant getComputedAt() {
        return computedAt;
    }

    /** Sets when it was computed. */
    public void setComputedAt(Instant value) {
        this.computedAt = value;
    }
}
