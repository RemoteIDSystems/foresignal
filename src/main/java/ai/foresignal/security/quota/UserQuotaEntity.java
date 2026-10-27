package ai.foresignal.security.quota;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * A user's own quota ({@code user_quota}, prompt R2-026). A null column is unset and falls through to the instance
 * default. The row is deleted by the database with the user.
 */
@Entity
@Table(name = "user_quota")
public class UserQuotaEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "api_rate_limit_per_minute")
    private Integer apiRateLimitPerMinute;

    @Column(name = "max_storage_bytes")
    private Long maxStorageBytes;

    @Column(name = "max_alerts")
    private Integer maxAlerts;

    @Column(name = "max_channels")
    private Integer maxChannels;

    @Column(name = "max_saved_views")
    private Integer maxSavedViews;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected UserQuotaEntity() {}

    /**
     * Creates a row.
     *
     * @param userId the user
     * @param quota the limits
     * @param now when it was written
     */
    public UserQuotaEntity(UUID userId, Quota quota, Instant now) {
        this.userId = userId;
        set(quota, now);
    }

    /** The user. */
    public UUID getUserId() {
        return userId;
    }

    /**
     * Replaces every limit.
     *
     * @param quota the limits
     * @param now when it was written
     */
    public void set(Quota quota, Instant now) {
        this.apiRateLimitPerMinute = quota.apiRateLimitPerMinute();
        this.maxStorageBytes = quota.maxStorageBytes();
        this.maxAlerts = quota.maxAlerts();
        this.maxChannels = quota.maxChannels();
        this.maxSavedViews = quota.maxSavedViews();
        this.updatedAt = now;
    }

    /** The limits as a {@link Quota}. */
    public Quota quota() {
        return new Quota(apiRateLimitPerMinute, maxStorageBytes, maxAlerts, maxChannels, maxSavedViews);
    }

    /** When it was last written. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
