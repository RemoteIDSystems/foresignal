package ai.foresignal.hypothesis;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One ACH evidence row of a hypothesis set (prompt 125, table {@code ach_evidence}): one per 116 evidence group, so a
 * re-report attaches to the existing row ({@code refs} grows) instead of adding weight. It holds the source, the
 * reference, the evidence group, the description, credibility and relevance, the ratings per hypothesis, the log-odds
 * contributions it applied (reversed when the row is removed), who added it and when, and {@code removed_at}.
 */
@Entity
@Table(name = "ach_evidence", indexes = {
    @Index(name = "ach_evidence_set_idx", columnList = "set_id"),
    @Index(name = "ach_evidence_group_idx", columnList = "set_id, evidence_group_key")
})
public class AchEvidenceEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "set_id", length = 26, nullable = false)
    private String setId;

    @Column(name = "source", length = 32, nullable = false)
    private String source;

    @Column(name = "ref", length = 512)
    private String ref;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "refs", columnDefinition = "jsonb", nullable = false)
    private List<String> refs = new ArrayList<>();

    @Column(name = "evidence_group_id", length = 26)
    private String evidenceGroupId;

    @Column(name = "evidence_group_key", length = 128)
    private String evidenceGroupKey;

    @Column(name = "description", length = 2000, nullable = false)
    private String description;

    @Column(name = "credibility", length = 1, nullable = false)
    private String credibility;

    @Column(name = "relevance", length = 1, nullable = false)
    private String relevance;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ratings", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> ratings = new LinkedHashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "applied", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> applied = new LinkedHashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "citations", columnDefinition = "jsonb", nullable = false)
    private List<String> citations = new ArrayList<>();

    @Column(name = "indicator_id", length = 26)
    private String indicatorId;

    @Column(name = "added_by", length = 128, nullable = false)
    private String addedBy;

    @Column(name = "added_at", nullable = false)
    private Instant addedAt;

    @Column(name = "removed_at")
    private Instant removedAt;

    /** For JPA. */
    protected AchEvidenceEntity() {}

    /**
     * Creates a row.
     *
     * @param id the ULID
     * @param setId the set
     * @param source the source
     * @param addedBy who added it
     * @param addedAt when
     */
    public AchEvidenceEntity(String id, String setId, String source, String addedBy, Instant addedAt) {
        this.id = id;
        this.setId = setId;
        this.source = source;
        this.addedBy = addedBy;
        this.addedAt = addedAt;
    }

    /** The id. */
    public String getId() {
        return id;
    }

    /** The set. */
    public String getSetId() {
        return setId;
    }

    /** The source. */
    public String getSource() {
        return source;
    }

    /** The first reference, or null. */
    public String getRef() {
        return ref;
    }

    /** Sets the reference. */
    public void setRef(String value) {
        this.ref = value;
    }

    /** Every reference attached (the first one and re-reports of the same group). */
    public List<String> getRefs() {
        return refs;
    }

    /** Sets every reference. */
    public void setRefs(List<String> value) {
        this.refs = value == null ? new ArrayList<>() : new ArrayList<>(value);
    }

    /** The 116 evidence group, or null. */
    public String getEvidenceGroupId() {
        return evidenceGroupId;
    }

    /** Sets the evidence group. */
    public void setEvidenceGroupId(String value) {
        this.evidenceGroupId = value;
    }

    /** The key rows share exactly when they are one piece of evidence ({@code group:} or {@code ref:}). */
    public String getEvidenceGroupKey() {
        return evidenceGroupKey;
    }

    /** Sets the evidence key. */
    public void setEvidenceGroupKey(String value) {
        this.evidenceGroupKey = value;
    }

    /** The description. */
    public String getDescription() {
        return description;
    }

    /** Sets the description. */
    public void setDescription(String value) {
        this.description = value;
    }

    /** The credibility grade. */
    public String getCredibility() {
        return credibility;
    }

    /** Sets the credibility. */
    public void setCredibility(String value) {
        this.credibility = value;
    }

    /** The relevance grade. */
    public String getRelevance() {
        return relevance;
    }

    /** Sets the relevance. */
    public void setRelevance(String value) {
        this.relevance = value;
    }

    /** The ratings per hypothesis id. */
    public Map<String, Object> getRatings() {
        return ratings;
    }

    /** Sets the ratings. */
    public void setRatings(Map<String, Object> value) {
        this.ratings = value == null ? new LinkedHashMap<>() : new LinkedHashMap<>(value);
    }

    /** The log-odds contributions applied per hypothesis id (manual evidence), reversed on removal. */
    public Map<String, Object> getApplied() {
        return applied;
    }

    /** Sets the applied contributions. */
    public void setApplied(Map<String, Object> value) {
        this.applied = value == null ? new LinkedHashMap<>() : new LinkedHashMap<>(value);
    }

    /** The citations. */
    public List<String> getCitations() {
        return citations;
    }

    /** Sets the citations. */
    public void setCitations(List<String> value) {
        this.citations = value == null ? new ArrayList<>() : new ArrayList<>(value);
    }

    /** The indicator that produced the row, or null. */
    public String getIndicatorId() {
        return indicatorId;
    }

    /** Sets the indicator. */
    public void setIndicatorId(String value) {
        this.indicatorId = value;
    }

    /** Who added it. */
    public String getAddedBy() {
        return addedBy;
    }

    /** When it was added. */
    public Instant getAddedAt() {
        return addedAt;
    }

    /** When it was removed, or null. */
    public Instant getRemovedAt() {
        return removedAt;
    }

    /** Sets when it was removed. */
    public void setRemovedAt(Instant value) {
        this.removedAt = value;
    }
}
