package ai.foresignal.raster;

import ai.foresignal.source.spi.GridDefinition;
import java.util.Map;

/**
 * A Lambert conformal conic grid on a sphere (prompt 045), as HRRR uses: {@code lat1 = lat2 =
 * 38.5}, {@code lov = −97.5}, sphere radius 6371229 m, 3 km cells. Every value comes from the
 * definition; none is hard-coded.
 *
 * <p>Formulas: Snyder, <i>Map Projections — A Working Manual</i> (USGS Professional Paper 1395,
 * 1987), §15, spherical forward equations (15-1 to 15-5) and inverse (15-7 to 15-9), with the
 * tangent-cone case {@code n = sin(lat1)} when the standard parallels coincide. The false origin is
 * the pole of the cone ({@code ρ0 = 0}); the grid is positioned by its first point instead.
 *
 * <p>Parameters: {@code lat1}, {@code lat2} (standard parallels, degrees), {@code lov} (central
 * meridian, degrees), {@code radius} (meters; default 6371229), {@code la1}/{@code lo1} (the center
 * of the <em>south-west</em> cell, degrees, the first point of a GRIB2 grid scanned northwards as HRRR
 * is), {@code dx}/{@code dy} (meters). The decoder (046) flips rows so the {@code .fsg} payload runs
 * north to south: row {@code r} lies {@code (ny − 1 − r)·dy} north of the first point.
 *
 * @param nx columns
 * @param ny rows
 * @param n the cone constant
 * @param f the scale constant {@code F}
 * @param radius sphere radius, meters
 * @param lov central meridian, degrees
 * @param x1 projected x of the south-west cell center, meters
 * @param y1 projected y of the south-west cell center, meters
 * @param dx column step, meters
 * @param dy row step, meters (northwards)
 * @param box the definition's bounding box {@code [west, south, east, north]}
 */
public record LccGrid(int nx, int ny, double n, double f, double radius, double lov, double x1, double y1,
        double dx, double dy, double[] box) implements GridDef {

    /** The NCEP sphere radius used by HRRR, meters. */
    public static final double NCEP_RADIUS = 6_371_229.0;

    /** Validates the steps. */
    public LccGrid {
        if (!(dx > 0) || !(dy > 0) || !(radius > 0) || n == 0 || Double.isNaN(n)) {
            throw new IllegalArgumentException("lcc needs positive dx, dy, radius and a valid cone");
        }
        box = box.clone();
    }

    /** The grid of {@code d}. */
    static LccGrid of(GridDefinition d) {
        Map<String, Object> p = d.params();
        double lat1 = Math.toRadians(GridDef.num(p, "lat1", null));
        double lat2 = Math.toRadians(GridDef.num(p, "lat2", Math.toDegrees(lat1)));
        double lov = GridDef.num(p, "lov", null);
        double radius = GridDef.num(p, "radius", NCEP_RADIUS);
        double n = Math.abs(lat1 - lat2) < 1e-12 ? Math.sin(lat1)
                : Math.log(Math.cos(lat1) / Math.cos(lat2))
                        / Math.log(Math.tan(Math.PI / 4 + lat2 / 2) / Math.tan(Math.PI / 4 + lat1 / 2));
        double f = Math.cos(lat1) * Math.pow(Math.tan(Math.PI / 4 + lat1 / 2), n) / n;
        double[] origin = project(n, f, radius, lov, GridDef.num(p, "lo1", null), GridDef.num(p, "la1", null));
        return new LccGrid(d.nx(), d.ny(), n, f, radius, lov, origin[0], origin[1],
                Math.abs(GridDef.num(p, "dx", null)), Math.abs(GridDef.num(p, "dy", null)), GridDef.box(d));
    }

    /** Snyder (15-1) to (15-4) with {@code ρ0 = 0}: {@code {x, y}} in meters. */
    static double[] project(double n, double f, double radius, double lov, double lon, double lat) {
        double phi = Math.toRadians(lat);
        double rho = radius * f / Math.pow(Math.tan(Math.PI / 4 + phi / 2), n);
        double theta = n * Math.toRadians(GridDef.normalizeLon(lon - lov));
        return new double[] {rho * Math.sin(theta), -rho * Math.cos(theta)};
    }

    /** The projected meters of a WGS84 point. */
    public double[] forward(double lon, double lat) {
        if (Math.abs(lat) >= 90.0 && Math.signum(lat) == -Math.signum(n)) {
            return GridDef.nowhere();
        }
        return project(n, f, radius, lov, lon, lat);
    }

    /** Snyder (15-7) to (15-9) with {@code ρ0 = 0}: {@code {lon, lat}} in degrees. */
    public double[] inverse(double x, double y) {
        double sign = Math.signum(n);
        double rho = sign * Math.hypot(x, y);
        double theta = Math.atan2(sign * x, sign * -y);
        double lat = rho == 0 ? sign * 90.0
                : Math.toDegrees(2 * Math.atan(Math.pow(radius * f / rho, 1 / n)) - Math.PI / 2);
        double lon = GridDef.normalizeLon(lov + Math.toDegrees(theta / n));
        return new double[] {lon, lat};
    }

    @Override
    public double[] lonLatToIndex(double lon, double lat) {
        if (Double.isNaN(lon) || Double.isNaN(lat)) {
            return GridDef.nowhere();
        }
        double[] xy = forward(lon, lat);
        if (Double.isNaN(xy[0])) {
            return xy;
        }
        double i = (xy[0] - x1) / dx;
        double j = (ny - 1) - (xy[1] - y1) / dy;
        return GridDef.inside(i, j, nx, ny) ? new double[] {i, j} : GridDef.nowhere();
    }

    @Override
    public double[] indexToLonLat(double i, double j) {
        return inverse(x1 + i * dx, y1 + (ny - 1 - j) * dy);
    }

    @Override
    public double[] bounds() {
        return box.clone();
    }
}
