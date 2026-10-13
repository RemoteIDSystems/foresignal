package ai.foresignal.raster.sample;

import ai.foresignal.raster.GridDef;
import java.util.Arrays;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;

/**
 * Statistics of one frame over an area (prompt 052): {@code /stats}, {@code wx-areas} and the MCP
 * {@code area_weather_stats} tool.
 *
 * <p>A cell belongs to the area when its center lies in it. Cell areas come from {@link CellAreas}, on
 * the sphere per grid type. A cell has coverage when band 0 is a value (not {@code nodata} and not a
 * no-coverage code). Pure and thread-safe.
 */
public final class AreaScan {

    private AreaScan() {}

    /**
     * The result of a scan.
     *
     * @param cellsInArea cells whose center lies in the area
     * @param coveredCells of those, cells holding a value
     * @param areaKm2 the area of the cells in the area
     * @param min the smallest value (physical, native units), NaN when none
     * @param max the largest value, NaN when none
     * @param mean the mean value, NaN when none
     * @param p95 the 95th percentile (nearest rank), NaN when none
     * @param areaAboveKm2 per threshold, the area of covered cells at or above it
     * @param maskCells the flat cells at or above the first threshold, when collected, else null
     */
    public record Result(long cellsInArea, long coveredCells, double areaKm2, double min, double max, double mean,
            double p95, double[] areaAboveKm2, int[] maskCells) {

        /** The covered share of the area's cells, in percent, NaN when the area holds no cell. */
        public double coveragePct() {
            return cellsInArea == 0 ? Double.NaN : 100.0 * coveredCells / cellsInArea;
        }
    }

    /**
     * Scans {@code view} over {@code area}.
     *
     * @param thresholds physical thresholds in the product's native units
     * @param collectMask true to return the cells at or above the first threshold
     */
    public static Result scan(FrameView view, Geometry area, double[] thresholds, boolean collectMask) {
        GridDef grid = view.grid();
        Envelope env = area.getEnvelopeInternal();
        GridWindow window = GridWindow.of(grid, env.getMinX(), env.getMinY(), env.getMaxX(), env.getMaxY());
        double[] above = new double[thresholds.length];
        if (window.isEmpty()) {
            return new Result(0, 0, 0, Double.NaN, Double.NaN, Double.NaN, Double.NaN, above,
                    collectMask ? new int[0] : null);
        }
        PreparedGeometry prepared = PreparedGeometryFactory.prepare(area);
        GeometryFactory factory = area.getFactory();
        int nx = grid.nx();
        long inArea = 0;
        long covered = 0;
        double areaKm2 = 0;
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        double sum = 0;
        float[] values = new float[1024];
        int nValues = 0;
        int[] mask = collectMask ? new int[256] : null;
        int nMask = 0;
        boolean rectangular = area.isRectangle();
        for (int r = window.r0(); r <= window.r1(); r++) {
            for (int c = window.c0(); c <= window.c1(); c++) {
                double[] ll = grid.indexToLonLat(c, r);
                if (Double.isNaN(ll[0])) {
                    continue;
                }
                boolean in = rectangular ? env.covers(ll[0], ll[1])
                        : env.covers(ll[0], ll[1]) && prepared.covers(factory.createPoint(new Coordinate(ll[0], ll[1])));
                if (!in) {
                    continue;
                }
                int cell = r * nx + c;
                double cellArea = CellAreas.cellAreaKm2(grid, c, r);
                inArea++;
                areaKm2 += cellArea;
                double v = view.value(cell, 0);
                if (Double.isNaN(v)) {
                    continue;
                }
                covered++;
                min = Math.min(min, v);
                max = Math.max(max, v);
                sum += v;
                if (nValues == values.length) {
                    values = Arrays.copyOf(values, values.length * 2);
                }
                values[nValues++] = (float) v;
                for (int t = 0; t < thresholds.length; t++) {
                    if (v >= thresholds[t] - 1e-9) {
                        above[t] += cellArea;
                    }
                }
                if (collectMask && thresholds.length > 0 && v >= thresholds[0] - 1e-9) {
                    if (nMask == mask.length) {
                        mask = Arrays.copyOf(mask, mask.length * 2);
                    }
                    mask[nMask++] = cell;
                }
            }
        }
        if (covered == 0) {
            return new Result(inArea, 0, areaKm2, Double.NaN, Double.NaN, Double.NaN, Double.NaN, above,
                    collectMask ? new int[0] : null);
        }
        float[] sorted = Arrays.copyOf(values, nValues);
        Arrays.sort(sorted);
        int rank = (int) Math.ceil(0.95 * nValues) - 1;
        double p95 = sorted[Math.max(0, Math.min(nValues - 1, rank))];
        return new Result(inArea, covered, areaKm2, min, max, sum / covered, p95, above,
                collectMask ? Arrays.copyOf(mask, nMask) : null);
    }
}
