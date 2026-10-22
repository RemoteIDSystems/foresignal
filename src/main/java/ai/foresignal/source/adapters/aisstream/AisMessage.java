package ai.foresignal.source.adapters.aisstream;

import java.time.Instant;

/**
 * One recognized AIS message that the vessel merge uses, already scrubbed of AIS sentinels (adapter rule
 * 11): every field that was "not available" is null.
 *
 * <p>Position fields are set only by the position reports ({@link #position()} true); static fields by
 * {@code ShipStaticData}, {@code StaticDataReport} (part A: the name; part B: type, call sign and
 * dimensions) and the static part of {@code ExtendedClassBPositionReport}.
 *
 * @param messageType the aisstream {@code MessageType}
 * @param mmsi the nine-digit MMSI
 * @param time the upstream message time ({@code MetaData.time_utc}), never the receive time
 * @param aisClass {@code A} or {@code B}
 * @param position true for a position report, even one whose position was a sentinel
 * @param latitude degrees, or null when absent, 91 or out of range
 * @param longitude degrees, or null when absent, 181 or out of range
 * @param sog speed over ground in knots, or null (102.3 = not available)
 * @param cog course over ground in degrees, or null (360 = not available)
 * @param heading true heading in degrees, or null (511 = not available)
 * @param navStatus navigational status 0 to 14, or null (15 = not defined; Class A only)
 * @param name the vessel name, trimmed of spaces and {@code @} padding, or null
 * @param callsign the call sign, trimmed, or null
 * @param imo the IMO number, or null (0 = not available)
 * @param shipType the AIS ship and cargo type, or null (0 = not available)
 * @param dimension the reference-point distances, or null when the message has none
 * @param eta the ETA parts, or null when any part is "not available"
 * @param draft the maximum static draft in meters, or null (0 = not available)
 * @param destination the destination, trimmed, or null
 */
public record AisMessage(
        String messageType,
        String mmsi,
        Instant time,
        String aisClass,
        boolean position,
        Double latitude,
        Double longitude,
        Double sog,
        Double cog,
        Integer heading,
        Integer navStatus,
        String name,
        String callsign,
        Integer imo,
        Integer shipType,
        Dimension dimension,
        Eta eta,
        Double draft,
        String destination) implements AisStreamMessageParser.Result {

    /** True when the message carried a usable position. */
    public boolean hasPosition() {
        return latitude != null && longitude != null;
    }

    /** True when the message carried any static field. */
    public boolean hasStatic() {
        return name != null || callsign != null || imo != null || shipType != null || dimension != null
                || eta != null || draft != null || destination != null;
    }

    @Override
    public boolean liveness() {
        return true;
    }

    /**
     * The distances in meters from the AIS reference point to the bow (A), stern (B), port (C) and
     * starboard (D). Zero means "not available" for that side.
     */
    public record Dimension(int a, int b, int c, int d) {

        /** {@code A + B}, or null when that is zero. */
        public Integer lengthM() {
            int length = a + b;
            return length > 0 ? length : null;
        }

        /** {@code C + D}, or null when that is zero. */
        public Integer beamM() {
            int beam = c + d;
            return beam > 0 ? beam : null;
        }

        /** True when every side is zero, i.e. no dimension at all. */
        public boolean isZero() {
            return a == 0 && b == 0 && c == 0 && d == 0;
        }
    }

    /**
     * An AIS ETA: month 1–12, day 1–31, hour 0–23, minute 0–59, with no year.
     */
    public record Eta(int month, int day, int hour, int minute) {}
}
