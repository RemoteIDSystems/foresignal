package ai.foresignal.source.spi;

import java.time.Instant;
import java.util.Arrays;

/**
 * Lightning flashes as parallel primitive arrays (prompt 048, Weather-Imagery-Design §2.4): what the
 * {@code goes-glm} adapter hands to {@link FlashSink#append} and what the flash store's read API
 * returns.
 *
 * <p>No per-flash objects: a stormy CONUS hour is tens of thousands of flashes, and the ring and the
 * archive keep them as arrays. Entry {@code i} of every array describes the same flash. The arrays
 * are not copied; a batch is a value handed from one owner to the next, never shared for writing.
 *
 * @param timesMs flash times, epoch milliseconds (UTC); the time of the flash's first event
 * @param lat latitudes, degrees north
 * @param lon longitudes, degrees east
 * @param energyFj radiant energies, femtojoules (GLM {@code flash_energy} × 10¹⁵)
 */
public record FlashBatch(long[] timesMs, float[] lat, float[] lon, float[] energyFj) {

    /** An empty batch. */
    public static final FlashBatch EMPTY = new FlashBatch(new long[0], new float[0], new float[0], new float[0]);

    /** Checks that every array has the same length. */
    public FlashBatch {
        if (timesMs == null || lat == null || lon == null || energyFj == null) {
            throw new IllegalArgumentException("A flash batch needs all four arrays");
        }
        int n = timesMs.length;
        if (lat.length != n || lon.length != n || energyFj.length != n) {
            throw new IllegalArgumentException("Flash batch arrays differ in length: " + n + ", " + lat.length + ", "
                    + lon.length + ", " + energyFj.length);
        }
    }

    /** The number of flashes. */
    public int size() {
        return timesMs.length;
    }

    /** True when the batch holds no flash. */
    public boolean isEmpty() {
        return timesMs.length == 0;
    }

    /** The time of flash {@code i}. */
    public Instant time(int i) {
        return Instant.ofEpochMilli(timesMs[i]);
    }

    /** The newest flash time, or null for an empty batch. */
    public Instant newest() {
        if (isEmpty()) {
            return null;
        }
        long max = Long.MIN_VALUE;
        for (long t : timesMs) {
            max = Math.max(max, t);
        }
        return Instant.ofEpochMilli(max);
    }

    /** The flashes at {@code indices}, in that order. */
    public FlashBatch select(int[] indices) {
        long[] t = new long[indices.length];
        float[] la = new float[indices.length];
        float[] lo = new float[indices.length];
        float[] e = new float[indices.length];
        for (int k = 0; k < indices.length; k++) {
            int i = indices[k];
            t[k] = timesMs[i];
            la[k] = lat[i];
            lo[k] = lon[i];
            e[k] = energyFj[i];
        }
        return new FlashBatch(t, la, lo, e);
    }

    /** Value equality over the array contents (records compare arrays by reference). */
    @Override
    public boolean equals(Object other) {
        return other instanceof FlashBatch b && Arrays.equals(timesMs, b.timesMs) && Arrays.equals(lat, b.lat)
                && Arrays.equals(lon, b.lon) && Arrays.equals(energyFj, b.energyFj);
    }

    /** A hash over the array contents. */
    @Override
    public int hashCode() {
        return 31 * (31 * (31 * Arrays.hashCode(timesMs) + Arrays.hashCode(lat)) + Arrays.hashCode(lon))
                + Arrays.hashCode(energyFj);
    }

    /** A short description, never the arrays. */
    @Override
    public String toString() {
        return "FlashBatch[" + size() + " flashes]";
    }
}
