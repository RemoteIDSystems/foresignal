package ai.foresignal.security.user;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Resolves a user's {@link EffectivePermissions} from {@code user_role} and {@code role_permission} (R2-003).
 *
 * <p>Prompt R2-004 caches the result per user, so the permission gate in the token filter does no database read on
 * the hot path. An entry is dropped after the transaction that changed it commits: {@link UserRolesChangedEvent} for
 * one user's roles, {@link RolePermissionsChangedEvent} for a role's permissions (every entry), and
 * {@link UserSessionsRevokedEvent} (suspension, disabling, a new password). Invalidating after commit, not on publish,
 * keeps a concurrent request from re-reading and caching the rows the change is about to replace. {@link #TTL} bounds
 * how long a change made outside those events (a direct SQL edit) can go unseen.
 */
@Service
public class PermissionResolver {

    /** How long an entry lives at most, whatever happens to the rows behind it. */
    public static final Duration TTL = Duration.ofMinutes(5);

    /** The most users held at once. */
    static final int MAXIMUM_SIZE = 10_000;

    private final UserRoleRepository userRoles;
    private final RolePermissionRepository rolePermissions;
    private final Cache<UUID, EffectivePermissions> cache =
            Caffeine.newBuilder().maximumSize(MAXIMUM_SIZE).expireAfterWrite(TTL).build();

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
     * The union of the permissions of a user's roles, from the cache or read now.
     *
     * @param userId the user
     * @return the effective permissions; {@link EffectivePermissions#NONE} for a user without roles
     */
    @Transactional(readOnly = true)
    public EffectivePermissions forUser(UUID userId) {
        return cache.get(userId, this::read);
    }

    /**
     * Drops one user's cached permissions after the role change commits.
     *
     * @param event the change
     */
    @TransactionalEventListener(fallbackExecution = true)
    public void onUserRolesChanged(UserRolesChangedEvent event) {
        invalidate(event.userId());
    }

    /**
     * Drops every cached entry after the role edit commits.
     *
     * @param event the change
     */
    @TransactionalEventListener(fallbackExecution = true)
    public void onRolePermissionsChanged(RolePermissionsChangedEvent event) {
        invalidateAll();
    }

    /**
     * Drops the user's cached permissions after a suspension, disabling or new password commits, so a later sign-in
     * starts from the rows.
     *
     * @param event the revocation
     */
    @TransactionalEventListener(fallbackExecution = true)
    public void onSessionsRevoked(UserSessionsRevokedEvent event) {
        invalidate(event.userId());
    }

    /**
     * Drops one user's entry now.
     *
     * @param userId the user
     */
    public void invalidate(UUID userId) {
        cache.invalidate(userId);
    }

    /** Drops every entry now. */
    public void invalidateAll() {
        cache.invalidateAll();
    }

    private EffectivePermissions read(UUID userId) {
        List<UUID> roles = userRoles.findByUserId(userId).stream().map(UserRoleEntity::getRoleId).toList();
        if (roles.isEmpty()) {
            return EffectivePermissions.NONE;
        }
        return EffectivePermissions.of(rolePermissions.findByRoleIdIn(roles).stream()
                .map(RolePermissionEntity::getPermissionKey).toList());
    }
}
