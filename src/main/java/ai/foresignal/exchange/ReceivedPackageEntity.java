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
 * A package imported read-only from another instance (table {@code received_package}, prompt 132): sender, authors,
 * signature status and key source, TLP, local recipients, the bundle and the renditions this instance rendered from it
 * (marked with their origin), the stored content files and the acknowledgement. Imported material never merges into
 * local analytic tables (RPT-2).
 */
@Entity
@Table(name = "received_package", indexes = {
    @Index(name = "received_package_package_idx", columnList = "package_id", unique = true),
    @Index(name = "received_package_object_idx", columnList = "object_ref")
})
public class ReceivedPackageEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "package_id", length = 26, nullable = false)
    private String packageId;

    @Column(name = "type", length = 32, nullable = false)
    private String type;

    @Column(name = "object_ref", length = 1100, nullable = false)
    private String objectRef;

    @Column(name = "title", length = 600, nullable = false)
    private String title;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sender", nullable = false)
    private Map<String, Object> sender;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "authors", nullable = false)
    private List<Map<String, Object>> authors = new ArrayList<>();

    @Column(name = "tlp", length = 16, nullable = false)
    private String tlp;

    @Column(name = "signature_status", length = 16, nullable = false)
    private String signatureStatus;

    @Column(name = "key_source", length = 16)
    private String keySource;

    @Column(name = "hashes_verified", nullable = false)
    private boolean hashesVerified;

    @Column(name = "received_via", length = 8, nullable = false)
    private String receivedVia;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "peer_id", length = 48)
    private String peerId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "recipient_ids", nullable = false)
    private List<String> recipientIds = new ArrayList<>();

    @Column(name = "everyone", nullable = false)
    private boolean everyone;

    @Column(name = "message", length = 2000)
    private String message;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @Column(name = "acknowledged_by", length = 26)
    private String acknowledgedBy;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "provenance_chain", nullable = false)
    private List<Map<String, Object>> provenanceChain = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "contents", nullable = false)
    private List<Map<String, Object>> contents = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "manifest", nullable = false)
    private Map<String, Object> manifest;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "bundle")
    private Map<String, Object> bundle;

    @Column(name = "markdown", columnDefinition = "text")
    private String markdown;

    @Column(name = "html", columnDefinition = "text")
    private String html;

    @Column(name = "file_path", length = 512)
    private String filePath;

    /** For JPA. */
    protected ReceivedPackageEntity() {}

    /**
     * A new received package.
     *
     * @param id the local id (ULID)
     * @param packageId the sender's package id
     * @param type the package type
     * @param objectRef {@code remote:{instanceId}/{type}:{id}}
     * @param title the title
     * @param tlp the TLP label
     * @param receivedVia {@code peer} or {@code import}
     * @param receivedAt when
     */
    public ReceivedPackageEntity(String id, String packageId, String type, String objectRef, String title, String tlp,
            String receivedVia, Instant receivedAt) {
        this.id = id;
        this.packageId = packageId;
        this.type = type;
        this.objectRef = objectRef;
        this.title = title;
        this.tlp = tlp;
        this.receivedVia = receivedVia;
        this.receivedAt = receivedAt;
    }

    /** The local id. */
    public String getId() {
        return id;
    }

    /** The sender's package id. */
    public String getPackageId() {
        return packageId;
    }

    /** The package type. */
    public String getType() {
        return type;
    }

    /** The remote reference of the received object. */
    public String getObjectRef() {
        return objectRef;
    }

    /** The title. */
    public String getTitle() {
        return title;
    }

    /** The sender ({@code instanceId}, {@code instanceName}, {@code keyId}, {@code peerId}). */
    public Map<String, Object> getSender() {
        return sender;
    }

    /** Sets the sender. */
    public void setSender(Map<String, Object> sender) {
        this.sender = sender;
    }

    /** The authors and forwarders. */
    public List<Map<String, Object>> getAuthors() {
        return authors;
    }

    /** Sets the authors. */
    public void setAuthors(List<Map<String, Object>> authors) {
        this.authors = authors == null ? new ArrayList<>() : new ArrayList<>(authors);
    }

    /** The TLP label. */
    public String getTlp() {
        return tlp;
    }

    /** {@code valid}, {@code invalid} or {@code unknown-key}. */
    public String getSignatureStatus() {
        return signatureStatus;
    }

    /** {@code peer-jwks} or {@code pinned}. */
    public String getKeySource() {
        return keySource;
    }

    /** Sets the verification outcome. */
    public void setVerification(String signatureStatus, String keySource, boolean hashesVerified) {
        this.signatureStatus = signatureStatus;
        this.keySource = keySource;
        this.hashesVerified = hashesVerified;
    }

    /** True when every content hash matched the manifest. */
    public boolean isHashesVerified() {
        return hashesVerified;
    }

    /** {@code peer} or {@code import}. */
    public String getReceivedVia() {
        return receivedVia;
    }

    /** When it was received. */
    public Instant getReceivedAt() {
        return receivedAt;
    }

    /** The delivering peer, or null for an import. */
    public String getPeerId() {
        return peerId;
    }

    /** Sets the delivering peer. */
    public void setPeerId(String peerId) {
        this.peerId = peerId;
    }

    /** The local recipient principal ids (empty with {@link #isEveryone()}). */
    public List<String> getRecipientIds() {
        return recipientIds;
    }

    /** Sets the local recipients. */
    public void setRecipientIds(List<String> recipientIds) {
        this.recipientIds = recipientIds == null ? new ArrayList<>() : new ArrayList<>(recipientIds);
    }

    /** True when every principal of this instance is a recipient (GREEN or CLEAR, {@code *}). */
    public boolean isEveryone() {
        return everyone;
    }

    /** Sets the wildcard recipient. */
    public void setEveryone(boolean everyone) {
        this.everyone = everyone;
    }

    /** The sender's message, or null. */
    public String getMessage() {
        return message;
    }

    /** Sets the message. */
    public void setMessage(String message) {
        this.message = message;
    }

    /** When a recipient acknowledged it, or null. */
    public Instant getAcknowledgedAt() {
        return acknowledgedAt;
    }

    /** The acknowledging principal, or null. */
    public String getAcknowledgedBy() {
        return acknowledgedBy;
    }

    /** Records the acknowledgement. */
    public void acknowledge(String principalId, Instant at) {
        this.acknowledgedBy = principalId;
        this.acknowledgedAt = at;
    }

    /** The provenance chain, origin first, ending with this instance as importer. */
    public List<Map<String, Object>> getProvenanceChain() {
        return provenanceChain;
    }

    /** Sets the provenance chain. */
    public void setProvenanceChain(List<Map<String, Object>> provenanceChain) {
        this.provenanceChain = provenanceChain == null ? new ArrayList<>() : new ArrayList<>(provenanceChain);
    }

    /** The stored content files ({@code path}, {@code sha256}, {@code size}, {@code mediaId}). */
    public List<Map<String, Object>> getContents() {
        return contents;
    }

    /** Sets the stored content files. */
    public void setContents(List<Map<String, Object>> contents) {
        this.contents = contents == null ? new ArrayList<>() : new ArrayList<>(contents);
    }

    /** The verified manifest. */
    public Map<String, Object> getManifest() {
        return manifest;
    }

    /** Sets the verified manifest. */
    public void setManifest(Map<String, Object> manifest) {
        this.manifest = manifest;
    }

    /** The JSON bundle, or null. */
    public Map<String, Object> getBundle() {
        return bundle;
    }

    /** Sets the JSON bundle. */
    public void setBundle(Map<String, Object> bundle) {
        this.bundle = bundle;
    }

    /** The Markdown rendition (rendered here, marked with its origin). */
    public String getMarkdown() {
        return markdown;
    }

    /** The HTML rendition (rendered here, marked with its origin). */
    public String getHtml() {
        return html;
    }

    /** Sets the renditions. */
    public void setRenditions(String markdown, String html) {
        this.markdown = markdown;
        this.html = html;
    }

    /** The stored ZIP, relative to the exchange directory. */
    public String getFilePath() {
        return filePath;
    }

    /** Sets the stored ZIP. */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
