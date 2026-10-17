package ai.foresignal.quality;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A persisted tuning suggestion with its lifecycle (prompt 115, table {@code tuning_suggestion}), deduplicated by
 * {@code (rule_id, kind, patch)} through {@code patch_hash}. {@code rule_rev} is the rule's definition hash when
 * the suggestion was generated; a rule edit makes an open suggestion {@code STALE}.
 */
@Entity
@Table(name = "tuning_suggestion",
        uniqueConstraints = @UniqueConstraint(name = "tuning_suggestion_dedupe_key",
                columnNames = {"rule_id", "kind", "patch_hash"}),
        indexes = @Index(name = "tuning_suggestion_rule_status_idx", columnList = "rule_id, status"))
public class TuningSuggestionEntity {

    /** Waiting for a decision. */
    public static final String OPEN = "OPEN";

    /** Applied through the rule update. */
    public static final String APPLIED = "APPLIED";

    /** Dismissed by the operator. */
    public static final String DISMISSED = "DISMISSED";

    /** The rule changed since the suggestion was generated. */
    public static final String STALE = "STALE";

    /** The statuses. */
    public static final List<String> STATUSES = List.of(OPEN, APPLIED, DISMISSED, STALE);

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "rule_id", nullable = false)
    private UUID ruleId;

    @Column(name = "rule_rev", length = 64, nullable = false)
    private String ruleRev;

    @Column(name = "kind", length = 40, nullable = false)
    private String kind;

    @Column(name = "suggestion_key", length = 200)
    private String key;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "patch")
    private Map<String, Object> patch = new LinkedHashMap<>();

    @Column(name = "patch_hash", length = 64, nullable = false)
    private String patchHash;

    @Column(name = "rationale", columnDefinition = "text")
    private String rationale;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "expected_effect")
    private Map<String, Object> expectedEffect;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "caveats")
    private List<Map<String, Object>> caveats = new ArrayList<>();

    @Column(name = "status", length = 16, nullable = false)
    private String status = OPEN;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "decided_by", length = 200)
    private String decidedBy;

    @Column(name = "decided_at")
    private Instant decidedAt;

    @Column(name = "decision_reason", columnDefinition = "text")
    private String decisionReason;

    protected TuningSuggestionEntity() {
        // for JPA
    }

    /** A new open suggestion. */
    public TuningSuggestionEntity(String id, UUID ruleId, String kind, String patchHash) {
        this.id = id;
        this.ruleId = ruleId;
        this.kind = kind;
        this.patchHash = patchHash;
    }

    /** The id (a ULID). */
    public String getId() {
        return id;
    }

    /** The rule. */
    public UUID getRuleId() {
        return ruleId;
    }

    /** The rule's definition hash at generation. */
    public String getRuleRev() {
        return ruleRev;
    }

    /** Sets the rule's definition hash. */
    public void setRuleRev(String ruleRev) {
        this.ruleRev = ruleRev;
    }

    /** The {@code TuningSuggestion.kind}. */
    public String getKind() {
        return kind;
    }

    /** The suggester's own key of the suggestion (for example {@code raise-threshold:mag}), or null. */
    public String getKey() {
        return key;
    }

    /** Sets the suggester's key. */
    public void setKey(String key) {
        this.key = key;
    }

    /** The partial {@code TriggerRuleInput} patch. */
    public Map<String, Object> getPatch() {
        return patch == null ? Map.of() : patch;
    }

    /** Sets the patch. */
    public void setPatch(Map<String, Object> patch) {
        this.patch = patch == null ? new LinkedHashMap<>() : new LinkedHashMap<>(patch);
    }

    /** The hash of the canonical patch. */
    public String getPatchHash() {
        return patchHash;
    }

    /** The rationale. */
    public String getRationale() {
        return rationale;
    }

    /** Sets the rationale. */
    public void setRationale(String rationale) {
        this.rationale = rationale;
    }

    /** The expected effect, or null. */
    public Map<String, Object> getExpectedEffect() {
        return expectedEffect;
    }

    /** Sets the expected effect. */
    public void setExpectedEffect(Map<String, Object> expectedEffect) {
        this.expectedEffect = expectedEffect == null ? null : new LinkedHashMap<>(expectedEffect);
    }

    /** The caveats. */
    public List<Map<String, Object>> getCaveats() {
        return caveats == null ? List.of() : caveats;
    }

    /** Sets the caveats. */
    public void setCaveats(List<Map<String, Object>> caveats) {
        this.caveats = caveats == null ? new ArrayList<>() : new ArrayList<>(caveats);
    }

    /** The status. */
    public String getStatus() {
        return status;
    }

    /** Sets the status. */
    public void setStatus(String status) {
        this.status = status;
    }

    /** When generated. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** Sets when generated. */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /** Who applied or dismissed it, or null. */
    public String getDecidedBy() {
        return decidedBy;
    }

    /** Sets who decided. */
    public void setDecidedBy(String decidedBy) {
        this.decidedBy = decidedBy;
    }

    /** When it was applied or dismissed, or null. */
    public Instant getDecidedAt() {
        return decidedAt;
    }

    /** Sets when it was decided. */
    public void setDecidedAt(Instant decidedAt) {
        this.decidedAt = decidedAt;
    }

    /** The dismissal reason or apply note, or null. */
    public String getDecisionReason() {
        return decisionReason;
    }

    /** Sets the decision reason. */
    public void setDecisionReason(String decisionReason) {
        this.decisionReason = decisionReason;
    }
}
