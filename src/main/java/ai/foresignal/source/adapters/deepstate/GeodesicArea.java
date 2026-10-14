package ai.foresignal.source.adapters.deepstate;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;

/**
 * The area of a longitude/latitude polygon on the WGS84 ellipsoid (prompt 081: {@code areaKm2} of the DeepState
 * occupied area).
 *
 * <p>Each latitude is mapped to its <em>authalic</em> latitude, which takes the ellipsoid onto a sphere of equal
 * area (radius {@code R_q}, Snyder, "Map Projections — A Working Manual", 1987, eq. 3-11 to 3-14), and each ring's
 * area on that sphere is {@code R_q² / 2 · |Σ (λ_{i+1} − λ_{i−1}) · sin β_i|} (the trapezoid sum on the sphere).
 * Holes are subtracted. For the short edges of a mapped boundary the result agrees with a geodesic area well within
 * 0.1 %; no geodesy library is needed.
 */
final class GeodesicArea {

    /** WGS84 semi-major axis, meters. */
    static final double A = 6_378_137.0;

    /** WGS84 flattening. */
    static final double F = 1.0 / 298.257_223_563;

    private static final double E2 = F * (2 - F);
    private static final double E = Math.sqrt(E2);
    private static final double QP = q(1.0);

    /** The authalic radius, meters. */
    static final double RQ = A * Math.sqrt(QP / 2.0);

    private GeodesicArea() {}

    /**
     * The area of {@code geometry}'s polygons, square kilometers; 0 for a geometry without polygons.
     *
     * @param geometry a Polygon, MultiPolygon or collection in longitude/latitude degrees
     */
    static double km2(Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) {
            return 0.0;
        }
        double m2 = 0.0;
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
            Geometry part = geometry.getGeometryN(i);
            if (part instanceof Polygon polygon) {
                m2 += Math.abs(ringM2(polygon.getExteriorRing()));
                for (int h = 0; h < polygon.getNumInteriorRing(); h++) {
                    m2 -= Math.abs(ringM2(polygon.getInteriorRingN(h)));
                }
            } else if (part != geometry) {
                m2 += km2(part) * 1.0e6;
            }
        }
        return Math.max(0.0, m2) / 1.0e6;
    }

    /** The signed area of one closed ring on the authalic sphere, square meters. */
    static double ringM2(LineString ring) {
        Coordinate[] c = ring.getCoordinates();
        int n = c.length - 1; // the closing point repeats the first
        if (n < 3) {
            return 0.0;
        }
        double sum = 0.0;
        for (int i = 0; i < n; i++) {
            Coordinate prev = c[(i - 1 + n) % n];
            Coordinate next = c[(i + 1) % n];
            double dLon = Math.toRadians(normalize(next.x - prev.x));
            sum += dLon * sinAuthalic(c[i].y);
        }
        return RQ * RQ * sum / 2.0;
    }

    /** {@code sin β} of the authalic latitude of {@code latDeg}. */
    static double sinAuthalic(double latDeg) {
        return q(Math.sin(Math.toRadians(latDeg))) / QP;
    }

    /** Snyder's {@code q} for {@code sin φ}. */
    private static double q(double sinPhi) {
        double es = E * sinPhi;
        return (1 - E2) * (sinPhi / (1 - E2 * sinPhi * sinPhi) - (1 / (2 * E)) * Math.log((1 - es) / (1 + es)));
    }

    /** A longitude difference in (−180, 180]. */
    private static double normalize(double degrees) {
        double d = degrees % 360.0;
        if (d > 180.0) {
            d -= 360.0;
        } else if (d <= -180.0) {
            d += 360.0;
        }
        return d;
    }
}
