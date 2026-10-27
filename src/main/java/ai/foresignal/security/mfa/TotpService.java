package ai.foresignal.security.mfa;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.OptionalLong;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Component;

/**
 * RFC 6238 TOTP (prompt R2-024): HMAC-SHA1, six digits, a 30-second step — the parameters every authenticator app
 * assumes from an {@code otpauth://totp} URI without {@code algorithm}, {@code digits} or {@code period}. The HOTP
 * computation and truncation are {@code com.eatthepath:java-otp}'s, over the JDK's {@code javax.crypto} HMAC; nothing
 * here reimplements them.
 *
 * <p>{@link #match} accepts the current step and one step either side (clock drift), never a step at or before the
 * last one accepted (replay), and compares codes in constant time.
 */
@Component
public class TotpService {

    /** The TOTP step. */
    public static final Duration STEP = TimeBasedOneTimePasswordGenerator.DEFAULT_TIME_STEP;

    /** Digits in a code. */
    public static final int DIGITS = 6;

    /** Steps accepted either side of the current one. */
    public static final int WINDOW = 1;

    /** Secret length in bytes: 160 bits, the HMAC-SHA1 block RFC 4226 recommends. */
    public static final int SECRET_BYTES = 20;

    private static final String HMAC = TimeBasedOneTimePasswordGenerator.TOTP_ALGORITHM_HMAC_SHA1;

    private final TimeBasedOneTimePasswordGenerator generator;
    private final SecureRandom random = new SecureRandom();

    /** Creates the service. */
    public TotpService() {
        try {
            this.generator = new TimeBasedOneTimePasswordGenerator(STEP, DIGITS, HMAC);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("The JDK has no " + HMAC, e);
        }
    }

    /** A new random secret. */
    public byte[] newSecret() {
        byte[] secret = new byte[SECRET_BYTES];
        random.nextBytes(secret);
        return secret;
    }

    /**
     * The Base32 form of {@code secret} (RFC 4648, upper case, no padding), as authenticator apps take it.
     *
     * @param secret the secret
     * @return the Base32 text
     */
    public static String base32(byte[] secret) {
        return new Base32().encodeAsString(secret).replace("=", "");
    }

    /**
     * The secret behind a Base32 text.
     *
     * @param text the Base32 text, any case
     * @return the secret
     */
    public static byte[] fromBase32(String text) {
        return new Base32().decode(text.toUpperCase(Locale.ROOT));
    }

    /**
     * The step {@code at} falls in.
     *
     * @param at an instant
     * @return the number of whole steps since the epoch
     */
    public static long step(Instant at) {
        return Math.floorDiv(at.getEpochSecond(), STEP.getSeconds());
    }

    /**
     * The code for one step.
     *
     * @param secret the secret
     * @param step the step
     * @return the zero-padded code
     */
    public String code(byte[] secret, long step) {
        try {
            return generator.generateOneTimePasswordString(new SecretKeySpec(secret, HMAC),
                    Instant.ofEpochSecond(step * STEP.getSeconds()), Locale.ROOT);
        } catch (InvalidKeyException e) {
            throw new IllegalStateException("The TOTP secret is not usable", e);
        }
    }

    /**
     * The step whose code {@code code} is, within {@link #WINDOW} steps of {@code now} and after {@code lastStep}.
     *
     * @param secret the secret
     * @param code the code presented, spaces ignored
     * @param now the current time
     * @param lastStep the last step accepted for this secret, or null
     * @return the matching step, or empty
     */
    public OptionalLong match(byte[] secret, String code, Instant now, Long lastStep) {
        if (code == null) {
            return OptionalLong.empty();
        }
        String presented = code.replace(" ", "");
        if (presented.length() != DIGITS || !presented.chars().allMatch(c -> c >= '0' && c <= '9')) {
            return OptionalLong.empty();
        }
        long current = step(now);
        OptionalLong found = OptionalLong.empty();
        for (long s = current - WINDOW; s <= current + WINDOW; s++) {
            boolean equal = MessageDigest.isEqual(code(secret, s).getBytes(StandardCharsets.US_ASCII),
                    presented.getBytes(StandardCharsets.US_ASCII));
            if (equal && (lastStep == null || s > lastStep) && found.isEmpty()) {
                found = OptionalLong.of(s);
            }
        }
        return found;
    }
}
