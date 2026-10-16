package ai.foresignal.export;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One bulk export, snapshot or offline pack job (prompt 111, table {@code export_job}).
 *
 * <p>{@code spec} is the request as submitted; {@code plan} is what the service decided at submit
 * time and the worker needs (the sources allowed by the egress and license rules, the exclusions,
 * the point in time of an asOf or atSeq read). {@code license_summary} is the
 * {@code ExportLicenceSummary}, completed with row counts when the job finishes.
 */
@Entity(name = "BulkExportJob")
@Table(name = "export_job", indexes = @Index(name = "export_job_principal_idx", columnList = "principal"))
public class ExportJobEntity {

    /** A bulk export. */
    public static final String EXPORT = "EXPORT";

    /** An atSeq snapshot. */
    public static final String SNAPSHOT = "SNAPSHOT";

    /** A PMTiles offline pack. */
    public static final String PACK = "PACK";

    /** Waiting for a worker. */
    public static final String QUEUED = "QUEUED";

    /** Being written. */
    public static final String RUNNING = "RUNNING";

    /** Finished; downloadable. */
    public static final String DONE = "DONE";

    /** Ended with an error. */
    public static final String FAILED = "FAILED";

    /** Canceled by its owner. */
    public static final String CANCELLED = "CANCELLED";

    /** Past {@code expiresAt}; its files are gone. */
    public static final String EXPIRED = "EXPIRED";

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "kind", length = 16, nullable = false)
    private String kind;

    @Column(name = "format", length = 16, nullable = false)
    private String format;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "spec")
    private Map<String, Object> spec;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "plan")
    private Map<String, Object> plan;

    @Column(name = "status", length = 16, nullable = false)
    private String status = QUEUED;

    @Column(name = "progress", nullable = false)
    private int progress;

    @Column(name = "principal", length = 64)
    private String principal;

    @Column(name = "row_count")
    private Long rowCount;

    @Column(name = "bytes")
    private Long bytes;

    @Column(name = "dir", length = 1024)
    private String dir;

    @Column(name = "manifest_sha256", length = 64)
    private String manifestSha256;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "license_summary")
    private Map<String, Object> licenceSummary;

    @Column(name = "watermark_seq")
    private Long watermarkSeq;

    @Column(name = "personal_use", nullable = false)
    private boolean personalUse;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "error", length = 1000)
    private String error;

    /** JPA. */
    protected ExportJobEntity() {}

    /** Creates a queued job. */
    public ExportJobEntity(String id, String kind, String format, Instant createdAt) {
        this.id = id;
        this.kind = kind;
        this.format = format;
        this.createdAt = createdAt;
    }

    /** The ULID. */
    public String getId() {
        return id;
    }

    /** {@link #EXPORT}, {@link #SNAPSHOT} or {@link #PACK}. */
    public String getKind() {
        return kind;
    }

    /** The format wire value ({@code geoparquet}, …, {@code pmtiles}). */
    public String getFormat() {
        return format;
    }

    /** The request as submitted. */
    public Map<String, Object> getSpec() {
        return spec;
    }

    /** Sets the request. */
    public void setSpec(Map<String, Object> spec) {
        this.spec = spec;
    }

    /** The service's decisions for the worker; never shown to clients. */
    public Map<String, Object> getPlan() {
        return plan == null ? Map.of() : plan;
    }

    /** Sets the plan. */
    public void setPlan(Map<String, Object> plan) {
        this.plan = plan;
    }

    /** The status. */
    public String getStatus() {
        return status;
    }

    /** Sets the status. */
    public void setStatus(String status) {
        this.status = status;
    }

    /** Progress, 0–100. */
    public int getProgress() {
        return progress;
    }

    /** Sets the progress. */
    public void setProgress(int progress) {
        this.progress = Math.max(0, Math.min(100, progress));
    }

    /** The token id that created the job. */
    public String getPrincipal() {
        return principal;
    }

    /** Sets the creating token id. */
    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    /** Rows (or tiles) written, once finished. */
    public Long getRowCount() {
        return rowCount;
    }

    /** Sets the row count. */
    public void setRowCount(Long rowCount) {
        this.rowCount = rowCount;
    }

    /** Bytes of the data files, once finished. */
    public Long getBytes() {
        return bytes;
    }

    /** Sets the bytes. */
    public void setBytes(Long bytes) {
        this.bytes = bytes;
    }

    /** The job directory. */
    public String getDir() {
        return dir;
    }

    /** Sets the job directory. */
    public void setDir(String dir) {
        this.dir = dir;
    }

    /** The SHA-256 of {@code manifest.json}, once finished. */
    public String getManifestSha256() {
        return manifestSha256;
    }

    /** Sets the manifest hash. */
    public void setManifestSha256(String manifestSha256) {
        this.manifestSha256 = manifestSha256;
    }

    /** The license summary. */
    public Map<String, Object> getLicenceSummary() {
        return licenceSummary;
    }

    /** Sets the license summary. */
    public void setLicenceSummary(Map<String, Object> licenceSummary) {
        this.licenceSummary = licenceSummary;
    }

    /** The watermark the rows were read at. */
    public Long getWatermarkSeq() {
        return watermarkSeq;
    }

    /** Sets the watermark. */
    public void setWatermarkSeq(Long watermarkSeq) {
        this.watermarkSeq = watermarkSeq;
    }

    /** True when the export includes sources only for personal use. */
    public boolean isPersonalUse() {
        return personalUse;
    }

    /** Sets the personal-use flag. */
    public void setPersonalUse(boolean personalUse) {
        this.personalUse = personalUse;
    }

    /** When the job was created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** When a worker started it. */
    public Instant getStartedAt() {
        return startedAt;
    }

    /** Sets the start time. */
    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    /** When it finished, failed or was canceled. */
    public Instant getFinishedAt() {
        return finishedAt;
    }

    /** Sets the finish time. */
    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }

    /** When the files are deleted (null: never, for a snapshot without {@code expiresAt}). */
    public Instant getExpiresAt() {
        return expiresAt;
    }

    /** Sets the expiry. */
    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    /** The failure, when {@link #FAILED}. */
    public String getError() {
        return error;
    }

    /** Sets the failure, truncated to the column. */
    public void setError(String error) {
        this.error = error == null || error.length() <= 1000 ? error : error.substring(0, 1000);
    }

    /** True once the job can no longer change: done, failed, canceled or expired. */
    public boolean terminal() {
        return DONE.equals(status) || CANCELLED.equals(status) || EXPIRED.equals(status) || FAILED.equals(status);
    }
}
