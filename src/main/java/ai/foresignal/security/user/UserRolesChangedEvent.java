package ai.foresignal.security.user;

import java.util.UUID;

/**
 * A user's role assignments changed (prompt R2-004): published by whatever assigns or removes a role (R2-005), so
 * {@link PermissionResolver} drops the user's cached {@link EffectivePermissions} once the change commits.
 *
 * @param userId the user whose roles changed
 */
public record UserRolesChangedEvent(UUID userId) {}
