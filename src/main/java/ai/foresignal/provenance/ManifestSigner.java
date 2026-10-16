package ai.foresignal.provenance;

import org.springframework.stereotype.Component;

/**
 * Signs an export/snapshot {@code manifest.json} (prompt 109), so prompt 111 only has to call
 * {@link #sign(byte[])}. The signature is a detached JWS over the exact manifest bytes with
 * {@code typ: application/json}.
 */
@Component
public class ManifestSigner {

    private final JwsSigner signer;

    /** Creates the signer. */
    public ManifestSigner(JwsSigner signer) {
        this.signer = signer;
    }

    /** The detached JWS of {@code manifestBytes}. */
    public String sign(byte[] manifestBytes) {
        return signer.signDetached(manifestBytes, "application/json");
    }
}
