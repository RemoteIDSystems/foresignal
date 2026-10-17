package ai.foresignal.observability;

import org.locationtech.jts.geom.Geometry;

/**
 * Where a detection probability is asked for (prompt 116): everywhere, a point, a polygon or a 040 region.
 * A polygon is judged at its representative point.
 *
 * @param lon the point's longitude, or null
 * @param lat the point's latitude, or null
 * @param polygon the polygon, or null
 * @param regionCode the region code, or null
 */
public record Place(Double lon, Double lat, Geometry polygon, String regionCode) {

    /** Everywhere. */
    public static final Place GLOBAL = new Place(null, null, null, null);

    /** A point. */
    public static Place point(double lon, double lat) {
        return new Place(lon, lat, null, null);
    }

    /** A polygon (judged at its representative point). */
    public static Place polygon(Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) {
            return GLOBAL;
        }
        org.locationtech.jts.geom.Point p = geometry.getInteriorPoint();
        return new Place(p.getX(), p.getY(), geometry, null);
    }

    /** A 040 region. */
    public static Place region(String code) {
        return new Place(null, null, null, code);
    }

    /** True when the place has a representative point. */
    public boolean located() {
        return lon != null && lat != null;
    }

    /** True for everywhere. */
    public boolean global() {
        return !located() && regionCode == null;
    }
}
