package ai.foresignal.security.pairing;

import ai.foresignal.security.Ulids;
import java.security.SecureRandom;
import java.util.Locale;

/**
 * Pairing code text (prompt 023): 8 Crockford base32 characters (40 random bits), shown as
 * {@code XXXX-XXXX}.
 *
 * <p>Input is read the Crockford way: case-insensitive, the dash optional, {@code I} and {@code L}
 * read as {@code 1}, {@code O} as {@code 0}.
 */
public final class PairingCodes {

    /** Characters in a code. */
    public static final int LENGTH = 8;

    private static final SecureRandom RANDOM = new SecureRandom();

    private PairingCodes() {}

    /** A new code, {@code XXXX-XXXX}. */
    public static String generate() {
        StringBuilder out = new StringBuilder(LENGTH + 1);
        for (int i = 0; i < LENGTH; i++) {
            if (i == LENGTH / 2) {
                out.append('-');
            }
            out.append(Ulids.CROCKFORD.charAt(RANDOM.nextInt(32)));
        }
        return out.toString();
    }

    /**
     * {@code input} as 8 canonical characters (no dash), or null when it cannot be a code.
     */
    public static String normalize(String input) {
        if (input == null) {
            return null;
        }
        String value = input.strip().toUpperCase(Locale.ROOT).replace("-", "");
        if (value.length() != LENGTH) {
            return null;
        }
        StringBuilder out = new StringBuilder(LENGTH);
        for (char c : value.toCharArray()) {
            char mapped = switch (c) {
                case 'I', 'L' -> '1';
                case 'O' -> '0';
                default -> c;
            };
            if (Ulids.CROCKFORD.indexOf(mapped) < 0) {
                return null;
            }
            out.append(mapped);
        }
        return out.toString();
    }
}
