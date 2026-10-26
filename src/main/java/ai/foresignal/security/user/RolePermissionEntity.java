package ai.foresignal.security.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * A permission granted by a role ({@code role_permission}, prompt R2-001). An explicit link entity with an
 * {@link IdClass}; the rows are deleted by the database when the role is deleted.
 */
@Entity
@Table(name = "role_permission")
@IdClass(RolePermissionEntity.Key.class)
public class RolePermissionEntity {

    @Id
    @Column(name = "role_id", nullable = false)
    private UUID roleId;

    @Id
    @Column(name = "permission_key", length = 64, nullable = false)
    private String permissionKey;

    /** For JPA. */
    protected RolePermissionEntity() {}

    /**
     * A role granting a permission.
     *
     * @param roleId the role
     * @param permissionKey a key from {@link Permissions}
     */
    public RolePermissionEntity(UUID roleId, String permissionKey) {
        this.roleId = roleId;
        this.permissionKey = permissionKey;
    }

    /** The role. */
    public UUID getRoleId() {
        return roleId;
    }

    /** The permission key. */
    public String getPermissionKey() {
        return permissionKey;
    }

    /** The composite key. */
    public static class Key implements Serializable {

        private static final long serialVersionUID = 1L;

        private UUID roleId;
        private String permissionKey;

        /** For JPA. */
        public Key() {}

        /**
         * A key.
         *
         * @param roleId the role
         * @param permissionKey the permission key
         */
        public Key(UUID roleId, String permissionKey) {
            this.roleId = roleId;
            this.permissionKey = permissionKey;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Key k && Objects.equals(roleId, k.roleId) && Objects.equals(permissionKey, k.permissionKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(roleId, permissionKey);
        }
    }
}
