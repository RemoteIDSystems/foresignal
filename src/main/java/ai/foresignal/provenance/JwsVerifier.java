package ai.foresignal.provenance;

import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Verifies a detached JWS (prompt 109; RFC 7515 Appendix F, RFC 7797) against a public key from the JWKS.
 * Used by the tests and by prompt 113's peer pinning.
 *
 * <p>The protected header is checked before the signature (RFC 7515 §4.1.11, RFC 7797 §6): {@code alg} must be
 * {@code Ed25519} (RFC 9864) or the deprecated {@code EdDSA} that {@code signing.legacyAlg} emits; {@code b64}
 * must be {@code false} and {@code crit} must be exactly {@code ["b64"]}, because the signing input is built from
 * the unencoded payload and a verifier must refuse critical parameters it does not understand. Anything else,
 * including {@code "alg":"none"}, is refused.
 */
public final class JwsVerifier {

    private static final Base64.Decoder URL = Base64.getUrlDecoder();

    private static final tools.jackson.databind.json.JsonMapper JSON =
            tools.jackson.databind.json.JsonMapper.builder().build();

    /** The {@code alg} values accepted: RFC 9864's fully specified name and the deprecated legacy one. */
    static final Set<String> ALGORITHMS = Set.of(SigningKeyService.ALG, "EdDSA");

    private JwsVerifier() {}

    /**
     * Verifies {@code jws} over {@code payload} with {@code key}.
     *
     * @return true when the header is acceptable and the signature is valid
     */
    public static boolean verify(byte[] payload, String jws, PublicKey key) {
        if (jws == null || payload == null || key == null) {
            return false;
        }
        int dot = jws.indexOf('.');
        int secondDot = dot < 0 ? -1 : jws.indexOf('.', dot + 1);
        if (dot <= 0 || secondDot != dot + 1 || jws.indexOf('.', secondDot + 1) >= 0) {
            return false;
        }
        String headerB64 = jws.substring(0, dot);
        String signatureB64 = jws.substring(secondDot + 1);
        Map<String, Object> header = header(headerB64);
        if (!acceptable(header)) {
            return false;
        }
        try {
            byte[] signature = URL.decode(signatureB64);
            Signature verifier = Signature.getInstance(SigningKeyService.ALG);
            verifier.initVerify(key);
            verifier.update(JwsSigner.signingInput(headerB64, payload));
            return verifier.verify(signature);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * True when a protected header is one this verifier understands: an accepted {@code alg}, {@code b64:false}
     * and {@code crit} exactly {@code ["b64"]}.
     */
    static boolean acceptable(Map<String, Object> header) {
        if (header == null) {
            return false;
        }
        Object alg = header.get("alg");
        if (!(alg instanceof String name) || !ALGORITHMS.contains(name)) {
            return false;
        }
        if (!Boolean.FALSE.equals(header.get("b64"))) {
            return false;
        }
        return header.get("crit") instanceof List<?> crit && crit.equals(List.of("b64"));
    }

    /** The {@code kid} of a detached JWS header, or null. */
    public static String kid(String jws) {
        int dot = jws == null ? -1 : jws.indexOf('.');
        if (dot <= 0) {
            return null;
        }
        Map<String, Object> header = header(jws.substring(0, dot));
        Object kid = header == null ? null : header.get("kid");
        return kid == null ? null : String.valueOf(kid);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> header(String headerB64) {
        try {
            return JSON.readValue(URL.decode(headerB64), Map.class);
        } catch (RuntimeException e) {
            return null;
        }
    }
}
