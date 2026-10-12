package ai.foresignal.inbound.device;

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
 * A device-scoped ingest key ({@code ingest_device}, prompt 039): an {@code eid_} key that
 * authenticates only for {@link #getSourceId()} and forces every record it pushes to
 * {@link #getExternalId()}.
 *
 * <p>Only the key's SHA-256 and its 8-character prefix are stored; the key itself is shown once, at
 * creation and rotation. A revoked row keeps its history (the device's records stay until retention
 * or a purge removes them).
 */
@Entity
@Table(name = "ingest_device", indexes = {
        @Index(name = "ingest_device_key_hash_idx", columnList = "key_hash", unique = true),
        @Index(name = "ingest_device_source_idx", columnList = "source_id")})
public class IngestDeviceEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "source_id", length = 64, nullable = false)
    private String sourceId;

    @Column(name = "external_id", length = 256, nullable = false)
    private String externalId;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "key_hash", length = 64, nullable = false)
    private String keyHash;

    @Column(name = "key_prefix", length = 8, nullable = false)
    private String keyPrefix;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "last_seen_at")
    private Instant lastSeenAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "revoked_at")
    private Instant revokedAt;

    /** For JPA. */
    protected IngestDeviceEntity() {}

    /**
     * A new device.
     *
     * @param id the device id
     * @param sourceId the custom source it may push into
     * @param externalId the entity it is bound to
     * @param name its display name
     * @param createdAt when it was created
     */
    public IngestDeviceEntity(UUID id, String sourceId, String externalId, String name, Instant createdAt) {
        this.id = id;
        this.sourceId = sourceId;
        this.externalId = externalId;
        this.name = name;
        this.createdAt = createdAt;
    }

    /** The device id. */
    public UUID getId() {
        return id;
    }

    /** The source it may push into. */
    public String getSourceId() {
        return sourceId;
    }

    /** The entity everything it pushes is forced to. */
    public String getExternalId() {
        return externalId;
    }

    /** Its display name. */
    public String getName() {
        return name;
    }

    /** The key's SHA-256 (hex). */
    public String getKeyHash() {
        return keyHash;
    }

    /** The key's first 8 characters ({@code eid_…}). */
    public String getKeyPrefix() {
        return keyPrefix;
    }

    /** Replaces the key; the old one stops working at once. */
    public void setKey(String hash, String prefix) {
        this.keyHash = hash;
        this.keyPrefix = prefix;
    }

    /** When it was created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** When it last pushed (at most once a minute precision), or null. */
    public Instant getLastSeenAt() {
        return lastSeenAt;
    }

    /** Sets {@link #getLastSeenAt()}. */
    public void setLastSeenAt(Instant lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    /** When its key was revoked, or null. */
    public Instant getRevokedAt() {
        return revokedAt;
    }

    /** Sets {@link #getRevokedAt()}. */
    public void setRevokedAt(Instant revokedAt) {
        this.revokedAt = revokedAt;
    }

    /** True when the key still authenticates. */
    public boolean isActive() {
        return revokedAt == null;
    }
}
