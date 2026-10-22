package ai.foresignal.source.adapters.bluesky;

import ai.foresignal.social.CueRequests;
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
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
 * {@code bluesky-search} (prompt 135): AppView {@code app.bsky.feed.searchPosts} ({@code q}, {@code sort=latest},
 * {@code since}, {@code tag}, {@code limit} ≤ 100, {@code cursor}; lexicon verified 2026-10-17) for the standing terms
 * and hashtags of the topics that apply to Bluesky, a paged search: complete within the API's window. Free, so its
 * source starts enabled (prompt 145). Whether social cues may use the platform is its platform row's setting
 * ({@code SocialPlatformService.requireEnabled}), a separate switch.
 *
 * <p>Without credentials it asks {@code https://api.bsky.app} (answered unauthenticated on 2026-10-17; the
 * {@code public.api.bsky.app} host refused search with 403). With an {@code identifier} and {@code appPassword} it opens
 * a session ({@code com.atproto.server.createSession} on {@code https://bsky.social}) and searches through the PDS,
 * which proxies the AppView. At most {@value #QUERIES_PER_RUN} queries per run (round robin), at most
 * {@value #MAX_PAGES} pages each, since the query's last run.
 *
 * <p>Deletion sync (on by default): once a day the stored posts inside retention are re-checked with
 * {@code app.bsky.feed.getPosts} (25 URIs per call); a post the AppView no longer returns is tombstoned.
 */
@Component
public class BlueskySearchAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = SocialVocabulary.BLUESKY_SEARCH;

    /** The unauthenticated AppView. */
    public static final String APPVIEW = "https://api.bsky.app";

    /** The entryway that opens sessions and proxies the AppView. */
    public static final String ENTRYWAY = "https://bsky.social";

    /** Test-only override of both hosts. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.bluesky-search.base-url";

    /** The most queries per run. */
    public static final int QUERIES_PER_RUN = 20;

    /** The most pages per query. */
    public static final int MAX_PAGES = 5;

    /** The page size. */
    public static final int PAGE = 100;

    /** The URIs per {@code getPosts} call. */
    public static final int GET_POSTS_MAX = 25;

    /** How often deletion sync runs. */
    public static final Duration DELETION_EVERY = Duration.ofDays(1);

    /** The most posts re-checked per run. */
    public static final int DELETION_PER_RUN = 500;

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String override;
    private final ObjectProvider<SocialRuntime> runtimes;
    private final SocialRuntime fixed;
    private volatile String accessJwt;
    private volatile Instant sessionAt;

    /** Creates the adapter. */
    @Autowired
    public BlueskySearchAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String override,
            ObjectProvider<SocialRuntime> runtimes) {
        this.override = blank(override);
        this.runtimes = runtimes;
        this.fixed = null;
    }

    /** Creates the adapter with a fixed runtime (tests; null for descriptor checks). */
    public BlueskySearchAdapter(String override, SocialRuntime runtime) {
        this.override = blank(override);
        this.runtimes = null;
        this.fixed = runtime;
    }

    private static String blank(String s) {
        return s == null || s.isBlank() ? null : s.strip().replaceAll("/+$", "");
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
        if (runtime == null) {
            return FetchResult.of(List.of());
        }
        Instant now = ctx.now();
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Accept", "application/json");
        String base = override != null ? override : APPVIEW;
        String jwt = session(ctx);
        if (jwt != null) {
            headers.put("Authorization", "Bearer " + jwt);
            base = override != null ? override : ENTRYWAY;
        }
        Map<String, NormalizedRecord> out = new LinkedHashMap<>();
        SocialCollector collector = runtime.collector();
        for (CueRequests.Request r : runtime.drainCues(ID)) {
            cue(ctx, runtime, base, headers, r, out);
        }
        for (SocialQueries.Query q : SocialQueries.window(SocialQueries.forNetwork(runtime.index(),
                SocialVocabulary.BLUESKY), ctx.state(), QUERIES_PER_RUN)) {
            if (SocialQueries.ACCOUNT.equals(q.kind())) {
                continue;
            }
            Instant since = parse(ctx.state().get(q.stateKey()).orElse(null));
            if (since == null) {
                since = now.minus(Duration.ofHours(1));
            }
            String cursor = null;
            for (int page = 0; page < MAX_PAGES; page++) {
                StringBuilder url = new StringBuilder(base).append("/xrpc/app.bsky.feed.searchPosts?q=")
                        .append(enc(q.text())).append("&sort=latest&limit=").append(PAGE).append("&since=")
                        .append(enc(since.toString()));
                if (SocialQueries.HASHTAG.equals(q.kind())) {
                    url.append("&tag=").append(enc(q.text()));
                }
                if (cursor != null) {
                    url.append("&cursor=").append(enc(cursor));
                }
                JsonNode root = get(ctx, URI.create(url.toString()), headers);
                JsonNode posts = root.get("posts");
                if (posts == null || !posts.isArray()) {
                    throw new UpstreamFormatException(ID + ": searchPosts answered without posts", "");
                }
                for (JsonNode view : posts) {
                    SocialPost post = BlueskyRecords.fromPostView(view, SocialVocabulary.COLLECTION_SEARCH);
                    if (post == null) {
                        continue;
                    }
                    SocialCollector.Observation obs = collector.observe(post, ID, SocialVocabulary.PAGED);
                    if (obs.record() != null) {
                        out.put(obs.record().externalId(), obs.record());
                    }
                }
                cursor = SocialJson.text(root, "cursor");
                if (cursor == null || posts.size() < PAGE) {
                    break;
                }
            }
            ctx.state().put(q.stateKey(), now.toString());
        }
        SocialPlatformSettings settings = runtime.settings(ID);
        if (settings.deletionSync()) {
            for (NormalizedRecord r : deletionSync(ctx, runtime, base, headers, settings)) {
                out.put(r.externalId(), r);
            }
        }
        return FetchResult.of(new ArrayList<>(out.values()));
    }

    /**
     * Runs one 137 cue query: {@code searchPosts} with the exact query since the window start, posts after the window
     * end dropped; posts are stored with {@code collection = cue} and counted as sampled (never baselined).
     */
    void cue(FetchContext ctx, SocialRuntime runtime, String base, Map<String, String> headers, CueRequests.Request r,
            Map<String, NormalizedRecord> out) {
        int found = 0;
        String error = null;
        try {
            String url = base + "/xrpc/app.bsky.feed.searchPosts?q=" + enc(r.query()) + "&sort=latest&limit="
                    + Math.max(1, Math.min(PAGE, r.maxResults())) + "&since=" + enc(r.from().toString());
            JsonNode posts = get(ctx, URI.create(url), headers).get("posts");
            if (posts != null && posts.isArray()) {
                for (JsonNode view : posts) {
                    SocialPost post = BlueskyRecords.fromPostView(view, SocialVocabulary.COLLECTION_CUE);
                    if (post == null || (post.createdAt() != null && r.to() != null && post.createdAt().isAfter(r.to()))) {
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
        } catch (AdapterException | RuntimeException e) {
            error = e.getMessage() == null ? e.toString() : e.getMessage();
        }
        runtime.reportCue(new CueRequests.Result(r.cueId(), ID, r.query(), found, BigDecimal.ZERO, error));
    }

    /** The session token when credentials are set (renewed hourly), else null. */
    String session(FetchContext ctx) throws AdapterException {
        Map<String, String> creds = ctx.credentials();
        String identifier = creds == null ? null : creds.get("identifier");
        String password = creds == null ? null : creds.get("appPassword");
        if (identifier == null || identifier.isBlank() || password == null || password.isBlank()) {
            return null;
        }
        if (accessJwt != null && sessionAt != null && sessionAt.isAfter(ctx.now().minus(Duration.ofHours(1)))) {
            return accessJwt;
        }
        String body = JSON.writeValueAsString(Map.of("identifier", identifier.strip(), "password", password.strip()));
        UpstreamResponse response = ctx.http().post(URI.create((override != null ? override : ENTRYWAY)
                + "/xrpc/com.atproto.server.createSession"), "application/json", body.getBytes(StandardCharsets.UTF_8),
                Map.of("Accept", "application/json"));
        if (response.status() != 200) {
            throw new AdapterException(ID + ": createSession answered HTTP " + response.status());
        }
        String jwt = SocialJson.text(JSON.readTree(response.body()), "accessJwt");
        if (jwt == null) {
            throw new UpstreamFormatException(ID + ": createSession answered without accessJwt", "");
        }
        accessJwt = jwt;
        sessionAt = ctx.now();
        return jwt;
    }

    /** Re-checks stored posts once a day; returns the tombstoned versions of those the AppView no longer has. */
    List<NormalizedRecord> deletionSync(FetchContext ctx, SocialRuntime runtime, String base,
            Map<String, String> headers, SocialPlatformSettings settings) throws AdapterException {
        StoredPosts stored = runtime.stored();
        Instant last = parse(ctx.state().get("del.last").orElse(null));
        Instant now = ctx.now();
        String after = ctx.state().get("del.after").orElse(null);
        if (stored == null || (after == null && last != null && last.isAfter(now.minus(DELETION_EVERY)))) {
            return List.of();
        }
        List<StoredPosts.Candidate> candidates = stored.deletionCandidates(ID, now.minus(settings.postRetention()),
                after, DELETION_PER_RUN);
        List<NormalizedRecord> out = new ArrayList<>();
        for (int i = 0; i < candidates.size(); i += GET_POSTS_MAX) {
            List<StoredPosts.Candidate> batch = candidates.subList(i, Math.min(candidates.size(), i + GET_POSTS_MAX));
            StringBuilder url = new StringBuilder(base).append("/xrpc/app.bsky.feed.getPosts?");
            for (int j = 0; j < batch.size(); j++) {
                url.append(j == 0 ? "" : "&").append("uris=").append(enc(batch.get(j).postId()));
            }
            JsonNode root = get(ctx, URI.create(url.toString()), headers);
            Set<String> present = new HashSet<>();
            if (root.get("posts") != null) {
                for (JsonNode p : root.get("posts")) {
                    if (SocialJson.text(p, "uri") != null) {
                        present.add(SocialJson.text(p, "uri"));
                    }
                }
            }
            for (StoredPosts.Candidate c : batch) {
                if (!present.contains(c.postId())) {
                    NormalizedRecord r = runtime.collector().observeDelete(ID, SocialVocabulary.BLUESKY, c.postId());
                    if (r != null) {
                        out.add(r);
                    }
                }
            }
        }
        if (candidates.size() < DELETION_PER_RUN) {
            ctx.state().remove("del.after");
            ctx.state().put("del.last", now.toString());
            runtime.deletionRan(ID, now);
        } else {
            ctx.state().put("del.after", candidates.get(candidates.size() - 1).externalId());
        }
        return out;
    }

    private static JsonNode get(FetchContext ctx, URI uri, Map<String, String> headers) throws AdapterException {
        UpstreamResponse response = ctx.http().get(uri, headers);
        if (response.status() != 200 || response.truncated()) {
            throw new UpstreamFormatException(ID + ": " + (response.status() != 200 ? "HTTP " + response.status()
                    : "response over the cap") + " from " + uri.getPath(), "");
        }
        return JSON.readTree(response.body());
    }

    static Instant parse(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(s.strip());
        } catch (RuntimeException e) {
            return null;
        }
    }

    static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("Bluesky search")
                .description("Bluesky AppView searchPosts (sort=latest) for the attention topics' social terms and"
                        + " hashtags: a paged search, complete within the API's window. Optional app password when"
                        + " the AppView requires a session.")
                .homepage("https://docs.bsky.app/")
                .attribution("Bluesky Social PBC")
                .licence("Bluesky public data; Bluesky Protocol Services terms")
                .termsUrl("https://bsky.social/about/support/tos")
                .mode(Mode.POLLING)
                .credential(CredentialField.optional("identifier", "Bluesky handle or email, for an app-password session"))
                .credential(CredentialField.optional("appPassword", "An app password (Settings, App passwords)"))
                .interval(Duration.ofMinutes(5), Duration.ofMinutes(15))
                .defaultBudget(Budget.of(30, 3000))
                .fetchTimeout(Duration.ofMinutes(3));
        return SocialDescriptors.posts(b, SocialVocabulary.BLUESKY).build();
    }
}
