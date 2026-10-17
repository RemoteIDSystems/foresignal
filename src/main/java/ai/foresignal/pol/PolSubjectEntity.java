package ai.foresignal.pol;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * An opted-in pattern-of-life subject (prompt 114, table {@code pol_subject}): a record, a 098 entity or every
 * member of a 036 watchlist. {@code last_seq} is the incremental cursor over persisted positions; {@code state} holds
 * the open stay candidate and the deviation bookkeeping between ticks.
 */
@Entity
@Table(name = "pol_subject")
public class PolSubjectEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "subject_type", length = 16, nullable = false)
    private String subjectType;

    @Column(name = "ref", length = 330, nullable = false)
    private String ref;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "params")
    private Map<String, Object> params = new LinkedHashMap<>();

    @Column(name = "private_subject", nullable = false)
    private boolean privateSubject;

    @Column(name = "precision_decimals", nullable = false)
    private int precisionDecimals = 7;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "last_seq")
    private Long lastSeq;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "last_computed_at")
    private Instant lastComputedAt;

    @Column(name = "recompute_requested", nullable = false)
    private boolean recomputeRequested;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "state")
    private Map<String, Object> state = new LinkedHashMap<>();

    /** For JPA. */
    protected PolSubjectEntity() {
        // for JPA
    }

    /** Creates a row with its id. */
    public PolSubjectEntity(String id) {
        this.id = id;
    }

    /** The ULID id. */
    public String getId() {
        return id;
    }

    /** {@code record}, {@code entity} or {@code watchlist}. */
    public String getSubjectType() {
        return subjectType;
    }

    /** Sets: {@code record}, {@code entity} or {@code watchlist}. */
    public void setSubjectType(String subjectType) {
        this.subjectType = subjectType;
    }

    /** The record {@code {sourceId}/{externalId}}, entity id or watchlist id. */
    public String getRef() {
        return ref;
    }

    /** Sets: the record {@code {sourceId}/{externalId}}, entity id or watchlist id. */
    public void setRef(String ref) {
        this.ref = ref;
    }

    /** True when the subject is analyzed. */
    public boolean isEnabled() {
        return enabled;
    }

    /** Sets: true when the subject is analyzed. */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** The stay and clustering parameters as given. */
    public Map<String, Object> getParams() {
        return params;
    }

    /** Sets: the stay and clustering parameters as given. */
    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    /** True when a member source is private: outputs are private and precision-capped. */
    public boolean isPrivateSubject() {
        return privateSubject;
    }

    /** Sets: true when a member source is private: outputs are private and precision-capped. */
    public void setPrivateSubject(boolean privateSubject) {
        this.privateSubject = privateSubject;
    }

    /** The coordinate decimals kept for a private subject. */
    public int getPrecisionDecimals() {
        return precisionDecimals;
    }

    /** Sets: the coordinate decimals kept for a private subject. */
    public void setPrecisionDecimals(int precisionDecimals) {
        this.precisionDecimals = precisionDecimals;
    }

    /** Who opted the subject in. */
    public String getCreatedBy() {
        return createdBy;
    }

    /** Sets: who opted the subject in. */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /** When created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** Sets: when created. */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /** The last persisted seq read, or null before the first pass. */
    public Long getLastSeq() {
        return lastSeq;
    }

    /** Sets: the last persisted seq read, or null before the first pass. */
    public void setLastSeq(Long lastSeq) {
        this.lastSeq = lastSeq;
    }

    /** When the clustering last ran, or null. */
    public Instant getLastComputedAt() {
        return lastComputedAt;
    }

    /** Sets: when the clustering last ran, or null. */
    public void setLastComputedAt(Instant lastComputedAt) {
        this.lastComputedAt = lastComputedAt;
    }

    /** True when a recompute was requested through the API. */
    public boolean isRecomputeRequested() {
        return recomputeRequested;
    }

    /** Sets: true when a recompute was requested through the API. */
    public void setRecomputeRequested(boolean recomputeRequested) {
        this.recomputeRequested = recomputeRequested;
    }

    /** The open stay candidate and deviation bookkeeping. */
    public Map<String, Object> getState() {
        return state;
    }

    /** Sets: the open stay candidate and deviation bookkeeping. */
    public void setState(Map<String, Object> state) {
        this.state = state;
    }

}
