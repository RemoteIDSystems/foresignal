/*
 * Ported from God's Eye View, server/providers/wind/grid.js (resampleWindGrid),
 * https://github.com/bilawalsidhu/gods-eye-view, commit 591f299d11f38a612629a274463196d57ae3862e.
 * Copyright (c) 2026 Bilawal Sidhu. MIT License; the full notice is in THIRD_PARTY_NOTICES.md.
 * Changes: Java; float arrays; no scalar overlay; the output point budget admits the 0.25° grid
 * (1440 × 721), which God's Eye View's 1,000,000-point budget refuses.
 */
package ai.foresignal.raster.model;

/**
 * Resamples a wind grid into the God's Eye View equirectangular layout (prompt 051): bilinear,
 * longitude wraps across the seam, latitude clamps at the poles; the output starts at
 * {@code lo1 = 0, la1 = 90}, rows north to south, with {@code nx = 360 / dx} columns and
 * {@code ny = 180 / dy + 1} rows (1°: 360 × 181; 0.25°: 1440 × 721).
 *
 * <p>The source grid also runs north to south ({@code la1} at the top, {@code dj} positive towards
 * the south) and starts at {@code lo1}. Limits from God's Eye View: at most {@value #MAX_SOURCE_POINTS}
 * source points ({@code ni × nj}), every value finite before and after, {@code dx, dy} in
 * [0.25, 180].
 */
public final class WindResampler {

    /** The largest source grid, points (HRRR's 1799 × 1059 = 1.9 M fits). */
    public static final int MAX_SOURCE_POINTS = 2_000_000;

    /** The largest output grid, points: the 0.25° grid. */
    public static final int MAX_OUTPUT_POINTS = 1440 * 721;

    private WindResampler() {}

    /**
     * A resampled field.
     *
     * @param nx columns
     * @param ny rows
     * @param lo1 first column's longitude, degrees (0)
     * @param la1 first row's latitude, degrees (90)
     * @param dx column step, degrees
     * @param dy row step, degrees
     * @param u eastward component, m/s, row-major north to south
     * @param v northward component, m/s
     */
    public record Field(int nx, int ny, double lo1, double la1, double dx, double dy, float[] u, float[] v) {

        /** The planar little-endian Float32 byte length (u then v). */
        public int byteLength() {
            return nx * ny * 8;
        }
    }

    /**
     * Resamples {@code u} and {@code v}.
     *
     * @param ni source columns
     * @param nj source rows
     * @param lo1 source first column's longitude, degrees
     * @param la1 source first row's latitude, degrees (the northernmost)
     * @param di source column step, degrees (positive)
     * @param dj source row step, degrees (positive, southwards)
     * @param dx output column step, degrees
     * @param dy output row step, degrees
     * @throws IllegalArgumentException "Invalid wind grid geometry", "Invalid wind grid values" or
     *     "Resampled wind grid exceeds point budget", as God's Eye View words them
     */
    public static Field resample(float[] u, float[] v, int ni, int nj, double lo1, double la1, double di, double dj,
            double dx, double dy) {
        long points = (long) ni * nj;
        if (ni < 1 || nj < 1 || points > MAX_SOURCE_POINTS || u == null || v == null || u.length != points
                || v.length != points || !finite(lo1, la1, di, dj, dx, dy) || di <= 0 || dj <= 0 || dx < 0.25
                || dy < 0.25 || dx > 180 || dy > 180) {
            throw new IllegalArgumentException("Invalid wind grid geometry");
        }
        requireFinite(u, "Invalid wind grid values");
        requireFinite(v, "Invalid wind grid values");
        int nx = (int) Math.round(360 / dx);
        int ny = (int) Math.round(180 / dy) + 1;
        if ((long) nx * ny > MAX_OUTPUT_POINTS) {
            throw new IllegalArgumentException("Resampled wind grid exceeds point budget");
        }
        float[] outU = new float[nx * ny];
        float[] outV = new float[nx * ny];
        for (int y = 0; y < ny; y++) {
            double lat = 90 - y * dy;
            double sy = Math.max(0, Math.min(nj - 1, (la1 - lat) / dj));
            int y0 = (int) Math.floor(sy);
            int y1 = Math.min(nj - 1, y0 + 1);
            double fy = sy - y0;
            for (int x = 0; x < nx; x++) {
                double lon = x * dx;
                double sx = (((lon - lo1) / di) % ni + ni) % ni;
                int x0 = (int) Math.floor(sx);
                if (x0 >= ni) {
                    x0 = ni - 1;
                }
                int x1 = (x0 + 1) % ni;
                double fx = sx - x0;
                int index = y * nx + x;
                outU[index] = (float) sample(u, ni, x0, x1, y0, y1, fx, fy);
                outV[index] = (float) sample(v, ni, x0, x1, y0, y1, fx, fy);
            }
        }
        requireFinite(outU, "Invalid resampled wind values");
        requireFinite(outV, "Invalid resampled wind values");
        return new Field(nx, ny, 0, 90, dx, dy, outU, outV);
    }

    private static double sample(float[] s, int ni, int x0, int x1, int y0, int y1, double fx, double fy) {
        double top = s[y0 * ni + x0] * (1 - fx) + s[y0 * ni + x1] * fx;
        double bottom = s[y1 * ni + x0] * (1 - fx) + s[y1 * ni + x1] * fx;
        return top * (1 - fy) + bottom * fy;
    }

    private static boolean finite(double... values) {
        for (double v : values) {
            if (!Double.isFinite(v)) {
                return false;
            }
        }
        return true;
    }

    private static void requireFinite(float[] values, String message) {
        for (float f : values) {
            if (!Float.isFinite(f)) {
                throw new IllegalArgumentException(message);
            }
        }
    }
}
