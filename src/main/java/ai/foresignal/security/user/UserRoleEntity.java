package ai.foresignal.security.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * A role held by a user ({@code user_role}, prompt R2-001). An explicit link entity with an {@link IdClass}, the way
 * this codebase models its other composite-key tables; the rows are deleted by the database when either the user or
 * the role is deleted. A user's effective permissions are the union of its roles' permissions.
 */
@Entity
@Table(name = "user_role", indexes = @Index(name = "user_role_role_idx", columnList = "role_id"))
@IdClass(UserRoleEntity.Key.class)
public class UserRoleEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Id
    @Column(name = "role_id", nullable = false)
    private UUID roleId;

    /** For JPA. */
    protected UserRoleEntity() {}

    /**
     * A user holding a role.
     *
     * @param userId the user
     * @param roleId the role
     */
    public UserRoleEntity(UUID userId, UUID roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }

    /** The user. */
    public UUID getUserId() {
        return userId;
    }

    /** The role. */
    public UUID getRoleId() {
        return roleId;
    }

    /** The composite key. */
    public static class Key implements Serializable {

        private static final long serialVersionUID = 1L;

        private UUID userId;
        private UUID roleId;

        /** For JPA. */
        public Key() {}

        /**
         * A key.
         *
         * @param userId the user
         * @param roleId the role
         */
        public Key(UUID userId, UUID roleId) {
            this.userId = userId;
            this.roleId = roleId;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Key k && Objects.equals(userId, k.userId) && Objects.equals(roleId, k.roleId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, roleId);
        }
    }
}
