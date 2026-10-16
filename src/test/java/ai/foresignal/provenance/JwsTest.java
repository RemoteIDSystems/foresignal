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

    /** RFC 8037 Appendix A.1: the example Ed25519 private key {@code d}. */
    static final String RFC8037_D = "nWGxne_9WmC6hEr0kuwsxERJxWl7MmkZcDusAxyuf2A";

    /** RFC 8037 Appendix A.1/A.2: the example public key {@code x}. */
    static final String RFC8037_X = "11qYAYKxCrfVS_7TyWQHOg7hcvPapiMlrwIaaPcHURo";

    /** RFC 8037 Appendix A.3: the RFC 7638 thumbprint of the example public JWK. */
    static final String RFC8037_THUMBPRINT = "kPrK_qmxVWaYVA9wwBF6Iuo3vVzz7TxHCTwXBygrS4k";

    /** RFC 8037 Appendix A.4: the signing input ({@code {"alg":"EdDSA"}} . "Example of Ed25519 signing"). */
    static final String RFC8037_SIGNING_INPUT = "eyJhbGciOiJFZERTQSJ9.RXhhbXBsZSBvZiBFZDI1NTE5IHNpZ25pbmc";

    /** RFC 8037 Appendix A.4: the signature over {@link #RFC8037_SIGNING_INPUT}. */
    static final String RFC8037_SIGNATURE =
            "hgyY0il_MGCjP0JzlnLWG1PPOt7-09PGcvMg3AIbQR6dWbhijcNR4ki4iylGjg5BhVsPt9g7sVvpAr_MuM0KAg";

    @Test
    @DisplayName("RFC 8037 Appendix A: the OKP JWK encoding and the RFC 7638 thumbprint of the example key")
    void jwk() {
        byte[] x = Base64.getUrlDecoder().decode(RFC8037_X);
        Map<String, Object> jwk = Jwk.of(RFC8037_THUMBPRINT, x);
        assertThat(jwk).containsEntry("kty", "OKP").containsEntry("crv", "Ed25519").containsEntry("alg", "Ed25519");
        assertThat(jwk.get("x")).isEqualTo(RFC8037_X);
        assertThat(Jwk.thumbprint(x)).isEqualTo(RFC8037_THUMBPRINT);
        assertThat(jwk).containsEntry("kid", RFC8037_THUMBPRINT);
    }

    @Test
    @DisplayName("RFC 8037 Appendix A.4: the JDK's Ed25519 over the example key gives the published signature")
    void rfc8037Signature() throws Exception {
        byte[] d = Base64.getUrlDecoder().decode(RFC8037_D);
        byte[] pkcs8 = new byte[48];
        byte[] prefix = {0x30, 0x2e, 0x02, 0x01, 0x00, 0x30, 0x05, 0x06, 0x03, 0x2b, 0x65, 0x70, 0x04, 0x22, 0x04, 0x20};
        System.arraycopy(prefix, 0, pkcs8, 0, prefix.length);
        System.arraycopy(d, 0, pkcs8, prefix.length, d.length);
        java.security.PrivateKey key = java.security.KeyFactory.getInstance("Ed25519")
                .generatePrivate(new java.security.spec.PKCS8EncodedKeySpec(pkcs8));
        Signature signature = Signature.getInstance("Ed25519");
        signature.initSign(key);
        signature.update(RFC8037_SIGNING_INPUT.getBytes(StandardCharsets.US_ASCII));
        assertThat(URL.encodeToString(signature.sign())).isEqualTo(RFC8037_SIGNATURE);
    }

    @Test
    @DisplayName("the verifier refuses alg none, an unknown alg, b64 not false and a crit other than [b64]")
    void headerChecks() {
        assertThat(JwsVerifier.acceptable(Map.of("alg", "Ed25519", "b64", false, "crit", List.of("b64")))).isTrue();
        assertThat(JwsVerifier.acceptable(Map.of("alg", "EdDSA", "b64", false, "crit", List.of("b64")))).isTrue();
        assertThat(JwsVerifier.acceptable(Map.of("alg", "none", "b64", false, "crit", List.of("b64")))).isFalse();
        assertThat(JwsVerifier.acceptable(Map.of("alg", "HS256", "b64", false, "crit", List.of("b64")))).isFalse();
        assertThat(JwsVerifier.acceptable(Map.of("alg", "Ed25519", "crit", List.of("b64")))).isFalse();
        assertThat(JwsVerifier.acceptable(Map.of("alg", "Ed25519", "b64", true, "crit", List.of("b64")))).isFalse();
        assertThat(JwsVerifier.acceptable(Map.of("alg", "Ed25519", "b64", false))).isFalse();
        assertThat(JwsVerifier.acceptable(Map.of("alg", "Ed25519", "b64", false, "crit", List.of("b64", "exp"))))
                .isFalse();
        assertThat(JwsVerifier.acceptable(null)).isFalse();
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
