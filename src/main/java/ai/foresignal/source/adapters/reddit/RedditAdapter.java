package ai.foresignal.source.adapters.reddit;

import ai.foresignal.social.CueRequests;
import ai.foresignal.social.QuotaLedger;
import ai.foresignal.social.SocialCollector;
import ai.foresignal.social.SocialPlatformSettings;
import ai.foresignal.social.SocialPost;
import ai.foresignal.social.SocialRuntime;
import ai.foresignal.social.SocialTopicBlock;
import ai.foresignal.social.SocialVocabulary;
import ai.foresignal.social.StoredPosts;
import ai.foresignal.social.TermIndex;
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
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code reddit} (prompt 135): the Reddit Data API with the operator's own OAuth app. A token comes from
 * {@code POST https://www.reddit.com/api/v1/access_token} with HTTP Basic {@code clientId:clientSecret} and
 * {@code grant_type=client_credentials} (application-only OAuth); calls go to {@code https://oauth.reddit.com}
 * ({@code [/r/{sub}]/new} with {@code limit} ≤ 100, {@code after}; {@code /search} with {@code q}, {@code sort=new},
 * {@code t=day}, {@code limit}; verified on reddit.com/dev/api and the OAuth2 wiki 2026-10-17). Off by default.
 *
 * <p>Paged collection: each channel (the platform's channels and the topics' {@code subreddit} channels) and each
 * standing term is read back to its last run. Every call takes one unit of the shared
 * {@value #QUERIES_PER_MINUTE}-a-minute quota ({@link QuotaLedger}); a run stops early when the quota or the
 * {@code X-Ratelimit-Remaining} header says so. The User-Agent is Foresignal's own with the operator's contact
 * ({@code contactEmail}, for example {@code /u/name}), as Reddit asks for a descriptive one.
 *
 * <p>The listing field names ({@code data.children[].data}: {@code name}, {@code subreddit}, {@code author},
 * {@code author_fullname}, {@code title}, {@code selftext}, {@code created_utc}, {@code permalink}, {@code url},
 * {@code num_comments}, {@code score}) are not in Reddit's published reference; the fixture is synthetic until
 * recaptured with a real app. Deletion sync (off by default) tombstones stored posts that {@code /api/info} no longer
 * returns.
 */
@Component
public class RedditAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = SocialVocabulary.REDDIT_ADAPTER;

    /** The token endpoint. */
    public static final String TOKEN_URL = "https://www.reddit.com/api/v1/access_token";

    /** The API host. */
    public static final String API = "https://oauth.reddit.com";

    /** Test-only override of both hosts. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.reddit.base-url";

    /** The shared query rate. */
    public static final int QUERIES_PER_MINUTE = 100;

    /** The most channels per run. */
    public static final int CHANNELS_PER_RUN = 20;

    /** The most standing queries per run. */
    public static final int QUERIES_PER_RUN = 20;

    /** The most pages per channel. */
    public static final int MAX_PAGES = 3;

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String override;
    private final ObjectProvider<SocialRuntime> runtimes;
    private final SocialRuntime fixed;
    private volatile String token;
    private volatile Instant tokenExpires;

    /** Creates the adapter. */
    @Autowired
    public RedditAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String override,
            ObjectProvider<SocialRuntime> runtimes) {
        this.override = override == null || override.isBlank() ? null : override.strip().replaceAll("/+$", "");
        this.runtimes = runtimes;
        this.fixed = null;
    }

    /** Creates the adapter with a fixed runtime (tests; null for descriptor checks). */
    public RedditAdapter(String override, SocialRuntime runtime) {
        this.override = override == null || override.isBlank() ? null : override.strip().replaceAll("/+$", "");
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
    public Map<String, String> checkCredentials(Map<String, String> values) {
        Map<String, String> problems = new LinkedHashMap<>();
        boolean id = values.get("clientId") != null && !values.get("clientId").isBlank();
        boolean secret = values.get("clientSecret") != null && !values.get("clientSecret").isBlank();
        if (id != secret) {
            problems.put(id ? "clientSecret" : "clientId", "clientId and clientSecret go together");
        }
        return problems;
    }

    /** Thrown inside a run when the query rate is spent; the run returns what it has. */
    static final class RateSpent extends Exception {
        private static final long serialVersionUID = 1L;
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        SocialRuntime runtime = runtime();
        if (runtime == null) {
            return FetchResult.of(List.of());
        }
        SocialPlatformSettings settings = runtime.settings(ID);
        Map<String, String> headers = Map.of("Authorization", "Bearer " + token(ctx), "Accept", "application/json");
        String api = override != null ? override : API;
        Map<String, NormalizedRecord> out = new LinkedHashMap<>();
        Instant now = ctx.now();
        for (CueRequests.Request r : runtime.drainCues(ID)) {
            cue(ctx, runtime, api, headers, r, out);
        }
        try {
            List<String> channels = channels(runtime.index(), settings);
            for (String sub : SocialQueries.window(channels.stream().map(c -> new SocialQueries.Query("channel", c,
                    List.of())).toList(), new Prefixed(ctx, "ch."), CHANNELS_PER_RUN).stream()
                    .map(SocialQueries.Query::text).toList()) {
                String key = "sub." + sub.toLowerCase(Locale.ROOT);
                double since = since(ctx, key, now);
                double newest = since;
                String after = null;
                for (int page = 0; page < MAX_PAGES; page++) {
                    JsonNode listing = get(ctx, runtime, URI.create(api + "/r/" + enc(sub) + "/new?limit=100"
                            + (after == null ? "" : "&after=" + enc(after))), headers);
                    Page p = read(ctx, runtime, listing, since, SocialVocabulary.COLLECTION_SEARCH, out);
                    newest = Math.max(newest, p.newest());
                    after = SocialJson.text(SocialJson.at(listing, "data"), "after");
                    if (after == null || p.reachedOld()) {
                        break;
                    }
                }
                ctx.state().put(key, Double.toString(newest));
            }
            for (SocialQueries.Query q : SocialQueries.window(SocialQueries.forNetwork(runtime.index(),
                    SocialVocabulary.REDDIT), ctx.state(), QUERIES_PER_RUN)) {
                if (SocialQueries.ACCOUNT.equals(q.kind())) {
                    continue;
                }
                String text = SocialQueries.HASHTAG.equals(q.kind()) ? q.text() : "\"" + q.text().replace("\"", "")
                        + "\"";
                double since = since(ctx, q.stateKey(), now);
                JsonNode listing = get(ctx, runtime, URI.create(api + "/search?q=" + enc(text)
                        + "&sort=new&t=day&limit=100"), headers);
                Page p = read(ctx, runtime, listing, since, SocialVocabulary.COLLECTION_SEARCH, out);
                ctx.state().put(q.stateKey(), Double.toString(Math.max(since, p.newest())));
            }
            if (settings.deletionSync()) {
                for (NormalizedRecord r : deletionSync(ctx, runtime, api, headers, settings)) {
                    out.put(r.externalId(), r);
                }
            }
        } catch (RateSpent e) {
            ctx.log().info("{}: the query rate is spent for this minute; the run stops here", ID);
        }
        return FetchResult.of(new ArrayList<>(out.values()));
    }

    /**
     * Runs one 137 cue query: {@code /search} with the exact query, newest first over the past week, posts outside the
     * window dropped; posts are stored with {@code collection = cue} and counted as sampled (never baselined).
     */
    void cue(FetchContext ctx, SocialRuntime runtime, String api, Map<String, String> headers, CueRequests.Request r,
            Map<String, NormalizedRecord> out) {
        int found = 0;
        String error = null;
        try {
            JsonNode listing = get(ctx, runtime, URI.create(api + "/search?q=" + enc(r.query()) + "&sort=new&t=week&limit="
                    + Math.max(1, Math.min(100, r.maxResults()))), headers);
            JsonNode children = SocialJson.at(listing, "data", "children");
            if (children != null && children.isArray()) {
                for (JsonNode child : children) {
                    SocialPost post = post(child.get("data"), SocialVocabulary.COLLECTION_CUE);
                    if (post == null || post.createdAt() == null || post.createdAt().isBefore(r.from())
                            || (r.to() != null && post.createdAt().isAfter(r.to()))) {
                        continue;
                    }
                    SocialCollector.Observation obs = runtime.collector().observe(
                            post.withCollection(SocialVocabulary.COLLECTION_CUE, r.cueId()), ID, SocialVocabulary.SAMPLED);
                    if (obs.record() != null) {
                        out.put(obs.record().externalId(), obs.record());
                    }
                    found++;
                }
            }
        } catch (RateSpent e) {
            error = "RATE_LIMITED";
        } catch (AdapterException | RuntimeException e) {
            error = e.getMessage() == null ? e.toString() : e.getMessage();
        }
        runtime.reportCue(new CueRequests.Result(r.cueId(), ID, r.query(), found, BigDecimal.ZERO, error));
    }

    /** The subreddits polled: the platform channels and the topics' subreddit channels. */
    static List<String> channels(TermIndex index, SocialPlatformSettings settings) {
        Set<String> out = new LinkedHashSet<>();
        for (String c : settings.channels()) {
            out.add(bare(c));
        }
        for (TermIndex.TopicEntry e : index.topics()) {
            if (e.block().cueOnly()) {
                continue;
            }
            for (SocialTopicBlock.Channel c : e.block().channels()) {
                if ("subreddit".equals(c.type()) && SocialVocabulary.REDDIT.equals(c.platform())) {
                    out.add(bare(c.id()));
                }
            }
        }
        out.removeIf(s -> s.isBlank() || !s.matches("^[A-Za-z0-9_]{2,32}$"));
        return List.copyOf(out);
    }

    private static String bare(String sub) {
        String s = sub.strip();
        if (s.startsWith("/r/")) {
            s = s.substring(3);
        } else if (s.startsWith("r/")) {
            s = s.substring(2);
        }
        return s.replaceAll("/+$", "");
    }

    private static double since(FetchContext ctx, String key, Instant now) {
        try {
            return Double.parseDouble(ctx.state().get(key).orElse(""));
        } catch (NumberFormatException e) {
            return now.minus(Duration.ofHours(1)).getEpochSecond();
        }
    }

    /**
     * What a listing page gave.
     *
     * @param newest the newest {@code created_utc} seen
     * @param reachedOld true when the page reached posts at or before {@code since}
     */
    record Page(double newest, boolean reachedOld) {}

    /** Reads one listing; stores matching posts newer than {@code since}. */
    Page read(FetchContext ctx, SocialRuntime runtime, JsonNode listing, double since, String collection,
            Map<String, NormalizedRecord> out) {
        JsonNode children = SocialJson.at(listing, "data", "children");
        double newest = since;
        boolean old = false;
        if (children == null || !children.isArray()) {
            return new Page(newest, true);
        }
        SocialCollector collector = runtime.collector();
        for (JsonNode child : children) {
            JsonNode d = child.get("data");
            Long created = SocialJson.number(d, "created_utc");
            double createdUtc = d == null || d.get("created_utc") == null ? 0 : d.get("created_utc").asDouble();
            if (created == null || createdUtc <= since) {
                old = true;
                continue;
            }
            newest = Math.max(newest, createdUtc);
            SocialPost post = post(d, collection);
            if (post == null) {
                continue;
            }
            SocialCollector.Observation obs = collector.observe(post, ID, SocialVocabulary.PAGED);
            if (obs.record() != null) {
                out.put(obs.record().externalId(), obs.record());
            }
        }
        return new Page(newest, old);
    }

    /** The post of a listing item ({@code t3} link). */
    static SocialPost post(JsonNode d, String collection) {
        String name = SocialJson.text(d, "name");
        if (name == null) {
            return null;
        }
        String permalink = SocialJson.text(d, "permalink");
        String title = SocialJson.text(d, "title");
        String body = SocialJson.text(d, "selftext");
        String text = title == null ? body : body == null ? title : title + "\n\n" + body;
        List<String> links = new ArrayList<>();
        String url = SocialJson.text(d, "url");
        if (url != null && (permalink == null || !url.contains(permalink))) {
            links.add(url);
        }
        double created = d.get("created_utc") == null ? 0 : d.get("created_utc").asDouble();
        String author = SocialJson.text(d, "author");
        return new SocialPost(SocialVocabulary.REDDIT, name, permalink == null ? null
                : "https://www.reddit.com" + permalink, new SocialPost.Author(author == null ? "" : author, null,
                SocialJson.text(d, "author_fullname") != null ? SocialJson.text(d, "author_fullname") : author, null,
                null, null), text, null, Instant.ofEpochMilli((long) (created * 1000)), null, null, null, links,
                List.of(), List.of(), List.of(), new SocialPost.Engagement(SocialJson.number(d, "num_comments"), null,
                null, SocialJson.number(d, "score"), null), null, SocialJson.text(d, "subreddit"), collection, null);
    }

    /** Re-checks stored posts with {@code /api/info}; those it no longer returns are tombstoned. */
    List<NormalizedRecord> deletionSync(FetchContext ctx, SocialRuntime runtime, String api,
            Map<String, String> headers, SocialPlatformSettings settings) throws AdapterException, RateSpent {
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
            StringBuilder ids = new StringBuilder();
            for (StoredPosts.Candidate c : batch) {
                ids.append(ids.isEmpty() ? "" : ",").append(c.postId());
            }
            JsonNode listing = get(ctx, runtime, URI.create(api + "/api/info?id=" + enc(ids.toString())), headers);
            Set<String> present = new HashSet<>();
            JsonNode children = SocialJson.at(listing, "data", "children");
            if (children != null && children.isArray()) {
                for (JsonNode child : children) {
                    String name = SocialJson.text(child.get("data"), "name");
                    if (name != null) {
                        present.add(name);
                    }
                }
            }
            for (StoredPosts.Candidate c : batch) {
                if (!present.contains(c.postId())) {
                    NormalizedRecord r = runtime.collector().observeDelete(ID, SocialVocabulary.REDDIT, c.postId());
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

    /** An application-only token, renewed a minute before it expires. */
    String token(FetchContext ctx) throws AdapterException {
        if (token != null && tokenExpires != null && tokenExpires.isAfter(ctx.now().plusSeconds(60))) {
            return token;
        }
        Map<String, String> creds = ctx.credentials();
        String id = creds == null ? null : creds.get("clientId");
        String secret = creds == null ? null : creds.get("clientSecret");
        if (id == null || secret == null) {
            throw new AdapterException(ID + ": clientId and clientSecret are required");
        }
        String basic = Base64.getEncoder().encodeToString((id.strip() + ":" + secret.strip())
                .getBytes(StandardCharsets.UTF_8));
        UpstreamResponse r = ctx.http().post(URI.create(override != null ? override + "/api/v1/access_token"
                : TOKEN_URL), "application/x-www-form-urlencoded",
                "grant_type=client_credentials".getBytes(StandardCharsets.UTF_8),
                Map.of("Authorization", "Basic " + basic, "Accept", "application/json"));
        if (r.status() != 200) {
            throw new AdapterException(ID + ": the token endpoint answered HTTP " + r.status());
        }
        JsonNode body = JSON.readTree(r.body());
        String value = SocialJson.text(body, "access_token");
        if (value == null) {
            throw new UpstreamFormatException(ID + ": the token endpoint answered without access_token", "");
        }
        Long expires = SocialJson.number(body, "expires_in");
        token = value;
        tokenExpires = ctx.now().plusSeconds(expires == null ? 3600 : expires);
        return value;
    }

    private JsonNode get(FetchContext ctx, SocialRuntime runtime, URI uri, Map<String, String> headers)
            throws AdapterException, RateSpent {
        QuotaLedger quotas = runtime.quotas();
        if (quotas != null && !quotas.tryUse(ID, "queries", QUERIES_PER_MINUTE, Duration.ofMinutes(1), 1)) {
            throw new RateSpent();
        }
        SourceHttp http = ctx.http();
        UpstreamResponse r = http.get(uri, headers);
        String remaining = r.header("x-ratelimit-remaining");
        if (r.status() == 429) {
            throw new RateSpent();
        }
        if (r.status() != 200 || r.truncated()) {
            throw new UpstreamFormatException(ID + ": HTTP " + r.status() + " from " + uri.getPath(), "");
        }
        JsonNode body = JSON.readTree(r.body());
        if (remaining != null) {
            try {
                if (Double.parseDouble(remaining) < 1) {
                    throw new RateSpent();
                }
            } catch (NumberFormatException e) {
                // an unreadable header is ignored
            }
        }
        return body;
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

    /** A view of the adapter state with prefixed keys (so the channel window does not share the query window). */
    record Prefixed(FetchContext ctx, String prefix) implements ai.foresignal.source.spi.AdapterStateStore {

        @Override
        public java.util.Optional<String> get(String key) {
            return ctx.state().get(prefix + key);
        }

        @Override
        public void put(String key, String value) {
            ctx.state().put(prefix + key, value);
        }

        @Override
        public void remove(String key) {
            ctx.state().remove(prefix + key);
        }
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("Reddit")
                .description("Reddit Data API with the operator's own OAuth app: new posts of the followed subreddits"
                        + " and search for the attention topics' social terms (paged). Non-commercial use under the"
                        + " Reddit Data API Terms; off by default.")
                .homepage("https://www.reddit.com/dev/api/")
                .attribution("Reddit")
                .licence("Reddit Data API Terms (non-commercial)")
                .termsUrl("https://redditinc.com/policies/data-api-terms")
                .mode(Mode.POLLING)
                .credential(CredentialField.required("clientId", "The OAuth app's client id (reddit.com/prefs/apps)"))
                .credential(CredentialField.required("clientSecret", "The OAuth app's secret"))
                .credential(CredentialField.optional(SourceHttp.USER_AGENT_CONTACT_CREDENTIAL,
                        "A contact appended to the User-Agent, for example /u/yourname"))
                .interval(Duration.ofMinutes(5), Duration.ofMinutes(10))
                .defaultBudget(Budget.of(QUERIES_PER_MINUTE, 20_000))
                .fetchTimeout(Duration.ofMinutes(3));
        return SocialDescriptors.posts(b, SocialVocabulary.REDDIT).build();
    }
}
