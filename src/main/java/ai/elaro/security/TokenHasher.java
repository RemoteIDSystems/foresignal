package ai.elaro.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import org.springframework.stereotype.Component;

/**
 * Generates and hashes bearer tokens.
 *
 * <p>Only the SHA-256 hash of a token is ever stored (Architecture §15, NFR-7), and comparison is
 * constant-time so a timing signal cannot be used to guess a token byte by byte.
 *
 * <p>Deliberately separate from {@code AdminTokenService} so prompt 019 can reuse it unchanged when
 * tokens move into the {@code api_token} table.
 */
@Component
public class TokenHasher {

    /** Every Elaro token starts with this, so a leaked string is recognisable as a credential. */
    public static final String TOKEN_PREFIX = "elr_";

    static final int TOKEN_ENTROPY_BYTES = 32;
    static final String HASH_ALGORITHM = "SHA-256";

    private final SecureRandom random = new SecureRandom();

    /** A new token: {@value #TOKEN_PREFIX} followed by 32 random bytes, base64url, unpadded. */
    public String generateToken() {
        byte[] entropy = new byte[TOKEN_ENTROPY_BYTES];
        random.nextBytes(entropy);
        return TOKEN_PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(entropy);
    }

    /** The lowercase hex SHA-256 of {@code token}, which is what gets stored. */
    public String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is required of every JVM.
            throw new IllegalStateException(HASH_ALGORITHM + " is not available", e);
        }
    }

    /**
     * True when {@code presentedToken} hashes to {@code expectedHash}.
     *
     * <p>Uses {@link MessageDigest#isEqual} rather than {@code String.equals} to keep the
     * comparison constant-time.
     */
    public boolean matches(String presentedToken, String expectedHash) {
        if (presentedToken == null || expectedHash == null) {
            return false;
        }
        return MessageDigest.isEqual(
                hash(presentedToken).getBytes(StandardCharsets.UTF_8),
                expectedHash.getBytes(StandardCharsets.UTF_8));
    }
}
