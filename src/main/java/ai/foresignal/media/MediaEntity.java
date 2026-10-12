package ai.foresignal.media;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One stored image (table {@code media}, Weather-Imagery-Design §2.3, prompt 043), plus
 * {@code variant_of} and {@code variant_width}, which link a thumbnail to its original.
 *
 * <p>This entity exists so Hibernate owns the table's DDL; the media code reads and writes it with
 * native SQL ({@link MediaQueries}), because retention deletes in bulk with {@code RETURNING}.
 *
 * <p>Media is content-addressed: every store inserts a new row, and rows with the same
 * {@code sha256} share the file {@code media/{ab}/{sha256}.{ext}} ({@code path}, relative to
 * {@code media/}). {@code expires_at} null means the row follows its owning record's retention.
 */
@Entity
@Table(name = "media", indexes = {
        @Index(name = "media_sha256_idx", columnList = "sha256"),
        @Index(name = "media_record_idx", columnList = "record_source_id, record_external_id"),
        @Index(name = "media_variant_of_idx", columnList = "variant_of"),
        @Index(name = "media_expires_at_idx", columnList = "expires_at")})
public class MediaEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "sha256", columnDefinition = "char(64)", nullable = false)
    private String sha256;

    @Column(name = "content_type", length = 32, nullable = false)
    private String contentType;

    @Column(name = "width", nullable = false)
    private int width;

    @Column(name = "height", nullable = false)
    private int height;

    @Column(name = "bytes", nullable = false)
    private long bytes;

    @Column(name = "path", length = 128, nullable = false)
    private String path;

    @Column(name = "role", length = 16, nullable = false)
    private String role;

    @Column(name = "source_id", length = 64)
    private String sourceId;

    @Column(name = "record_source_id", length = 64)
    private String recordSourceId;

    @Column(name = "record_external_id", length = 256)
    private String recordExternalId;

    @Column(name = "licence", length = 512)
    private String licence;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "variant_of", length = 26)
    private String variantOf;

    @Column(name = "variant_width")
    private Integer variantWidth;

    /** For JPA. */
    protected MediaEntity() {}

    /** The media id (ULID). */
    public String getId() {
        return id;
    }

    /** The SHA-256 of the file. */
    public String getSha256() {
        return sha256;
    }

    /** The content type. */
    public String getContentType() {
        return contentType;
    }

    /** The width, pixels. */
    public int getWidth() {
        return width;
    }

    /** The height, pixels. */
    public int getHeight() {
        return height;
    }

    /** The file size. */
    public long getBytes() {
        return bytes;
    }

    /** The path relative to {@code media/}. */
    public String getPath() {
        return path;
    }

    /** The role. */
    public String getRole() {
        return role;
    }

    /** The producing source, or null. */
    public String getSourceId() {
        return sourceId;
    }

    /** The owning record's source, or null. */
    public String getRecordSourceId() {
        return recordSourceId;
    }

    /** The owning record's external id, or null. */
    public String getRecordExternalId() {
        return recordExternalId;
    }

    /** The license (column {@code licence}), or null. */
    public String getLicence() {
        return licence;
    }

    /** When stored. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** When it expires, or null to follow the owning record. */
    public Instant getExpiresAt() {
        return expiresAt;
    }

    /** The original of a thumbnail, or null. */
    public String getVariantOf() {
        return variantOf;
    }

    /** The long-edge width of a thumbnail, or null. */
    public Integer getVariantWidth() {
        return variantWidth;
    }
}
