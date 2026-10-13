package ai.foresignal.source.formats.readsb;

/**
 * How {@link ReadsbAircraftMapper} maps entries for one source (prompts 062 and 069).
 *
 * @param kind the record kind; {@value AircraftFields#KIND} for every built-in source
 * @param lastPositionFallback true to use {@code lastPosition} when an entry has no current
 *     {@code lat}/{@code lon} (adsb.lol); a local receiver may turn it off
 * @param receiverId when non-null, every record carries one {@code Observer} with this receiver id and the
 *     entry's {@code rssi} in dBFS (prompt 069: a local receiver is itself the observer); null for the
 *     aggregators, which do not say which receiver heard an aircraft
 * @param band when non-null, the payload's {@code band} (prompt 069: {@code "978"} for a UAT receiver)
 * @param integrityExtras true to keep {@code nic_baro, sil, gva, sda} (and {@code uat_version}) in
 *     {@code quality.extra["adsb.*"]} (prompt 069, local receivers)
 */
public record MapperOptions(String kind, boolean lastPositionFallback, String receiverId, String band,
        boolean integrityExtras) {

    /** The options of the global aircraft sources: kind {@code aircraft}, {@code lastPosition} used. */
    public static final MapperOptions DEFAULTS = new MapperOptions(AircraftFields.KIND, true);

    /** Defaults the kind. */
    public MapperOptions {
        kind = kind == null || kind.isBlank() ? AircraftFields.KIND : kind;
        receiverId = receiverId == null || receiverId.isBlank() ? null : receiverId;
        band = band == null || band.isBlank() ? null : band;
    }

    /** Options without an observer, a band or integrity extras (the shape of prompt 062). */
    public MapperOptions(String kind, boolean lastPositionFallback) {
        this(kind, lastPositionFallback, null, null, false);
    }

    /**
     * The options of a local receiver (prompt 069): the observer is the source itself, {@code band} is set
     * for 978 MHz only, the integrity values are kept, and {@code lastPosition} is used as for adsb.lol.
     *
     * @param sourceId the receiver id of the observer
     * @param band {@code "1090"} or {@code "978"}
     */
    public static MapperOptions localReceiver(String sourceId, String band) {
        return new MapperOptions(AircraftFields.KIND, true, sourceId, "978".equals(band) ? "978" : null, true);
    }
}
