package ai.foresignal.security.user;

import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * A session {@link SessionService#authenticate} accepted (prompt R2-003): the user behind it and its effective
 * permissions as read for this request. Held as a request attribute next to the {@code CurrentToken} the token filter
 * sets, so everything that reads the current token keeps working and anything that needs the user or its permissions
 * (R2-004 onward) reads this.
 *
 * @param sessionId the session
 * @param userId the user
 * @param email the user's email
 * @param principalId the user's {@code person} principal
 * @param tokenPrefix the bearer's first eight characters
 * @param expiresAt the absolute expiry
 * @param idleExpiresAt the idle expiry
 * @param permissions the effective permissions
 */
public record AuthenticatedSession(UUID sessionId, UUID userId, String email, String principalId, String tokenPrefix,
        Instant expiresAt, Instant idleExpiresAt, EffectivePermissions permissions) {

    /** The request attribute holding it. */
    public static final String ATTRIBUTE = "ai.foresignal.currentSession";

    /** The {@code CurrentToken} name of every session: {@code session:} plus the user's email. */
    public static final String NAME_PREFIX = "session:";

    /**
     * The session of a request, if it presented one.
     *
     * @param request the request
     * @return the session, or empty
     */
    public static Optional<AuthenticatedSession> of(HttpServletRequest request) {
        Object value = request == null ? null : request.getAttribute(ATTRIBUTE);
        return value instanceof AuthenticatedSession s ? Optional.of(s) : Optional.empty();
    }

    /** The coarse scope derived from the permissions ({@link EffectivePermissions#scope()}). */
    public Scope scope() {
        return permissions.scope();
    }

    /**
     * The token view of the session, for the existing token contract: its id is the session id, its name
     * {@code session:<email>}, its scope the derived scope, no restrictions, the user's principal, and the private
     * grant only for an admin-scoped session.
     *
     * @return the synthetic token
     */
    public AuthenticatedToken asToken() {
        Scope scope = scope();
        return new AuthenticatedToken(sessionId, NAME_PREFIX + email, scope, TokenRestrictions.NONE, null,
                principalId, false, scope == Scope.ADMIN);
    }
}
