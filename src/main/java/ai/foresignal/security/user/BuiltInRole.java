package ai.foresignal.security.user;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * The five built-in roles (prompt R2-001), seeded by {@code V14__identity_model.sql} with these stable ids and
 * exactly these permission sets. Built-in roles are protected: they cannot be deleted and their permissions are
 * fixed (enforced by the role management API, R2-006).
 */
public enum BuiltInRole {
    /** Every permission in the catalog. */
    ADMIN("00000000-0000-4000-8000-000000000001", "Admin", "Full control of the instance, its users and roles",
            EnumSet.allOf(Permissions.class)),
    /** All Platform and all Data permissions; not the Identity ones. */
    OPERATOR("00000000-0000-4000-8000-000000000002", "Operator",
            "Runs the platform: collection, collector, shared LLM, channels, storage, network and audit",
            union(Permissions.inGroup(PermissionGroup.PLATFORM), Permissions.inGroup(PermissionGroup.DATA))),
    /** All Self-service and all Data permissions. */
    EDITOR("00000000-0000-4000-8000-000000000003", "Editor",
            "Reads all data and owns personal sources, alerts, channels, LLM, views and workspaces",
            union(Permissions.inGroup(PermissionGroup.SELF), Permissions.inGroup(PermissionGroup.DATA))),
    /** Read-only: {@code data.read}, {@code self.views} and {@code self.preferences}. */
    VIEWER("00000000-0000-4000-8000-000000000004", "Viewer", "Read-only access to collected data",
            EnumSet.of(Permissions.DATA_READ, Permissions.SELF_VIEWS, Permissions.SELF_PREFERENCES)),
    /** {@code audit.read} and {@code data.read}. */
    AUDITOR("00000000-0000-4000-8000-000000000005", "Auditor", "Reads the audit log and collected data",
            EnumSet.of(Permissions.AUDIT_READ, Permissions.DATA_READ));

    private final UUID id;
    private final String roleName;
    private final String description;
    private final Set<Permissions> permissions;

    BuiltInRole(String id, String roleName, String description, Set<Permissions> permissions) {
        this.id = UUID.fromString(id);
        this.roleName = roleName;
        this.description = description;
        this.permissions = Collections.unmodifiableSet(EnumSet.copyOf(permissions));
    }

    /** The stable seeded id. */
    public UUID id() {
        return id;
    }

    /** The seeded, unique role name. */
    public String roleName() {
        return roleName;
    }

    /** The seeded description. */
    public String description() {
        return description;
    }

    /** Exactly the permissions seeded into {@code role_permission} for this role. */
    public Set<Permissions> permissions() {
        return permissions;
    }

    /**
     * The built-in role with a seeded id.
     *
     * @param id a role id
     * @return the built-in role, or empty for a custom role
     */
    public static Optional<BuiltInRole> byId(UUID id) {
        for (BuiltInRole role : values()) {
            if (role.id.equals(id)) {
                return Optional.of(role);
            }
        }
        return Optional.empty();
    }

    private static Set<Permissions> union(Set<Permissions> a, Set<Permissions> b) {
        EnumSet<Permissions> out = EnumSet.noneOf(Permissions.class);
        out.addAll(a);
        out.addAll(b);
        return out;
    }
}
