package ai.foresignal.raster;

import ai.foresignal.source.spi.GridDefinition;
import java.util.Map;

/**
 * The normalized geostationary projection (prompt 045): the GOES-R ABI fixed grid and the CGMS
 * LRIT/HRIT normalized geostationary projection.
 *
 * <p>Formulas: GOES-R Series Product Definition and Users' Guide (PUG), Volume 3 (Level 1b
 * products), §5.1.2.8 "Navigation of Image Data" (ABI fixed grid: scan angle to geodetic latitude
 * and longitude, and the reverse), as reproduced by the NOAA STAR "GOES Imager Projection (ABI Fixed
 * Grid)" training page; and the CGMS LRIT/HRIT Global Specification (CGMS 03), §4.4.3.2
 * "Normalized Geostationary Projection", for the {@code sweep = y} (Meteosat-style) axis order.
 * Written from those public formulas; no SatDump or GPL code (Weather-Imagery-Design §3).
 *
 * <p>The Earth is the ellipsoid {@code (a, b)}; the satellite sits at distance {@code H = h + a}
 * from its center above longitude {@code lon0}. A ground point maps to the scan angles {@code (x, y)}
 * (radians) of the instrument; {@code x} grows eastwards and {@code y} northwards.
 * <ul>
 *   <li>{@code sweep = x} (GOES): {@code y = atan(sz / sx)}, {@code x = asin(sy / |s|)};
 *   <li>{@code sweep = y} (Meteosat): {@code x = atan(sy / sx)}, {@code y = atan(sz / √(sx² + sy²))};
 * </ul>
 * where {@code s} is the vector from the satellite to the point, {@code sx} pointing at the Earth's
 * center. A point is visible when the vector from it to the satellite has a positive component
 * along the ellipsoid normal; points beyond the visible disc give NaN.
 *
 * <p>Parameters: {@code lon0} (degrees), {@code h} (perspective point height above the ellipsoid,
 * meters; required), {@code a} and {@code b} (default GRS80, 6378137 and 6356752.31414),
 * {@code sweep} ({@code x} by default), and the scan angles of the cell centers: column {@code c} is
 * at {@code x0 + c·dx}, row {@code r} at {@code y0 + r·dy} (radians; {@code dy} is normally negative,
 * because rows run north to south).
 *
 * @param nx columns
 * @param ny rows
 * @param lon0 sub-satellite longitude, degrees
 * @param h perspective point height above the ellipsoid, meters
 * @param a semi-major axis, meters
 * @param b semi-minor axis, meters
 * @param sweepX true for the GOES ({@code x}) sweep axis
 * @param x0 scan angle of column 0, radians
 * @param dx column step, radians
 * @param y0 scan angle of row 0, radians
 * @param dy row step, radians
 * @param box the definition's bounding box {@code [west, south, east, north]}
 */
public record GeosGrid(int nx, int ny, double lon0, double h, double a, double b, boolean sweepX, double x0,
        double dx, double y0, double dy, double[] box) implements GridDef {

    /** GRS80 semi-major axis, meters. */
    public static final double GRS80_A = 6_378_137.0;

    /** GRS80 semi-minor axis, meters (as in the GOES-R {@code goes_imager_projection}). */
    public static final double GRS80_B = 6_356_752.31414;

    /** Validates the parameters. */
    public GeosGrid {
        if (!(h > 0) || !(a > 0) || !(b > 0) || b > a || dx == 0 || dy == 0) {
            throw new IllegalArgumentException("geos needs h > 0, 0 < b <= a and non-zero dx, dy");
        }
        box = box.clone();
    }

    /** The grid of {@code d}. */
    static GeosGrid of(GridDefinition d) {
        Map<String, Object> p = d.params();
        String sweep = GridDef.text(p, "sweep", "x");
        if (!sweep.equals("x") && !sweep.equals("y")) {
            throw new IllegalArgumentException("geos sweep must be x or y, was " + sweep);
        }
        return new GeosGrid(d.nx(), d.ny(), GridDef.num(p, "lon0", null), GridDef.num(p, "h", null),
                GridDef.num(p, "a", GRS80_A), GridDef.num(p, "b", GRS80_B), sweep.equals("x"),
                GridDef.num(p, "x0", null), GridDef.num(p, "dx", null), GridDef.num(p, "y0", null),
                GridDef.num(p, "dy", null), GridDef.box(d));
    }

    /**
     * The scan angles of a WGS84 point (PUG Vol. 3 §5.1.2.8.2, geodetic to fixed grid).
     *
     * @return {@code {x, y}} in radians, or {@code {NaN, NaN}} beyond the visible disc
     */
    public double[] scanAngles(double lon, double lat) {
        double phi = Math.toRadians(lat);
        double dLambda = Math.toRadians(lon - lon0);
        double e2 = (a * a - b * b) / (a * a);
        double phiC = Math.atan((b * b) / (a * a) * Math.tan(phi));
        double cosC = Math.cos(phiC);
        double rc = b / Math.sqrt(1 - e2 * cosC * cosC);
        double px = rc * cosC * Math.cos(dLambda);
        double py = rc * cosC * Math.sin(dLambda);
        double pz = rc * Math.sin(phiC);
        double bigH = h + a;
        double sx = bigH - px;
        // Visible when (S − P) · normal(P) > 0, with normal ∝ (px/a², py/a², pz/b²).
        if (sx * px - py * py - (a * a) / (b * b) * pz * pz <= 0) {
            return GridDef.nowhere();
        }
        double x;
        double y;
        if (sweepX) {
            double norm = Math.sqrt(sx * sx + py * py + pz * pz);
            x = Math.asin(py / norm);
            y = Math.atan(pz / sx);
        } else {
            x = Math.atan(py / sx);
            y = Math.atan(pz / Math.hypot(sx, py));
        }
        return new double[] {x, y};
    }

    /**
     * The WGS84 position at scan angles {@code (x, y)} (PUG Vol. 3 §5.1.2.8.1, fixed grid to
     * geodetic).
     *
     * @return {@code {lon, lat}} in degrees, or {@code {NaN, NaN}} when the ray misses the Earth
     */
    public double[] lonLatAt(double x, double y) {
        // Direction of the ray from the satellite, towards the Earth along -X, in units of its X part.
        double vy;
        double vz;
        if (sweepX) {
            vz = Math.tan(y);
            vy = Math.tan(x) * Math.hypot(1, vz);
        } else {
            vy = Math.tan(x);
            vz = Math.tan(y) * Math.hypot(1, vy);
        }
        double bigH = h + a;
        double ratio = (a * a) / (b * b);
        double qa = 1 + vy * vy + ratio * vz * vz;
        double qb = -2 * bigH;
        double qc = bigH * bigH - a * a;
        double disc = qb * qb - 4 * qa * qc;
        if (disc < 0) {
            return GridDef.nowhere();
        }
        double k = (-qb - Math.sqrt(disc)) / (2 * qa);
        double px = bigH - k;
        double py = k * vy;
        double pz = k * vz;
        double lon = lon0 + Math.toDegrees(Math.atan2(py, px));
        double lat = Math.toDegrees(Math.atan(ratio * pz / Math.hypot(px, py)));
        return new double[] {GridDef.normalizeLon(lon), lat};
    }

    @Override
    public double[] lonLatToIndex(double lon, double lat) {
        double[] xy = scanAngles(lon, lat);
        if (Double.isNaN(xy[0])) {
            return xy;
        }
        double i = (xy[0] - x0) / dx;
        double j = (xy[1] - y0) / dy;
        return GridDef.inside(i, j, nx, ny) ? new double[] {i, j} : GridDef.nowhere();
    }

    @Override
    public double[] indexToLonLat(double i, double j) {
        return lonLatAt(x0 + i * dx, y0 + j * dy);
    }

    @Override
    public double[] bounds() {
        return box.clone();
    }
}
