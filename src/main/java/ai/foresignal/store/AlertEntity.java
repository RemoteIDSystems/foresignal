package ai.foresignal.store;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** One firing of one rule against one record revision. */
@Entity
@Table(
        name = "alert",
        indexes = {
            // Supports the cooldown check: "did this rule already fire for this record recently?"
            @Index(
                    name = "alert_cooldown_idx",
                    columnList = "rule_id, source_id, external_id, fired_at"),
            @Index(name = "alert_fired_at_idx", columnList = "fired_at")
        })
public class AlertEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "rule_id", nullable = false)
    private UUID ruleId;

    /** The {@code record_version.seq} that triggered this alert. */
    @Column(name = "seq", nullable = false)
    private long seq;

    @Column(name = "source_id", length = 64, nullable = false)
    private String sourceId;

    @Column(name = "external_id", length = 256, nullable = false)
    private String externalId;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "fired_at", nullable = false)
    private Instant firedAt;

    @Column(name = "message", columnDefinition = "text")
    private String message;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    protected AlertEntity() {
        // for JPA
    }

    public AlertEntity(UUID ruleId, long seq, String sourceId, String externalId, Instant firedAt) {
        this.ruleId = ruleId;
        this.seq = seq;
        this.sourceId = sourceId;
        this.externalId = externalId;
        this.firedAt = firedAt;
    }

    public Long getId() {
        return id;
    }

    public UUID getRuleId() {
        return ruleId;
    }

    public void setRuleId(UUID ruleId) {
        this.ruleId = ruleId;
    }

    public long getSeq() {
        return seq;
    }

    public void setSeq(long seq) {
        this.seq = seq;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public Instant getFiredAt() {
        return firedAt;
    }

    public void setFiredAt(Instant firedAt) {
        this.firedAt = firedAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public void setAcknowledgedAt(Instant acknowledgedAt) {
        this.acknowledgedAt = acknowledgedAt;
    }
}
