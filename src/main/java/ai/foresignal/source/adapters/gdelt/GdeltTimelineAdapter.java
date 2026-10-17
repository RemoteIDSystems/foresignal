package ai.foresignal.source.adapters.gdelt;

import ai.foresignal.source.formats.attention.TopicParams;
import ai.foresignal.source.series.SeriesPoint;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
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
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code gdelt-timelines} (prompt 117): news volume and tone per attention topic from the GDELT DOC 2.0 API, as
 * {@code series-point} and {@code series-latest} records.
 *
 * <p>The {@code topics} parameter is synced from the enabled attention topics by {@code AttentionTopicService}
 * ({@link TopicParams}); the adapter never reads the store. For each topic a run sends two requests to
 * {@code /api/v2/doc/doc} with {@code query={gdelt_query}}, {@code format=json}, {@code timelinesmooth=0}:
 * {@code mode=TimelineVolRaw} and {@code mode=TimelineTone}, with {@code timespan} = the time since the topic's last
 * stored bucket plus one 15-minute step (at most {@value #MAX_TIMESPAN_MINUTES} minutes, three days; the first run
 * asks for {@value #FIRST_TIMESPAN}). GDELT allows one request every 5 seconds: the adapter keeps at least
 * {@link #SPACING} between the starts of its requests ({@link RequestSpacing}), on top of the {@code perMinute 10}
 * budget. A run covers at most {@value #TOPICS_PER_RUN} topics (two requests each, within the per-minute budget);
 * more topics are taken round-robin over the following runs.
 *
 * <p>Records: {@code gdelt.{topicId}.vol} (value = matching article count per step; {@code extra.total} = all
 * articles GDELT monitored in that step; {@code extra.share} = count ÷ total) and {@code gdelt.{topicId}.tone}
 * (average tone). The step is the API's timeline resolution (15 minutes for spans under 72 hours), recorded as
 * {@code extra.resolution}. Family attention or tone, release lag 0. GDELT's open terms ask for a citation and link.
 */
@Component
public class GdeltTimelineAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "gdelt-timelines";

    /** The production upstream. */
    public static final String DEFAULT_BASE_URL = "https://api.gdeltproject.org";

    /** Test-only override. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.gdelt-timelines.base-url";

    /** The least time between two request starts (GDELT: one request every 5 seconds). */
    public static final Duration SPACING = Duration.ofSeconds(5);

    /** The longest timespan asked for: three days. */
    public static final long MAX_TIMESPAN_MINUTES = 3 * 24 * 60;

    /** The shortest timespan GDELT accepts. */
    public static final long MIN_TIMESPAN_MINUTES = 15;

    /** The first run's timespan. */
    public static final String FIRST_TIMESPAN = "7d";

    /** The most topics one run covers (two requests each). */
    public static final int TOPICS_PER_RUN = 5;

    /** The attribution GDELT's terms ask for. */
    public static final String ATTRIBUTION = "The GDELT Project (https://www.gdeltproject.org/)";

    /** The adapter-state key of the next topic index (round-robin). */
    static final String NEXT_KEY = "next";

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String baseUrl;
    private final RequestSpacing spacing;

    /** Creates the adapter with the system clock's spacing. */
    @Autowired
    public GdeltTimelineAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this(baseUrlOverride, RequestSpacing.system(SPACING));
    }

    /** Creates the adapter with {@code spacing} (tests pass a fake clock). */
    public GdeltTimelineAdapter(String baseUrlOverride, RequestSpacing spacing) {
        this.baseUrl = baseUrlOverride == null || baseUrlOverride.isBlank()
                ? DEFAULT_BASE_URL : baseUrlOverride.trim().replaceAll("/+$", "");
        this.spacing = spacing;
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

    /** The request URL of one timeline. */
    URI uri(String mode, String query, String timespan) {
        return URI.create(baseUrl + "/api/v2/doc/doc?query=" + enc(query) + "&mode=" + mode
                + "&format=json&timespan=" + timespan + "&timelinesmooth=0");
    }

    /**
     * The timespan of a topic: since its last stored bucket plus one 15-minute step, at least
     * {@value #MIN_TIMESPAN_MINUTES} minutes and at most three days ({@code 3d}); {@value #FIRST_TIMESPAN} when
     * nothing is stored.
     */
    static String timespan(Instant lastBucket, Instant now) {
        if (lastBucket == null) {
            return FIRST_TIMESPAN;
        }
        long minutes = Duration.between(lastBucket, now).toMinutes() + MIN_TIMESPAN_MINUTES;
        if (minutes >= MAX_TIMESPAN_MINUTES) {
            return "3d";
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
        List<NormalizedRecord> out = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            TopicParams.GdeltTopic topic = topics.get((next + i) % topics.size());
            Instant last = parseInstant(state.get(lastKey(topic.id())).orElse(null));
            String timespan = timespan(last, now);
            GdeltTimelineParser.Timeline volume = GdeltTimelineParser.parse(
                    get(ctx, uri("TimelineVolRaw", topic.query(), timespan)));
            GdeltTimelineParser.Timeline tone = GdeltTimelineParser.parse(
                    get(ctx, uri("TimelineTone", topic.query(), timespan)));
            List<NormalizedRecord> points = new ArrayList<>(GdeltTimelineParser.volume(topic.id(), volume, now));
            points.addAll(GdeltTimelineParser.tone(topic.id(), tone, GdeltTimelineParser.counts(volume), now));
            out.addAll(points);
            out.addAll(SeriesPoint.latestPerSeries(points,
                    p -> GdeltTimelineParser.meta(p, topic.id()).withEgress(DESCRIPTOR.egress())));
            Instant newest = volume.buckets().isEmpty() ? null
                    : volume.buckets().get(volume.buckets().size() - 1).start();
            if (newest != null && (last == null || newest.isAfter(last))) {
                state.put(lastKey(topic.id()), newest.toString());
            }
        }
        state.put(NEXT_KEY, Integer.toString((next + count) % topics.size()));
        ctx.log().debug("{}: {} record(s) from {} topic(s)", ID, out.size(), count);
        return FetchResult.of(out);
    }

    /**
     * One budgeted test call of a query (prompt 117: {@code AttentionTopicService} validates a new topic's GDELT
     * query with it): a single {@code TimelineVolRaw} request over one day.
     *
     * @return the parsed volume points of the pseudo topic {@code query-test}
     * @throws UpstreamFormatException when GDELT does not answer with a timeline (a malformed query)
     */
    public FetchResult testQuery(FetchContext ctx, String query) throws AdapterException {
        GdeltTimelineParser.Timeline volume = GdeltTimelineParser.parse(get(ctx, uri("TimelineVolRaw", query, "1d")));
        return FetchResult.of(GdeltTimelineParser.volume("query-test", volume, ctx.now()));
    }

    private byte[] get(FetchContext ctx, URI uri) throws AdapterException {
        try {
            spacing.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AdapterException(ID + ": interrupted while spacing requests");
        }
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
        SourceDescriptor.Builder builder = SeriesPoint.seriesDefaults(SourceDescriptor.builder(ID)
                        .name("GDELT topic timelines")
                        .description("News volume and tone per attention topic from the GDELT DOC 2.0 API: the"
                                + " matching article count per 15-minute step (with its share of all monitored"
                                + " articles) and the average tone. Topics come from /v1/attention/topics. GDELT"
                                + " allows one request every 5 seconds, so the adapter spaces its requests.")
                        .homepage("https://www.gdeltproject.org/")
                        .attribution(ATTRIBUTION)
                        .licence("GDELT open terms: free use with citation and a link to the GDELT Project")
                        .termsUrl("https://www.gdeltproject.org/about.html#termsofuse")
                        .mode(Mode.POLLING)
                        .egress(Egress.OPEN)
                        .param(new ParamSpec(TopicParams.PARAM, ParamType.STRING_LIST,
                                "Attention topics, synced from /v1/attention/topics: one JSON object"
                                        + " {\"id\", \"query\"} per entry", List.of(), false, List.of()))
                        .interval(Duration.ofMinutes(15), Duration.ofMinutes(30))
                        .defaultBudget(Budget.of(10, 1000))
                        .fetchTimeout(Duration.ofMinutes(3))
                        .observedAtMode(ObservedAtMode.RECEIVED))
                .kind(SeriesPoint.KIND)
                .kind(SeriesPoint.LATEST_KIND);
        SeriesPoint.declareFieldSpecs(builder);
        SeriesPoint.declareKindDisplay(builder);
        return builder.build();
    }
}
