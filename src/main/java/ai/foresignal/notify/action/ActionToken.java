package ai.foresignal.notify.action;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * The capability token of a notification action (prompt 033).
 *
 * <p>Format: {@code eac_<22 chars>.<11 chars>}, where the first part is base64url (no padding) of 16
 * random bytes and the second is base64url of the first 8 bytes of
 * {@code HMAC-SHA256(k, randomBytes)}. 38 characters in all, so a token fits Telegram's 64-byte
 * {@code callback_data} with the {@code ea:} prefix.
 *
 * <p>{@link #verify} checks the MAC in constant time ({@link MessageDigest#isEqual}) <em>before</em>
 * any database lookup, so a forged or mistyped token costs one HMAC and never a query. Only the
 * SHA-256 of a token is stored ({@link #hash}).
 */
public final class ActionToken {

    /** The token prefix. */
    public static final String PREFIX = "eac_";

    /** Random bytes per token. */
    public static final int RANDOM_BYTES = 16;

    /** MAC bytes kept per token. */
    public static final int MAC_BYTES = 8;

    /** Characters of the random part. */
    static final int RANDOM_CHARS = 22;

    /** Characters of the MAC part. */
    static final int MAC_CHARS = 11;

    /** The exact length of a token. */
    public static final int LENGTH = PREFIX.length() + RANDOM_CHARS + 1 + MAC_CHARS;

    private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder UNB64 = Base64.getUrlDecoder();
    private static final SecureRandom RANDOM = new SecureRandom();

    private ActionToken() {}

    /** A new token under {@code key}. */
    public static String mint(byte[] key) {
        byte[] random = new byte[RANDOM_BYTES];
        RANDOM.nextBytes(random);
        return format(key, random);
    }

    /** The token of {@code random} under {@code key}. */
    static String format(byte[] key, byte[] random) {
        return PREFIX + B64.encodeToString(random) + "." + B64.encodeToString(mac(key, random));
    }

    /**
     * True when {@code token} is well formed and its MAC matches under {@code key}. Runs no query;
     * the MAC comparison is constant time.
     */
    public static boolean verify(byte[] key, String token) {
        Optional<byte[][]> parts = parse(token);
        if (parts.isEmpty()) {
            return false;
        }
        byte[] expected = mac(key, parts.get()[0]);
        return MessageDigest.isEqual(expected, parts.get()[1]);
    }

    /** The random and MAC bytes of a well-formed token. */
    static Optional<byte[][]> parse(String token) {
        if (token == null || token.length() != LENGTH || !token.startsWith(PREFIX)
                || token.charAt(PREFIX.length() + RANDOM_CHARS) != '.') {
            return Optional.empty();
        }
        String random = token.substring(PREFIX.length(), PREFIX.length() + RANDOM_CHARS);
        String mac = token.substring(PREFIX.length() + RANDOM_CHARS + 1);
        try {
            byte[] r = UNB64.decode(random);
            byte[] m = UNB64.decode(mac);
            if (r.length != RANDOM_BYTES || m.length != MAC_BYTES) {
                return Optional.empty();
            }
            // Reject non-canonical encodings, so one capability has exactly one spelling.
            if (!B64.encodeToString(r).equals(random) || !B64.encodeToString(m).equals(mac)) {
                return Optional.empty();
            }
            return Optional.of(new byte[][] {r, m});
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /** The stored form of a token: lowercase hex SHA-256 of its UTF-8 bytes. */
    public static String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    private static byte[] mac(byte[] key, byte[] random) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return Arrays.copyOf(mac.doFinal(random), MAC_BYTES);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("HMAC-SHA256 is unavailable", e);
        }
    }
}
