package ai.foresignal.security.user;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Persistence of {@link RolePermissionEntity}. */
public interface RolePermissionRepository extends JpaRepository<RolePermissionEntity, RolePermissionEntity.Key> {

    /**
     * The permissions a role grants.
     *
     * @param roleId the role
     * @return its links
     */
    List<RolePermissionEntity> findByRoleId(UUID roleId);

    /**
     * The permissions several roles grant.
     *
     * @param roleIds the roles
     * @return their links
     */
    List<RolePermissionEntity> findByRoleIdIn(Collection<UUID> roleIds);

    /**
     * Removes every permission from a role.
     *
     * @param roleId the role
     */
    void deleteByRoleId(UUID roleId);
}
