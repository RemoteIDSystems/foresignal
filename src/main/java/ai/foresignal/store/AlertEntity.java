package ai.foresignal.store;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One firing of one rule against one record version, with its lifecycle (prompt 017).
 *
 * <p>Hibernate maps the table (Flyway creates it); the trigger engine and the alert service write it with native SQL so
 * that {@code rev} comes from the {@code alert_rev} sequence (created by
 * the Flyway baseline) on every change. The partial index on open alerts is created
 * there too, because JPA cannot express a {@code WHERE} clause.
 */
@Entity
@Table(
        name = "alert",
        indexes = {
            // The cooldown seed: "did this rule fire for this record within the cooldown?" on observed_at.
            @Index(
                    name = "alert_rule_entity_observed_idx",
                    columnList = "rule_id, source_id, external_id, observed_at"),
            // Rule-scope cooldown and per-rule statistics.
            @Index(name = "alert_rule_observed_idx", columnList = "rule_id, observed_at"),
            // The alert stream's resume (rev > Last-Event-ID).
            @Index(name = "alert_rev_idx", columnList = "rev"),
            @Index(name = "alert_fired_at_idx", columnList = "fired_at"),
            // Purging and listing one exercise run's alerts (prompt 115).
            @Index(name = "alert_exercise_run_idx", columnList = "exercise_run_id")
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

    /** The kind of the matching version. */
    @Column(name = "kind", length = 64)
    private String kind;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "fired_at", nullable = false)
    private Instant firedAt;

    @Column(name = "message", columnDefinition = "text")
    private String message;

    /** {@code info}, {@code warning} or {@code critical}, copied from the rule. */
    @Column(name = "severity", nullable = false, length = 16)
    @ColumnDefault("'warning'")
    private String severity = "warning";

    /** {@code firing}, {@code acknowledged} or {@code resolved}. */
    @Column(name = "state", nullable = false, length = 16)
    @ColumnDefault("'firing'")
    private String state = "firing";

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @Column(name = "acknowledged_by", length = 200)
    private String acknowledgedBy;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "resolved_at")
    private Instant resolvedAt;

    /** {@code inverse_edge}, {@code tombstone}, {@code expiry}, {@code stale}, {@code admin} or {@code token:<name>}. */
    @Column(name = "resolved_by", length = 200)
    private String resolvedBy;

    /** The seq of the version that resolved the alert, for automatic resolution. */
    @Column(name = "resolved_seq")
    private Long resolvedSeq;

    /** {@code useful}, {@code noise}, {@code late} or {@code false_positive}. */
    @Column(name = "feedback", length = 16)
    private String feedback;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "feedback_at")
    private Instant feedbackAt;

    @Column(name = "note", columnDefinition = "text")
    private String note;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "snoozed_until")
    private Instant snoozedUntil;

    /** True when a rule or alert snooze recorded the match without routing it. */
    @Column(name = "suppressed", nullable = false, columnDefinition = "boolean default false")
    private boolean suppressed;

    /** {@code alert_snoozed} or {@code rule_snoozed}. */
    @Column(name = "suppressed_reason", length = 32)
    private String suppressedReason;

    /** The rule's tags at fire time. */
    @JdbcTypeCode(SqlTypes.JSON)
    @ColumnDefault("'[]'::jsonb")
    @Column(name = "tags", nullable = false, columnDefinition = "jsonb")
    private List<String> tags = new ArrayList<>();

    /** Monotonic revision, bumped on every change; native writes take it from {@code alert_rev}. */
    @Column(name = "rev", nullable = false, columnDefinition = "bigint default 0")
    private long rev;

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

    /** The enrichment summary (prompt 030), or null until enriched. */
    @Column(name = "summary", columnDefinition = "text")
    private String summary;

    /** When the enrichment summary was stored (prompt 030), or null. */
    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "enriched_at")
    private Instant enrichedAt;

    /** The verified citations of the summary ({@code AskCitation} objects, prompt 030), or null. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "summary_citations")
    private List<Map<String, Object>> summaryCitations;

    /**
     * True for an alert of a rule referencing a watchlist (prompt 036): hidden from anonymous callers
     * and feed keys.
     */
    @Column(name = "sensitive", nullable = false, columnDefinition = "boolean default false")
    private boolean sensitive;

    /** The computed alert context ({@code AlertContext}: {@code near}, {@code anchor}; prompt 036), or null. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "context")
    private Map<String, Object> context;

    /**
     * The record's region tags snapshotted when the alert fired ({@code RegionTags}, prompt 040), or null;
     * the {@code country}, {@code admin1} and {@code county} filters of {@code listAlerts} read it.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "region")
    private Map<String, Object> region;

    /**
     * True for an exercise replay alert (prompt 115): delivered only to opted-in or named channels and never
     * counted in real alert state, cooldowns, rate caps or statistics.
     */
    @Column(name = "exercise", nullable = false, columnDefinition = "boolean default false")
    private boolean exercise;

    /** The exercise run that produced this alert (prompt 115), or null for a real alert. */
    @Column(name = "exercise_run_id", length = 26)
    private String exerciseRunId;

    protected AlertEntity() {
        // for JPA
    }

    /**
     * A firing alert.
     *
     * @param ruleId the rule
     * @param seq the matching version
     * @param sourceId the source
     * @param externalId the entity
     * @param firedAt when it fired
     */
    public AlertEntity(UUID ruleId, long seq, String sourceId, String externalId, Instant firedAt) {
        this.ruleId = ruleId;
        this.seq = seq;
        this.sourceId = sourceId;
        this.externalId = externalId;
        this.firedAt = firedAt;
    }

    /** The alert id. */
    public Long getId() {
        return id;
    }

    /** The rule id. */
    public UUID getRuleId() {
        return ruleId;
    }

    /** Sets the rule id. */
    public void setRuleId(UUID ruleId) {
        this.ruleId = ruleId;
    }

    /** The matching version's seq. */
    public long getSeq() {
        return seq;
    }

    /** Sets the matching version's seq. */
    public void setSeq(long seq) {
        this.seq = seq;
    }

    /** The source. */
    public String getSourceId() {
        return sourceId;
    }

    /** Sets the source. */
    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    /** The entity. */
    public String getExternalId() {
        return externalId;
    }

    /** Sets the entity. */
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    /** The kind, or null for alerts written before prompt 017. */
    public String getKind() {
        return kind;
    }

    /** Sets the kind. */
    public void setKind(String kind) {
        this.kind = kind;
    }

    /** When the alert fired. */
    public Instant getFiredAt() {
        return firedAt;
    }

    /** Sets when the alert fired. */
    public void setFiredAt(Instant firedAt) {
        this.firedAt = firedAt;
    }

    /** The rendered message. */
    public String getMessage() {
        return message;
    }

    /** Sets the rendered message. */
    public void setMessage(String message) {
        this.message = message;
    }

    /** The severity. */
    public String getSeverity() {
        return severity;
    }

    /** Sets the severity. */
    public void setSeverity(String severity) {
        this.severity = severity;
    }

    /** The lifecycle state. */
    public String getState() {
        return state;
    }

    /** Sets the lifecycle state. */
    public void setState(String state) {
        this.state = state;
    }

    /** When the alert was acknowledged, or null. */
    public Instant getAcknowledgedAt() {
        return acknowledgedAt;
    }

    /** Sets when the alert was acknowledged. */
    public void setAcknowledgedAt(Instant acknowledgedAt) {
        this.acknowledgedAt = acknowledgedAt;
    }

    /** Who acknowledged the alert, or null. */
    public String getAcknowledgedBy() {
        return acknowledgedBy;
    }

    /** When the alert was resolved, or null. */
    public Instant getResolvedAt() {
        return resolvedAt;
    }

    /** Who or what resolved the alert, or null. */
    public String getResolvedBy() {
        return resolvedBy;
    }

    /** The feedback, or null. */
    public String getFeedback() {
        return feedback;
    }

    /** Whether the alert was suppressed by a snooze. */
    public boolean isSuppressed() {
        return suppressed;
    }

    /** The tags. */
    public List<String> getTags() {
        return tags;
    }

    /** The revision. */
    public long getRev() {
        return rev;
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
    /** The region tags at fire time (prompt 040), or null. */
    public Map<String, Object> getRegion() {
        return region;
    }

    /** Sets the region tags snapshot (prompt 040). */
    public void setRegion(Map<String, Object> region) {
        this.region = region;
    }

    public Map<String, Object> getRecordSnapshot() {
        return recordSnapshot;
    }

    /** Sets the record snapshot. */
    public void setRecordSnapshot(Map<String, Object> recordSnapshot) {
        this.recordSnapshot = recordSnapshot;
    }

    /** The enrichment summary, or null. */
    public String getSummary() {
        return summary;
    }

    /** When the summary was stored, or null. */
    public Instant getEnrichedAt() {
        return enrichedAt;
    }

    /** The verified citations of the summary, or null. */
    public List<Map<String, Object>> getSummaryCitations() {
        return summaryCitations;
    }

    /** True for an exercise replay alert (prompt 115). */
    public boolean isExercise() {
        return exercise;
    }

    /** The exercise run of an exercise alert, or null (prompt 115). */
    public String getExerciseRunId() {
        return exerciseRunId;
    }
}
