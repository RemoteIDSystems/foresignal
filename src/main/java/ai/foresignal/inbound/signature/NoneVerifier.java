package ai.foresignal.inbound.signature;

import ai.foresignal.inbound.format.IngestRequest;
import org.springframework.stereotype.Component;

/** The {@code none} scheme: no signature is checked (the ingest key alone authenticates). */
@Component
public class NoneVerifier implements SignatureVerifier {

    @Override
    public String scheme() {
        return VerifierConfig.NONE;
    }

    @Override
    public Verification verify(IngestRequest request, VerifierConfig config, String secret) {
        return Verification.VALID;
    }
}
