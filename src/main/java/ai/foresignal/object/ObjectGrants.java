package ai.foresignal.object;

import java.util.Optional;

/**
 * The access-list grants of shareable objects (prompt 130), as the object package sees them: implemented by
 * {@code ai.foresignal.collab.AccessResolver}. Workspaces (129) combine their own owner and member roles with these
 * grants ({@link WorkspaceAccess}); the workspace share link checks the TLP label.
 */
public interface ObjectGrants {

    /**
     * The role the access list grants {@code caller} on {@code objectRef}: {@code owner}, {@code editor},
     * {@code commenter} or {@code viewer}, or null when it grants none. Admin scope is not a grant: callers apply
     * their own admin rules.
     *
     * @param objectRef the canonical reference
     * @param caller who asks
     * @return the role, or null
     */
    String grantedRole(String objectRef, ObjectCaller caller);

    /** The TLP label of {@code objectRef} (its wire value), when it is a registered work product. */
    Optional<String> tlp(String objectRef);
}
