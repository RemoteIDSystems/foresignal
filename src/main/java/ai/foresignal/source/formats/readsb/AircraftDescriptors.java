package ai.foresignal.source.formats.readsb;

import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldThreshold;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.MotionSpec;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import java.time.Duration;
import java.util.List;
import java.util.Set;

/**
 * The descriptor defaults every global aircraft source shares (prompt 062; Core-Data-Model §5.3 row
 * "Global aircraft", as overridden by {@code docs/Conventions.md}).
 *
 * <ul>
 *   <li>TRACK, {@code ttlAnchor} OBSERVED, {@code observedAtMode} UPSTREAM;
 *   <li>{@code recordTtl} 6 minutes ({@link #TTL}): §5.3's 5 minutes fails
 *       {@code keyframe ≤ ttl − pollInterval − 30 s} at a 60 s poll; 6 minutes allows polls up to 90 s with
 *       the 240 s keyframe. {@code opensky-states} and the 30-minute blind-spot sources pass their own;
 *   <li>SAMPLED {@code {minInterval 60s, keyframe 240s, minDeviationMeters 1000, thresholds true,
 *       edgeMinInterval 30s}}: a moved aircraft keeps a trail point every 60 s, as Shadowbroker adds one each
 *       minute only when the position changed; an unmoved one keeps a keyframe every 240 s;
 *   <li>significant {@code flight, r, t, squawk, category, emergency, onGround, navDegraded}; thresholds
 *       {@code alt_baro} 300 m (§5.3 states 1,000 ft) and {@code track} 20° (angular); motion
 *       {@code {gs, KNOTS, track}};
 *   <li>tiles {@code flight, r, t, alt_baro, gs, track, squawk, mil}; search {@code flight, r, t, hex};
 *       title {@code flight} (the title resolver then falls back to {@code r} and {@code hex});
 *   <li>entity keys {@code icao24 → hex}, {@code registration → r}, {@code callsign → flight} (Adapter
 *       Guide §13), tiles aggregated below zoom 7, and a {@code fieldSpecs} entry for every payload key of
 *       {@link AircraftFields}.
 * </ul>
 *
 * <p>Each adapter adds what is its own: id, name, description, intervals, budget, params, credentials,
 * license, terms, attribution, egress and its kind display.
 */
public final class AircraftDescriptors {

    /** The record TTL of the polled global aircraft sources (Conventions, "Defaults that override"). */
    public static final Duration TTL = Duration.ofMinutes(6);

    /** The §5.3 SAMPLED sampling interval. */
    public static final Duration MIN_INTERVAL = Duration.ofSeconds(60);

    /** The §5.3 keyframe: an unmoved aircraft keeps a point every 240 s. */
    public static final Duration KEYFRAME = Duration.ofSeconds(240);

    /** The §5.3 deviation from the dead-reckoned position that earns a sample. */
    public static final double MIN_DEVIATION_METERS = 1000.0;

    /** The §5.3 trigger-edge interval. */
    public static final Duration EDGE_MIN_INTERVAL = Duration.ofSeconds(30);

    /** The {@code alt_baro} threshold in meters (§5.3's 1,000 ft, about 300 m). */
    public static final double ALT_BARO_THRESHOLD_METERS = 300.0;

    /** The {@code track} threshold in degrees. */
    public static final double TRACK_THRESHOLD_DEGREES = 20.0;

    /** The zoom below which aircraft tiles aggregate (Adapter Guide §11: 6 to 8 suits global aircraft). */
    public static final int AGG_BELOW_ZOOM = 7;

    /** The significant fields of every aircraft source (§5.3). */
    public static final List<String> SIGNIFICANT_FIELDS = List.of(
            AircraftFields.FLIGHT, AircraftFields.REGISTRATION, AircraftFields.TYPE, AircraftFields.SQUAWK,
            AircraftFields.CATEGORY, AircraftFields.EMERGENCY, AircraftFields.ON_GROUND, AircraftFields.NAV_DEGRADED);

    /** The tile properties of every aircraft source. */
    public static final List<String> TILE_PROPERTIES = List.of(
            AircraftFields.FLIGHT, AircraftFields.REGISTRATION, AircraftFields.TYPE, AircraftFields.ALT_BARO,
            AircraftFields.GROUND_SPEED, AircraftFields.TRACK, AircraftFields.SQUAWK, AircraftFields.MILITARY);

    /** The search fields of every aircraft source. */
    public static final List<String> SEARCH_FIELDS = List.of(
            AircraftFields.FLIGHT, AircraftFields.REGISTRATION, AircraftFields.TYPE, AircraftFields.HEX);

    /** The datum label of {@code alt_baro}. */
    public static final String PRESSURE_DATUM = "pressure altitude (1013.25 hPa)";

    /** The datum label of {@code alt_geom}. */
    public static final String ELLIPSOID_DATUM = "WGS84 ellipsoid";

    /** The record TTL of the local receivers (prompt 069; Core-Data-Model §5.3 row {@code local-adsb-*}). */
    public static final Duration LOCAL_TTL = Duration.ofMinutes(5);

    /** The local receivers' SAMPLED sampling interval. */
    public static final Duration LOCAL_MIN_INTERVAL = Duration.ofSeconds(10);

    /** The local receivers' keyframe. */
    public static final Duration LOCAL_KEYFRAME = Duration.ofSeconds(120);

    /** The local receivers' dead-reckoning deviation. */
    public static final double LOCAL_MIN_DEVIATION_METERS = 300.0;

    /** The local receivers' trigger-edge interval. */
    public static final Duration LOCAL_EDGE_MIN_INTERVAL = Duration.ofSeconds(5);

    /** The local receivers' {@code alt_baro} threshold in meters (500 ft). */
    public static final double LOCAL_ALT_BARO_THRESHOLD_METERS = 150.0;

    /** The local receivers' {@code track} threshold in degrees. */
    public static final double LOCAL_TRACK_THRESHOLD_DEGREES = 10.0;

    private AircraftDescriptors() {}

    /**
     * The SAMPLED policy of the local receivers (prompt 069, Core-Data-Model §5.3):
     * {@code {10s, 120s, 300 m, thresholds, edgeMin 5s}}.
     */
    public static IngestPolicy localPolicy() {
        return new IngestPolicy.Sampled(LOCAL_MIN_INTERVAL, LOCAL_KEYFRAME, LOCAL_MIN_DEVIATION_METERS, true,
                LOCAL_EDGE_MIN_INTERVAL);
    }

    /**
     * Applies the local receiver defaults (prompt 069): the shared keys, motion, tiles, search and field specs
     * of {@link #global}, with TTL {@link #LOCAL_TTL}, {@link #localPolicy()}, thresholds {@code alt_baro}
     * 150 m and {@code track} 10°, the {@code band} field spec, and {@code observedAtMode} as given
     * (UPSTREAM for {@code aircraft.json}, RECEIVED for SBS, whose times have no zone).
     *
     * @param builder the adapter's builder
     * @param observedAtMode where {@code observedAt} comes from
     * @return {@code builder}
     */
    public static SourceDescriptor.Builder local(SourceDescriptor.Builder builder, ObservedAtMode observedAtMode) {
        apply(builder, LOCAL_TTL, localPolicy(), LOCAL_ALT_BARO_THRESHOLD_METERS, LOCAL_TRACK_THRESHOLD_DEGREES,
                observedAtMode);
        builder.fieldSpec(AircraftFields.BAND, new FieldSpec(FieldType.STRING, null, "Band",
                "Receiver band: 978 for a UAT receiver; omitted for 1090 MHz", null, Set.of()));
        return builder;
    }

    /** The SAMPLED default policy of §5.3. */
    public static IngestPolicy defaultPolicy() {
        return new IngestPolicy.Sampled(MIN_INTERVAL, KEYFRAME, MIN_DEVIATION_METERS, true, EDGE_MIN_INTERVAL);
    }

    /** Applies the shared defaults with the 6-minute {@link #TTL}. */
    public static SourceDescriptor.Builder global(SourceDescriptor.Builder builder) {
        return global(builder, TTL);
    }

    /**
     * Applies the shared defaults.
     *
     * @param builder the adapter's builder
     * @param recordTtl the record TTL ({@link #TTL}, or 15 minutes for {@code opensky-states})
     * @return {@code builder}
     */
    public static SourceDescriptor.Builder global(SourceDescriptor.Builder builder, Duration recordTtl) {
        return apply(builder, recordTtl, defaultPolicy(), ALT_BARO_THRESHOLD_METERS, TRACK_THRESHOLD_DEGREES,
                ObservedAtMode.UPSTREAM);
    }

    private static SourceDescriptor.Builder apply(SourceDescriptor.Builder builder, Duration recordTtl,
            IngestPolicy policy, double altThreshold, double trackThreshold, ObservedAtMode observedAtMode) {
        builder.kind(AircraftFields.KIND)
                .historyTier(HistoryTier.TRACK)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .observedAtMode(observedAtMode)
                .recordTtl(recordTtl)
                .defaultIngestPolicy(policy)
                .significantFields(SIGNIFICANT_FIELDS.toArray(String[]::new))
                .threshold(FieldThreshold.of(AircraftFields.ALT_BARO, altThreshold))
                .threshold(FieldThreshold.angular(AircraftFields.TRACK, trackThreshold))
                .motion(new MotionSpec(AircraftFields.GROUND_SPEED, MotionSpec.SpeedUnit.KNOTS, AircraftFields.TRACK))
                .tileProperties(TILE_PROPERTIES.toArray(String[]::new))
                .searchFields(SEARCH_FIELDS.toArray(String[]::new))
                .titleField(AircraftFields.FLIGHT)
                .aggBelowZoom(AGG_BELOW_ZOOM)
                .entityKey("icao24", AircraftFields.HEX)
                .entityKey("registration", AircraftFields.REGISTRATION)
                .entityKey("callsign", AircraftFields.FLIGHT)
                .entityKeyPrefix("callsign", "air")
                .entityAttribute(AircraftFields.TYPE, "type")
                .entityAttribute(AircraftFields.DESCRIPTION, "typeDescription");
        fieldSpecs(builder);
        return builder;
    }

    /** The kind display of a civil aircraft layer: a plane icon rotated by {@code track}. */
    public static KindDisplay civilDisplay() {
        return new KindDisplay("{flight}", "plane", "#1C7ED6", null, AircraftFields.TRACK, null, null);
    }

    /** The kind display of a military aircraft layer: a jet icon rotated by {@code track}. */
    public static KindDisplay militaryDisplay() {
        return new KindDisplay("{flight}", "fighter-jet", "#5C940D", null, AircraftFields.TRACK, null, null);
    }

    /** Declares every payload key of {@link AircraftFields} (the 011 {@code fieldSpecs} contract). */
    static void fieldSpecs(SourceDescriptor.Builder b) {
        text(b, AircraftFields.HEX, "ICAO address",
                "ICAO 24-bit address in lower-case hex; a leading ~ marks a non-ICAO (anonymous or TIS-B) address");
        text(b, AircraftFields.FLIGHT, "Callsign", "Flight number or callsign, trimmed");
        text(b, AircraftFields.REGISTRATION, "Registration", "Aircraft registration (tail number)");
        text(b, AircraftFields.TYPE, "Type", "ICAO aircraft type designator");
        text(b, AircraftFields.DESCRIPTION, "Type description", "Long aircraft type description");
        text(b, AircraftFields.SQUAWK, "Squawk", "Mode A transponder code, four octal digits");
        text(b, AircraftFields.EMERGENCY, "Emergency",
                "ADS-B emergency or priority status (general, lifeguard, minfuel, nordo, unlawful, downed, reserved);"
                        + " omitted when none");
        text(b, AircraftFields.CATEGORY, "Emitter category", "ADS-B emitter category, A0 to D7");
        flag(b, AircraftFields.ON_GROUND, "On ground", "True when the aircraft reports being on the ground");
        b.fieldSpec(AircraftFields.ALT_BARO, new FieldSpec(FieldType.INTEGER, "m", "Barometric altitude",
                "Pressure altitude in meters, rounded to 1 m; omitted on the ground", PRESSURE_DATUM, Set.of()));
        b.fieldSpec(AircraftFields.ALT_GEOM, new FieldSpec(FieldType.INTEGER, "m", "Geometric altitude",
                "GNSS altitude in meters above the WGS84 ellipsoid, rounded to 1 m", ELLIPSOID_DATUM, Set.of()));
        b.fieldSpec(AircraftFields.GROUND_SPEED, new FieldSpec(FieldType.NUMBER, "[kn_i]", "Ground speed",
                "Ground speed in knots, rounded to 0.1", null, Set.of()));
        b.fieldSpec(AircraftFields.TRACK, new FieldSpec(FieldType.NUMBER, "deg", "Track",
                "True track over ground in degrees, 0 to 360, rounded to 0.1", null, Set.of()));
        b.fieldSpec(AircraftFields.BARO_RATE, new FieldSpec(FieldType.NUMBER, "m/s", "Vertical rate",
                "Barometric vertical rate in meters per second, positive climbing, rounded to 0.1", null, Set.of()));
        text(b, AircraftFields.POS_SOURCE, "Position source",
                "How the position was obtained: adsb, adsr, adsc, mlat, tisb, modes or other (readsb and local receivers;"
                        + " other for SBS); adsb, asterix, mlat or flarm (OpenSky). MLAT positions are never"
                        + " redistributable");
        b.fieldSpec(AircraftFields.NAC_P, new FieldSpec(FieldType.INTEGER, "1", "NACp",
                "Navigation accuracy category for position, 0 to 11", null, Set.of()));
        b.fieldSpec(AircraftFields.NIC, new FieldSpec(FieldType.INTEGER, "1", "NIC",
                "Navigation integrity category, 0 to 11", null, Set.of()));
        flag(b, AircraftFields.NAV_DEGRADED, "Navigation degraded",
                "True when NACp is below 8 or NIC below 7 (possible GNSS interference); present only when either is reported");
        flag(b, AircraftFields.MILITARY, "Military", "Flagged military in the upstream database (dbFlags bit 1); omitted when false");
        flag(b, AircraftFields.PIA, "Privacy address", "Privacy ICAO address (dbFlags bit 4); omitted when false");
        flag(b, AircraftFields.LADD, "LADD", "On the FAA LADD list (dbFlags bit 8); omitted when false");
        text(b, AircraftFields.ORIGIN_COUNTRY, "Origin country",
                "Country of registration inferred from the ICAO address (OpenSky only)");
    }

    private static void text(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.STRING, null, label, description, null, Set.of()));
    }

    private static void flag(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.BOOLEAN, null, label, description, null, Set.of()));
    }
}
