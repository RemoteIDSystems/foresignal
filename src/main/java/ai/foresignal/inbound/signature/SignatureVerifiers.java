package ai.foresignal.inbound.signature;

import ai.foresignal.inbound.format.IngestRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Every {@link SignatureVerifier}, by scheme (prompt 024).
 */
@Component
public class SignatureVerifiers {

    private final Map<String, SignatureVerifier> byScheme = new LinkedHashMap<>();

    /**
     * Collects the verifier beans.
     *
     * @throws IllegalStateException on a duplicate scheme
     */
    public SignatureVerifiers(List<SignatureVerifier> verifiers) {
        for (SignatureVerifier verifier : verifiers) {
            if (byScheme.putIfAbsent(verifier.scheme(), verifier) != null) {
                throw new IllegalStateException("Duplicate signature scheme '" + verifier.scheme() + "'");
            }
        }
    }

    /**
     * Checks {@code request} under {@code config}.
     *
     * @return {@link Verification#VALID} for no config or {@code none}; {@link Verification#INVALID}
     *     for an unknown scheme
     */
    public Verification verify(VerifierConfig config, String secret, IngestRequest request) {
        if (config == null || !config.active()) {
            return Verification.VALID;
        }
        SignatureVerifier verifier = byScheme.get(config.scheme());
        if (verifier == null) {
            return Verification.INVALID;
        }
        return verifier.verify(request, config, secret);
    }

    /** True when {@code scheme} is known. */
    public boolean supports(String scheme) {
        return scheme != null && byScheme.containsKey(scheme);
    }

    /** The known schemes, sorted. */
    public List<String> schemes() {
        List<String> schemes = new ArrayList<>(byScheme.keySet());
        Collections.sort(schemes);
        return schemes;
    }
}
