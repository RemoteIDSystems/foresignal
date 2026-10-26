package ai.foresignal.security.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A role: a named set of permissions ({@code role}, prompt R2-001; OpenAPI {@code Role}). The five
 * {@link BuiltInRole}s are seeded with {@code built_in = true}; custom roles are created through the role management
 * API (R2-006). The permission set lives in {@link RolePermissionEntity}.
 */
@Entity
@Table(name = "role")
public class RoleEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", length = 100, nullable = false, unique = true)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "built_in", nullable = false)
    private boolean builtIn;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** For JPA. */
    protected RoleEntity() {}

    /**
     * A new role.
     *
     * @param id the role id
     * @param name the unique name
     * @param description the description, or null
     * @param builtIn true only for the seeded built-in roles
     * @param now the creation time
     */
    public RoleEntity(UUID id, String name, String description, boolean builtIn, Instant now) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.builtIn = builtIn;
        this.createdAt = now;
        this.updatedAt = now;
    }

    /** The role id. */
    public UUID getId() {
        return id;
    }

    /** The unique name. */
    public String getName() {
        return name;
    }

    /**
     * Renames the role.
     *
     * @param name the new name
     * @param now the change time
     */
    public void setName(String name, Instant now) {
        this.name = name;
        this.updatedAt = now;
    }

    /** The description, or null. */
    public String getDescription() {
        return description;
    }

    /**
     * Changes the description.
     *
     * @param description the new description, or null
     * @param now the change time
     */
    public void setDescription(String description, Instant now) {
        this.description = description;
        this.updatedAt = now;
    }

    /** True for a seeded built-in role, which cannot be deleted and whose permissions are fixed. */
    public boolean isBuiltIn() {
        return builtIn;
    }

    /** When the role was created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** When the role last changed. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Marks the role changed, for a change to its permission set.
     *
     * @param now the change time
     */
    public void touch(Instant now) {
        this.updatedAt = now;
    }
}
