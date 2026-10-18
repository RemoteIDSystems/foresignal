package ai.foresignal.social;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * One hour of a platform-language denominator (prompt 135, table {@code social_denominator_hourly}): every post seen
 * on the platform's complete streams, matching or not, and whether collection was complete for the whole hour
 * ({@code complete} is null until the hour is closed). {@code id} is {@code {platform}|{language}|{hour epoch
 * seconds}}. Baselines divide by it so platform growth and outages do not read as attention changes.
 */
@Entity
@Table(name = "social_denominator_hourly", indexes = @Index(name = "social_denominator_hourly_idx",
        columnList = "platform, hour"))
public class SocialDenominatorHourlyEntity {

    @Id
    @Column(name = "id", length = 120, nullable = false)
    private String id;

    @Column(name = "platform", length = 32, nullable = false)
    private String platform;

    @Column(name = "language", length = 16, nullable = false)
    private String language;

    @Column(name = "hour", nullable = false)
    private Instant hour;

    @Column(name = "posts", nullable = false)
    private long posts;

    @Column(name = "complete")
    private Boolean complete;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected SocialDenominatorHourlyEntity() {}

    /** The id. */
    public String getId() {
        return id;
    }
}
