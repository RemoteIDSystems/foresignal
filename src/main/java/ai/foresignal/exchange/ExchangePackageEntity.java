package ai.foresignal.exchange;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A package built by this instance (table {@code exchange_package}, prompt 132): its signed manifest (the parsed
 * document and the exact signed bytes), the detached signature, the stored ZIP, its build and delivery state, the
 * reductions applied for egress, and per-peer deliveries with their receipts.
 */
@Entity
@Table(name = "exchange_package", indexes = {
    @Index(name = "exchange_package_created_by_idx", columnList = "created_by"),
    @Index(name = "exchange_package_object_idx", columnList = "object_ref")
})
public class ExchangePackageEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "type", length = 32, nullable = false)
    private String type;

    @Column(name = "object_ref", length = 1024, nullable = false)
    private String objectRef;

    @Column(name = "tlp", length = 16, nullable = false)
    private String tlp;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "manifest")
    private Map<String, Object> manifest;

    @Column(name = "manifest_json", columnDefinition = "text")
    private String manifestJson;

    @Column(name = "signature", columnDefinition = "text")
    private String signature;

    @Column(name = "status", length = 16, nullable = false)
    private String status = ExchangeVocabulary.BUILDING;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "file_path", length = 512)
    private String filePath;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "deliveries", nullable = false)
    private List<Map<String, Object>> deliveries = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "reductions", nullable = false)
    private List<Map<String, Object>> reductions = new ArrayList<>();

    @Column(name = "deliver", length = 8, nullable = false)
    private String deliver = "peer";

    @Column(name = "created_by", length = 128, nullable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "error", length = 2000)
    private String error;

    /** For JPA. */
    protected ExchangePackageEntity() {}

    /**
     * A new package.
     *
     * @param id the package id (ULID)
     * @param type the package type
     * @param objectRef the packaged object
     * @param tlp the TLP label
     * @param createdBy the creating principal
     * @param now when
     */
    public ExchangePackageEntity(String id, String type, String objectRef, String tlp, String createdBy, Instant now) {
        this.id = id;
        this.type = type;
        this.objectRef = objectRef;
        this.tlp = tlp;
        this.createdBy = createdBy;
        this.createdAt = now;
    }

    /** The package id. */
    public String getId() {
        return id;
    }

    /** The package type. */
    public String getType() {
        return type;
    }

    /** The packaged object. */
    public String getObjectRef() {
        return objectRef;
    }

    /** The TLP label. */
    public String getTlp() {
        return tlp;
    }

    /** The parsed manifest. */
    public Map<String, Object> getManifest() {
        return manifest;
    }

    /** The exact signed manifest bytes, as UTF-8 text. */
    public String getManifestJson() {
        return manifestJson;
    }

    /** The detached JWS. */
    public String getSignature() {
        return signature;
    }

    /** Sets the signed manifest. */
    public void setManifest(Map<String, Object> manifest, String manifestJson, String signature) {
        this.manifest = manifest;
        this.manifestJson = manifestJson;
        this.signature = signature;
    }

    /** The status. */
    public String getStatus() {
        return status;
    }

    /** Sets the status. */
    public void setStatus(String status) {
        this.status = status;
    }

    /** The ZIP size, or null while building. */
    public Long getSizeBytes() {
        return sizeBytes;
    }

    /** The stored ZIP, relative to the exchange directory. */
    public String getFilePath() {
        return filePath;
    }

    /** Sets the stored ZIP and its size. */
    public void setFile(String filePath, long sizeBytes) {
        this.filePath = filePath;
        this.sizeBytes = sizeBytes;
    }

    /** The deliveries to peers. */
    public List<Map<String, Object>> getDeliveries() {
        return deliveries;
    }

    /** Sets the deliveries. */
    public void setDeliveries(List<Map<String, Object>> deliveries) {
        this.deliveries = deliveries == null ? new ArrayList<>() : new ArrayList<>(deliveries);
    }

    /** The reductions applied for egress ({@code Caveat}s). */
    public List<Map<String, Object>> getReductions() {
        return reductions;
    }

    /** Sets the reductions. */
    public void setReductions(List<Map<String, Object>> reductions) {
        this.reductions = reductions == null ? new ArrayList<>() : new ArrayList<>(reductions);
    }

    /** {@code peer} or {@code file}. */
    public String getDeliver() {
        return deliver;
    }

    /** Sets the delivery mode. */
    public void setDeliver(String deliver) {
        this.deliver = deliver;
    }

    /** The creating principal. */
    public String getCreatedBy() {
        return createdBy;
    }

    /** When it was created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** The build error, or null. */
    public String getError() {
        return error;
    }

    /** Sets the build error. */
    public void setError(String error) {
        this.error = error;
    }
}
