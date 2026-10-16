package ai.foresignal.provenance;

import ai.foresignal.security.CredentialCipher;
import ai.foresignal.security.Encrypted;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The instance Ed25519 signing key (prompt 109): generated once, stored encrypted, published as a
 * JWKS and rotated on demand. Retired keys are kept for {@code signing.retainRetired} (default
 * 365 d) so old signatures stay verifiable.
 */
@Service
public class SigningKeyService {

    private static final Logger log = LoggerFactory.getLogger(SigningKeyService.class);

    /** The AES-GCM nonce length CredentialCipher uses. */
    private static final int NONCE_BYTES = 12;

    /** The algorithm (RFC 9864). */
    public static final String ALG = "Ed25519";

    private final SigningKeyRepository keys;
    private final CredentialCipher cipher;
    private final Clock clock;
    private final Duration retainRetired;

    /** Creates the service. */
    public SigningKeyService(SigningKeyRepository keys, CredentialCipher cipher, Clock clock,
            @Value("${foresignal.signing.retain-retired:P365D}") Duration retainRetired) {
        this.keys = keys;
        this.cipher = cipher;
        this.clock = clock;
        this.retainRetired = retainRetired == null ? Duration.ofDays(365) : retainRetired;
    }

    /** An in-memory key. */
    public record Key(String kid, PublicKey publicKey, byte[] x) {}

    /** The active key, generated on first use. */
    @Transactional
    public Key active() {
        SigningKeyEntity entity = keys.findFirstByStatusOrderByCreatedAtDesc(SigningKeyEntity.ACTIVE)
                .orElseGet(() -> generate());
        return toKey(entity);
    }

    /** The active key's plaintext, decrypted, for signing. */
    @Transactional
    public PrivateKey activePrivate() {
        SigningKeyEntity entity = keys.findFirstByStatusOrderByCreatedAtDesc(SigningKeyEntity.ACTIVE)
                .orElseGet(() -> generate());
        return privateKey(entity);
    }

    /** The active key's {@code kid}. */
    @Transactional
    public String activeKid() {
        return active().kid();
    }

    /** The JWKS document: active and retired keys within {@code retainRetired}, newest first. */
    @Transactional
    public Map<String, Object> jwks() {
        active();
        List<Map<String, Object>> out = new ArrayList<>();
        Instant cutoff = clock.instant().minus(retainRetired);
        for (SigningKeyEntity entity : keys.findAll()) {
            if (SigningKeyEntity.RETIRED.equals(entity.getStatus())
                    && entity.getRetiredAt() != null && entity.getRetiredAt().isBefore(cutoff)) {
                continue;
            }
            out.add(Jwk.of(entity.getKid(), entity.getPublicKey()));
        }
        return Map.of("keys", out);
    }

    /** The public keys, for a verifier. */
    @Transactional
    public Map<String, PublicKey> publicKeys() {
        Map<String, PublicKey> out = new java.util.LinkedHashMap<>();
        for (SigningKeyEntity entity : keys.findAll()) {
            out.put(entity.getKid(), publicKey(entity));
        }
        return out;
    }

    /** Retires the active key and activates a new one. */
    @Transactional
    public Key rotate() {
        keys.findFirstByStatusOrderByCreatedAtDesc(SigningKeyEntity.ACTIVE).ifPresent(entity -> {
            entity.setStatus(SigningKeyEntity.RETIRED);
            entity.setRetiredAt(clock.instant());
            keys.save(entity);
        });
        return toKey(generate());
    }

    /** Retires the active key and returns the new key's metadata ({@code SigningKeyInfo}). */
    @Transactional
    public Map<String, Object> rotateView() {
        rotate();
        SigningKeyEntity active = keys.findFirstByStatusOrderByCreatedAtDesc(SigningKeyEntity.ACTIVE)
                .orElseThrow(() -> new IllegalStateException("No active signing key"));
        return info(active);
    }

    /** The active key's metadata ({@code SigningKeyInfo}). */
    @Transactional
    public Map<String, Object> activeView() {
        active();
        SigningKeyEntity entity = keys.findFirstByStatusOrderByCreatedAtDesc(SigningKeyEntity.ACTIVE)
                .orElseThrow(() -> new IllegalStateException("No active signing key"));
        return info(entity);
    }

    private Map<String, Object> info(SigningKeyEntity entity) {
        Map<String, Object> out = new java.util.LinkedHashMap<>();
        out.put("kid", entity.getKid());
        out.put("alg", ALG);
        out.put("status", entity.getStatus());
        out.put("createdAt", entity.getCreatedAt().toString());
        out.put("retiredAt", entity.getRetiredAt() == null ? null : entity.getRetiredAt().toString());
        out.put("removeAfter", entity.getRetiredAt() == null ? null
                : entity.getRetiredAt().plus(retainRetired).toString());
        out.put("jwk", Jwk.of(entity.getKid(), entity.getPublicKey()));
        return out;
    }

    private SigningKeyEntity generate() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(ALG);
            KeyPair pair = generator.generateKeyPair();
            byte[] x = rawPublic(pair.getPublic());
            String kid = Jwk.thumbprint(x);
            Encrypted encrypted = cipher.encrypt(pair.getPrivate().getEncoded());
            byte[] stored = new byte[encrypted.nonce().length + encrypted.ciphertext().length];
            System.arraycopy(encrypted.nonce(), 0, stored, 0, encrypted.nonce().length);
            System.arraycopy(encrypted.ciphertext(), 0, stored, encrypted.nonce().length, encrypted.ciphertext().length);
            SigningKeyEntity entity = new SigningKeyEntity(kid, stored, x, clock.instant());
            return keys.saveAndFlush(entity);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Ed25519 is not available", e);
        }
    }

    private PrivateKey privateKey(SigningKeyEntity entity) {
        byte[] stored = entity.getPrivateKey();
        byte[] nonce = java.util.Arrays.copyOfRange(stored, 0, NONCE_BYTES);
        byte[] ciphertext = java.util.Arrays.copyOfRange(stored, NONCE_BYTES, stored.length);
        byte[] pkcs8 = cipher.decrypt(new Encrypted(ciphertext, nonce));
        try {
            return KeyFactory.getInstance(ALG).generatePrivate(new PKCS8EncodedKeySpec(pkcs8));
        } catch (Exception e) {
            throw new IllegalStateException("Could not rebuild the signing key", e);
        }
    }

    private PublicKey publicKey(SigningKeyEntity entity) {
        try {
            // Rebuild an X.509 SubjectPublicKeyInfo from the raw point.
            byte[] x509 = x509(entity.getPublicKey());
            return KeyFactory.getInstance(ALG).generatePublic(new X509EncodedKeySpec(x509));
        } catch (Exception e) {
            log.warn("Could not rebuild the verification key {}", entity.getKid(), e);
            throw new IllegalStateException("Could not rebuild the signing key", e);
        }
    }

    private static Key toKey(SigningKeyEntity entity) {
        try {
            return new Key(entity.getKid(),
                    KeyFactory.getInstance(ALG).generatePublic(new X509EncodedKeySpec(x509(entity.getPublicKey()))),
                    entity.getPublicKey());
        } catch (Exception e) {
            throw new IllegalStateException("Could not rebuild the signing key", e);
        }
    }

    /** Wraps a raw 32-byte Ed25519 point in the X.509 SubjectPublicKeyInfo DER prefix. */
    private static byte[] x509(byte[] raw) {
        byte[] prefix = {0x30, 0x2a, 0x30, 0x05, 0x06, 0x03, 0x2b, 0x65, 0x70, 0x03, 0x21, 0x00};
        byte[] out = new byte[prefix.length + raw.length];
        System.arraycopy(prefix, 0, out, 0, prefix.length);
        System.arraycopy(raw, 0, out, prefix.length, raw.length);
        return out;
    }

    /** The raw 32-byte point of an Ed25519 public key (the tail of its X.509 encoding). */
    private static byte[] rawPublic(PublicKey publicKey) {
        byte[] encoded = publicKey.getEncoded();
        return java.util.Arrays.copyOfRange(encoded, encoded.length - 32, encoded.length);
    }
}
