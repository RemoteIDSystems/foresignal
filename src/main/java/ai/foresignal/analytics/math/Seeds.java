package ai.foresignal.analytics.math;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Deterministic seeds (prompt 119): the first eight bytes of the SHA-256 of the parts, joined by a unit separator, so
 * a definition and a range always seed the same random stream (INT-1).
 */
public final class Seeds {

    private Seeds() {}

    /** The seed of {@code parts}. */
    public static long of(String... parts) {
        return ByteBuffer.wrap(digest(parts)).getLong();
    }

    /** The hexadecimal form of the seed of {@code parts} (16 characters), as results report it. */
    public static String hex(String... parts) {
        byte[] d = digest(parts);
        return HexFormat.of().formatHex(d, 0, 8);
    }

    /** The full SHA-256 of {@code parts}, hexadecimal. */
    public static String sha256(String... parts) {
        return HexFormat.of().formatHex(digest(parts));
    }

    private static byte[] digest(String... parts) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            for (int i = 0; i < parts.length; i++) {
                if (i > 0) {
                    md.update((byte) 0x1f);
                }
                md.update(String.valueOf(parts[i]).getBytes(StandardCharsets.UTF_8));
            }
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }
}
