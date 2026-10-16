package ai.foresignal.provenance;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.security.audit.AuditService;
import ai.foresignal.test.IntegrationTest;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * {@link SigningKeyService} against PostgreSQL (prompt 109, task 6): the key is generated once, even when first
 * uses race; the private key is encrypted at rest; the JWKS has the OKP shape with an RFC 7638 thumbprint
 * {@code kid}; rotation leaves exactly one active key, keeps the retired key verifiable and writes an audit event.
 */
@IntegrationTest
class SigningKeyServiceIT {

    @Autowired SigningKeyService signing;
    @Autowired SigningKeyRepository keys;
    @Autowired JwsSigner signer;
    @Autowired JdbcTemplate jdbc;
    @Autowired AuditService audit;

    @BeforeEach
    void setUp() {
        jdbc.update("DELETE FROM signing_key");
    }

    @Test
    @DisplayName("generated once: repeated and concurrent first uses all see one active key")
    void generatedOnce() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(8);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<String>> kids = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            kids.add(pool.submit(() -> {
                start.await();
                return signing.activeKid();
            }));
        }
        start.countDown();
        Set<String> seen = ConcurrentHashMap.newKeySet();
        for (Future<String> kid : kids) {
            seen.add(kid.get(30, TimeUnit.SECONDS));
        }
        pool.shutdown();
        assertThat(seen).hasSize(1);
        assertThat(keys.findByStatus(SigningKeyEntity.ACTIVE)).hasSize(1);
        assertThat(signing.activeKid()).isEqualTo(seen.iterator().next());
    }

    @Test
    @DisplayName("the private key is stored encrypted, never as its PKCS#8 bytes")
    void encryptedAtRest() {
        SigningKeyService.Material material = signing.activeMaterial();
        byte[] stored = jdbc.queryForObject("SELECT private_key FROM signing_key WHERE kid = ?", byte[].class,
                material.kid());
        byte[] pkcs8 = material.privateKey().getEncoded();
        assertThat(stored).isNotEqualTo(pkcs8);
        assertThat(indexOf(stored, java.util.Arrays.copyOfRange(pkcs8, pkcs8.length - 32, pkcs8.length)))
                .as("the raw private scalar does not appear in the stored bytes").isEqualTo(-1);
        byte[] payload = "manifest".getBytes(StandardCharsets.UTF_8);
        assertThat(JwsVerifier.verify(payload, signer.signDetached(payload, "application/json"),
                material.publicKey())).isTrue();
    }

    @Test
    @DisplayName("JWKS: OKP Ed25519 keys whose kid is the RFC 7638 thumbprint of x")
    @SuppressWarnings("unchecked")
    void jwksShape() {
        signing.activeKid();
        List<Map<String, Object>> published = (List<Map<String, Object>>) signing.jwks().get("keys");
        assertThat(published).hasSize(1);
        Map<String, Object> jwk = published.getFirst();
        assertThat(jwk).containsEntry("kty", "OKP").containsEntry("crv", "Ed25519").containsEntry("use", "sig")
                .containsEntry("alg", "Ed25519");
        byte[] x = Base64.getUrlDecoder().decode((String) jwk.get("x"));
        assertThat(x).hasSize(32);
        assertThat(jwk.get("kid")).isEqualTo(Jwk.thumbprint(x)).isEqualTo(signing.activeKid());
    }

    @Test
    @DisplayName("rotation: one active key, the retired key still published and verifying, an audit event")
    @SuppressWarnings("unchecked")
    void rotation() {
        byte[] payload = "briefing".getBytes(StandardCharsets.UTF_8);
        String before = signer.signDetached(payload, "text/markdown");
        String oldKid = JwsVerifier.kid(before);

        SigningKeyService.Key rotated = signing.rotate();

        assertThat(rotated.kid()).isNotEqualTo(oldKid);
        assertThat(keys.findByStatus(SigningKeyEntity.ACTIVE)).extracting(SigningKeyEntity::getKid)
                .containsExactly(rotated.kid());
        List<Map<String, Object>> published = (List<Map<String, Object>>) signing.jwks().get("keys");
        assertThat(published).extracting(k -> k.get("kid")).contains(oldKid, rotated.kid());
        assertThat(JwsVerifier.verify(payload, before, signing.publicKeys().get(oldKid))).isTrue();
        assertThat(JwsVerifier.kid(signer.signDetached(payload, "text/markdown"))).isEqualTo(rotated.kid());

        audit.flush();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM audit_event WHERE target_type = ? AND target_id = ?",
                Long.class, SigningKeyService.AUDIT_TARGET, rotated.kid())).isEqualTo(1L);
    }

    private static int indexOf(byte[] haystack, byte[] needle) {
        outer:
        for (int i = 0; i + needle.length <= haystack.length; i++) {
            for (int j = 0; j < needle.length; j++) {
                if (haystack[i + j] != needle[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
}
