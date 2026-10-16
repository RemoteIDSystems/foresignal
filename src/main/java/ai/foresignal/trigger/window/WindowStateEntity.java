package ai.foresignal.trigger.window;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * One group's windowed-rule state (prompt 100, table {@code window_state}). Keyed by rule and group.
 * {@code state} is {@code active}, {@code clear}, {@code unknown} or {@code learning}.
 */
@Entity
@Table(name = "window_state")
@IdClass(WindowStateEntity.Key.class)
public class WindowStateEntity {

    @Id
    @Column(name = "rule_id", nullable = false)
    private UUID ruleId;

    @Id
    @Column(name = "group_key", nullable = false, length = 256)
    private String groupKey;

    @Column(name = "state", nullable = false, length = 16)
    private String state = "clear";

    @Column(name = "since")
    private Instant since;

    @Column(name = "last_value")
    private Double lastValue;

    @Column(name = "last_evaluated_at")
    private Instant lastEvaluatedAt;

    @Column(name = "last_watermark_seq")
    private Long lastWatermarkSeq;

    @Column(name = "reason")
    private String reason;

    @Column(name = "last_alert_id")
    private Long lastAlertId;

    /**
     * True while an alert is owed for the group's latest clear → active transition: it is written once
     * the {@code rule-windows} version of that state exists (so {@code alert.seq} references it), and
     * retried until it is written.
     */
    @Column(name = "alert_pending")
    private Boolean alertPending;

    /** When the owed alert became pending. */
    @Column(name = "alert_pending_since")
    private Instant alertPendingSince;

    /** The owed alert's {@code alert.context} JSON (the anomaly fields), or null. */
    @Column(name = "pending_context", columnDefinition = "text")
    private String pendingContext;

    /** The owed alert's message, or null for the default. */
    @Column(name = "pending_message", columnDefinition = "text")
    private String pendingMessage;

    /** JPA. */
    protected WindowStateEntity() {}

    /** Creates one group's state. */
    public WindowStateEntity(UUID ruleId, String groupKey) {
        this.ruleId = ruleId;
        this.groupKey = groupKey;
    }

    public UUID getRuleId() {
        return ruleId;
    }

    public void setRuleId(UUID ruleId) {
        this.ruleId = ruleId;
    }

    public String getGroupKey() {
        return groupKey;
    }

    public void setGroupKey(String groupKey) {
        this.groupKey = groupKey;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Instant getSince() {
        return since;
    }

    public void setSince(Instant since) {
        this.since = since;
    }

    public Double getLastValue() {
        return lastValue;
    }

    public void setLastValue(Double lastValue) {
        this.lastValue = lastValue;
    }

    public Instant getLastEvaluatedAt() {
        return lastEvaluatedAt;
    }

    public void setLastEvaluatedAt(Instant lastEvaluatedAt) {
        this.lastEvaluatedAt = lastEvaluatedAt;
    }

    public Long getLastWatermarkSeq() {
        return lastWatermarkSeq;
    }

    public void setLastWatermarkSeq(Long lastWatermarkSeq) {
        this.lastWatermarkSeq = lastWatermarkSeq;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Long getLastAlertId() {
        return lastAlertId;
    }

    public void setLastAlertId(Long lastAlertId) {
        this.lastAlertId = lastAlertId;
    }

    /** True while an alert is owed for this group. */
    public boolean isAlertPending() {
        return Boolean.TRUE.equals(alertPending);
    }

    /** Marks an alert as owed (with its context and message) or settled. */
    public void setAlertPending(boolean pending, Instant since, String context, String message) {
        this.alertPending = pending;
        this.alertPendingSince = pending ? since : null;
        this.pendingContext = pending ? context : null;
        this.pendingMessage = pending ? message : null;
    }

    public Instant getAlertPendingSince() {
        return alertPendingSince;
    }

    public String getPendingContext() {
        return pendingContext;
    }

    public String getPendingMessage() {
        return pendingMessage;
    }

    /** The composite key. */
    public static class Key implements java.io.Serializable {

        private static final long serialVersionUID = 1L;

        private UUID ruleId;
        private String groupKey;

        /** JPA. */
        public Key() {}

        /** Creates the key. */
        public Key(UUID ruleId, String groupKey) {
            this.ruleId = ruleId;
            this.groupKey = groupKey;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Key id)) {
                return false;
            }
            return java.util.Objects.equals(ruleId, id.ruleId) && java.util.Objects.equals(groupKey, id.groupKey);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(ruleId, groupKey);
        }
    }
}
