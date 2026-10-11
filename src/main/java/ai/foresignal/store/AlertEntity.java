package ai.foresignal.store;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
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

    /** The triggering observation's {@code observed_at}. */
    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "observed_at")
    private Instant observedAt;

    /**
     * The record as it was when the alert fired (Core-Data-Model §8): kind, version, observedAt,
     * GeoJSON geometry and payload, at most 16 KB. Alerts outlive the versions they refer to, so
     * they carry their own copy.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "record_snapshot")
    private Map<String, Object> recordSnapshot;

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

    /** The triggering observation's time, or null. */
    public Instant getObservedAt() {
        return observedAt;
    }

    /** Sets the triggering observation's time. */
    public void setObservedAt(Instant observedAt) {
        this.observedAt = observedAt;
    }

    /** The record snapshot taken when the alert fired, or null. */
    public Map<String, Object> getRecordSnapshot() {
        return recordSnapshot;
    }

    /** Sets the record snapshot. */
    public void setRecordSnapshot(Map<String, Object> recordSnapshot) {
        this.recordSnapshot = recordSnapshot;
    }
}
