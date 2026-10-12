package ai.foresignal.inbound.signature;

import ai.foresignal.inbound.format.IngestRequest;

/**
 * Verifies one webhook signature scheme over a raw request (prompt 024). Implementations are
 * Spring beans collected by {@link SignatureVerifiers}.
 */
public interface SignatureVerifier {

    /** The scheme name, as stored in {@link VerifierConfig#scheme()}. */
    String scheme();

    /**
     * Checks {@code request}.
     *
     * @param request the raw request (body bytes exactly as received)
     * @param config the source's verifier configuration for the format
     * @param secret the decrypted shared secret; may be null for {@code none}
     * @return whether the signature verified
     */
    Verification verify(IngestRequest request, VerifierConfig config, String secret);
}
