package ai.foresignal.api.support;

import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenFilter;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Who made a request, as alert lifecycle fields record it ({@code acknowledgedBy},
 * {@code resolvedBy}; prompt 017).
 *
 * <p>Foresignal has a single-operator model and no user directory. Until prompt 023 adds named
 * tokens, the only credential is the bootstrap admin token, recorded as {@code admin}; 023 records
 * its tokens as {@code token:<name>}.
 */
public final class Actors {

    /** The bootstrap admin token. */
    public static final String ADMIN = "admin";

    private Actors() {}

    /**
     * The actor of {@code request}: {@code admin} for the admin token. Prompt 023 resolves named
     * tokens here (the token filter will set their name next to {@link TokenFilter#SCOPE_ATTRIBUTE}).
     */
    public static String of(HttpServletRequest request) {
        Object scope = request.getAttribute(TokenFilter.SCOPE_ATTRIBUTE);
        return scope == null || scope == Scope.ADMIN ? ADMIN : "token:" + scope;
    }
}
