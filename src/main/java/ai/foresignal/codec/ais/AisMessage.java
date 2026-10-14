package ai.foresignal.codec.ais;

/**
 * The AIS messages Foresignal decodes (prompt 085, ITU-R M.1371-5), with every "not available" sentinel already turned
 * into null (adapter rule 11): latitude 91 and longitude 181 (or anything out of range), SOG 1023 (102.3 kn), COG
 * 3600, heading 511 (or above 359), rate of turn −128, navigational status 15, draft 0, IMO 0, ship type 0, an ETA
 * with any part unavailable, all-zero dimensions and empty or all-{@code @} text.
 *
 * <p>Scaled values are exact multiples of the wire resolution, so {@link AisEncoder} writes them back unchanged:
 * positions in degrees from 1/10,000 minute (1/10 minute for type 27), speeds in knots from 1/10 kn, courses in
 * degrees from 1/10°, draft in meters from 1/10 m. The rate of turn is the raw ROT<sub>AIS</sub> value −127 to
 * 127 ({@code 4.733 × sqrt(degrees per minute)}, signed), as Digitraffic reports it too.
 */
public sealed interface AisMessage {

    /** The message type, 1 to 27. */
    int type();

    /** The repeat indicator, 0 to 3. */
    int repeat();

    /** The MMSI, as transmitted. */
    int mmsi();

    /** The MMSI as a nine-digit string, zero-padded. */
    default String mmsiText() {
        return String.format("%09d", mmsi());
    }

    /**
     * The distances in meters from the reference point to the bow (A), stern (B), port (C) and starboard (D).
     *
     * @param a to bow, 0 to 511
     * @param b to stern, 0 to 511
     * @param c to port, 0 to 63
     * @param d to starboard, 0 to 63
     */
    record Dimension(int a, int b, int c, int d) {

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

        /** The dimension, or null when every side is zero ("not available"). */
        public static Dimension ofOrNull(int a, int b, int c, int d) {
            return a == 0 && b == 0 && c == 0 && d == 0 ? null : new Dimension(a, b, c, d);
        }
    }

    /**
     * An ETA without a year.
     *
     * @param month 1 to 12
     * @param day 1 to 31
     * @param hour 0 to 23
     * @param minute 0 to 59
     */
    record Eta(int month, int day, int hour, int minute) {

        /** The ETA, or null when any part is "not available" (month 0, day 0, hour 24, minute 60) or out of range. */
        public static Eta ofOrNull(int month, int day, int hour, int minute) {
            if (month < 1 || month > 12 || day < 1 || day > 31 || hour > 23 || minute > 59 || hour < 0 || minute < 0) {
                return null;
            }
            return new Eta(month, day, hour, minute);
        }
    }

    /**
     * Types 1, 2 and 3: Class A position report.
     *
     * @param type 1, 2 or 3
     * @param repeat the repeat indicator
     * @param mmsi the MMSI
     * @param navStatus the navigational status 0 to 14, or null
     * @param rot the raw rate of turn −127 to 127, or null
     * @param sog speed over ground in knots, or null
     * @param accuracy the position accuracy flag (true: better than 10 m)
     * @param longitude degrees, or null
     * @param latitude degrees, or null
     * @param cog course over ground in degrees, or null
     * @param heading true heading in degrees, or null
     * @param second the UTC second of the report, 0 to 63 (60 and above are "not available" codes)
     * @param maneuver the maneuver indicator, 0 to 2
     * @param raim the RAIM flag
     * @param radio the radio status, 19 bits
     */
    record ClassAPosition(int type, int repeat, int mmsi, Integer navStatus, Integer rot, Double sog, boolean accuracy,
            Double longitude, Double latitude, Double cog, Integer heading, int second, int maneuver, boolean raim,
            int radio) implements AisMessage {}

    /**
     * Type 4: base station report (counted only).
     *
     * @param repeat the repeat indicator
     * @param mmsi the station's MMSI
     */
    record BaseStationReport(int repeat, int mmsi) implements AisMessage {

        @Override
        public int type() {
            return 4;
        }
    }

    /**
     * Type 5: static and voyage related data (Class A).
     *
     * @param repeat the repeat indicator
     * @param mmsi the MMSI
     * @param aisVersion the AIS version, 0 to 3
     * @param imo the IMO number, or null
     * @param callsign the call sign, or null
     * @param name the vessel name, or null
     * @param shipType the ship and cargo type 1 to 255, or null
     * @param dimension the dimensions, or null
     * @param epfd the position fix device type, 0 to 15
     * @param eta the ETA, or null
     * @param draft the maximum present static draft in meters, or null
     * @param destination the destination, or null
     * @param dte the data terminal flag (true: not ready)
     */
    record StaticVoyage(int repeat, int mmsi, int aisVersion, Integer imo, String callsign, String name,
            Integer shipType, Dimension dimension, int epfd, Eta eta, Double draft, String destination, boolean dte)
            implements AisMessage {

        @Override
        public int type() {
            return 5;
        }
    }

    /**
     * Type 18: standard Class B position report.
     *
     * @param repeat the repeat indicator
     * @param mmsi the MMSI
     * @param sog speed over ground in knots, or null
     * @param accuracy the position accuracy flag
     * @param longitude degrees, or null
     * @param latitude degrees, or null
     * @param cog course over ground in degrees, or null
     * @param heading true heading in degrees, or null
     * @param second the UTC second
     * @param regional the regional reserved bits, 0 to 3
     * @param cs the carrier-sense unit flag
     * @param display the display flag
     * @param dsc the DSC flag
     * @param band the band flag
     * @param msg22 the message 22 flag
     * @param assigned the assigned-mode flag
     * @param raim the RAIM flag
     * @param radio the radio status, 20 bits
     */
    record ClassBPosition(int repeat, int mmsi, Double sog, boolean accuracy, Double longitude, Double latitude,
            Double cog, Integer heading, int second, int regional, boolean cs, boolean display, boolean dsc,
            boolean band, boolean msg22, boolean assigned, boolean raim, int radio) implements AisMessage {

        @Override
        public int type() {
            return 18;
        }
    }

    /**
     * Type 19: extended Class B position report, with the name, type and dimensions.
     *
     * @param repeat the repeat indicator
     * @param mmsi the MMSI
     * @param sog speed over ground in knots, or null
     * @param accuracy the position accuracy flag
     * @param longitude degrees, or null
     * @param latitude degrees, or null
     * @param cog course over ground in degrees, or null
     * @param heading true heading in degrees, or null
     * @param second the UTC second
     * @param name the vessel name, or null
     * @param shipType the ship and cargo type, or null
     * @param dimension the dimensions, or null
     * @param epfd the position fix device type
     * @param raim the RAIM flag
     * @param dte the data terminal flag
     * @param assigned the assigned-mode flag
     */
    record ClassBExtended(int repeat, int mmsi, Double sog, boolean accuracy, Double longitude, Double latitude,
            Double cog, Integer heading, int second, String name, Integer shipType, Dimension dimension, int epfd,
            boolean raim, boolean dte, boolean assigned) implements AisMessage {

        @Override
        public int type() {
            return 19;
        }
    }

    /**
     * Type 21: aid-to-navigation report (counted only).
     *
     * @param repeat the repeat indicator
     * @param mmsi the aid's MMSI
     */
    record AidToNavigation(int repeat, int mmsi) implements AisMessage {

        @Override
        public int type() {
            return 21;
        }
    }

    /**
     * Type 24 part A: the Class B vessel name.
     *
     * @param repeat the repeat indicator
     * @param mmsi the MMSI
     * @param name the vessel name, or null
     */
    record StaticDataA(int repeat, int mmsi, String name) implements AisMessage {

        @Override
        public int type() {
            return 24;
        }
    }

    /**
     * Type 24 part B: the Class B type, call sign and dimensions.
     *
     * @param repeat the repeat indicator
     * @param mmsi the MMSI
     * @param shipType the ship and cargo type, or null
     * @param vendorId the three-character manufacturer id, or null
     * @param model the unit model code, 0 to 15
     * @param serial the unit serial number, 20 bits
     * @param callsign the call sign, or null
     * @param dimension the dimensions, or null (always null for an auxiliary craft)
     * @param mothershipMmsi the mother ship's MMSI for an auxiliary craft (MMSI {@code 98xxxxxxx}), else null
     */
    record StaticDataB(int repeat, int mmsi, Integer shipType, String vendorId, int model, int serial, String callsign,
            Dimension dimension, Integer mothershipMmsi) implements AisMessage {

        @Override
        public int type() {
            return 24;
        }

        /** True for an auxiliary craft of a mother ship (MMSI {@code 98xxxxxxx}). */
        public static boolean auxiliary(int mmsi) {
            return mmsi / 10_000_000 == 98;
        }
    }

    /**
     * Type 27: long-range position report (position at 1/10 minute).
     *
     * @param repeat the repeat indicator
     * @param mmsi the MMSI
     * @param accuracy the position accuracy flag
     * @param raim the RAIM flag
     * @param navStatus the navigational status 0 to 14, or null
     * @param longitude degrees, or null
     * @param latitude degrees, or null
     * @param sog speed over ground in whole knots, or null (63 = not available)
     * @param cog course over ground in whole degrees, or null (511 = not available)
     * @param gnss the GNSS position status flag (false: current position)
     */
    record LongRangePosition(int repeat, int mmsi, boolean accuracy, boolean raim, Integer navStatus, Double longitude,
            Double latitude, Double sog, Double cog, boolean gnss) implements AisMessage {

        @Override
        public int type() {
            return 27;
        }
    }

    /**
     * Any other type, or a type 24 with a reserved part number: counted and dropped.
     *
     * @param type the message type
     * @param repeat the repeat indicator
     * @param mmsi the MMSI, or 0 when the payload is too short to carry one
     */
    record Unsupported(int type, int repeat, int mmsi) implements AisMessage {}
}
