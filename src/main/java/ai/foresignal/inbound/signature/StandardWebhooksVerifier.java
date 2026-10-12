package ai.foresignal.inbound.signature;

import ai.foresignal.inbound.format.IngestRequest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * The {@code standard-webhooks} scheme (https://www.standardwebhooks.com): headers
 * {@code webhook-id}, {@code webhook-timestamp} (epoch seconds) and {@code webhook-signature}
 * (space-separated {@code v1,<base64>} entries) over {@code id.timestamp.body}.
 *
 * <p>The timestamp must be within ±5 minutes of the clock, and a {@code webhook-id} already
 * accepted within the last 10 minutes is refused as a replay. The secret is {@code whsec_<base64>}
 * (decoded) or, without that prefix, its raw UTF-8 bytes.
 */
@Component
public class StandardWebhooksVerifier implements SignatureVerifier {

    /** The scheme name. */
    public static final String SCHEME = "standard-webhooks";

    /** How far the timestamp may be from the clock. */
    public static final Duration TOLERANCE = Duration.ofMinutes(5);

    /** How long an accepted id is remembered. */
    public static final Duration REPLAY_WINDOW = Duration.ofMinutes(10);

    /** The most ids remembered at once; older entries are evicted beyond it. */
    static final int MAX_REMEMBERED = 100_000;

    private final Clock clock;
    private final Map<String, Instant> seen = new ConcurrentHashMap<>();

    /** Creates the verifier. */
    public StandardWebhooksVerifier(Clock clock) {
        this.clock = clock;
    }

    @Override
    public String scheme() {
        return SCHEME;
    }

    @Override
    public Verification verify(IngestRequest request, VerifierConfig config, String secret) {
        String id = request.header("webhook-id");
        String timestamp = request.header("webhook-timestamp");
        String signatures = request.header("webhook-signature");
        if (id == null || timestamp == null || signatures == null) {
            return Verification.MISSING;
        }
        if (secret == null) {
            return Verification.INVALID;
        }
        Instant now = clock.instant();
        Instant sent;
        try {
            sent = Instant.ofEpochSecond(Long.parseLong(timestamp.strip()));
        } catch (NumberFormatException e) {
            return Verification.INVALID;
        }
        if (Duration.between(sent, now).abs().compareTo(TOLERANCE) > 0) {
            return Verification.INVALID;
        }
        byte[] key = key(secret);
        if (key == null) {
            return Verification.INVALID;
        }
        String expected = Base64.getEncoder().encodeToString(
                Hmac.sha256(key, Hmac.utf8(id + "." + timestamp.strip() + "."), request.body()));
        boolean matched = false;
        for (String entry : signatures.strip().split("\\s+")) {
            int comma = entry.indexOf(',');
            if (comma > 0 && "v1".equals(entry.substring(0, comma)) && Hmac.equal(expected, entry.substring(comma + 1))) {
                matched = true;
            }
        }
        if (!matched) {
            return Verification.INVALID;
        }
        prune(now);
        Instant previous = seen.putIfAbsent(id, now);
        if (previous != null && Duration.between(previous, now).compareTo(REPLAY_WINDOW) < 0) {
            return Verification.INVALID;
        }
        if (previous != null) {
            seen.put(id, now);
        }
        return Verification.VALID;
    }

    private static byte[] key(String secret) {
        if (secret.startsWith("whsec_")) {
            try {
                return Base64.getDecoder().decode(secret.substring("whsec_".length()));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return Hmac.utf8(secret);
    }

    private void prune(Instant now) {
        Instant cutoff = now.minus(REPLAY_WINDOW);
        if (seen.size() < MAX_REMEMBERED / 2) {
            seen.values().removeIf(at -> at.isBefore(cutoff));
            return;
        }
        Iterator<Map.Entry<String, Instant>> it = seen.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Instant> entry = it.next();
            if (entry.getValue().isBefore(cutoff) || seen.size() >= MAX_REMEMBERED) {
                it.remove();
            }
        }
    }
}
