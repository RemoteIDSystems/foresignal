package ai.foresignal.api.support;

import java.util.ArrayList;
import java.util.List;
import org.locationtech.jts.geom.Envelope;

/**
 * A parsed {@code near=lon,lat} with {@code radiusKm}.
 *
 * <p>The SQL first narrows candidates with {@link #prefilter()}, a conservative degree envelope
 * that an index can use ({@code geom && envelope}), then applies the exact
 * {@code ST_DWithin(geom::geography, point::geography, radiusMeters)}.
 *
 * @param lon the center longitude
 * @param lat the center latitude
 * @param radiusKm the radius in kilometers, in (0, 2000]
 */
public record NearFilter(double lon, double lat, double radiusKm) {

    /** Kilometers per degree of latitude, rounded down so the envelope is never too small. */
    static final double KM_PER_DEGREE_LAT = 110.5;

    /** The smallest cosine used for the longitude span; nearer a pole the span covers every longitude. */
    static final double MIN_COS = 0.01;

    /** Widens the envelope by 1% to absorb rounding. */
    static final double SAFETY = 1.01;

    /** The radius in meters. */
    public double radiusMeters() {
        return radiusKm * 1000.0;
    }

    /**
     * Degree envelopes that contain every point within the radius: one, or two when the span
     * crosses the antimeridian, or a full longitude band near a pole.
     */
    public List<Envelope> prefilter() {
        double dLat = radiusKm / KM_PER_DEGREE_LAT * SAFETY;
        double minLat = Math.max(-90.0, lat - dLat);
        double maxLat = Math.min(90.0, lat + dLat);
        double extremeLat = Math.min(90.0, Math.abs(lat) + dLat);
        double cos = Math.cos(Math.toRadians(extremeLat));
        if (cos < MIN_COS || extremeLat >= 90.0) {
            return List.of(new Envelope(-180.0, 180.0, minLat, maxLat));
        }
        double dLon = dLat / cos;
        if (dLon >= 180.0) {
            return List.of(new Envelope(-180.0, 180.0, minLat, maxLat));
        }
        double minLon = lon - dLon;
        double maxLon = lon + dLon;
        List<Envelope> result = new ArrayList<>(2);
        if (minLon < -180.0) {
            result.add(new Envelope(-180.0, maxLon, minLat, maxLat));
            result.add(new Envelope(minLon + 360.0, 180.0, minLat, maxLat));
        } else if (maxLon > 180.0) {
            result.add(new Envelope(minLon, 180.0, minLat, maxLat));
            result.add(new Envelope(-180.0, maxLon - 360.0, minLat, maxLat));
        } else {
            result.add(new Envelope(minLon, maxLon, minLat, maxLat));
        }
        return List.copyOf(result);
    }

    /** A stable text form, for cursor fingerprints and entity tags. */
    public String canonical() {
        return lon + "," + lat + "," + radiusKm;
    }
}
