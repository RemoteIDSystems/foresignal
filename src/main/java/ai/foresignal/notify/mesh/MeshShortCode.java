package ai.foresignal.notify.mesh;

import java.util.Locale;

/**
 * The 4-character short id of an alert on the mesh (prompt 084), in Crockford base32
 * ({@code 0123456789ABCDEFGHJKMNPQRSTVWXYZ}): the low 20 bits of the alert id, so consecutive alerts never share a
 * code until a million alerts later. It ends every alert text ({@code #7K2Q}) and is stored on the delivery row, so
 * a {@code #E ACK 7K2Q} reply finds the alert.
 */
public final class MeshShortCode {

    /** The Crockford base32 alphabet. */
    public static final String ALPHABET = "0123456789ABCDEFGHJKMNPQRSTVWXYZ";

    /** The code length. */
    public static final int LENGTH = 4;

    private MeshShortCode() {}

    /** The code of {@code alertId}. */
    public static String of(long alertId) {
        long value = alertId & 0xFFFFFL;
        char[] out = new char[LENGTH];
        for (int i = LENGTH - 1; i >= 0; i--) {
            out[i] = ALPHABET.charAt((int) (value & 31));
            value >>>= 5;
        }
        return new String(out);
    }

    /**
     * {@code text} as a code: upper case, with Crockford's look-alikes read as digits ({@code O} as 0, {@code I} and
     * {@code L} as 1) and an optional leading {@code #}; null when it is not four Crockford characters.
     */
    public static String normalize(String text) {
        if (text == null) {
            return null;
        }
        String t = text.strip().toUpperCase(Locale.ROOT);
        if (t.startsWith("#")) {
            t = t.substring(1);
        }
        if (t.length() != LENGTH) {
            return null;
        }
        StringBuilder out = new StringBuilder(LENGTH);
        for (char c : t.toCharArray()) {
            char mapped = switch (c) {
                case 'O' -> '0';
                case 'I', 'L' -> '1';
                default -> c;
            };
            if (ALPHABET.indexOf(mapped) < 0) {
                return null;
            }
            out.append(mapped);
        }
        return out.toString();
    }
}
