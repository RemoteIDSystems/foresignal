package ai.foresignal.source.adapters.gbfs;

import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import java.time.Duration;
import java.util.List;
import java.util.Set;

/**
 * The descriptor of a {@code gbfs-{id}} source (prompt 071).
 *
 * <ul>
 *   <li><strong>Schedule:</strong> POLLING every 60 s (minimum 30 s), budget 3/min and 2000/day; responses capped at
 *       5 MB, 15 s per fetch; <strong>redirects refused</strong> ({@code followRedirects false}: a 3xx reaches the
 *       adapter, which fails the run with {@code redirect refused}).
 *   <li><strong>Policy:</strong> STANDARD, {@code LATEST {keyframe 20m, edgeMinInterval 30s}}, TTL 30 min anchored
 *       on RECEIVED, which satisfies {@code keyframe ≤ ttl − interval − 30 s} ({@code 1200 ≤ 1800 − 60 − 30}).
 *       Significant {@code name, capacity, isInstalled, isRenting, isReturning, availability}: a change of
 *       service state is a version, while the counts travel as live updates.
 *   <li><strong>Time:</strong> UPSTREAM; {@code observedAt} = the station's {@code last_reported}.
 *   <li><strong>Retention:</strong> 365 d, the {@code state} class (Conventions: SAMPLED-style station status).
 * </ul>
 *
 * <p>License, attribution and terms come from the system entry.
 */
public final class GbfsDescriptors {

    /** The default poll interval. */
    public static final Duration INTERVAL = Duration.ofSeconds(60);

    /** The minimum poll interval. */
    public static final Duration MIN_INTERVAL = Duration.ofSeconds(30);

    /** The record TTL. */
    public static final Duration TTL = Duration.ofMinutes(30);

    /** The LATEST keyframe. */
    public static final Duration KEYFRAME = Duration.ofMinutes(20);

    /** The trigger-edge interval. */
    public static final Duration EDGE_MIN_INTERVAL = Duration.ofSeconds(30);

    /** The response cap (GEV {@code GBFS_MAX_BODY_BYTES}). */
    public static final int MAX_RESPONSE_BYTES = 5 * 1024 * 1024;

    /** The fetch timeout. */
    public static final Duration FETCH_TIMEOUT = Duration.ofSeconds(15);

    /** Raw retention: the {@code state} class (Conventions), 365 days. */
    public static final Duration RETENTION = Duration.ofDays(365);

    /** The significant fields. */
    public static final List<String> SIGNIFICANT =
            List.of("name", "capacity", "isInstalled", "isRenting", "isReturning", "availability");

    /** The tile properties. */
    public static final List<String> TILES = List.of("bikesAvailable", "docksAvailable", "availability",
            "ebikesAvailable");

    /** The {@code language} parameter: the language of GBFS 3.0 localized names. */
    public static final String LANGUAGE_PARAM = "language";

    private GbfsDescriptors() {}

    /** The LATEST policy {@code {keyframe 20m, edgeMinInterval 30s}}. */
    public static IngestPolicy policy() {
        return new IngestPolicy.Latest(KEYFRAME, EDGE_MIN_INTERVAL);
    }

    /** The descriptor of {@code system}'s source. */
    public static SourceDescriptor descriptor(GbfsSystem system) {
        SourceDescriptor.Builder b = SourceDescriptor.builder(system.sourceId())
                .name(system.provider() + " bikeshare stations")
                .description("Docked bikeshare stations of " + system.provider() + " (" + system.city() + ") from"
                        + " the GBFS station_information and station_status feeds, merged into one snapshot per"
                        + " station. A version is written when a station's service state changes (name, capacity,"
                        + " installed, renting, returning or availability); bike and dock counts arrive as live"
                        + " updates. observedAt = the station's last_reported (a station without one is dropped)."
                        + " The feed's ttl is honored and redirects are refused.")
                .kind(GbfsParser.KIND)
                .homepage(system.licenseUrl().toString())
                .attribution(system.attribution())
                .licence(system.license())
                .termsUrl(system.licenseUrl().toString())
                .mode(Mode.POLLING)
                .interval(MIN_INTERVAL, INTERVAL)
                .defaultBudget(3, 2000)
                .maxResponseBytes(MAX_RESPONSE_BYTES)
                .fetchTimeout(FETCH_TIMEOUT)
                .followRedirects(false)
                .historyTier(HistoryTier.STANDARD)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .recordTtl(TTL)
                .defaultIngestPolicy(policy())
                .significantFields(SIGNIFICANT.toArray(String[]::new))
                .defaultRetention(RETENTION)
                .kindRetention(GbfsParser.KIND, RETENTION)
                .zeroResultsNormal(false)
                .titleField("name")
                .searchFields("name", "address")
                .tileProperties(TILES.toArray(String[]::new))
                .param(ParamSpec.optional(LANGUAGE_PARAM, ParamType.STRING, "Language of GBFS 3.0 localized station"
                        + " names, a BCP 47 tag such as en or fr; the first name is used when it is missing",
                        GbfsParser.DEFAULT_LANGUAGE))
                .kindDisplay(GbfsParser.KIND, new KindDisplay("{name}", "bicycle", "#1971C2", null));
        text(b, "name", "Name", "The station's name");
        text(b, "address", "Address", "The station's street address, when the operator publishes one");
        integer(b, "capacity", "Capacity", "Number of docks");
        text(b, "regionId", "Region", "GBFS region_id of the station");
        integer(b, "bikesAvailable", "Bikes", "Bikes (GBFS 3.0: vehicles) available to rent");
        integer(b, "ebikesAvailable", "E-bikes", "E-bikes available, when a GBFS 1.x or 2.x feed says");
        integer(b, "docksAvailable", "Docks", "Empty docks available for returns");
        integer(b, "disabledBikes", "Disabled bikes", "Bikes at the station that cannot be rented");
        integer(b, "disabledDocks", "Disabled docks", "Docks out of service");
        bool(b, "isInstalled", "Installed", "The station is installed on the street");
        bool(b, "isRenting", "Renting", "The station is renting bikes");
        bool(b, "isReturning", "Returning", "The station accepts returns");
        b.fieldSpec("availability", new FieldSpec(FieldType.STRING, null, "Availability",
                "out-of-service (not installed or not renting), empty (0 bikes), low (at most 2 bikes), full (0 docks)"
                        + " or ok, the first that applies", null, Set.of()));
        text(b, "system", "System", "The reviewed system id");
        return b.build();
    }

    private static void text(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.STRING, null, label, description, null, Set.of()));
    }

    private static void integer(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.INTEGER, null, label, description, null, Set.of()));
    }

    private static void bool(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.BOOLEAN, null, label, description, null, Set.of()));
    }
}
