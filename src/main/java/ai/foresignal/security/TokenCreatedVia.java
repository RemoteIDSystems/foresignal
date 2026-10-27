package ai.foresignal.security;

/** How an API token came to exist ({@code api_token.created_via}, OpenAPI {@code ApiToken.createdVia}). */
public enum TokenCreatedVia {
    /** {@code POST /v1/tokens}. */
    API,
    /** A claimed pairing code. */
    PAIRING,
    /** The first-start admin token. */
    BOOTSTRAP,
    /** {@code POST /v1/me/tokens}: a signed-in user's own token, owned by their principal (prompt R2-025). */
    SELF;

    /** The wire value. */
    public String wireValue() {
        return name().toLowerCase();
    }
}
