package ai.foresignal.social.narrative;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * One use of a media fingerprint in a post (prompt 136, table {@code media_use}); {@code id} is
 * {@code {fingerprintId}|{postKey}}. Declares the schema; {@link NarrativeStore} writes it.
 */
@Entity
@Table(name = "media_use", indexes = {
        @Index(name = "media_use_fingerprint_idx", columnList = "fingerprint_id, seen_at"),
        @Index(name = "media_use_post_idx", columnList = "post_key")})
public class MediaUseEntity {

    @Id
    @Column(name = "id", length = 300, nullable = false)
    private String id;

    @Column(name = "fingerprint_id", length = 26, nullable = false)
    private String fingerprintId;

    @Column(name = "post_ref", length = 500, nullable = false)
    private String postRef;

    @Column(name = "post_key", length = 256, nullable = false)
    private String postKey;

    @Column(name = "url", length = 2000)
    private String url;

    @Column(name = "platform", length = 32)
    private String platform;

    @Column(name = "seen_at", nullable = false)
    private Instant seenAt;

    /** For JPA. */
    protected MediaUseEntity() {}

    /** The id. */
    public String getId() {
        return id;
    }
}
