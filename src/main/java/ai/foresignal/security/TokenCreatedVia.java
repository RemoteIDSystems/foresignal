package ai.foresignal.security;

/** How an API token came to exist ({@code api_token.created_via}, OpenAPI {@code ApiToken.createdVia}). */
public enum TokenCreatedVia {
    /** {@code POST /v1/tokens}. */
    API,
    /** A claimed pairing code. */
    PAIRING,
    /** The first-start admin token. */
    BOOTSTRAP;

    /** The wire value. */
    public String wireValue() {
        return name().toLowerCase();
    }
}
