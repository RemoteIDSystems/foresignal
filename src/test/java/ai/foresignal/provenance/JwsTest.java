package ai.foresignal.provenance;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Detached JWS over Ed25519: the signing input, verification and the RFC 8037 JWK/thumbprint (prompt 109). */
class JwsTest {

    private static final Base64.Encoder URL = Base64.getUrlEncoder().withoutPadding();

    @Test
    @DisplayName("a detached b64:false JWS over Ed25519 round trips and a tampered payload fails")
    void roundTrip() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("Ed25519");
        KeyPair pair = generator.generateKeyPair();
        byte[] x = java.util.Arrays.copyOfRange(pair.getPublic().getEncoded(),
                pair.getPublic().getEncoded().length - 32, pair.getPublic().getEncoded().length);
        String kid = Jwk.thumbprint(x);
        String header = "{\"alg\":\"Ed25519\",\"kid\":\"" + kid + "\",\"b64\":false,\"crit\":[\"b64\"]}";
        String headerB64 = URL.encodeToString(header.getBytes(StandardCharsets.US_ASCII));
        byte[] payload = "the manifest".getBytes(StandardCharsets.UTF_8);
        Signature signature = Signature.getInstance("Ed25519");
        signature.initSign(pair.getPrivate());
        signature.update(JwsSigner.signingInput(headerB64, payload));
        String jws = headerB64 + ".." + URL.encodeToString(signature.sign());

        assertThat(JwsVerifier.verify(payload, jws, pair.getPublic())).isTrue();
        assertThat(JwsVerifier.verify("tampered".getBytes(StandardCharsets.UTF_8), jws, pair.getPublic())).isFalse();
        assertThat(JwsVerifier.kid(jws)).isEqualTo(kid);
    }

    @Test
    @DisplayName("the OKP JWK and its RFC 7638 thumbprint are stable")
    void jwk() {
        byte[] x = new byte[32];
        for (int k = 0; k < x.length; k++) {
            x[k] = (byte) k;
        }
        Map<String, Object> jwk = Jwk.of("abc", x);
        assertThat(jwk).containsEntry("kty", "OKP").containsEntry("crv", "Ed25519").containsEntry("alg", "Ed25519");
        assertThat(jwk.get("x")).isEqualTo(URL.encodeToString(x));
        String kid = Jwk.thumbprint(x);
        assertThat(kid).isEqualTo(Jwk.thumbprint(x));
        assertThat(jwk).containsEntry("kid", "abc");
    }

    @Test
    @DisplayName("the signing input is ASCII(headerB64) . payload")
    void signingInput() {
        byte[] input = JwsSigner.signingInput("HEADER", new byte[] {1, 2, 3});
        assertThat(input[0]).isEqualTo((byte) 'H');
        assertThat(input[6]).isEqualTo((byte) '.');
        assertThat(List.of(input[7], input[8], input[9])).containsExactly((byte) 1, (byte) 2, (byte) 3);
    }
}
