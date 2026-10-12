package ai.foresignal.store.track;

/**
 * One point of a track.
 *
 * @param lon longitude in degrees; may lie outside ±180 while a segment is unwrapped
 * @param lat latitude in degrees
 * @param time epoch milliseconds ({@code observed_at})
 * @param keep true when simplification must keep the point (a FIRST, REAPPEAR, SIGNIFICANT or
 *     TRIGGER_EDGE version); segment endpoints are always kept
 * @param values the requested numeric payload fields, parallel to the requested names; an entry is
 *     null when the field is absent or not a number
 */
public record TrackPoint(double lon, double lat, long time, boolean keep, Double[] values) {

    /** Replaces a null value array with an empty one. */
    public TrackPoint {
        values = values == null ? new Double[0] : values;
    }

    /** This point with longitude {@code value}. */
    public TrackPoint withLon(double value) {
        return new TrackPoint(value, lat, time, keep, values);
    }
}
