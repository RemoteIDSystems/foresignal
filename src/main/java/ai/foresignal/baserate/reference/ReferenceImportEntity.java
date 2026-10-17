package ai.foresignal.baserate.reference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One reference-history import (prompt 121, table {@code reference_import}): the dataset, its normalized parameters,
 * the file hash of a file import, rows read and written, the period covered, the status and the 103 job that runs it.
 * Imports are idempotent per {@code (dataset, period)} ({@code import_key}): asking again resumes or re-runs the same
 * row, and its completed chunks ({@code progress.done}) are skipped on a resume.
 */
@Entity
@Table(name = "reference_import", indexes = {
    @Index(name = "reference_import_key_idx", columnList = "import_key"),
    @Index(name = "reference_import_created_idx", columnList = "created_at")
})
public class ReferenceImportEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "dataset", length = 16, nullable = false)
    private String dataset;

    @Column(name = "import_key", length = 64, nullable = false)
    private String importKey;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "params", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> params = new LinkedHashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "progress", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> progress = new LinkedHashMap<>();

    @Column(name = "file_sha256", length = 64)
    private String fileSha256;

    @Column(name = "rows_read", nullable = false)
    private long rowsRead;

    @Column(name = "rows_written", nullable = false)
    private long rowsWritten;

    @Column(name = "period_from")
    private LocalDate periodFrom;

    @Column(name = "period_to")
    private LocalDate periodTo;

    @Column(name = "status", length = 16, nullable = false)
    private String status = "queued";

    @Column(name = "status_reason", length = 500)
    private String statusReason;

    @Column(name = "licence", length = 64, nullable = false)
    private String license;

    @Column(name = "egress", length = 16, nullable = false)
    private String egress;

    @Column(name = "job_id", length = 26)
    private String jobId;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "error", columnDefinition = "text")
    private String error;

    @Column(name = "created_by", length = 200, nullable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** For JPA. */
    protected ReferenceImportEntity() {}

    /**
     * Creates an import.
     *
     * @param id the ULID
     * @param dataset the dataset
     * @param importKey the idempotency key
     * @param createdBy the actor
     * @param createdAt when it was created
     */
    public ReferenceImportEntity(String id, String dataset, String importKey, String createdBy, Instant createdAt) {
        this.id = id;
        this.dataset = dataset;
        this.importKey = importKey;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    /** The ULID. */
    public String getId() {
        return id;
    }

    /** {@code ucdp-ged}, {@code acled}, {@code usgs-comcat} or {@code gdacs}. */
    public String getDataset() {
        return dataset;
    }

    /** The idempotency key: a SHA-256 of the dataset and its normalized parameters. */
    public String getImportKey() {
        return importKey;
    }

    /** The normalized parameters. */
    public Map<String, Object> getParams() {
        return params == null ? Map.of() : params;
    }

    /** Sets the parameters. */
    public void setParams(Map<String, Object> value) {
        this.params = value == null ? new LinkedHashMap<>() : new LinkedHashMap<>(value);
    }

    /** The resume state ({@code done}: the completed chunks, {@code fraction}: progress 0–1). */
    public Map<String, Object> getProgress() {
        return progress == null ? Map.of() : progress;
    }

    /** Sets the resume state. */
    public void setProgress(Map<String, Object> value) {
        this.progress = value == null ? new LinkedHashMap<>() : new LinkedHashMap<>(value);
    }

    /** The SHA-256 of an imported file, or null. */
    public String getFileSha256() {
        return fileSha256;
    }

    /** Sets the file hash. */
    public void setFileSha256(String value) {
        this.fileSha256 = value;
    }

    /** Upstream rows read. */
    public long getRowsRead() {
        return rowsRead;
    }

    /** Sets the rows read. */
    public void setRowsRead(long value) {
        this.rowsRead = value;
    }

    /** Weekly rows written. */
    public long getRowsWritten() {
        return rowsWritten;
    }

    /** Sets the rows written. */
    public void setRowsWritten(long value) {
        this.rowsWritten = value;
    }

    /** The first day covered, or null. */
    public LocalDate getPeriodFrom() {
        return periodFrom;
    }

    /** Sets the first day covered. */
    public void setPeriodFrom(LocalDate value) {
        this.periodFrom = value;
    }

    /** The day after the last day covered (exclusive), or null. */
    public LocalDate getPeriodTo() {
        return periodTo;
    }

    /** Sets the end of the period (exclusive). */
    public void setPeriodTo(LocalDate value) {
        this.periodTo = value;
    }

    /** {@code queued}, {@code running}, {@code done}, {@code failed}, {@code skipped} or {@code cancelled}. */
    public String getStatus() {
        return status;
    }

    /** Sets the status. */
    public void setStatus(String value) {
        this.status = value;
    }

    /** Why the import has its status (e.g. {@code credentials absent}), or null. */
    public String getStatusReason() {
        return statusReason;
    }

    /** Sets the status reason. */
    public void setStatusReason(String value) {
        this.statusReason = value == null || value.length() <= 500 ? value : value.substring(0, 500);
    }

    /** The dataset's license. */
    public String getLicense() {
        return license;
    }

    /** Sets the license. */
    public void setLicense(String value) {
        this.license = value;
    }

    /** {@code OPEN} or {@code LOCAL_ONLY}. */
    public String getEgress() {
        return egress;
    }

    /** Sets the egress. */
    public void setEgress(String value) {
        this.egress = value;
    }

    /** The 103 job, or null. */
    public String getJobId() {
        return jobId;
    }

    /** Sets the job. */
    public void setJobId(String value) {
        this.jobId = value;
    }

    /** When the run started, or null. */
    public Instant getStartedAt() {
        return startedAt;
    }

    /** Sets the start. */
    public void setStartedAt(Instant value) {
        this.startedAt = value;
    }

    /** When the run finished, or null. */
    public Instant getFinishedAt() {
        return finishedAt;
    }

    /** Sets the finish. */
    public void setFinishedAt(Instant value) {
        this.finishedAt = value;
    }

    /** The failure, or null. */
    public String getError() {
        return error;
    }

    /** Sets the failure. */
    public void setError(String value) {
        this.error = value;
    }

    /** Who asked for it. */
    public String getCreatedBy() {
        return createdBy;
    }

    /** When it was created. */
    public Instant getCreatedAt() {
        return createdAt;
    }
}
