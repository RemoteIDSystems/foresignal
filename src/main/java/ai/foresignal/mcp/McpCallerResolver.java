package ai.foresignal.mcp;

import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.CurrentToken;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * Resolves the {@link McpCaller} of an HTTP request on {@code /mcp}: the token {@code TokenFilter}
 * accepted (or none), the read-side settings, and the private sources hidden from that caller
 * (the same {@link ReadCallerResolver} REST uses).
 */
@Component
public class McpCallerResolver {

    private final ReadCallerResolver readCallers;

    /** Creates the resolver. */
    public McpCallerResolver(ReadCallerResolver readCallers) {
        this.readCallers = readCallers;
    }

    /** The caller of {@code request}; the attribute set by {@link McpCallerFilter} when present. */
    public McpCaller resolve(HttpServletRequest request) {
        Object attribute = request.getAttribute(McpCaller.CONTEXT_KEY);
        if (attribute instanceof McpCaller caller) {
            return caller;
        }
        AuthenticatedToken token = CurrentToken.of(request).map(CurrentToken::token).orElse(null);
        ReadCaller readCaller = readCallers.resolve(request);
        return new McpCaller(token, readCaller, readCallers.excludedSources(request), request.getRemoteAddr());
    }
}
