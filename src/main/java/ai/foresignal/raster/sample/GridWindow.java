package ai.foresignal.raster.sample;

import ai.foresignal.raster.GridDef;

/**
 * The rectangle of grid cells that covers a longitude/latitude box (prompt 052), found by projecting
 * a densified outline of the box (and the grid's own corners when they fall inside the box), so it works
 * for every projection. Inclusive bounds; empty when the box misses the grid.
 *
 * @param c0 first column
 * @param r0 first row
 * @param c1 last column
 * @param r1 last row
 */
public record GridWindow(int c0, int r0, int c1, int r1) {

    /** Points per side of the densified box outline. */
    static final int SIDE_POINTS = 64;

    /** True when the window holds no cell. */
    public boolean isEmpty() {
        return c1 < c0 || r1 < r0;
    }

    /** Cells in the window. */
    public long cells() {
        return isEmpty() ? 0 : (long) (c1 - c0 + 1) * (r1 - r0 + 1);
    }

    /** The empty window. */
    public static GridWindow empty() {
        return new GridWindow(0, 0, -1, -1);
    }

    /**
     * The window of {@code [west, south, east, north]} (west may exceed east across the antimeridian),
     * grown by one cell.
     */
    public static GridWindow of(GridDef grid, double west, double south, double east, double north) {
        double spanLon = east >= west ? east - west : east + 360 - west;
        double minI = Double.POSITIVE_INFINITY;
        double maxI = Double.NEGATIVE_INFINITY;
        double minJ = Double.POSITIVE_INFINITY;
        double maxJ = Double.NEGATIVE_INFINITY;
        boolean any = false;
        for (int a = 0; a <= SIDE_POINTS; a++) {
            for (int b = 0; b <= SIDE_POINTS; b++) {
                if (a != 0 && a != SIDE_POINTS && b != 0 && b != SIDE_POINTS && (a % 8 != 0 || b % 8 != 0)) {
                    continue;
                }
                double lon = west + spanLon * a / SIDE_POINTS;
                double lat = south + (north - south) * b / SIDE_POINTS;
                double[] ij = grid.lonLatToIndex(lon, lat);
                if (Double.isNaN(ij[0]) || Double.isNaN(ij[1])) {
                    continue;
                }
                any = true;
                minI = Math.min(minI, ij[0]);
                maxI = Math.max(maxI, ij[0]);
                minJ = Math.min(minJ, ij[1]);
                maxJ = Math.max(maxJ, ij[1]);
            }
        }
        int nx = grid.nx();
        int ny = grid.ny();
        int[][] corners = {{0, 0}, {nx - 1, 0}, {0, ny - 1}, {nx - 1, ny - 1}};
        for (int[] corner : corners) {
            double[] ll = grid.indexToLonLat(corner[0], corner[1]);
            if (Double.isNaN(ll[0]) || !inside(ll[0], ll[1], west, south, east, north)) {
                continue;
            }
            any = true;
            minI = Math.min(minI, corner[0]);
            maxI = Math.max(maxI, corner[0]);
            minJ = Math.min(minJ, corner[1]);
            maxJ = Math.max(maxJ, corner[1]);
        }
        if (!any) {
            return empty();
        }
        if (grid.wrapsLongitude() && spanLon >= 359) {
            minI = 0;
            maxI = nx - 1;
        }
        int c0 = Math.max(0, (int) Math.floor(minI) - 1);
        int c1 = Math.min(nx - 1, (int) Math.ceil(maxI) + 1);
        int r0 = Math.max(0, (int) Math.floor(minJ) - 1);
        int r1 = Math.min(ny - 1, (int) Math.ceil(maxJ) + 1);
        if (grid.wrapsLongitude() && maxI - minI > nx / 2.0 && east < west) {
            // A wrapping grid and a box across the seam: scan the whole width rather than the far side.
            c0 = 0;
            c1 = nx - 1;
        }
        return new GridWindow(c0, r0, c1, r1);
    }

    private static boolean inside(double lon, double lat, double west, double south, double east, double north) {
        if (lat < south || lat > north) {
            return false;
        }
        return east >= west ? lon >= west && lon <= east : lon >= west || lon <= east;
    }
}
