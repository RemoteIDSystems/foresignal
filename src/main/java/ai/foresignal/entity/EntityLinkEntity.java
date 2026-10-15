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
 * A time-bounded link from a source record to an entity, with the key that produced it (prompt 098,
 * table {@code entity_link}). At most one open link per {@code (source_id, external_id)}; the partial
 * unique index is created by {@code EntitySchemaInitializer}. A tombstone closes the record's open
 * link.
 */
@Entity
@Table(name = "entity_link", indexes = {@Index(name = "entity_link_entity_idx", columnList = "entity_id, valid_to")})
public class EntityLinkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "entity_id", length = 26, nullable = false)
    private String entityId;

    @Column(name = "source_id", length = 64, nullable = false)
    private String sourceId;

    @Column(name = "external_id", length = 256, nullable = false)
    private String externalId;

    @Column(name = "key_type", length = 32, nullable = false)
    private String keyType;

    @Column(name = "key_value", length = 64, nullable = false)
    private String keyValue;

    @Column(name = "confidence", nullable = false)
    private float confidence;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "valid_to")
    private Instant validTo;

    @Column(name = "first_seq")
    private Long firstSeq;

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

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
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

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
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

    public Long getFirstSeq() {
        return firstSeq;
    }

    public void setFirstSeq(Long firstSeq) {
        this.firstSeq = firstSeq;
    }
}
