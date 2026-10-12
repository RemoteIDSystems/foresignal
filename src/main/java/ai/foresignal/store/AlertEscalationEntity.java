package ai.foresignal.store;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * The escalation state of one alert ({@code alert_escalation}, prompt 033), created when an alert of a
 * rule with an escalation policy fires. {@code next_step} indexes the policy's steps (the rule's own
 * routing is step 0 of the alert's life, so the policy's first step is index 0 here);
 * {@code state} is {@code active}, {@code stopped} or {@code exhausted}. Declared for Hibernate to
 * own the table; the escalation worker reads and writes it with native SQL
 * ({@code FOR UPDATE SKIP LOCKED}).
 */
@Entity
@Table(name = "alert_escalation", indexes = @Index(name = "alert_escalation_due_idx", columnList = "state, next_at"))
public class AlertEscalationEntity {

    @Id
    @Column(name = "alert_id", nullable = false)
    private long alertId;

    @Column(name = "policy_id", nullable = false)
    private UUID policyId;

    @Column(name = "next_step", nullable = false)
    private int nextStep;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "next_at")
    private Instant nextAt;

    @Column(name = "repeats_done", nullable = false)
    private int repeatsDone;

    @Column(name = "state", nullable = false, length = 16)
    private String state;

    @Column(name = "stopped_reason", length = 64)
    private String stoppedReason;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected AlertEscalationEntity() {}

    /** The alert. */
    public long getAlertId() {
        return alertId;
    }

    /** The policy. */
    public UUID getPolicyId() {
        return policyId;
    }

    /** The next step index. */
    public int getNextStep() {
        return nextStep;
    }

    /** When the next step is due, or null. */
    public Instant getNextAt() {
        return nextAt;
    }

    /** Repeats of the last step done. */
    public int getRepeatsDone() {
        return repeatsDone;
    }

    /** {@code active}, {@code stopped} or {@code exhausted}. */
    public String getState() {
        return state;
    }

    /** Why it stopped, or null. */
    public String getStoppedReason() {
        return stoppedReason;
    }

    /** When last changed. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
