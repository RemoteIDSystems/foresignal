package ai.foresignal.mcp;

import ai.foresignal.api.support.Actors;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.Scope;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Who is calling the MCP server, resolved once per HTTP request from what {@code TokenFilter}
 * accepted (prompt 023) and carried to the protocol handlers in the SDK's transport context.
 *
 * @param token the token, or null for an anonymous caller (allowed only while {@code openReads} is on)
 * @param readCaller the read-side view: scope, {@code exposeObserver} and {@code openReads}
 * @param excludedSources the private sources this caller may not see
 * @param remoteIp the client address after trusted-proxy handling, for the audit log
 */
public record McpCaller(AuthenticatedToken token, ReadCaller readCaller, Set<String> excludedSources,
        String remoteIp) {

    /** The transport-context key and request attribute holding the caller. */
    public static final String CONTEXT_KEY = "ai.foresignal.mcp.caller";

    /** The binding key of anonymous sessions. */
    public static final String ANONYMOUS_KEY = "anonymous";

    /** Requires the read caller; copies the excluded sources. */
    public McpCaller {
        Objects.requireNonNull(readCaller, "readCaller");
        excludedSources = excludedSources == null ? Set.of() : Set.copyOf(excludedSources);
    }

    /** True when no token was presented. */
    public boolean anonymous() {
        return token == null;
    }

    /** The token's scope, or null when anonymous. */
    public Scope scope() {
        return token == null ? null : token.scope();
    }

    /** The {@code security.openReads} setting at the time of the request. */
    public boolean openReads() {
        return readCaller.openReads();
    }

    /**
     * What a session is bound to: the token id, or {@value #ANONYMOUS_KEY}. A session created with
     * one token is refused to any other.
     */
    public String key() {
        return token == null ? ANONYMOUS_KEY : "token:" + token.id();
    }

    /** The actor recorded on writes: {@code admin} for the bootstrap token, else {@code token:<name>}. */
    public String actor() {
        if (token == null) {
            return ANONYMOUS_KEY;
        }
        return token.bootstrap() ? Actors.ADMIN : "token:" + token.name();
    }

    /** The token as {@link CurrentToken}, for components that take one. */
    public Optional<CurrentToken> currentToken() {
        return token == null ? Optional.empty() : Optional.of(new CurrentToken(token, false));
    }
}
