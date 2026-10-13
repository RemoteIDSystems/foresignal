package ai.foresignal.source.adapters.wx;

import ai.foresignal.derive.HeadSnapshot;
import ai.foresignal.source.spi.Geo;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.locationtech.jts.geom.Point;

/**
 * The {@code cell} section of a place (prompt 052; Weather-Imagery-Design §7.2): the time for a ≥ 50 dBZ
 * core's projected centroid track to enter the place radius within 60 minutes.
 *
 * <p>Each current {@code radar-cell} record with {@code maxDbz ≥ 50} and a {@code motion} is projected from
 * its centroid along {@code motion.bearingDeg} (the direction it moves toward) at {@code motion.speedKmh},
 * starting from the record's observation time, minute by minute up to 60 minutes after {@code now}. The
 * first minute whose position lies within the radius is its ETA; already inside is 0. The smallest ETA
 * wins, rounded up to 5 minutes; none is {@value WxRound#NONE}. Pure.
 */
public final class CellEta {

    /** The look-ahead. */
    public static final Duration HORIZON = Duration.ofMinutes(60);

    /** The core threshold, dBZ. */
    public static final double CORE_DBZ = 50;

    private CellEta() {}

    /**
     * The {@code cell} metrics of a place.
     *
     * @param cells current {@code radar-cell} records
     */
    public static Map<String, Object> metrics(List<HeadSnapshot> cells, double lon, double lat, double radiusKm,
            Instant now) {
        double best = Double.NaN;
        HeadSnapshot bestCell = null;
        double bestSpeed = 0;
        double bestBearing = 0;
        for (HeadSnapshot c : cells) {
            if (c.tombstone() || c.geometry() == null || c.geometry().isEmpty()) {
                continue;
            }
            double dbz = number(c.payload().get("maxDbz"));
            if (Double.isNaN(dbz) || dbz < CORE_DBZ) {
                continue;
            }
            Point centroid = c.geometry().getCentroid();
            double speed = 0;
            double bearing = 0;
            if (c.payload().get("motion") instanceof Map<?, ?> motion) {
                speed = number(motion.get("speedKmh"));
                bearing = number(motion.get("bearingDeg"));
                if (Double.isNaN(speed)) {
                    speed = 0;
                }
                if (Double.isNaN(bearing)) {
                    bearing = 0;
                }
            }
            double eta = eta(centroid.getX(), centroid.getY(), speed, bearing,
                    c.observedAt() == null ? now : c.observedAt(), lon, lat, radiusKm, now);
            if (!Double.isNaN(eta) && (Double.isNaN(best) || eta < best)) {
                best = eta;
                bestCell = c;
                bestSpeed = speed;
                bestBearing = bearing;
            }
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("eta50Min", WxRound.etaMin(best));
        if (bestCell != null) {
            m.put("etaCellId", bestCell.externalId());
            m.put("etaSpeedKmh", WxRound.speedKmh(bestSpeed));
            m.put("etaBearingDeg", WxRound.bearing10(bestBearing));
        }
        return m;
    }

    /**
     * Minutes after {@code now} until a centroid moving at {@code speedKmh} toward {@code bearingDeg} from its
     * position at {@code observedAt} lies within {@code radiusKm} of the place, or NaN when not within the
     * horizon.
     */
    public static double eta(double cellLon, double cellLat, double speedKmh, double bearingDeg, Instant observedAt,
            double lon, double lat, double radiusKm, Instant now) {
        double elapsedMin = Math.max(0, Duration.between(observedAt, now).toMillis() / 60_000.0);
        for (int t = 0; t <= HORIZON.toMinutes(); t++) {
            double km = speedKmh * (elapsedMin + t) / 60.0;
            double[] p = destination(cellLat, cellLon, bearingDeg, km);
            if (Geo.haversineKm(lat, lon, p[0], p[1]) <= radiusKm) {
                return t;
            }
            if (speedKmh <= 0) {
                return Double.NaN;
            }
        }
        return Double.NaN;
    }

    /** The point {@code km} from {@code (lat, lon)} toward {@code bearingDeg}: {@code {lat, lon}}. */
    static double[] destination(double lat, double lon, double bearingDeg, double km) {
        double d = km / Geo.EARTH_RADIUS_KM;
        double b = Math.toRadians(bearingDeg);
        double p1 = Math.toRadians(lat);
        double l1 = Math.toRadians(lon);
        double p2 = Math.asin(Math.sin(p1) * Math.cos(d) + Math.cos(p1) * Math.sin(d) * Math.cos(b));
        double l2 = l1 + Math.atan2(Math.sin(b) * Math.sin(d) * Math.cos(p1), Math.cos(d) - Math.sin(p1) * Math.sin(p2));
        return new double[] {Math.toDegrees(p2), ((Math.toDegrees(l2) + 540) % 360) - 180};
    }

    private static double number(Object o) {
        return o instanceof Number n ? n.doubleValue() : Double.NaN;
    }
}
