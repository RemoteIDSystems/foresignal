package ai.foresignal.security;

import java.security.SecureRandom;
import java.time.Instant;

/**
 * ULID identifiers (26 Crockford base32 characters: 48 bits of milliseconds, 80 random bits).
 *
 * <p>Principals use ULIDs (OpenAPI {@code Ulid}) so their ids sort by creation time and carry no
 * information beyond it. Also exposes the Crockford alphabet, which pairing codes share.
 */
public final class Ulids {

    /** Crockford base32: digits and upper-case letters without I, L, O and U. */
    public static final String CROCKFORD = "0123456789ABCDEFGHJKMNPQRSTVWXYZ";

    /** Length of a ULID. */
    public static final int LENGTH = 26;

    private static final SecureRandom RANDOM = new SecureRandom();

    private Ulids() {}

    /** A new ULID for {@code now}. */
    public static String next(Instant now) {
        long millis = now.toEpochMilli();
        char[] out = new char[LENGTH];
        // 10 characters of time (50 bits, the top two always zero for 48-bit times).
        for (int i = 9; i >= 0; i--) {
            out[i] = CROCKFORD.charAt((int) (millis & 31));
            millis >>>= 5;
        }
        // 16 characters of randomness (80 bits).
        byte[] random = new byte[10];
        RANDOM.nextBytes(random);
        long high = 0;
        for (int i = 0; i < 5; i++) {
            high = (high << 8) | (random[i] & 0xff);
        }
        long low = 0;
        for (int i = 5; i < 10; i++) {
            low = (low << 8) | (random[i] & 0xff);
        }
        for (int i = 17; i >= 10; i--) {
            out[i] = CROCKFORD.charAt((int) (high & 31));
            high >>>= 5;
        }
        for (int i = 25; i >= 18; i--) {
            out[i] = CROCKFORD.charAt((int) (low & 31));
            low >>>= 5;
        }
        return new String(out);
    }

    /** True when {@code value} has the shape of a ULID. */
    public static boolean isUlid(String value) {
        if (value == null || value.length() != LENGTH) {
            return false;
        }
        for (int i = 0; i < LENGTH; i++) {
            if (CROCKFORD.indexOf(value.charAt(i)) < 0) {
                return false;
            }
        }
        return true;
    }
}
