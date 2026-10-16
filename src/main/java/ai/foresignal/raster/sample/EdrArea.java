package ai.foresignal.raster.sample;

import ai.foresignal.raster.GridDef;
import ai.foresignal.raster.LatLonGrid;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The native-grid subset of a frame inside a WGS84 box, for OGC API &ndash; EDR {@code /area} (prompt
 * 108): every stored cell whose center lies in the box, read once from the decoded frame (no per-cell
 * frame lookups and no resampling), with no-data cells as {@code null} (the CoverageJSON nodata mask).
 *
 * <p>A regular latitude/longitude grid answers a CoverageJSON {@code Grid} domain (x ascending, y from
 * north to south, values flat and row-major as {@code [y][x]}). A projected grid (geostationary, Lambert,
 * affine) has no axis-aligned lon/lat lattice, so its cells answer a {@code MultiPoint} domain whose
 * {@code composite} axis lists each cell center.
 */
public final class EdrArea {

    /** Points sampled along each box edge (and per side of the interior lattice) to find a projected index range. */
    private static final int EDGE_SAMPLES = 64;

    private EdrArea() {}

    /**
     * The subset, or the number of cells it would hold when that exceeds {@code maxCells}.
     *
     * @param domain the CoverageJSON domain ({@code Grid} or {@code MultiPoint}), null when too large
     * @param axisNames the range's axis names
     * @param shape the range's shape
     * @param values the flat physical values ({@code null} = no data), null when too large
     * @param cells the native cells in the box (also when too large)
     */
    public record Subset(Map<String, Object> domain, List<String> axisNames, List<Integer> shape,
            List<Double> values, long cells) {

        /** True when the subset exceeded the cap and carries no values. */
        public boolean tooLarge() {
            return values == null;
        }
    }

    /**
     * The native cells of {@code view} inside {@code box}.
     *
     * @param box {@code {west, south, east, north}}, {@code west > east} across the antimeridian
     * @param maxCells the cap on native cells
     */
    public static Subset of(FrameView view, double[] box, int maxCells) {
        GridDef grid = view.grid();
        if (grid instanceof LatLonGrid latlon) {
            return regular(view, latlon, box, maxCells);
        }
        return projected(view, grid, box, maxCells);
    }

    private static Subset regular(FrameView view, LatLonGrid grid, double[] box, int maxCells) {
        double width = width(box);
        List<double[]> included = new ArrayList<>();
        for (int i = 0; i < grid.nx(); i++) {
            double off = eastOf(box[0], grid.lo1() + i * grid.dx());
            if (off <= width + 1e-9) {
                included.add(new double[] {off, i});
            }
        }
        included.sort(java.util.Comparator.comparingDouble(c -> c[0]));
        List<Integer> columns = included.stream().map(c -> (int) c[1]).toList();
        List<Double> offsets = included.stream().map(c -> c[0]).toList();
        List<Integer> rows = new ArrayList<>();
        for (int j = 0; j < grid.ny(); j++) {
            double lat = grid.la1() - j * grid.dy();
            if (lat >= box[1] - 1e-9 && lat <= box[3] + 1e-9) {
                rows.add(j);
            }
        }
        long cells = (long) columns.size() * rows.size();
        if (cells > maxCells) {
            return new Subset(null, null, null, null, cells);
        }
        List<Double> values = new ArrayList<>((int) cells);
        for (int j : rows) {
            for (int i : columns) {
                values.add(valueOf(view, j * grid.nx() + i));
            }
        }
        Map<String, Object> x = axis(offsets.stream().map(o -> box[0] + o).toList(), grid.dx());
        Map<String, Object> y = axis(rows.stream().map(j -> grid.la1() - j * grid.dy()).toList(), grid.dy());
        Map<String, Object> domain = new LinkedHashMap<>();
        domain.put("type", "Domain");
        domain.put("domainType", "Grid");
        domain.put("axes", Map.of("x", x, "y", y));
        domain.put("referencing", List.of(crs84(List.of("x", "y"))));
        return new Subset(domain, List.of("y", "x"), List.of(rows.size(), columns.size()), values, cells);
    }

    private static Subset projected(FrameView view, GridDef grid, double[] box, int maxCells) {
        double width = width(box);
        double minI = Double.POSITIVE_INFINITY;
        double maxI = Double.NEGATIVE_INFINITY;
        double minJ = Double.POSITIVE_INFINITY;
        double maxJ = Double.NEGATIVE_INFINITY;
        for (int a = 0; a <= EDGE_SAMPLES; a++) {
            for (int b = 0; b <= EDGE_SAMPLES; b++) {
                double lon = box[0] + width * a / EDGE_SAMPLES;
                double lat = box[1] + (box[3] - box[1]) * b / EDGE_SAMPLES;
                double[] ij = grid.lonLatToIndex(GridDef.normalizeLon(lon), lat);
                if (Double.isNaN(ij[0]) || Double.isNaN(ij[1])) {
                    continue;
                }
                minI = Math.min(minI, ij[0]);
                maxI = Math.max(maxI, ij[0]);
                minJ = Math.min(minJ, ij[1]);
                maxJ = Math.max(maxJ, ij[1]);
            }
        }
        List<double[]> points = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        long cells = 0;
        if (minI <= maxI) {
            int i0 = Math.max(0, (int) Math.floor(minI) - 1);
            int i1 = Math.min(grid.nx() - 1, (int) Math.ceil(maxI) + 1);
            int j0 = Math.max(0, (int) Math.floor(minJ) - 1);
            int j1 = Math.min(grid.ny() - 1, (int) Math.ceil(maxJ) + 1);
            long candidates = (long) (i1 - i0 + 1) * (j1 - j0 + 1);
            if (candidates > 4L * maxCells) {
                return new Subset(null, null, null, null, candidates);
            }
            for (int j = j0; j <= j1; j++) {
                for (int i = i0; i <= i1; i++) {
                    double[] ll = grid.indexToLonLat(i, j);
                    if (Double.isNaN(ll[0]) || Double.isNaN(ll[1]) || ll[1] < box[1] || ll[1] > box[3]
                            || eastOf(box[0], ll[0]) > width + 1e-9) {
                        continue;
                    }
                    cells++;
                    if (cells > maxCells) {
                        continue;
                    }
                    points.add(new double[] {ll[0], ll[1]});
                    values.add(valueOf(view, j * grid.nx() + i));
                }
            }
        }
        if (cells > maxCells) {
            return new Subset(null, null, null, null, cells);
        }
        Map<String, Object> composite = new LinkedHashMap<>();
        composite.put("dataType", "tuple");
        composite.put("coordinates", List.of("x", "y"));
        composite.put("values", points.stream().map(p -> List.of(p[0], p[1])).toList());
        Map<String, Object> domain = new LinkedHashMap<>();
        domain.put("type", "Domain");
        domain.put("domainType", "MultiPoint");
        domain.put("axes", Map.of("composite", composite));
        domain.put("referencing", List.of(crs84(List.of("x", "y"))));
        return new Subset(domain, List.of("composite"), List.of(points.size()), values, cells);
    }

    /** Band 0's physical value of a cell rounded like {@code /v1/sample}, or null for no data. */
    private static Double valueOf(FrameView view, int cell) {
        double v = view.value(cell, 0);
        return Double.isNaN(v) ? null : Math.round(v * 1000.0) / 1000.0;
    }

    /** A regular axis as {@code start/stop/num}, else its explicit values. */
    private static Map<String, Object> axis(List<Double> values, double step) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (values.isEmpty()) {
            out.put("values", List.of());
            return out;
        }
        boolean regular = true;
        for (int k = 1; k < values.size(); k++) {
            if (Math.abs(Math.abs(values.get(k) - values.get(k - 1)) - step) > step * 1e-6) {
                regular = false;
                break;
            }
        }
        if (regular && values.size() > 1) {
            out.put("start", values.get(0));
            out.put("stop", values.get(values.size() - 1));
            out.put("num", values.size());
        } else {
            out.put("values", values);
        }
        return out;
    }

    /** Degrees east of {@code west} to {@code lon}, in [0, 360). */
    private static double eastOf(double west, double lon) {
        double off = ((lon - west) % 360.0 + 360.0) % 360.0;
        return off > 360.0 - 1e-9 ? 0.0 : off;
    }

    /** The box's east-west span, degrees. */
    private static double width(double[] box) {
        double w = box[2] - box[0];
        return w < 0 ? w + 360.0 : w;
    }

    private static Map<String, Object> crs84(List<String> coordinates) {
        return Map.of("coordinates", coordinates, "system", Map.of("type", "GeographicCRS",
                "id", "http://www.opengis.net/def/crs/OGC/1.3/CRS84"));
    }
}
