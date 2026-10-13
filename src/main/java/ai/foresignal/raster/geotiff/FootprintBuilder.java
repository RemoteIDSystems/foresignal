package ai.foresignal.raster.geotiff;

import java.util.ArrayList;
import java.util.List;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.GeometryFixer;
import org.locationtech.jts.simplify.TopologyPreservingSimplifier;

/**
 * The real footprint of a polar overlay from its alpha mask (prompt 054, Weather-Imagery-Design §4.5
 * item 2).
 *
 * <p>Each row's leftmost and rightmost valid (non-transparent) pixel bound the swath on that row. The
 * polygon runs down the left edge (top to bottom) and back up the right edge (bottom to top), in pixel
 * corners, is transformed to WGS84 with unwrapped longitudes, simplified with
 * {@link TopologyPreservingSimplifier} (default {@value #DEFAULT_TOLERANCE}°), repaired with
 * {@link GeometryFixer} and split at ±180° ({@link Antimeridian}). Rows without a valid pixel break the
 * outline into separate parts.
 *
 * <p>A global EPSG:4326 image (one row spans 360°) can hold a swath that leaves its right edge and
 * re-enters at the left. On such a row whose valid pixels touch both edges, the extent is the
 * complement of the widest transparent gap, unwrapped past the right edge, so the outline stays one
 * continuous polygon before the split.
 */
public final class FootprintBuilder {

    /** The default simplification tolerance, degrees. */
    public static final double DEFAULT_TOLERANCE = 0.02;

    private FootprintBuilder() {}

    /**
     * The footprint of an image.
     *
     * @param width columns
     * @param height rows
     * @param rgba packed {@code 0xRRGGBBAA} pixels, row-major
     * @param georeference where the pixels lie
     * @param tolerance the simplification tolerance, degrees
     * @return the footprint, SRID 4326; empty when no pixel is valid
     */
    public static MultiPolygon build(int width, int height, int[] rgba, GeoReference georeference, double tolerance) {
        boolean global = georeference.epsg() == 4326 && georeference.lonSpan(width) >= 359.9;
        List<int[]> extents = new ArrayList<>(height);
        for (int row = 0; row < height; row++) {
            extents.add(extent(rgba, row, width, global));
        }
        List<Polygon> parts = new ArrayList<>();
        int start = -1;
        for (int row = 0; row <= height; row++) {
            boolean valid = row < height && extents.get(row) != null;
            if (valid && start < 0) {
                start = row;
            } else if (!valid && start >= 0) {
                parts.add(outline(extents, start, row, georeference));
                start = -1;
            }
        }
        if (parts.isEmpty()) {
            return Antimeridian.GEOMETRY.createMultiPolygon();
        }
        Geometry merged = parts.size() == 1 ? parts.get(0)
                : Antimeridian.GEOMETRY.createMultiPolygon(parts.toArray(Polygon[]::new));
        Geometry simplified = TopologyPreservingSimplifier.simplify(merged, tolerance);
        Geometry fixed = GeometryFixer.fix(simplified);
        return Antimeridian.split(fixed);
    }

    /** The footprint with the default tolerance. */
    public static MultiPolygon build(GeoTiffReader.Image image) {
        return build(image.width(), image.height(), image.rgba(), image.georeference(), DEFAULT_TOLERANCE);
    }

    /**
     * The {@code [left, right]} valid columns of a row (right may exceed {@code width} on a wrapped
     * global row), or null when the row has no valid pixel.
     */
    static int[] extent(int[] rgba, int row, int width, boolean global) {
        int base = row * width;
        int left = -1;
        int right = -1;
        for (int x = 0; x < width; x++) {
            if ((rgba[base + x] & 0xFF) != 0) {
                left = x;
                break;
            }
        }
        if (left < 0) {
            return null;
        }
        for (int x = width - 1; x >= left; x--) {
            if ((rgba[base + x] & 0xFF) != 0) {
                right = x;
                break;
            }
        }
        if (global && left == 0 && right == width - 1) {
            // Find the widest transparent gap; the swath is everything else, continuing past the edge.
            int bestStart = -1;
            int bestLength = 0;
            int runStart = -1;
            for (int x = 0; x < width; x++) {
                boolean transparent = (rgba[base + x] & 0xFF) == 0;
                if (transparent && runStart < 0) {
                    runStart = x;
                }
                if ((!transparent || x == width - 1) && runStart >= 0) {
                    int end = transparent ? x : x - 1;
                    int length = end - runStart + 1;
                    if (length > bestLength) {
                        bestLength = length;
                        bestStart = runStart;
                    }
                    runStart = -1;
                }
            }
            if (bestLength > 0) {
                int gapEnd = bestStart + bestLength - 1;
                return new int[] {gapEnd + 1, bestStart - 1 + width};
            }
        }
        return new int[] {left, right};
    }

    /** The outline of rows {@code [from, to)}: down the left edge, up the right edge. */
    private static Polygon outline(List<int[]> extents, int from, int to, GeoReference georeference) {
        List<Coordinate> ring = new ArrayList<>();
        for (int row = from; row < to; row++) {
            int left = extents.get(row)[0];
            ring.add(coordinate(georeference, left, row));
            ring.add(coordinate(georeference, left, row + 1));
        }
        for (int row = to - 1; row >= from; row--) {
            int right = extents.get(row)[1] + 1;
            ring.add(coordinate(georeference, right, row + 1));
            ring.add(coordinate(georeference, right, row));
        }
        ring.add(new Coordinate(ring.get(0)));
        Coordinate[] coordinates = ring.toArray(Coordinate[]::new);
        Antimeridian.unwrap(coordinates);
        coordinates[coordinates.length - 1] = new Coordinate(coordinates[0]);
        return Antimeridian.GEOMETRY.createPolygon(coordinates);
    }

    private static Coordinate coordinate(GeoReference georeference, double col, double row) {
        double[] ll = georeference.lonLat(col, row);
        return new Coordinate(ll[0], ll[1]);
    }
}
