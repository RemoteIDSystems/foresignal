package ai.foresignal.notify.push;

import java.security.GeneralSecurityException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * HKDF with HMAC-SHA-256 (RFC 5869), as Web Push encryption needs it (RFC 8291 §3.3, RFC 8188 §2.2).
 *
 * <p>Java 21 has no HKDF API (the {@code javax.crypto.KDF} API arrived as a preview in JDK 24 and
 * final in JDK 25), so this small class implements Extract and Expand on top of the JDK's
 * {@code HmacSHA256}. It is verified against the RFC 5869 SHA-256 test cases ({@code WebPushCryptoTest}).
 */
public final class Hkdf {

    /** HMAC-SHA-256 output length in bytes. */
    public static final int HASH_LENGTH = 32;

    private static final String HMAC = "HmacSHA256";

    private Hkdf() {}

    /**
     * HKDF-Extract: {@code PRK = HMAC-Hash(salt, IKM)}. An empty or null salt is a string of
     * {@link #HASH_LENGTH} zero bytes (RFC 5869 §2.2).
     */
    public static byte[] extract(byte[] salt, byte[] ikm) {
        byte[] key = salt == null || salt.length == 0 ? new byte[HASH_LENGTH] : salt;
        return hmac(key, ikm == null ? new byte[0] : ikm);
    }

    /**
     * HKDF-Expand: the first {@code length} bytes of {@code T(1) ‖ T(2) ‖ …} where
     * {@code T(i) = HMAC-Hash(PRK, T(i-1) ‖ info ‖ i)}.
     *
     * @throws IllegalArgumentException when {@code length} exceeds 255 × 32 bytes or is negative
     */
    public static byte[] expand(byte[] prk, byte[] info, int length) {
        if (length < 0 || length > 255 * HASH_LENGTH) {
            throw new IllegalArgumentException("HKDF-Expand length must be between 0 and " + 255 * HASH_LENGTH);
        }
        byte[] safeInfo = info == null ? new byte[0] : info;
        byte[] out = new byte[length];
        byte[] previous = new byte[0];
        int written = 0;
        for (int i = 1; written < length; i++) {
            byte[] input = new byte[previous.length + safeInfo.length + 1];
            System.arraycopy(previous, 0, input, 0, previous.length);
            System.arraycopy(safeInfo, 0, input, previous.length, safeInfo.length);
            input[input.length - 1] = (byte) i;
            previous = hmac(prk, input);
            int n = Math.min(previous.length, length - written);
            System.arraycopy(previous, 0, out, written, n);
            written += n;
        }
        return out;
    }

    /** Extract then Expand. */
    public static byte[] derive(byte[] salt, byte[] ikm, byte[] info, int length) {
        return expand(extract(salt, ikm), info, length);
    }

    private static byte[] hmac(byte[] key, byte[] data) {
        try {
            Mac mac = Mac.getInstance(HMAC);
            mac.init(new SecretKeySpec(key, HMAC));
            return mac.doFinal(data);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HmacSHA256 is not available: " + e.getMessage(), e);
        }
    }
}
