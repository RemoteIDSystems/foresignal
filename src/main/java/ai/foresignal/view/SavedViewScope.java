package ai.foresignal.view;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.user.AuthenticatedSession;
import ai.foresignal.security.user.Permissions;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;

/**
 * Which saved views a caller may see and manage (prompt R2-012), on the pattern of the LLM provider scope of R2-011.
 *
 * <p>A view is <b>personal</b> when {@code owner_principal_id} names a user's principal and <b>shared</b> when it is
 * null: every view made before R2-012, every view an API token makes, and every view a briefing, chronicle, share link
 * or workspace could name before views had owners.
 *
 * <ul>
 *   <li>A session sees its own views and the shared ones, and manages its own; what it creates is its own.
 *   <li>A session holding {@code users.manage} (an admin, the definition the last-admin guard uses) sees and manages
 *       every view, other users' and the shared ones included.
 *   <li>An API token (the machine path) sees and manages every view and creates shared ones, as before R2-012.
 *   <li>Anyone else (anonymous under {@code security.openReads}, a feed or tile key) sees the shared views only.
 * </ul>
 *
 * @param principalId the session's principal, or null when the caller is not a session
 * @param all true when the caller sees and manages every view
 */
public record SavedViewScope(String principalId, boolean all) {

    /** An API token: every view; new views are shared. */
    public static final SavedViewScope MACHINE = new SavedViewScope(null, true);

    /** Anonymous callers and path keys: shared views only, nothing managed. */
    public static final SavedViewScope SHARED_ONLY = new SavedViewScope(null, false);

    /**
     * The scope of a session.
     *
     * @param session the session
     * @return its scope
     */
    public static SavedViewScope of(AuthenticatedSession session) {
        return new SavedViewScope(session.principalId(),
                session.permissions().keys().contains(Permissions.USERS_MANAGE.key()));
    }

    /**
     * The scope of a request: its session, else the machine scope for an API token, else shared only.
     *
     * @param request the request, or null for an internal caller (the machine scope)
     * @return its scope
     */
    public static SavedViewScope of(HttpServletRequest request) {
        if (request == null) {
            return MACHINE;
        }
        AuthenticatedSession session = AuthenticatedSession.of(request).orElse(null);
        if (session != null) {
            return of(session);
        }
        return CurrentToken.of(request).isPresent() ? MACHINE : SHARED_ONLY;
    }

    /**
     * True when a view owned by {@code owner} is visible: the shared ones and the caller's own, or all.
     *
     * @param owner the view's owner principal, or null for a shared view
     * @return true when visible
     */
    public boolean sees(String owner) {
        return all || owner == null || owner.equals(principalId);
    }

    /**
     * True when the caller may replace or delete a view owned by {@code owner}: its own, or every view for
     * {@link #all()}.
     *
     * @param owner the view's owner principal, or null for a shared view
     * @return true when allowed
     */
    public boolean manages(String owner) {
        return all || (principalId != null && principalId.equals(owner));
    }

    /**
     * The owner of a view this caller creates: its principal for a session, null (shared) otherwise.
     *
     * @return the owner principal, or null
     */
    public String ownerForNew() {
        return principalId;
    }

    /**
     * The refusal of a shared view the caller can see but not change.
     *
     * @return the exception ({@code 403 PERMISSION_DENIED}, {@code requiredPermission} {@code users.manage})
     */
    public static ForesignalException sharedDenied() {
        return new ForesignalException(HttpStatus.FORBIDDEN, ErrorCodes.PERMISSION_DENIED,
                "Shared saved views are changed by an admin (the users.manage permission), which none of your roles"
                        + " grants. Save a copy under a new slug to make it your own.")
                .withProperty("requiredPermission", Permissions.USERS_MANAGE.key());
    }
}
