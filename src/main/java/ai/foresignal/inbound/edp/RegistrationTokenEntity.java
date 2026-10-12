package ai.foresignal.inbound.edp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * An EDP registration token ({@code edp_registration_token}, prompt 024): {@code fsx_} + base64url
 * of 32 random bytes, stored as its SHA-256. A provider uses it once (by default) to self-register
 * with {@code POST /v1/external/register}.
 */
@Entity
@Table(name = "edp_registration_token", indexes = @Index(name = "edp_registration_token_hash_idx",
        columnList = "token_hash", unique = true))
public class RegistrationTokenEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "token_hash", length = 64, nullable = false)
    private String tokenHash;

    @Column(name = "prefix", length = 8, nullable = false)
    private String prefix;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "max_uses", nullable = false)
    private int maxUses = 1;

    @Column(name = "uses", nullable = false)
    private int uses;

    @Column(name = "source_id_prefix", length = 48)
    private String sourceIdPrefix;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** For JPA. */
    protected RegistrationTokenEntity() {}

    /** A new token row. */
    public RegistrationTokenEntity(String id, String name, String tokenHash, String prefix, Instant expiresAt,
            int maxUses, String sourceIdPrefix, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.tokenHash = tokenHash;
        this.prefix = prefix;
        this.expiresAt = expiresAt;
        this.maxUses = maxUses;
        this.sourceIdPrefix = sourceIdPrefix;
        this.createdAt = createdAt;
    }

    /** The ULID. */
    public String getId() {
        return id;
    }

    /** The operator's name for it. */
    public String getName() {
        return name;
    }

    /** The token's SHA-256. */
    public String getTokenHash() {
        return tokenHash;
    }

    /** The first 8 characters. */
    public String getPrefix() {
        return prefix;
    }

    /** When it expires, or null for never. */
    public Instant getExpiresAt() {
        return expiresAt;
    }

    /** How many registrations it allows. */
    public int getMaxUses() {
        return maxUses;
    }

    /** How many it has made. */
    public int getUses() {
        return uses;
    }

    /** Counts one use. */
    public void use() {
        uses++;
    }

    /** The provider id prefix it is limited to, or null. */
    public String getSourceIdPrefix() {
        return sourceIdPrefix;
    }

    /** When it was created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** True when it can still register a provider at {@code now}. */
    public boolean usable(Instant now) {
        return uses < maxUses && (expiresAt == null || expiresAt.isAfter(now));
    }
}
