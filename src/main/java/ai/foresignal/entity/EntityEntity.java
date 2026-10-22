package ai.foresignal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A canonical physical object (prompt 098, table {@code entity}): one aircraft, vessel, satellite or
 * station seen across sources. Never a person or an organization.
 *
 * <p>{@code id} is a ULID. A provisional entity is created from weak keys only and may later be
 * merged into an anchored entity by the resolver, or by an operator; a merged entity keeps its row
 * with {@code status=MERGED} and {@code merged_into} pointing at the canonical entity.
 */
@Entity
@Table(name = "entity")
public class EntityEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "type", length = 16, nullable = false)
    private String type;

    @Column(name = "display_name", length = 200)
    private String displayName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attributes")
    private Map<String, Object> attributes = new LinkedHashMap<>();

    @Column(name = "provisional", nullable = false)
    private boolean provisional;

    /** {@code ACTIVE} or {@code MERGED}. */
    @Column(name = "status", length = 16, nullable = false)
    private String status = "ACTIVE";

    @Column(name = "merged_into", length = 26)
    private String mergedInto;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes == null ? new LinkedHashMap<>() : attributes;
    }

    public boolean isProvisional() {
        return provisional;
    }

    public void setProvisional(boolean provisional) {
        this.provisional = provisional;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMergedInto() {
        return mergedInto;
    }

    public void setMergedInto(String mergedInto) {
        this.mergedInto = mergedInto;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
