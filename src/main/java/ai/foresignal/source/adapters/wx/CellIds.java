package ai.foresignal.source.adapters.wx;

import java.security.SecureRandom;
import java.time.Instant;

/**
 * ULIDs for new storm-cell identities (prompt 052: {@code c-{ulid}}): 26 Crockford base32 characters, 48
 * bits of milliseconds and 80 random bits, so ids sort by the time the cell was first seen. A copy of the
 * prompt 048 helper rather than an import, because adapter packages never import each other.
 */
final class CellIds {

    /** Crockford base32. */
    static final String CROCKFORD = "0123456789ABCDEFGHJKMNPQRSTVWXYZ";

    private static final SecureRandom RANDOM = new SecureRandom();

    private CellIds() {}

    /** A new ULID whose time part is {@code at}. */
    static String ulid(Instant at) {
        long ms = at.toEpochMilli();
        char[] out = new char[26];
        for (int i = 9; i >= 0; i--) {
            out[i] = CROCKFORD.charAt((int) (ms & 31));
            ms >>>= 5;
        }
        byte[] random = new byte[10];
        RANDOM.nextBytes(random);
        long hi = 0;
        for (int i = 0; i < 5; i++) {
            hi = (hi << 8) | (random[i] & 0xFF);
        }
        long lo = 0;
        for (int i = 5; i < 10; i++) {
            lo = (lo << 8) | (random[i] & 0xFF);
        }
        for (int i = 17; i >= 10; i--) {
            out[i] = CROCKFORD.charAt((int) (hi & 31));
            hi >>>= 5;
        }
        for (int i = 25; i >= 18; i--) {
            out[i] = CROCKFORD.charAt((int) (lo & 31));
            lo >>>= 5;
        }
        return new String(out);
    }

    /** A new cell id {@code c-{ulid}}. */
    static String cellId(Instant at) {
        return "c-" + ulid(at);
    }
}
