package ai.foresignal.federation;

import ai.foresignal.provenance.Jwk;
import ai.foresignal.provenance.JwsVerifier;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Remote signing keys (prompt 113): Ed25519 public keys from a peer's JWKS, their RFC 7638 thumbprints (the
 * {@code kid} 109 publishes) and detached-JWS verification against a pinned key.
 */
public final class PeerKeys {

    private static final Base64.Decoder URL = Base64.getUrlDecoder();

    /** The X.509 SubjectPublicKeyInfo prefix of an Ed25519 key (RFC 8410). */
    private static final byte[] ED25519_PREFIX = {0x30, 0x2a, 0x30, 0x05, 0x06, 0x03, 0x2b, 0x65, 0x70, 0x03, 0x21,
        0x00};

    private PeerKeys() {}

    /**
     * The outcome of a verification.
     *
     * @param signatureValid true when the JWS verifies with the key its {@code kid} names
     * @param kid the signing key's {@code kid}, or null
     * @param key the signing key's raw 32-byte point, or null
     * @param error why it failed, or null
     */
    public record Verification(boolean signatureValid, String kid, byte[] key, String error) {

        static Verification failed(String kid, String error) {
            return new Verification(false, kid, null, error);
        }
    }

    /** The Ed25519 public key of a raw 32-byte point. */
    public static PublicKey publicKey(byte[] x) {
        if (x == null || x.length != 32) {
            throw new IllegalArgumentException("an Ed25519 public key is 32 bytes");
        }
        byte[] der = new byte[ED25519_PREFIX.length + x.length];
        System.arraycopy(ED25519_PREFIX, 0, der, 0, ED25519_PREFIX.length);
        System.arraycopy(x, 0, der, ED25519_PREFIX.length, x.length);
        try {
            return KeyFactory.getInstance("Ed25519").generatePublic(new X509EncodedKeySpec(der));
        } catch (Exception e) {
            throw new IllegalArgumentException("not an Ed25519 public key", e);
        }
    }

    /**
     * Verifies {@code jws} over {@code body} with the key of a JWKS document: the JWK whose {@code kid} the JWS names
     * must be an OKP Ed25519 key whose RFC 7638 thumbprint equals that {@code kid}.
     */
    @SuppressWarnings("unchecked")
    public static Verification verifyWithJwks(byte[] body, String jws, Map<String, Object> jwks) {
        String kid = JwsVerifier.kid(jws);
        if (jws == null || kid == null) {
            return Verification.failed(null, "the response carries no Foresignal-Signature");
        }
        Object keys = jwks == null ? null : jwks.get("keys");
        if (!(keys instanceof List<?> list)) {
            return Verification.failed(kid, "the JWKS has no keys");
        }
        for (Object entry : list) {
            if (entry instanceof Map<?, ?> jwk && kid.equals(jwk.get("kid"))) {
                Object x = jwk.get("x");
                if (!"OKP".equals(jwk.get("kty")) || !"Ed25519".equals(jwk.get("crv")) || !(x instanceof String text)) {
                    return Verification.failed(kid, "the signing key is not an Ed25519 key");
                }
                byte[] raw;
                try {
                    raw = URL.decode(text);
                } catch (IllegalArgumentException e) {
                    return Verification.failed(kid, "the signing key is not base64url");
                }
                return verify(body, jws, raw);
            }
        }
        return Verification.failed(kid, "the JWKS does not hold the signing key " + kid);
    }

    /**
     * Verifies {@code jws} over {@code body} with the raw point {@code key}, whose thumbprint must equal the JWS's
     * {@code kid}.
     */
    public static Verification verify(byte[] body, String jws, byte[] key) {
        String kid = JwsVerifier.kid(jws);
        if (key == null || key.length != 32) {
            return Verification.failed(kid, "no usable key");
        }
        String thumbprint = Jwk.thumbprint(key);
        if (kid == null || !kid.equals(thumbprint)) {
            return Verification.failed(kid, "the key's thumbprint does not match the signature's kid");
        }
        PublicKey publicKey;
        try {
            publicKey = publicKey(key);
        } catch (IllegalArgumentException e) {
            return Verification.failed(kid, e.getMessage());
        }
        if (!JwsVerifier.verify(body, jws, publicKey)) {
            return Verification.failed(kid, "the signature does not verify");
        }
        return new Verification(true, kid, key.clone(), null);
    }

    /** The raw point of a base64url-encoded key, or null when it does not decode to 32 bytes. */
    public static byte[] decodeKey(String base64url) {
        if (base64url == null || base64url.isBlank()) {
            return null;
        }
        try {
            byte[] raw = URL.decode(base64url.strip());
            return raw.length == 32 ? raw : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
