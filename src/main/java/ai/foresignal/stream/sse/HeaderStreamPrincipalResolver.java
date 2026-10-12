package ai.foresignal.stream.sse;

import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenFilter;
import ai.foresignal.security.TokenHasher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

/**
 * The default {@link StreamPrincipalResolver}: the scope {@link TokenFilter} recorded after
 * verifying the {@code Authorization: Bearer} token, and a principal id derived from that token's
 * SHA-256 (the first 16 hex digits), so the raw token is never kept. A request without a verified
 * token is anonymous and keyed on its remote address.
 */
@Component
public class HeaderStreamPrincipalResolver implements StreamPrincipalResolver {

    static final String BEARER_PREFIX = "Bearer ";

    /** Hex digits of the token hash kept in the principal id. */
    static final int ID_HASH_DIGITS = 16;

    private final TokenHasher hasher;

    /** Creates the resolver. */
    public HeaderStreamPrincipalResolver(TokenHasher hasher) {
        this.hasher = hasher;
    }

    @Override
    public StreamPrincipal resolve(HttpServletRequest request) {
        Object attribute = request.getAttribute(TokenFilter.SCOPE_ATTRIBUTE);
        String token = bearer(request.getHeader(HttpHeaders.AUTHORIZATION));
        if (!(attribute instanceof Scope scope) || token == null) {
            return StreamPrincipal.anonymous(request.getRemoteAddr());
        }
        String id = "token:" + hasher.hash(token).substring(0, ID_HASH_DIGITS);
        return new StreamPrincipal(id, scope, request.getRemoteAddr());
    }

    private static String bearer(String header) {
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            return null;
        }
        String token = header.substring(BEARER_PREFIX.length()).trim();
        return token.isEmpty() ? null : token;
    }
}
