package ai.foresignal.source.spi;

import org.locationtech.jts.geom.Envelope;

/**
 * How much of an upstream's expected reporting actually arrived in one fetch
 * (Core-Data-Model §14).
 *
 * <p>This is what lets health tell "the feed is quiet" apart from "we lost two thirds of the
 * receivers", which otherwise look identical from a record count alone.
 *
 * @param receivers receivers that contributed
 * @param expected records the upstream said to expect
 * @param actual records actually returned
 * @param region the area the fetch covered
 */
public record Coverage(Integer receivers, Integer expected, Integer actual, Envelope region) {

    public static Coverage none() {
        return new Coverage(null, null, null, null);
    }

    public static Coverage of(int expected, int actual) {
        return new Coverage(null, expected, actual, null);
    }

    /** {@code actual / expected}, or empty when the upstream states no expectation. */
    public Double completeness() {
        if (expected == null || actual == null || expected <= 0) {
            return null;
        }
        return (double) actual / expected;
    }

    public boolean isEmpty() {
        return receivers == null && expected == null && actual == null && region == null;
    }
}
