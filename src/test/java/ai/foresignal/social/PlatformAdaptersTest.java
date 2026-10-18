package ai.foresignal.social;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.adapters.instagram.InstagramHashtagsAdapter;
import ai.foresignal.source.adapters.mastodon.MastodonAdapter;
import ai.foresignal.source.adapters.reddit.RedditAdapter;
import ai.foresignal.source.adapters.threads.ThreadsAdapter;
import ai.foresignal.source.adapters.youtube.YouTubeAdapter;
import ai.foresignal.source.series.SeriesPoint;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.StreamHandle;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import ai.foresignal.testsupport.social.SocialStreamContext;
import ai.foresignal.testsupport.social.SocialTestSupport;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * The search and poll adapters against WireMock (prompt 135): Mastodon's trending-tag history as series points, Reddit
 * paging, the shared query rate and {@code X-Ratelimit-Remaining}, YouTube unit and search-call accounting, the Threads
 * 2,200-query window (queries without results free), and Instagram's 30 unique hashtags per 7 days. Fixtures are
 * synthetic (built from the providers' documentation); recapture with real keys.
 */
@IntegrationTest
class PlatformAdaptersTest extends WireMockSupport {

    @Autowired SocialRuntime runtime;
    @Autowired QuotaLedger quotas;
    @Autowired SocialPlatformRepository platforms;
    @Autowired TermIndexService terms;
    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper json;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    AdapterHttpHarness harness;
    MutableClock clock;

    @BeforeEach
    void setUp() {
        SocialTestSupport.clean(jdbc);
        clock = new MutableClock(Instant.now().truncatedTo(ChronoUnit.SECONDS));
        harness = new AdapterHttpHarness(jdbc, json, properties, install, clock, Budget.of(1000, 100_000));
        Map<String, Object> block = new java.util.LinkedHashMap<>(SocialTestSupport.block(List.of("Bab el-Mandeb"),
                List.of("RedSea")));
        block.put("channels", List.of(Map.of("platform", "reddit", "type", "subreddit", "id", "shipping")));
        terms.install(SocialTestSupport.index(Map.of("red-sea", block)));
    }

    private void platform(String id, List<String> channels, Map<String, Object> options) {
        SocialPlatformEntity row = new SocialPlatformEntity(id, Instant.now());
        row.setEnabled(true);
        row.setChannels(channels);
        row.setDeletionSync(false);
        row.setOptions(options);
        platforms.save(row);
    }

    private void reset(SourceAdapter adapter) {
        harness.reset(adapter.id());
        jdbc.update("update source_health set adapter_state = '{}'::jsonb where source_id = ?", adapter.id());
    }

    private static void json(String path, String body) {
        WIREMOCK.stubFor(get(urlPathEqualTo(path)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(body)));
    }

    @Test
    @DisplayName("Mastodon: trending tags that match a topic become daily series points of uses (accounts as extra)")
    void mastodonTrends() throws Exception {
        platform("mastodon", List.of(), Map.of("instances", List.of("mastodon.example")));
        long day = Instant.now().truncatedTo(ChronoUnit.DAYS).getEpochSecond();
        json("/api/v1/trends/tags", "[{\"name\":\"RedSea\",\"url\":\"https://mastodon.example/tags/redsea\","
                + "\"history\":[{\"day\":\"" + day + "\",\"uses\":\"120\",\"accounts\":\"40\"},{\"day\":\""
                + (day - 86400) + "\",\"uses\":\"30\",\"accounts\":\"12\"}]},{\"name\":\"cats\",\"history\":"
                + "[{\"day\":\"" + day + "\",\"uses\":\"999\",\"accounts\":\"500\"}]}]");
        MastodonAdapter adapter = new MastodonAdapter(baseUrl(), runtime);
        SocialStreamContext ctx = new SocialStreamContext(adapter.descriptor(), clock, SocialStreamContext.forwarding());
        List<NormalizedRecord> sink = new CopyOnWriteArrayList<>();
        StreamHandle handle = adapter.start(ctx, sink::add);
        try {
            long deadline = System.nanoTime() + Duration.ofSeconds(10).toNanos();
            while (sink.size() < 2 && System.nanoTime() < deadline) {
                Thread.sleep(50);
            }
        } finally {
            handle.close();
        }
        assertThat(sink).hasSize(2).allSatisfy(r -> {
            assertThat(r.kind()).isEqualTo(SeriesPoint.KIND);
            assertThat(r.payload()).containsEntry("series", "mastodon-trend:mastodon.example:redsea")
                    .containsEntry("frequency", "P1D").containsEntry("topicId", "red-sea");
        });
        assertThat(sink).extracting(r -> r.payload().get("value")).containsExactlyInAnyOrder(120.0, 30.0);
        assertThat(ctx.opened()).as("no token: no hashtag stream").isEmpty();
        assertThat(ctx.connectedCalls()).isEqualTo(1);
    }

    @Test
    @DisplayName("Reddit: application-only token, /r/{sub}/new paged back to the last run, search for terms")
    void redditPaging() throws Exception {
        platform("reddit", List.of(), Map.of());
        harness.credentials.put("clientId", "id");
        harness.credentials.put("clientSecret", "secret");
        RedditAdapter adapter = new RedditAdapter(baseUrl(), runtime);
        reset(adapter);
        long now = clock.instant().getEpochSecond();
        WIREMOCK.stubFor(post(urlPathEqualTo("/api/v1/access_token")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"access_token\":\"tok\",\"token_type\":\"bearer\",\"expires_in\":3600}")));
        WIREMOCK.stubFor(get(urlPathEqualTo("/r/shipping/new")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody("{\"kind\":\"Listing\",\"data\":{\"after\":"
                        + "null,\"children\":[" + link("t3_a", now - 60, "Bab el-Mandeb closed") + ","
                        + link("t3_b", now - 120, "Port news") + "," + link("t3_old", now - 7200, "Old Bab el-Mandeb")
                        + "]}}")));
        json("/search", "{\"kind\":\"Listing\",\"data\":{\"after\":null,\"children\":[" + link("t3_c", now - 30,
                "Houthi claim near Bab el-Mandeb") + "]}}");
        FetchResult result = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(result.records()).extracting(NormalizedRecord::externalId)
                .containsExactlyInAnyOrder("reddit:t3_a", "reddit:t3_c");
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo("/r/shipping/new")).withHeader("Authorization",
                equalTo("Bearer tok")).withQueryParam("limit", equalTo("100")));
        assertThat(quotas.used("reddit", "queries", Duration.ofMinutes(1))).isGreaterThanOrEqualTo(2);
        NormalizedRecord a = result.records().stream().filter(r -> r.externalId().equals("reddit:t3_a")).findFirst()
                .orElseThrow();
        assertThat(a.payload()).containsEntry("channel", "shipping").containsEntry("collection", "search");
    }

    private static String link(String name, long created, String title) {
        return "{\"kind\":\"t3\",\"data\":{\"name\":\"" + name + "\",\"subreddit\":\"shipping\",\"author\":\"user_"
                + name + "\",\"author_fullname\":\"t2_" + name + "\",\"title\":\"" + title + "\",\"selftext\":\"\","
                + "\"created_utc\":" + created + ".0,\"permalink\":\"/r/shipping/comments/" + name + "/x/\","
                + "\"url\":\"https://www.reddit.com/r/shipping/comments/" + name + "/x/\",\"num_comments\":3,"
                + "\"score\":10}}";
    }

    @Test
    @DisplayName("Reddit: a spent minute quota sends nothing; X-Ratelimit-Remaining 0 stops the run")
    void redditRateLimits() throws Exception {
        platform("reddit", List.of(), Map.of());
        harness.credentials.put("clientId", "id");
        harness.credentials.put("clientSecret", "secret");
        RedditAdapter adapter = new RedditAdapter(baseUrl(), runtime);
        reset(adapter);
        WIREMOCK.stubFor(post(urlPathEqualTo("/api/v1/access_token")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody("{\"access_token\":\"tok\",\"expires_in\":60}")));
        quotas.record("reddit", "queries", null, RedditAdapter.QUERIES_PER_MINUTE);
        adapter.fetch(harness.context(adapter, Map.of()));
        WIREMOCK.verify(0, getRequestedFor(urlPathMatching("/r/.*")));
        jdbc.update("DELETE FROM social_quota_use");
        WIREMOCK.stubFor(get(urlPathEqualTo("/r/shipping/new")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withHeader("X-Ratelimit-Remaining", "0")
                .withBody("{\"data\":{\"after\":null,\"children\":[]}}")));
        adapter.fetch(harness.context(adapter, Map.of()));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo("/r/shipping/new")));
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo("/search")));
    }

    @Test
    @DisplayName("YouTube: a search takes one call of the 100-a-day bucket, videos.list one unit; a full bucket sends none")
    void youtubeUnits() throws Exception {
        platform("youtube", List.of(), Map.of());
        harness.credentials.put("apiKey", "key");
        YouTubeAdapter adapter = new YouTubeAdapter(baseUrl(), runtime);
        reset(adapter);
        json("/search", "{\"items\":[{\"id\":{\"kind\":\"youtube#video\",\"videoId\":\"vid1\"}}]}");
        json("/videos", "{\"items\":[{\"id\":\"vid1\",\"snippet\":{\"publishedAt\":\"2026-10-16T10:00:00Z\","
                + "\"channelId\":\"UCabcdefghijk\",\"channelTitle\":\"Ship Watch\",\"title\":\"Bab el-Mandeb today\","
                + "\"description\":\"Footage\",\"defaultLanguage\":\"en\",\"thumbnails\":{\"default\":{\"url\":"
                + "\"https://i.ytimg.com/vi/vid1/default.jpg\"}}},\"statistics\":{\"viewCount\":\"1000\","
                + "\"likeCount\":\"50\",\"commentCount\":\"7\"}}]}");
        FetchResult result = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(result.records()).singleElement().satisfies(r -> {
            assertThat(r.externalId()).isEqualTo("youtube:vid1");
            assertThat(r.payload()).containsEntry("collection", "sample").containsEntry("channel", "UCabcdefghijk");
        });
        assertThat(quotas.used("youtube", "queries", Duration.ofDays(1))).isEqualTo(2);
        assertThat(quotas.used("youtube", "units", Duration.ofDays(1))).isEqualTo(2);
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo("/search")).withQueryParam("order", equalTo("date"))
                .withQueryParam("type", equalTo("video")).withQueryParam("key", equalTo("key")));
        jdbc.update("DELETE FROM social_quota_use");
        quotas.record("youtube", "queries", null, YouTubeAdapter.SEARCH_CALLS_PER_DAY);
        WIREMOCK.resetRequests();
        adapter.fetch(harness.context(adapter, Map.of()));
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo("/search")));
    }

    @Test
    @DisplayName("Threads: a query with results takes one of 2,200 per 24 h, an empty one is free; a full window sends none")
    void threadsQuota() throws Exception {
        platform("threads", List.of(), Map.of());
        harness.credentials.put("accessToken", "tok");
        ThreadsAdapter adapter = new ThreadsAdapter(baseUrl(), runtime);
        reset(adapter);
        WIREMOCK.stubFor(get(urlPathEqualTo("/keyword_search")).withQueryParam("search_mode", equalTo("KEYWORD"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody("{\"data\":[{\"id\":\"th1\",\"text\":\"Bab el-Mandeb closed\",\"media_type\":"
                                + "\"TEXT_POST\",\"permalink\":\"https://www.threads.net/@a/post/x\",\"timestamp\":"
                                + "\"2026-10-16T10:00:00+0000\",\"username\":\"a\"}]}")));
        WIREMOCK.stubFor(get(urlPathEqualTo("/keyword_search")).withQueryParam("search_mode", equalTo("TAG"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody("{\"data\":[]}")));
        FetchResult result = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(result.records()).extracting(NormalizedRecord::externalId).containsExactly("threads:th1");
        assertThat(quotas.used("threads", "queries", Duration.ofDays(1))).isEqualTo(1);
        jdbc.update("DELETE FROM social_quota_use");
        quotas.record("threads", "queries", null, ThreadsAdapter.QUERIES_PER_DAY);
        WIREMOCK.resetRequests();
        adapter.fetch(harness.context(adapter, Map.of()));
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo("/keyword_search")));
    }

    @Test
    @DisplayName("Instagram: 30 unique hashtags per 7 days; a hashtag already searched stays free, a new one is skipped")
    void instagramWindow() throws Exception {
        platform("instagram-hashtags", List.of(), Map.of());
        harness.credentials.put("accessToken", "tok");
        harness.credentials.put("userId", "17841400000000000");
        InstagramHashtagsAdapter adapter = new InstagramHashtagsAdapter(baseUrl(), runtime);
        reset(adapter);
        for (int i = 0; i < InstagramHashtagsAdapter.HASHTAGS_PER_WEEK; i++) {
            quotas.record("instagram-hashtags", "hashtags", "tag" + i, 1);
        }
        adapter.fetch(harness.context(adapter, Map.of()));
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo("/ig_hashtag_search")));

        jdbc.update("DELETE FROM social_quota_use WHERE use_key = 'tag0'");
        quotas.record("instagram-hashtags", "hashtags", "redsea", 1);
        json("/ig_hashtag_search", "{\"data\":[{\"id\":\"17843853986012965\"}]}");
        json("/17843853986012965/recent_media", "{\"data\":[{\"id\":\"ig1\",\"caption\":\"Bab el-Mandeb #RedSea\","
                + "\"media_type\":\"IMAGE\",\"media_url\":\"https://scontent.example/ig1.jpg\",\"permalink\":"
                + "\"https://www.instagram.com/p/ig1/\",\"timestamp\":\"2026-10-16T10:00:00+0000\","
                + "\"comments_count\":2,\"like_count\":9}]}");
        FetchResult result = adapter.fetch(harness.context(adapter, Map.of()));
        assertThat(result.records()).singleElement().satisfies(r -> {
            assertThat(r.externalId()).isEqualTo("instagram:ig1");
            assertThat(r.payload()).containsEntry("collection", "sample").containsEntry("channel", "#redsea");
        });
        assertThat(quotas.keys("instagram-hashtags", "hashtags", Duration.ofDays(7))).hasSize(30);
    }
}
