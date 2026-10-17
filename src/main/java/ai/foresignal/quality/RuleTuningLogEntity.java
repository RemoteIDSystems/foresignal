package ai.foresignal.quality;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One applied tuning change (prompt 115, table {@code rule_tuning_log}): the rule definition before and after,
 * who applied it and when, and its undo. {@code after_rev} is the definition hash right after the change, so an
 * undo is allowed only while the rule is unchanged since.
 */
@Entity
@Table(name = "rule_tuning_log", indexes = @Index(name = "rule_tuning_log_rule_idx", columnList = "rule_id, applied_at"))
public class RuleTuningLogEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "rule_id", nullable = false)
    private UUID ruleId;

    @Column(name = "suggestion_id", length = 26)
    private String suggestionId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "before_input")
    private Map<String, Object> before = new LinkedHashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "after_input")
    private Map<String, Object> after = new LinkedHashMap<>();

    @Column(name = "after_rev", length = 64, nullable = false)
    private String afterRev;

    @Column(name = "note", columnDefinition = "text")
    private String note;

    @Column(name = "applied_by", length = 200, nullable = false)
    private String appliedBy;

    @Column(name = "applied_at", nullable = false)
    private Instant appliedAt;

    @Column(name = "undone_by", length = 200)
    private String undoneBy;

    @Column(name = "undone_at")
    private Instant undoneAt;

    protected RuleTuningLogEntity() {
        // for JPA
    }

    /** A new entry. */
    public RuleTuningLogEntity(String id, UUID ruleId) {
        this.id = id;
        this.ruleId = ruleId;
    }

    /** The id (a ULID). */
    public String getId() {
        return id;
    }

    /** The rule. */
    public UUID getRuleId() {
        return ruleId;
    }

    /** The applied suggestion, or null. */
    public String getSuggestionId() {
        return suggestionId;
    }

    /** Sets the applied suggestion. */
    public void setSuggestionId(String suggestionId) {
        this.suggestionId = suggestionId;
    }

    /** The rule input before the change. */
    public Map<String, Object> getBefore() {
        return before == null ? Map.of() : before;
    }

    /** Sets the rule input before the change. */
    public void setBefore(Map<String, Object> before) {
        this.before = before == null ? new LinkedHashMap<>() : new LinkedHashMap<>(before);
    }

    /** The rule input after the change. */
    public Map<String, Object> getAfter() {
        return after == null ? Map.of() : after;
    }

    /** Sets the rule input after the change. */
    public void setAfter(Map<String, Object> after) {
        this.after = after == null ? new LinkedHashMap<>() : new LinkedHashMap<>(after);
    }

    /** The definition hash after the change. */
    public String getAfterRev() {
        return afterRev;
    }

    /** Sets the definition hash after the change. */
    public void setAfterRev(String afterRev) {
        this.afterRev = afterRev;
    }

    /** The note, or null. */
    public String getNote() {
        return note;
    }

    /** Sets the note. */
    public void setNote(String note) {
        this.note = note;
    }

    /** Who applied it. */
    public String getAppliedBy() {
        return appliedBy;
    }

    /** Sets who applied it. */
    public void setAppliedBy(String appliedBy) {
        this.appliedBy = appliedBy;
    }

    /** When it was applied. */
    public Instant getAppliedAt() {
        return appliedAt;
    }

    /** Sets when it was applied. */
    public void setAppliedAt(Instant appliedAt) {
        this.appliedAt = appliedAt;
    }

    /** Who undid it, or null. */
    public String getUndoneBy() {
        return undoneBy;
    }

    /** Sets who undid it. */
    public void setUndoneBy(String undoneBy) {
        this.undoneBy = undoneBy;
    }

    /** When it was undone, or null. */
    public Instant getUndoneAt() {
        return undoneAt;
    }

    /** Sets when it was undone. */
    public void setUndoneAt(Instant undoneAt) {
        this.undoneAt = undoneAt;
    }
}
