package ai.foresignal.orbit;

import ai.foresignal.raster.geotiff.Antimeridian;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.GeometryFixer;
import org.locationtech.jts.operation.union.UnaryUnionOp;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.data.DataContext;
import org.orekit.frames.Frame;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.propagation.analytical.tle.TLEPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;

/**
 * The approximate swath of a SatDump pass that has no usable GeoTIFF (prompt 054,
 * Weather-Imagery-Design §4.5 item 3): the SGP4 ground track from the pass's TLE between the first and
 * last product timestamps, at {@value #STEP_SECONDS}-second steps, widened by half the swath on each
 * side of the track, split at ±180°. The outline is the union of one quadrilateral per step, each
 * split at ±180° on its own; a step whose edges straddle a pole is left out.
 *
 * <p>The width is the product's {@code corr_swath} (km) when it has one, else a per-instrument table
 * ({@link #TABLE_KM}: MSU-MR about 2,800 km, AVHRR about 2,900 km, MHS about 2,200 km). The cross-track
 * offset is a great-circle step perpendicular to the track's bearing on a sphere of the WGS84
 * equatorial radius: an outline for the map and for geometry queries, never a georeferenced image.
 * Nothing here reimplements SatDump's raytracer, and no tiles are ever made from a swath.
 */
public final class SwathEstimator {

    /** The propagation step. */
    public static final int STEP_SECONDS = 10;

    /** The longest window propagated (a polar pass is at most about 16 minutes). */
    public static final Duration MAX_WINDOW = Duration.ofMinutes(30);

    /** The swath widths by instrument, km. */
    public static final Map<String, Double> TABLE_KM = Map.of("msu-mr", 2800.0, "avhrr", 2900.0, "mhs", 2200.0);

    private static final double EARTH_RADIUS_KM = Constants.WGS84_EARTH_EQUATORIAL_RADIUS / 1000.0;

    /**
     * An estimated swath.
     *
     * @param footprint the outline, SRID 4326, split at ±180°
     * @param widthKm the full swath width used
     * @param points the ground-track points propagated
     */
    public record Swath(MultiPolygon footprint, double widthKm, int points) {}

    private SwathEstimator() {}

    /**
     * The swath width: {@code corrSwath} when positive, else the instrument's table entry.
     *
     * @param corrSwath the product's {@code corr_swath} in km, or null
     * @param instrument the instrument ({@code msu_mr}, {@code MSU-MR}, {@code avhrr_3}, …), or null
     * @return the width in km, or empty when neither is known
     */
    public static Optional<Double> widthKm(Double corrSwath, String instrument) {
        if (corrSwath != null && corrSwath > 0 && Double.isFinite(corrSwath)) {
            return Optional.of(corrSwath);
        }
        if (instrument == null) {
            return Optional.empty();
        }
        String key = instrument.toLowerCase(Locale.ROOT).replace('_', '-');
        for (Map.Entry<String, Double> e : TABLE_KM.entrySet()) {
            if (key.equals(e.getKey()) || key.startsWith(e.getKey() + "-") || key.startsWith(e.getKey())) {
                return Optional.of(e.getValue());
            }
        }
        return Optional.empty();
    }

    /**
     * Estimates the swath.
     *
     * @param line1 TLE line 1
     * @param line2 TLE line 2
     * @param start the first product timestamp
     * @param end the last product timestamp (at least {@code start}; capped at {@link #MAX_WINDOW})
     * @param widthKm the full swath width, km
     * @return the swath, or empty when the TLE is unusable or the track is degenerate
     */
    public static Optional<Swath> estimate(String line1, String line2, Instant start, Instant end, double widthKm) {
        if (line1 == null || line2 == null || start == null || end == null || !(widthKm > 0)) {
            return Optional.empty();
        }
        Instant last = end.isBefore(start) ? start : end;
        if (Duration.between(start, last).compareTo(MAX_WINDOW) > 0) {
            last = start.plus(MAX_WINDOW);
        }
        List<double[]> track;
        try {
            track = groundTrack(line1.strip(), line2.strip(), start, last);
        } catch (RuntimeException e) {
            return Optional.empty();
        }
        if (track.size() < 2) {
            return Optional.empty();
        }
        double half = widthKm / 2 / EARTH_RADIUS_KM;
        List<Coordinate> left = new ArrayList<>();
        List<Coordinate> right = new ArrayList<>();
        for (int i = 0; i < track.size(); i++) {
            double[] p = track.get(i);
            double[] a = track.get(Math.max(0, i - 1));
            double[] b = track.get(Math.min(track.size() - 1, i + 1));
            double bearing = bearing(a, b);
            left.add(destination(p, bearing - Math.PI / 2, half));
            right.add(destination(p, bearing + Math.PI / 2, half));
        }
        // One quadrilateral per track step, each unwrapped and split on its own, then unioned: a long
        // swath can wind across ±180° (and pass near a pole) in ways one unwrapped ring cannot follow.
        List<Geometry> quads = new ArrayList<>();
        for (int i = 0; i + 1 < track.size(); i++) {
            Coordinate[] quad = {new Coordinate(left.get(i)), new Coordinate(left.get(i + 1)),
                new Coordinate(right.get(i + 1)), new Coordinate(right.get(i)), new Coordinate(left.get(i))};
            Antimeridian.unwrap(quad);
            quad[4] = new Coordinate(quad[0]);
            double minX = Double.POSITIVE_INFINITY;
            double maxX = Double.NEGATIVE_INFINITY;
            for (Coordinate c : quad) {
                minX = Math.min(minX, c.x);
                maxX = Math.max(maxX, c.x);
            }
            if (maxX - minX >= 180) {
                continue; // a step whose edges straddle a pole: left out rather than drawn inside out
            }
            Geometry polygon = GeometryFixer.fix(Antimeridian.GEOMETRY.createPolygon(quad));
            MultiPolygon split = Antimeridian.split(polygon);
            if (!split.isEmpty()) {
                quads.add(split);
            }
        }
        if (quads.isEmpty()) {
            return Optional.empty();
        }
        Geometry union = GeometryFixer.fix(UnaryUnionOp.union(quads));
        List<Polygon> parts = new ArrayList<>();
        for (int i = 0; i < union.getNumGeometries(); i++) {
            if (union.getGeometryN(i) instanceof Polygon p && !p.isEmpty()) {
                parts.add(p);
            }
        }
        if (parts.isEmpty()) {
            return Optional.empty();
        }
        MultiPolygon footprint = Antimeridian.GEOMETRY.createMultiPolygon(parts.toArray(Polygon[]::new));
        return Optional.of(new Swath(footprint, widthKm, track.size()));
    }

    /**
     * The SGP4 sub-satellite points {@code [lonDeg, latDeg]} from {@code start} to {@code end} at
     * {@value #STEP_SECONDS}-second steps (both ends included).
     */
    static List<double[]> groundTrack(String line1, String line2, Instant start, Instant end) {
        DataContext context = OrekitData.context();
        TimeScale utc = context.getTimeScales().getUTC();
        TLE tle = new TLE(line1, line2, utc);
        Frame teme = context.getFrames().getTEME();
        Frame itrf = context.getFrames().getITRF(IERSConventions.IERS_2010, true);
        TLEPropagator propagator = TLEPropagator.selectExtrapolator(tle, teme);
        OneAxisEllipsoid earth = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
                Constants.WGS84_EARTH_FLATTENING, itrf);
        List<double[]> out = new ArrayList<>();
        long seconds = Math.max(0, Duration.between(start, end).toSeconds());
        for (long t = 0; ; t += STEP_SECONDS) {
            long at = Math.min(t, seconds);
            AbsoluteDate date = new AbsoluteDate(start.plusSeconds(at), utc);
            Vector3D position = propagator.getPVCoordinates(date).getPosition();
            Vector3D fixed = teme.getStaticTransformTo(itrf, date).transformPosition(position);
            GeodeticPoint point = earth.transform(fixed, itrf, date);
            out.add(new double[] {Math.toDegrees(point.getLongitude()), Math.toDegrees(point.getLatitude())});
            if (at >= seconds) {
                break;
            }
        }
        return out;
    }

    /** The initial great-circle bearing from {@code a} to {@code b}, radians clockwise from north. */
    static double bearing(double[] a, double[] b) {
        double lat1 = Math.toRadians(a[1]);
        double lat2 = Math.toRadians(b[1]);
        double dLon = Math.toRadians(b[0] - a[0]);
        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
        return Math.atan2(y, x);
    }

    /** The point {@code distance} radians from {@code p} along {@code bearing}, as a coordinate. */
    static Coordinate destination(double[] p, double bearing, double distance) {
        double lat1 = Math.toRadians(p[1]);
        double lon1 = Math.toRadians(p[0]);
        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(distance)
                + Math.cos(lat1) * Math.sin(distance) * Math.cos(bearing));
        double lon2 = lon1 + Math.atan2(Math.sin(bearing) * Math.sin(distance) * Math.cos(lat1),
                Math.cos(distance) - Math.sin(lat1) * Math.sin(lat2));
        return new Coordinate(Math.toDegrees(lon2), Math.toDegrees(lat2));
    }
}
