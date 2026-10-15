package ai.foresignal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One deterministic key held by an entity for a validity interval (prompt 098, table
 * {@code entity_key}). Rows are never deleted, only closed ({@code valid_to}) or retracted; a partial
 * unique index on {@code (key_type, key_value) WHERE valid_to IS NULL} keeps at most one open holder
 * per key, and is created by {@code EntitySchemaInitializer}.
 */
@Entity
@Table(name = "entity_key", indexes = {@Index(name = "entity_key_entity_idx", columnList = "entity_id")})
public class EntityKeyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "entity_id", length = 26, nullable = false)
    private String entityId;

    @Column(name = "key_type", length = 32, nullable = false)
    private String keyType;

    @Column(name = "key_value", length = 64, nullable = false)
    private String keyValue;

    /** {@code ANCHOR} or {@code WEAK}. */
    @Column(name = "role", length = 8, nullable = false)
    private String role;

    @Column(name = "confidence", nullable = false)
    private float confidence;

    /** {@code REGISTRY}, {@code CO_ASSERTED}, {@code OBSERVED} or {@code OPERATOR}. */
    @Column(name = "origin", length = 16, nullable = false)
    private String origin;

    @Column(name = "origin_ref", length = 320)
    private String originRef;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "valid_to")
    private Instant validTo;

    @Column(name = "retracted", nullable = false)
    private boolean retracted;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getKeyType() {
        return keyType;
    }

    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }

    public String getKeyValue() {
        return keyValue;
    }

    public void setKeyValue(String keyValue) {
        this.keyValue = keyValue;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getOriginRef() {
        return originRef;
    }

    public void setOriginRef(String originRef) {
        this.originRef = originRef;
    }

    public Instant getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Instant validFrom) {
        this.validFrom = validFrom;
    }

    public Instant getValidTo() {
        return validTo;
    }

    public void setValidTo(Instant validTo) {
        this.validTo = validTo;
    }

    public boolean isRetracted() {
        return retracted;
    }

    public void setRetracted(boolean retracted) {
        this.retracted = retracted;
    }
}
