package ai.foresignal.mcp.oauth;

import com.nimbusds.jose.jwk.JWKSet;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The identity provider's signing keys for MCP remote authorization (prompt 141).
 *
 * <p>The JWKS location is {@code mcp.auth.jwksUri} when set; otherwise it is discovered from the issuer, first as
 * RFC 8414 authorization server metadata ({@code /.well-known/oauth-authorization-server} inserted before the
 * issuer's path), then as OpenID Connect discovery ({@code {issuer}/.well-known/openid-configuration}). A discovery
 * document counts only when its {@code issuer} equals the configured issuer exactly (RFC 8414 §3.3, OpenID Connect
 * Discovery §4.3). Every fetch goes through {@link IdpDocumentFetcher} (the outbound URL policy).
 *
 * <p>Caching:
 *
 * <ul>
 *   <li>The keys are kept for {@link #MAX_AGE}, then reloaded on next use; when a reload fails the previous keys
 *       keep serving.
 *   <li>A token naming an unknown {@code kid} may trigger {@link #refresh}, which fetches again <em>at most once
 *       per {@link #MIN_REFRESH_INTERVAL}</em>, so tokens with made-up key ids cannot turn Foresignal into a load
 *       generator against the identity provider.
 *   <li>A failed load is retried at most once per {@link #MIN_REFRESH_INTERVAL} as well.
 *   <li>A change of issuer or {@code jwksUri} starts from scratch.
 * </ul>
 */
@Component
public class JwksCache {

    private static final Logger log = LoggerFactory.getLogger(JwksCache.class);

    /** The shortest interval between two unknown-{@code kid} refreshes, and between retries of a failed load. */
    public static final Duration MIN_REFRESH_INTERVAL = Duration.ofMinutes(1);

    /** How long loaded keys are used before they are reloaded. */
    public static final Duration MAX_AGE = Duration.ofHours(1);

    private final IdpDocumentFetcher fetcher;
    private final JsonMapper json;
    private final Clock clock;

    /** Guarded by {@code this}. */
    private State state;

    /**
     * What is cached for one configuration.
     *
     * @param configKey the issuer and configured {@code jwksUri} it belongs to
     * @param jwksUri the resolved JWKS location, or null before the first successful discovery
     * @param keys the public keys, or null before the first successful load
     * @param loadedAt when {@code keys} were loaded
     * @param lastAttempt the last fetch attempt, successful or not
     * @param lastRefresh the last unknown-{@code kid} refresh, or null
     * @param failure the reason of the last failed attempt, or null
     */
    private record State(String configKey, String jwksUri, JWKSet keys, Instant loadedAt, Instant lastAttempt,
            Instant lastRefresh, String failure) {}

    /** Thrown when no keys are available: nothing loaded yet and the last attempt failed. */
    public static final class JwksUnavailableException extends Exception {

        private static final long serialVersionUID = 1L;

        /** Creates the exception. */
        public JwksUnavailableException(String message) {
            super(message);
        }
    }

    /** Creates the cache. */
    public JwksCache(IdpDocumentFetcher fetcher, JsonMapper json, Clock clock) {
        this.fetcher = fetcher;
        this.json = json;
        this.clock = clock;
    }

    /**
     * The keys for {@code settings}: cached while fresh, else loaded.
     *
     * @throws JwksUnavailableException when there are no keys to use
     */
    public synchronized JWKSet keys(McpAuthSettings settings) throws JwksUnavailableException {
        String key = configKey(settings);
        Instant now = clock.instant();
        State current = state != null && state.configKey().equals(key) ? state : null;
        if (current != null && current.keys() != null && now.isBefore(current.loadedAt().plus(MAX_AGE))) {
            return current.keys();
        }
        if (current != null && current.failure() != null && now.isBefore(current.lastAttempt().plus(MIN_REFRESH_INTERVAL))) {
            if (current.keys() != null) {
                return current.keys();
            }
            throw new JwksUnavailableException("the identity provider's keys could not be loaded: " + current.failure());
        }
        State loaded = load(settings, key, current, now, current == null ? null : current.lastRefresh());
        if (loaded.keys() == null) {
            throw new JwksUnavailableException("the identity provider's keys could not be loaded: " + loaded.failure());
        }
        return loaded.keys();
    }

    /**
     * Fetches the keys again because a token named an unknown {@code kid}, unless a refresh ran less than
     * {@link #MIN_REFRESH_INTERVAL} ago.
     *
     * @return the new keys, or empty when no refresh was allowed or it failed
     */
    public synchronized Optional<JWKSet> refresh(McpAuthSettings settings) {
        String key = configKey(settings);
        Instant now = clock.instant();
        State current = state != null && state.configKey().equals(key) ? state : null;
        if (current != null && current.lastRefresh() != null
                && now.isBefore(current.lastRefresh().plus(MIN_REFRESH_INTERVAL))) {
            return Optional.empty();
        }
        State loaded = load(settings, key, current, now, now);
        return loaded.failure() == null ? Optional.ofNullable(loaded.keys()) : Optional.empty();
    }

    /** Forgets everything (a settings change, tests). */
    public synchronized void invalidate() {
        state = null;
    }

    /** Loads the keys and records the outcome; a failure keeps the previous keys. */
    private State load(McpAuthSettings settings, String key, State current, Instant now, Instant refreshMark) {
        String jwksUri = current == null ? null : current.jwksUri();
        try {
            if (jwksUri == null) {
                jwksUri = resolveJwksUri(settings);
            }
            JWKSet keys = JWKSet.parse(fetcher.fetch(new URI(jwksUri))).toPublicJWKSet();
            state = new State(key, jwksUri, keys, now, now, refreshMark, null);
        } catch (IOException | ParseException | URISyntaxException | RuntimeException e) {
            String reason = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            log.warn("MCP remote authorization: could not load the identity provider's JWKS: {}", reason);
            state = new State(key, jwksUri, current == null ? null : current.keys(),
                    current == null ? null : current.loadedAt(), now, refreshMark, reason);
        }
        return state;
    }

    /**
     * The JWKS location: {@code jwksUri}, else from RFC 8414 metadata, else from OpenID Connect discovery.
     *
     * @throws IOException naming why both discovery documents failed
     */
    String resolveJwksUri(McpAuthSettings settings) throws IOException {
        if (settings.jwksUri() != null && !settings.jwksUri().isBlank()) {
            return settings.jwksUri().strip();
        }
        String issuer = settings.issuer();
        if (issuer == null || issuer.isBlank()) {
            throw new IOException("mcp.auth.issuer is not set");
        }
        String rfc8414;
        try {
            return discover(authorizationServerMetadataUri(issuer), issuer);
        } catch (IOException | RuntimeException e) {
            rfc8414 = e.getMessage();
        }
        try {
            return discover(openIdConfigurationUri(issuer), issuer);
        } catch (IOException | RuntimeException e) {
            throw new IOException("discovery failed (RFC 8414: " + rfc8414 + "; OpenID Connect: " + e.getMessage()
                    + ")", e);
        }
    }

    /** The {@code jwks_uri} of the discovery document at {@code uri}, which must name {@code issuer}. */
    private String discover(URI uri, String issuer) throws IOException {
        JsonNode document;
        try {
            document = json.readTree(fetcher.fetch(uri));
        } catch (tools.jackson.core.JacksonException e) {
            throw new IOException(uri.getPath() + " is not JSON", e);
        }
        JsonNode documentIssuer = document == null ? null : document.get("issuer");
        if (documentIssuer == null || !documentIssuer.isString() || !issuer.equals(documentIssuer.asString())) {
            throw new IOException(uri.getPath() + " names another issuer");
        }
        JsonNode jwks = document.get("jwks_uri");
        if (jwks == null || !jwks.isString() || jwks.asString().isBlank()) {
            throw new IOException(uri.getPath() + " has no jwks_uri");
        }
        return jwks.asString().strip();
    }

    /**
     * The RFC 8414 §3.1 metadata location: {@code /.well-known/oauth-authorization-server} inserted between the
     * issuer's host and path ({@code https://kc/realms/r} → {@code https://kc/.well-known/oauth-authorization-server/realms/r}).
     */
    static URI authorizationServerMetadataUri(String issuer) {
        URI uri = URI.create(issuer.strip());
        String path = uri.getRawPath() == null ? "" : uri.getRawPath();
        while (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return URI.create(uri.getScheme() + "://" + uri.getRawAuthority() + "/.well-known/oauth-authorization-server"
                + path);
    }

    /** The OpenID Connect discovery location: {@code {issuer without trailing slash}/.well-known/openid-configuration}. */
    static URI openIdConfigurationUri(String issuer) {
        String base = issuer.strip();
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return URI.create(base + "/.well-known/openid-configuration");
    }

    private static String configKey(McpAuthSettings settings) {
        return Objects.toString(settings.issuer(), "") + "\n" + Objects.toString(settings.jwksUri(), "");
    }
}
