package ai.foresignal.collab;

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
 * An uploaded document or image (table {@code attachment}, prompt 130). The bytes are stored content-addressed by
 * SHA-256 ({@link AttachmentBlobStore}); several rows may share one file. Extracted text (at most 1 MB) feeds search.
 * Owner, visibility and TLP are in {@code shareable_object}. Files are data, never code (COL-3).
 */
@Entity
@Table(name = "attachment", indexes = @Index(name = "attachment_sha_idx", columnList = "sha256"))
public class AttachmentEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "media_id", length = 80, nullable = false)
    private String mediaId;

    @Column(name = "filename", length = 255, nullable = false)
    private String filename;

    @Column(name = "content_type", length = 100, nullable = false)
    private String contentType;

    @Column(name = "size", nullable = false)
    private long size;

    @Column(name = "sha256", length = 64, nullable = false)
    private String sha256;

    @Column(name = "storage_path", length = 200, nullable = false)
    private String storagePath;

    @Column(name = "text", columnDefinition = "text")
    private String text;

    @Column(name = "text_truncated", nullable = false)
    private boolean textTruncated;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "image_meta")
    private Map<String, Object> imageMeta;

    @Column(name = "uploaded_by", length = 64, nullable = false)
    private String uploadedBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** For JPA. */
    protected AttachmentEntity() {}

    /**
     * A new attachment.
     *
     * @param id its ULID
     * @param sha256 the content hash
     * @param storagePath the content-addressed file, relative to the attachment root
     * @param filename the sanitized file name
     * @param contentType the safe content type it is served with
     * @param size the stored size in bytes
     * @param uploadedBy the uploading principal
     * @param now the upload time
     */
    public AttachmentEntity(String id, String sha256, String storagePath, String filename, String contentType,
            long size, String uploadedBy, Instant now) {
        this.id = id;
        this.sha256 = sha256;
        this.mediaId = "sha256:" + sha256;
        this.storagePath = storagePath;
        this.filename = filename;
        this.contentType = contentType;
        this.size = size;
        this.uploadedBy = uploadedBy;
        this.createdAt = now;
    }

    /** The id. */
    public String getId() {
        return id;
    }

    /** The content-addressed media id ({@code sha256:{hex}}). */
    public String getMediaId() {
        return mediaId;
    }

    /** The file name. */
    public String getFilename() {
        return filename;
    }

    /** The content type it is served with. */
    public String getContentType() {
        return contentType;
    }

    /** The stored size in bytes. */
    public long getSize() {
        return size;
    }

    /** The SHA-256 of the stored bytes. */
    public String getSha256() {
        return sha256;
    }

    /** The content-addressed file, relative to the attachment root. */
    public String getStoragePath() {
        return storagePath;
    }

    /** The extracted text, or null. */
    public String getText() {
        return text;
    }

    /** Sets the extracted text. */
    public void setText(String value, boolean truncated) {
        this.text = value;
        this.textTruncated = truncated;
    }

    /** True when the extracted text was cut at 1 MB. */
    public boolean isTextTruncated() {
        return textTruncated;
    }

    /** {@code width}, {@code height} and {@code exifStripped} of an image, or null. */
    public Map<String, Object> getImageMeta() {
        return imageMeta;
    }

    /** Sets the image metadata. */
    public void setImageMeta(Map<String, Object> value) {
        this.imageMeta = value;
    }

    /** The uploading principal. */
    public String getUploadedBy() {
        return uploadedBy;
    }

    /** When it was uploaded. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** True for an image (served inline). */
    public boolean isImage() {
        return contentType.startsWith("image/");
    }
}
