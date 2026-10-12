package ai.foresignal.inbound.signature;

/**
 * The non-secret part of one format's signature verifier on a custom source (OpenAPI
 * {@code SignatureVerifierConfig}); the secret is stored encrypted and passed separately.
 *
 * @param scheme {@code none}, {@code hmac-sha256}, {@code standard-webhooks} or {@code foresignal}
 * @param header the signature header for {@code hmac-sha256}; null means {@code X-Signature-256}
 * @param keyOptional true when a valid signature alone authenticates the request
 */
public record VerifierConfig(String scheme, String header, boolean keyOptional) {

    /** The scheme that verifies nothing. */
    public static final String NONE = "none";

    /** True when this verifier checks a signature (any scheme but {@code none}). */
    public boolean active() {
        return scheme != null && !NONE.equals(scheme);
    }
}
