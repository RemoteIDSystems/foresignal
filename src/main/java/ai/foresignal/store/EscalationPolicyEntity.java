package ai.foresignal.store;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * An escalation policy ({@code escalation_policy}, prompt 033): up to five steps
 * {@code [{afterMinutes, channelIds, includePush}]} with strictly increasing {@code afterMinutes},
 * an optional {@code repeat {everyMinutes, maxTimes}} of the last step, and {@code stopOn}
 * ({@code resolve} and {@code snooze}; acknowledgement always stops). Rules reference it through
 * {@code trigger_rule.escalation_policy_id}.
 */
@Entity
@Table(name = "escalation_policy")
public class EscalationPolicyEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "steps", nullable = false, columnDefinition = "jsonb")
    private List<Map<String, Object>> steps = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "repeat")
    private Map<String, Object> repeat;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "stop_on", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> stopOn = new LinkedHashMap<>();

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected EscalationPolicyEntity() {}

    /** A new policy with {@code id}. */
    public EscalationPolicyEntity(UUID id) {
        this.id = id;
    }

    /** The id. */
    public UUID getId() {
        return id;
    }

    /** The name. */
    public String getName() {
        return name;
    }

    /** Sets the name. */
    public void setName(String name) {
        this.name = name;
    }

    /** The steps as stored. */
    public List<Map<String, Object>> getSteps() {
        return steps;
    }

    /** Sets the steps. */
    public void setSteps(List<Map<String, Object>> steps) {
        this.steps = steps;
    }

    /** The repeat, or null. */
    public Map<String, Object> getRepeat() {
        return repeat;
    }

    /** Sets the repeat. */
    public void setRepeat(Map<String, Object> repeat) {
        this.repeat = repeat;
    }

    /** The stop conditions. */
    public Map<String, Object> getStopOn() {
        return stopOn;
    }

    /** Sets the stop conditions. */
    public void setStopOn(Map<String, Object> stopOn) {
        this.stopOn = stopOn;
    }

    /** When created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** Sets when created. */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /** When last changed. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** Sets when last changed. */
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
