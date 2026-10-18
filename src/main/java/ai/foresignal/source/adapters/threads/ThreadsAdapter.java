package ai.foresignal.source.adapters.threads;

import ai.foresignal.social.CueRequests;
import ai.foresignal.social.QuotaLedger;
import ai.foresignal.social.SocialCollector;
import ai.foresignal.social.SocialPlatformSettings;
import ai.foresignal.social.SocialPost;
import ai.foresignal.social.SocialRuntime;
import ai.foresignal.social.SocialVocabulary;
import ai.foresignal.social.StoredPosts;
import ai.foresignal.source.formats.social.SocialDescriptors;
import ai.foresignal.source.formats.social.SocialJson;
import ai.foresignal.source.formats.social.SocialQueries;
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
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code threads} (prompt 135): Threads keyword search ({@code GET https://graph.threads.net/v1.0/keyword_search} with
 * {@code q}, {@code search_type=RECENT}, {@code search_mode} {@code KEYWORD} or {@code TAG}, {@code since} (unix time),
 * {@code limit} ≤ 100 and {@code fields=id,text,media_type,permalink,timestamp,username,has_replies,is_quote_post,
 * is_reply}; developers.facebook.com/docs/threads/keyword-search, verified 2026-10-17). It needs the operator's token
 * with {@code threads_basic} and {@code threads_keyword_search}; without App Review it covers only the operator's own
 * posts. A paged search of the standing terms and hashtags; off by default.
 *
 * <p>Quota: 2,200 queries per rolling 24 hours per user; a query without results does not count
 * ({@link QuotaLedger}, unit {@code queries}). The answer has no author id ({@code owner} is not returned), so the
 * handle is the account id. Deletion sync (on by default): stored posts inside retention are re-read daily by id
 * ({@code GET /v1.0/{id}?fields=id}); one that is no longer there is tombstoned.
 */
@Component
public class ThreadsAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = SocialVocabulary.THREADS_ADAPTER;

    /** The API base. */
    public static final String API = "https://graph.threads.net/v1.0";

    /** Test-only override of the API base. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.threads.base-url";

    /** The queries per rolling 24 hours. */
    public static final int QUERIES_PER_DAY = 2_200;

    /** The queries per run. */
    public static final int QUERIES_PER_RUN = 20;

    /** The fields asked for. */
    static final String FIELDS = "id,text,media_type,permalink,timestamp,username,has_replies,is_quote_post,is_reply";

    /** The earliest {@code since} the API accepts. */
    static final long MIN_SINCE = 1_688_540_400L;

    private static final Duration DAY = Duration.ofDays(1);
    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String base;
    private final ObjectProvider<SocialRuntime> runtimes;
    private final SocialRuntime fixed;

    /** Creates the adapter. */
    @Autowired
    public ThreadsAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String override,
            ObjectProvider<SocialRuntime> runtimes) {
        this.base = override == null || override.isBlank() ? API : override.strip().replaceAll("/+$", "");
        this.runtimes = runtimes;
        this.fixed = null;
    }

    /** Creates the adapter with a fixed runtime (tests; null for descriptor checks). */
    public ThreadsAdapter(String override, SocialRuntime runtime) {
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

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        SocialRuntime runtime = runtime();
        String token = ctx.credentials() == null ? null : ctx.credentials().get("accessToken");
        if (runtime == null || token == null || token.isBlank()) {
            return FetchResult.of(List.of());
        }
        Map<String, NormalizedRecord> out = new LinkedHashMap<>();
        Instant now = ctx.now();
        QuotaLedger quotas = runtime.quotas();
        SocialCollector collector = runtime.collector();
        for (CueRequests.Request r : runtime.drainCues(ID)) {
            cue(ctx, runtime, token.strip(), r, out);
        }
        for (SocialQueries.Query q : SocialQueries.window(SocialQueries.forNetwork(runtime.index(),
                SocialVocabulary.THREADS), ctx.state(), QUERIES_PER_RUN)) {
            if (SocialQueries.ACCOUNT.equals(q.kind())) {
                continue;
            }
            if (quotas != null && quotas.used(ID, "queries", DAY) + 1 > QUERIES_PER_DAY) {
                ctx.log().info("{}: 2,200 queries in the last 24 hours; the run stops here", ID);
                break;
            }
            Instant since = parse(ctx.state().get(q.stateKey()).orElse(null));
            long sinceEpoch = Math.max(MIN_SINCE, (since == null ? now.minus(DAY) : since).getEpochSecond());
            URI uri = URI.create(base + "/keyword_search?q=" + enc(q.text()) + "&search_type=RECENT&search_mode="
                    + (SocialQueries.HASHTAG.equals(q.kind()) ? "TAG" : "KEYWORD") + "&fields=" + enc(FIELDS)
                    + "&since=" + sinceEpoch + "&limit=100&access_token=" + enc(token.strip()));
            JsonNode root = get(ctx, uri);
            JsonNode data = root.get("data");
            int n = data != null && data.isArray() ? data.size() : 0;
            if (n > 0 && quotas != null) {
                quotas.record(ID, "queries", null, 1);
            }
            if (data != null && data.isArray()) {
                for (JsonNode item : data) {
                    SocialPost post = post(item);
                    if (post != null) {
                        SocialCollector.Observation obs = collector.observe(post, ID, SocialVocabulary.PAGED);
                        if (obs.record() != null) {
                            out.put(obs.record().externalId(), obs.record());
                        }
                    }
                }
            }
            ctx.state().put(q.stateKey(), now.toString());
        }
        SocialPlatformSettings settings = runtime.settings(ID);
        if (settings.deletionSync()) {
            for (NormalizedRecord r : deletionSync(ctx, runtime, settings, token.strip())) {
                out.put(r.externalId(), r);
            }
        }
        return FetchResult.of(new ArrayList<>(out.values()));
    }

    /**
     * Runs one 137 cue query: {@code keyword_search} (RECENT, KEYWORD) with the exact query since the window start
     * within the rolling 2,200-query quota, posts after the window end dropped; posts are stored with
     * {@code collection = cue} and counted as sampled (never baselined).
     */
    void cue(FetchContext ctx, SocialRuntime runtime, String token, CueRequests.Request r,
            Map<String, NormalizedRecord> out) {
        int found = 0;
        String error = null;
        QuotaLedger quotas = runtime.quotas();
        try {
            if (quotas != null && quotas.used(ID, "queries", DAY) + 1 > QUERIES_PER_DAY) {
                error = "BUDGET_EXCEEDED: 2,200 queries in the last 24 hours";
            } else {
                long since = Math.max(MIN_SINCE, r.from().getEpochSecond());
                JsonNode data = get(ctx, URI.create(base + "/keyword_search?q=" + enc(r.query())
                        + "&search_type=RECENT&search_mode=KEYWORD&fields=" + enc(FIELDS) + "&since=" + since + "&limit="
                        + Math.max(1, Math.min(100, r.maxResults())) + "&access_token=" + enc(token))).get("data");
                if (quotas != null) {
                    quotas.record(ID, "queries", null, 1);
                }
                if (data != null && data.isArray()) {
                    for (JsonNode item : data) {
                        SocialPost post = post(item);
                        if (post == null || (post.createdAt() != null && r.to() != null
                                && post.createdAt().isAfter(r.to()))) {
                            continue;
                        }
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
        } catch (AdapterException | RuntimeException e) {
            error = e.getMessage() == null ? e.toString() : e.getMessage();
        }
        runtime.reportCue(new CueRequests.Result(r.cueId(), ID, r.query(), found, BigDecimal.ZERO, error));
    }

    /** The post of a keyword-search item. */
    static SocialPost post(JsonNode item) {
        String id = SocialJson.text(item, "id");
        if (id == null) {
            return null;
        }
        String username = SocialJson.text(item, "username");
        String mediaType = SocialJson.text(item, "media_type");
        String permalink = SocialJson.text(item, "permalink");
        List<SocialPost.Media> media = new ArrayList<>();
        if (permalink != null && ("IMAGE".equals(mediaType) || "VIDEO".equals(mediaType)
                || "CAROUSEL_ALBUM".equals(mediaType))) {
            media.add(new SocialPost.Media("VIDEO".equals(mediaType) ? "video" : "image", permalink, null, null));
        }
        return new SocialPost(SocialVocabulary.THREADS, id, permalink, new SocialPost.Author(username == null ? ""
                : username, null, username, null, null, null), SocialJson.text(item, "text"), null,
                SocialJson.instant(item, "timestamp"), null, null, null, List.of(), List.of(), List.of(), media, null,
                null, null, SocialVocabulary.COLLECTION_SEARCH, null);
    }

    /** Re-reads stored posts once a day; those the API no longer has are tombstoned. */
    List<NormalizedRecord> deletionSync(FetchContext ctx, SocialRuntime runtime, SocialPlatformSettings settings,
            String token) throws AdapterException {
        StoredPosts stored = runtime.stored();
        Instant now = ctx.now();
        Instant last = parse(ctx.state().get("del.last").orElse(null));
        if (stored == null || (last != null && last.isAfter(now.minus(DAY)))) {
            return List.of();
        }
        List<NormalizedRecord> out = new ArrayList<>();
        List<StoredPosts.Candidate> candidates = stored.deletionCandidates(ID, now.minus(settings.postRetention()),
                ctx.state().get("del.after").orElse(null), 200);
        for (StoredPosts.Candidate c : candidates) {
            UpstreamResponse r = ctx.http().get(URI.create(base + "/" + enc(c.postId()) + "?fields=id&access_token="
                    + enc(token)), Map.of("Accept", "application/json"));
            if (r.status() == 400 || r.status() == 404) {
                NormalizedRecord t = runtime.collector().observeDelete(ID, SocialVocabulary.THREADS, c.postId());
                if (t != null) {
                    out.add(t);
                }
            }
        }
        if (candidates.size() < 200) {
            ctx.state().remove("del.after");
            ctx.state().put("del.last", now.toString());
            runtime.deletionRan(ID, now);
        } else {
            ctx.state().put("del.after", candidates.get(candidates.size() - 1).externalId());
        }
        return out;
    }

    private static JsonNode get(FetchContext ctx, URI uri) throws AdapterException {
        UpstreamResponse r = ctx.http().get(uri, Map.of("Accept", "application/json"));
        if (r.status() != 200 || r.truncated()) {
            throw new UpstreamFormatException(ID + ": HTTP " + r.status() + " from keyword_search", "");
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
                .name("Threads keyword search")
                .description("Threads keyword_search (RECENT) for the attention topics' social terms and hashtags with"
                        + " the operator's token (threads_basic, threads_keyword_search); 2,200 queries per rolling 24"
                        + " hours. Off by default.")
                .homepage("https://developers.facebook.com/docs/threads/keyword-search")
                .attribution("Threads (Meta)")
                .licence("Meta Platform Terms")
                .termsUrl("https://developers.facebook.com/terms/")
                .mode(Mode.POLLING)
                .credential(CredentialField.required("accessToken",
                        "A Threads user access token with threads_basic and threads_keyword_search"))
                .interval(Duration.ofMinutes(10), Duration.ofMinutes(30))
                .defaultBudget(Budget.of(30, 2400))
                .fetchTimeout(Duration.ofMinutes(3));
        return SocialDescriptors.posts(b, SocialVocabulary.THREADS).build();
    }
}
