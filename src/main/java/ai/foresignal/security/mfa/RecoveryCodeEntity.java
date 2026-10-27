package ai.foresignal.security.mfa;

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
 * One single-use MFA recovery code ({@code user_recovery_code}, prompt R2-024). Only the SHA-256 hex of the normalized
 * code is stored ({@link ai.foresignal.security.TokenHasher}); the code itself is shown once, when its set is
 * generated.
 */
@Entity
@Table(name = "user_recovery_code", indexes = @Index(name = "user_recovery_code_user_idx", columnList = "user_id"))
public class RecoveryCodeEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "code_hash", length = 64, nullable = false)
    private String codeHash;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "used_at")
    private Instant usedAt;

    /** For JPA. */
    protected RecoveryCodeEntity() {}

    /**
     * A new, unused code.
     *
     * @param userId the user
     * @param codeHash the SHA-256 hex of the normalized code
     * @param createdAt when its set was generated
     */
    public RecoveryCodeEntity(UUID userId, String codeHash, Instant createdAt) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.codeHash = codeHash;
        this.createdAt = createdAt;
    }

    /** The row id. */
    public UUID getId() {
        return id;
    }

    /** The user. */
    public UUID getUserId() {
        return userId;
    }

    /** The SHA-256 hex of the normalized code. */
    public String getCodeHash() {
        return codeHash;
    }

    /** When the set was generated. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** When the code was used, or null while it is unused. */
    public Instant getUsedAt() {
        return usedAt;
    }

    /**
     * Consumes the code.
     *
     * @param at the use time
     */
    public void use(Instant at) {
        this.usedAt = at;
    }
}
