package ai.foresignal.object;

/**
 * Who may do what with a workspace (prompt 129). Members are {@code viewer} or {@code editor}; the owner may do
 * everything. Reading needs ownership or membership (an admin token may read any workspace); a private workspace is
 * also invisible to callers without private access. Context and layout changes need the owner or an editor; metadata
 * changes need the owner, or an editor whose token holds agent scope; deleting needs the owner or an admin.
 */
public final class WorkspaceAccess {

    private WorkspaceAccess() {}

    /** The caller's role: {@code owner}, {@code editor}, {@code viewer}, or null when it has none. */
    public static String role(WorkspaceEntity w, String principal) {
        if (principal == null) {
            return null;
        }
        if (principal.equals(w.getOwner())) {
            return "owner";
        }
        return w.getMembers().get(principal);
    }

    /** True when the caller may read the workspace. */
    public static boolean canRead(WorkspaceEntity w, ObjectCaller caller) {
        if (caller.anonymous()) {
            return false;
        }
        if (w.isPrivateWorkspace() && !caller.seesPrivate()) {
            return false;
        }
        return caller.admin() || role(w, caller.principal()) != null;
    }

    /** True when the caller may change the context or the layout. */
    public static boolean canEditContext(WorkspaceEntity w, ObjectCaller caller) {
        String role = role(w, caller.principal());
        return "owner".equals(role) || "editor".equals(role);
    }

    /** True when the caller may change the metadata (name, members, views, investigation, private). */
    public static boolean canEditMetadata(WorkspaceEntity w, ObjectCaller caller) {
        String role = role(w, caller.principal());
        return "owner".equals(role) || ("editor".equals(role) && caller.agent());
    }

    /** True when the caller may delete the workspace. */
    public static boolean canDelete(WorkspaceEntity w, ObjectCaller caller) {
        return caller.admin() || "owner".equals(role(w, caller.principal()));
    }
}
