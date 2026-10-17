package ai.foresignal.outlook.index;

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
 * One row of {@code regional_index_fit} (prompt 126): a weight fit of an index ({@code IndexFitResult}), run as a 103
 * job (the row's id is the job's). The proposed weights carry the {@code weights_version} to pass to the weights
 * operation; {@code applied} records whether (and when) they were applied.
 */
@Entity
@Table(name = "regional_index_fit", indexes = {
    @Index(name = "regional_index_fit_index_idx", columnList = "index_id, created_at")
})
public class RegionalIndexFitEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "index_id", length = 64, nullable = false)
    private String indexId;

    @Column(name = "status", length = 16, nullable = false)
    private String status = "queued";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "lambda")
    private Double lambda;

    @Column(name = "regions")
    private Integer regions;

    @Column(name = "origins")
    private Integer origins;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "proposed_weights", columnDefinition = "jsonb")
    private Map<String, Double> proposedWeights;

    @Column(name = "weights_version")
    private Integer weightsVersion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "skill", columnDefinition = "jsonb")
    private Map<String, Object> skill;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "components", columnDefinition = "jsonb")
    private List<Map<String, Object>> components = new ArrayList<>();

    @Column(name = "applied", nullable = false)
    private boolean applied;

    @Column(name = "applied_at")
    private Instant appliedAt;

    @Column(name = "manifest_id", length = 26)
    private String manifestId;

    @Column(name = "error", columnDefinition = "text")
    private String error;

    @Column(name = "requested_by", length = 200)
    private String requestedBy;

    /** JPA. */
    protected RegionalIndexFitEntity() {}

    /** Creates a queued fit. */
    public RegionalIndexFitEntity(String id, String indexId, Instant createdAt) {
        this.id = id;
        this.indexId = indexId;
        this.createdAt = createdAt;
    }

    /** The id (the 103 job id). */
    public String getId() {
        return id;
    }

    /** The index. */
    public String getIndexId() {
        return indexId;
    }

    /** {@code queued}, {@code running}, {@code done} or {@code failed}. */
    public String getStatus() {
        return status;
    }

    /** Sets the status. */
    public void setStatus(String status) {
        this.status = status;
    }

    /** When it was requested. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** When it started, or null. */
    public Instant getStartedAt() {
        return startedAt;
    }

    /** Sets when it started. */
    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    /** When it finished, or null. */
    public Instant getFinishedAt() {
        return finishedAt;
    }

    /** Sets when it finished. */
    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }

    /** The chosen L2 penalty, or null. */
    public Double getLambda() {
        return lambda;
    }

    /** Sets the penalty. */
    public void setLambda(Double lambda) {
        this.lambda = lambda;
    }

    /** The regions fitted, or null. */
    public Integer getRegions() {
        return regions;
    }

    /** Sets the regions fitted. */
    public void setRegions(Integer regions) {
        this.regions = regions;
    }

    /** The weekly origins fitted, or null. */
    public Integer getOrigins() {
        return origins;
    }

    /** Sets the origins. */
    public void setOrigins(Integer origins) {
        this.origins = origins;
    }

    /** The proposed weights, or null. */
    public Map<String, Double> getProposedWeights() {
        return proposedWeights;
    }

    /** Sets the proposed weights. */
    public void setProposedWeights(Map<String, Double> proposedWeights) {
        this.proposedWeights = proposedWeights == null ? null : new LinkedHashMap<>(proposedWeights);
    }

    /** The weights version to apply, or null. */
    public Integer getWeightsVersion() {
        return weightsVersion;
    }

    /** Sets the weights version. */
    public void setWeightsVersion(Integer weightsVersion) {
        this.weightsVersion = weightsVersion;
    }

    /** The out-of-sample skill, or null. */
    public Map<String, Object> getSkill() {
        return skill;
    }

    /** Sets the skill. */
    public void setSkill(Map<String, Object> skill) {
        this.skill = skill == null ? null : new LinkedHashMap<>(skill);
    }

    /** The per-component fit. */
    public List<Map<String, Object>> getComponents() {
        return components;
    }

    /** Sets the per-component fit. */
    public void setComponents(List<Map<String, Object>> components) {
        this.components = components == null ? new ArrayList<>() : new ArrayList<>(components);
    }

    /** True once applied. */
    public boolean isApplied() {
        return applied;
    }

    /** Sets whether it was applied. */
    public void setApplied(boolean applied) {
        this.applied = applied;
    }

    /** When it was applied, or null. */
    public Instant getAppliedAt() {
        return appliedAt;
    }

    /** Sets when it was applied. */
    public void setAppliedAt(Instant appliedAt) {
        this.appliedAt = appliedAt;
    }

    /** The 116 manifest, or null. */
    public String getManifestId() {
        return manifestId;
    }

    /** Sets the manifest. */
    public void setManifestId(String manifestId) {
        this.manifestId = manifestId;
    }

    /** The error of a failed fit, or null. */
    public String getError() {
        return error;
    }

    /** Sets the error. */
    public void setError(String error) {
        this.error = error;
    }

    /** Who requested it, or null. */
    public String getRequestedBy() {
        return requestedBy;
    }

    /** Sets who requested it. */
    public void setRequestedBy(String requestedBy) {
        this.requestedBy = requestedBy;
    }
}
