package ai.foresignal.codec.ais;

import java.time.Instant;

/**
 * One source-neutral piece of a vessel's state (prompt 085), what {@link VesselStateMerger} merges: AIVDM messages are
 * turned into these by {@link VesselStateMerger#merge(AisMessage, Instant, ai.foresignal.source.spi.Observer)}, and
 * {@code ais-digitraffic} builds them from its JSON. Every value is already free of AIS sentinels (null means absent).
 */
public sealed interface VesselUpdate {

    /** The nine-digit MMSI. */
    String mmsi();

    /** When the upstream says the data was valid (or, for a RECEIVED source, when it arrived). */
    Instant time();

    /**
     * A position report.
     *
     * @param mmsi the MMSI
     * @param time the report time
     * @param aisClass {@code A}, {@code B}, or null when the report does not say (type 27, Digitraffic)
     * @param latitude degrees, or null
     * @param longitude degrees, or null
     * @param sog knots, or null
     * @param cog degrees, or null
     * @param heading degrees, or null
     * @param navStatus 0 to 14, or null
     * @param rot the raw rate of turn −127 to 127, or null ({@code quality.extra["ais.rot"]})
     * @param positionAccuracy the position accuracy flag, or null ({@code quality.extra["ais.pos_acc"]})
     * @param longRange true for a type 27 long-range report (accuracy 185 m)
     */
    record Position(String mmsi, Instant time, String aisClass, Double latitude, Double longitude, Double sog,
            Double cog, Integer heading, Integer navStatus, Integer rot, Boolean positionAccuracy, boolean longRange)
            implements VesselUpdate {

        /** True when the report carried a usable position. */
        public boolean hasPosition() {
            return latitude != null && longitude != null;
        }
    }

    /**
     * Static data.
     *
     * @param mmsi the MMSI
     * @param time the message time
     * @param aisClass {@code A}, {@code B}, or null
     * @param voyage true when the message carries the whole voyage block (type 5, Digitraffic metadata), so an absent
     *     destination, ETA, draft or IMO there means cleared; false when it carries only some identity fields (types
     *     19 and 24), so an absent value means "not carried"
     * @param name the name, or null
     * @param callsign the call sign, or null
     * @param imo the IMO number, or null
     * @param shipType the ship and cargo type, or null
     * @param dimension the dimensions, or null
     * @param eta the ETA parts, or null
     * @param draft meters, or null
     * @param destination the destination, or null
     */
    record Static(String mmsi, Instant time, String aisClass, boolean voyage, String name, String callsign, Integer imo,
            Integer shipType, AisMessage.Dimension dimension, AisMessage.Eta eta, Double draft, String destination)
            implements VesselUpdate {}
}
