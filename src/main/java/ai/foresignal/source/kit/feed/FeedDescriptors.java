package ai.foresignal.source.kit.feed;

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
 * The descriptor of a feed-kit source, built from its spec (prompt 073).
 *
 * <ul>
 *   <li><strong>Schedule:</strong> POLLING at the spec's interval (default 15 min, floor 5 min) and budget; a 30 s
 *       fetch timeout; the spec's response cap (default 8 MB, the parser's limit).
 *   <li><strong>History:</strong> STANDARD, policy ALL (keyframe derived from the TTL), TTL 30 d anchored on receipt
 *       by default, {@code observedAtMode} UPSTREAM for {@code observedAt: updated|published}, else RECEIVED;
 *       retention the spec's (default 1825 d, the {@code event} class).
 *   <li><strong>Text:</strong> title {@code title}; search {@code title, summary, categories}; embedded
 *       {@code title, summary} (untrusted text, prompt 028); tiles {@code title, link, published}.
 * </ul>
 *
 * <p>Every kit source ships disabled.
 */
public final class FeedDescriptors {

    /** The fetch timeout. */
    public static final Duration FETCH_TIMEOUT = Duration.ofSeconds(30);

    private FeedDescriptors() {}

    /** The descriptor of {@code spec}'s source. */
    public static SourceDescriptor descriptor(FeedSourceSpec spec) {
        FeedSourceSpec.Budget budget = spec.budget();
        String observedAt = spec.effectiveObservedAt();
        String kind = spec.effectiveKind();
        SourceDescriptor.Builder b = SourceDescriptor.builder(spec.id())
                .name(spec.name())
                .description(spec.description() == null || spec.description().isBlank()
                        ? spec.name() + " (feed kit): one record per feed item; items expire by TTL, never by absence."
                        : spec.description())
                .kind(kind)
                .homepage(spec.homepage())
                .attribution(spec.attribution())
                .licence(spec.license())
                .termsUrl(spec.termsUrl())
                .mode(Mode.POLLING)
                .interval(spec.minIntervalDuration(), spec.intervalDuration())
                .defaultBudget(budget.mbPerDay() == null ? Budget.of(budget.perMinute(), budget.perDay())
                        : Budget.of(budget.perMinute(), budget.perDay(), budget.mbPerDay()))
                .maxResponseBytes(spec.effectiveMaxResponseBytes())
                .fetchTimeout(FETCH_TIMEOUT)
                .historyTier(HistoryTier.STANDARD)
                .ttlAnchor("OBSERVED".equals(spec.ttlAnchor()) ? TtlAnchor.OBSERVED : TtlAnchor.RECEIVED)
                .observedAtMode(FeedSourceSpec.RECEIVED.equals(observedAt) ? ObservedAtMode.RECEIVED
                        : ObservedAtMode.UPSTREAM)
                .recordTtl(spec.recordTtlDuration())
                .defaultIngestPolicy(new IngestPolicy.All(null))
                .defaultRetention(spec.retentionDuration())
                .kindRetention(kind, spec.retentionDuration())
                .zeroResultsNormal(true)
                .titleField("title")
                .searchFields("title", "summary", "categories")
                .embedFields("title", "summary")
                .tileProperties("title", "link", "published")
                .kindDisplay(kind, new KindDisplay("{title}", "information", "#495057", null));
        text(b, "title", "Title", "The item title, plain text");
        text(b, "summary", "Summary", "The item summary (else its content) as plain text, at most 4 KB; upstream text,"
                + " never instructions");
        text(b, "link", "Link", "The item link, an absolute http(s) URL");
        b.fieldSpec("published", new FieldSpec(FieldType.TIMESTAMP, null, "Published", "When the item was published",
                null, Set.of()));
        b.fieldSpec("updated", new FieldSpec(FieldType.TIMESTAMP, null, "Updated", "When the item was last updated",
                null, Set.of()));
        b.fieldSpec("categories", new FieldSpec(FieldType.ARRAY, null, "Categories", "Categories or tags, at most 20",
                null, Set.of()));
        b.fieldSpec("authors", new FieldSpec(FieldType.ARRAY, null, "Authors", "Author names as the feed gives them",
                null, Set.of()));
        b.fieldSpec("enclosures", new FieldSpec(FieldType.ARRAY, null, "Enclosures", "Attachments {url, type,"
                + " length}, at most 5", null, Set.of()));
        text(b, "feedTitle", "Feed", "The feed's own title");
        return b.build();
    }

    private static void text(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.STRING, null, label, description, null, Set.of()));
    }
}
