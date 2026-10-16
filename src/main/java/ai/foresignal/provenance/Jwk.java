package ai.foresignal.provenance;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * OKP JWK helpers for Ed25519 (prompt 109; RFC 8037). The {@code kid} is the RFC 7638 thumbprint of
 * the canonical {@code {"crv","kty","x"}} JSON.
 */
public final class Jwk {

    private static final Base64.Encoder URL = Base64.getUrlEncoder().withoutPadding();

    private Jwk() {}

    /** The JWK of an Ed25519 public key ({@code x} is the raw 32-byte point). */
    public static Map<String, Object> of(String kid, byte[] x) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("kty", "OKP");
        out.put("crv", "Ed25519");
        out.put("x", URL.encodeToString(x));
        out.put("kid", kid);
        out.put("use", "sig");
        out.put("alg", "Ed25519");
        return out;
    }

    /** The RFC 7638 thumbprint of the OKP JWK, base64url. */
    public static String thumbprint(byte[] x) {
        String xb64 = URL.encodeToString(x);
        String canonical = "{\"crv\":\"Ed25519\",\"kty\":\"OKP\",\"x\":\"" + xb64 + "\"}";
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(canonical.getBytes(StandardCharsets.UTF_8));
            return URL.encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
