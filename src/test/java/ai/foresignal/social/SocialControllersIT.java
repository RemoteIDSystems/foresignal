package ai.foresignal.social;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.attention.AttentionTopicEntity;
import ai.foresignal.attention.AttentionTopicRepository;
import ai.foresignal.ingest.FetchLogEntry;
import ai.foresignal.ingest.IngestQueue;
import ai.foresignal.ingest.IngestRecord;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.mcp.McpSessions;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.mcp.tools.GetSocialVolumeTool;
import ai.foresignal.mcp.tools.SearchSocialPostsTool;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.store.FetchTrigger;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.security.TestTokens;
import ai.foresignal.testsupport.social.SocialTestSupport;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The Social endpoints (prompt 135): platforms (read; settings admin), coverage, volume with baselines, posts and one
 * post, their scopes and Problem codes ({@code UNKNOWN_PLATFORM}, {@code NOT_FOUND}, {@code VALIDATION_FAILED},
 * {@code INVALID_CURSOR}), the attention topic's stored social block, and the MCP tools {@code get_social_volume} and
 * {@code search_social_posts}.
 */
@IntegrationTest
class SocialControllersIT {

    static final Instant T0 = Instant.now().truncatedTo(ChronoUnit.HOURS).minus(Duration.ofHours(3));

    @Autowired MockMvc mvc;
    @Autowired TokenService tokens;
    @Autowired JsonMapper json;
    @Autowired JdbcTemplate jdbc;
    @Autowired IngestQueue queue;
    @Autowired SourceService sources;
    @Autowired SocialPlatformService platforms;
    @Autowired AttentionTopicRepository topics;
    @Autowired GetSocialVolumeTool volumeTool;
    @Autowired SearchSocialPostsTool postsTool;

    String read;
    String admin;

    @BeforeEach
    void setUp() throws Exception {
        SocialTestSupport.clean(jdbc);
        sources.seedDefaults();
        platforms.seed();
        topics.deleteAll();
        AttentionTopicEntity t = new AttentionTopicEntity("red-sea", T0);
        t.setName("Red Sea");
        t.setEnabled(true);
        topics.save(t);
        read = TestTokens.bearer(TestTokens.create(tokens, "social-read", Scope.READ));
        admin = TestTokens.bearer(TestTokens.create(tokens, "social-admin", Scope.ADMIN));
        SocialPost post = new SocialPost("bluesky", "at://did:plc:a/app.bsky.feed.post/1", null,
                new SocialPost.Author("a.bsky.social", null, "did:plc:a", null, null, null),
                "Tanker hit near Bab el-Mandeb", "en", T0, null, null, null, List.of(), List.of(), List.of(),
                List.of(), null, null, null, "stream", null);
        NormalizedRecord r1 = SocialPosts.record(post, "a".repeat(64), List.of("red-sea"), T0.plusSeconds(5));
        SocialPost other = new SocialPost("x", "99", null, new SocialPost.Author("b", null, "u2", null, null, null),
                "Unrelated", null, T0.plusSeconds(60), null, null, null, List.of(), List.of(), List.of(), List.of(),
                null, null, null, "sample", null);
        NormalizedRecord r2 = SocialPosts.record(other, "b".repeat(64), List.of(), T0.plusSeconds(65));
        queue.submit("bluesky-jetstream", FetchLogEntry.ok("bluesky-jetstream", FetchTrigger.MANUAL, T0, T0),
                List.of(new IngestRecord(r1, T0, 1)), Duration.ofSeconds(5)).get(10, TimeUnit.SECONDS);
        queue.submit("x", FetchLogEntry.ok("x", FetchTrigger.MANUAL, T0, T0), List.of(new IngestRecord(r2, T0, 2)),
                Duration.ofSeconds(5)).get(10, TimeUnit.SECONDS);
        Timestamp at = Timestamp.from(Instant.now());
        for (int k = 0; k <= 8; k++) {
            Instant h = T0.minus(Duration.ofDays(7L * k));
            jdbc.update("INSERT INTO social_volume_hourly (id, scope_type, scope_id, platform, language, hour,"
                    + " collection_type, posts, unique_authors, reposts, engagement, updated_at) VALUES (?, 'topic',"
                    + " 'red-sea', 'bluesky', 'en', ?, 'complete', ?, ?, 0, 0, ?)", "topic:red-sea|bluesky|en|"
                    + h.getEpochSecond() + "|complete", Timestamp.from(h), k == 0 ? 60 : 10, k == 0 ? 30 : 8, at);
            jdbc.update("INSERT INTO social_denominator_hourly (id, platform, language, hour, posts, complete,"
                    + " updated_at) VALUES (?, 'bluesky', 'en', ?, 1000, true, ?)", "bluesky|en|"
                    + h.getEpochSecond(), Timestamp.from(h), at);
        }
        jdbc.update("INSERT INTO social_coverage (id, platform, region_code, hour, posts_seen, denominator, complete,"
                + " updated_at) VALUES (?, 'bluesky', 'YE', ?, 4, 1000, true, ?)", "bluesky|YE|" + T0.getEpochSecond(),
                Timestamp.from(T0), at);
    }

    private MockHttpServletResponse call(String bearer, MockHttpServletRequestBuilder request) throws Exception {
        if (bearer != null) {
            request = request.header(HttpHeaders.AUTHORIZATION, bearer);
        }
        return mvc.perform(request).andReturn().getResponse();
    }

    private JsonNode body(MockHttpServletResponse response) throws Exception {
        return json.readTree(response.getContentAsString(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("platforms: eight adapters, Jetstream the only default, X with its cap and prices; 404 UNKNOWN_PLATFORM")
    void platforms() throws Exception {
        JsonNode list = body(call(read, get("/v1/social/platforms")));
        assertThat(list).hasSize(8);
        assertThat(list.get(0).get("id").asString()).isEqualTo("bluesky-jetstream");
        assertThat(list.get(0).get("defaultEnabled").asBoolean()).isTrue();
        assertThat(list.get(0).get("paths").get(0).get("feedsBaselines").asBoolean()).isTrue();
        JsonNode x = body(call(read, get("/v1/social/platforms/x")));
        assertThat(x.get("state").asString()).isEqualTo("disabled");
        assertThat(x.get("spend").get("monthlyCapUsd").asDouble()).isEqualTo(25.0);
        assertThat(x.get("spend").get("pricesAsOf").asString()).isEqualTo("2026-10-17");
        assertThat(x.get("credentials").get("required").asBoolean()).isTrue();
        assertThat(x.get("terms").get("url").asString()).isEqualTo("https://docs.x.com/developer-terms/policy");
        MockHttpServletResponse missing = call(read, get("/v1/social/platforms/myspace"));
        assertThat(missing.getStatus()).isEqualTo(404);
        assertThat(body(missing).get("code").asString()).isEqualTo("UNKNOWN_PLATFORM");
    }

    @Test
    @DisplayName("PUT is admin: enabling X without credentials is credentials-required; bad mode 400; read token 403")
    void updatePlatform() throws Exception {
        String bodyJson = "{\"enabled\":true,\"mode\":\"counts\",\"monthlyCapUsd\":10,\"postRetention\":\"14d\"}";
        assertThat(call(read, put("/v1/social/platforms/x").contentType(MediaType.APPLICATION_JSON)
                .content(bodyJson)).getStatus()).isEqualTo(403);
        MockHttpServletResponse ok = call(admin, put("/v1/social/platforms/x").contentType(MediaType.APPLICATION_JSON)
                .content(bodyJson));
        assertThat(ok.getStatus()).isEqualTo(200);
        JsonNode x = body(ok);
        assertThat(x.get("state").asString()).isEqualTo("credentials-required");
        assertThat(x.get("settings").get("postRetention").asString()).isEqualTo("14d");
        assertThat(x.get("settings").get("monthlyCapUsd").asDouble()).isEqualTo(10.0);
        assertThat(sources.view("x").enabled()).as("the source stays off without its bearer token").isFalse();
        assertThat(sources.view("x").retention()).isEqualTo(Duration.ofDays(14));
        MockHttpServletResponse bad = call(admin, put("/v1/social/platforms/x").contentType(MediaType.APPLICATION_JSON)
                .content("{\"enabled\":true,\"mode\":\"everything\"}"));
        assertThat(bad.getStatus()).isEqualTo(400);
        MockHttpServletResponse off = call(admin, put("/v1/social/platforms/bluesky-jetstream")
                .contentType(MediaType.APPLICATION_JSON).content("{\"enabled\":false}"));
        assertThat(body(off).get("state").asString()).isEqualTo("disabled");
    }

    @Test
    @DisplayName("volume: the topic's hourly series with an 8-week baseline (ratio 6); exactly one scope; 404 NOT_FOUND")
    void volume() throws Exception {
        JsonNode v = body(call(read, get("/v1/social/volume").param("topic", "red-sea").param("platform", "bluesky")
                .param("since", T0.toString()).param("until", T0.plus(Duration.ofHours(1)).toString())));
        assertThat(v.get("scope").get("type").asString()).isEqualTo("topic");
        assertThat(v.get("baseline").get("name").asString()).isEqualTo("SocialBaseline");
        JsonNode series = v.get("series").get(0);
        assertThat(series.get("seriesRef").asString()).isEqualTo("social:topic:red-sea/bluesky/posts");
        assertThat(series.get("baselineEligible").asBoolean()).isTrue();
        JsonNode point = series.get("points").get(0);
        assertThat(point.get("observed").asDouble()).isEqualTo(60.0);
        assertThat(point.get("baseline").asDouble()).isEqualTo(10.0);
        assertThat(point.get("velocityRatio").asDouble()).isEqualTo(6.0);
        assertThat(point.get("complete").asBoolean()).isTrue();
        assertThat(call(read, get("/v1/social/volume")).getStatus()).isEqualTo(400);
        assertThat(call(read, get("/v1/social/volume").param("topic", "red-sea").param("place", "p")).getStatus())
                .isEqualTo(400);
        MockHttpServletResponse unknown = call(read, get("/v1/social/volume").param("topic", "no-such-topic"));
        assertThat(unknown.getStatus()).isEqualTo(404);
        assertThat(body(unknown).get("code").asString()).isEqualTo("NOT_FOUND");
        assertThat(call(read, get("/v1/social/volume").param("topic", "red-sea").param("metric", "likes"))
                .getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("coverage: hourly buckets per platform and region with the social-attention verdict")
    void coverage() throws Exception {
        JsonNode page = body(call(read, get("/v1/social/coverage").param("region", "YE").param("platform", "bluesky")
                .param("since", T0.toString()).param("until", T0.plus(Duration.ofHours(2)).toString())));
        JsonNode item = page.get("items").get(0);
        assertThat(item.get("regionCode").asString()).isEqualTo("YE");
        assertThat(item.get("buckets").get(0).get("postsSeen").asLong()).isEqualTo(4);
        assertThat(item.get("absenceOk").asBoolean()).isFalse();
        assertThat(call(read, get("/v1/social/coverage").param("region", "Yemen")).getStatus()).isEqualTo(400);
        assertThat(call(read, get("/v1/social/coverage").param("cursor", "%%%")).getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("posts: newest first with filters; one post by network and id; 404 NOT_FOUND; bad cursor 400")
    void posts() throws Exception {
        JsonNode all = body(call(read, get("/v1/social/posts")));
        assertThat(all.get("items")).hasSize(2);
        assertThat(all.get("items").get(0).get("platform").asString()).isEqualTo("x");
        JsonNode topic = body(call(read, get("/v1/social/posts").param("topic", "red-sea")));
        assertThat(topic.get("items")).hasSize(1);
        JsonNode p = topic.get("items").get(0);
        assertThat(p.get("ref").asString()).startsWith("record:bluesky-jetstream/");
        assertThat(p.get("collectionType").asString()).isEqualTo("complete");
        assertThat(p.get("deleted").asBoolean()).isFalse();
        JsonNode paged = body(call(read, get("/v1/social/posts").param("limit", "1")));
        assertThat(paged.get("nextCursor").isNull()).isFalse();
        JsonNode second = body(call(read, get("/v1/social/posts").param("limit", "1").param("cursor",
                paged.get("nextCursor").asString())));
        assertThat(second.get("items").get(0).get("platform").asString()).isEqualTo("bluesky");
        assertThat(body(call(read, get("/v1/social/posts").param("q", "bab el"))).get("items")).hasSize(1);
        assertThat(call(read, get("/v1/social/posts").param("cursor", "garbage!")).getStatus()).isEqualTo(400);
        MockHttpServletResponse one = call(read, get(java.net.URI.create("/v1/social/posts/bluesky/"
                + "at:%2F%2Fdid:plc:a%2Fapp.bsky.feed.post%2F1")));
        assertThat(one.getStatus()).isEqualTo(200);
        assertThat(body(one).get("text").asString()).isEqualTo("Tanker hit near Bab el-Mandeb");
        MockHttpServletResponse missing = call(read, get("/v1/social/posts/bluesky/nothing"));
        assertThat(missing.getStatus()).isEqualTo(404);
        assertThat(body(missing).get("code").asString()).isEqualTo("NOT_FOUND");
    }

    @Test
    @DisplayName("attention topics store and return the social block; an invalid block is 400 TOPIC_INVALID")
    void topicBlock() throws Exception {
        String topic = "{\"id\":\"hormuz-social\",\"name\":\"Hormuz\",\"enabled\":true,\"social\":{\"terms\":{\"en\":"
                + "[\"Strait of Hormuz\"]},\"hashtags\":[\"#Hormuz\"],\"mode\":\"standing\"}}";
        MockHttpServletResponse created = call(admin, post(
                "/v1/attention/topics").contentType(MediaType.APPLICATION_JSON).content(topic));
        assertThat(created.getStatus()).isEqualTo(201);
        assertThat(body(created).get("social").get("hashtags").get(0).asString()).isEqualTo("Hormuz");
        MockHttpServletResponse bad = call(admin, post(
                "/v1/attention/topics").contentType(MediaType.APPLICATION_JSON).content(
                        "{\"id\":\"bad-social\",\"name\":\"Bad\",\"social\":{\"mode\":\"sometimes\"}}"));
        assertThat(bad.getStatus()).isEqualTo(400);
        assertThat(body(bad).get("code").asString()).isEqualTo("TOPIC_INVALID");
    }

    @Test
    @DisplayName("MCP: get_social_volume and search_social_posts are read tools with these shapes")
    void mcp() throws Exception {
        ToolContext context = new ToolContext(new McpCaller(null, new ReadCaller(null, false, true), Set.of(),
                "127.0.0.1"), "session", McpSessions.STREAMABLE);
        ToolResult volume = volumeTool.call(context, json.readTree("{\"topic\":\"red-sea\",\"since\":\"" + T0
                + "\",\"until\":\"" + T0.plus(Duration.ofHours(1)) + "\"}"));
        assertThat(volume.error()).isFalse();
        assertThat(volume.structured().get("series").get(0).get("platform").asString()).isEqualTo("bluesky");
        assertThat(volumeTool.call(context, json.readTree("{}")).error()).isTrue();
        ToolResult posts = postsTool.call(context, json.readTree("{\"topic\":\"red-sea\"}"));
        assertThat(posts.error()).isFalse();
        assertThat(posts.structured().get("items")).hasSize(1);
        for (var tool : List.of(volumeTool, postsTool)) {
            assertThat(tool.requiredScope()).isEqualTo(Scope.READ);
            assertThat(tool.readOnlyHint()).isTrue();
        }
        assertThat(ai.foresignal.analyst.AnalystRunner.readToolNames()).contains("get_social_volume",
                "search_social_posts");
    }
}
