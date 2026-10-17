package ai.foresignal.source.adapters.wikimedia;

import ai.foresignal.collect.UpstreamHttpException;
import ai.foresignal.source.formats.attention.TopicParams;
import ai.foresignal.source.series.SeriesPoint;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.KindDisplay;
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
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code wikimedia-pageviews} (prompt 117): daily Wikipedia pageviews of the attention topics' articles, as
 * {@code series-point} and {@code series-latest} records, plus each project's daily top articles.
 *
 * <p>Pageviews for day D are published on D+1, so the source works once a UTC day, at or after
 * {@value #RUN_AFTER_UTC} UTC: the scheduler polls hourly and a run before that time, or after the day's work is
 * done, sends nothing. For each article of the {@code topics} parameter (synced from the enabled attention topics by
 * {@code AttentionTopicService}) it reads
 * {@code /metrics/pageviews/per-article/{project}/all-access/user/{article}/daily/{start}/{yesterday}}, with
 * {@code start} = the last stored day − 3 days (the first run: {@value #FIRST_RUN_DAYS} days), {@code agent=user}.
 * Requests are serial. An article with no data in the range answers 404 and is skipped.
 *
 * <p>For each project any topic uses, {@code /metrics/pageviews/top/{project}/all-access/{yyyy}/{mm}/{dd}} for
 * yesterday becomes one {@code attention-top} record ({@code externalId = {project}:{date}}, the top 100
 * {@code {article, views, rank}}; analytic class {@code bulk-event}, 90 days).
 *
 * <p>The User-Agent is the standard Foresignal one, which carries the project URL as contact and, when the operator
 * set {@code server.contactEmail} ({@code PUT /v1/settings/server}), {@code ; contact={value}}, as Wikimedia's
 * User-Agent policy asks. A 403 marks the source failing with a reason that names the User-Agent policy. License
 * CC0.
 */
@Component
public class WikimediaPageviewsAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "wikimedia-pageviews";

    /** The production upstream. */
    public static final String DEFAULT_BASE_URL = "https://wikimedia.org";

    /** Test-only override. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.wikimedia.base-url";

    /** The time of day (UTC) after which yesterday's pageviews are read. */
    public static final String RUN_AFTER_UTC = "02:30";

    /** The first run's window, in days. */
    public static final int FIRST_RUN_DAYS = 90;

    /** The days re-read before the last stored day. */
    public static final int REREAD_DAYS = 3;

    /** The adapter-state key of the last UTC day whose work is done. */
    static final String DONE_KEY = "done";

    /** The failing reason of a 403. */
    static final String UA_REFUSED = "Wikimedia refused the request (HTTP 403); its User-Agent policy"
            + " (https://meta.wikimedia.org/wiki/User-Agent_policy) asks for contact details: set server.contactEmail"
            + " with PUT /v1/settings/server";

    private static final LocalTime RUN_AFTER = LocalTime.parse(RUN_AFTER_UTC);

    private static final DateTimeFormatter COMPACT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String baseUrl;

    /** Creates the adapter. */
    public WikimediaPageviewsAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.baseUrl = baseUrlOverride == null || baseUrlOverride.isBlank()
                ? DEFAULT_BASE_URL : baseUrlOverride.trim().replaceAll("/+$", "");
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        for (String entry : params.stringList(TopicParams.PARAM)) {
            String problem = TopicParams.wikiProblem(entry);
            if (problem != null) {
                problems.put(TopicParams.PARAM, problem);
                break;
            }
        }
        return problems;
    }

    /** The per-article request. */
    URI perArticleUri(String project, String article, LocalDate start, LocalDate end) {
        return URI.create(baseUrl + "/api/rest_v1/metrics/pageviews/per-article/" + project + "/all-access/user/"
                + PageviewsParser.encodeArticle(article) + "/daily/" + COMPACT.format(start) + "/"
                + COMPACT.format(end));
    }

    /** The top-articles request of one day. */
    URI topUri(String project, LocalDate day) {
        return URI.create(baseUrl + "/api/rest_v1/metrics/pageviews/top/" + project + "/all-access/"
                + String.format(Locale.ROOT, "%04d/%02d/%02d", day.getYear(), day.getMonthValue(),
                        day.getDayOfMonth()));
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        List<TopicParams.WikiTopic> topics = TopicParams.wiki(ctx.params() == null ? List.of()
                : ctx.params().stringList(TopicParams.PARAM));
        AdapterStateStore state = ctx.state();
        Instant now = ctx.now();
        ZonedDateTime utc = now.atZone(ZoneOffset.UTC);
        LocalDate today = utc.toLocalDate();
        if (topics.isEmpty() || utc.toLocalTime().isBefore(RUN_AFTER)
                || today.toString().equals(state.get(DONE_KEY).orElse(null))) {
            return FetchResult.of(List.of());
        }
        LocalDate yesterday = today.minusDays(1);
        List<NormalizedRecord> out = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        Set<String> projects = new LinkedHashSet<>();
        try {
            for (TopicParams.WikiTopic topic : topics) {
                for (TopicParams.WikiArticle article : topic.articles()) {
                    projects.add(article.project());
                    String seriesId = PageviewsParser.seriesId(article.project(), article.article());
                    if (!seen.add(seriesId)) {
                        continue;
                    }
                    LocalDate last = parseDate(state.get(lastKey(seriesId)).orElse(null));
                    LocalDate start = last == null ? yesterday.minusDays(FIRST_RUN_DAYS - 1L)
                            : last.minusDays(REREAD_DAYS);
                    if (start.isAfter(yesterday)) {
                        start = yesterday;
                    }
                    byte[] body = get(ctx, perArticleUri(article.project(), article.article(), start, yesterday));
                    if (body == null) {
                        continue;
                    }
                    List<NormalizedRecord> points = PageviewsParser.perArticle(article.project(), article.article(),
                            body, now);
                    out.addAll(points);
                    out.addAll(SeriesPoint.latestPerSeries(points,
                            p -> PageviewsParser.meta(topic.id()).withEgress(DESCRIPTOR.egress())));
                    LocalDate newest = newest(points);
                    if (newest != null && (last == null || newest.isAfter(last))) {
                        state.put(lastKey(seriesId), newest.toString());
                    }
                }
            }
            for (String project : projects) {
                byte[] body = get(ctx, topUri(project, yesterday));
                NormalizedRecord top = body == null ? null : PageviewsParser.top(project, yesterday, body, now);
                if (top != null) {
                    out.add(top);
                }
            }
        } catch (UpstreamHttpException e) {
            if (e.getStatus() == 403) {
                state.fail(UA_REFUSED);
                throw new AdapterException(ID + ": " + UA_REFUSED, e);
            }
            throw e;
        }
        state.clearFailing();
        state.put(DONE_KEY, today.toString());
        ctx.log().debug("{}: {} record(s)", ID, out.size());
        return FetchResult.of(out);
    }

    /** The body of a 200, or null for a 404 (no data in the range). */
    private static byte[] get(FetchContext ctx, URI uri) throws AdapterException {
        UpstreamResponse response;
        try {
            response = ctx.http().get(uri, Map.of("Accept", "application/json"));
        } catch (UpstreamHttpException e) {
            if (e.getStatus() == 404) {
                return null;
            }
            throw e;
        }
        if (response.status() == 404) {
            return null;
        }
        if (response.status() != 200 || response.truncated()) {
            throw new UpstreamFormatException(ID + ": Wikimedia" + (response.status() != 200
                    ? " answered HTTP " + response.status() : " response exceeds the cap"), "");
        }
        return response.body();
    }

    private static LocalDate newest(List<NormalizedRecord> points) {
        LocalDate newest = null;
        for (NormalizedRecord point : points) {
            LocalDate day = parseDate(String.valueOf(point.payload().get("period")));
            if (day != null && (newest == null || day.isAfter(newest))) {
                newest = day;
            }
        }
        return newest;
    }

    static String lastKey(String seriesId) {
        return "last." + seriesId;
    }

    private static LocalDate parseDate(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(text.strip());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder builder = SeriesPoint.seriesDefaults(SourceDescriptor.builder(ID)
                        .name("Wikipedia pageviews")
                        .description("Daily Wikipedia pageviews (human readers, all access methods) of the attention"
                                + " topics' articles, plus each project's daily top 100 articles. Day D is published"
                                + " on D+1; the source reads yesterday once a day after 02:30 UTC. Topics come from"
                                + " /v1/attention/topics.")
                        .homepage("https://wikimedia.org/api/rest_v1/")
                        .attribution("Wikimedia Foundation, Pageviews API")
                        .licence("CC0 1.0")
                        .termsUrl("https://meta.wikimedia.org/wiki/User-Agent_policy")
                        .mode(Mode.POLLING)
                        .egress(Egress.OPEN)
                        .param(new ParamSpec(TopicParams.PARAM, ParamType.STRING_LIST,
                                "Attention topics, synced from /v1/attention/topics: one JSON object {\"id\","
                                        + " \"articles\": [{\"project\", \"article\"}]} per entry", List.of(), false,
                                List.of()))
                        .interval(Duration.ofMinutes(30), Duration.ofHours(1))
                        .defaultBudget(Budget.of(60, 2000))
                        .fetchTimeout(Duration.ofMinutes(5))
                        .observedAtMode(ObservedAtMode.RECEIVED)
                        .kindRetention(PageviewsParser.TOP_KIND, Duration.ofDays(90)))
                .kind(SeriesPoint.KIND)
                .kind(SeriesPoint.LATEST_KIND)
                .kind(PageviewsParser.TOP_KIND);
        SeriesPoint.declareFieldSpecs(builder);
        SeriesPoint.declareKindDisplay(builder);
        builder.fieldSpec("project", new FieldSpec(FieldType.STRING, null, "Project",
                "The Wikipedia project, for example en.wikipedia", null, Set.of(PageviewsParser.TOP_KIND)));
        builder.fieldSpec("date", new FieldSpec(FieldType.STRING, null, "Date",
                "The UTC day the top list covers", null, Set.of(PageviewsParser.TOP_KIND)));
        builder.fieldSpec("title", new FieldSpec(FieldType.STRING, null, "Title",
                "Top {project} articles {date}", null, Set.of(PageviewsParser.TOP_KIND)));
        builder.fieldSpec("articles", new FieldSpec(FieldType.ARRAY, null, "Articles",
                "The top 100 articles: {article, views, rank}", null, Set.of(PageviewsParser.TOP_KIND)));
        builder.kindDisplay(PageviewsParser.TOP_KIND, new KindDisplay("{title}", null, "#1971C2", null));
        return builder.build();
    }
}
