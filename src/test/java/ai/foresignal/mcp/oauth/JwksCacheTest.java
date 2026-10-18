package ai.foresignal.mcp.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.mcp.TestIdp;
import com.nimbusds.jose.jwk.JWKSet;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@link JwksCache} (prompt 141): RFC 8414 then OpenID Connect discovery with the issuer check, the explicit
 * {@code jwksUri}, the one-hour reload with stale keys on failure, the once-per-minute refresh and failure retry
 * limits, and a configuration change starting afresh.
 */
class JwksCacheTest {

    static final Instant T = Instant.parse("2026-10-18T12:00:00Z");
    static final TestIdp IDP = TestIdp.create();

    static final String ISSUER = "https://kc.example/realms/home";
    static final String RFC8414 = "https://kc.example/.well-known/oauth-authorization-server/realms/home";
    static final String OIDC = "https://kc.example/realms/home/.well-known/openid-configuration";
    static final String CERTS = "https://kc.example/realms/home/protocol/openid-connect/certs";

    MutableClock clock;
    FakeIdpFetcher fetcher;
    JwksCache cache;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(T);
        fetcher = new FakeIdpFetcher();
        cache = new JwksCache(fetcher, JsonMapper.builder().build(), clock);
    }

    static McpAuthSettings discovered(String issuer) {
        return new McpAuthSettings("oauth", issuer, null, null, null, null, null);
    }

    static String metadata(String issuer, String jwks) {
        return "{\"issuer\":\"" + issuer + "\",\"jwks_uri\":\"" + jwks + "\"}";
    }

    @Test
    @DisplayName("the RFC 8414 location inserts the well-known segment before the issuer's path")
    void locations() {
        assertThat(JwksCache.authorizationServerMetadataUri(ISSUER)).hasToString(RFC8414);
        assertThat(JwksCache.authorizationServerMetadataUri("https://auth.example/")).hasToString(
                "https://auth.example/.well-known/oauth-authorization-server");
        assertThat(JwksCache.authorizationServerMetadataUri("https://auth.example/application/o/foresignal/"))
                .hasToString("https://auth.example/.well-known/oauth-authorization-server/application/o/foresignal");
        assertThat(JwksCache.openIdConfigurationUri("https://auth.example/application/o/foresignal/"))
                .hasToString("https://auth.example/application/o/foresignal/.well-known/openid-configuration");
        assertThat(JwksCache.openIdConfigurationUri("https://tenant.auth0.com/"))
                .hasToString("https://tenant.auth0.com/.well-known/openid-configuration");
    }

    @Test
    @DisplayName("RFC 8414 metadata is used when the identity provider serves it")
    void rfc8414() throws Exception {
        fetcher.serve(RFC8414, metadata(ISSUER, CERTS)).serve(CERTS, IDP.jwks());
        JWKSet keys = cache.keys(discovered(ISSUER));
        assertThat(keys.getKeyByKeyId("rsa-1")).isNotNull();
        assertThat(keys.getKeyByKeyId("rsa-1").isPrivate()).isFalse();
        assertThat(fetcher.fetched()).containsExactly(RFC8414, CERTS);
    }

    @Test
    @DisplayName("without RFC 8414 metadata, OpenID Connect discovery is used")
    void oidcFallback() throws Exception {
        fetcher.serve(OIDC, metadata(ISSUER, CERTS)).serve(CERTS, IDP.jwks());
        assertThat(cache.keys(discovered(ISSUER)).getKeys()).hasSize(3);
        assertThat(fetcher.fetched()).containsExactly(RFC8414, OIDC, CERTS);
    }

    @Test
    @DisplayName("a discovery document naming another issuer is not trusted")
    void issuerMismatch() {
        fetcher.serve(RFC8414, metadata("https://evil.example", CERTS))
                .serve(OIDC, metadata(ISSUER + "/", CERTS)).serve(CERTS, IDP.jwks());
        assertThatThrownBy(() -> cache.keys(discovered(ISSUER)))
                .isInstanceOf(JwksCache.JwksUnavailableException.class)
                .hasMessageContaining("names another issuer");
        assertThat(fetcher.count(CERTS)).isZero();
    }

    @Test
    @DisplayName("a discovery document without jwks_uri, or not JSON, fails discovery")
    void badDocuments() {
        fetcher.serve(RFC8414, "{\"issuer\":\"" + ISSUER + "\"}").serve(OIDC, "<html>");
        assertThatThrownBy(() -> cache.keys(discovered(ISSUER)))
                .hasMessageContaining("no jwks_uri").hasMessageContaining("not JSON");
    }

    @Test
    @DisplayName("an explicit jwksUri skips discovery")
    void explicitJwksUri() throws Exception {
        fetcher.serve(CERTS, IDP.jwks());
        cache.keys(new McpAuthSettings("oauth", ISSUER, null, CERTS, null, null, null));
        assertThat(fetcher.fetched()).containsExactly(CERTS);
    }

    @Test
    @DisplayName("keys are cached for an hour, then reloaded; a failed reload keeps the old keys")
    void maxAge() throws Exception {
        McpAuthSettings settings = new McpAuthSettings("oauth", ISSUER, null, CERTS, null, null, null);
        fetcher.serve(CERTS, IDP.jwks());
        cache.keys(settings);
        clock.advance(Duration.ofMinutes(59));
        cache.keys(settings);
        assertThat(fetcher.count(CERTS)).isEqualTo(1);

        clock.advance(Duration.ofMinutes(2));
        fetcher.serve(CERTS, TestIdp.jwks(IDP.rotated));
        assertThat(cache.keys(settings).getKeyByKeyId("rsa-2")).isNotNull();
        assertThat(fetcher.count(CERTS)).isEqualTo(2);

        clock.advance(Duration.ofMinutes(61));
        fetcher.remove(CERTS);
        assertThat(cache.keys(settings).getKeyByKeyId("rsa-2")).as("stale keys keep serving").isNotNull();
        assertThat(cache.keys(settings).getKeyByKeyId("rsa-2")).isNotNull();
        assertThat(fetcher.count(CERTS)).as("the failed reload is not retried within a minute").isEqualTo(3);
    }

    @Test
    @DisplayName("a failed first load is retried at most once a minute")
    void failureRetry() throws Exception {
        McpAuthSettings settings = new McpAuthSettings("oauth", ISSUER, null, CERTS, null, null, null);
        assertThatThrownBy(() -> cache.keys(settings)).isInstanceOf(JwksCache.JwksUnavailableException.class);
        assertThatThrownBy(() -> cache.keys(settings)).isInstanceOf(JwksCache.JwksUnavailableException.class);
        assertThat(fetcher.count(CERTS)).isEqualTo(1);
        clock.advance(Duration.ofSeconds(61));
        fetcher.serve(CERTS, IDP.jwks());
        assertThat(cache.keys(settings).getKeys()).isNotEmpty();
        assertThat(fetcher.count(CERTS)).isEqualTo(2);
    }

    @Test
    @DisplayName("refresh runs at most once per minute")
    void refreshLimit() throws Exception {
        McpAuthSettings settings = new McpAuthSettings("oauth", ISSUER, null, CERTS, null, null, null);
        fetcher.serve(CERTS, IDP.jwks());
        cache.keys(settings);
        assertThat(cache.refresh(settings)).isPresent();
        assertThat(cache.refresh(settings)).isEmpty();
        clock.advance(Duration.ofSeconds(59));
        assertThat(cache.refresh(settings)).isEmpty();
        clock.advance(Duration.ofSeconds(1));
        assertThat(cache.refresh(settings)).isPresent();
        assertThat(fetcher.count(CERTS)).isEqualTo(3);
        fetcher.remove(CERTS);
        clock.advance(Duration.ofSeconds(60));
        assertThat(cache.refresh(settings)).as("a failed refresh").isEmpty();
        assertThat(cache.keys(settings).getKeyByKeyId("rsa-1")).as("old keys kept").isNotNull();
    }

    @Test
    @DisplayName("a new issuer or jwksUri starts afresh; invalidate forgets everything")
    void configurationChange() throws Exception {
        String other = "https://other.example/jwks";
        fetcher.serve(CERTS, IDP.jwks()).serve(other, TestIdp.jwks(IDP.rotated));
        assertThat(cache.keys(new McpAuthSettings("oauth", ISSUER, null, CERTS, null, null, null))
                .getKeyByKeyId("rsa-1")).isNotNull();
        assertThat(cache.keys(new McpAuthSettings("oauth", ISSUER, null, other, null, null, null))
                .getKeyByKeyId("rsa-2")).isNotNull();
        cache.invalidate();
        cache.keys(new McpAuthSettings("oauth", ISSUER, null, other, null, null, null));
        assertThat(fetcher.count(other)).isEqualTo(2);
    }

    @Test
    @DisplayName("discovery without an issuer fails")
    void noIssuer() {
        assertThatThrownBy(() -> cache.keys(discovered(null))).hasMessageContaining("issuer is not set");
    }
}
