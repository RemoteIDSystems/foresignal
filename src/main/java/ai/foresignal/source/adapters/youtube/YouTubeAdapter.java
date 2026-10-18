package ai.foresignal.source.adapters.youtube;

import ai.foresignal.social.QuotaLedger;
import ai.foresignal.social.SocialCollector;
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
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
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
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code youtube} (prompt 135): YouTube Data API v3 with the operator's API key (developers.google.com/youtube/v3,
 * verified 2026-10-17).
 *
 * <ul>
 *   <li>{@code search.list} ({@code part=snippet}, {@code type=video}, {@code order=date}, {@code publishedAfter},
 *       {@code maxResults} ≤ 50, {@code q} or {@code channelId}) for the standing terms and the followed channels: a
 *       <em>ranked sample</em>. Since June 2026 search has its own bucket of {@value #SEARCH_CALLS_PER_DAY} calls a day,
 *       counted in {@link QuotaLedger} unit {@code queries}; at most {@value #SEARCH_PER_RUN} searches per run.
 *   <li>{@code videos.list} ({@code part=snippet,statistics}, comma-separated ids, 1 unit) for new search results and,
 *       every {@value #REFRESH_HOURS} hours, the stored videos of the last 7 days: their {@code viewCount},
 *       {@code likeCount} and {@code commentCount} (strings in JSON) are the tracked videos' complete statistics, so a
 *       changed count is a new version. Units count against 10,000 a day (unit {@code units}).
 *   <li>Comment threads (option {@code comments}) are not read: their response fields were not verified.
 * </ul>
 *
 * <p>Quota windows are rolling 24 hours, which never allows more than the daily quota. A {@code quotaExceeded} answer
 * ends the run.
 */
@Component
public class YouTubeAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = SocialVocabulary.YOUTUBE_ADAPTER;

    /** The API base. */
    public static final String API = "https://www.googleapis.com/youtube/v3";

    /** Test-only override of the API base. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.youtube.base-url";

    /** The search calls a day (Search Queries bucket). */
    public static final int SEARCH_CALLS_PER_DAY = 100;

    /** The units a day. */
    public static final int UNITS_PER_DAY = 10_000;

    /** The searches per run. */
    public static final int SEARCH_PER_RUN = 4;

    /** How often stored videos' statistics are refreshed. */
    public static final int REFRESH_HOURS = 6;

    private static final Duration DAY = Duration.ofDays(1);
    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String base;
    private final ObjectProvider<SocialRuntime> runtimes;
    private final SocialRuntime fixed;

    /** Creates the adapter. */
    @Autowired
    public YouTubeAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String override,
            ObjectProvider<SocialRuntime> runtimes) {
        this.base = override == null || override.isBlank() ? API : override.strip().replaceAll("/+$", "");
        this.runtimes = runtimes;
        this.fixed = null;
    }

    /** Creates the adapter with a fixed runtime (tests; null for descriptor checks). */
    public YouTubeAdapter(String override, SocialRuntime runtime) {
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

    /** Thrown when a quota is spent; the run returns what it has. */
    static final class QuotaSpent extends Exception {
        private static final long serialVersionUID = 1L;
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        SocialRuntime runtime = runtime();
        String key = ctx.credentials() == null ? null : ctx.credentials().get("apiKey");
        if (runtime == null || key == null || key.isBlank()) {
            return FetchResult.of(List.of());
        }
        Map<String, NormalizedRecord> out = new LinkedHashMap<>();
        Instant now = ctx.now();
        try {
            List<SocialQueries.Query> queries = new ArrayList<>();
            for (String channel : channels(runtime.index(), runtime.settings(ID).channels())) {
                queries.add(new SocialQueries.Query("channel", channel, List.of()));
            }
            for (SocialQueries.Query q : SocialQueries.forNetwork(runtime.index(), SocialVocabulary.YOUTUBE)) {
                if (!SocialQueries.ACCOUNT.equals(q.kind())) {
                    queries.add(q);
                }
            }
            for (SocialQueries.Query q : SocialQueries.window(queries, ctx.state(), SEARCH_PER_RUN)) {
                use(runtime, "queries", SEARCH_CALLS_PER_DAY, 1);
                Instant since = parse(ctx.state().get(q.stateKey()).orElse(null));
                since = since == null ? now.minus(DAY) : since;
                String filter = "channel".equals(q.kind()) ? "&channelId=" + enc(q.text())
                        : "&q=" + enc(SocialQueries.HASHTAG.equals(q.kind()) ? "#" + q.text() : q.text());
                JsonNode result = get(ctx, URI.create(base + "/search?part=snippet&type=video&order=date&maxResults=50"
                        + "&publishedAfter=" + enc(since.toString()) + filter + "&key=" + enc(key.strip())));
                List<String> ids = new ArrayList<>();
                JsonNode items = result.get("items");
                if (items != null && items.isArray()) {
                    for (JsonNode item : items) {
                        String id = SocialJson.text(SocialJson.at(item, "id"), "videoId");
                        if (id != null) {
                            ids.add(id);
                        }
                    }
                }
                videos(ctx, runtime, key, ids, SocialVocabulary.COLLECTION_SAMPLE, SocialVocabulary.SAMPLED, out);
                ctx.state().put(q.stateKey(), now.toString());
            }
            Instant refreshed = parse(ctx.state().get("refresh.last").orElse(null));
            StoredPosts stored = runtime.stored();
            if (stored != null && (refreshed == null || refreshed.isBefore(now.minus(Duration.ofHours(REFRESH_HOURS))))) {
                List<String> ids = new ArrayList<>();
                for (StoredPosts.Candidate c : stored.deletionCandidates(ID, now.minus(Duration.ofDays(7)), null,
                        500)) {
                    ids.add(c.postId());
                }
                videos(ctx, runtime, key, ids, SocialVocabulary.COLLECTION_SAMPLE, SocialVocabulary.SAMPLED, out);
                ctx.state().put("refresh.last", now.toString());
            }
        } catch (QuotaSpent e) {
            ctx.log().info("{}: a YouTube quota is spent; the run stops here", ID);
        }
        return FetchResult.of(new ArrayList<>(out.values()));
    }

    /** The followed channel ids: platform channels and the topics' youtube-channel channels. */
    static List<String> channels(TermIndex index, List<String> platformChannels) {
        Set<String> out = new LinkedHashSet<>(platformChannels);
        for (TermIndex.TopicEntry e : index.topics()) {
            if (e.block().cueOnly()) {
                continue;
            }
            for (SocialTopicBlock.Channel c : e.block().channels()) {
                if ("youtube-channel".equals(c.type())) {
                    out.add(c.id());
                }
            }
        }
        out.removeIf(s -> !s.matches("^[A-Za-z0-9_-]{10,64}$"));
        return List.copyOf(out);
    }

    /** Reads videos in batches of 50 with {@code videos.list} and observes them. */
    void videos(FetchContext ctx, SocialRuntime runtime, String key, List<String> ids, String collection,
            String collectionType, Map<String, NormalizedRecord> out) throws AdapterException, QuotaSpent {
        SocialCollector collector = runtime.collector();
        for (int i = 0; i < ids.size(); i += 50) {
            List<String> batch = ids.subList(i, Math.min(ids.size(), i + 50));
            use(runtime, "units", UNITS_PER_DAY, 1);
            JsonNode result = get(ctx, URI.create(base + "/videos?part=snippet,statistics&id="
                    + enc(String.join(",", batch)) + "&key=" + enc(key.strip())));
            JsonNode items = result.get("items");
            if (items == null || !items.isArray()) {
                continue;
            }
            for (JsonNode item : items) {
                SocialPost post = post(item, collection);
                if (post != null) {
                    SocialCollector.Observation obs = collector.observe(post, ID, collectionType);
                    if (obs.record() != null) {
                        out.put(obs.record().externalId(), obs.record());
                    }
                }
            }
        }
    }

    /** The post of a {@code videos.list} item. */
    static SocialPost post(JsonNode item, String collection) {
        String id = SocialJson.text(item, "id");
        JsonNode snippet = SocialJson.at(item, "snippet");
        if (id == null || snippet == null) {
            return null;
        }
        JsonNode stats = SocialJson.at(item, "statistics");
        String title = SocialJson.text(snippet, "title");
        String description = SocialJson.text(snippet, "description");
        String text = title == null ? description : description == null ? title : title + "\n\n" + description;
        String language = SocialJson.text(snippet, "defaultLanguage") != null ? SocialJson.text(snippet,
                "defaultLanguage") : SocialJson.text(snippet, "defaultAudioLanguage");
        String thumb = SocialJson.text(SocialJson.at(snippet, "thumbnails", "high"), "url");
        if (thumb == null) {
            thumb = SocialJson.text(SocialJson.at(snippet, "thumbnails", "default"), "url");
        }
        String url = "https://www.youtube.com/watch?v=" + id;
        String channelId = SocialJson.text(snippet, "channelId");
        return new SocialPost(SocialVocabulary.YOUTUBE, id, url, new SocialPost.Author(
                SocialJson.text(snippet, "channelTitle") == null ? "" : SocialJson.text(snippet, "channelTitle"), null,
                channelId, null, null, null), text, language, SocialJson.instant(snippet, "publishedAt"), null, null,
                null, List.of(), SocialJson.strings(snippet.get("tags")), List.of(), thumb == null ? List.of()
                : List.of(new SocialPost.Media("video", url, thumb, null)), new SocialPost.Engagement(
                SocialJson.number(stats, "commentCount"), null, null, SocialJson.number(stats, "likeCount"),
                SocialJson.number(stats, "viewCount")), null, channelId, collection, null);
    }

    private static void use(SocialRuntime runtime, String unit, int limit, int amount) throws QuotaSpent {
        QuotaLedger quotas = runtime.quotas();
        if (quotas != null && !quotas.tryUse(ID, unit, limit, DAY, amount)) {
            throw new QuotaSpent();
        }
    }

    private static JsonNode get(FetchContext ctx, URI uri) throws AdapterException, QuotaSpent {
        UpstreamResponse r = ctx.http().get(uri, Map.of("Accept", "application/json"));
        if (r.status() == 403 && r.bodyAsString().contains("quotaExceeded")) {
            throw new QuotaSpent();
        }
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
                .name("YouTube")
                .description("YouTube Data API v3 with the operator's API key: search.list (a ranked sample, newest"
                        + " first) for the attention topics' social terms and followed channels, and videos.list"
                        + " statistics for tracked videos, inside the daily quotas. Off by default.")
                .homepage("https://developers.google.com/youtube/v3")
                .attribution("YouTube")
                .licence("YouTube API Services Terms of Service")
                .termsUrl("https://developers.google.com/youtube/terms/api-services-terms-of-service")
                .mode(Mode.POLLING)
                .credential(CredentialField.required("apiKey", "A YouTube Data API v3 key (Google Cloud console)"))
                .interval(Duration.ofMinutes(30), Duration.ofMinutes(60))
                .defaultBudget(Budget.of(20, 2000))
                .fetchTimeout(Duration.ofMinutes(3));
        // Tracked videos' statistics are this platform's complete path: a changed count is a new version.
        return SocialDescriptors.posts(b, SocialVocabulary.YOUTUBE, false).build();
    }
}
