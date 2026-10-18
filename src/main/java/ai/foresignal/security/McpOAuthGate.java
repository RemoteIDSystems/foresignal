package ai.foresignal.security;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.Set;

/**
 * What {@link TokenFilter} asks about access tokens from the operator's identity provider on {@code /mcp}
 * (prompt 141, MCP remote authorization).
 *
 * <p>The implementation lives with the MCP server ({@code ai.foresignal.mcp.oauth.McpOAuthService}); this
 * interface keeps the filter independent of it. It applies to {@code /mcp} and {@code /mcp/**} only: on every
 * other path an identity-provider token is just an unknown bearer and gets the usual {@code 401}.
 *
 * <p>Foresignal never forwards the presented token anywhere (no token passthrough): it is verified locally
 * against the identity provider's published keys and then dropped.
 */
public interface McpOAuthGate {

    /** The request attribute holding the {@link Accepted} outcome of an identity-provider token. */
    String ATTRIBUTE = "ai.foresignal.mcp.oauth";

    /** True when {@code mcp.auth.mode} is {@code oauth} or {@code both}. */
    boolean active();

    /** True when Foresignal API tokens are also accepted on {@code /mcp} ({@code mode} {@code token} or {@code both}). */
    boolean acceptsApiTokens();

    /**
     * Verifies an identity-provider access token.
     *
     * @param bearer the compact JWT from {@code Authorization: Bearer}
     * @return accepted with the Foresignal view of the caller, rejected with a reason, or valid but carrying
     *     no mapped scope
     */
    Outcome authenticate(String bearer);

    /**
     * The {@code WWW-Authenticate} value of a {@code 401} on {@code /mcp}: {@code Bearer resource_metadata="…",
     * scope="…"} (RFC 9728 §5.1), plus {@code error="invalid_token"} when a token was presented and refused.
     *
     * @param request the refused request (for the base URL when {@code server.publicBaseUrl} is not set)
     * @param tokenPresented true when a credential was presented
     */
    String challenge(HttpServletRequest request, boolean tokenPresented);

    /**
     * The {@code WWW-Authenticate} value of a {@code 403} for a call needing more than the token carries:
     * {@code Bearer error="insufficient_scope", scope="…", resource_metadata="…"}.
     *
     * @param request the refused request
     * @param required the Foresignal scope needed
     */
    String insufficientScope(HttpServletRequest request, Scope required);

    /**
     * True when {@code bearer} has the shape of a compact JWS ({@code header.payload.signature}, the header
     * starting {@code eyJ}); Foresignal tokens ({@code fsr_…}) and keys never do.
     */
    static boolean looksLikeJwt(String bearer) {
        if (bearer == null || !bearer.startsWith("eyJ")) {
            return false;
        }
        int first = bearer.indexOf('.');
        int second = first < 0 ? -1 : bearer.indexOf('.', first + 1);
        return first > 0 && second > first + 1 && bearer.indexOf('.', second + 1) < 0;
    }

    /** The outcome of {@link #authenticate}. */
    sealed interface Outcome permits Accepted, Rejected, NoScope {}

    /**
     * A verified token whose scopes map to a Foresignal scope.
     *
     * @param token the Foresignal view: a synthetic token (stable id per issuer and subject, the mapped scope,
     *     no restrictions) owned by the subject's principal
     * @param issuer the verified {@code iss}
     * @param subject the verified {@code sub} (or the client id of a client-credentials token)
     * @param idpScopes the scopes the token carried
     */
    record Accepted(AuthenticatedToken token, String issuer, String subject, Set<String> idpScopes)
            implements Outcome {

        /** Requires the token, issuer and subject; copies the scopes. */
        public Accepted {
            Objects.requireNonNull(token, "token");
            Objects.requireNonNull(issuer, "issuer");
            Objects.requireNonNull(subject, "subject");
            idpScopes = idpScopes == null ? Set.of() : Set.copyOf(idpScopes);
        }
    }

    /**
     * A token that is not valid here (signature, issuer, audience, lifetime, algorithm, or keys unavailable).
     *
     * @param reason why, for the problem detail; never contains the token
     */
    record Rejected(String reason) implements Outcome {}

    /**
     * A valid token none of whose scopes maps to a Foresignal scope: {@code 403 insufficient_scope}.
     *
     * @param reason the problem detail
     */
    record NoScope(String reason) implements Outcome {}
}
