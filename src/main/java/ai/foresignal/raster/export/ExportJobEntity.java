package ai.foresignal.raster.export;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** One raster export, place pack or (from 111) archive job (prompt 108). */
@Entity
@Table(name = "raster_export_job")
public class ExportJobEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "kind", length = 16, nullable = false)
    private String kind;

    @Column(name = "format", length = 16, nullable = false)
    private String format;

    @Column(name = "product_id", length = 64)
    private String productId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "spec")
    private Map<String, Object> spec;

    @Column(name = "status", length = 16, nullable = false)
    private String status = "QUEUED";

    @Column(name = "progress", nullable = false)
    private int progress;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "files")
    private List<Map<String, Object>> files;

    @Column(name = "row_count")
    private Long rowCount;

    @Column(name = "bytes")
    private Long bytes;

    @Column(name = "manifest_sha256", length = 64)
    private String manifestSha256;

    @Column(name = "error", length = 500)
    private String error;

    @Column(name = "created_by", length = 64)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    /** The place pack input hash (prompt 108: a pack is reused for 10 minutes per input), or null. */
    @Column(name = "input_hash", length = 64)
    private String inputHash;

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

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
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

    public List<Map<String, Object>> getFiles() {
        return files;
    }

    public void setFiles(List<Map<String, Object>> files) {
        this.files = files;
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

    public String getManifestSha256() {
        return manifestSha256;
    }

    public void setManifestSha256(String manifestSha256) {
        this.manifestSha256 = manifestSha256;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
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

    /** The input hash of a place pack, or null. */
    public String getInputHash() {
        return inputHash;
    }

    /** Sets the input hash. */
    public void setInputHash(String inputHash) {
        this.inputHash = inputHash;
    }
}
