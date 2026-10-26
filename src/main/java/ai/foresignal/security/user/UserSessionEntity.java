package ai.foresignal.security.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A login session ({@code user_session}, prompt R2-003; OpenAPI {@code Session}).
 *
 * <p>Only the SHA-256 hex of the session bearer and its first eight characters are stored. A session is live while
 * it is not revoked, before its absolute {@code expires_at} and before its sliding {@code idle_expires_at}.
 */
@Entity
@Table(name = "user_session", indexes = {
        @Index(name = "user_session_hash_idx", columnList = "token_hash", unique = true),
        @Index(name = "user_session_user_revoked_idx", columnList = "user_id, revoked_at")})
public class UserSessionEntity {

    /** Longest stored {@code User-Agent}; longer values are cut. */
    public static final int USER_AGENT_LENGTH = 512;

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "token_hash", length = 64, nullable = false)
    private String tokenHash;

    @Column(name = "token_prefix", length = 8, nullable = false)
    private String tokenPrefix;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "last_seen_at")
    private Instant lastSeenAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "idle_expires_at", nullable = false)
    private Instant idleExpiresAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "user_agent", length = USER_AGENT_LENGTH)
    private String userAgent;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    /** For JPA. */
    protected UserSessionEntity() {}

    /**
     * A new session.
     *
     * @param id the session id
     * @param userId the user
     * @param tokenHash the SHA-256 hex of the bearer
     * @param tokenPrefix the bearer's first eight characters
     * @param now the creation time, also the first {@code last_seen_at}
     * @param expiresAt the absolute expiry
     * @param idleExpiresAt the first idle expiry
     * @param userAgent the client's {@code User-Agent}, cut to {@value #USER_AGENT_LENGTH}, or null
     * @param ipAddress the client address, or null
     */
    public UserSessionEntity(UUID id, UUID userId, String tokenHash, String tokenPrefix, Instant now,
            Instant expiresAt, Instant idleExpiresAt, String userAgent, String ipAddress) {
        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.tokenPrefix = tokenPrefix;
        this.createdAt = now;
        this.lastSeenAt = now;
        this.expiresAt = expiresAt;
        this.idleExpiresAt = idleExpiresAt;
        this.userAgent = userAgent == null || userAgent.length() <= USER_AGENT_LENGTH ? userAgent
                : userAgent.substring(0, USER_AGENT_LENGTH);
        this.ipAddress = ipAddress == null || ipAddress.length() <= 64 ? ipAddress : ipAddress.substring(0, 64);
    }

    /** The session id. */
    public UUID getId() {
        return id;
    }

    /** The user. */
    public UUID getUserId() {
        return userId;
    }

    /** The SHA-256 hex of the bearer. */
    public String getTokenHash() {
        return tokenHash;
    }

    /** The bearer's first eight characters. */
    public String getTokenPrefix() {
        return tokenPrefix;
    }

    /** When the session was created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** When the session was last used (updated at most once a minute). */
    public Instant getLastSeenAt() {
        return lastSeenAt;
    }

    /** The absolute expiry. */
    public Instant getExpiresAt() {
        return expiresAt;
    }

    /** The sliding idle expiry. */
    public Instant getIdleExpiresAt() {
        return idleExpiresAt;
    }

    /** When the session was revoked, or null. */
    public Instant getRevokedAt() {
        return revokedAt;
    }

    /** The client's {@code User-Agent}, or null. */
    public String getUserAgent() {
        return userAgent;
    }

    /** The client address at creation, or null. */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * True while the session may be used: not revoked, before both expiries.
     *
     * @param now the current time
     * @return true when live
     */
    public boolean isLive(Instant now) {
        return revokedAt == null && now.isBefore(expiresAt) && now.isBefore(idleExpiresAt);
    }

    /**
     * Records a use: moves {@code last_seen_at} and the idle expiry (never past the absolute expiry).
     *
     * @param now the use time
     * @param idleExpiresAt the new idle expiry
     */
    public void touch(Instant now, Instant idleExpiresAt) {
        this.lastSeenAt = now;
        this.idleExpiresAt = idleExpiresAt.isAfter(expiresAt) ? expiresAt : idleExpiresAt;
    }

    /**
     * Revokes the session; a second revocation keeps the first time.
     *
     * @param now the revocation time
     */
    public void revoke(Instant now) {
        if (revokedAt == null) {
            revokedAt = now;
        }
    }
}
