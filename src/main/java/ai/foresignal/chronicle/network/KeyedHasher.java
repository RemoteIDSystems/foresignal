package ai.foresignal.chronicle.network;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Keyed hashing of global evidence keys (prompt 134): {@code HMAC-SHA256(salt, key)}, truncated to its first 64 bits
 * (big-endian). Two parties that hold the same salt derive the same value; nobody without the salt can test a guess.
 * The 64-bit value is what sketches and key sets hold; collisions are negligible at chronicle sizes.
 */
public final class KeyedHasher {

    private final byte[] salt;

    /**
     * Creates a hasher.
     *
     * @param salt the salt (at least 16 bytes)
     * @throws IllegalArgumentException when the salt is shorter
     */
    public KeyedHasher(byte[] salt) {
        if (salt == null || salt.length < 16) {
            throw new IllegalArgumentException("a discovery salt has at least 16 bytes");
        }
        this.salt = Arrays.copyOf(salt, salt.length);
    }

    /** The 64-bit keyed hash of a key. */
    public long hash(String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(salt, "HmacSHA256"));
            byte[] digest = mac.doFinal(key.getBytes(StandardCharsets.UTF_8));
            return ByteBuffer.wrap(digest, 0, 8).getLong();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HMAC-SHA256 is not available", e);
        }
    }
}
