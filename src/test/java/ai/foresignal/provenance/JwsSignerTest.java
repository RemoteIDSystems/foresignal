package ai.foresignal.provenance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@link JwsSigner} (prompt 109): the detached {@code b64:false} JWS round trips through {@link JwsVerifier}, a
 * tampered payload or a foreign key fails, the protected header carries {@code alg}, {@code kid}, {@code typ},
 * {@code b64}, {@code crit}, {@code iat} and {@code iss}, and {@code signing.legacyAlg} emits {@code EdDSA}.
 */
class JwsSignerTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    KeyPair pair;
    SigningKeyService keys;

    @BeforeEach
    void setUp() throws Exception {
        pair = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
        keys = mock(SigningKeyService.class);
        when(keys.activeMaterial()).thenReturn(new SigningKeyService.Material("kid-1", pair.getPublic(),
                pair.getPrivate()));
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> header(String jws) {
        String b64 = jws.substring(0, jws.indexOf('.'));
        return JSON.readValue(Base64.getUrlDecoder().decode(b64), Map.class);
    }

    @Test
    @DisplayName("detached b64:false round trip; tampered payload and a foreign key fail")
    void roundTrip() throws Exception {
        JwsSigner signer = new JwsSigner(keys, false, "instance-a");
        byte[] payload = "{\"files\":[]}".getBytes(StandardCharsets.UTF_8);
        String jws = signer.signDetached(payload, "application/json");

        assertThat(jws).contains("..");
        assertThat(JwsVerifier.verify(payload, jws, pair.getPublic())).isTrue();
        assertThat(JwsVerifier.verify("{\"files\":[1]}".getBytes(StandardCharsets.UTF_8), jws, pair.getPublic()))
                .isFalse();
        KeyPair other = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
        assertThat(JwsVerifier.verify(payload, jws, other.getPublic())).isFalse();
        assertThat(JwsVerifier.kid(jws)).isEqualTo("kid-1");
    }

    @Test
    @DisplayName("the protected header: Ed25519, kid, typ, b64:false, crit [b64], iat and iss")
    void headerShape() {
        String jws = new JwsSigner(keys, false, "instance-a").signDetached(new byte[] {1}, "text/markdown");
        Map<String, Object> header = header(jws);
        assertThat(header).containsEntry("alg", "Ed25519").containsEntry("kid", "kid-1")
                .containsEntry("typ", "text/markdown").containsEntry("b64", false)
                .containsEntry("crit", List.of("b64")).containsEntry("iss", "instance-a").containsKey("iat");
    }

    @Test
    @DisplayName("legacyAlg emits EdDSA, which the verifier still accepts")
    void legacyAlg() {
        byte[] payload = "brief".getBytes(StandardCharsets.UTF_8);
        String jws = new JwsSigner(keys, true, null).signDetached(payload, "text/markdown");
        assertThat(header(jws)).containsEntry("alg", "EdDSA").containsEntry("iss", "foresignal");
        assertThat(JwsVerifier.verify(payload, jws, pair.getPublic())).isTrue();
    }

    @Test
    @DisplayName("a JWS that is not detached (a payload between the dots) is refused")
    void notDetached() {
        byte[] payload = "brief".getBytes(StandardCharsets.UTF_8);
        String jws = new JwsSigner(keys, false, "a").signDetached(payload, "text/markdown");
        String attached = jws.replace("..", ".YnJpZWY.");
        assertThat(JwsVerifier.verify(payload, attached, pair.getPublic())).isFalse();
    }
}
