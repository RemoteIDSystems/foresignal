package ai.foresignal.backtest;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** One backtest job (prompt 103). */
@Entity
@Table(name = "backtest_job")
public class BacktestJobEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    // 32: runner job types such as forecast-evaluation (19 characters) outgrew the original 16.
    @Column(name = "type", length = 32, nullable = false)
    private String type;

    @Column(name = "target_id", length = 64)
    private String targetId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "definition")
    private Map<String, Object> definition;

    @Column(name = "definition_hash", length = 64)
    private String definitionHash;

    @Column(name = "from_seq")
    private Long fromSeq;

    @Column(name = "to_seq")
    private Long toSeq;

    @Column(name = "range_from")
    private Instant rangeFrom;

    @Column(name = "range_to")
    private Instant rangeTo;

    @Column(name = "status", length = 16, nullable = false)
    private String status = "queued";

    @Column(name = "progress", nullable = false)
    private double progress;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "result")
    private Map<String, Object> result;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "caveats")
    private List<Map<String, Object>> caveats;

    @Column(name = "detector_version", length = 32)
    private String detectorVersion;

    /** The analysis manifest of this output (prompt 116), or null for outputs written before it. */
    @Column(name = "manifest_id", length = 26)
    private String manifestId;

    @Column(name = "created_by", length = 64)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "cancelled", nullable = false)
    private boolean canceled;

    /** True when the start was raised to the retained floor (nullable: the column arrived after prompt 103). */
    @Column(name = "range_clamped")
    private Boolean rangeClamped;

    /**
     * The process that submitted or last claimed the job (prompt 142): {@link BacktestJobService#INSTANCE} of that
     * JVM. Startup recovery fails {@code running} jobs and re-dispatches {@code queued} jobs owned by any other
     * process. Nullable: the column arrived after prompt 141.
     */
    @Column(name = "worker_instance", length = 36)
    private String workerInstance;

    /** JPA. */
    protected BacktestJobEntity() {}

    /** Creates a job. */
    public BacktestJobEntity(String id, String type, Instant createdAt) {
        this.id = id;
        this.type = type;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public Map<String, Object> getDefinition() {
        return definition;
    }

    public void setDefinition(Map<String, Object> definition) {
        this.definition = definition;
    }

    public String getDefinitionHash() {
        return definitionHash;
    }

    public void setDefinitionHash(String definitionHash) {
        this.definitionHash = definitionHash;
    }

    public Long getFromSeq() {
        return fromSeq;
    }

    public void setFromSeq(Long fromSeq) {
        this.fromSeq = fromSeq;
    }

    public Long getToSeq() {
        return toSeq;
    }

    public void setToSeq(Long toSeq) {
        this.toSeq = toSeq;
    }

    public Instant getRangeFrom() {
        return rangeFrom;
    }

    public void setRangeFrom(Instant rangeFrom) {
        this.rangeFrom = rangeFrom;
    }

    public Instant getRangeTo() {
        return rangeTo;
    }

    public void setRangeTo(Instant rangeTo) {
        this.rangeTo = rangeTo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }

    public Map<String, Object> getResult() {
        return result;
    }

    public void setResult(Map<String, Object> result) {
        this.result = result;
    }

    public List<Map<String, Object>> getCaveats() {
        return caveats;
    }

    public void setCaveats(List<Map<String, Object>> caveats) {
        this.caveats = caveats;
    }

    public String getDetectorVersion() {
        return detectorVersion;
    }

    public void setDetectorVersion(String detectorVersion) {
        this.detectorVersion = detectorVersion;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    /** True when the start was raised to the retained floor; null on rows from before the column existed. */
    public Boolean getRangeClamped() {
        return rangeClamped;
    }

    /** Records whether the start was raised to the retained floor. */
    public void setRangeClamped(Boolean rangeClamped) {
        this.rangeClamped = rangeClamped;
    }

    /** The process that submitted or last claimed the job, or null for rows written before prompt 142. */
    public String getWorkerInstance() {
        return workerInstance;
    }

    /** Sets the process that owns the job. */
    public void setWorkerInstance(String workerInstance) {
        this.workerInstance = workerInstance;
    }

    /** The analysis manifest of this output (prompt 116), or null. */
    public String getManifestId() {
        return manifestId;
    }

    /** Sets the analysis manifest (prompt 116). */
    public void setManifestId(String manifestId) {
        this.manifestId = manifestId;
    }
}
