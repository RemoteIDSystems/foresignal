package ai.foresignal.source.adapters.cap;

import ai.foresignal.collect.BudgetExceededException;
import ai.foresignal.source.formats.cap.CapAlert;
import ai.foresignal.source.formats.cap.CapParser;
import ai.foresignal.source.formats.cap.CapThreadResolver;
import ai.foresignal.source.kit.feed.FeedDocument;
import ai.foresignal.source.kit.feed.FeedItem;
import ai.foresignal.source.kit.feed.FeedParser;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * One reviewed CAP feed (prompt 089): a conditional GET of the Atom/RSS index, then a fetch of the CAP
 * documents whose link is new (at most {@value #MAX_DOCS_PER_RUN} a run, serial, within the budget; a budget refusal
 * stops the run cleanly and the rest are read next run), parsed with prompt 067's {@link CapParser} and threaded
 * with the shared {@link CapThreadResolver}. Only links on the feed's own host (or its reviewed
 * {@link CapFeed#documentHosts}) are fetched.
 *
 * <p>An {@code Update} versions the same record; a {@code Cancel} is a tombstone of the thread. Absence from
 * the index never closes an alert; alerts expire through their own {@code expires} ({@code validUntil}).
 */
public final class CapFeedAdapter implements PollingAdapter {

    /**
     * The most CAP documents fetched in one run: the 6-a-minute budget less the index request. The documents left
     * over are read on the next run (the index validators are forgotten so a 304 cannot hide them).
     */
    public static final int MAX_DOCS_PER_RUN = 5;

    /** The most document links remembered. */
    public static final int MAX_SEEN = 5_000;

    /** The adapter-state key for the index validators. */
    static final String INDEX_STATE_KEY = "cap:index";

    /** The adapter-state key for the remembered document links. */
    static final String SEEN_STATE_KEY = "cap:seen";

    /** The adapter-state key of the non-{@code Actual} document links skipped while {@code includeTests} is off. */
    static final String SKIPPED_TESTS_STATE_KEY = "cap:skippedTests";

    /** The adapter-state key of the {@code includeTests} value the index validators belong to. */
    static final String INCLUDE_TESTS_STATE_KEY = "cap:includeTests";

    /** The retention of the {@code cap-alert} kind (the event class). */
    static final Duration RETENTION = Duration.ofDays(1825);

    private final CapFeed feed;
    private final SourceDescriptor descriptor;

    /** Creates the adapter for one reviewed feed. */
    public CapFeedAdapter(CapFeed feed) {
        this.feed = feed;
        this.descriptor = build(feed);
    }

    /** The reviewed feed this adapter serves. */
    public CapFeed feed() {
        return feed;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        boolean includeTests = ctx.params().bool("includeTests", false);
        if ("cap-inline".equals(feed.format())) {
            return fetchInline(ctx, includeTests);
        }
        return fetchIndex(ctx, includeTests);
    }

    private FetchResult fetchIndex(FetchContext ctx, boolean includeTests) throws AdapterException {
        String mode = Boolean.toString(includeTests);
        if (!mode.equals(ctx.state().get(INCLUDE_TESTS_STATE_KEY).orElse(null))) {
            // Turning includeTests on must re-read the index even if it is unchanged, to reach the skipped documents.
            SourceHttp.forgetValidators(ctx.state(), INDEX_STATE_KEY);
            ctx.state().put(INCLUDE_TESTS_STATE_KEY, mode);
        }
        UpstreamResponse index = ctx.http().getConditional(feed.feedUrl(), INDEX_STATE_KEY);
        if (index.notModified()) {
            return FetchResult.unchanged();
        }
        if (index.status() != 200) {
            throw new UpstreamFormatException("The CAP index of " + feed.id() + " answered HTTP " + index.status(),
                    "");
        }
        FeedDocument document = FeedParser.parse(index.body(), index.contentType());
        SeenSet seen = SeenSet.load(ctx.state(), SEEN_STATE_KEY);
        SeenSet skippedTests = SeenSet.load(ctx.state(), SKIPPED_TESTS_STATE_KEY);
        CapThreadResolver resolver = CapThreadResolver.load(ctx.state(), ctx.now());
        List<NormalizedRecord> records = new ArrayList<>();
        int fetched = 0;
        int refused = 0;
        boolean backlog = false;
        for (FeedItem item : document.items()) {
            String link = item.link();
            if (link == null || link.isBlank() || seen.contains(link)
                    || (!includeTests && skippedTests.contains(link))) {
                continue;
            }
            URI uri;
            try {
                uri = URI.create(link.strip());
            } catch (IllegalArgumentException e) {
                uri = null;
            }
            if (!feed.allowsDocument(uri)) {
                // An index is upstream data: a link off the feed's reviewed hosts is never fetched.
                refused++;
                seen.add(link);
                continue;
            }
            if (fetched >= MAX_DOCS_PER_RUN) {
                backlog = true;
                break;
            }
            fetched++;
            byte[] body;
            try {
                UpstreamResponse response = ctx.http().get(uri);
                if (response.status() != 200 || response.truncated()) {
                    continue;
                }
                body = response.body();
            } catch (BudgetExceededException e) {
                backlog = true;
                ctx.log().info("{}: the budget stopped the document fetches; the next run continues",
                        ctx.sourceId());
                break;
            } catch (AdapterException e) {
                if (e.httpStatus().orElse(0) == 404 || e.httpStatus().orElse(0) == 410) {
                    seen.add(link);
                }
                continue;
            }
            mapped(ctx, body, resolver, seen, skippedTests, link, includeTests, records);
        }
        if (refused > 0) {
            ctx.log().warn("{}: refused {} document link(s) outside the feed's reviewed hosts", ctx.sourceId(),
                    refused);
        }
        if (backlog) {
            // The index may answer 304 next time; forget its validators so the documents left over are still read.
            SourceHttp.forgetValidators(ctx.state(), INDEX_STATE_KEY);
        }
        resolver.save(ctx.state());
        seen.save(ctx.state());
        skippedTests.save(ctx.state());
        return FetchResult.of(records);
    }

    private FetchResult fetchInline(FetchContext ctx, boolean includeTests) throws AdapterException {
        UpstreamResponse response = ctx.http().getConditional(feed.feedUrl(), INDEX_STATE_KEY);
        if (response.notModified()) {
            return FetchResult.unchanged();
        }
        if (response.status() != 200) {
            throw new UpstreamFormatException("The CAP document of " + feed.id() + " answered HTTP "
                    + response.status(), "");
        }
        CapThreadResolver resolver = CapThreadResolver.load(ctx.state(), ctx.now());
        List<NormalizedRecord> records = new ArrayList<>();
        mapped(ctx, response.body(), resolver, null, null, null, includeTests, records);
        resolver.save(ctx.state());
        return FetchResult.of(records);
    }

    /**
     * Parses, threads and maps one CAP document. A document that does not parse is marked seen; a non-{@code Actual}
     * one skipped because {@code includeTests} is off goes to {@code skippedTests} instead, so enabling
     * {@code includeTests} later still picks it up.
     */
    private void mapped(FetchContext ctx, byte[] body, CapThreadResolver resolver, SeenSet seen,
            SeenSet skippedTests, String link, boolean includeTests, List<NormalizedRecord> records) {
        CapAlert alert;
        try {
            alert = CapParser.parse(body);
        } catch (UpstreamFormatException e) {
            if (seen != null) {
                seen.add(link);
            }
            return;
        }
        if (!includeTests && !"Actual".equals(alert.status())) {
            if (skippedTests != null) {
                skippedTests.add(link);
            }
            return;
        }
        if (seen != null) {
            seen.add(link);
        }
        String thread = resolver.resolve(alert, null, ctx.now());
        NormalizedRecord record = CapAlertRecords.map(alert, thread, feed.languages(), includeTests);
        if (record != null) {
            records.add(record);
        }
    }

    private static SourceDescriptor build(CapFeed feed) {
        return SourceDescriptor.builder(feed.id())
                .name(feed.authority() + " CAP alerts")
                .description("Reviewed national CAP feed (" + feed.country() + ", " + feed.authority() + "), "
                        + "seeded from the WMO Register of Alerting Authorities. Update and Cancel messages "
                        + "follow the shared CAP update chain. " + (feed.notes() == null ? "" : feed.notes()))
                .kind(CapAlertRecords.KIND)
                .mode(Mode.POLLING)
                .homepage(feed.feedUrl().toString())
                .attribution(feed.authority())
                .licence(feed.licence())
                .termsUrl(feed.termsUrl() == null ? null : feed.termsUrl().toString())
                .param(ParamSpec.optional("includeTests", ParamType.BOOLEAN,
                        "Keep messages whose status is not Actual (Exercise, Test, System, Draft)", false))
                .interval(Duration.ofMinutes(5), Duration.ofMinutes(5))
                .defaultBudget(6, 1_000)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .recordTtl(null)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .entityKey("cap-identifier", "identifier")
                .searchFields("event", "headline", "areaDesc")
                .embedFields("headline", "description")
                .zeroResultsNormal(true)
                .kindDisplay(CapAlertRecords.KIND, new KindDisplay("{event}", "warning", "#C92A2A", null))
                .build();
    }

    /** A bounded, persisted set of document links already fetched. */
    static final class SeenSet {

        private final AdapterStateStore store;
        private final String key;
        private final LinkedHashSet<String> values;
        private boolean dirty;

        private SeenSet(AdapterStateStore store, String key, LinkedHashSet<String> values) {
            this.store = store;
            this.key = key;
            this.values = values;
        }

        static SeenSet load(AdapterStateStore store, String key) {
            LinkedHashSet<String> values = new LinkedHashSet<>();
            store.get(key).orElse("").lines().filter(line -> !line.isBlank()).forEach(values::add);
            return new SeenSet(store, key, values);
        }

        boolean contains(String link) {
            return values.contains(link);
        }

        void add(String link) {
            if (link == null || !values.add(link)) {
                return;
            }
            dirty = true;
            while (values.size() > MAX_SEEN) {
                var oldest = values.iterator();
                oldest.next();
                oldest.remove();
            }
        }

        void save(AdapterStateStore store) {
            if (!dirty) {
                return;
            }
            store.put(key, String.join("\n", values));
        }
    }
}
