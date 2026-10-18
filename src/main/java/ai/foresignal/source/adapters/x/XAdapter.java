package ai.foresignal.source.adapters.x;

import ai.foresignal.collect.BudgetExceededException;
import ai.foresignal.social.CueRequests;
import ai.foresignal.social.SocialCollector;
import ai.foresignal.social.SocialPlatformSettings;
import ai.foresignal.social.SocialPost;
import ai.foresignal.social.SocialRuntime;
import ai.foresignal.social.SocialTopicBlock;
import ai.foresignal.social.SocialVocabulary;
import ai.foresignal.social.StoredPosts;
import ai.foresignal.social.TermIndex;
import ai.foresignal.social.XSpend;
import ai.foresignal.source.formats.social.SocialDescriptors;
import ai.foresignal.source.formats.social.SocialJson;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CredentialField;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code x} (prompt 135): the X API v2, pay-per-use, with the operator's bearer token (docs.x.com and the live
 * OpenAPI {@code api.x.com/2/openapi.json}, read 2026-10-17). Off by default.
 *
 * <ul>
 *   <li><b>Counts</b> (mode {@code counts} or {@code counts+search}, option {@code standingCounts}, default on):
 *       {@code GET /2/tweets/counts/recent} ({@code query}, {@code granularity=hour}, {@code start_time} within 7 days)
 *       once an hour per standing topic; each request costs {@code counts-recent-request} whatever it returns. The
 *       hourly {@code post_count} (or {@code tweet_count}) is the topic's complete X volume
 *       ({@code social_volume_hourly}, language bucket {@code other}). With option {@code fullArchive},
 *       {@code /2/tweets/counts/all} at {@code counts-all-request}.
 *   <li><b>Search</b> (mode {@code search} or {@code counts+search}): {@code GET /2/tweets/search/recent} with
 *       {@code max_results} = option {@code maxResults} (10–100, default 10), {@code since_id} per topic,
 *       {@code expansions=author_id,attachments.media_keys,geo.place_id} and the post and user fields; a sampled path
 *       billed per post returned.
 *   <li><b>Deletion sync</b> (on by default, the X Developer Policy asks for 24 hours): daily
 *       {@code GET /2/tweets?ids=} (100 per call); a post answered with {@code resource-not-found} is tombstoned.
 * </ul>
 *
 * <p><strong>Spend (SOC-4):</strong> before each call the most it can cost is estimated from
 * {@code social.x.prices} ({@link XSpend}); {@code spent + estimate > social.x.monthlyCapUsd} refuses it
 * ({@code BUDGET_EXCEEDED}, counted) and ends the run's paid calls; the actual cost is charged from the answer.
 *
 * <p>Field parameters: the live OpenAPI names them {@code post.fields} while the data dictionary still says
 * {@code tweet.fields}; option {@code fieldsParam} ({@code post} by default, or {@code tweet}) chooses, unverified
 * until one real call.
 */
@Component
public class XAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = SocialVocabulary.X_ADAPTER;

    /** The API base. */
    public static final String API = "https://api.x.com";

    /** Test-only override of the API base. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.x.base-url";

    /** The longest query. */
    public static final int MAX_QUERY = 4096;

    /** The default {@code max_results} of a search. */
    public static final int DEFAULT_MAX_RESULTS = 10;

    /** The post fields asked for. */
    static final String POST_FIELDS = "created_at,lang,author_id,public_metrics,entities,geo,conversation_id";

    /** The user fields asked for. */
    static final String USER_FIELDS = "username,name,created_at,public_metrics,verified";

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String base;
    private final ObjectProvider<SocialRuntime> runtimes;
    private final SocialRuntime fixed;

    /** Creates the adapter. */
    @Autowired
    public XAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String override, ObjectProvider<SocialRuntime> runtimes) {
        this.base = override == null || override.isBlank() ? API : override.strip().replaceAll("/+$", "");
        this.runtimes = runtimes;
        this.fixed = null;
    }

    /** Creates the adapter with a fixed runtime (tests; null for descriptor checks). */
    public XAdapter(String override, SocialRuntime runtime) {
        this.base = override == null || override.isBlank() ? API : override.strip().replaceAll("/+$", "");
        this.runtimes = null;
        this.fixed = runtime;
    }

    private SocialRuntime runtime() {
        return fixed != null ? fixed : runtimes == null ? null : runtimes.getIfAvailable();
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /**
     * A standing topic's query.
     *
     * @param topicId the topic
     * @param query the X search query
     */
    record TopicQuery(String topicId, String query) {}

    /** The X queries of the standing topics that apply to X: quoted terms, {@code #tags} and {@code from:} accounts. */
    static List<TopicQuery> queries(TermIndex index) {
        List<TopicQuery> out = new ArrayList<>();
        for (TermIndex.TopicEntry e : index.topics()) {
            SocialTopicBlock b = e.block();
            if (b.cueOnly() || (!b.platforms().isEmpty() && !b.platforms().contains(SocialVocabulary.X))) {
                continue;
            }
            Set<String> parts = new LinkedHashSet<>();
            for (String term : TermIndex.queryTerms(e)) {
                parts.add("\"" + term.replace("\"", "") + "\"");
            }
            for (String tag : b.hashtags()) {
                parts.add("#" + (tag.startsWith("#") ? tag.substring(1) : tag));
            }
            for (SocialTopicBlock.Account a : b.accounts()) {
                if (SocialVocabulary.X.equals(a.platform())) {
                    parts.add("from:" + (a.handle().startsWith("@") ? a.handle().substring(1) : a.handle()));
                }
            }
            StringBuilder q = new StringBuilder();
            for (String part : parts) {
                if (q.length() + part.length() + 6 > MAX_QUERY) {
                    break;
                }
                q.append(q.isEmpty() ? "" : " OR ").append(part);
            }
            if (!q.isEmpty()) {
                out.add(new TopicQuery(e.topicId(), q.length() == 0 ? "" : "(" + q + ")"));
            }
        }
        return out;
    }

    /** Thrown when the cap refuses a call; the run keeps what it has. */
    static final class Capped extends Exception {
        private static final long serialVersionUID = 1L;

        Capped(String message) {
            super(message);
        }
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        SocialRuntime runtime = runtime();
        String bearer = ctx.credentials() == null ? null : ctx.credentials().get("bearerToken");
        if (runtime == null) {
            return FetchResult.of(List.of());
        }
        Map<String, NormalizedRecord> cueOut = new LinkedHashMap<>();
        for (CueRequests.Request r : runtime.drainCues(ID)) {
            cue(ctx, runtime, bearer, r, cueOut);
        }
        if (bearer == null || bearer.isBlank()) {
            return FetchResult.of(new ArrayList<>(cueOut.values()));
        }
        SocialPlatformSettings settings = runtime.settings(ID);
        String mode = settings.mode() == null ? "counts" : settings.mode();
        boolean full = settings.option("fullArchive", false);
        Map<String, String> headers = Map.of("Authorization", "Bearer " + bearer.strip(), "Accept",
                "application/json");
        Map<String, NormalizedRecord> out = new LinkedHashMap<>(cueOut);
        Instant now = ctx.now();
        try {
            for (TopicQuery q : queries(runtime.index())) {
                if (mode.contains("counts") && settings.option("standingCounts", true)) {
                    counts(ctx, runtime, settings, headers, q, full, now);
                }
                if (mode.contains("search")) {
                    search(ctx, runtime, settings, headers, q, full, out);
                }
            }
            if (settings.deletionSync()) {
                for (NormalizedRecord r : deletionSync(ctx, runtime, settings, headers)) {
                    out.put(r.externalId(), r);
                }
            }
        } catch (Capped e) {
            ctx.log().info("{}: {}", ID, e.getMessage());
            ctx.state().putDurably("cap.refusedAt", now.toString());
        }
        return FetchResult.of(new ArrayList<>(out.values()));
    }

    /** One hourly counts request for a topic. */
    void counts(FetchContext ctx, SocialRuntime runtime, SocialPlatformSettings settings, Map<String, String> headers,
            TopicQuery q, boolean full, Instant now) throws AdapterException, Capped {
        String key = "counts." + q.topicId();
        Instant last = parse(ctx.state().get(key).orElse(null));
        Instant hour = now.truncatedTo(ChronoUnit.HOURS);
        if (last != null && !last.isBefore(hour)) {
            return;
        }
        Instant start = last == null ? hour.minus(Duration.ofHours(24)) : last.minus(Duration.ofHours(1));
        Instant floor = now.minus(Duration.ofDays(7)).plus(Duration.ofMinutes(5)).truncatedTo(ChronoUnit.HOURS)
                .plus(Duration.ofHours(1));
        if (!full && start.isBefore(floor)) {
            start = floor;
        }
        BigDecimal estimate = XSpend.estimateCounts(settings, full);
        reserve(runtime, settings, estimate);
        URI uri = URI.create(base + "/2/tweets/counts/" + (full ? "all" : "recent") + "?query=" + enc(q.query())
                + "&granularity=hour&start_time=" + enc(start.toString()) + "&end_time=" + enc(hour.toString()));
        UpstreamResponse r = ctx.http().get(uri, headers);
        charge(runtime, estimate);
        JsonNode root = ok(r, uri);
        JsonNode data = root.get("data");
        if (data != null && data.isArray() && runtime.volume() != null) {
            for (JsonNode bucket : data) {
                Instant bStart = SocialJson.instant(bucket, "start");
                Instant bEnd = SocialJson.instant(bucket, "end");
                Long count = SocialJson.number(bucket, "post_count");
                if (count == null) {
                    count = SocialJson.number(bucket, "tweet_count");
                }
                if (bStart != null && bEnd != null && count != null && !bEnd.isAfter(now)) {
                    runtime.collector().reportCount(runtime.volume(), SocialVocabulary.scopeKey(
                            SocialVocabulary.SCOPE_TOPIC, q.topicId()), SocialVocabulary.X,
                            bStart.truncatedTo(ChronoUnit.HOURS), count);
                }
            }
        }
        ctx.state().put(key, hour.toString());
    }

    /** One recent-search request for a topic (a sample). */
    void search(FetchContext ctx, SocialRuntime runtime, SocialPlatformSettings settings, Map<String, String> headers,
            TopicQuery q, boolean full, Map<String, NormalizedRecord> out) throws AdapterException, Capped {
        int max = maxResults(settings);
        BigDecimal estimate = XSpend.estimateSearch(settings, max);
        reserve(runtime, settings, estimate);
        String sinceKey = "since." + q.topicId();
        String since = ctx.state().get(sinceKey).orElse(null);
        URI uri = URI.create(base + "/2/tweets/search/" + (full ? "all" : "recent") + "?query=" + enc(q.query())
                + "&max_results=" + max + (since == null ? "" : "&since_id=" + enc(since)) + "&" + fieldsParam(settings)
                + "=" + enc(POST_FIELDS) + "&expansions=" + enc("author_id,attachments.media_keys,geo.place_id")
                + "&user.fields=" + enc(USER_FIELDS));
        UpstreamResponse r = ctx.http().get(uri, headers);
        JsonNode root;
        try {
            root = ok(r, uri);
        } catch (AdapterException e) {
            charge(runtime, BigDecimal.ZERO);
            throw e;
        }
        Long count = SocialJson.number(SocialJson.at(root, "meta"), "result_count");
        int returned = root.get("data") != null && root.get("data").isArray() ? root.get("data").size() : 0;
        charge(runtime, XSpend.estimateSearch(settings, count == null ? returned : count.intValue()));
        SocialCollector collector = runtime.collector();
        for (SocialPost post : XPosts.posts(root, SocialVocabulary.COLLECTION_SAMPLE)) {
            SocialCollector.Observation obs = collector.observe(post, ID, SocialVocabulary.SAMPLED);
            if (obs.record() != null) {
                out.put(obs.record().externalId(), obs.record());
            }
        }
        String newest = SocialJson.text(SocialJson.at(root, "meta"), "newest_id");
        if (newest != null) {
            ctx.state().put(sinceKey, newest);
        }
    }

    /**
     * Runs one 137 cue request. Posts another backend already found ({@code xai-x-search}) are stored as they are.
     * Otherwise a recent search with the exact query over the window (clipped to the recent-search week): the call is
     * estimated first and refused when it would exceed the request's cost cap or the monthly cap (SOC-4), then charged
     * per post returned. Posts are stored with {@code collection = cue} and counted as sampled (never baselined).
     */
    void cue(FetchContext ctx, SocialRuntime runtime, String bearer, CueRequests.Request r,
            Map<String, NormalizedRecord> out) {
        int found = 0;
        BigDecimal cost = BigDecimal.ZERO;
        String error = null;
        try {
            if (!r.posts().isEmpty()) {
                for (SocialPost post : r.posts()) {
                    SocialCollector.Observation obs = runtime.collector().observe(
                            post.withCollection(SocialVocabulary.COLLECTION_CUE, r.cueId()), ID, SocialVocabulary.SAMPLED);
                    if (obs.record() != null) {
                        out.put(obs.record().externalId(), obs.record());
                    }
                    found++;
                }
            } else if (bearer == null || bearer.isBlank()) {
                error = "PLATFORM_DISABLED: no bearer token";
            } else {
                SocialPlatformSettings settings = runtime.settings(ID);
                int max = Math.max(10, Math.min(100, r.maxResults()));
                BigDecimal estimate = XSpend.estimateSearch(settings, max);
                if (r.maxCostUsd() != null && estimate.compareTo(r.maxCostUsd()) > 0) {
                    error = "BUDGET_EXCEEDED: estimate " + estimate + " USD over the cue's cap " + r.maxCostUsd();
                } else {
                    reserve(runtime, settings, estimate);
                    Instant now = ctx.now();
                    Instant floor = now.minus(Duration.ofDays(7)).plus(Duration.ofMinutes(5));
                    Instant start = r.from().isBefore(floor) ? floor : r.from();
                    Instant end = r.to() == null || r.to().isAfter(now.minusSeconds(30)) ? null : r.to();
                    URI uri = URI.create(base + "/2/tweets/search/recent?query=" + enc(r.query()) + "&max_results=" + max
                            + "&start_time=" + enc(start.toString()) + (end == null || !end.isAfter(start) ? ""
                            : "&end_time=" + enc(end.toString())) + "&" + fieldsParam(settings) + "=" + enc(POST_FIELDS)
                            + "&expansions=" + enc("author_id,attachments.media_keys,geo.place_id") + "&user.fields="
                            + enc(USER_FIELDS));
                    Map<String, String> headers = Map.of("Authorization", "Bearer " + bearer.strip(), "Accept",
                            "application/json");
                    UpstreamResponse resp = ctx.http().get(uri, headers);
                    JsonNode root;
                    try {
                        root = ok(resp, uri);
                    } catch (AdapterException e) {
                        charge(runtime, BigDecimal.ZERO);
                        throw e;
                    }
                    Long count = SocialJson.number(SocialJson.at(root, "meta"), "result_count");
                    int returned = root.get("data") != null && root.get("data").isArray() ? root.get("data").size() : 0;
                    cost = XSpend.estimateSearch(settings, count == null ? returned : count.intValue());
                    charge(runtime, cost);
                    for (SocialPost post : XPosts.posts(root, SocialVocabulary.COLLECTION_CUE)) {
                        SocialCollector.Observation obs = runtime.collector().observe(
                                post.withCollection(SocialVocabulary.COLLECTION_CUE, r.cueId()), ID,
                                SocialVocabulary.SAMPLED);
                        if (obs.record() != null) {
                            out.put(obs.record().externalId(), obs.record());
                        }
                        found++;
                    }
                }
            }
        } catch (Capped e) {
            error = "BUDGET_EXCEEDED: " + e.getMessage();
        } catch (AdapterException | RuntimeException e) {
            error = e.getMessage() == null ? e.toString() : e.getMessage();
        }
        runtime.reportCue(new CueRequests.Result(r.cueId(), ID, r.query(), found, cost, error));
    }

    /** Daily lookup of stored posts; those X answers as not found are tombstoned. */
    List<NormalizedRecord> deletionSync(FetchContext ctx, SocialRuntime runtime, SocialPlatformSettings settings,
            Map<String, String> headers) throws AdapterException, Capped {
        StoredPosts stored = runtime.stored();
        Instant now = ctx.now();
        Instant last = parse(ctx.state().get("del.last").orElse(null));
        if (stored == null || (last != null && last.isAfter(now.minus(Duration.ofDays(1))))) {
            return List.of();
        }
        List<StoredPosts.Candidate> candidates = stored.deletionCandidates(ID, now.minus(settings.postRetention()),
                ctx.state().get("del.after").orElse(null), 500);
        List<NormalizedRecord> out = new ArrayList<>();
        for (int i = 0; i < candidates.size(); i += 100) {
            List<StoredPosts.Candidate> batch = candidates.subList(i, Math.min(candidates.size(), i + 100));
            BigDecimal estimate = XSpend.estimateLookup(settings, batch.size());
            reserve(runtime, settings, estimate);
            StringBuilder ids = new StringBuilder();
            for (StoredPosts.Candidate c : batch) {
                ids.append(ids.isEmpty() ? "" : ",").append(c.postId());
            }
            URI uri = URI.create(base + "/2/tweets?ids=" + enc(ids.toString()));
            JsonNode root = ok(ctx.http().get(uri, headers), uri);
            int returned = root.get("data") != null && root.get("data").isArray() ? root.get("data").size() : 0;
            charge(runtime, XSpend.estimateLookup(settings, returned));
            Set<String> gone = new HashSet<>();
            JsonNode errors = root.get("errors");
            if (errors != null && errors.isArray()) {
                for (JsonNode e : errors) {
                    String type = SocialJson.text(e, "type");
                    String value = SocialJson.text(e, "value") != null ? SocialJson.text(e, "value")
                            : SocialJson.text(e, "resource_id");
                    if (type != null && type.endsWith("resource-not-found") && value != null) {
                        gone.add(value);
                    }
                }
            }
            for (StoredPosts.Candidate c : batch) {
                if (gone.contains(c.postId())) {
                    NormalizedRecord r = runtime.collector().observeDelete(ID, SocialVocabulary.X, c.postId());
                    if (r != null) {
                        out.add(r);
                    }
                }
            }
        }
        if (candidates.size() < 500) {
            ctx.state().remove("del.after");
            ctx.state().put("del.last", now.toString());
            runtime.deletionRan(ID, now);
        } else {
            ctx.state().put("del.after", candidates.get(candidates.size() - 1).externalId());
        }
        return out;
    }

    private static void reserve(SocialRuntime runtime, SocialPlatformSettings settings, BigDecimal estimate)
            throws Capped {
        XSpend spend = runtime.spend();
        if (spend == null) {
            return;
        }
        try {
            spend.reserve(ID, settings, estimate);
        } catch (BudgetExceededException e) {
            throw new Capped(e.getMessage());
        }
    }

    private static void charge(SocialRuntime runtime, BigDecimal actual) {
        if (runtime.spend() != null) {
            runtime.spend().charge(ID, actual);
        }
    }

    static int maxResults(SocialPlatformSettings settings) {
        Object v = settings.options().get("maxResults");
        int n = DEFAULT_MAX_RESULTS;
        if (v instanceof Number num) {
            n = num.intValue();
        } else if (v != null) {
            try {
                n = Integer.parseInt(v.toString());
            } catch (NumberFormatException e) {
                n = DEFAULT_MAX_RESULTS;
            }
        }
        return Math.max(10, Math.min(100, n));
    }

    static String fieldsParam(SocialPlatformSettings settings) {
        Object v = settings.options().get("fieldsParam");
        return "tweet".equals(v) ? "tweet.fields" : "post.fields";
    }

    private static JsonNode ok(UpstreamResponse r, URI uri) throws AdapterException {
        if (r.status() != 200 || r.truncated()) {
            throw new UpstreamFormatException(ID + ": HTTP " + r.status() + " from " + uri.getPath(), "");
        }
        return JSON.readTree(r.body());
    }

    private static Instant parse(String s) {
        try {
            return s == null ? null : Instant.parse(s);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("X")
                .description("X API v2 pay-per-use with the operator's bearer token: hourly counts for standing topics"
                        + " (billed per request), recent search for sampled standing topics (billed per post), daily"
                        + " deletion checks. Every call is estimated first and refused at social.x.monthlyCapUsd. Off"
                        + " by default.")
                .homepage("https://docs.x.com/x-api")
                .attribution("X")
                .licence("X Developer Agreement and Policy")
                .termsUrl("https://docs.x.com/developer-terms/policy")
                .mode(Mode.POLLING)
                .credential(CredentialField.required("bearerToken", "An X API v2 bearer token (developer console)"))
                .interval(Duration.ofMinutes(15), Duration.ofMinutes(60))
                .defaultBudget(Budget.of(30, 2000))
                .fetchTimeout(Duration.ofMinutes(3));
        return SocialDescriptors.posts(b, SocialVocabulary.X).build();
    }
}
