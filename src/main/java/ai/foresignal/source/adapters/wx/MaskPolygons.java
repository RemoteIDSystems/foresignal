package ai.foresignal.source.adapters.wx;

import ai.foresignal.raster.GridDef;
import ai.foresignal.source.spi.Geo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.operation.union.UnaryUnionOp;
import org.locationtech.jts.precision.GeometryPrecisionReducer;
import org.locationtech.jts.simplify.TopologyPreservingSimplifier;

/**
 * Polygons of a set of grid cells by run-rectangle union (prompt 052; Weather-Imagery-Design §7.2): each
 * row's runs of consecutive cells become one rectangle in index space, whose corners are projected to
 * WGS84; runs with the same columns on consecutive rows merge into one taller rectangle; the rectangles are
 * unioned, simplified and snapped to a grid of {@code quantumDeg} degrees, so a steady mask gives an
 * identical geometry (and so no new version). Pure.
 */
public final class MaskPolygons {

    private MaskPolygons() {}

    /**
     * The polygonal geometry of {@code cells}.
     *
     * @param cells flat cell indices ({@code row × nx + column}), any order
     * @param quantumDeg the snapping grid and simplification tolerance, degrees (0.01 ≈ 1 km, 0.005 ≈ 500 m)
     * @return the geometry, or null when {@code cells} is empty or projects nowhere
     */
    public static Geometry of(GridDef grid, int[] cells, double quantumDeg) {
        if (cells == null || cells.length == 0) {
            return null;
        }
        GeometryFactory factory = Geo.factory();
        int nx = grid.nx();
        int[] sorted = cells.clone();
        Arrays.sort(sorted);
        // Runs per row: {row, c0, c1}.
        List<int[]> runs = new ArrayList<>();
        int i = 0;
        while (i < sorted.length) {
            int row = sorted[i] / nx;
            int c0 = sorted[i] % nx;
            int c1 = c0;
            int j = i + 1;
            while (j < sorted.length && sorted[j] / nx == row && sorted[j] % nx == c1 + 1) {
                c1++;
                j++;
            }
            runs.add(new int[] {row, c0, c1});
            i = j;
        }
        // Merge identical column spans on consecutive rows into {r0, r1, c0, c1}.
        List<int[]> rects = new ArrayList<>();
        java.util.Map<Long, int[]> open = new java.util.HashMap<>();
        for (int[] run : runs) {
            long key = ((long) run[1] << 32) | (run[2] & 0xFFFFFFFFL);
            int[] rect = open.get(key);
            if (rect != null && rect[1] == run[0] - 1) {
                rect[1] = run[0];
            } else {
                rect = new int[] {run[0], run[0], run[1], run[2]};
                rects.add(rect);
                open.put(key, rect);
            }
        }
        List<Geometry> polygons = new ArrayList<>(rects.size());
        for (int[] r : rects) {
            Polygon p = rectangle(grid, factory, r[2] - 0.5, r[0] - 0.5, r[3] + 0.5, r[1] + 0.5);
            if (p != null) {
                polygons.add(p);
            }
        }
        if (polygons.isEmpty()) {
            return null;
        }
        Geometry union = UnaryUnionOp.union(polygons);
        if (quantumDeg > 0) {
            union = TopologyPreservingSimplifier.simplify(union, quantumDeg);
            union = GeometryPrecisionReducer.reduce(union, new PrecisionModel(1.0 / quantumDeg));
        }
        if (union == null || union.isEmpty()) {
            return null;
        }
        union.setSRID(Geo.SRID);
        return union;
    }

    /** The polygon of an index rectangle, its edges densified so projected grids keep their shape. */
    private static Polygon rectangle(GridDef grid, GeometryFactory factory, double i0, double j0, double i1,
            double j1) {
        int steps = Math.max(1, (int) Math.min(16, Math.ceil(Math.max(i1 - i0, j1 - j0) / 8)));
        List<Coordinate> ring = new ArrayList<>();
        double[][] corners = {{i0, j0}, {i1, j0}, {i1, j1}, {i0, j1}};
        for (int k = 0; k < 4; k++) {
            double[] a = corners[k];
            double[] b = corners[(k + 1) % 4];
            for (int s = 0; s < steps; s++) {
                double t = (double) s / steps;
                double[] ll = grid.indexToLonLat(a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t);
                if (Double.isNaN(ll[0]) || Double.isNaN(ll[1])) {
                    return null;
                }
                ring.add(new Coordinate(ll[0], ll[1]));
            }
        }
        ring.add(new Coordinate(ring.get(0)));
        if (ring.size() < 4) {
            return null;
        }
        return factory.createPolygon(ring.toArray(Coordinate[]::new));
    }
}
