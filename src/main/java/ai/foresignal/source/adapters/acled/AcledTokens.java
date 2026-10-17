package ai.foresignal.source.adapters.acled;

import ai.foresignal.source.adapters.conflict.ConflictSources;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.CredentialsRejectedException;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import tools.jackson.databind.JsonNode;

/**
 * ACLED OAuth tokens, kept <strong>in memory only</strong> per source (prompt 081).
 *
 * <p>Verified 2026-10-13 in the ACLED API getting-started guide: a form POST to {@code /oauth/token} with
 * {@code username} (the account email), {@code password}, {@code grant_type=password}, {@code client_id=acled} and
 * {@code scope=authenticated} returns an access token valid for 24 hours and a refresh token valid for 14 days; the
 * refresh is the same POST with {@code grant_type=refresh_token}, the refresh token and {@code client_id=acled}.
 *
 * <ul>
 *   <li>An access token is reused until {@link #REFRESH_MARGIN} before it expires; then the refresh token is used
 *       while it is valid, and a refused refresh (400 or 401) falls back to a password login.
 *   <li>Tokens are never written to the adapter state, logs or exception messages, so a restart logs in again. The
 *       token POSTs go through the source's own {@code SourceHttp}: they are budgeted, and their traces carry no body.
 *   <li>A changed email or password makes the cached tokens unusable (they are keyed by a SHA-256 fingerprint of
 *       both).
 *   <li>A 400 or 401 from a password login is {@link CredentialsRejectedException}.
 * </ul>
 */
final class AcledTokens {

    /** Refresh this long before the access token expires. */
    static final Duration REFRESH_MARGIN = Duration.ofMinutes(5);

    /** The access token lifetime assumed when the response gives none (24 h). */
    static final Duration DEFAULT_ACCESS_LIFETIME = Duration.ofHours(24);

    /** The refresh token lifetime (14 days). */
    static final Duration REFRESH_LIFETIME = Duration.ofDays(14);

    /** One source's tokens. */
    record Tokens(String fingerprint, String accessToken, Instant accessExpiresAt, String refreshToken,
            Instant refreshExpiresAt) {

        /** Never prints the tokens. */
        @Override
        public String toString() {
            return "Tokens[access expires " + accessExpiresAt + ", refresh expires " + refreshExpiresAt + "]";
        }
    }

    private final Map<String, Tokens> tokens = new ConcurrentHashMap<>();
    private final URI tokenUri;

    /** @param tokenUri the token endpoint */
    AcledTokens(URI tokenUri) {
        this.tokenUri = tokenUri;
    }

    /** The token endpoint. */
    URI tokenUri() {
        return tokenUri;
    }

    /**
     * A valid access token for the source: cached, refreshed, or from a new password login.
     *
     * @throws CredentialsRejectedException when the password login is refused
     * @throws AdapterException when a token request fails otherwise
     */
    String accessToken(FetchContext ctx, String email, String password) throws AdapterException {
        return accessToken(ctx.http(), ctx.sourceId(), ctx.now(), email, password,
                message -> ctx.log().info(message));
    }

    /**
     * A valid access token for the source outside a fetch (prompt 121: the ACLED history import), through the
     * source's own {@link SourceHttp} so the token requests are budgeted like the adapter's.
     *
     * @param http the source's HTTP client
     * @param sourceId the source
     * @param now the current time
     * @param email the account email
     * @param password the account password
     * @param info where an informational message goes
     * @throws CredentialsRejectedException when the password login is refused
     * @throws AdapterException when a token request fails otherwise
     */
    String accessToken(SourceHttp http, String sourceId, Instant now, String email, String password,
            java.util.function.Consumer<String> info) throws AdapterException {
        String fingerprint = fingerprint(email, password);
        Tokens cached = tokens.get(sourceId);
        if (cached != null && cached.fingerprint().equals(fingerprint)) {
            if (now.isBefore(cached.accessExpiresAt().minus(REFRESH_MARGIN))) {
                return cached.accessToken();
            }
            if (cached.refreshToken() != null && now.isBefore(cached.refreshExpiresAt().minus(REFRESH_MARGIN))) {
                try {
                    Tokens refreshed = request(http, "grant_type=refresh_token&refresh_token="
                            + encode(cached.refreshToken()) + "&client_id=acled", fingerprint, now, false);
                    tokens.put(sourceId, refreshed);
                    return refreshed.accessToken();
                } catch (CredentialsRejectedException e) {
                    info.accept("acled: the refresh token was refused; logging in again");
                }
            }
        }
        Tokens fresh = request(http, "username=" + encode(email) + "&password=" + encode(password)
                + "&grant_type=password&client_id=acled&scope=authenticated", fingerprint, now, true);
        tokens.put(sourceId, fresh);
        return fresh.accessToken();
    }

    /** Forgets the source's access token (after a 401), keeping a usable refresh token. */
    void invalidateAccess(String sourceId) {
        tokens.computeIfPresent(sourceId, (id, t) -> new Tokens(t.fingerprint(), t.accessToken(), Instant.EPOCH,
                t.refreshToken(), t.refreshExpiresAt()));
    }

    /** Forgets everything held for the source (a restart has the same effect). */
    void forget(String sourceId) {
        tokens.remove(sourceId);
    }

    /** True when tokens are held for the source (tests). */
    boolean holds(String sourceId) {
        return tokens.containsKey(sourceId);
    }

    private Tokens request(SourceHttp http, String form, String fingerprint, Instant now, boolean login)
            throws AdapterException {
        UpstreamResponse response;
        try {
            response = http.post(tokenUri, "application/x-www-form-urlencoded",
                    form.getBytes(StandardCharsets.UTF_8), Map.of());
        } catch (AdapterException e) {
            int status = e.httpStatus().orElse(0);
            if (status == 400 || status == 401) {
                throw new CredentialsRejectedException(login
                        ? "ACLED refused the login (HTTP " + status + " from the token endpoint); check the email and"
                                + " password credentials"
                        : "ACLED refused the refresh token (HTTP " + status + ")");
            }
            throw e;
        }
        if (response.status() != 200) {
            throw new UpstreamFormatException("ACLED token endpoint answered HTTP " + response.status(), "");
        }
        JsonNode body = ConflictSources.json(response.body(), "ACLED token response");
        String access = ConflictSources.text(body.get("access_token"));
        if (access == null) {
            throw new UpstreamFormatException("ACLED token response has no access_token", "");
        }
        Long expiresIn = ConflictSources.integer(body.get("expires_in"));
        Duration lifetime = expiresIn != null && expiresIn > 0 ? Duration.ofSeconds(expiresIn) : DEFAULT_ACCESS_LIFETIME;
        String refresh = ConflictSources.text(body.get("refresh_token"));
        return new Tokens(fingerprint, access, now.plus(lifetime), refresh,
                refresh == null ? Instant.EPOCH : now.plus(REFRESH_LIFETIME));
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /** A SHA-256 of the email and password, so the cache never holds the password as a key. */
    static String fingerprint(String email, String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(email.getBytes(StandardCharsets.UTF_8));
            digest.update((byte) 0);
            digest.update(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }
}
