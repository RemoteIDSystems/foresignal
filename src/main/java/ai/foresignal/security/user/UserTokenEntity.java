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
 * A single-use, expiring account activation or password reset token ({@code user_token}, prompt R2-001).
 *
 * <p>Only the SHA-256 of the token is stored, in the same 64-character hex form as {@code api_token.token_hash}
 * ({@code ai.foresignal.security.TokenHasher}); {@code token_prefix} keeps its first eight characters for lookup and
 * display. The plaintext is never stored. Issuing and consuming tokens is R2-002.
 */
@Entity
@Table(name = "user_token", indexes = {
        @Index(name = "user_token_hash_idx", columnList = "token_hash"),
        @Index(name = "user_token_user_kind_idx", columnList = "user_id, kind")})
public class UserTokenEntity {

    /** Characters of the token kept in {@code token_prefix}. */
    public static final int PREFIX_LENGTH = 8;

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "kind", length = 16, nullable = false)
    private String kind;

    @Column(name = "token_hash", length = 64, nullable = false)
    private String tokenHash;

    @Column(name = "token_prefix", length = PREFIX_LENGTH, nullable = false)
    private String tokenPrefix;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "consumed_at")
    private Instant consumedAt;

    @Column(name = "created_by")
    private UUID createdBy;

    /** For JPA. */
    protected UserTokenEntity() {}

    /**
     * A new, unconsumed token.
     *
     * @param id the token id
     * @param userId the user it acts on
     * @param kind activation or reset
     * @param tokenHash the SHA-256 hex of the token, never the plaintext
     * @param tokenPrefix the first {@value #PREFIX_LENGTH} characters of the token
     * @param createdBy the admin who issued it, or null when the user asked for it
     * @param now the issue time
     * @param expiresAt when it stops being accepted
     */
    public UserTokenEntity(UUID id, UUID userId, UserTokenKind kind, String tokenHash, String tokenPrefix,
            UUID createdBy, Instant now, Instant expiresAt) {
        this.id = id;
        this.userId = userId;
        this.kind = kind.wireValue();
        this.tokenHash = tokenHash;
        this.tokenPrefix = tokenPrefix;
        this.createdBy = createdBy;
        this.createdAt = now;
        this.expiresAt = expiresAt;
    }

    /** The token id. */
    public UUID getId() {
        return id;
    }

    /** The user the token acts on. */
    public UUID getUserId() {
        return userId;
    }

    /** Activation or reset. */
    public UserTokenKind getKind() {
        return UserTokenKind.fromWire(kind);
    }

    /** The SHA-256 hex of the token. */
    public String getTokenHash() {
        return tokenHash;
    }

    /** The first characters of the token. */
    public String getTokenPrefix() {
        return tokenPrefix;
    }

    /** When the token was issued. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** When the token stops being accepted. */
    public Instant getExpiresAt() {
        return expiresAt;
    }

    /** When the token was used, or null while unused. */
    public Instant getConsumedAt() {
        return consumedAt;
    }

    /** The admin who issued the token, or null. */
    public UUID getCreatedBy() {
        return createdBy;
    }

    /**
     * True when the token may still be consumed: unused and not yet expired.
     *
     * @param now the current time
     * @return true when usable
     */
    public boolean isUsable(Instant now) {
        return consumedAt == null && now.isBefore(expiresAt);
    }

    /**
     * Marks the token used. A token is single-use; consuming twice is refused.
     *
     * @param now the use time
     * @throws IllegalStateException when the token was already consumed
     */
    public void consume(Instant now) {
        if (consumedAt != null) {
            throw new IllegalStateException("user token already consumed");
        }
        this.consumedAt = now;
    }
}
