package ai.foresignal.raster;

import ai.foresignal.source.spi.GridDefinition;
import java.util.Map;

/**
 * A grid given by a GeoTIFF/GDAL-style affine geotransform (prompt 045), in EPSG:4326 (degrees) or
 * EPSG:3857 (WebMercator meters), as SatDump polar GeoTIFFs carry.
 *
 * <p>Parameters: {@code crs} ({@code EPSG:4326}, the default, or {@code EPSG:3857}) and
 * {@code gt0} … {@code gt5}, the GDAL geotransform of the cell <em>corners</em>:
 * {@code X = gt0 + col·gt1 + row·gt2}, {@code Y = gt3 + col·gt4 + row·gt5}. Cell centers sit at
 * {@code col + 0.5, row + 0.5}, so the continuous index is the corner coordinate minus 0.5.
 *
 * @param nx columns
 * @param ny rows
 * @param mercator true for EPSG:3857
 * @param gt the six geotransform coefficients
 * @param box the definition's bounding box {@code [west, south, east, north]}
 */
public record AffineGrid(int nx, int ny, boolean mercator, double[] gt, double[] box) implements GridDef {

    /** The WebMercator sphere radius, meters. */
    static final double R = 6_378_137.0;

    /** Validates the transform. */
    public AffineGrid {
        if (gt.length != 6) {
            throw new IllegalArgumentException("affine needs gt0..gt5");
        }
        if (gt[1] * gt[5] - gt[2] * gt[4] == 0) {
            throw new IllegalArgumentException("affine geotransform is singular");
        }
        gt = gt.clone();
        box = box.clone();
    }

    /** The grid of {@code d}. */
    static AffineGrid of(GridDefinition d) {
        Map<String, Object> p = d.params();
        String crs = GridDef.text(p, "crs", "EPSG:4326");
        if (!crs.equals("EPSG:4326") && !crs.equals("EPSG:3857")) {
            throw new IllegalArgumentException("affine crs must be EPSG:4326 or EPSG:3857, was " + crs);
        }
        double[] gt = new double[6];
        for (int k = 0; k < 6; k++) {
            gt[k] = GridDef.num(p, "gt" + k, null);
        }
        return new AffineGrid(d.nx(), d.ny(), crs.equals("EPSG:3857"), gt, GridDef.box(d));
    }

    @Override
    public double[] lonLatToIndex(double lon, double lat) {
        if (Double.isNaN(lon) || Double.isNaN(lat)) {
            return GridDef.nowhere();
        }
        double x = mercator ? R * Math.toRadians(GridDef.normalizeLon(lon)) : lon;
        double y = mercator ? R * Math.log(Math.tan(Math.PI / 4 + Math.toRadians(lat) / 2)) : lat;
        double[] index = index(x, y);
        if (index == null) {
            // Prompt 054: a grid whose longitudes run past ±180° (a SatDump pass over the Bering Sea) is
            // looked up one turn east or west.
            double turn = mercator ? 2 * Math.PI * R : 360;
            index = index(x + turn, y);
            if (index == null) {
                index = index(x - turn, y);
            }
        }
        return index == null ? GridDef.nowhere() : index;
    }

    /** The continuous cell index of the projected point, or null when it is outside the grid. */
    private double[] index(double x, double y) {
        double det = gt[1] * gt[5] - gt[2] * gt[4];
        double u = x - gt[0];
        double v = y - gt[3];
        double col = (gt[5] * u - gt[2] * v) / det;
        double row = (-gt[4] * u + gt[1] * v) / det;
        double i = col - 0.5;
        double j = row - 0.5;
        return GridDef.inside(i, j, nx, ny) ? new double[] {i, j} : null;
    }

    @Override
    public double[] indexToLonLat(double i, double j) {
        double col = i + 0.5;
        double row = j + 0.5;
        double x = gt[0] + col * gt[1] + row * gt[2];
        double y = gt[3] + col * gt[4] + row * gt[5];
        if (!mercator) {
            return new double[] {GridDef.normalizeLon(x), y};
        }
        return new double[] {GridDef.normalizeLon(Math.toDegrees(x / R)),
            Math.toDegrees(2 * Math.atan(Math.exp(y / R)) - Math.PI / 2)};
    }

    @Override
    public double[] bounds() {
        return box.clone();
    }
}
