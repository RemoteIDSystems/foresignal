package ai.foresignal.inbound.signature;

import ai.foresignal.inbound.format.IngestRequest;
import java.util.HexFormat;
import java.util.Locale;
import org.springframework.stereotype.Component;

/**
 * The {@code hmac-sha256} scheme: a header (default {@code X-Signature-256}) carrying
 * {@code sha256=<hex>} of HMAC-SHA256(secret, raw body), as GitHub-style webhooks send it.
 */
@Component
public class HmacSha256Verifier implements SignatureVerifier {

    /** The scheme name. */
    public static final String SCHEME = "hmac-sha256";

    /** The default signature header. */
    public static final String DEFAULT_HEADER = "X-Signature-256";

    @Override
    public String scheme() {
        return SCHEME;
    }

    @Override
    public Verification verify(IngestRequest request, VerifierConfig config, String secret) {
        String header = config.header() == null || config.header().isBlank() ? DEFAULT_HEADER : config.header();
        String value = request.header(header);
        if (value == null || value.isBlank()) {
            return Verification.MISSING;
        }
        if (secret == null) {
            return Verification.INVALID;
        }
        String expected = "sha256=" + HexFormat.of().formatHex(Hmac.sha256(Hmac.utf8(secret), request.body()));
        return Hmac.equal(expected, value.strip().toLowerCase(Locale.ROOT)) ? Verification.VALID : Verification.INVALID;
    }
}
