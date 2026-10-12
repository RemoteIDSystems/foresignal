package ai.foresignal.inbound.signature;

import ai.foresignal.inbound.format.IngestRequest;
import ai.foresignal.notify.HmacSigner;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import org.springframework.stereotype.Component;

/**
 * The {@code foresignal} scheme: exactly what Foresignal's own webhook channel sends
 * ({@link HmacSigner}), so one instance can push into another. {@code X-Foresignal-Timestamp}
 * plus {@code X-Foresignal-Signature: sha256=<hex HMAC-SHA256(secret, "<ts>.<body>")>}, with the
 * timestamp within ±5 minutes of the clock.
 */
@Component
public class ForesignalSignatureVerifier implements SignatureVerifier {

    /** The scheme name. */
    public static final String SCHEME = "foresignal";

    /** The timestamp header. */
    public static final String TIMESTAMP_HEADER = "X-Foresignal-Timestamp";

    /** How far the timestamp may be from the clock. */
    public static final Duration TOLERANCE = Duration.ofMinutes(5);

    private final Clock clock;

    /** Creates the verifier. */
    public ForesignalSignatureVerifier(Clock clock) {
        this.clock = clock;
    }

    @Override
    public String scheme() {
        return SCHEME;
    }

    @Override
    public Verification verify(IngestRequest request, VerifierConfig config, String secret) {
        String timestamp = request.header(TIMESTAMP_HEADER);
        String signature = request.header(HmacSigner.HEADER);
        if (timestamp == null || signature == null) {
            return Verification.MISSING;
        }
        if (secret == null) {
            return Verification.INVALID;
        }
        Instant sent = parse(timestamp.strip());
        if (sent == null || Duration.between(sent, clock.instant()).abs().compareTo(TOLERANCE) > 0) {
            return Verification.INVALID;
        }
        String expected = HmacSigner.sign(secret, timestamp, request.body());
        return Hmac.equal(expected, signature.strip().toLowerCase(Locale.ROOT)) ? Verification.VALID : Verification.INVALID;
    }

    /** The timestamp as epoch seconds or an ISO-8601 instant; null when neither. */
    static Instant parse(String timestamp) {
        try {
            return Instant.ofEpochSecond(Long.parseLong(timestamp));
        } catch (NumberFormatException e) {
            try {
                return Instant.parse(timestamp);
            } catch (DateTimeParseException again) {
                return null;
            }
        }
    }
}
