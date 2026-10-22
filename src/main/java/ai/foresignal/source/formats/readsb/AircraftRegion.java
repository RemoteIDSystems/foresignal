package ai.foresignal.source.formats.readsb;

import java.math.BigDecimal;
import java.util.List;
import org.locationtech.jts.geom.Envelope;

/**
 * A fixed circle an aircraft source polls: a name for log lines, a center and a radius in nautical miles.
 *
 * <p>The two fixed sets are Shadowbroker's (read for its timing and coverage only, nothing copied): the six
 * continent-sized circles of its main adsb.lol feed ({@link #WORLD}) and the eight 250 nm "blind spot" circles
 * it fills from airplanes.live and adsb.fi every 30 minutes ({@link #BLIND_SPOTS}).
 *
 * @param name a label for log lines
 * @param lat center latitude
 * @param lon center longitude
 * @param radiusNm radius in nautical miles
 */
public record AircraftRegion(String name, double lat, double lon, int radiusNm) {

    /** Nautical miles per degree of latitude. */
    static final double NM_PER_DEGREE = 60.0;

    /** The radius of every blind-spot circle. */
    public static final int BLIND_SPOT_RADIUS_NM = 250;

    /**
     * The six circles of the main adsb.lol feed: North America, Europe, Asia, Australia, Africa and South
     * America, 2,000 nm each and 2,500 nm for Africa.
     */
    public static final List<AircraftRegion> WORLD = List.of(
            new AircraftRegion("North America", 39.8, -98.5, 2_000),
            new AircraftRegion("Europe", 50.0, 15.0, 2_000),
            new AircraftRegion("Asia", 35.0, 105.0, 2_000),
            new AircraftRegion("Australia", -25.0, 133.0, 2_000),
            new AircraftRegion("Africa", 0.0, 20.0, 2_500),
            new AircraftRegion("South America", -15.0, -60.0, 2_000));

    /** The eight blind-spot circles the main feed covers thinly, 250 nm each. */
    public static final List<AircraftRegion> BLIND_SPOTS = List.of(
            new AircraftRegion("Yekaterinburg", 56.8, 60.6, BLIND_SPOT_RADIUS_NM),
            new AircraftRegion("Novosibirsk", 55.0, 82.9, BLIND_SPOT_RADIUS_NM),
            new AircraftRegion("Krasnoyarsk", 56.0, 92.9, BLIND_SPOT_RADIUS_NM),
            new AircraftRegion("Vladivostok", 43.1, 131.9, BLIND_SPOT_RADIUS_NM),
            new AircraftRegion("Urumqi", 43.8, 87.6, BLIND_SPOT_RADIUS_NM),
            new AircraftRegion("Chengdu", 30.6, 104.1, BLIND_SPOT_RADIUS_NM),
            new AircraftRegion("Lagos-Accra", 6.5, 3.4, BLIND_SPOT_RADIUS_NM),
            new AircraftRegion("Addis Ababa", 9.0, 38.7, BLIND_SPOT_RADIUS_NM));

    /** The center latitude as written in a request path: no exponent, no trailing zeros. */
    public String latText() {
        return plain(lat);
    }

    /** The center longitude as written in a request path: no exponent, no trailing zeros. */
    public String lonText() {
        return plain(lon);
    }

    /**
     * The bounding envelope of the circle: latitude clamped to the poles, the whole longitude range when the
     * circle reaches a pole or crosses the antimeridian.
     */
    public Envelope envelope() {
        double dLat = radiusNm / NM_PER_DEGREE;
        double minLat = Math.max(-90.0, lat - dLat);
        double maxLat = Math.min(90.0, lat + dLat);
        double cos = Math.cos(Math.toRadians(lat));
        if (minLat <= -90.0 || maxLat >= 90.0 || cos < 1e-6) {
            return new Envelope(-180.0, 180.0, minLat, maxLat);
        }
        double dLon = dLat / cos;
        double minLon = lon - dLon;
        double maxLon = lon + dLon;
        if (minLon < -180.0 || maxLon > 180.0) {
            return new Envelope(-180.0, 180.0, minLat, maxLat);
        }
        return new Envelope(minLon, maxLon, minLat, maxLat);
    }

    /** The envelope covering every circle of {@code regions}. */
    public static Envelope envelopeOf(List<AircraftRegion> regions) {
        Envelope all = new Envelope();
        regions.forEach(region -> all.expandToInclude(region.envelope()));
        return all;
    }

    private static String plain(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }
}
