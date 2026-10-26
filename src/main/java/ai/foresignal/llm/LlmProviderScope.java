package ai.foresignal.llm;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.user.AuthenticatedSession;
import ai.foresignal.security.user.Permissions;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import org.springframework.http.HttpStatus;

/**
 * Which LLM providers a caller may see and manage (prompt R2-011), on the pattern of the channel scope of R2-009.
 *
 * <p>A provider is <b>personal</b> when {@code owner_principal_id} names a user's principal and <b>shared</b> when it
 * is null: the admin-managed providers every provider before R2-011 was.
 *
 * <ul>
 *   <li>A session sees its own providers and the shared ones. It manages its own; it manages the shared ones only
 *       with {@code llm.shared.manage}. What it creates is its own with {@code self.llm}, shared otherwise (or when the
 *       body asks for {@code shared: true}, which needs {@code llm.shared.manage}).
 *   <li>A session holding {@code users.manage} (an admin) sees and manages every provider, other users' included.
 *   <li>An API token (the machine path) sees and manages every provider and creates shared ones, as before R2-011.
 * </ul>
 *
 * @param principalId the session's principal, or null for an API token
 * @param all true when the caller sees and manages every provider
 * @param shared true when the caller manages shared providers ({@code llm.shared.manage})
 * @param personal true when the caller may own providers ({@code self.llm})
 */
public record LlmProviderScope(String principalId, boolean all, boolean shared, boolean personal) {

    /** An API token: every provider; new providers are shared. */
    public static final LlmProviderScope MACHINE = new LlmProviderScope(null, true, true, false);

    /**
     * The scope of a session.
     *
     * @param session the session
     * @return its scope
     */
    public static LlmProviderScope of(AuthenticatedSession session) {
        Set<String> keys = session.permissions().keys();
        return new LlmProviderScope(session.principalId(), keys.contains(Permissions.USERS_MANAGE.key()),
                keys.contains(Permissions.LLM_SHARED_MANAGE.key()), keys.contains(Permissions.SELF_LLM.key()));
    }

    /**
     * The scope of a request: its session, else the machine scope for an API token, else nothing.
     *
     * @param request the request
     * @return its scope
     */
    public static LlmProviderScope of(HttpServletRequest request) {
        if (request == null) {
            return MACHINE;
        }
        AuthenticatedSession session = AuthenticatedSession.of(request).orElse(null);
        if (session != null) {
            return of(session);
        }
        return CurrentToken.of(request).isPresent() ? MACHINE : new LlmProviderScope(null, false, false, false);
    }

    /**
     * True when a provider owned by {@code owner} is visible: the shared ones and the caller's own, or all.
     *
     * @param owner the provider's owner principal, or null for a shared provider
     * @return true when visible
     */
    public boolean sees(String owner) {
        return all || owner == null || owner.equals(principalId);
    }

    /**
     * True when the caller may change, test, list the models of, read the budget of or delete a provider owned by
     * {@code owner}.
     *
     * @param owner the provider's owner principal, or null for a shared provider
     * @return true when allowed
     */
    public boolean manages(String owner) {
        if (all) {
            return true;
        }
        return owner == null ? shared : owner.equals(principalId);
    }

    /**
     * The owner of a provider this caller creates.
     *
     * @param requestedShared the body's {@code shared} member, or null when absent
     * @return the owner principal, or null for a shared provider
     * @throws ForesignalException {@code 403 PERMISSION_DENIED} when the caller may not create that kind
     */
    public String ownerForNew(Boolean requestedShared) {
        if (principalId == null) {
            if (Boolean.FALSE.equals(requestedShared)) {
                throw denied(Permissions.SELF_LLM.key(), "An API token creates shared LLM providers only.");
            }
            return null;
        }
        boolean wantShared = requestedShared == null ? !personal : requestedShared;
        if (wantShared) {
            if (!shared && !all) {
                throw sharedDenied();
            }
            return null;
        }
        if (!personal && !all) {
            throw denied(Permissions.SELF_LLM.key(),
                    "A personal LLM provider needs the self.llm permission, which none of your roles grants.");
        }
        return principalId;
    }

    /**
     * The refusal of a provider the caller can see but not manage: a shared provider without
     * {@code llm.shared.manage}.
     *
     * @return the exception
     */
    public static ForesignalException sharedDenied() {
        return denied(Permissions.LLM_SHARED_MANAGE.key(),
                "Shared LLM providers are managed with the llm.shared.manage permission, which none of your roles"
                        + " grants.");
    }

    private static ForesignalException denied(String permission, String detail) {
        return new ForesignalException(HttpStatus.FORBIDDEN, ErrorCodes.PERMISSION_DENIED, detail)
                .withProperty("requiredPermission", permission);
    }
}
