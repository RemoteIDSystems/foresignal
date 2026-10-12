package ai.foresignal.security;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;

/**
 * The token behind the current request, as {@link TokenFilter} resolved it (prompt 023): a bearer
 * token, or the parent token of a stream or tile ticket. Held as a request attribute, so it is
 * available to controllers, audit, Ask actions (028) and MCP (025), and survives the async dispatch
 * of an SSE response.
 *
 * <p>Anonymous requests (open endpoints, or data reads while {@code openReads} is on) have none.
 *
 * @param token the authenticated token
 * @param viaTicket true when the request presented a ticket rather than the token itself
 */
public record CurrentToken(AuthenticatedToken token, boolean viaTicket) {

    /** The request attribute holding it. */
    public static final String ATTRIBUTE = "ai.foresignal.currentToken";

    /** The token of {@code request}, if one was presented and accepted. */
    public static Optional<CurrentToken> of(HttpServletRequest request) {
        Object value = request == null ? null : request.getAttribute(ATTRIBUTE);
        return value instanceof CurrentToken current ? Optional.of(current) : Optional.empty();
    }

    /** The token id. */
    public UUID id() {
        return token.id();
    }

    /** The token's name. */
    public String name() {
        return token.name();
    }

    /** The scope. */
    public Scope scope() {
        return token.scope();
    }

    /** The restrictions. */
    public TokenRestrictions restrictions() {
        return token.restrictions();
    }

    /** The reported device, or null. */
    public DeviceInfo device() {
        return token.device();
    }

    /** The owning principal. */
    public String principalId() {
        return token.principalId();
    }
}
