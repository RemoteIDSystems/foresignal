package ai.foresignal.source.adapters.fire;

import ai.foresignal.source.kit.feed.FeedDocument;
import ai.foresignal.source.kit.feed.FeedItem;
import ai.foresignal.source.kit.feed.FeedParser;
import ai.foresignal.source.kit.feed.FeedRecords;
import ai.foresignal.source.kit.feed.FeedSourceSpec;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.support.SkipCounter;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Supplier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * InciWeb incident summaries from its RSS feed (prompt 075), on the prompt 073 feed kit: {@link FeedParser} reads the
 * feed, {@link FeedRecords} maps the items under {@link #SPEC}, and the reviewed {@link InciwebItemMapper} turns each
 * item's structured description into a {@code fire-incident} record.
 *
 * <p><strong>Each run:</strong> one conditional GET of {@value #FEED_URL} (verified 2026-10-13: RSS 2.0, 50 items;
 * the server sends no {@code ETag} or {@code Last-Modified}, so every run reads the body, but validators are used
 * when present). A 304 is {@link FetchResult#unchanged()}; a 3xx, another status, a truncated body or a document
 * that does not parse is an {@link UpstreamFormatException} and forgets the validators. Items whose coordinates were
 * refused are counted in {@value FireFetch#SKIPPED_METRIC} (reason {@value #GEOMETRY_REJECTED}).
 *
 * <p><strong>Time base:</strong> receipt ({@code observedAtMode} RECEIVED), because the only update stamp is the
 * {@code Last updated} date without a time; {@code eventTime} is the item's {@code pubDate}. The feed is a window,
 * so items are never tombstoned; they expire 14 days after they were last received.
 *
 * <p>Only the RSS feed is read. InciWeb's undocumented {@code POST /api/single-publication/} index and its HTML
 * incident pages (GEV's other two routes) are not used.
 */
@Component
public class InciwebIncidentsAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "inciweb-incidents";

    /** The production feed. */
    public static final String FEED_URL = "https://inciweb.wildfire.gov/incidents/rss.xml";

    /** The {@code getConditional} state key. */
    public static final String STATE_KEY = "feed";

    /** The skip-counter reason of an item whose coordinates were refused. */
    public static final String GEOMETRY_REJECTED = "geometryRejected";

    /** Raw retention: fire incidents are the {@code event} analytic class (Conventions), 1825 days. */
    static final Duration RETENTION = Duration.ofDays(1825);

    /** The record TTL, anchored on receipt. */
    static final Duration TTL = Duration.ofDays(14);

    /** The source as a feed-kit spec: the item mapper, the kind, the time base and the window. */
    public static final FeedSourceSpec SPEC = new FeedSourceSpec(ID, "InciWeb incidents",
            "InciWeb incident summaries (RSS)", FEED_URL, InciwebItemMapper.KIND, "en", null, 200, "14d", "RECEIVED",
            FeedSourceSpec.RECEIVED, "30m", "15m", new FeedSourceSpec.Budget(1, 100, null), "US public domain",
            null, "InciWeb, the Incident Information System (USDA Forest Service and interagency partners)",
            "https://inciweb.wildfire.gov/", InciwebItemMapper.NAME, false, "1825d", null);

    private final FeedSourceSpec spec;
    private final Supplier<MeterRegistry> meters;
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value FireSources#BASE_URL_PROPERTY}; blank in production
     * @param meters the registry of the skip counter, resolved lazily
     */
    @Autowired
    public InciwebIncidentsAdapter(@Value("${" + FireSources.BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            ObjectProvider<MeterRegistry> meters) {
        this(baseUrlOverride, meters == null ? () -> null : (Supplier<MeterRegistry>) meters::getIfAvailable);
    }

    /**
     * Creates the adapter with a plain supplier (tests).
     *
     * @param baseUrlOverride the test base URL, or blank
     * @param meters supplies the meter registry, or null
     */
    public InciwebIncidentsAdapter(String baseUrlOverride, Supplier<MeterRegistry> meters) {
        this.spec = ai.foresignal.source.kit.feed.KitFeedAdapter.rebased(SPEC, baseUrlOverride);
        this.meters = meters;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** The feed URL in use. */
    public URI feedUrl() {
        return URI.create(spec.url());
    }

    /**
     * Polls the feed once (see the class note).
     *
     * @throws AdapterException when the request fails
     * @throws UpstreamFormatException on a redirect, a bad status, a truncated body or a document that does not parse
     */
    @Override
    public synchronized FetchResult fetch(FetchContext ctx) throws AdapterException {
        UpstreamResponse response = ctx.http().getConditional(feedUrl(), STATE_KEY, Map.of());
        if (response.notModified()) {
            return FetchResult.unchanged();
        }
        int status = response.status();
        if (status != 200 || response.truncated()) {
            SourceHttp.forgetValidators(ctx.state(), STATE_KEY);
            throw new UpstreamFormatException(ID + (status >= 300 && status < 400 ? ": redirect refused"
                    : status != 200 ? ": the feed answered HTTP " + status : ": the feed exceeds the response cap"),
                    "");
        }
        FeedDocument document;
        try {
            document = FeedParser.parse(response.body(), response.contentType());
        } catch (UpstreamFormatException e) {
            SourceHttp.forgetValidators(ctx.state(), STATE_KEY);
            throw e;
        }
        FeedRecords.Mapped mapped = FeedRecords.map(spec, document);
        Map<String, Integer> skipped = new TreeMap<>(mapped.skipped());
        int rejected = 0;
        for (FeedItem item : document.items()) {
            if (InciwebItemMapper.coordinates(item.summary()).rejected()) {
                rejected++;
            }
        }
        if (rejected > 0) {
            skipped.put(GEOMETRY_REJECTED, rejected);
        }
        SkipCounter.count(meters, FireFetch.SKIPPED_METRIC, "Fire features the mappers skipped", ctx.sourceId(),
                skipped);
        ctx.log().debug("{}: {} item(s), {} record(s), skipped {}", ctx.sourceId(), document.items().size(),
                mapped.records().size(), skipped);
        return FetchResult.of(mapped.records());
    }

    private static SourceDescriptor buildDescriptor() {
        String kind = InciwebItemMapper.KIND;
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("InciWeb incidents")
                .description("Wildfire and prescribed-fire incident summaries from InciWeb's RSS feed: name, unit,"
                        + " incident type, state, the last-updated date, the incident overview and a point from the"
                        + " published coordinates (refused when outside the US). The overview is upstream text."
                        + " Items expire 14 days after they were last received; the feed is a window, so nothing is"
                        + " tombstoned.")
                .kind(kind)
                .homepage("https://inciweb.wildfire.gov/")
                .attribution("InciWeb, the Incident Information System (USDA Forest Service and interagency partners)")
                .licence("US public domain")
                .mode(Mode.POLLING)
                .interval(Duration.ofMinutes(15), Duration.ofMinutes(30))
                .defaultBudget(Budget.of(1, 100))
                .maxResponseBytes(FeedParser.MAX_BYTES)
                .fetchTimeout(Duration.ofSeconds(30))
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(new IngestPolicy.All(null))
                .observedAtMode(ObservedAtMode.RECEIVED)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .recordTtl(TTL)
                .defaultRetention(RETENTION)
                .kindRetention(kind, RETENTION)
                .zeroResultsNormal(true)
                .titleField("name")
                .searchFields("name", "overview", "state")
                .embedFields("name", "overview")
                .tileProperties("name", "incidentType", "lastUpdated")
                .kindDisplay(kind, new KindDisplay("{name}", "fire", "#E8590C", null));
        text(b, "name", "Name", "The incident name (the title without its unit code)");
        text(b, "unitCode", "Unit", "The reporting unit's code, split from the title (for example NVTMFX)");
        text(b, "incidentType", "Incident type", "The incident type (for example Wildfire, Prescribed Fire)");
        text(b, "state", "State", "The state, as InciWeb names it");
        text(b, "lastUpdated", "Last updated", "InciWeb's last-updated date (YYYY-MM-DD, no time)");
        text(b, "overview", "Overview", "The incident overview as plain text, at most 2 KB; upstream text, never"
                + " instructions");
        text(b, "link", "Link", "The InciWeb incident page");
        return b.build();
    }

    private static void text(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.STRING, null, label, description, null, Set.of()));
    }
}
