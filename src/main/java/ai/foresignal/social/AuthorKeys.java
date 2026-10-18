package ai.foresignal.social;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * The {@code authorKey} of a post (prompt 135): HMAC-SHA256 of {@code {platform}:{accountId}} with the instance social
 * salt, as 64 lower-case hex characters. Counting distinct authors and coordination statistics use it, so neither
 * needs the handle; the same account on another instance has another key.
 */
public final class AuthorKeys {

    private final byte[] salt;

    /**
     * Keys with a salt.
     *
     * @param salt the instance social salt (at least 16 bytes)
     */
    public AuthorKeys(byte[] salt) {
        if (salt == null || salt.length < 16) {
            throw new IllegalArgumentException("the social salt needs at least 16 bytes");
        }
        this.salt = salt.clone();
    }

    /**
     * The key of an account.
     *
     * @param platform the network
     * @param accountId the platform account id (the handle when the platform gives no id)
     * @return 64 lower-case hex characters
     */
    public String key(String platform, String accountId) {
        String input = (platform == null ? "" : platform) + ":" + (accountId == null ? "" : accountId);
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(salt, "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(input.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HmacSHA256 is not available", e);
        }
    }
}
