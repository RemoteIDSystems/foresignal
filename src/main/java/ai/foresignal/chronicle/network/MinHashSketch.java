package ai.foresignal.chronicle.network;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Base64;

/**
 * A MinHash signature (prompt 134): for each of {@value #PERMUTATIONS} seeded, deterministic hash functions, the
 * smallest hashed value of the set. Two signatures agree in a slot with probability equal to the Jaccard similarity
 * of their sets, so the share of agreeing slots estimates it. Adding values only lowers slots, so updating a signature
 * incrementally gives exactly the signature of the union (incremental update equals recompute).
 *
 * <p>The hash functions are the SplitMix64 finalizer over the value XOR a per-permutation seed drawn from a fixed
 * SplitMix64 sequence, so every instance computes identical signatures for identical keyed hashes. An empty slot holds
 * {@link Long#MAX_VALUE}. Immutable.
 */
public final class MinHashSketch {

    /** The number of permutations (signature slots). */
    public static final int PERMUTATIONS = 128;

    /** LSH bands. */
    public static final int BANDS = 16;

    /** LSH rows per band ({@value #BANDS} × {@value #ROWS} = {@value #PERMUTATIONS}). */
    public static final int ROWS = 8;

    /** The value of an empty slot. */
    public static final long EMPTY = Long.MAX_VALUE;

    /** The fixed seed of the permutation sequence; changing it breaks every stored and exchanged sketch. */
    static final long SEED = 0x466F726573696731L;

    private static final long[] SEEDS = seeds();

    private final long[] slots;

    private MinHashSketch(long[] slots) {
        this.slots = slots;
    }

    /** The signature of the empty set. */
    public static MinHashSketch empty() {
        long[] s = new long[PERMUTATIONS];
        Arrays.fill(s, EMPTY);
        return new MinHashSketch(s);
    }

    /** The signature of a set of keyed hashes. */
    public static MinHashSketch of(Iterable<Long> values) {
        return empty().with(values);
    }

    /** This signature updated with more values: the signature of the union. */
    public MinHashSketch with(Iterable<Long> values) {
        long[] s = Arrays.copyOf(slots, PERMUTATIONS);
        for (Long v : values) {
            if (v == null) {
                continue;
            }
            for (int i = 0; i < PERMUTATIONS; i++) {
                long h = permute(v, i);
                if (h < s[i]) {
                    s[i] = h;
                }
            }
        }
        return new MinHashSketch(s);
    }

    /** The signature of the union of two sets, slot by slot. */
    public MinHashSketch union(MinHashSketch other) {
        long[] s = new long[PERMUTATIONS];
        for (int i = 0; i < PERMUTATIONS; i++) {
            s[i] = Math.min(slots[i], other.slots[i]);
        }
        return new MinHashSketch(s);
    }

    /** The value of permutation {@code i} for {@code value}, never {@link #EMPTY}. */
    static long permute(long value, int i) {
        long z = value ^ SEEDS[i];
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        z = z ^ (z >>> 31);
        return z == EMPTY ? EMPTY - 1 : z;
    }

    private static long[] seeds() {
        long[] out = new long[PERMUTATIONS];
        long state = SEED;
        for (int i = 0; i < PERMUTATIONS; i++) {
            state += 0x9E3779B97F4A7C15L;
            long z = state;
            z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
            z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
            out[i] = z ^ (z >>> 31);
        }
        return out;
    }

    /** True when no value was added. */
    public boolean isEmpty() {
        for (long s : slots) {
            if (s != EMPTY) {
                return false;
            }
        }
        return true;
    }

    /** A copy of the slots. */
    public long[] slots() {
        return Arrays.copyOf(slots, PERMUTATIONS);
    }

    /**
     * The estimated Jaccard similarity: the share of slots in which both signatures hold the same value; 0 when either
     * set is empty.
     */
    public double jaccard(MinHashSketch other) {
        if (isEmpty() || other.isEmpty()) {
            return 0;
        }
        int same = 0;
        for (int i = 0; i < PERMUTATIONS; i++) {
            if (slots[i] == other.slots[i] && slots[i] != EMPTY) {
                same++;
            }
        }
        return same / (double) PERMUTATIONS;
    }

    /** The share of slots that differ between two signatures (0 when identical). */
    public double changedShare(MinHashSketch other) {
        int changed = 0;
        for (int i = 0; i < PERMUTATIONS; i++) {
            if (slots[i] != other.slots[i]) {
                changed++;
            }
        }
        return changed / (double) PERMUTATIONS;
    }

    /**
     * The estimated number of shared values of two sets from their Jaccard similarity and sizes:
     * {@code J × (|A| + |B|) / (1 + J)}.
     */
    public static double sharedEstimate(double jaccard, double sizeA, double sizeB) {
        if (jaccard <= 0 || sizeA <= 0 || sizeB <= 0) {
            return 0;
        }
        double shared = jaccard * (sizeA + sizeB) / (1 + jaccard);
        return Math.min(shared, Math.min(sizeA, sizeB));
    }

    /** The containment of the smaller set: shared ÷ the smaller size (0 when either is empty). */
    public static double containment(double shared, double sizeA, double sizeB) {
        double smaller = Math.min(sizeA, sizeB);
        return smaller <= 0 ? 0 : Math.min(1.0, shared / smaller);
    }

    /** The {@value #BANDS} LSH band keys of this signature (band index mixed in), or an empty array when empty. */
    public long[] bandKeys() {
        if (isEmpty()) {
            return new long[0];
        }
        long[] out = new long[BANDS];
        for (int b = 0; b < BANDS; b++) {
            long h = 0x9E3779B97F4A7C15L * (b + 1);
            for (int r = 0; r < ROWS; r++) {
                h = permute(h ^ slots[b * ROWS + r], r);
            }
            out[b] = h;
        }
        return out;
    }

    /** The signature as {@value #PERMUTATIONS} big-endian longs. */
    public byte[] toBytes() {
        ByteBuffer buf = ByteBuffer.allocate(PERMUTATIONS * Long.BYTES);
        for (long s : slots) {
            buf.putLong(s);
        }
        return buf.array();
    }

    /** The signature as base64 of {@link #toBytes()} (the sketch package form). */
    public String toBase64() {
        return Base64.getEncoder().encodeToString(toBytes());
    }

    /**
     * A signature from {@link #toBytes()}.
     *
     * @throws IllegalArgumentException when the length is not {@value #PERMUTATIONS} longs
     */
    public static MinHashSketch fromBytes(byte[] bytes) {
        if (bytes == null || bytes.length != PERMUTATIONS * Long.BYTES) {
            throw new IllegalArgumentException("a MinHash signature is " + PERMUTATIONS * Long.BYTES + " bytes");
        }
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        long[] s = new long[PERMUTATIONS];
        for (int i = 0; i < PERMUTATIONS; i++) {
            s[i] = buf.getLong();
        }
        return new MinHashSketch(s);
    }

    /**
     * A signature from {@link #toBase64()}.
     *
     * @throws IllegalArgumentException when it is not base64 of {@value #PERMUTATIONS} longs
     */
    public static MinHashSketch fromBase64(String base64) {
        if (base64 == null) {
            throw new IllegalArgumentException("a MinHash signature is required");
        }
        return fromBytes(Base64.getDecoder().decode(base64));
    }

    /** The log2 size band of a set ({@code floor(log2(size))}; 0 for sizes 0 and 1). */
    public static int sizeBand(long size) {
        return size <= 1 ? 0 : 63 - Long.numberOfLeadingZeros(size);
    }

    /** The size a log2 band stands for: the geometric middle of {@code [2^band, 2^(band+1))}. */
    public static double sizeOfBand(int band) {
        int b = Math.max(0, Math.min(62, band));
        return Math.pow(2, b + 0.5);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof MinHashSketch other && Arrays.equals(slots, other.slots);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(slots);
    }
}
