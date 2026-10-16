package ai.foresignal.share;

import ai.foresignal.security.TokenRestrictions;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A path-bound share link (prompt 112, table {@code share_link}). Only the SHA-256 of the {@code fss_} key is
 * stored; the plaintext is shown once, at creation. The link resolves server-side to its target
 * ({@code record | alert | view | track | briefing-run | export}) with the stored mask, precision and the
 * creating token's read restriction.
 */
@Entity
@Table(name = "share_link", indexes = {
    @Index(name = "share_link_key_hash_idx", columnList = "key_hash", unique = true),
    @Index(name = "share_link_created_by_idx", columnList = "created_by, id")
})
public class ShareLinkEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "key_hash", length = 64, nullable = false)
    private String keyHash;

    @Column(name = "key_prefix", length = 16, nullable = false)
    private String keyPrefix;

    @Column(name = "target_type", length = 16, nullable = false)
    private String targetType;

    @Column(name = "target_ref", length = 600, nullable = false)
    private String targetRef;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "field_mask", columnDefinition = "jsonb")
    private List<String> fieldMask;

    @Column(name = "precision_m")
    private Integer precisionM;

    @Column(name = "masked", nullable = false)
    private boolean masked;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "max_uses")
    private Integer maxUses;

    @Column(name = "uses", nullable = false)
    private int uses;

    @Column(name = "created_by", length = 64)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "restriction_snapshot", columnDefinition = "jsonb")
    private TokenRestrictions restrictionSnapshot;

    /** For JPA. */
    protected ShareLinkEntity() {}

    /**
     * Creates a link.
     *
     * @param id the ULID
     * @param keyHash the SHA-256 (hex) of the plaintext key
     * @param keyPrefix the key's first characters, for listings
     * @param targetType the target type
     * @param targetRef the target reference
     * @param fieldMask the payload allowlist, or null
     * @param precisionM the coordinate precision cap in meters, or null
     * @param masked true when the privacy mask applies
     * @param expiresAt when the link expires
     * @param maxUses the most uses, or null for no limit
     * @param createdBy the creating token's id
     * @param createdAt when it was created
     * @param restrictionSnapshot the creating token's read restriction, or null
     */
    public ShareLinkEntity(String id, String keyHash, String keyPrefix, String targetType, String targetRef,
            List<String> fieldMask, Integer precisionM, boolean masked, Instant expiresAt, Integer maxUses,
            String createdBy, Instant createdAt, TokenRestrictions restrictionSnapshot) {
        this.id = id;
        this.keyHash = keyHash;
        this.keyPrefix = keyPrefix;
        this.targetType = targetType;
        this.targetRef = targetRef;
        this.fieldMask = fieldMask == null ? null : List.copyOf(fieldMask);
        this.precisionM = precisionM;
        this.masked = masked;
        this.expiresAt = expiresAt;
        this.maxUses = maxUses;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.restrictionSnapshot = restrictionSnapshot;
    }

    /** The ULID. */
    public String getId() {
        return id;
    }

    /** The SHA-256 of the key. */
    public String getKeyHash() {
        return keyHash;
    }

    /** The key's first characters. */
    public String getKeyPrefix() {
        return keyPrefix;
    }

    /** The target type. */
    public String getTargetType() {
        return targetType;
    }

    /** The target reference. */
    public String getTargetRef() {
        return targetRef;
    }

    /** The payload allowlist, or null. */
    public List<String> getFieldMask() {
        return fieldMask;
    }

    /** The precision cap in meters, or null. */
    public Integer getPrecisionM() {
        return precisionM;
    }

    /** True when the privacy mask applies. */
    public boolean isMasked() {
        return masked;
    }

    /** When the link expires. */
    public Instant getExpiresAt() {
        return expiresAt;
    }

    /** The most uses, or null. */
    public Integer getMaxUses() {
        return maxUses;
    }

    /** The uses so far. */
    public int getUses() {
        return uses;
    }

    /** The creating token's id. */
    public String getCreatedBy() {
        return createdBy;
    }

    /** When it was created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** When it was revoked, or null. */
    public Instant getRevokedAt() {
        return revokedAt;
    }

    /** When it was last used, or null. */
    public Instant getLastUsedAt() {
        return lastUsedAt;
    }

    /** The creating token's read restriction, or null. */
    public TokenRestrictions getRestrictionSnapshot() {
        return restrictionSnapshot;
    }

    /** Marks the link revoked. */
    public void revoke(Instant at) {
        if (revokedAt == null) {
            revokedAt = at;
        }
    }

    /**
     * The link's status at {@code now}: {@code revoked}, {@code expired}, {@code exhausted} or {@code active}.
     */
    public String status(Instant now) {
        if (revokedAt != null) {
            return "revoked";
        }
        if (!expiresAt.isAfter(now)) {
            return "expired";
        }
        if (maxUses != null && uses >= maxUses) {
            return "exhausted";
        }
        return "active";
    }
}
