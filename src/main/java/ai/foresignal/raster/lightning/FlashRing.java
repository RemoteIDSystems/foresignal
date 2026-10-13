package ai.foresignal.raster.lightning;

import ai.foresignal.source.spi.FlashBatch;
import java.util.Arrays;

/**
 * The in-memory GLM ring (prompt 048, Weather-Imagery-Design §2.6): the last {@value #WINDOW_MINUTES}
 * minutes of flashes, in {@value #BUCKETS} one-minute buckets of primitive arrays, with no per-flash
 * objects.
 *
 * <p>The window follows the newest flash minute added, not the wall clock, so a catch-up after an
 * outage fills the ring in data time. A flash older than the window is dropped. A bucket is reused
 * for a later minute by resetting it; its arrays are kept for the next minute.
 *
 * <p>Not thread-safe; {@link FlashStore} guards it with a read-write lock.
 */
final class FlashRing {

    /** Minutes kept. */
    static final int WINDOW_MINUTES = 60;

    /** Buckets: the window plus the current minute. */
    static final int BUCKETS = WINDOW_MINUTES + 1;

    private static final long MINUTE_MS = 60_000L;

    private final Bucket[] buckets = new Bucket[BUCKETS];
    private long newestMinute = Long.MIN_VALUE;

    FlashRing() {
        for (int i = 0; i < BUCKETS; i++) {
            buckets[i] = new Bucket();
        }
    }

    /** One minute of flashes. */
    private static final class Bucket {

        long minute = Long.MIN_VALUE;
        int size;
        long[] t = new long[0];
        float[] lat = new float[0];
        float[] lon = new float[0];
        float[] energy = new float[0];

        void reset(long minute) {
            this.minute = minute;
            this.size = 0;
        }

        void add(long time, float la, float lo, float e) {
            if (size == t.length) {
                int c = Math.max(16, t.length * 2);
                t = Arrays.copyOf(t, c);
                lat = Arrays.copyOf(lat, c);
                lon = Arrays.copyOf(lon, c);
                energy = Arrays.copyOf(energy, c);
            }
            t[size] = time;
            lat[size] = la;
            lon[size] = lo;
            energy[size] = e;
            size++;
        }
    }

    /** A visitor of ring flashes. */
    @FunctionalInterface
    interface Visitor {

        /** Visits one flash. */
        void visit(long timeMs, float lat, float lon, float energyFj);
    }

    /**
     * Adds one flash; returns false when it is older than the window and was dropped.
     *
     * @param timeMs the flash time, epoch milliseconds
     */
    boolean add(long timeMs, float lat, float lon, float energyFj) {
        long minute = Math.floorDiv(timeMs, MINUTE_MS);
        if (newestMinute != Long.MIN_VALUE && minute <= newestMinute - BUCKETS) {
            return false;
        }
        if (minute > newestMinute) {
            newestMinute = minute;
        }
        Bucket bucket = buckets[(int) Math.floorMod(minute, (long) BUCKETS)];
        if (bucket.minute != minute) {
            bucket.reset(minute);
        }
        bucket.add(timeMs, lat, lon, energyFj);
        return true;
    }

    /** True when {@code bucket} holds minutes still inside the window. */
    private boolean live(Bucket bucket) {
        return bucket.minute != Long.MIN_VALUE && bucket.minute > newestMinute - BUCKETS && bucket.size > 0;
    }

    /** Visits the flashes with {@code fromMs <= time <= toMs}, oldest bucket first. */
    void forEach(long fromMs, long toMs, Visitor visitor) {
        if (newestMinute == Long.MIN_VALUE) {
            return;
        }
        long firstMinute = Math.max(Math.floorDiv(fromMs, MINUTE_MS), newestMinute - BUCKETS + 1);
        long lastMinute = Math.min(Math.floorDiv(toMs, MINUTE_MS), newestMinute);
        for (long minute = firstMinute; minute <= lastMinute; minute++) {
            Bucket b = buckets[(int) Math.floorMod(minute, (long) BUCKETS)];
            if (b.minute != minute || !live(b)) {
                continue;
            }
            for (int i = 0; i < b.size; i++) {
                long t = b.t[i];
                if (t >= fromMs && t <= toMs) {
                    visitor.visit(t, b.lat[i], b.lon[i], b.energy[i]);
                }
            }
        }
    }

    /** The flashes with {@code fromMs <= time <= toMs}, oldest bucket first. */
    FlashBatch between(long fromMs, long toMs) {
        FlashArchive.Growable out = new FlashArchive.Growable(256);
        forEach(fromMs, toMs, out::add);
        return out.toBatch();
    }

    /** The number of flashes inside the window. */
    long size() {
        long n = 0;
        for (Bucket b : buckets) {
            if (live(b)) {
                n += b.size;
            }
        }
        return n;
    }

    /** The newest minute added (epoch minutes), or {@link Long#MIN_VALUE} when empty. */
    long newestMinute() {
        return newestMinute;
    }

    /** Empties the ring. */
    void clear() {
        for (Bucket b : buckets) {
            b.reset(Long.MIN_VALUE);
        }
        newestMinute = Long.MIN_VALUE;
    }
}
