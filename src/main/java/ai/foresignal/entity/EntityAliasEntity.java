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
 * A display name, former name or operator label of an entity (prompt 098, table {@code entity_alias}).
 * A {@code lower(alias) text_pattern_ops} index serves prefix search; a hyphenated registration is
 * also kept here as a {@code NAME} alias.
 */
@Entity
@Table(name = "entity_alias", indexes = {@Index(name = "entity_alias_entity_idx", columnList = "entity_id")})
public class EntityAliasEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "entity_id", length = 26, nullable = false)
    private String entityId;

    @Column(name = "alias", length = 200, nullable = false)
    private String alias;

    /** {@code NAME}, {@code FORMER_NAME} or {@code OPERATOR_LABEL}. */
    @Column(name = "alias_type", length = 16, nullable = false)
    private String aliasType;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "valid_to")
    private Instant validTo;

    @Column(name = "origin", length = 16, nullable = false)
    private String origin;

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

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAliasType() {
        return aliasType;
    }

    public void setAliasType(String aliasType) {
        this.aliasType = aliasType;
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

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }
}
