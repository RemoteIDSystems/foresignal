package ai.foresignal.object;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A candidate historical benchmark case proposed by a verdict (table {@code benchmark_case_proposal}, prompt 129):
 * a verdict on an object with a time window becomes a 123 {@code benchmark_case} (source {@code operator}) only when
 * an operator accepts it through {@link BenchmarkCaseProposals#accept}; it is never added automatically.
 */
@Entity
@Table(name = "benchmark_case_proposal")
public class BenchmarkCaseProposalEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "annotation_id", length = 26, nullable = false)
    private String annotationId;

    @Column(name = "target_ref", length = 1024, nullable = false)
    private String targetRef;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "window_from", nullable = false)
    private Instant windowFrom;

    @Column(name = "window_to", nullable = false)
    private Instant windowTo;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "region_codes", nullable = false)
    private List<String> regionCodes = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "expectations", nullable = false)
    private Map<String, Object> expectations = new LinkedHashMap<>();

    @Column(name = "status", length = 16, nullable = false)
    private String status = "proposed";

    @Column(name = "private", nullable = false)
    private boolean privateProposal;

    @Column(name = "created_by", length = 200, nullable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "decided_by", length = 200)
    private String decidedBy;

    @Column(name = "decided_at")
    private Instant decidedAt;

    @Column(name = "case_id", length = 26)
    private String caseId;

    /** For JPA. */
    protected BenchmarkCaseProposalEntity() {}

    /**
     * A new proposal.
     *
     * @param id its ULID
     * @param annotationId the verdict that proposed it
     * @param targetRef the judged object
     * @param name the case name
     * @param windowFrom the case window start
     * @param windowTo the case window end
     * @param createdBy the verdict's author
     * @param now the creation time
     */
    public BenchmarkCaseProposalEntity(String id, String annotationId, String targetRef, String name,
            Instant windowFrom, Instant windowTo, String createdBy, Instant now) {
        this.id = id;
        this.annotationId = annotationId;
        this.targetRef = targetRef;
        this.name = name;
        this.windowFrom = windowFrom;
        this.windowTo = windowTo;
        this.createdBy = createdBy;
        this.createdAt = now;
    }

    /** The id. */
    public String getId() {
        return id;
    }

    /** The proposing verdict. */
    public String getAnnotationId() {
        return annotationId;
    }

    /** The judged object. */
    public String getTargetRef() {
        return targetRef;
    }

    /** The case name. */
    public String getName() {
        return name;
    }

    /** The window start. */
    public Instant getWindowFrom() {
        return windowFrom;
    }

    /** The window end. */
    public Instant getWindowTo() {
        return windowTo;
    }

    /** The 040 region codes. */
    public List<String> getRegionCodes() {
        return regionCodes;
    }

    /** Sets the region codes. */
    public void setRegionCodes(List<String> value) {
        this.regionCodes = value == null ? new ArrayList<>() : new ArrayList<>(value);
    }

    /** The case expectations (123 {@code BenchmarkCaseInput.expectations}). */
    public Map<String, Object> getExpectations() {
        return expectations;
    }

    /** Sets the expectations. */
    public void setExpectations(Map<String, Object> value) {
        this.expectations = value == null ? new LinkedHashMap<>() : new LinkedHashMap<>(value);
    }

    /** {@code proposed}, {@code accepted} or {@code rejected}. */
    public String getStatus() {
        return status;
    }

    /** True when the judged object is private. */
    public boolean isPrivateProposal() {
        return privateProposal;
    }

    /** Sets the private flag. */
    public void setPrivateProposal(boolean value) {
        this.privateProposal = value;
    }

    /** Who proposed it. */
    public String getCreatedBy() {
        return createdBy;
    }

    /** When it was proposed. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** Who decided it, or null. */
    public String getDecidedBy() {
        return decidedBy;
    }

    /** When it was decided, or null. */
    public Instant getDecidedAt() {
        return decidedAt;
    }

    /** The benchmark case created on acceptance, or null. */
    public String getCaseId() {
        return caseId;
    }

    /** Records a decision. */
    public void decide(String newStatus, String actor, Instant at, String createdCaseId) {
        this.status = newStatus;
        this.decidedBy = actor;
        this.decidedAt = at;
        this.caseId = createdCaseId;
    }
}
