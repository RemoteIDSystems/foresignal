package ai.foresignal.api.support;

import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenFilter;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * Who made a request, as alert lifecycle fields record it ({@code acknowledgedBy},
 * {@code resolvedBy}; prompt 017).
 *
 * <p>The bootstrap admin token is recorded as {@code admin}; every other token as
 * {@code token:<name>} (prompt 023).
 */
public final class Actors {

    /** The bootstrap admin token. */
    public static final String ADMIN = "admin";

    private Actors() {}

    /**
     * The actor of {@code request}: {@code admin} for the bootstrap admin token, {@code token:<name>}
     * for any other token ({@link CurrentToken}, set by {@link TokenFilter}).
     */
    public static String of(HttpServletRequest request) {
        Optional<CurrentToken> current = CurrentToken.of(request);
        if (current.isPresent()) {
            return current.get().token().bootstrap() ? ADMIN : "token:" + current.get().name();
        }
        Object scope = request.getAttribute(TokenFilter.SCOPE_ATTRIBUTE);
        return scope == null || scope == Scope.ADMIN ? ADMIN : "token:" + scope;
    }
}
