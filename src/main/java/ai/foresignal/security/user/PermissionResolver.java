package ai.foresignal.security.user;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Resolves a user's {@link EffectivePermissions} from {@code user_role} and {@code role_permission} (R2-003). */
@Service
public class PermissionResolver {

    private final UserRoleRepository userRoles;
    private final RolePermissionRepository rolePermissions;

    /**
     * Creates the resolver.
     *
     * @param userRoles user-to-role links
     * @param rolePermissions role-to-permission links
     */
    public PermissionResolver(UserRoleRepository userRoles, RolePermissionRepository rolePermissions) {
        this.userRoles = userRoles;
        this.rolePermissions = rolePermissions;
    }

    /**
     * The union of the permissions of a user's roles, read now.
     *
     * @param userId the user
     * @return the effective permissions; {@link EffectivePermissions#NONE} for a user without roles
     */
    @Transactional(readOnly = true)
    public EffectivePermissions forUser(UUID userId) {
        List<UUID> roles = userRoles.findByUserId(userId).stream().map(UserRoleEntity::getRoleId).toList();
        if (roles.isEmpty()) {
            return EffectivePermissions.NONE;
        }
        return EffectivePermissions.of(rolePermissions.findByRoleIdIn(roles).stream()
                .map(RolePermissionEntity::getPermissionKey).toList());
    }
}
