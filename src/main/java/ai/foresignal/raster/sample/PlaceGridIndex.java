package ai.foresignal.raster.sample;

import ai.foresignal.raster.GridDef;
import ai.foresignal.source.spi.Geo;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.Arrays;

/**
 * Per place and grid, the cells within {@value #RANGE_KM} km of the place, sorted by distance (prompt
 * 052; Weather-Imagery-Design §7.2 "Performance"), so one pass over a frame yields every distance
 * threshold and the in-radius maximum: the first cell at or above a threshold gives its nearest
 * distance, and the scan stops at the radius once every threshold has been seen.
 *
 * <p>Distances are great-circle kilometers from the place's representative point to the cell center.
 * Indexes are built once per (grid, point) and cached; a grid's cells never move.
 */
public final class PlaceGridIndex {

    /** The range of the index, km: "none within 100 km" is the sentinel 999. */
    public static final double RANGE_KM = 100;

    /** The farthest a cell center may lie and still be the place's own cell (half the coarsest grid's diagonal). */
    static final double OWN_CELL_KM = 20;

    private static final Cache<String, Entry> CACHE = Caffeine.newBuilder().maximumSize(1_000).build();

    private PlaceGridIndex() {}

    /**
     * The cells of one place on one grid.
     *
     * @param cells flat cell indices, nearest first
     * @param distKm their distances, ascending
     */
    public record Entry(int[] cells, float[] distKm) {

        /** The number of cells. */
        public int size() {
            return cells.length;
        }

        /** How many leading cells lie within {@code km}. */
        public int within(double km) {
            int lo = 0;
            int hi = distKm.length;
            while (lo < hi) {
                int mid = (lo + hi) >>> 1;
                if (distKm[mid] <= km) {
                    lo = mid + 1;
                } else {
                    hi = mid;
                }
            }
            return lo;
        }
    }

    /**
     * The (cached) index of a point on a grid.
     *
     * @param gridKey a stable key of the grid (its id)
     */
    public static Entry of(String gridKey, GridDef grid, double lon, double lat) {
        String key = gridKey + "|" + Math.round(lon * 1e5) + "|" + Math.round(lat * 1e5);
        return CACHE.get(key, k -> build(grid, lon, lat, RANGE_KM));
    }

    /** Builds an index (uncached). */
    public static Entry build(GridDef grid, double lon, double lat, double maxKm) {
        double dLat = maxKm / 111.0;
        double cos = Math.max(0.05, Math.cos(Math.toRadians(lat)));
        double dLon = Math.min(180, maxKm / (111.0 * cos));
        GridWindow window = GridWindow.of(grid, lon - dLon, Math.max(-90, lat - dLat), lon + dLon,
                Math.min(90, lat + dLat));
        if (window.isEmpty()) {
            return new Entry(new int[0], new float[0]);
        }
        int nx = grid.nx();
        long n = window.cells();
        long[] packed = new long[(int) Math.min(n, Integer.MAX_VALUE - 8)];
        int count = 0;
        for (int r = window.r0(); r <= window.r1(); r++) {
            for (int c = window.c0(); c <= window.c1(); c++) {
                double[] ll = grid.indexToLonLat(c, r);
                if (Double.isNaN(ll[0])) {
                    continue;
                }
                double d = Geo.haversineKm(lat, lon, ll[1], ll[0]);
                if (d > maxKm) {
                    continue;
                }
                // Distance in the high bits (float bits are ordered for non-negative values), cell in the low.
                packed[count++] = ((long) Float.floatToIntBits((float) d) << 32) | ((r * (long) nx + c) & 0xFFFFFFFFL);
            }
        }
        long[] sorted = Arrays.copyOf(packed, count);
        Arrays.sort(sorted);
        int[] cells = new int[count];
        float[] dist = new float[count];
        for (int i = 0; i < count; i++) {
            cells[i] = (int) (sorted[i] & 0xFFFFFFFFL);
            dist[i] = Float.intBitsToFloat((int) (sorted[i] >>> 32));
        }
        return new Entry(cells, dist);
    }

    /** Empties the cache (tests). */
    public static void clearCache() {
        CACHE.invalidateAll();
    }

    /**
     * One pass over a frame for a place: the nearest distance of each threshold and the maximum within the
     * radius.
     *
     * @param nearestKm per threshold, the distance of the nearest covered cell at or above it, or NaN
     *     when none lies within the index range
     * @param maxInRadius the largest value within the radius, NaN when no covered cell lies there
     * @param radiusCells cells within the radius
     * @param coveredInRadius of those, cells holding a value
     * @param noCoverageInRadius of those, cells with a no-coverage code
     */
    public record Scan(double[] nearestKm, double maxInRadius, int radiusCells, int coveredInRadius,
            int noCoverageInRadius) {}

    /**
     * Scans a frame along an index.
     *
     * @param thresholds physical thresholds, native units
     * @param radiusKm the place radius
     */
    public static Scan scan(FrameView view, Entry entry, double[] thresholds, double radiusKm) {
        double[] nearest = new double[thresholds.length];
        Arrays.fill(nearest, Double.NaN);
        double[] codes = new double[thresholds.length];
        for (int t = 0; t < thresholds.length; t++) {
            codes[t] = view.codeAtLeast(thresholds[t]);
        }
        int found = 0;
        double max = Double.NEGATIVE_INFINITY;
        int radiusCells = 0;
        int covered = 0;
        int noCoverage = 0;
        int[] cells = entry.cells();
        float[] dist = entry.distKm();
        // A radius smaller than a cell still holds the place's own (nearest) cell.
        double radius = cells.length == 0 || dist[0] > OWN_CELL_KM ? radiusKm : Math.max(radiusKm, dist[0]);
        for (int i = 0; i < cells.length; i++) {
            boolean inRadius = dist[i] <= radius;
            if (!inRadius && found == thresholds.length) {
                break;
            }
            double code = view.code(cells[i], 0);
            FrameView.Kind kind = view.classify(code);
            if (inRadius) {
                radiusCells++;
                if (kind == FrameView.Kind.NO_COVERAGE) {
                    noCoverage++;
                }
            }
            if (kind != FrameView.Kind.VALUE) {
                continue;
            }
            if (inRadius) {
                covered++;
                double v = view.physical(code);
                if (v > max) {
                    max = v;
                }
            }
            if (found < thresholds.length) {
                for (int t = 0; t < thresholds.length; t++) {
                    if (Double.isNaN(nearest[t]) && code >= codes[t]) {
                        nearest[t] = dist[i];
                        found++;
                    }
                }
            }
        }
        return new Scan(nearest, max == Double.NEGATIVE_INFINITY ? Double.NaN : max, radiusCells, covered, noCoverage);
    }
}
