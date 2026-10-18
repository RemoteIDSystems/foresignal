package ai.foresignal.exchange;

import ai.foresignal.federation.PeerKeys;
import ai.foresignal.provenance.JwsVerifier;
import java.util.Map;

/**
 * Verifies the detached JWS of a package manifest (prompt 132): against a sender's JWKS (the JWK whose {@code kid}
 * the signature names must be an Ed25519 key whose RFC 7638 thumbprint is that {@code kid}) or against a single pinned
 * key. The header rules of 109's {@link JwsVerifier} apply ({@code alg} Ed25519, {@code b64:false},
 * {@code crit:["b64"]}), so {@code alg:none} and tampered manifests fail.
 */
public final class PackageVerifier {

    /** A valid signature. */
    public static final String VALID = "valid";

    /** A signature that does not verify. */
    public static final String INVALID = "invalid";

    /** No key was available for the signature's {@code kid}. */
    public static final String UNKNOWN_KEY = "unknown-key";

    private PackageVerifier() {}

    /**
     * What a verification found.
     *
     * @param status {@value #VALID}, {@value #INVALID} or {@value #UNKNOWN_KEY}
     * @param kid the signature's {@code kid}, or null
     * @param error why it failed, or null
     */
    public record Result(String status, String kid, String error) {

        /** True for a valid signature. */
        public boolean valid() {
            return VALID.equals(status);
        }
    }

    /** The {@code kid} of a detached JWS, or null. */
    public static String kid(String jws) {
        return JwsVerifier.kid(jws);
    }

    /** True when {@code jwks} holds a key with {@code kid}. */
    public static boolean holds(Map<String, Object> jwks, String kid) {
        if (jwks == null || kid == null || !(jwks.get("keys") instanceof java.util.List<?> keys)) {
            return false;
        }
        for (Object k : keys) {
            if (k instanceof Map<?, ?> jwk && kid.equals(jwk.get("kid"))) {
                return true;
            }
        }
        return false;
    }

    /** Verifies {@code jws} over {@code manifest} with the key of {@code jwks} that it names. */
    public static Result verify(byte[] manifest, String jws, Map<String, Object> jwks) {
        String kid = kid(jws);
        if (kid == null) {
            return new Result(INVALID, null, "the signature has no kid");
        }
        if (!holds(jwks, kid)) {
            return new Result(UNKNOWN_KEY, kid, "no key " + kid + " is known for the sender");
        }
        PeerKeys.Verification v = PeerKeys.verifyWithJwks(manifest, jws, jwks);
        return v.signatureValid() ? new Result(VALID, kid, null) : new Result(INVALID, kid, v.error());
    }

    /** Verifies {@code jws} over {@code manifest} with a pinned raw Ed25519 key. */
    public static Result verifyPinned(byte[] manifest, String jws, byte[] key) {
        PeerKeys.Verification v = PeerKeys.verify(manifest, jws, key);
        return v.signatureValid() ? new Result(VALID, v.kid(), null) : new Result(INVALID, v.kid(), v.error());
    }
}
