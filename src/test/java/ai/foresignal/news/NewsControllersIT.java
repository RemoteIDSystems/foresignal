package ai.foresignal.news;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.attention.AttentionTopicEntity;
import ai.foresignal.attention.AttentionTopicRepository;
import ai.foresignal.evidence.CanonicalRules;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.mcp.McpSessions;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.mcp.tools.GetNewsStoryTool;
import ai.foresignal.mcp.tools.GetOutletTool;
import ai.foresignal.mcp.tools.ListNewsStoriesTool;
import ai.foresignal.media.outlet.OutletRegistry;
import ai.foresignal.object.ObjectTypes;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.source.formats.news.ArticleUrls;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.news.NewsTestSupport;
import ai.foresignal.testsupport.security.TestTokens;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The News and Media endpoints (prompt 133): stories, the story detail grouped by outlet, articles, outlets, the admin
 * edit, providers and imports, their scopes and Problem codes, contract 1.4, and the shapes of the MCP tools
 * {@code list_news_stories}, {@code get_news_story} and {@code get_outlet}. No article carries an author name: bodies
 * and bylines can only come from EDP {@code article} documents bound later (138).
 */
@IntegrationTest
class NewsControllersIT {

    static final Instant T0 = Instant.parse("2026-10-16T08:00:00Z");

    @Autowired MockMvc mvc;
    @Autowired TokenService tokens;
    @Autowired JsonMapper json;
    @Autowired JdbcTemplate jdbc;
    @Autowired NewsStoryResolver resolver;
    @Autowired NewsStoryStore store;
    @Autowired CanonicalRules rules;
    @Autowired OutletRegistry outlets;
    @Autowired AttentionTopicRepository topics;
    @Autowired ListNewsStoriesTool listTool;
    @Autowired GetNewsStoryTool storyTool;
    @Autowired GetOutletTool outletTool;

    String read;
    String admin;
    String storyId;

    @BeforeEach
    void setUp() {
        NewsTestSupport.clean(jdbc);
        topics.deleteAll();
        AttentionTopicEntity t = new AttentionTopicEntity("red-sea", T0);
        t.setName("Red Sea");
        t.setRegionCodes(List.of("YE"));
        t.setEnabled(true);
        topics.save(t);
        outlets.seedWires();
        read = TestTokens.bearer(TestTokens.create(tokens, "news-read", Scope.READ));
        admin = TestTokens.bearer(TestTokens.create(tokens, "news-admin", Scope.ADMIN));
        String title = "Cargo ship struck by missile in the Red Sea";
        resolver.process(NewsTestSupport.rule(rules), NewsTestSupport.article(1, "https://apnews.com/article/ship-1",
                title, List.of("red-sea"), T0));
        resolver.process(NewsTestSupport.rule(rules), NewsTestSupport.article(2,
                "https://daily.example/world/ship?utm_source=x", title, List.of("red-sea"), T0.plusSeconds(60)));
        storyId = store.article("gdelt-articles", ArticleUrls.externalId("https://apnews.com/article/ship-1"))
                .orElseThrow().storyId();
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
    @DisplayName("stories: list, filters, paging, detail grouped by outlet, 404 UNKNOWN_STORY, 400 on a bad filter")
    void stories() throws Exception {
        MockHttpServletResponse list = call(read, get("/v1/news/stories").param("topic", "red-sea"));
        assertThat(list.getStatus()).isEqualTo(200);
        JsonNode items = body(list).get("items");
        assertThat(items).hasSize(1);
        assertThat(items.get(0).get("ref").asString()).isEqualTo("news-story:" + storyId);
        assertThat(items.get(0).get("evidenceGroupCount").asInt()).isEqualTo(1);
        assertThat(items.get(0).get("coverage").get("coverageBalance").get("unrated").asDouble()).isEqualTo(1.0);
        assertThat(body(call(read, get("/v1/news/stories").param("topic", "other"))).get("items")).isEmpty();
        assertThat(body(call(read, get("/v1/news/stories").param("blindspot", "any"))).get("items")).isEmpty();
        assertThat(call(read, get("/v1/news/stories").param("blindspot", "up")).getStatus()).isEqualTo(400);
        assertThat(call(read, get("/v1/news/stories").param("limit", "0")).getStatus()).isEqualTo(400);

        MockHttpServletResponse detail = call(read, get("/v1/news/stories/" + storyId));
        assertThat(detail.getStatus()).isEqualTo(200);
        JsonNode story = body(detail);
        assertThat(story.get("outlets")).hasSize(2);
        JsonNode first = story.get("outlets").get(0);
        assertThat(first.get("outlet").get("ref").asString()).startsWith("outlet:");
        JsonNode article = first.get("articles").get(0);
        assertThat(article.has("author")).isFalse();
        assertThat(article.has("authors")).isFalse();
        assertThat(article.has("byline")).isFalse();
        assertThat(story.get("canonicalEvents").toString()).contains("canonical-event:" + storyId);
        assertThat(detail.getContentAsString()).doesNotContain("utm_source");

        MockHttpServletResponse missing = call(read, get("/v1/news/stories/01NOSUCHSTORY000000000000"));
        assertThat(missing.getStatus()).isEqualTo(404);
        assertThat(missing.getContentAsString()).contains(ErrorCodes.UNKNOWN_STORY);
    }

    @Test
    @DisplayName("articles: by story and outlet, newest first, with the syndicated flag and outbound links only")
    void articles() throws Exception {
        JsonNode items = body(call(read, get("/v1/news/articles").param("story", storyId))).get("items");
        assertThat(items).hasSize(2);
        assertThat(items.get(0).get("domain").asString()).isEqualTo("daily.example");
        assertThat(items.get(0).get("syndicated").asBoolean()).isTrue();
        assertThat(items.get(1).get("syndicated").asBoolean()).isFalse();
        assertThat(items.get(0).get("url").asString()).isEqualTo("https://daily.example/world/ship");
        String outletId = items.get(1).get("outletId").asString();
        assertThat(body(call(read, get("/v1/news/articles").param("outlet", outletId))).get("items")).hasSize(1);
        MockHttpServletResponse page = call(read, get("/v1/news/articles").param("limit", "1"));
        String cursor = body(page).get("nextCursor").asString();
        assertThat(body(call(read, get("/v1/news/articles").param("limit", "1").param("cursor", cursor)))
                .get("items")).hasSize(1);
        assertThat(call(read, get("/v1/news/articles").param("cursor", "garbage")).getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("outlets: read list and detail, admin PATCH, 404 UNKNOWN_OUTLET; providers and imports are admin")
    void mediaEndpoints() throws Exception {
        assertThat(body(call(read, get("/v1/media/outlets").param("type", "wire"))).get("items").size())
                .isGreaterThan(0);
        String ap = outlets.byDomain("apnews.com").orElseThrow().id();
        JsonNode detail = body(call(read, get("/v1/media/outlets/" + ap)));
        assertThat(detail.get("type").asString()).isEqualTo("wire");
        assertThat(detail.get("ratings")).isEmpty();
        MockHttpServletResponse missing = call(read, get("/v1/media/outlets/01NOSUCHOUTLET00000000000"));
        assertThat(missing.getStatus()).isEqualTo(404);
        assertThat(missing.getContentAsString()).contains(ErrorCodes.UNKNOWN_OUTLET);

        String patchBody = "{\"ownerOrg\":\"Associated Press\"}";
        assertThat(call(read, patch("/v1/media/outlets/" + ap).contentType("application/merge-patch+json")
                .content(patchBody)).getStatus()).isEqualTo(403);
        MockHttpServletResponse patched = call(admin, patch("/v1/media/outlets/" + ap)
                .contentType("application/merge-patch+json").content(patchBody));
        assertThat(patched.getStatus()).isEqualTo(200);
        assertThat(body(patched).get("ownerOrg").asString()).isEqualTo("Associated Press");

        assertThat(call(read, get("/v1/media/providers")).getStatus()).isEqualTo(403);
        JsonNode providers = body(call(admin, get("/v1/media/providers")));
        assertThat(providers).hasSize(5);
        MockHttpServletResponse refused = call(admin, put("/v1/media/providers/allsides")
                .contentType(MediaType.APPLICATION_JSON).content("{\"enabled\":true}"));
        assertThat(refused.getStatus()).isEqualTo(409);
        assertThat(refused.getContentAsString()).contains(ErrorCodes.PROVIDER_LICENCE_NOT_ACKNOWLEDGED);
        assertThat(call(admin, put("/v1/media/providers/nope").contentType(MediaType.APPLICATION_JSON)
                .content("{}")).getStatus()).isEqualTo(404);

        String csv = "domain,dimension,value,label,licence,attribution\napnews.com,lean,0,,CC BY 4.0,Test\n";
        assertThat(call(read, post("/v1/media/ratings/import").contentType("text/csv").content(csv)).getStatus())
                .isEqualTo(403);
        MockHttpServletResponse imported = call(admin, post("/v1/media/ratings/import").param("provider", "operator")
                .contentType("text/csv").content(csv));
        assertThat(imported.getStatus()).isEqualTo(200);
        assertThat(body(imported).get("imported").asInt()).isEqualTo(1);
        MockHttpServletResponse viaJson = call(admin, post("/v1/media/ratings/import").contentType(
                MediaType.APPLICATION_JSON).content("{\"rows\":[{\"domain\":\"apnews.com\",\"dimension\":\"factuality\","
                + "\"value\":\"5\",\"licence\":\"CC BY 4.0\",\"attribution\":\"Test\"}]}"));
        assertThat(body(viaJson).get("imported").asInt()).isEqualTo(1);
        MockHttpServletResponse viaFile = mvc.perform(multipart("/v1/media/ratings/import").file(
                new MockMultipartFile("file", "ratings.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8)))
                .header(HttpHeaders.AUTHORIZATION, admin)).andReturn().getResponse();
        assertThat(body(viaFile).get("updated").asInt()).isEqualTo(1);
        MockHttpServletResponse invalid = call(admin, post("/v1/media/ratings/import").contentType("text/csv")
                .content("nonsense"));
        assertThat(invalid.getStatus()).isEqualTo(400);
        assertThat(invalid.getContentAsString()).contains(ErrorCodes.RATINGS_IMPORT_INVALID);
        JsonNode rated = body(call(read, get("/v1/media/outlets/" + ap)));
        assertThat(rated.get("ratings")).hasSize(2);
        assertThat(rated.get("consensus").toString()).contains("\"bucket\":\"center\"");
    }

    @Test
    @DisplayName("contract 1.4 lists news-story and outlet, and both resolve as objects")
    void contract() throws Exception {
        JsonNode contract = body(call(read, get("/v1/contract")));
        assertThat(contract.get("contractVersion").asString()).isEqualTo("1.4");
        assertThat(contract.get("objectTypes").toString()).contains("news-story").contains("outlet");
        assertThat(ObjectTypes.ALL).contains(ObjectTypes.NEWS_STORY, ObjectTypes.OUTLET);
        MockHttpServletResponse story = call(read, get("/v1/objects/news-story:" + storyId));
        assertThat(story.getStatus()).isEqualTo(200);
        assertThat(body(story).get("title").asString()).contains("Cargo ship");
        String ap = outlets.byDomain("apnews.com").orElseThrow().id();
        assertThat(call(read, get("/v1/objects/outlet:" + ap)).getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("MCP: list_news_stories, get_news_story and get_outlet are read tools with these shapes")
    void mcp() throws Exception {
        ToolContext context = new ToolContext(new McpCaller(null, new ReadCaller(null, false, true), Set.of(),
                "127.0.0.1"), "session", McpSessions.STREAMABLE);
        ToolResult list = listTool.call(context, json.readTree("{\"topic\":\"red-sea\"}"));
        assertThat(list.error()).isFalse();
        assertThat(list.structured().get("items").get(0).get("id").asString()).isEqualTo(storyId);
        ToolResult story = storyTool.call(context, json.readTree("{\"storyId\":\"news-story:" + storyId + "\"}"));
        assertThat(story.error()).isFalse();
        assertThat(story.structured().get("outlets")).hasSize(2);
        assertThat(storyTool.call(context, json.readTree("{\"storyId\":\"01NOPE\"}")).error()).isTrue();
        ToolResult outlet = outletTool.call(context, json.readTree("{\"domain\":\"apnews.com\"}"));
        assertThat(outlet.error()).isFalse();
        assertThat(outlet.structured().get("originatorId").asString()).isEqualTo("ap");
        assertThat(outletTool.call(context, json.readTree("{}")).error()).isTrue();
        for (var tool : List.of(listTool, storyTool, outletTool)) {
            assertThat(tool.requiredScope()).isEqualTo(Scope.READ);
            assertThat(tool.readOnlyHint()).isTrue();
        }
        assertThat(ai.foresignal.analyst.AnalystRunner.readToolNames()).contains("list_news_stories",
                "get_news_story", "get_outlet");
    }
}
