package ai.foresignal.cli.config;

/**
 * One named profile of {@code config.json}. {@link #toString()} never includes the token.
 *
 * @param name  the profile name ({@code default} unless {@code --profile} names another)
 * @param url   the server's base URL, or {@code null}
 * @param token the API token, or {@code null}
 */
public record Profile(String name, String url, String token) {

    /**
     * The name and URL; the token is redacted.
     *
     * @return a redacted description
     */
    @Override
    public String toString() {
        return "Profile[name=" + name + ", url=" + url + ", token=" + (token == null ? "none" : "<redacted>") + "]";
    }
}
