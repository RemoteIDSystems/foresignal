package ai.foresignal.source.adapters.roads;

import ai.foresignal.source.kit.feature.FeatureGeometry;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import java.time.Duration;
import java.util.Set;

/**
 * The descriptor settings every road-event source shares (prompt 074).
 *
 * <ul>
 *   <li><strong>History:</strong> {@code kind = road-event}, STANDARD, policy ALL with no keyframe,
 *       {@code observedAtMode} UPSTREAM (each format's update time), {@code ttlAnchor} RECEIVED, no record TTL
 *       (events end by tombstone), no significant or volatile fields; retention 1825 d (the {@code event} class).
 *   <li><strong>Display and search:</strong> title {@code headline}; search {@code headline, description, roads};
 *       tiles {@code category, severity, isClosure, headline}; embeddings {@code headline, description}; the interval
 *       {@code startsAt}/{@code endsAt}; the {@code construction} icon.
 *   <li><strong>Fetching:</strong> POLLING; zero results are normal (a feed can be empty, such as Finland's weight
 *       restrictions in autumn).
 * </ul>
 *
 * <p>Every source ships disabled; each adds its own id, texts, schedule, budget and limits.
 */
public final class RoadEventDescriptors {

    /** Raw retention: the {@code event} class (Conventions), 1825 days. */
    public static final Duration RETENTION = Duration.ofDays(1825);

    private RoadEventDescriptors() {}

    /**
     * A descriptor builder with the shared settings.
     *
     * @param id the source id
     * @param name the display name
     * @param description what the source holds
     * @param minInterval the shortest interval an operator may set
     * @param defaultInterval the default interval
     * @param budget the default budget
     * @param maxResponseBytes the response cap
     */
    public static SourceDescriptor.Builder builder(String id, String name, String description, Duration minInterval,
            Duration defaultInterval, Budget budget, int maxResponseBytes) {
        SourceDescriptor.Builder b = SourceDescriptor.builder(id)
                .name(name)
                .description(description)
                .kind(RoadEvents.KIND)
                .mode(Mode.POLLING)
                .interval(minInterval, defaultInterval)
                .defaultBudget(budget)
                .maxResponseBytes(maxResponseBytes)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .recordTtl(null)
                .defaultIngestPolicy(new IngestPolicy.All(null))
                .defaultRetention(RETENTION)
                .kindRetention(RoadEvents.KIND, RETENTION)
                .zeroResultsNormal(true)
                .titleField("headline")
                .searchFields("headline", "description", "roads")
                .tileProperties("category", "severity", "isClosure", "headline")
                .embedFields("headline", "description")
                .intervalFields("startsAt", "endsAt")
                .kindDisplay(RoadEvents.KIND, new KindDisplay("{headline}", "construction", "#F08C00", null));
        text(b, "category", "Category", "roadwork, incident, closure, restriction, weather, event or other");
        text(b, "subtype", "Subtype", "The upstream's own event type or subtype code, in lower-kebab case");
        text(b, "severity", "Severity", "unknown, minor, moderate, major or severe");
        text(b, "headline", "Headline", "A one-line summary, at most 200 characters; upstream text, untrusted");
        text(b, "description", "Description", "Plain-text description, at most 4 KB; upstream text, untrusted");
        b.fieldSpec("roads", new FieldSpec(FieldType.ARRAY, null, "Roads", "Up to 10 road names or numbers", null,
                Set.of()));
        text(b, "direction", "Direction", "both, northbound, southbound, eastbound, westbound, inbound, outbound or"
                + " unknown");
        text(b, "status", "Status", "active, or planned when the event has not started yet");
        b.fieldSpec("isClosure", new FieldSpec(FieldType.BOOLEAN, null, "Road closed", "True when the road itself"
                + " (not only a lane) is closed", null, Set.of()));
        text(b, "lanesAffected", "Lanes affected", "The upstream's lane impact, as text");
        b.fieldSpec("speedLimitKph", new FieldSpec(FieldType.NUMBER, "km/h", "Temporary speed limit", "The reduced"
                + " speed limit in force, when the upstream gives one", null, Set.of()));
        b.fieldSpec("startsAt", new FieldSpec(FieldType.TIMESTAMP, null, "Starts", "The scheduled start (ISO-8601)",
                null, Set.of()));
        b.fieldSpec("endsAt", new FieldSpec(FieldType.TIMESTAMP, null, "Ends", "The scheduled end of the last"
                + " interval (ISO-8601), when the upstream gives one", null, Set.of()));
        text(b, "format", "Format", "open511, wzdx, digitraffic or datex3");
        text(b, "url", "URL", "The upstream's own URL of the event");
        text(b, "language", "Language", "The language of the text (ISO 639-1)");
        b.fieldSpec(FeatureGeometry.SIMPLIFIED_KEY, new FieldSpec(FieldType.BOOLEAN, null, "Geometry simplified",
                "Present and true when the geometry had more than 9,500 vertices and was simplified", null, Set.of()));
        b.fieldSpec(FeatureGeometry.TOLERANCE_KEY, new FieldSpec(FieldType.NUMBER, "deg", "Simplification tolerance",
                "The tolerance used to simplify the geometry", null, Set.of()));
        return b;
    }

    private static void text(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.STRING, null, label, description, null, Set.of()));
    }
}
