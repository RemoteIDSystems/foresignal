package ai.foresignal.object;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.support.Actors;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.intel.IntelVisibility;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.CurrentToken;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/** Builds {@link ObjectCaller}s from REST requests and MCP callers. */
@Component
public class ObjectCallers {

    private final ReadCallerResolver callers;
    private final IntelVisibility visibility;

    /** Creates the component. */
    public ObjectCallers(ReadCallerResolver callers, IntelVisibility visibility) {
        this.callers = callers;
        this.visibility = visibility;
    }

    /** The caller of a REST request. */
    public ObjectCaller of(HttpServletRequest request) {
        ReadCaller read = callers.resolve(request);
        Set<String> excluded = callers.excludedSources(request);
        Optional<CurrentToken> token = CurrentToken.of(request);
        String principal = token.map(t -> principalOf(t.token())).orElse(null);
        return new ObjectCaller(principal, Actors.of(request), token.map(CurrentToken::scope).orElse(null),
                visibility.seesPrivate(read, excluded), excluded);
    }

    /** The caller of a REST request that must present a token (401 otherwise). */
    public ObjectCaller authenticated(HttpServletRequest request) {
        ObjectCaller caller = of(request);
        if (caller.anonymous()) {
            throw new ForesignalException(HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED,
                    "This operation needs a token.");
        }
        return caller;
    }

    /** The caller of an MCP tool. */
    public ObjectCaller of(McpCaller caller) {
        AuthenticatedToken token = caller.token();
        return new ObjectCaller(token == null ? null : principalOf(token), caller.actor(),
                caller.anonymous() ? null : caller.scope(), visibility.seesPrivate(caller), caller.excludedSources());
    }

    /** The principal id of a token: its principal, or {@code token:<id>} for a token without one. */
    public static String principalOf(AuthenticatedToken token) {
        return token.principalId() != null ? token.principalId() : "token:" + token.id();
    }
}
