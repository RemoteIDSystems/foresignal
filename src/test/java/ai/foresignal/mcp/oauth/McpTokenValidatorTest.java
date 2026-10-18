package ai.foresignal.mcp.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.mcp.TestIdp;
import com.nimbusds.jose.JWSAlgorithm;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@link McpTokenValidator} with an in-test identity provider (prompt 141): the three algorithms, the issuer,
 * audience, lifetime (60 s skew) and algorithm checks, subjects and scope claims, and the unknown-{@code kid}
 * refresh that runs at most once per minute.
 */
class McpTokenValidatorTest {

    static final Instant T = Instant.parse("2026-10-18T12:00:00Z");
    static final String ISSUER = "https://idp.example/realms/home";
    static final String AUDIENCE = "https://foresignal.example/mcp";
    static final String JWKS = "https://idp.example/realms/home/protocol/openid-connect/certs";

    static final TestIdp IDP = TestIdp.create();

    MutableClock clock;
    FakeIdpFetcher fetcher;
    JwksCache cache;
    McpTokenValidator validator;
    McpAuthSettings settings;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(T);
        fetcher = new FakeIdpFetcher().serve(JWKS, IDP.jwks());
        cache = new JwksCache(fetcher, JsonMapper.builder().build(), clock);
        validator = new McpTokenValidator(cache, clock);
        settings = new McpAuthSettings("oauth", ISSUER, null, JWKS, null, null, null);
    }

    TestIdp.Token token() {
        return IDP.token(ISSUER, AUDIENCE).expiresAt(T.plus(Duration.ofMinutes(5))).scope("foresignal:read");
    }

    McpTokenValidator.Verified verify(String token) throws Exception {
        return validator.verify(token, settings, AUDIENCE);
    }

    void assertRefused(String token, String reason) {
        assertThatThrownBy(() -> verify(token))
                .isInstanceOf(McpTokenValidator.InvalidTokenException.class)
                .hasMessageContaining(reason);
    }

    @Test
    @DisplayName("an RS256 token is verified: issuer, subject, scopes and expiry come back")
    void rs256() throws Exception {
        McpTokenValidator.Verified verified = verify(token().scope("openid foresignal:read foresignal:agent")
                .claim("preferred_username", "ada").sign());
        assertThat(verified.issuer()).isEqualTo(ISSUER);
        assertThat(verified.subject()).isEqualTo("user-1");
        assertThat(verified.displayName()).isEqualTo("ada");
        assertThat(verified.scopes()).containsExactly("openid", "foresignal:read", "foresignal:agent");
        assertThat(verified.expiresAt()).isEqualTo(T.plus(Duration.ofMinutes(5)));
    }

    @Test
    @DisplayName("ES256 and EdDSA/Ed25519 tokens are verified (Ed25519 through Tink)")
    void es256AndEdDsa() throws Exception {
        assertThat(verify(token().signWith(IDP.ec, JWSAlgorithm.ES256).sign()).subject()).isEqualTo("user-1");
        assertThat(verify(token().signWith(IDP.ed, JWSAlgorithm.EdDSA).sign()).subject()).isEqualTo("user-1");
        assertThat(verify(token().signWith(IDP.ed, JWSAlgorithm.Ed25519).sign()).subject()).isEqualTo("user-1");
    }

    @Test
    @DisplayName("an algorithm outside allowedAlgs is refused before any key is fetched")
    void algorithmNotAllowed() {
        settings = new McpAuthSettings("oauth", ISSUER, null, JWKS, null, null, List.of("ES256"));
        assertRefused(token().sign(), "algorithm RS256 is not allowed");
        assertThat(fetcher.fetched()).isEmpty();
    }

    @Test
    @DisplayName("HS256 (a public key used as an HMAC secret) and alg none are never accepted")
    void hmacAndNone() {
        byte[] secret = IDP.rsa.toPublicJWK().toJSONString().substring(0, 64).getBytes(StandardCharsets.UTF_8);
        assertRefused(token().hmac(secret).sign(), "algorithm HS256 is not allowed");
        assertRefused(TestIdp.unsigned(ISSUER, AUDIENCE, T.plusSeconds(300)), "not a signed JWT");
        assertRefused("not-a-jwt", "not a signed JWT");
    }

    @Test
    @DisplayName("a wrong issuer is refused, and no key is fetched for it")
    void wrongIssuer() {
        assertRefused(token().issuer("https://evil.example/realms/home").sign(), "issuer does not match");
        assertRefused(token().issuer(ISSUER + "/").sign(), "issuer does not match");
        assertThat(fetcher.fetched()).isEmpty();
    }

    @Test
    @DisplayName("aud must contain the audience (RFC 8707); a trailing slash is not significant; extra values are fine")
    void audience() throws Exception {
        assertRefused(token().audience("https://other.example/mcp").sign(), "not for this resource");
        assertRefused(token().audience().sign(), "not for this resource");
        assertThat(verify(token().audience("account", AUDIENCE).sign()).subject()).isEqualTo("user-1");
        assertThat(verify(token().audience(AUDIENCE + "/").sign()).subject()).isEqualTo("user-1");
    }

    @Test
    @DisplayName("no audience configured refuses every token")
    void noAudience() {
        assertThatThrownBy(() -> validator.verify(token().sign(), settings, null))
                .hasMessageContaining("no audience is configured");
        assertThatThrownBy(() -> validator.verify(token().sign(), new McpAuthSettings("oauth", null, null, JWKS, null,
                null, null), AUDIENCE)).hasMessageContaining("issuer is not configured");
    }

    @Test
    @DisplayName("exp is required and honored with 60 s of skew")
    void expiry() throws Exception {
        assertRefused(token().expiresAt(null).sign(), "no exp");
        assertThat(verify(token().expiresAt(T.minusSeconds(60)).sign()).subject()).isEqualTo("user-1");
        assertRefused(token().expiresAt(T.minusSeconds(61)).sign(), "expired");
    }

    @Test
    @DisplayName("nbf is honored with 60 s of skew")
    void notBefore() throws Exception {
        assertThat(verify(token().notBefore(T.plusSeconds(60)).sign()).subject()).isEqualTo("user-1");
        assertRefused(token().notBefore(T.plusSeconds(61)).sign(), "not valid yet");
    }

    @Test
    @DisplayName("a signature by a key outside the JWKS under a published kid does not verify")
    void forgedSignature() {
        assertRefused(token().signWith(IDP.rotated, JWSAlgorithm.RS256).kid("rsa-1").sign(),
                "signature does not verify");
    }

    @Test
    @DisplayName("a token without kid is tried against every key that fits the algorithm")
    void noKid() throws Exception {
        assertThat(verify(token().kid(null).sign()).subject()).isEqualTo("user-1");
        assertThat(verify(token().signWith(IDP.ec, JWSAlgorithm.ES256).kid(null).sign()).subject()).isEqualTo("user-1");
    }

    @Test
    @DisplayName("an unknown kid triggers exactly one JWKS refresh per minute")
    void unknownKidRefresh() throws Exception {
        verify(token().sign());
        assertThat(fetcher.count(JWKS)).isEqualTo(1);

        fetcher.serve(JWKS, IDP.jwksWithRotated());
        assertThat(verify(token().signWith(IDP.rotated, JWSAlgorithm.RS256).sign()).subject()).isEqualTo("user-1");
        assertThat(fetcher.count(JWKS)).isEqualTo(2);

        assertRefused(token().kid("made-up-1").sign(), "made-up-1");
        assertRefused(token().kid("made-up-2").sign(), "made-up-2");
        assertThat(fetcher.count(JWKS)).isEqualTo(2);

        clock.advance(Duration.ofSeconds(61));
        assertRefused(token().kid("made-up-3").expiresAt(clock.instant().plusSeconds(300)).sign(), "made-up-3");
        assertThat(fetcher.count(JWKS)).isEqualTo(3);
    }

    @Test
    @DisplayName("a known kid of the wrong key type is refused without a refresh")
    void wrongKeyType() {
        verifyQuietly(token().sign());
        assertRefused(token().signWith(IDP.ec, JWSAlgorithm.ES256).kid("rsa-1").sign(), "rsa-1");
        assertThat(fetcher.count(JWKS)).isEqualTo(1);
    }

    @Test
    @DisplayName("the subject falls back to client_id, then azp; none at all is refused")
    void subjects() throws Exception {
        assertThat(verify(token().subject(null).claim("client_id", "svc@clients").sign()).subject())
                .isEqualTo("svc@clients");
        assertThat(verify(token().subject(null).claim("azp", "claude").sign()).subject()).isEqualTo("claude");
        assertRefused(token().subject(null).sign(), "no subject");
    }

    @Test
    @DisplayName("scopeClaim scp reads an array; scope claims of either shape are read")
    void scpClaim() throws Exception {
        settings = new McpAuthSettings("oauth", ISSUER, null, JWKS, Map.of("mcp.read", "read"), "scp", null);
        assertThat(verify(token().scp("mcp.read", "mcp.other").sign()).scopes()).containsExactly("mcp.read", "mcp.other");
        assertThat(verify(token().claim("scp", "a b").sign()).scopes()).containsExactly("a", "b");
        assertThat(verify(token().claim("scp", null).sign()).scopes()).isEmpty();
    }

    @Test
    @DisplayName("keys that cannot be loaded refuse the token with the reason")
    void keysUnavailable() {
        fetcher.remove(JWKS);
        assertRefused(token().sign(), "could not be loaded");
    }

    @Test
    @DisplayName("candidates honor kid, use and alg of the JWK")
    void candidates() throws Exception {
        com.nimbusds.jose.jwk.JWKSet set = com.nimbusds.jose.jwk.JWKSet.parse(IDP.jwks());
        assertThat(McpTokenValidator.candidates(set, "rsa-1", JWSAlgorithm.RS256)).hasSize(1);
        assertThat(McpTokenValidator.candidates(set, null, JWSAlgorithm.ES256)).hasSize(1);
        assertThat(McpTokenValidator.candidates(set, null, JWSAlgorithm.EdDSA)).hasSize(1);
        assertThat(McpTokenValidator.candidates(set, "ec-1", JWSAlgorithm.RS256)).isEmpty();
        com.nimbusds.jose.jwk.RSAKey encryption = new com.nimbusds.jose.jwk.RSAKey.Builder(IDP.rsa.toRSAPublicKey())
                .keyID("enc").keyUse(com.nimbusds.jose.jwk.KeyUse.ENCRYPTION).build();
        assertThat(McpTokenValidator.candidates(new com.nimbusds.jose.jwk.JWKSet(encryption), "enc",
                JWSAlgorithm.RS256)).isEmpty();
        assertThat(McpTokenValidator.allowed(JWSAlgorithm.PS256, McpAuthSettings.SUPPORTED_ALGS)).isFalse();
        assertThat(McpTokenValidator.allowed(null, McpAuthSettings.SUPPORTED_ALGS)).isFalse();
    }

    private void verifyQuietly(String token) {
        try {
            verify(token);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
