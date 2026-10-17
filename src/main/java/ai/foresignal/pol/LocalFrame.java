package ai.foresignal.pol;

import ai.foresignal.kinematics.GeoMath;
import java.util.Arrays;
import java.util.List;

/**
 * A local equirectangular frame in meters around a subject's median position (prompt 114): what DBSCAN and the
 * place statistics compute in. Accurate for the tens of kilometers a subject's places span.
 *
 * @param lon0 the origin longitude (median)
 * @param lat0 the origin latitude (median)
 */
public record LocalFrame(double lon0, double lat0) {

    /** The frame around the median of {@code lonLat} ({@code {lon, lat}} pairs); the origin when empty. */
    public static LocalFrame around(List<double[]> lonLat) {
        if (lonLat.isEmpty()) {
            return new LocalFrame(0, 0);
        }
        double[] lons = lonLat.stream().mapToDouble(p -> p[0]).toArray();
        double[] lats = lonLat.stream().mapToDouble(p -> p[1]).toArray();
        return new LocalFrame(median(lons), median(lats));
    }

    static double median(double[] values) {
        double[] sorted = values.clone();
        Arrays.sort(sorted);
        int mid = sorted.length / 2;
        return sorted.length % 2 == 1 ? sorted[mid] : (sorted[mid - 1] + sorted[mid]) / 2;
    }

    /** {@code (lon, lat)} as {@code {x, y}} meters east and north of the origin. */
    public double[] toXy(double lon, double lat) {
        double cos = Math.cos(Math.toRadians(lat0));
        return new double[] {GeoMath.normalizeLon(lon - lon0) * GeoMath.METERS_PER_DEGREE_LON * cos,
                (lat - lat0) * GeoMath.METERS_PER_DEGREE_LAT};
    }

    /** {@code {x, y}} meters back to {@code {lon, lat}}. */
    public double[] toLonLat(double x, double y) {
        double cos = Math.max(1e-9, Math.cos(Math.toRadians(lat0)));
        double lon = lon0 + x / (GeoMath.METERS_PER_DEGREE_LON * cos);
        lon = ((lon + 540) % 360) - 180;
        return new double[] {lon, lat0 + y / GeoMath.METERS_PER_DEGREE_LAT};
    }
}
