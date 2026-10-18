package ai.foresignal.source.adapters.instagram;

import ai.foresignal.social.QuotaLedger;
import ai.foresignal.social.SocialCollector;
import ai.foresignal.social.SocialPost;
import ai.foresignal.social.SocialRuntime;
import ai.foresignal.social.SocialTopicBlock;
import ai.foresignal.social.SocialVocabulary;
import ai.foresignal.social.TermIndex;
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
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
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
 * {@code instagram-hashtags} (prompt 135): Instagram Graph API hashtag search for the operator's account
 * ({@code GET /ig_hashtag_search?user_id=&q=} for the hashtag id, then {@code GET /{hashtag-id}/recent_media?user_id=
 * &fields=id,caption,media_type,media_url,permalink,timestamp,comments_count,like_count&limit=50}; Graph API v26.0;
 * developers.facebook.com reference pages verified 2026-10-17). It needs the Instagram Public Content Access feature
 * and {@code instagram_basic} with a user token tied to a Facebook Page. Off by default.
 *
 * <p>A ranked sample of the last 24 hours. At most {@value #HASHTAGS_PER_WEEK} unique hashtags per rolling 7 days
 * ({@link QuotaLedger} unit {@code hashtags}, keyed by hashtag): a hashtag already searched in the window is free, a
 * new one needs room. The answer names no author ({@code username} is not available), so each post counts as its own
 * author.
 */
@Component
public class InstagramHashtagsAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = SocialVocabulary.INSTAGRAM_ADAPTER;

    /** The Graph API base. */
    public static final String API = "https://graph.facebook.com/v26.0";

    /** Test-only override of the API base. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.instagram-hashtags.base-url";

    /** Unique hashtags per rolling 7 days. */
    public static final int HASHTAGS_PER_WEEK = 30;

    /** The fields asked for. */
    static final String FIELDS = "id,caption,media_type,media_url,permalink,timestamp,comments_count,like_count";

    private static final Duration WEEK = Duration.ofDays(7);
    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String base;
    private final ObjectProvider<SocialRuntime> runtimes;
    private final SocialRuntime fixed;

    /** Creates the adapter. */
    @Autowired
    public InstagramHashtagsAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String override,
            ObjectProvider<SocialRuntime> runtimes) {
        this.base = override == null || override.isBlank() ? API : override.strip().replaceAll("/+$", "");
        this.runtimes = runtimes;
        this.fixed = null;
    }

    /** Creates the adapter with a fixed runtime (tests; null for descriptor checks). */
    public InstagramHashtagsAdapter(String override, SocialRuntime runtime) {
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

    /** The hashtags searched: the topics' hashtags (for topics that apply to Instagram) and the platform channels. */
    static List<String> hashtags(TermIndex index, List<String> channels) {
        Set<String> out = new LinkedHashSet<>();
        for (TermIndex.TopicEntry e : index.topics()) {
            SocialTopicBlock b = e.block();
            if (b.cueOnly() || (!b.platforms().isEmpty() && !b.platforms().contains(SocialVocabulary.INSTAGRAM))) {
                continue;
            }
            for (String tag : b.hashtags()) {
                out.add(bare(tag));
            }
        }
        for (String c : channels) {
            out.add(bare(c));
        }
        out.removeIf(String::isBlank);
        return List.copyOf(out);
    }

    private static String bare(String tag) {
        String t = tag.strip();
        while (t.startsWith("#")) {
            t = t.substring(1);
        }
        return t.toLowerCase(Locale.ROOT);
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        SocialRuntime runtime = runtime();
        String token = ctx.credentials() == null ? null : ctx.credentials().get("accessToken");
        String user = ctx.credentials() == null ? null : ctx.credentials().get("userId");
        if (runtime == null || token == null || token.isBlank() || user == null || user.isBlank()) {
            return FetchResult.of(List.of());
        }
        QuotaLedger quotas = runtime.quotas();
        SocialCollector collector = runtime.collector();
        Map<String, NormalizedRecord> out = new LinkedHashMap<>();
        for (String tag : hashtags(runtime.index(), runtime.settings(ID).channels())) {
            if (quotas != null && !quotas.tryKey(ID, "hashtags", tag, HASHTAGS_PER_WEEK, WEEK)) {
                ctx.log().info("{}: #{} skipped, 30 unique hashtags were searched in the last 7 days", ID, tag);
                continue;
            }
            String idKey = "tag." + tag;
            String hashtagId = ctx.state().get(idKey).orElse(null);
            if (hashtagId == null) {
                JsonNode found = get(ctx, URI.create(base + "/ig_hashtag_search?user_id=" + enc(user.strip()) + "&q="
                        + enc(tag) + "&access_token=" + enc(token.strip())));
                JsonNode data = found.get("data");
                hashtagId = data != null && data.isArray() && !data.isEmpty() ? SocialJson.text(data.get(0), "id")
                        : null;
                if (hashtagId == null) {
                    continue;
                }
                ctx.state().put(idKey, hashtagId);
            }
            JsonNode media = get(ctx, URI.create(base + "/" + enc(hashtagId) + "/recent_media?user_id="
                    + enc(user.strip()) + "&fields=" + enc(FIELDS) + "&limit=50&access_token=" + enc(token.strip())));
            JsonNode data = media.get("data");
            if (data == null || !data.isArray()) {
                continue;
            }
            for (JsonNode item : data) {
                SocialPost post = post(item, tag);
                if (post != null) {
                    SocialCollector.Observation obs = collector.observe(post, ID, SocialVocabulary.SAMPLED);
                    if (obs.record() != null) {
                        out.put(obs.record().externalId(), obs.record());
                    }
                }
            }
        }
        return FetchResult.of(new ArrayList<>(out.values()));
    }

    /** The post of a {@code recent_media} item (no author: each post is its own author). */
    static SocialPost post(JsonNode item, String tag) {
        String id = SocialJson.text(item, "id");
        if (id == null) {
            return null;
        }
        String mediaType = SocialJson.text(item, "media_type");
        String mediaUrl = SocialJson.text(item, "media_url");
        List<SocialPost.Media> media = mediaUrl == null ? List.of() : List.of(new SocialPost.Media(
                "VIDEO".equals(mediaType) ? "video" : "image", mediaUrl, null, null));
        List<String> hashtags = new ArrayList<>(ai.foresignal.social.SocialPosts.hashtags(SocialJson.text(item,
                "caption")));
        if (hashtags.stream().noneMatch(h -> h.equalsIgnoreCase(tag))) {
            hashtags.add(tag);
        }
        return new SocialPost(SocialVocabulary.INSTAGRAM, id, SocialJson.text(item, "permalink"),
                new SocialPost.Author("", null, "post:" + id, null, null, null), SocialJson.text(item, "caption"),
                null, SocialJson.instant(item, "timestamp"), null, null, null, List.of(), hashtags, List.of(), media,
                new SocialPost.Engagement(SocialJson.number(item, "comments_count"), null, null,
                        SocialJson.number(item, "like_count"), null), null, "#" + tag,
                SocialVocabulary.COLLECTION_SAMPLE, null);
    }

    private static JsonNode get(FetchContext ctx, URI uri) throws AdapterException {
        UpstreamResponse r = ctx.http().get(uri, Map.of("Accept", "application/json"));
        if (r.status() != 200 || r.truncated()) {
            throw new UpstreamFormatException(ID + ": HTTP " + r.status() + " from the Graph API", "");
        }
        return JSON.readTree(r.body());
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("Instagram hashtag search")
                .description("Instagram Graph API hashtag search (recent media of the last 24 hours) for the attention"
                        + " topics' hashtags with the operator's business account token; 30 unique hashtags per rolling"
                        + " 7 days. A ranked sample; off by default.")
                .homepage("https://developers.facebook.com/docs/instagram-platform/")
                .attribution("Instagram (Meta)")
                .licence("Meta Platform Terms")
                .termsUrl("https://developers.facebook.com/terms/")
                .mode(Mode.POLLING)
                .credential(CredentialField.required("accessToken",
                        "A user access token with instagram_basic and Instagram Public Content Access"))
                .credential(CredentialField.required("userId", "The Instagram professional account's user id"))
                .interval(Duration.ofMinutes(30), Duration.ofMinutes(60))
                .defaultBudget(Budget.of(20, 1000))
                .fetchTimeout(Duration.ofMinutes(3));
        return SocialDescriptors.posts(b, SocialVocabulary.INSTAGRAM).build();
    }
}
