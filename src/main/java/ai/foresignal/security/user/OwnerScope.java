package ai.foresignal.security.user;

import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.restrict.AlertAudience;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Whose user-owned configuration a caller may see and manage (prompt R2-008; the same rule serves the channels, LLM
 * providers and views that R2-009…R2-012 give an owner).
 *
 * <ul>
 *   <li>A signed-in user's session sees and manages what its user owns; with {@code users.manage} (an admin, the same
 *       definition as the last-admin guard) it sees and manages everything, shared rows included. What it creates is
 *       its own.
 *   <li>An API token (the machine path, the bootstrap admin token included) sees and manages everything, as before
 *       R2-008; what it creates is shared (no owner).
 *   <li>Anyone else (anonymous under {@code security.openReads}, a feed or tile key) sees shared rows only.
 * </ul>
 *
 * <p>An owner is a user's principal id ({@code user.principal_id}, R2-001), the convention
 * {@code notification_channel.owner_principal_id} already used; null means shared, managed by an admin.
 *
 * @param principalId the signed-in user's principal, or null when the caller is not a session
 * @param all true when the caller sees and manages every row
 */
public record OwnerScope(String principalId, boolean all) {

    /** An API token: everything, and new rows are shared. */
    public static final OwnerScope MACHINE = new OwnerScope(null, true);

    /** Anonymous callers and path keys: shared rows only, nothing managed. */
    public static final OwnerScope SHARED_ONLY = new OwnerScope(null, false);

    /**
     * The scope of a session.
     *
     * @param session the session
     * @return its scope
     */
    public static OwnerScope of(AuthenticatedSession session) {
        return new OwnerScope(session.principalId(),
                session.permissions().keys().contains(Permissions.USERS_MANAGE.key()));
    }

    /**
     * The scope of a request: its session, else its API token, else shared only.
     *
     * @param request the request
     * @return its scope
     */
    public static OwnerScope of(HttpServletRequest request) {
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
     * True when a row owned by {@code owner} is visible: everything for {@link #all()}, else the caller's own rows,
     * or, for a caller that is not a session, the shared ones.
     *
     * @param owner the row's owner principal, or null for a shared row
     * @return true when visible
     */
    public boolean sees(String owner) {
        if (all) {
            return true;
        }
        return principalId == null ? owner == null : principalId.equals(owner);
    }

    /**
     * True when the caller may change or delete a row owned by {@code owner}: everything for {@link #all()}, else only
     * its own rows.
     *
     * @param owner the row's owner principal, or null for a shared row
     * @return true when allowed
     */
    public boolean manages(String owner) {
        return all || (principalId != null && principalId.equals(owner));
    }

    /**
     * The owner of a row this caller creates: its principal for a session, null (shared) otherwise.
     *
     * @return the owner principal, or null
     */
    public String ownerForNew() {
        return principalId;
    }

    /**
     * The alerts this caller may see: every alert for {@link #all()}, else its own and the shared ones (shared only
     * for a caller that is not a session).
     *
     * @return the audience, or null for every alert
     */
    public AlertAudience alertAudience() {
        if (all) {
            return null;
        }
        return principalId == null ? AlertAudience.SHARED_ONLY : AlertAudience.own(principalId);
    }
}
