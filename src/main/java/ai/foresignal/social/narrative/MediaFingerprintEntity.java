package ai.foresignal.social.narrative;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A PDQ fingerprint of an image or video thumbnail seen in posts (prompt 136, table {@code media_fingerprint}): the hash,
 * its quality, when and in which post it was first seen, how often and where it was used, the narratives it appears in,
 * the stored image when {@code social.keepMedia} is on, and its {@code media-reuse} findings. Declares the schema;
 * {@link NarrativeStore} reads and writes it.
 */
@Entity
@Table(name = "media_fingerprint", indexes = {
        @Index(name = "media_fingerprint_first_idx", columnList = "first_seen_at"),
        @Index(name = "media_fingerprint_pdq_uq", columnList = "pdq", unique = true)})
public class MediaFingerprintEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "pdq", length = 64, nullable = false)
    private String pdq;

    @Column(name = "quality", nullable = false)
    private int quality;

    @Column(name = "first_seen_at", nullable = false)
    private Instant firstSeenAt;

    @Column(name = "first_post_ref", length = 500, nullable = false)
    private String firstPostRef;

    @Column(name = "first_post_key", length = 256, nullable = false)
    private String firstPostKey;

    @Column(name = "use_count", nullable = false)
    private int useCount;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "platforms", columnDefinition = "text[]")
    private String[] platforms;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "narrative_ids", columnDefinition = "text[]")
    private String[] narrativeIds;

    @Column(name = "media_id", length = 26)
    private String mediaId;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "reuse_finding_ids", columnDefinition = "text[]")
    private String[] reuseFindingIds;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected MediaFingerprintEntity() {}

    /** The id. */
    public String getId() {
        return id;
    }
}
