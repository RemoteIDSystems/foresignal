package ai.foresignal.object;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Who may do what with a workspace (prompt 129, extended by 130). Members are {@code viewer} or {@code editor}; the
 * owner may do everything. Prompt 130's access lists ({@link ObjectGrants}) add roles through principal, group and
 * instance grants: an access-list {@code owner} or {@code editor} counts as an editor, a {@code commenter} or
 * {@code viewer} as a viewer.
 *
 * <p>Reading needs ownership, membership or a grant (an admin token may read any workspace); a private workspace is
 * also invisible to callers without private access. Context and layout changes need the owner or an editor; metadata
 * changes need the owner, or an editor whose token holds agent scope; deleting needs the owner or an admin.
 */
@Component
public class WorkspaceAccess {

    private final ObjectProvider<ObjectGrants> grants;

    /** Creates the component over the access-list grants, when there are any. */
    public WorkspaceAccess(ObjectProvider<ObjectGrants> grants) {
        this.grants = grants;
    }

    /** The role a workspace itself gives {@code principal}: {@code owner}, a member role, or null. */
    public static String directRole(WorkspaceEntity w, String principal) {
        if (principal == null) {
            return null;
        }
        if (principal.equals(w.getOwner())) {
            return "owner";
        }
        return w.getMembers().get(principal);
    }

    /** The caller's effective role: {@code owner}, {@code editor}, {@code viewer}, or null when it has none. */
    public String role(WorkspaceEntity w, ObjectCaller caller) {
        if (caller == null || caller.anonymous()) {
            return null;
        }
        String direct = directRole(w, caller.principal());
        if ("owner".equals(direct)) {
            return direct;
        }
        ObjectGrants g = grants.getIfAvailable();
        String granted = g == null ? null : g.grantedRole(ObjectTypes.WORKSPACE + ":" + w.getId(), caller);
        String mapped = granted == null ? null
                : ("owner".equals(granted) || "editor".equals(granted)) ? "editor" : "viewer";
        if ("editor".equals(direct) || "editor".equals(mapped)) {
            return "editor";
        }
        return direct != null ? direct : mapped;
    }

    /** True when the caller may read the workspace. */
    public boolean canRead(WorkspaceEntity w, ObjectCaller caller) {
        if (caller == null || caller.anonymous()) {
            return false;
        }
        if (w.isPrivateWorkspace() && !caller.seesPrivate()) {
            return false;
        }
        return caller.admin() || role(w, caller) != null;
    }

    /** True when the caller may change the context or the layout. */
    public boolean canEditContext(WorkspaceEntity w, ObjectCaller caller) {
        String role = role(w, caller);
        return "owner".equals(role) || "editor".equals(role);
    }

    /** True when the caller may change the metadata (name, members, views, investigation, private). */
    public boolean canEditMetadata(WorkspaceEntity w, ObjectCaller caller) {
        String role = role(w, caller);
        return "owner".equals(role) || ("editor".equals(role) && caller.agent());
    }

    /** True when the caller may delete the workspace. */
    public boolean canDelete(WorkspaceEntity w, ObjectCaller caller) {
        return caller.admin() || "owner".equals(role(w, caller));
    }

    /** The TLP label of the workspace, when prompt 130 registered one. */
    public java.util.Optional<String> tlp(WorkspaceEntity w) {
        ObjectGrants g = grants.getIfAvailable();
        return g == null ? java.util.Optional.empty() : g.tlp(ObjectTypes.WORKSPACE + ":" + w.getId());
    }
}
