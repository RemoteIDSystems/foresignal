package ai.foresignal.testsupport.security;

import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenCreatedVia;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.TokenService;
import java.time.Instant;

/** Creates API tokens for tests through the production {@link TokenService} (prompt 023). */
public final class TestTokens {

    private TestTokens() {}

    /** A new unrestricted token of {@code scope}; returns the created token with its plaintext. */
    public static TokenService.CreatedToken create(TokenService tokens, String name, Scope scope) {
        return create(tokens, name, scope, TokenRestrictions.NONE, null);
    }

    /** A new token with {@code restrictions} and {@code expiresAt} (null for none). */
    public static TokenService.CreatedToken create(TokenService tokens, String name, Scope scope,
            TokenRestrictions restrictions, Instant expiresAt) {
        return tokens.create(new TokenService.TokenSpec(name, scope, expiresAt, restrictions, null,
                TokenCreatedVia.API, null));
    }

    /** A new unrestricted token of {@code scope} with the 039 private grant {@code privateAccess}. */
    public static TokenService.CreatedToken create(TokenService tokens, String name, Scope scope,
            boolean privateAccess) {
        return tokens.create(new TokenService.TokenSpec(name, scope, null, TokenRestrictions.NONE, null,
                TokenCreatedVia.API, null, privateAccess));
    }

    /** {@code Bearer <plaintext>} of {@code created}. */
    public static String bearer(TokenService.CreatedToken created) {
        return "Bearer " + created.plaintext();
    }
}
