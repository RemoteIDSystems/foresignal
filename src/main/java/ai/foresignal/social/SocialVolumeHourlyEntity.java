package ai.foresignal.social;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * One hour of social mention volume (prompt 135, table {@code social_volume_hourly}) of one scope (topic, place or
 * entity) on one platform in one language bucket and collection type: posts, distinct authors, reposts and
 * engagement. {@code id} is {@code {scope}|{platform}|{language}|{hour epoch seconds}|{collectionType}}. Written by
 * SQL ({@link SocialVolumeStore}, additive deltas); the {@code social:} series of 119 read these rows. Kept like other
 * analytic series (10 years).
 */
@Entity
@Table(name = "social_volume_hourly", indexes = @Index(name = "social_volume_hourly_scope_idx",
        columnList = "scope_type, scope_id, platform, hour"))
public class SocialVolumeHourlyEntity {

    @Id
    @Column(name = "id", length = 300, nullable = false)
    private String id;

    @Column(name = "scope_type", length = 16, nullable = false)
    private String scopeType;

    @Column(name = "scope_id", length = 64, nullable = false)
    private String scopeId;

    @Column(name = "platform", length = 32, nullable = false)
    private String platform;

    @Column(name = "language", length = 16, nullable = false)
    private String language;

    @Column(name = "hour", nullable = false)
    private Instant hour;

    @Column(name = "collection_type", length = 16, nullable = false)
    private String collectionType;

    @Column(name = "posts", nullable = false)
    private long posts;

    @Column(name = "unique_authors", nullable = false)
    private long uniqueAuthors;

    @Column(name = "reposts", nullable = false)
    private long reposts;

    @Column(name = "engagement", nullable = false)
    private long engagement;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected SocialVolumeHourlyEntity() {}

    /** The id. */
    public String getId() {
        return id;
    }
}
