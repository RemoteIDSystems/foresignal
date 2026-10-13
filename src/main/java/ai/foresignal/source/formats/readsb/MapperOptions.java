package ai.foresignal.source.formats.readsb;

/**
 * How {@link ReadsbAircraftMapper} maps entries for one source (prompt 062).
 *
 * @param kind the record kind; {@value AircraftFields#KIND} for every built-in source
 * @param lastPositionFallback true to use {@code lastPosition} when an entry has no current
 *     {@code lat}/{@code lon} (adsb.lol); a local receiver may turn it off
 */
public record MapperOptions(String kind, boolean lastPositionFallback) {

    /** The options of the global aircraft sources: kind {@code aircraft}, {@code lastPosition} used. */
    public static final MapperOptions DEFAULTS = new MapperOptions(AircraftFields.KIND, true);

    /** Defaults the kind. */
    public MapperOptions {
        kind = kind == null || kind.isBlank() ? AircraftFields.KIND : kind;
    }
}
