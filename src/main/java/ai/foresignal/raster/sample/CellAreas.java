package ai.foresignal.raster.sample;

import ai.foresignal.raster.GridDef;
import ai.foresignal.raster.LatLonGrid;
import ai.foresignal.raster.LccGrid;
import ai.foresignal.source.spi.Geo;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;

/**
 * Cell and polygon areas on the sphere (prompt 052), per grid type, for {@code /stats}, {@code wx-areas}
 * and {@code wx-cells}.
 *
 * <ul>
 *   <li>{@code latlon}: exact, {@code R² · Δλ · (sin φ_north − sin φ_south)};
 *   <li>{@code lcc}: the projected cell {@code dx · dy} divided by the conformal area scale {@code k²},
 *       {@code k = n·ρ / (R·cos φ)} at the cell center (Snyder 1987, (15-4));
 *   <li>any other grid: the spherical area of the quadrilateral through the four cell corners.
 * </ul>
 * The Earth radius is {@link Geo#EARTH_RADIUS_KM} for the sphere formulas, and the grid's own radius
 * for {@code lcc}.
 */
public final class CellAreas {

    private CellAreas() {}

    /**
     * The area of the cell centered at column {@code c}, row {@code r}, in km².
     *
     * @return the area, or 0 when the cell does not lie on the Earth
     */
    public static double cellAreaKm2(GridDef grid, int c, int r) {
        if (grid instanceof LatLonGrid ll) {
            double north = Math.min(90, ll.la1() - (r - 0.5) * ll.dy());
            double south = Math.max(-90, ll.la1() - (r + 0.5) * ll.dy());
            double rr = Geo.EARTH_RADIUS_KM;
            return rr * rr * Math.toRadians(ll.dx()) * Math.abs(Math.sin(Math.toRadians(north))
                    - Math.sin(Math.toRadians(south)));
        }
        if (grid instanceof LccGrid lcc) {
            double[] ll = lcc.indexToLonLat(c, r);
            if (Double.isNaN(ll[1])) {
                return 0;
            }
            double phi = Math.toRadians(ll[1]);
            double rho = lcc.radius() * lcc.f() / Math.pow(Math.tan(Math.PI / 4 + phi / 2), lcc.n());
            double k = lcc.n() * rho / (lcc.radius() * Math.cos(phi));
            return (lcc.dx() * lcc.dy()) / (k * k) / 1e6;
        }
        double[] a = grid.indexToLonLat(c - 0.5, r - 0.5);
        double[] b = grid.indexToLonLat(c + 0.5, r - 0.5);
        double[] d = grid.indexToLonLat(c + 0.5, r + 0.5);
        double[] e = grid.indexToLonLat(c - 0.5, r + 0.5);
        if (Double.isNaN(a[0]) || Double.isNaN(b[0]) || Double.isNaN(d[0]) || Double.isNaN(e[0])) {
            return 0;
        }
        return ringAreaKm2(new double[][] {a, b, d, e});
    }

    /**
     * The spherical area of a closed ring of {@code {lon, lat}} points (closing point optional), km²:
     * the Chamberlain–Duquette formula {@code R²/2 · |Σ (λ_{i+1} − λ_{i−1}) · sin φ_i|}, longitudes unwrapped.
     */
    public static double ringAreaKm2(double[][] ring) {
        int n = ring.length;
        if (n > 1 && ring[0][0] == ring[n - 1][0] && ring[0][1] == ring[n - 1][1]) {
            n--;
        }
        if (n < 3) {
            return 0;
        }
        double[] lon = new double[n];
        lon[0] = ring[0][0];
        for (int i = 1; i < n; i++) {
            double d = ring[i][0] - ring[i - 1][0];
            d = ((d + 540) % 360) - 180;
            lon[i] = lon[i - 1] + d;
        }
        double sum = 0;
        for (int i = 0; i < n; i++) {
            double next = Math.toRadians(lon[(i + 1) % n]);
            double prev = Math.toRadians(lon[(i - 1 + n) % n]);
            sum += (next - prev) * Math.sin(Math.toRadians(ring[i][1]));
        }
        double r = Geo.EARTH_RADIUS_KM;
        return Math.abs(sum) * r * r / 2;
    }

    /** The spherical area of a polygonal geometry (holes subtracted), km²; 0 for points and lines. */
    public static double geometryAreaKm2(Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) {
            return 0;
        }
        double total = 0;
        for (int g = 0; g < geometry.getNumGeometries(); g++) {
            if (geometry.getGeometryN(g) instanceof Polygon p) {
                total += ringArea(p.getExteriorRing().getCoordinates());
                for (int h = 0; h < p.getNumInteriorRing(); h++) {
                    total -= ringArea(p.getInteriorRingN(h).getCoordinates());
                }
            }
        }
        return Math.max(0, total);
    }

    private static double ringArea(Coordinate[] coordinates) {
        double[][] ring = new double[coordinates.length][];
        for (int i = 0; i < coordinates.length; i++) {
            ring[i] = new double[] {coordinates[i].x, coordinates[i].y};
        }
        return ringAreaKm2(ring);
    }
}
