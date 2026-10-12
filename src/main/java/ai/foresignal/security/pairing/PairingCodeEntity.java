package ai.foresignal.security.pairing;

import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
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
 * A single-use pairing code ({@code pairing_code}, prompt 023) that a phone or desktop app claims
 * for a {@code read} or {@code agent} token without ever seeing an admin token. Only the SHA-256 of
 * the normalized code is stored.
 */
@Entity
@Table(name = "pairing_code", indexes = @Index(name = "pairing_code_hash_idx", columnList = "code_hash", unique = true))
public class PairingCodeEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "code_hash", length = 64, nullable = false)
    private String codeHash;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "scope", length = 16, nullable = false)
    private String scope;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "restrictions", columnDefinition = "jsonb")
    private TokenRestrictions restrictions;

    @Column(name = "principal_id", length = 26)
    private String principalId;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "claimed_at")
    private Instant claimedAt;

    @Column(name = "claimed_token_id")
    private UUID claimedTokenId;

    @Column(name = "created_by_token_id")
    private UUID createdByTokenId;

    protected PairingCodeEntity() {
        // for JPA
    }

    /**
     * A new, unclaimed code.
     *
     * @param id the id
     * @param codeHash the SHA-256 of the normalized code
     * @param name the name given to the token created on claim
     * @param scope {@code read} or {@code agent}
     * @param restrictions restrictions for that token, or null
     * @param principalId the principal that will own the token, or null for a new one
     * @param createdAt when the code was created
     * @param expiresAt when it stops being claimable
     * @param createdByTokenId the admin token that created it
     */
    public PairingCodeEntity(UUID id, String codeHash, String name, Scope scope, TokenRestrictions restrictions,
            String principalId, Instant createdAt, Instant expiresAt, UUID createdByTokenId) {
        this.id = id;
        this.codeHash = codeHash;
        this.name = name;
        this.scope = scope.wireValue();
        this.restrictions = restrictions == null || restrictions.isEmpty() ? null : restrictions;
        this.principalId = principalId;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.createdByTokenId = createdByTokenId;
    }

    /** The id. */
    public UUID getId() {
        return id;
    }

    /** The SHA-256 of the normalized code. */
    public String getCodeHash() {
        return codeHash;
    }

    /** The name the claimed token gets. */
    public String getName() {
        return name;
    }

    /** The scope the claimed token gets. */
    public Scope getScope() {
        return Scope.fromWireValue(scope);
    }

    /** The restrictions the claimed token gets ({@link TokenRestrictions#NONE} when none). */
    public TokenRestrictions getRestrictions() {
        return TokenRestrictions.orNone(restrictions);
    }

    /** The principal that will own the token, or null. */
    public String getPrincipalId() {
        return principalId;
    }

    /** When the code was created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** When it stops being claimable. */
    public Instant getExpiresAt() {
        return expiresAt;
    }

    /** When it was claimed, or null. */
    public Instant getClaimedAt() {
        return claimedAt;
    }

    /** The token created by the claim, or null. */
    public UUID getClaimedTokenId() {
        return claimedTokenId;
    }

    /** Records the token the claim created. */
    public void setClaimedTokenId(UUID claimedTokenId) {
        this.claimedTokenId = claimedTokenId;
    }

    /** The admin token that created it, or null. */
    public UUID getCreatedByTokenId() {
        return createdByTokenId;
    }

    /** True while unclaimed and before expiry. */
    public boolean isOpen(Instant now) {
        return claimedAt == null && now.isBefore(expiresAt);
    }
}
