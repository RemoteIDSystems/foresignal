package ai.foresignal.sink;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * BaseStation SBS-1 {@code MSG} lines (prompt 110). Message types 1 (callsign), 3 (position) and 4
 * (velocity); fields that do not apply are left empty.
 */
public final class SbsEncoder {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy/MM/dd").withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneOffset.UTC);

    private SbsEncoder() {}

    /** {@code MSG,1} on a callsign. */
    public static String callsign(String hex, String callsign, Instant at) {
        return line(1, hex, at, callsign, null, null, null, null, null, null, null, null, null);
    }

    /** {@code MSG,3} on a position. */
    public static String position(String hex, String callsign, Integer altFt, Double gsKt, Double track, double lat,
            double lon, Boolean onGround, Instant at) {
        return line(3, hex, at, callsign, altFt, gsKt, track, lat, lon, null, null, null, onGround);
    }

    /** {@code MSG,4} on a velocity. */
    public static String velocity(String hex, String callsign, Double gsKt, Double track, Integer vrateFtMin,
            Instant at) {
        return line(4, hex, at, callsign, null, gsKt, track, null, null, vrateFtMin, null, null, null);
    }

    private static String line(int type, String hex, Instant at, String callsign, Integer alt, Double gs, Double track,
            Double lat, Double lon, Integer vrate, String squawk, String onGroundExtra, Boolean onGround) {
        StringBuilder out = new StringBuilder("MSG,").append(type).append(",1,1,").append(hex == null ? "" : hex)
                .append(",1,").append(DATE.format(at)).append(',').append(TIME.format(at)).append(',')
                .append(DATE.format(at)).append(',').append(TIME.format(at)).append(',')
                .append(callsign == null ? "" : callsign.strip()).append(',')
                .append(alt == null ? "" : alt).append(',')
                .append(fmt(gs)).append(',')
                .append(fmt(track)).append(',')
                .append(lat == null ? "" : fmt(lat)).append(',')
                .append(lon == null ? "" : fmt(lon)).append(',')
                .append(vrate == null ? "" : vrate).append(',')
                .append(squawk == null ? "" : squawk).append(',')
                .append("0,0,0,")
                .append(onGround == null ? "" : (onGround ? "-1" : "0"));
        return out.toString();
    }

    private static String fmt(Double value) {
        if (value == null) {
            return "";
        }
        return value == Math.rint(value) ? Long.toString(value.longValue()) : Double.toString(value);
    }
}
