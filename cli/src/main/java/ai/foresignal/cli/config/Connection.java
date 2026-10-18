package ai.foresignal.cli.config;

/**
 * Where and as whom the CLI calls the API: the resolved base URL and token after flags, environment and profile.
 * {@link #toString()} never includes the token.
 *
 * @param baseUrl the server's base URL without a trailing slash, e.g. {@code https://foresignal.example}
 * @param token   the API token, or {@code null} for anonymous calls (open reads)
 */
public record Connection(String baseUrl, String token) {

    /**
     * Normalizes the base URL (trims it and drops trailing slashes).
     *
     * @param baseUrl the server's base URL
     * @param token   the API token, or {@code null}
     */
    public Connection {
        baseUrl = normalizeUrl(baseUrl);
        token = token == null || token.isBlank() ? null : token.trim();
    }

    /**
     * Trims a base URL and removes trailing slashes.
     *
     * @param url the URL as typed
     * @return the normalized URL
     */
    public static String normalizeUrl(String url) {
        String trimmed = url == null ? "" : url.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    /**
     * Whether a token is configured.
     *
     * @return true when calls carry {@code Authorization: Bearer …}
     */
    public boolean hasToken() {
        return token != null;
    }

    /**
     * The base URL and whether a token is set; the token itself is never shown.
     *
     * @return a redacted description
     */
    @Override
    public String toString() {
        return "Connection[baseUrl=" + baseUrl + ", token=" + (token == null ? "none" : "<redacted>") + "]";
    }
}
