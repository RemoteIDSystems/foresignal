package ai.foresignal.security.user;

import java.util.UUID;

/**
 * A role's permission set changed, or the role was deleted (prompt R2-004): published by the role editor (R2-006), so
 * {@link PermissionResolver} drops every cached {@link EffectivePermissions} once the change commits. Every entry,
 * because finding the role's holders would cost the read the cache exists to avoid, and role edits are rare.
 *
 * @param roleId the role that changed
 */
public record RolePermissionsChangedEvent(UUID roleId) {}
