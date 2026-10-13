package ai.foresignal.mapping;

/**
 * Where a record's geometry comes from (prompt 059).
 */
public sealed interface GeometrySpec {

    /**
     * The point used when the mapped values give none ({@code fallback: [lon, lat]}), or null.
     */
    double[] fallback();

    /**
     * A point from two values. A coordinate outside the valid range (an AIS sentinel such as lat 91 or lon 181)
     * leaves the geometry out, with a warning.
     *
     * @param lon the longitude
     * @param lat the latitude
     * @param fallback the point used when either value is absent or out of range, or null
     */
    record LonLat(ValueSpec lon, ValueSpec lat, double[] fallback) implements GeometrySpec {}

    /**
     * A GeoJSON geometry at a path.
     *
     * @param value where the geometry is read
     * @param fallback the point used when it is absent, or null
     */
    record GeoJson(ValueSpec value, double[] fallback) implements GeometrySpec {}

    /**
     * A constant point, such as a weather station's position.
     *
     * @param lon the longitude
     * @param lat the latitude
     */
    record ConstPoint(double lon, double lat) implements GeometrySpec {
        @Override
        public double[] fallback() {
            return null;
        }
    }
}
