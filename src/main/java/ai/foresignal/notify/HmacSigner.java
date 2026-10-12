package ai.foresignal.notify;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Signs generic webhook deliveries (Architecture §17, FR-37):
 * {@code X-Foresignal-Signature: sha256=<hex HMAC-SHA256(secret, "<timestamp>.<body>")>}.
 *
 * <p>A receiver recomputes the HMAC over the {@code X-Foresignal-Timestamp} header, a dot and the raw
 * body bytes, compares in constant time, and rejects timestamps far from its own clock.
 */
public final class HmacSigner {

    /** The signature header. */
    public static final String HEADER = "X-Foresignal-Signature";

    private static final String ALGORITHM = "HmacSHA256";

    private HmacSigner() {}

    /** {@code sha256=<hex>} of {@code timestamp + "." + body} under {@code secret}. */
    public static String sign(String secret, String timestamp, byte[] body) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM));
            mac.update((timestamp + ".").getBytes(StandardCharsets.UTF_8));
            mac.update(body);
            return "sha256=" + HexFormat.of().formatHex(mac.doFinal());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HMAC-SHA256 is unavailable", e);
        }
    }

    /** Raw hex HMAC-SHA256 of {@code message} under {@code key} (for known-answer tests). */
    public static String hex(String key, String message) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM));
            return HexFormat.of().formatHex(mac.doFinal(message.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HMAC-SHA256 is unavailable", e);
        }
    }
}
