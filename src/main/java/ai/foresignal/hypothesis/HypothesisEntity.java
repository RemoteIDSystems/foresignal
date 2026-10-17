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
 * One competing hypothesis of a set (prompt 125, table {@code hypothesis}): a short label and statement, its type
 * ({@code outcome}, {@code explanation}, {@code speculative-link}, {@code other}), the prior and its outside-view
 * basis, the current probability (changed only through revisions), the derived ICD 203 term, a separate confidence and
 * its basis, the ledger outcome it maps to and its status. Speculative links also carry the linked findings, the
 * coincidence baseline, at least two alternatives and the computed probability cap with any logged override.
 */
@Entity
@Table(name = "hypothesis", indexes = @Index(name = "hypothesis_set_idx", columnList = "set_id"))
public class HypothesisEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "set_id", length = 26, nullable = false)
    private String setId;

    @Column(name = "position", nullable = false)
    private int position;

    @Column(name = "label", length = 80, nullable = false)
    private String label;

    @Column(name = "statement", length = 2000, nullable = false)
    private String statement;

    @Column(name = "type", length = 32, nullable = false)
    private String type;

    @Column(name = "prior", nullable = false)
    private double prior;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "prior_basis", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> priorBasis = new LinkedHashMap<>();

    @Column(name = "probability", nullable = false)
    private double probability;

    @Column(name = "estimative_term", length = 32, nullable = false)
    private String estimativeTerm;

    @Column(name = "confidence", length = 16, nullable = false)
    private String confidence = HypothesisVocabulary.LOW;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "confidence_basis", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> confidenceBasis = new LinkedHashMap<>();

    @Column(name = "ledger_outcome", length = 64)
    private String ledgerOutcome;

    @Column(name = "status", length = 16, nullable = false)
    private String status = "open";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "linked_finding_ids", columnDefinition = "jsonb", nullable = false)
    private List<String> linkedFindingIds = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "coincidence_baseline", columnDefinition = "jsonb")
    private Map<String, Object> coincidenceBaseline;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "alternatives", columnDefinition = "jsonb", nullable = false)
    private List<String> alternatives = new ArrayList<>();

    @Column(name = "probability_cap")
    private Double probabilityCap;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "cap_override", columnDefinition = "jsonb")
    private Map<String, Object> capOverride;

    @Column(name = "version", nullable = false)
    private int version = 1;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected HypothesisEntity() {}

    /**
     * Creates a hypothesis.
     *
     * @param id the ULID
     * @param setId the set
     * @param now the creation time
     */
    public HypothesisEntity(String id, String setId, Instant now) {
        this.id = id;
        this.setId = setId;
        this.createdAt = now;
        this.updatedAt = now;
    }

    /** The id. */
    public String getId() {
        return id;
    }

    /** The set. */
    public String getSetId() {
        return setId;
    }

    /** The position within the set (creation order). */
    public int getPosition() {
        return position;
    }

    /** Sets the position. */
    public void setPosition(int value) {
        this.position = value;
    }

    /** The short label. */
    public String getLabel() {
        return label;
    }

    /** Sets the label. */
    public void setLabel(String value) {
        this.label = value;
    }

    /** The statement. */
    public String getStatement() {
        return statement;
    }

    /** Sets the statement. */
    public void setStatement(String value) {
        this.statement = value;
    }

    /** The type. */
    public String getType() {
        return type;
    }

    /** Sets the type. */
    public void setType(String value) {
        this.type = value;
    }

    /** True for a speculative link. */
    public boolean speculative() {
        return HypothesisVocabulary.SPECULATIVE_LINK.equals(type);
    }

    /** The prior. */
    public double getPrior() {
        return prior;
    }

    /** Sets the prior. */
    public void setPrior(double value) {
        this.prior = value;
    }

    /** The prior basis ({@code {type, ref, rationale, value?}}). */
    public Map<String, Object> getPriorBasis() {
        return priorBasis;
    }

    /** Sets the prior basis. */
    public void setPriorBasis(Map<String, Object> value) {
        this.priorBasis = value == null ? new LinkedHashMap<>() : new LinkedHashMap<>(value);
    }

    /** The current probability. */
    public double getProbability() {
        return probability;
    }

    /** Sets the probability and its derived term (only the revision writer calls this). */
    public void setProbability(double value) {
        this.probability = value;
        this.estimativeTerm = ai.foresignal.forecast.language.EstimativeLanguage.term(value);
    }

    /** The ICD 203 term of the probability. */
    public String getEstimativeTerm() {
        return estimativeTerm;
    }

    /** The confidence. */
    public String getConfidence() {
        return confidence;
    }

    /** Sets the confidence. */
    public void setConfidence(String value) {
        this.confidence = value;
    }

    /** The confidence basis. */
    public Map<String, Object> getConfidenceBasis() {
        return confidenceBasis;
    }

    /** Sets the confidence basis. */
    public void setConfidenceBasis(Map<String, Object> value) {
        this.confidenceBasis = value == null ? new LinkedHashMap<>() : new LinkedHashMap<>(value);
    }

    /** The ledger outcome this hypothesis maps to, or null. */
    public String getLedgerOutcome() {
        return ledgerOutcome;
    }

    /** Sets the ledger outcome. */
    public void setLedgerOutcome(String value) {
        this.ledgerOutcome = value;
    }

    /** The status. */
    public String getStatus() {
        return status;
    }

    /** Sets the status. */
    public void setStatus(String value) {
        this.status = value;
    }

    /** The linked 119 findings (speculative links). */
    public List<String> getLinkedFindingIds() {
        return linkedFindingIds;
    }

    /** Sets the linked findings. */
    public void setLinkedFindingIds(List<String> value) {
        this.linkedFindingIds = value == null ? new ArrayList<>() : new ArrayList<>(value);
    }

    /** The coincidence baseline (speculative links), or null. */
    public Map<String, Object> getCoincidenceBaseline() {
        return coincidenceBaseline;
    }

    /** Sets the coincidence baseline. */
    public void setCoincidenceBaseline(Map<String, Object> value) {
        this.coincidenceBaseline = value == null ? null : new LinkedHashMap<>(value);
    }

    /** The alternative hypothesis ids (speculative links). */
    public List<String> getAlternatives() {
        return alternatives;
    }

    /** Sets the alternatives. */
    public void setAlternatives(List<String> value) {
        this.alternatives = value == null ? new ArrayList<>() : new ArrayList<>(value);
    }

    /** The computed probability cap (speculative links), or null. */
    public Double getProbabilityCap() {
        return probabilityCap;
    }

    /** Sets the probability cap. */
    public void setProbabilityCap(Double value) {
        this.probabilityCap = value;
    }

    /** The logged cap override ({@code {by, at, rationale}}), or null. */
    public Map<String, Object> getCapOverride() {
        return capOverride;
    }

    /** Sets the cap override. */
    public void setCapOverride(Map<String, Object> value) {
        this.capOverride = value == null ? null : new LinkedHashMap<>(value);
    }

    /** The version, incremented on every change. */
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

    /** Marks a change at {@code now} and increments the version. */
    public void touch(Instant now) {
        this.updatedAt = now;
        this.version++;
    }
}
