package ai.foresignal.raster;

import ai.foresignal.source.spi.GridDefinition;
import java.util.Map;

/**
 * The projection math of a native grid (prompt 045; Weather-Imagery-Design §3 "Reprojection"),
 * built from a {@code raster_grid.definition} ({@link GridDefinition}).
 *
 * <p>Index coordinates are continuous: the center of the cell in column {@code c} and row {@code r}
 * is exactly {@code (i, j) = (c, r)}, columns run west to east and rows north to south, as in the
 * {@code .fsg} payload. The nearest cell of a point is therefore {@code (round(i), round(j))}.
 *
 * <p>Four projections are implemented in-house (no GeoTools, which is LGPL, and no proj4j):
 * <ul>
 *   <li>{@link LatLonGrid}: a regular latitude/longitude grid;
 *   <li>{@link GeosGrid}: the normalized geostationary projection of the GOES-R ABI fixed grid and
 *       of CGMS LRIT/HRIT;
 *   <li>{@link LccGrid}: a spherical Lambert conformal conic grid (HRRR);
 *   <li>{@link AffineGrid}: a GeoTIFF-style geotransform in EPSG:4326 or EPSG:3857.
 * </ul>
 *
 * <p>Every parameter comes from the definition's {@code params}; nothing is hard-coded per product.
 */
public sealed interface GridDef permits LatLonGrid, GeosGrid, LccGrid, AffineGrid {

    /** Columns. */
    int nx();

    /** Rows. */
    int ny();

    /**
     * The continuous index of a WGS84 point.
     *
     * @param lon longitude, degrees (any range)
     * @param lat latitude, degrees
     * @return {@code {i, j}}, or {@code {NaN, NaN}} when the point lies outside the grid or, for a
     *     geostationary grid, beyond the visible disc
     */
    double[] lonLatToIndex(double lon, double lat);

    /**
     * The WGS84 position of a continuous index.
     *
     * @return {@code {lon, lat}} in degrees, longitude in [-180, 180), or {@code {NaN, NaN}} when
     *     the index does not land on the Earth (a geostationary scan angle off the disc)
     */
    double[] indexToLonLat(double i, double j);

    /** The grid's bounding box {@code [west, south, east, north]}, WGS84 degrees ({@code west > east} across the antimeridian). */
    double[] bounds();

    /** True when columns wrap around the globe (a global lat/lon grid). */
    default boolean wrapsLongitude() {
        return false;
    }

    /**
     * The column of integer column {@code c}, wrapped on a global grid.
     *
     * @return the column, or -1 when it lies outside a non-wrapping grid
     */
    default int column(int c) {
        if (wrapsLongitude()) {
            return Math.floorMod(c, nx());
        }
        return c < 0 || c >= nx() ? -1 : c;
    }

    /**
     * The flat cell index ({@code row × nx + column}) nearest a point.
     *
     * @return the cell, or -1 outside the grid
     */
    default int nearestCell(double lon, double lat) {
        double[] ij = lonLatToIndex(lon, lat);
        if (Double.isNaN(ij[0]) || Double.isNaN(ij[1])) {
            return -1;
        }
        int c = column((int) Math.round(ij[0]));
        int r = (int) Math.round(ij[1]);
        if (c < 0 || r < 0 || r >= ny()) {
            return -1;
        }
        return r * nx() + c;
    }

    /**
     * The projection of {@code definition}.
     *
     * @throws IllegalArgumentException when a required parameter is missing or invalid
     */
    static GridDef of(GridDefinition definition) {
        return switch (definition.type()) {
            case GridDefinition.LATLON -> LatLonGrid.of(definition);
            case GridDefinition.GEOS -> GeosGrid.of(definition);
            case GridDefinition.LCC -> LccGrid.of(definition);
            case GridDefinition.AFFINE -> AffineGrid.of(definition);
            default -> throw new IllegalArgumentException("Unknown grid type " + definition.type());
        };
    }

    /** {@code {NaN, NaN}}. */
    static double[] nowhere() {
        return new double[] {Double.NaN, Double.NaN};
    }

    /** {@code lon} normalized once to [-180, 180). */
    static double normalizeLon(double lon) {
        double l = ((lon + 180.0) % 360.0 + 360.0) % 360.0 - 180.0;
        return l == 180.0 ? -180.0 : l;
    }

    /** True when the continuous index lies on the grid (half a cell of margin around the centers). */
    static boolean inside(double i, double j, int nx, int ny) {
        return i >= -0.5 && i <= nx - 0.5 && j >= -0.5 && j <= ny - 0.5;
    }

    /** The definition's box as an array. */
    static double[] box(GridDefinition d) {
        return new double[] {d.west(), d.south(), d.east(), d.north()};
    }

    /**
     * A numeric parameter.
     *
     * @throws IllegalArgumentException when absent and {@code fallback} is null, or not a number
     */
    static double num(Map<String, Object> params, String key, Double fallback) {
        Object v = params.get(key);
        if (v == null) {
            if (fallback == null) {
                throw new IllegalArgumentException("Grid parameter '" + key + "' is required");
            }
            return fallback;
        }
        if (v instanceof Number n) {
            return n.doubleValue();
        }
        try {
            return Double.parseDouble(v.toString().strip());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Grid parameter '" + key + "' must be a number, was " + v);
        }
    }

    /** A text parameter, or {@code fallback}. */
    static String text(Map<String, Object> params, String key, String fallback) {
        Object v = params.get(key);
        return v == null ? fallback : v.toString().strip();
    }
}
