package ai.foresignal.object;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A time-aware, provenance-aware edge between two objects (table {@code relation}, docs/Client-Contract.md §4).
 *
 * <p>Projected relations are materialized by {@link RelationProjector} from an owning structure ({@code owner_key},
 * e.g. {@code situation:01J…}) under a stable {@code projection_key}, so re-projecting is idempotent; they are never
 * edited by hand. Operator and agent relations carry {@code provenance.operator} and are retired, never deleted.
 */
@Entity
@Table(name = "relation", indexes = {
        @Index(name = "relation_from_idx", columnList = "from_ref, type"),
        @Index(name = "relation_to_idx", columnList = "to_ref, type"),
        @Index(name = "relation_validity_idx", columnList = "valid_from, valid_to"),
        @Index(name = "relation_owner_idx", columnList = "owner_key"),
        @Index(name = "relation_projection_key", columnList = "projection_key", unique = true)})
public class RelationEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "from_ref", length = 1024, nullable = false)
    private String fromRef;

    @Column(name = "to_ref", length = 1024, nullable = false)
    private String toRef;

    @Column(name = "type", length = 32, nullable = false)
    private String type;

    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;

    @Column(name = "valid_to")
    private Instant validTo;

    @Column(name = "confidence", nullable = false)
    private double confidence;

    @Column(name = "label", length = 16, nullable = false)
    private String label;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "provenance", nullable = false)
    private Map<String, Object> provenance = new LinkedHashMap<>();

    @Column(name = "projected", nullable = false)
    private boolean projected;

    @Column(name = "owner_key", length = 1100)
    private String ownerKey;

    @Column(name = "projection_key", length = 64)
    private String projectionKey;

    @Column(name = "private", nullable = false)
    private boolean privateRelation;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "retired_at")
    private Instant retiredAt;

    /** For JPA. */
    protected RelationEntity() {}

    /**
     * A new relation.
     *
     * @param id its ULID
     * @param fromRef the source object
     * @param toRef the target object
     * @param type the relation type
     * @param validFrom start of validity
     * @param createdAt when it was written
     */
    public RelationEntity(String id, String fromRef, String toRef, String type, Instant validFrom, Instant createdAt) {
        this.id = id;
        this.fromRef = fromRef;
        this.toRef = toRef;
        this.type = type;
        this.validFrom = validFrom;
        this.createdAt = createdAt;
    }

    /** The id. */
    public String getId() {
        return id;
    }

    /** The source object reference. */
    public String getFromRef() {
        return fromRef;
    }

    /** The target object reference. */
    public String getToRef() {
        return toRef;
    }

    /** The relation type. */
    public String getType() {
        return type;
    }

    /** Start of validity. */
    public Instant getValidFrom() {
        return validFrom;
    }

    /** Sets the start of validity. */
    public void setValidFrom(Instant value) {
        this.validFrom = value;
    }

    /** End of validity, or null while open. */
    public Instant getValidTo() {
        return validTo;
    }

    /** Sets the end of validity. */
    public void setValidTo(Instant value) {
        this.validTo = value;
    }

    /** The confidence, 0–1. */
    public double getConfidence() {
        return confidence;
    }

    /** Sets the confidence. */
    public void setConfidence(double value) {
        this.confidence = value;
    }

    /** The label (confirmed, likely, possible, speculative). */
    public String getLabel() {
        return label;
    }

    /** Sets the label. */
    public void setLabel(String value) {
        this.label = value;
    }

    /** The provenance: {@code {component, version, manifestId}} or {@code {operator}}. */
    public Map<String, Object> getProvenance() {
        return provenance;
    }

    /** Sets the provenance. */
    public void setProvenance(Map<String, Object> value) {
        this.provenance = value == null ? new LinkedHashMap<>() : new LinkedHashMap<>(value);
    }

    /** True for a relation materialized by the projector. */
    public boolean isProjected() {
        return projected;
    }

    /** Marks the relation projected. */
    public void setProjected(boolean value) {
        this.projected = value;
    }

    /** The owning structure of a projected relation, or null. */
    public String getOwnerKey() {
        return ownerKey;
    }

    /** Sets the owning structure. */
    public void setOwnerKey(String value) {
        this.ownerKey = value;
    }

    /** The stable key of a projected relation, or null. */
    public String getProjectionKey() {
        return projectionKey;
    }

    /** Sets the projection key. */
    public void setProjectionKey(String value) {
        this.projectionKey = value;
    }

    /** True when the relation touches a private object. */
    public boolean isPrivateRelation() {
        return privateRelation;
    }

    /** Sets the private flag. */
    public void setPrivateRelation(boolean value) {
        this.privateRelation = value;
    }

    /** When it was written. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** When it was retired, or null. */
    public Instant getRetiredAt() {
        return retiredAt;
    }

    /** Retires (or revives, with null) the relation. */
    public void setRetiredAt(Instant value) {
        this.retiredAt = value;
    }
}
