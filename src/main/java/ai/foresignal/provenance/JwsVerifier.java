package ai.foresignal.provenance;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;
import java.util.Map;

/**
 * Verifies a detached JWS (prompt 109) against a public key from the JWKS. Used by the tests and by
 * prompt 113's peer pinning.
 */
public final class JwsVerifier {

    private static final Base64.Decoder URL = Base64.getUrlDecoder();

    private JwsVerifier() {}

    /**
     * Verifies {@code jws} over {@code payload} with {@code key}.
     *
     * @return true when the signature is valid
     */
    public static boolean verify(byte[] payload, String jws, PublicKey key) {
        if (jws == null) {
            return false;
        }
        int dot = jws.indexOf('.');
        int secondDot = dot < 0 ? -1 : jws.indexOf('.', dot + 1);
        if (dot <= 0 || secondDot != dot + 1) {
            return false;
        }
        String headerB64 = jws.substring(0, dot);
        String signatureB64 = jws.substring(secondDot + 1);
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

    /** The {@code kid} of a detached JWS header, or null. */
    public static String kid(String jws) {
        int dot = jws == null ? -1 : jws.indexOf('.');
        if (dot <= 0) {
            return null;
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> header = tools.jackson.databind.json.JsonMapper.builder().build()
                    .readValue(URL.decode(jws.substring(0, dot)), Map.class);
            Object kid = header.get("kid");
            return kid == null ? null : String.valueOf(kid);
        } catch (RuntimeException e) {
            return null;
        }
    }
}
