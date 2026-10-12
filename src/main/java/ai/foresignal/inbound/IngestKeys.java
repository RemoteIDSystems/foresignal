package ai.foresignal.inbound;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import org.springframework.stereotype.Component;

/**
 * Generates and hashes ingest keys ({@code eik_} + base64url of 32 random bytes, prompt 024).
 *
 * <p>Only the SHA-256 and the first 8 characters are stored; the key itself is shown once.
 */
@Component
public class IngestKeys {

    /** Every ingest key starts with this. */
    public static final String PREFIX = "eik_";

    /** Stored prefix length. */
    public static final int PREFIX_LENGTH = 8;

    private static final int ENTROPY_BYTES = 32;

    private final SecureRandom random = new SecureRandom();

    /** A new key. */
    public String generate() {
        return generate(PREFIX);
    }

    /** A new secret with {@code prefix} (also used for {@code fsx_} registration tokens). */
    public String generate(String prefix) {
        byte[] entropy = new byte[ENTROPY_BYTES];
        random.nextBytes(entropy);
        return prefix + Base64.getUrlEncoder().withoutPadding().encodeToString(entropy);
    }

    /** The lowercase hex SHA-256 of {@code key}. */
    public String hash(String key) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(key.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    /** The stored prefix of {@code key}. */
    public static String prefixOf(String key) {
        return key.length() <= PREFIX_LENGTH ? key : key.substring(0, PREFIX_LENGTH);
    }

    /** Constant-time comparison of a presented key with a stored hash. */
    public boolean matches(String presented, String storedHash) {
        if (presented == null || storedHash == null) {
            return false;
        }
        return MessageDigest.isEqual(hash(presented).getBytes(StandardCharsets.UTF_8),
                storedHash.getBytes(StandardCharsets.UTF_8));
    }
}
