package ai.foresignal.raster.model;

import ai.foresignal.raster.GridDef;
import ai.foresignal.raster.LccGrid;

/**
 * Rotates grid-relative wind components to earth-relative (prompt 051).
 *
 * <p>GRIB2 flag table 3.3 ("resolution and component flags"), bit 5: when set, {@code u} and
 * {@code v} are resolved along the grid's {@code x} and {@code y} axes rather than east and north.
 * HRRR's Lambert conformal grid sets it (octet 47 of its template 3.30 section is {@code 0x08},
 * verified on the 2026-10-12 12z run); GFS's latitude/longitude grid does not.
 *
 * <p>On a Lambert conformal conic grid the angle between grid north and true north at longitude
 * {@code λ} is {@code α = n·(λ − λ₀)}, with the cone constant {@code n} ({@code sin φ₁} for the
 * tangent cone HRRR uses: {@code φ₁ = 38.5°}, {@code λ₀ = −97.5°}). Then
 * {@code u_e = u·cos α + v·sin α} and {@code v_e = −u·sin α + v·cos α}.
 */
public final class WindRotation {

    /** GRIB2 flag table 3.3, bit 5 (bit 1 is the most significant): components are grid-relative. */
    public static final int GRID_RELATIVE_FLAG = 0x08;

    private WindRotation() {}

    /** True when the resolution and component flags say the winds are grid-relative. */
    public static boolean gridRelative(int resolutionAndComponentFlags) {
        return (resolutionAndComponentFlags & GRID_RELATIVE_FLAG) != 0;
    }

    /** The rotation angle at longitude {@code lon} on a cone {@code n} with central meridian {@code lov}, radians. */
    public static double angle(double n, double lov, double lon) {
        double d = GridDef.normalizeLon(lon - lov);
        return n * Math.toRadians(d);
    }

    /**
     * Rotates one vector.
     *
     * @return {@code {u_e, v_e}}
     */
    public static double[] rotate(double u, double v, double alpha) {
        double c = Math.cos(alpha);
        double s = Math.sin(alpha);
        return new double[] {u * c + v * s, -u * s + v * c};
    }

    /**
     * Rotates every cell of a Lambert conformal grid in place (rows north to south, as stored).
     *
     * @param grid the grid
     * @param u grid-relative x components, replaced with eastward ones
     * @param v grid-relative y components, replaced with northward ones
     * @throws IllegalArgumentException when the arrays do not fit the grid
     */
    public static void toEarthRelative(LccGrid grid, float[] u, float[] v) {
        int cells = grid.nx() * grid.ny();
        if (u.length != cells || v.length != cells) {
            throw new IllegalArgumentException("Wind components have " + u.length + "/" + v.length + " samples; the "
                    + grid.nx() + "x" + grid.ny() + " grid needs " + cells);
        }
        for (int j = 0; j < grid.ny(); j++) {
            for (int i = 0; i < grid.nx(); i++) {
                int k = j * grid.nx() + i;
                double lon = grid.indexToLonLat(i, j)[0];
                double alpha = angle(grid.n(), grid.lov(), lon);
                double c = Math.cos(alpha);
                double s = Math.sin(alpha);
                float gu = u[k];
                float gv = v[k];
                u[k] = (float) (gu * c + gv * s);
                v[k] = (float) (-gu * s + gv * c);
            }
        }
    }
}
