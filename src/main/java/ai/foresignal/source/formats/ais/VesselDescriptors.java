package ai.foresignal.source.formats.ais;

import ai.foresignal.codec.ais.VesselStateMerger;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldThreshold;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.MotionSpec;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import java.time.Duration;
import java.util.List;
import java.util.Set;

/**
 * The descriptor defaults of a vessel source (Core-Data-Model §5.3, the Vessels row; prompts 063 and 085), so every
 * vessel source stores, samples, tiles and describes the {@code vessel} kind the same way.
 *
 * <ul>
 *   <li>TRACK, SAMPLED {30 s, keyframe 1,200 s, 250 m dead reckoning, edge 15 s} ({@code 1200 ≤ 1800 − 2 − 30}),
 *       TTL 30 min, {@code ttlAnchor} OBSERVED;
 *   <li>thresholds {@code cog} 15° (angular) and {@code sog} 3 kn; motion {@code {sog, kn, cog}};
 *   <li>significant {@code name, callsign, imo, shipType, destination, navStatus, draught};
 *   <li>tile properties {@code name, shipType, sog, cog, heading, navStatus}; search {@code name, callsign, mmsi,
 *       imo}; title {@code name} (the title resolver falls back to {@code callsign}, then {@code mmsi});
 *   <li>entity keys {@code mmsi}, {@code imo}, {@code callsign}; the {@code ship} icon rotated by {@code cog};
 *   <li>a {@code fieldSpecs} entry for every payload key, exactly prompt 063's.
 * </ul>
 */
public final class VesselDescriptors {

    /** The record kind. */
    public static final String KIND = VesselStateMerger.KIND;

    /** The record TTL. */
    public static final Duration TTL = Duration.ofMinutes(30);

    /** The significant fields (§5.3). */
    public static final List<String> SIGNIFICANT_FIELDS =
            List.of("name", "callsign", "imo", "shipType", "destination", "navStatus", "draught");

    /** The tile properties. */
    public static final List<String> TILE_PROPERTIES = List.of("name", "shipType", "sog", "cog", "heading", "navStatus");

    /** The search fields of the keyless sources (prompt 085). */
    public static final List<String> SEARCH_FIELDS = List.of("name", "callsign", "mmsi", "imo");

    /** The kind display: the name, the {@code ship} icon, rotated by {@code cog}. */
    public static final KindDisplay DISPLAY = new KindDisplay("{name}", "ship", "#1098AD", null, "cog", null, null);

    private VesselDescriptors() {}

    /** The SAMPLED vessel policy of Core-Data-Model §5.3. */
    public static IngestPolicy defaultPolicy() {
        return new IngestPolicy.Sampled(Duration.ofSeconds(30), Duration.ofSeconds(1200), 250.0, true,
                Duration.ofSeconds(15));
    }

    /**
     * Applies every vessel default to {@code b}: tier, time anchor, TTL, policy, significant fields, motion,
     * thresholds, tiles, search, title, entity keys, display and field specs.
     *
     * @return {@code b}
     */
    public static SourceDescriptor.Builder apply(SourceDescriptor.Builder b) {
        b.historyTier(HistoryTier.TRACK)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .recordTtl(TTL)
                .defaultIngestPolicy(defaultPolicy())
                .significantFields(SIGNIFICANT_FIELDS.toArray(String[]::new))
                .motion(new MotionSpec("sog", MotionSpec.SpeedUnit.KNOTS, "cog"))
                .threshold(FieldThreshold.angular("cog", 15.0))
                .threshold(FieldThreshold.of("sog", 3.0))
                .tileProperties(TILE_PROPERTIES.toArray(String[]::new))
                .searchFields(SEARCH_FIELDS.toArray(String[]::new))
                .titleField("name")
                .entityKey("mmsi", "mmsi")
                .entityKey("imo", "imo")
                .entityKey("callsign", "callsign")
                .kindDisplay(KIND, DISPLAY);
        fieldSpecs(b);
        return b;
    }

    /** Declares prompt 063's {@code fieldSpecs} for every vessel payload key. */
    public static void fieldSpecs(SourceDescriptor.Builder b) {
        text(b, "mmsi", "MMSI", "Maritime Mobile Service Identity, nine digits");
        text(b, "name", "Name", "Vessel name, trimmed of spaces and @ padding");
        text(b, "callsign", "Call sign", "Radio call sign");
        integer(b, "imo", "1", "IMO number", "IMO ship identification number, seven digits");
        integer(b, "shipType", "1", "Ship type", "AIS ship and cargo type code, 1 to 99");
        text(b, "class", "AIS class", "AIS transponder class: A or B");
        text(b, "destination", "Destination", "Destination as entered by the crew");
        integer(b, "navStatus", "1", "Navigation status",
                "AIS navigational status 0 to 14 (0 under way using engine, 1 at anchor, 5 moored, …); Class A only");
        b.fieldSpec("draught", new FieldSpec(FieldType.NUMBER, "m", "Draft",
                "Maximum present static draft in meters", null, Set.of()));
        integer(b, "lengthM", "m", "Length", "Length overall in meters (dimension A + B)");
        integer(b, "beamM", "m", "Beam", "Beam in meters (dimension C + D)");
        b.fieldSpec("eta", new FieldSpec(FieldType.TIMESTAMP, null, "ETA",
                "Estimated time of arrival, UTC, at the next occurrence of the reported month, day, hour and minute",
                null, Set.of()));
        b.fieldSpec("sog", new FieldSpec(FieldType.NUMBER, "[kn_i]", "Speed over ground",
                "Speed over ground in knots", null, Set.of()));
        b.fieldSpec("cog", new FieldSpec(FieldType.NUMBER, "deg", "Course over ground",
                "Course over ground in degrees, 0 to 359.9", null, Set.of()));
        integer(b, "heading", "deg", "True heading", "True heading in degrees, 0 to 359");
    }

    private static void text(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.STRING, null, label, description, null, Set.of()));
    }

    private static void integer(SourceDescriptor.Builder b, String key, String unit, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.INTEGER, unit, label, description, null, Set.of()));
    }
}
