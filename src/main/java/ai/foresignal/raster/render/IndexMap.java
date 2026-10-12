package ai.foresignal.raster.render;

import ai.foresignal.raster.GridDef;

/**
 * For each output pixel, which native-grid cell(s) supply it (prompt 045): the cached half of a
 * render, so drawing a frame is a gather plus a palette lookup.
 *
 * <p>The {@link #nearest()} variant holds one flat cell index per pixel ({@code row × nx + col}, −1
 * outside the grid). The {@link #bilinear()} variant, for products whose spec sets
 * {@code resample: bilinear}, holds four cells and four weights per pixel (a missing neighbour has
 * cell −1 and weight 0) and still the nearest cell, which decides special codes.
 */
public final class IndexMap {

    /** Locates the WGS84 position of an output pixel's center. */
    @FunctionalInterface
    public interface PixelLocator {

        /**
         * Writes {@code {lon, lat}} of the center of pixel {@code (px, py)} into {@code out}.
         *
         * @param px column, 0 at the left
         * @param py row, 0 at the top
         */
        void locate(int px, int py, double[] out);
    }

    private final int width;
    private final int height;
    private final int[] nearest;
    private final int[] corners;
    private final float[] weights;
    private final boolean empty;

    private IndexMap(int width, int height, int[] nearest, int[] corners, float[] weights) {
        this.width = width;
        this.height = height;
        this.nearest = nearest;
        this.corners = corners;
        this.weights = weights;
        boolean none = true;
        for (int cell : nearest) {
            if (cell >= 0) {
                none = false;
                break;
            }
        }
        this.empty = none;
    }

    /**
     * Builds the map of a {@code width × height} output.
     *
     * @param grid the native grid
     * @param locator the pixel-center positions
     * @param bilinear true for the four-cell variant
     */
    public static IndexMap build(GridDef grid, int width, int height, PixelLocator locator, boolean bilinear) {
        int pixels = width * height;
        int[] nearest = new int[pixels];
        int[] corners = bilinear ? new int[pixels * 4] : null;
        float[] weights = bilinear ? new float[pixels * 4] : null;
        double[] ll = new double[2];
        int nx = grid.nx();
        int ny = grid.ny();
        for (int py = 0; py < height; py++) {
            for (int px = 0; px < width; px++) {
                int p = py * width + px;
                locator.locate(px, py, ll);
                double[] ij = grid.lonLatToIndex(ll[0], ll[1]);
                if (Double.isNaN(ij[0]) || Double.isNaN(ij[1])) {
                    nearest[p] = -1;
                    if (bilinear) {
                        for (int k = 0; k < 4; k++) {
                            corners[p * 4 + k] = -1;
                        }
                    }
                    continue;
                }
                int c = grid.column((int) Math.round(ij[0]));
                int r = (int) Math.round(ij[1]);
                nearest[p] = c < 0 || r < 0 || r >= ny ? -1 : r * nx + c;
                if (bilinear) {
                    int i0 = (int) Math.floor(ij[0]);
                    int j0 = (int) Math.floor(ij[1]);
                    double fx = ij[0] - i0;
                    double fy = ij[1] - j0;
                    corner(grid, corners, weights, p * 4, i0, j0, (1 - fx) * (1 - fy));
                    corner(grid, corners, weights, p * 4 + 1, i0 + 1, j0, fx * (1 - fy));
                    corner(grid, corners, weights, p * 4 + 2, i0, j0 + 1, (1 - fx) * fy);
                    corner(grid, corners, weights, p * 4 + 3, i0 + 1, j0 + 1, fx * fy);
                }
            }
        }
        return new IndexMap(width, height, nearest, corners, weights);
    }

    private static void corner(GridDef grid, int[] corners, float[] weights, int slot, int i, int j, double w) {
        int c = grid.column(i);
        if (c < 0 || j < 0 || j >= grid.ny()) {
            corners[slot] = -1;
            weights[slot] = 0f;
            return;
        }
        corners[slot] = j * grid.nx() + c;
        weights[slot] = (float) w;
    }

    /** The locator of a WebMercator tile of {@code size} pixels. */
    public static PixelLocator tile(int z, int x, int y, int size) {
        double[] m = WebMercator.tileMeters(z, x, y);
        double step = (m[2] - m[0]) / size;
        return (px, py, out) -> {
            out[0] = WebMercator.lon(m[0] + (px + 0.5) * step);
            out[1] = WebMercator.lat(m[3] - (py + 0.5) * step);
        };
    }

    /**
     * The locator of an image over a WGS84 box.
     *
     * @param mercator true for EPSG:3857 output (rows evenly spaced in meters), false for EPSG:4326
     */
    public static PixelLocator image(double west, double south, double east, double north, int width, int height,
            boolean mercator) {
        double e = east < west ? east + 360 : east;
        if (mercator) {
            double minX = WebMercator.x(west);
            double maxX = WebMercator.x(e);
            double minY = WebMercator.y(south);
            double maxY = WebMercator.y(north);
            double sx = (maxX - minX) / width;
            double sy = (maxY - minY) / height;
            return (px, py, out) -> {
                out[0] = WebMercator.lon(minX + (px + 0.5) * sx);
                out[1] = WebMercator.lat(maxY - (py + 0.5) * sy);
            };
        }
        double sx = (e - west) / width;
        double sy = (north - south) / height;
        return (px, py, out) -> {
            out[0] = west + (px + 0.5) * sx;
            out[1] = north - (py + 0.5) * sy;
        };
    }

    /** Output width, pixels. */
    public int width() {
        return width;
    }

    /** Output height, pixels. */
    public int height() {
        return height;
    }

    /** The nearest cell per pixel (−1 outside). */
    public int[] nearest() {
        return nearest;
    }

    /** True for the four-cell variant. */
    public boolean bilinear() {
        return corners != null;
    }

    /** The four cells per pixel (bilinear only). */
    public int[] corners() {
        return corners;
    }

    /** The four weights per pixel (bilinear only). */
    public float[] weights() {
        return weights;
    }

    /** True when no pixel touches the grid. */
    public boolean isEmpty() {
        return empty;
    }

    /** The bytes the map holds, for the cache weigher. */
    public long bytes() {
        long b = 64L + 4L * nearest.length;
        if (corners != null) {
            b += 8L * corners.length;
        }
        return b;
    }
}
