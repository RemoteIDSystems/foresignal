package ai.foresignal.security.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * One row of the fixed permission catalog ({@code permission}, prompt R2-001; OpenAPI {@code Permission}). Seeded by
 * {@code V14__identity_model.sql} from the same definitions as {@link Permissions} and read-only at runtime: there is
 * no setter and nothing inserts permissions.
 */
@Entity
@Table(name = "permission")
public class PermissionEntity {

    // "key" is an HQL keyword, so the attribute is named permissionKey.
    @Id
    @Column(name = "key", length = 64, nullable = false)
    private String permissionKey;

    @Column(name = "`group`", length = 16, nullable = false)
    private String group;

    @Column(name = "description", length = 500, nullable = false)
    private String description;

    /** For JPA. */
    protected PermissionEntity() {}

    /** The permission key, for example {@code users.manage}. */
    public String getKey() {
        return permissionKey;
    }

    /** The catalog group's wire value. */
    public String getGroup() {
        return group;
    }

    /** The description. */
    public String getDescription() {
        return description;
    }
}
