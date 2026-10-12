package ai.foresignal.notify.push.apns;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.notify.push.EcKeys;
import ai.foresignal.notify.push.Vapid;
import ai.foresignal.notify.push.provider.ProviderCredentials;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.push.NativeTestKeys;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.interfaces.ECPrivateKey;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** The APNs provider token (prompt 032): ES256 JWT, raw signature, 40-minute reuse, forced refresh. */
class ApnsJwtCacheTest {

    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");
    static final JsonMapper JSON = JsonMapper.builder().build();

    final KeyPair pair = NativeTestKeys.ec();
    final MutableClock clock = new MutableClock(T);
    final ApnsJwtCache cache = new ApnsJwtCache(clock);

    ProviderCredentials.Apns credentials(String fingerprint) {
        return new ProviderCredentials.Apns("ios", "TEAM123456", "KEY1234567", "production", false,
                (ECPrivateKey) pair.getPrivate(), fingerprint);
    }

    static JsonNode part(String jwt, int index) {
        return JSON.readTree(new String(EcKeys.unb64(jwt.split("\\.")[index]), StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("header {alg: ES256, kid}, claims {iss: team, iat}, and a raw R||S signature that verifies")
    void claimsHeaderAndSignature() {
        String jwt = cache.token(credentials("ios:1"));
        JsonNode header = part(jwt, 0);
        assertThat(header.get("alg").asString()).isEqualTo("ES256");
        assertThat(header.get("kid").asString()).isEqualTo("KEY1234567");
        assertThat(header.size()).isEqualTo(2);
        JsonNode claims = part(jwt, 1);
        assertThat(claims.get("iss").asString()).isEqualTo("TEAM123456");
        assertThat(claims.get("iat").asLong()).isEqualTo(T.getEpochSecond());
        assertThat(claims.size()).isEqualTo(2);
        assertThat(EcKeys.unb64(jwt.split("\\.")[2])).hasSize(64);
        assertThat(Vapid.verify(jwt, NativeTestKeys.ecPublic(pair))).isTrue();
        assertThat(Vapid.verify(jwt, NativeTestKeys.ecPublic(NativeTestKeys.ec()))).isFalse();
    }

    @Test
    @DisplayName("reused for 40 minutes (never more often than every 20), replaced after")
    void reuseAndRefresh() {
        String first = cache.token(credentials("ios:1"));
        clock.advance(Duration.ofMinutes(20));
        assertThat(cache.token(credentials("ios:1"))).isEqualTo(first);
        clock.advance(Duration.ofMinutes(19).plusSeconds(59));
        assertThat(cache.token(credentials("ios:1"))).isEqualTo(first);
        clock.advance(Duration.ofSeconds(1));
        String second = cache.token(credentials("ios:1"));
        assertThat(second).isNotEqualTo(first);
        assertThat(part(second, 1).get("iat").asLong()).isEqualTo(T.plus(Duration.ofMinutes(40)).getEpochSecond());
    }

    @Test
    @DisplayName("ExpiredProviderToken: one forced refresh; a second caller with the same stale token gets the new one")
    void forcedRefreshOnce() {
        String stale = cache.token(credentials("ios:1"));
        clock.advance(Duration.ofSeconds(1));
        String fresh = cache.forceRefresh(credentials("ios:1"), stale);
        assertThat(fresh).isNotEqualTo(stale);
        clock.advance(Duration.ofSeconds(1));
        assertThat(cache.forceRefresh(credentials("ios:1"), stale)).isEqualTo(fresh);
        assertThat(cache.token(credentials("ios:1"))).isEqualTo(fresh);
    }

    @Test
    @DisplayName("a changed provider (new fingerprint) gets a new token at once")
    void fingerprintChange() {
        String first = cache.token(credentials("ios:1"));
        clock.advance(Duration.ofSeconds(1));
        assertThat(cache.token(credentials("ios:2"))).isNotEqualTo(first);
    }
}
