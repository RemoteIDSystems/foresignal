package ai.foresignal.security.user;

import ai.foresignal.security.Scope;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * A user's effective permissions: the union of its roles' permissions (prompt R2-003, invariant RBAC-1). Resolved on
 * every request by {@link PermissionResolver}, never cached on the session, so a role change applies to the next
 * request.
 *
 * <p>{@link #scope()} maps the set onto the existing scope gate, so a session passes the scope-gated operations
 * without the permission gate of R2-004:
 *
 * <ul>
 *   <li>any {@code identity} or {@code platform} permission other than {@code audit.read} → {@code admin};
 *   <li>otherwise any configuration-owning {@code self} permission ({@code self.sources}, {@code self.alerts},
 *       {@code self.channels}, {@code self.llm}, {@code self.workspaces}) or {@code data.export} → {@code agent};
 *   <li>otherwise → {@code read}.
 * </ul>
 *
 * <p>{@code audit.read} alone does not make an admin (an Auditor reads the log; it does not run the instance), and
 * {@code self.views} and {@code self.preferences} alone do not make an agent (a Viewer stays read-only).
 *
 * @param keys the permission keys, sorted
 */
public record EffectivePermissions(Set<String> keys) {

    /** No permissions. */
    public static final EffectivePermissions NONE = new EffectivePermissions(Set.of());

    /** The {@code self} permissions that let a user own configuration, which make a session {@code agent}. */
    static final Set<String> AGENT_GRANTING = Set.of(Permissions.SELF_SOURCES.key(), Permissions.SELF_ALERTS.key(),
            Permissions.SELF_CHANNELS.key(), Permissions.SELF_LLM.key(), Permissions.SELF_WORKSPACES.key(),
            Permissions.DATA_EXPORT.key());

    /** Copies and sorts the keys. */
    public EffectivePermissions {
        keys = Collections.unmodifiableSet(new TreeSet<>(keys));
    }

    /**
     * The permissions from a collection of keys.
     *
     * @param keys permission keys
     * @return the set
     */
    public static EffectivePermissions of(Collection<String> keys) {
        return new EffectivePermissions(new TreeSet<>(keys));
    }

    /**
     * True when the set holds a permission.
     *
     * @param permission the permission
     * @return true when held
     */
    public boolean has(Permissions permission) {
        return keys.contains(permission.key());
    }

    /** The keys as a sorted list, for responses. */
    public List<String> asList() {
        return List.copyOf(keys);
    }

    /** The coarse scope a session with these permissions carries (see the class comment). */
    public Scope scope() {
        for (String key : keys) {
            Permissions p = Permissions.byKey(key).orElse(null);
            if (p != null && p != Permissions.AUDIT_READ
                    && (p.group() == PermissionGroup.IDENTITY || p.group() == PermissionGroup.PLATFORM)) {
                return Scope.ADMIN;
            }
        }
        for (String key : keys) {
            if (AGENT_GRANTING.contains(key)) {
                return Scope.AGENT;
            }
        }
        return Scope.READ;
    }
}
