package ai.foresignal.space;

import ai.foresignal.source.spi.Geo;
import java.util.ArrayList;
import java.util.List;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;

/**
 * A predicted pass's ground track as a geometry (prompt 064): a {@code LineString}, or a
 * {@code MultiLineString} split where the track crosses ±180°, in SRID 4326, with coordinates rounded so that
 * an element refresh that moves the track by less than the rounding does not make a new record version.
 */
public final class GroundTracks {

    /** Decimals kept per coordinate (0.01° ≈ 1.1 km). */
    public static final int DECIMALS = 2;

    private GroundTracks() {}

    /**
     * The track of {@code points} ({@code {lon, lat}}), or null for fewer than two distinct points.
     *
     * @param points the sub-satellite points in time order
     */
    public static Geometry geometry(List<double[]> points) {
        if (points == null || points.size() < 2) {
            return null;
        }
        double scale = Math.pow(10, DECIMALS);
        List<List<Coordinate>> parts = new ArrayList<>();
        List<Coordinate> current = new ArrayList<>();
        double[] previous = null;
        for (double[] p : points) {
            double lon = Math.round(p[0] * scale) / scale;
            double lat = Math.round(p[1] * scale) / scale;
            if (previous != null && Math.abs(lon - previous[0]) > 180) {
                // Crossing ±180°: end this part at the meridian and start the next one on the other side.
                double edge = previous[0] > 0 ? 180 : -180;
                double span = 360 - Math.abs(lon - previous[0]);
                double t = span == 0 ? 0 : (edge - previous[0]) * (previous[0] > 0 ? 1 : -1) / span;
                double latEdge = Math.round((previous[1] + (lat - previous[1]) * t) * scale) / scale;
                current.add(new Coordinate(edge, latEdge));
                parts.add(current);
                current = new ArrayList<>();
                current.add(new Coordinate(-edge, latEdge));
            }
            Coordinate c = new Coordinate(lon, lat);
            if (current.isEmpty() || !current.get(current.size() - 1).equals2D(c)) {
                current.add(c);
            }
            previous = new double[] {lon, lat};
        }
        parts.add(current);
        List<LineString> lines = new ArrayList<>();
        for (List<Coordinate> part : parts) {
            if (part.size() >= 2) {
                lines.add(Geo.factory().createLineString(part.toArray(Coordinate[]::new)));
            }
        }
        if (lines.isEmpty()) {
            return null;
        }
        return lines.size() == 1 ? lines.get(0) : Geo.factory().createMultiLineString(lines.toArray(LineString[]::new));
    }
}
