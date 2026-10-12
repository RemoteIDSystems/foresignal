package ai.foresignal.notify.push.fcm;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.notify.push.EcKeys;
import ai.foresignal.notify.push.NativePushHttp;
import ai.foresignal.notify.push.NativePushTransport.NativePushException;
import ai.foresignal.notify.push.PushProperties;
import ai.foresignal.notify.push.provider.ProviderCredentials;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.push.NativeTestKeys;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The FCM OAuth access token (prompt 032) against a WireMock token endpoint: the RS256 assertion's
 * claims and signature, caching, and refresh before expiry. The key is generated at test time.
 */
class FcmAccessTokenCacheTest {

    @RegisterExtension
    static final WireMockExtension OAUTH = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort()).resetOnEachTest(true).build();

    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");
    static final JsonMapper JSON = JsonMapper.builder().build();

    final KeyPair pair = NativeTestKeys.rsa();
    final MutableClock clock = new MutableClock(T);
    final NativePushHttp http = new NativePushHttp(NativePushHttp.platformTls(),
            new PushProperties(true, true, null, null, 0, null, 0, null, null));
    final FcmAccessTokenCache cache = new FcmAccessTokenCache(http, JSON, clock);

    ProviderCredentials.Fcm credentials;

    @BeforeEach
    void setUp() {
        credentials = new ProviderCredentials.Fcm("android", "my-project", "sender@my-project.iam.gserviceaccount.com",
                OAUTH.baseUrl() + "/token", (RSAPrivateKey) pair.getPrivate(), "android:1");
        stubToken("ya29.first", 3600);
    }

    void stubToken(String token, long expiresIn) {
        OAUTH.stubFor(post(urlPathEqualTo("/token")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"access_token\":\"" + token + "\",\"expires_in\":" + expiresIn + ",\"token_type\":\"Bearer\"}")));
    }

    static Map<String, String> form(LoggedRequest request) {
        Map<String, String> out = new LinkedHashMap<>();
        for (String pair : request.getBodyAsString().split("&")) {
            String[] kv = pair.split("=", 2);
            out.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8), URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
        }
        return out;
    }

    @Test
    @DisplayName("the assertion: RS256 header, iss/scope/aud/iat/exp claims, a signature that verifies, the jwt-bearer grant")
    void assertionClaims() throws Exception {
        assertThat(cache.token(credentials)).isEqualTo("ya29.first");
        List<LoggedRequest> requests = OAUTH.findAll(postRequestedFor(urlPathEqualTo("/token")));
        assertThat(requests).hasSize(1);
        assertThat(requests.getFirst().getHeader("Content-Type")).startsWith("application/x-www-form-urlencoded");
        Map<String, String> form = form(requests.getFirst());
        assertThat(form.get("grant_type")).isEqualTo("urn:ietf:params:oauth:grant-type:jwt-bearer");
        String[] jwt = form.get("assertion").split("\\.");
        JsonNode header = JSON.readTree(new String(EcKeys.unb64(jwt[0]), StandardCharsets.UTF_8));
        assertThat(header.get("alg").asString()).isEqualTo("RS256");
        assertThat(header.get("typ").asString()).isEqualTo("JWT");
        JsonNode claims = JSON.readTree(new String(EcKeys.unb64(jwt[1]), StandardCharsets.UTF_8));
        assertThat(claims.get("iss").asString()).isEqualTo("sender@my-project.iam.gserviceaccount.com");
        assertThat(claims.get("scope").asString()).isEqualTo("https://www.googleapis.com/auth/firebase.messaging");
        assertThat(claims.get("aud").asString()).isEqualTo(OAUTH.baseUrl() + "/token");
        assertThat(claims.get("iat").asLong()).isEqualTo(T.getEpochSecond());
        assertThat(claims.get("exp").asLong()).isEqualTo(T.getEpochSecond() + 3600);
        Signature verify = Signature.getInstance("SHA256withRSA");
        verify.initVerify(pair.getPublic());
        verify.update((jwt[0] + "." + jwt[1]).getBytes(StandardCharsets.US_ASCII));
        assertThat(verify.verify(EcKeys.unb64(jwt[2]))).isTrue();
    }

    @Test
    @DisplayName("cached until expires_in minus 300 s, then fetched again")
    void cachingAndRefresh() {
        assertThat(cache.token(credentials)).isEqualTo("ya29.first");
        clock.advance(Duration.ofSeconds(3299));
        assertThat(cache.token(credentials)).isEqualTo("ya29.first");
        assertThat(OAUTH.findAll(postRequestedFor(urlPathEqualTo("/token")))).hasSize(1);
        stubToken("ya29.second", 3600);
        clock.advance(Duration.ofSeconds(1));
        assertThat(cache.token(credentials)).isEqualTo("ya29.second");
        assertThat(OAUTH.findAll(postRequestedFor(urlPathEqualTo("/token")))).hasSize(2);
    }

    @Test
    @DisplayName("invalidate and a changed key fetch a new token")
    void invalidateAndFingerprint() {
        cache.token(credentials);
        cache.invalidate("android");
        cache.token(credentials);
        ProviderCredentials.Fcm changed = new ProviderCredentials.Fcm("android", "my-project", credentials.clientEmail(),
                credentials.tokenUri(), credentials.key(), "android:2");
        cache.token(changed);
        assertThat(OAUTH.findAll(postRequestedFor(urlPathEqualTo("/token")))).hasSize(3);
    }

    @Test
    @DisplayName("a refusal is reported with Google's error code only, never the key or the assertion")
    void refusal() {
        OAUTH.stubFor(post(urlPathEqualTo("/token")).willReturn(aResponse().withStatus(400)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"error\":\"invalid_grant\",\"error_description\":\"Invalid JWT Signature.\"}")));
        assertThatThrownBy(() -> cache.token(credentials)).isInstanceOf(NativePushException.class)
                .hasMessage("the OAuth token endpoint answered 400 invalid_grant");
    }
}
