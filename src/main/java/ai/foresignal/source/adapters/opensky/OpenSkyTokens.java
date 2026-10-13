/*
 * The token cache follows God's Eye View, server/providers/aircraft/opensky.js (getOpenSkyToken: the
 * client_credentials POST, a 1800 s default lifetime and a 60 s refresh margin),
 * https://github.com/bilawalsidhu/gods-eye-view, commit 591f299d11f38a612629a274463196d57ae3862e.
 * Copyright (c) 2026 Bilawal Sidhu. MIT License; the full notice is in THIRD_PARTY_NOTICES.md.
 * Changes: Java; one cached token per source keyed by a fingerprint of the credentials; the POST goes
 * through the budgeted SourceHttp; a 400 or 401 is CredentialsRejectedException.
 */
package ai.foresignal.source.adapters.opensky;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.CredentialsRejectedException;
import ai.foresignal.source.spi.FetchContext;
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
import java.util.OptionalInt;
import java.util.concurrent.ConcurrentHashMap;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * OpenSky OAuth2 access tokens, cached in memory per source (prompt 062).
 *
 * <p>A token is fetched with a client-credentials POST to the OpenSky identity provider through the
 * source's own {@code SourceHttp}, so it is budgeted and its trace is redacted like any request. Tokens last
 * 30 minutes; one is reused until 60 s before it expires. A changed client id or secret makes the cached
 * token unusable (it is keyed by a fingerprint of both). The secret is never logged: it travels only in the
 * POST body, and every trace URL is redacted with the stored credential values.
 */
final class OpenSkyTokens {

    /** Refresh this long before the token expires. */
    static final Duration REFRESH_MARGIN = Duration.ofSeconds(60);

    /** The lifetime assumed when the response gives none. */
    static final Duration DEFAULT_LIFETIME = Duration.ofMinutes(30);

    /** The token path under the identity provider's base URL. */
    static final String TOKEN_PATH = "/auth/realms/opensky-network/protocol/openid-connect/token";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    /** One cached token. */
    record Token(String fingerprint, String accessToken, Instant expiresAt) {}

    private final Map<String, Token> tokens = new ConcurrentHashMap<>();
    private final String authBaseUrl;

    /** @param authBaseUrl the identity provider's base URL, without a trailing slash */
    OpenSkyTokens(String authBaseUrl) {
        this.authBaseUrl = authBaseUrl;
    }

    /** The token endpoint. */
    URI tokenUri() {
        return URI.create(authBaseUrl + TOKEN_PATH);
    }

    /**
     * A valid access token for the source, from the cache or freshly fetched.
     *
     * @throws CredentialsRejectedException when the identity provider answers 400 or 401
     * @throws AdapterException when the request fails otherwise
     */
    String token(FetchContext ctx, String clientId, String clientSecret) throws AdapterException {
        String fingerprint = fingerprint(clientId, clientSecret);
        Instant now = ctx.clock().instant();
        Token cached = tokens.get(ctx.sourceId());
        if (cached != null && cached.fingerprint().equals(fingerprint)
                && now.isBefore(cached.expiresAt().minus(REFRESH_MARGIN))) {
            return cached.accessToken();
        }
        Token fresh = fetch(ctx, clientId, clientSecret, fingerprint, now);
        tokens.put(ctx.sourceId(), fresh);
        return fresh.accessToken();
    }

    /** Forgets the source's token, so the next {@link #token} fetches a new one (after a 401). */
    void invalidate(String sourceId) {
        tokens.remove(sourceId);
    }

    private Token fetch(FetchContext ctx, String clientId, String clientSecret, String fingerprint, Instant now)
            throws AdapterException {
        String form = "grant_type=client_credentials"
                + "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8);
        UpstreamResponse response;
        try {
            response = ctx.http().post(tokenUri(), "application/x-www-form-urlencoded",
                    form.getBytes(StandardCharsets.UTF_8), Map.of());
        } catch (AdapterException e) {
            OptionalInt status = e.httpStatus();
            if (status.isPresent() && (status.getAsInt() == 400 || status.getAsInt() == 401)) {
                throw new CredentialsRejectedException(
                        "OpenSky refused the client credentials (HTTP " + status.getAsInt() + " from the token"
                                + " endpoint); check clientId and clientSecret", e);
            }
            throw e;
        }
        JsonNode body;
        try {
            body = JSON.readTree(response.body());
        } catch (JacksonException e) {
            throw new UpstreamFormatException("OpenSky token response is not JSON", null, e);
        }
        JsonNode token = body == null ? null : body.get("access_token");
        if (token == null || !token.isString() || token.stringValue().isBlank()) {
            throw new UpstreamFormatException("OpenSky token response has no access_token", null);
        }
        JsonNode expiresIn = body.get("expires_in");
        Duration lifetime = expiresIn != null && expiresIn.isNumber() && expiresIn.longValue() > 0
                ? Duration.ofSeconds(expiresIn.longValue())
                : DEFAULT_LIFETIME;
        return new Token(fingerprint, token.stringValue(), now.plus(lifetime));
    }

    /** A SHA-256 of the id and secret, so the cache never holds the secret as a key. */
    static String fingerprint(String clientId, String clientSecret) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(clientId.getBytes(StandardCharsets.UTF_8));
            digest.update((byte) 0);
            digest.update(clientSecret.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }
}
