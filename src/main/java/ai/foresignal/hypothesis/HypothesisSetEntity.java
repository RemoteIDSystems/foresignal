package ai.foresignal.hypothesis;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A hypothesis set (prompt 125, table {@code hypothesis_set}): one key question, its kind ({@code exclusive}: the
 * probabilities sum to 1 with exactly one {@code other} hypothesis; {@code independent}), the review cadence, the
 * horizon by which it resolves, the status ({@code active}, {@code resolved}, {@code archived}), the linked situation
 * and ledger question, regions and themes, its origin, the set-level confidence, review times and visibility.
 * {@code version} is the optimistic lock of {@code PATCH}; the band-change notification state implements the 6-hour
 * rate limit (the latest change wins).
 */
@Entity
@Table(name = "hypothesis_set", indexes = {
    @Index(name = "hypothesis_set_status_idx", columnList = "status, cadence"),
    @Index(name = "hypothesis_set_situation_idx", columnList = "situation_id"),
    @Index(name = "hypothesis_set_question_idx", columnList = "question_id")
})
public class HypothesisSetEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "question", length = 300, nullable = false)
    private String question;

    @Column(name = "description", length = 4000)
    private String description;

    @Column(name = "kind", length = 16, nullable = false)
    private String kind;

    @Column(name = "cadence", length = 16, nullable = false)
    private String cadence;

    @Column(name = "horizon")
    private LocalDate horizon;

    @Column(name = "status", length = 16, nullable = false)
    private String status = HypothesisVocabulary.SET_ACTIVE;

    @Column(name = "situation_id", length = 26)
    private String situationId;

    @Column(name = "question_id", length = 26)
    private String questionId;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "region_codes", columnDefinition = "text[]")
    private String[] regionCodes = new String[0];

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "themes", columnDefinition = "text[]")
    private String[] themes = new String[0];

    @Column(name = "origin", length = 16, nullable = false)
    private String origin;

    @Column(name = "confidence", length = 16, nullable = false)
    private String confidence = HypothesisVocabulary.LOW;

    @Column(name = "last_reviewed_at")
    private Instant lastReviewedAt;

    @Column(name = "next_review_at")
    private Instant nextReviewAt;

    @Column(name = "last_revision_id", length = 26)
    private String lastRevisionId;

    @Column(name = "last_revision_at")
    private Instant lastRevisionAt;

    @Column(name = "private", nullable = false)
    private boolean privateSet;

    @Column(name = "review_flagged", nullable = false)
    private boolean reviewFlagged;

    @Column(name = "resolved_outcome", length = 64)
    private String resolvedOutcome;

    @Column(name = "last_band_notified_at")
    private Instant lastBandNotifiedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "pending_band_change", columnDefinition = "jsonb")
    private Map<String, Object> pendingBandChange;

    @Column(name = "created_by", length = 128)
    private String createdBy;

    @Version
    @Column(name = "version", nullable = false)
    private int version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected HypothesisSetEntity() {}

    /**
     * Creates a set.
     *
     * @param id the ULID
     * @param now the creation time
     */
    public HypothesisSetEntity(String id, Instant now) {
        this.id = id;
        this.createdAt = now;
        this.updatedAt = now;
    }

    /** The id. */
    public String getId() {
        return id;
    }

    /** The key question. */
    public String getQuestion() {
        return question;
    }

    /** Sets the key question. */
    public void setQuestion(String value) {
        this.question = value;
    }

    /** The description, or null. */
    public String getDescription() {
        return description;
    }

    /** Sets the description. */
    public void setDescription(String value) {
        this.description = value;
    }

    /** {@code exclusive} or {@code independent}. */
    public String getKind() {
        return kind;
    }

    /** Sets the kind. */
    public void setKind(String value) {
        this.kind = value;
    }

    /** True for an exclusive set. */
    public boolean exclusive() {
        return HypothesisVocabulary.EXCLUSIVE.equals(kind);
    }

    /** The review cadence. */
    public String getCadence() {
        return cadence;
    }

    /** Sets the review cadence. */
    public void setCadence(String value) {
        this.cadence = value;
    }

    /** The horizon, or null. */
    public LocalDate getHorizon() {
        return horizon;
    }

    /** Sets the horizon. */
    public void setHorizon(LocalDate value) {
        this.horizon = value;
    }

    /** The status. */
    public String getStatus() {
        return status;
    }

    /** Sets the status. */
    public void setStatus(String value) {
        this.status = value;
    }

    /** True while the set is active. */
    public boolean active() {
        return HypothesisVocabulary.SET_ACTIVE.equals(status);
    }

    /** The linked situation, or null. */
    public String getSituationId() {
        return situationId;
    }

    /** Sets the linked situation. */
    public void setSituationId(String value) {
        this.situationId = value;
    }

    /** The linked ledger question, or null. */
    public String getQuestionId() {
        return questionId;
    }

    /** Sets the linked ledger question. */
    public void setQuestionId(String value) {
        this.questionId = value;
    }

    /** The region codes. */
    public List<String> getRegionCodes() {
        return regionCodes == null ? List.of() : List.of(regionCodes);
    }

    /** Sets the region codes. */
    public void setRegionCodes(List<String> value) {
        this.regionCodes = value == null ? new String[0] : value.toArray(String[]::new);
    }

    /** The themes. */
    public List<String> getThemes() {
        return themes == null ? List.of() : List.of(themes);
    }

    /** Sets the themes. */
    public void setThemes(List<String> value) {
        this.themes = value == null ? new String[0] : value.toArray(String[]::new);
    }

    /** {@code operator}, {@code agent} or {@code analyst}. */
    public String getOrigin() {
        return origin;
    }

    /** Sets the origin. */
    public void setOrigin(String value) {
        this.origin = value;
    }

    /** The set-level confidence. */
    public String getConfidence() {
        return confidence;
    }

    /** Sets the set-level confidence. */
    public void setConfidence(String value) {
        this.confidence = value;
    }

    /** When it was last reviewed, or null. */
    public Instant getLastReviewedAt() {
        return lastReviewedAt;
    }

    /** Sets when it was last reviewed. */
    public void setLastReviewedAt(Instant value) {
        this.lastReviewedAt = value;
    }

    /** When the next review is due, or null. */
    public Instant getNextReviewAt() {
        return nextReviewAt;
    }

    /** Sets when the next review is due. */
    public void setNextReviewAt(Instant value) {
        this.nextReviewAt = value;
    }

    /** The newest revision, or null. */
    public String getLastRevisionId() {
        return lastRevisionId;
    }

    /** Sets the newest revision. */
    public void setLastRevisionId(String value) {
        this.lastRevisionId = value;
    }

    /** When the newest revision was written, or null. */
    public Instant getLastRevisionAt() {
        return lastRevisionAt;
    }

    /** Sets when the newest revision was written. */
    public void setLastRevisionAt(Instant value) {
        this.lastRevisionAt = value;
    }

    /** Whether the set is private. */
    public boolean isPrivateSet() {
        return privateSet;
    }

    /** Sets whether the set is private. */
    public void setPrivateSet(boolean value) {
        this.privateSet = value;
    }

    /** True when a broken linchpin flagged the set for review. */
    public boolean isReviewFlagged() {
        return reviewFlagged;
    }

    /** Sets the review flag. */
    public void setReviewFlagged(boolean value) {
        this.reviewFlagged = value;
    }

    /** The outcome of the linked question once resolved, or null. */
    public String getResolvedOutcome() {
        return resolvedOutcome;
    }

    /** Sets the resolved outcome. */
    public void setResolvedOutcome(String value) {
        this.resolvedOutcome = value;
    }

    /** When the last band-change notification was sent, or null. */
    public Instant getLastBandNotifiedAt() {
        return lastBandNotifiedAt;
    }

    /** Sets when the last band-change notification was sent. */
    public void setLastBandNotifiedAt(Instant value) {
        this.lastBandNotifiedAt = value;
    }

    /** The band change waiting for the rate limit to pass, or null. */
    public Map<String, Object> getPendingBandChange() {
        return pendingBandChange;
    }

    /** Sets the waiting band change. */
    public void setPendingBandChange(Map<String, Object> value) {
        this.pendingBandChange = value == null ? null : new LinkedHashMap<>(value);
    }

    /** Who created it, or null. */
    public String getCreatedBy() {
        return createdBy;
    }

    /** Sets who created it. */
    public void setCreatedBy(String value) {
        this.createdBy = value;
    }

    /** The optimistic lock version. */
    public int getVersion() {
        return version;
    }

    /** When it was created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** When it last changed. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** Marks a change at {@code now}. */
    public void touch(Instant now) {
        this.updatedAt = now;
    }
}
