package ai.foresignal.inbound.signature;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** HMAC-SHA256 and constant-time comparison for the verifiers. */
final class Hmac {

    private static final String ALGORITHM = "HmacSHA256";

    private Hmac() {}

    /** HMAC-SHA256 of the concatenated {@code parts} under {@code key}. */
    static byte[] sha256(byte[] key, byte[]... parts) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(key, ALGORITHM));
            for (byte[] part : parts) {
                mac.update(part);
            }
            return mac.doFinal();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HMAC-SHA256 is unavailable", e);
        }
    }

    /** UTF-8 bytes. */
    static byte[] utf8(String text) {
        return text.getBytes(StandardCharsets.UTF_8);
    }

    /** Constant-time comparison of two strings' UTF-8 bytes. */
    static boolean equal(String a, String b) {
        return a != null && b != null && MessageDigest.isEqual(utf8(a), utf8(b));
    }

    /** Constant-time comparison of two byte arrays. */
    static boolean equal(byte[] a, byte[] b) {
        return a != null && b != null && MessageDigest.isEqual(a, b);
    }
}
