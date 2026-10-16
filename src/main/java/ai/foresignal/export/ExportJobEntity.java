package ai.foresignal.export;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** One bulk export, snapshot or offline pack job (prompt 111, table {@code export_job}). */
@Entity
@Table(name = "export_job")
public class ExportJobEntity {

    /** The kinds. */
    public static final String EXPORT = "EXPORT";
    public static final String SNAPSHOT = "SNAPSHOT";
    public static final String PACK = "PACK";

    /** The statuses. */
    public static final String QUEUED = "QUEUED";
    public static final String RUNNING = "RUNNING";
    public static final String DONE = "DONE";
    public static final String FAILED = "FAILED";
    public static final String CANCELLED = "CANCELLED";
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

    @Column(name = "dir", length = 200)
    private String dir;

    @Column(name = "manifest_sha256", length = 64)
    private String manifestSha256;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "licence_summary")
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

    @Column(name = "error", length = 500)
    private String error;

    /** JPA. */
    protected ExportJobEntity() {}

    /** Creates a job. */
    public ExportJobEntity(String id, String kind, String format, Instant createdAt) {
        this.id = id;
        this.kind = kind;
        this.format = format;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getKind() {
        return kind;
    }

    public String getFormat() {
        return format;
    }

    public Map<String, Object> getSpec() {
        return spec;
    }

    public void setSpec(Map<String, Object> spec) {
        this.spec = spec;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public Long getRowCount() {
        return rowCount;
    }

    public void setRowCount(Long rowCount) {
        this.rowCount = rowCount;
    }

    public Long getBytes() {
        return bytes;
    }

    public void setBytes(Long bytes) {
        this.bytes = bytes;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getManifestSha256() {
        return manifestSha256;
    }

    public void setManifestSha256(String manifestSha256) {
        this.manifestSha256 = manifestSha256;
    }

    public Map<String, Object> getLicenceSummary() {
        return licenceSummary;
    }

    public void setLicenceSummary(Map<String, Object> licenceSummary) {
        this.licenceSummary = licenceSummary;
    }

    public Long getWatermarkSeq() {
        return watermarkSeq;
    }

    public void setWatermarkSeq(Long watermarkSeq) {
        this.watermarkSeq = watermarkSeq;
    }

    public boolean isPersonalUse() {
        return personalUse;
    }

    public void setPersonalUse(boolean personalUse) {
        this.personalUse = personalUse;
    }

    public Instant getCreatedAt() {
        return createdAt;
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

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    /** True when cancelled or expired. */
    public boolean terminal() {
        return CANCELLED.equals(status) || EXPIRED.equals(status) || FAILED.equals(status);
    }

    /** The files map (unused placeholder for API symmetry). */
    public List<Map<String, Object>> getFiles() {
        return List.of();
    }
}
