package ai.foresignal.source.adapters.gdelt;

import ai.foresignal.source.formats.attention.TopicParams;
import ai.foresignal.source.formats.news.ArticleUrls;
import ai.foresignal.source.formats.news.NewsArticles;
import ai.foresignal.source.formats.news.NewsOutletLookup;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.spi.ValidatedParams;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code gdelt-articles} (prompt 133): news article metadata per attention topic from the GDELT DOC 2.0 API, as
 * {@code news-article} records ({@link NewsArticles}).
 *
 * <p>For each enabled topic (the {@code topics} parameter synced by {@code AttentionTopicService}, {@link TopicParams})
 * a run sends one request to {@code /api/v2/doc/doc} with {@code query={gdelt_query}}, {@code mode=ArtList},
 * {@code format=json}, {@code maxrecords=250}, {@code sort=DateDesc} and {@code timespan} = the time since the topic's
 * last run (at least 15 minutes, at most one day; the first run asks for one day). At most {@value #TOPICS_PER_RUN}
 * topics are read per run, the rest round-robin over later runs.
 *
 * <p><strong>Pacing:</strong> GDELT allows one request every 5 seconds per client. The descriptor sets
 * {@code hostPacing} 5 s, so {@code UpstreamClient}'s shared {@code HostPacer} keeps this source and
 * {@code gdelt-timelines} (which declares the same pacing) serial on {@code api.gdeltproject.org}.
 *
 * <p><strong>Metadata only:</strong> nothing but the ArtList answer is fetched: never the article, never the image.
 * An article found for several topics in one run is one record with every topic. The outlet id comes from the
 * {@link NewsOutletLookup} (the media registry; an unknown domain is registered as an {@code unrated} outlet).
 */
@Component
public class GdeltArticlesAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = NewsArticles.GDELT_SOURCE;

    /** Test-only override of the upstream. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.gdelt-articles.base-url";

    /** The least time between two request starts to the GDELT host, shared with {@code gdelt-timelines}. */
    public static final Duration HOST_PACING = Duration.ofSeconds(5);

    /** The most articles one request asks for (the DOC API's maximum). */
    public static final int MAX_RECORDS = 250;

    /** The most topics one run reads. */
    public static final int TOPICS_PER_RUN = 10;

    /** The shortest timespan, minutes. */
    public static final long MIN_TIMESPAN_MINUTES = 15;

    /** The longest timespan, minutes (one day). */
    public static final long MAX_TIMESPAN_MINUTES = 24 * 60;

    /** The adapter-state key of the next topic index. */
    static final String NEXT_KEY = "next";

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String baseUrl;
    private final ObjectProvider<NewsOutletLookup> outlets;

    /** Creates the adapter. */
    @Autowired
    public GdeltArticlesAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            ObjectProvider<NewsOutletLookup> outlets) {
        this.baseUrl = baseUrlOverride == null || baseUrlOverride.isBlank()
                ? GdeltTimelineAdapter.DEFAULT_BASE_URL : baseUrlOverride.trim().replaceAll("/+$", "");
        this.outlets = outlets;
    }

    /** Creates the adapter without an outlet registry (tests, descriptor checks). */
    public GdeltArticlesAdapter(String baseUrlOverride) {
        this(baseUrlOverride, null);
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        for (String entry : params.stringList(TopicParams.PARAM)) {
            String problem = TopicParams.gdeltProblem(entry);
            if (problem != null) {
                problems.put(TopicParams.PARAM, problem);
                break;
            }
        }
        return problems;
    }

    /** The request URL of one topic. */
    URI uri(String query, String timespan) {
        return URI.create(baseUrl + "/api/v2/doc/doc?query=" + enc(query) + "&mode=ArtList&format=json&maxrecords="
                + MAX_RECORDS + "&sort=DateDesc&timespan=" + timespan);
    }

    /**
     * The timespan of a topic: the minutes since its last run, at least {@value #MIN_TIMESPAN_MINUTES} and at most a
     * day; {@code 1d} when it never ran.
     */
    static String timespan(Instant lastRun, Instant now) {
        if (lastRun == null) {
            return "1d";
        }
        long minutes = Duration.between(lastRun, now).toMinutes();
        if (minutes >= MAX_TIMESPAN_MINUTES) {
            return "1d";
        }
        return Math.max(MIN_TIMESPAN_MINUTES, minutes) + "min";
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        List<TopicParams.GdeltTopic> topics = TopicParams.gdelt(ctx.params() == null ? List.of()
                : ctx.params().stringList(TopicParams.PARAM));
        if (topics.isEmpty()) {
            return FetchResult.of(List.of());
        }
        AdapterStateStore state = ctx.state();
        Instant now = ctx.now();
        int next = index(state.get(NEXT_KEY).orElse(null), topics.size());
        int count = Math.min(TOPICS_PER_RUN, topics.size());
        Map<String, GdeltArtListParser.Entry> byId = new LinkedHashMap<>();
        Map<String, Set<String>> topicsById = new LinkedHashMap<>();
        for (int i = 0; i < count; i++) {
            TopicParams.GdeltTopic topic = topics.get((next + i) % topics.size());
            Instant last = parseInstant(state.get(lastKey(topic.id())).orElse(null));
            List<GdeltArtListParser.Entry> entries = GdeltArtListParser.parse(get(ctx, uri(topic.query(),
                    timespan(last, now))));
            for (GdeltArtListParser.Entry e : entries) {
                String id = ArticleUrls.externalId(e.url());
                if (id == null) {
                    continue;
                }
                byId.putIfAbsent(id, e);
                topicsById.computeIfAbsent(id, k -> new LinkedHashSet<>()).add(topic.id());
            }
            state.put(lastKey(topic.id()), now.toString());
        }
        state.put(NEXT_KEY, Integer.toString((next + count) % topics.size()));
        NewsOutletLookup lookup = outlets == null ? null : outlets.getIfAvailable();
        List<NormalizedRecord> out = new ArrayList<>();
        for (Map.Entry<String, GdeltArtListParser.Entry> item : byId.entrySet()) {
            GdeltArtListParser.Entry e = item.getValue();
            String domain = ArticleUrls.domain(e.url());
            String outletId = null;
            if (lookup != null && domain != null) {
                try {
                    outletId = lookup.resolve(domain, e.sourceCountry(), e.language());
                } catch (RuntimeException ex) {
                    ctx.log().debug("{}: outlet lookup failed for {}: {}", ID, domain, ex.toString());
                }
            }
            NormalizedRecord record = NewsArticles.record(new NewsArticles.Article(e.title(), e.url(), outletId,
                    e.language(), e.sourceCountry(), e.seenAt(), null, new ArrayList<>(topicsById.get(item.getKey())),
                    null, e.imageUrl()));
            if (record != null) {
                out.add(record);
            }
        }
        ctx.log().debug("{}: {} article(s) from {} topic(s)", ID, out.size(), count);
        return FetchResult.of(out);
    }

    private byte[] get(FetchContext ctx, URI uri) throws AdapterException {
        UpstreamResponse response = ctx.http().get(uri, Map.of("Accept", "application/json"));
        if (response.status() != 200 || response.truncated()) {
            throw new UpstreamFormatException(ID + ": GDELT" + (response.status() != 200
                    ? " answered HTTP " + response.status() : " response exceeds the cap"), "");
        }
        return response.body();
    }

    static String lastKey(String topicId) {
        return "last." + topicId;
    }

    private static int index(String stored, int size) {
        if (stored == null) {
            return 0;
        }
        try {
            int value = Integer.parseInt(stored.strip());
            return value < 0 ? 0 : value % size;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static Instant parseInstant(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(text.strip());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder builder = SourceDescriptor.builder(ID)
                .name("GDELT news articles")
                .description("News article metadata (title, link, outlet domain, language, source country, time) per"
                        + " attention topic from the GDELT DOC 2.0 ArtList API. Article bodies and bylines are never"
                        + " fetched. Topics come from /v1/attention/topics; GDELT allows one request every 5 seconds,"
                        + " shared with gdelt-timelines.")
                .homepage("https://www.gdeltproject.org/")
                .attribution(GdeltTimelineAdapter.ATTRIBUTION)
                .licence("GDELT open terms: free use with citation and a link to the GDELT Project")
                .termsUrl("https://www.gdeltproject.org/about.html#termsofuse")
                .mode(Mode.POLLING)
                .egress(Egress.OPEN)
                .kind(NewsArticles.KIND)
                .param(new ParamSpec(TopicParams.PARAM, ParamType.STRING_LIST,
                        "Attention topics, synced from /v1/attention/topics: one JSON object {\"id\", \"query\"} per"
                                + " entry", List.of(), false, List.of()))
                .interval(Duration.ofMinutes(15), Duration.ofMinutes(30))
                .defaultBudget(Budget.of(10, 1000))
                .hostPacing(HOST_PACING)
                .fetchTimeout(Duration.ofMinutes(3))
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(IngestPolicy.all())
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .recordTtl(null)
                .zeroResultsNormal(true)
                .defaultRetention(NewsArticles.RETENTION)
                .kindRetention(NewsArticles.KIND, NewsArticles.RETENTION)
                .titleField("title")
                .embedFields("title")
                .searchFields("title", "domain", "summary");
        NewsArticles.declareFieldSpecs(builder);
        return builder.build();
    }
}
