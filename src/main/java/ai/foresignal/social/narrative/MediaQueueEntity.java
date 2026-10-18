package ai.foresignal.social.narrative;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * A post image waiting to be fingerprinted (prompt 136, table {@code social_media_queue}); {@code id} is the SHA-256 of
 * {@code {postKey}|{url}}. {@link MediaFingerprinter} drains it outside the evidence cursor so fetching never delays
 * clustering. Declares the schema; {@link NarrativeStore} reads and writes it.
 */
@Entity
@Table(name = "social_media_queue", indexes = {
        @Index(name = "social_media_queue_status_idx", columnList = "status, created_at")})
public class MediaQueueEntity {

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @Column(name = "url", length = 2000, nullable = false)
    private String url;

    @Column(name = "source_id", length = 128, nullable = false)
    private String sourceId;

    @Column(name = "external_id", length = 256, nullable = false)
    private String externalId;

    @Column(name = "post_key", length = 256, nullable = false)
    private String postKey;

    @Column(name = "platform", length = 32)
    private String platform;

    @Column(name = "narrative_id", length = 26)
    private String narrativeId;

    @Column(name = "seen_at", nullable = false)
    private Instant seenAt;

    @Column(name = "attempts", nullable = false)
    private int attempts;

    @Column(name = "status", length = 16, nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** For JPA. */
    protected MediaQueueEntity() {}

    /** The id. */
    public String getId() {
        return id;
    }
}
