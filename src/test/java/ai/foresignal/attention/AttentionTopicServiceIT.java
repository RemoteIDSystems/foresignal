package ai.foresignal.attention;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.mcp.McpSessions;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.mcp.tools.ListAttentionTopicsTool;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.adapters.gdelt.GdeltTimelineAdapter;
import ai.foresignal.source.adapters.wikimedia.WikimediaPageviewsAdapter;
import ai.foresignal.source.formats.attention.TopicParams;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.security.TestTokens;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Attention topics end to end (prompt 117): CRUD, validation, the shipped examples, and the sync of the enabled
 * topics (never the disabled ones) into the {@code topics} parameter of {@code gdelt-timelines} and
 * {@code wikimedia-pageviews}, keeping the sources' other parameters. The REST scopes and the MCP shape are checked
 * too. {@code gdelt-timelines} stays disabled, so no GDELT test call is made.
 */
@IntegrationTest
class AttentionTopicServiceIT {

    @Autowired AttentionTopicService service;
    @Autowired AttentionTopicRepository repository;
    @Autowired SourceService sources;
    @Autowired AppSettingService settings;
    @Autowired ListAttentionTopicsTool tool;
    @Autowired MockMvc mvc;
    @Autowired TokenService tokens;
    @Autowired JsonMapper json;

    String read;
    String agent;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        sources.update(GdeltTimelineAdapter.ID, SourceSettingsUpdate.params(Map.of()));
        sources.update(WikimediaPageviewsAdapter.ID, SourceSettingsUpdate.params(Map.of()));
        read = TestTokens.bearer(TestTokens.create(tokens, "attention-read", Scope.READ));
        agent = TestTokens.bearer(TestTokens.create(tokens, "attention-agent", Scope.AGENT));
    }

    private static AttentionTopicInput topic(String id, boolean enabled) {
        return new AttentionTopicInput(id, "Topic " + id, "A test topic", "\"Strait of Hormuz\"",
                List.of(new AttentionTopicInput.WikiArticle("en.wikipedia", "Strait of Hormuz")),
                List.of("Hormuz"), List.of("IR", "OM"), List.of("energy"), enabled, null);
    }

    @SuppressWarnings("unchecked")
    private List<String> topicsParam(String sourceId) {
        Object value = sources.storedParams(sourceId).get(TopicParams.PARAM);
        return value == null ? List.of() : ((List<Object>) value).stream().map(String::valueOf).toList();
    }

    @Test
    @DisplayName("create, read, list, replace and delete a topic")
    void crud() {
        AttentionTopicEntity created = service.create(topic("hormuz-test", false), "token-1");
        assertThat(created.getCreatedBy()).isEqualTo("token-1");
        assertThat(created.getWikiArticles()).containsExactly(Map.of("project", "en.wikipedia",
                "article", "Strait_of_Hormuz"));
        assertThat(service.get("hormuz-test").getName()).isEqualTo("Topic hormuz-test");
        assertThat(service.list(null, null, 100).items()).extracting(AttentionTopicEntity::getId)
                .containsExactly("hormuz-test");

        AttentionTopicInput changed = new AttentionTopicInput("hormuz-test", "Hormuz", null, null, List.of(),
                List.of(), List.of(), List.of(), true, null);
        AttentionTopicEntity updated = service.update("hormuz-test", changed);
        assertThat(updated.getName()).isEqualTo("Hormuz");
        assertThat(updated.getGdeltQuery()).isNull();
        assertThat(updated.isEnabled()).isTrue();

        assertThatThrownBy(() -> service.create(topic("hormuz-test", false), null))
                .isInstanceOf(ConflictException.class);
        service.delete("hormuz-test");
        assertThatThrownBy(() -> service.get("hormuz-test")).isInstanceOf(NotFoundException.class)
                .hasMessageContaining("hormuz-test");
    }

    @Test
    @DisplayName("validation: id pattern, name, query characters, articles, keywords, region codes, path id")
    void validation() {
        AttentionTopicInput badId = new AttentionTopicInput("Bad Id", "x", null, null, null, null, null, null, null,
                null);
        assertThatThrownBy(() -> service.create(badId, null)).isInstanceOfSatisfying(ValidationException.class,
                e -> assertThat(e.getErrors()).extracting(f -> f.field()).contains("id"));
        AttentionTopicInput badQuery = new AttentionTopicInput("bad-query", "x", null, "(\"unbalanced", null, null,
                null, null, null, null);
        assertThatThrownBy(() -> service.create(badQuery, null)).isInstanceOfSatisfying(ValidationException.class,
                e -> assertThat(e.getErrors()).extracting(f -> f.field()).contains("gdeltQuery"));
        AttentionTopicInput longQuery = new AttentionTopicInput("long-query", "x", null, "a".repeat(251), null, null,
                null, null, null, null);
        assertThatThrownBy(() -> service.create(longQuery, null)).isInstanceOf(ValidationException.class);
        AttentionTopicInput badArticle = new AttentionTopicInput("bad-article", "x", null, null,
                List.of(new AttentionTopicInput.WikiArticle("wikipedia.org", "X")), null, null, null, null, null);
        assertThatThrownBy(() -> service.create(badArticle, null)).isInstanceOf(ValidationException.class);
        List<AttentionTopicInput.WikiArticle> tooMany = new java.util.ArrayList<>();
        for (int i = 0; i < 21; i++) {
            tooMany.add(new AttentionTopicInput.WikiArticle("en.wikipedia", "Article_" + i));
        }
        assertThatThrownBy(() -> service.create(new AttentionTopicInput("too-many", "x", null, null, tooMany, null,
                null, null, null, null), null)).isInstanceOf(ValidationException.class);
        List<String> keywords = java.util.Collections.nCopies(31, "k");
        assertThatThrownBy(() -> service.create(new AttentionTopicInput("keywords", "x", null, null, null, keywords,
                null, null, null, null), null)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> service.create(new AttentionTopicInput("regions", "x", null, null, null, null,
                List.of("Yemen"), null, null, null), null)).isInstanceOf(ValidationException.class);
        service.create(topic("path-id", false), null);
        assertThatThrownBy(() -> service.update("path-id", topic("other-id", false)))
                .isInstanceOf(ValidationException.class);
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("enabled topics are synced into both sources' topics params; disabled ones are left out")
    void sync() {
        sources.update(GdeltTimelineAdapter.ID, SourceSettingsUpdate.params(Map.of(TopicParams.PARAM, List.of())));
        service.create(topic("enabled-one", true), null);
        service.create(topic("disabled-one", false), null);

        List<TopicParams.GdeltTopic> gdelt = TopicParams.gdelt(topicsParam(GdeltTimelineAdapter.ID));
        assertThat(gdelt).containsExactly(new TopicParams.GdeltTopic("enabled-one", "\"Strait of Hormuz\""));
        List<TopicParams.WikiTopic> wiki = TopicParams.wiki(topicsParam(WikimediaPageviewsAdapter.ID));
        assertThat(wiki).extracting(TopicParams.WikiTopic::id).containsExactly("enabled-one");
        assertThat(wiki.get(0).articles()).containsExactly(new TopicParams.WikiArticle("en.wikipedia",
                "Strait_of_Hormuz"));
        // The params pass the sources' own schemas.
        assertThat(sources.effectiveParams(GdeltTimelineAdapter.ID).stringList(TopicParams.PARAM)).hasSize(1);

        service.update("disabled-one", topic("disabled-one", true));
        assertThat(TopicParams.gdelt(topicsParam(GdeltTimelineAdapter.ID))).extracting(TopicParams.GdeltTopic::id)
                .containsExactly("disabled-one", "enabled-one");
        service.delete("enabled-one");
        assertThat(TopicParams.wiki(topicsParam(WikimediaPageviewsAdapter.ID)))
                .extracting(TopicParams.WikiTopic::id).containsExactly("disabled-one");
    }

    @Test
    @DisplayName("the sync keeps the sources' other stored parameters")
    void keepsOtherParams() {
        Map<String, Object> before = sources.storedParams(WikimediaPageviewsAdapter.ID);
        service.create(topic("keep-params", true), null);
        Map<String, Object> after = sources.storedParams(WikimediaPageviewsAdapter.ID);
        before.forEach((key, value) -> {
            if (!key.equals(TopicParams.PARAM)) {
                assertThat(after).containsEntry(key, value);
            }
        });
        assertThat(after).containsKey(TopicParams.PARAM);
    }

    @Test
    @DisplayName("the five examples are created disabled once, and not again after the setting is recorded")
    void examples() {
        settings.put(SettingKeys.ATTENTION_SEEDED, Boolean.FALSE);
        service.seedExamples();
        assertThat(repository.findAllByOrderByIdAsc()).extracting(AttentionTopicEntity::getId).containsExactly(
                "gulf-energy-infrastructure", "red-sea-shipping", "strait-of-hormuz", "taiwan-strait",
                "ukraine-front");
        assertThat(repository.findAll()).noneMatch(AttentionTopicEntity::isEnabled);
        repository.deleteAll();
        service.seedExamples();
        assertThat(repository.count()).isZero();
        for (AttentionTopicInput example : AttentionTopicExamples.ALL) {
            service.validate(example, null);
        }
    }

    private MockHttpServletResponse call(String bearer, MockHttpServletRequestBuilder request) throws Exception {
        if (bearer != null) {
            request = request.header(HttpHeaders.AUTHORIZATION, bearer);
        }
        return mvc.perform(request).andReturn().getResponse();
    }

    @Test
    @DisplayName("REST: reads are read scope, changes agent; 201, 204, 404 UNKNOWN_ATTENTION_TOPIC, 400 TOPIC_INVALID")
    void rest() throws Exception {
        String body = json.writeValueAsString(topic("rest-topic", false));
        assertThat(call(read, post("/v1/attention/topics").contentType(MediaType.APPLICATION_JSON).content(body))
                .getStatus()).isEqualTo(403);
        MockHttpServletResponse created = call(agent, post("/v1/attention/topics")
                .contentType(MediaType.APPLICATION_JSON).content(body));
        assertThat(created.getStatus()).isEqualTo(201);
        JsonNode view = json.readTree(created.getContentAsString());
        assertThat(view.get("id").asString()).isEqualTo("rest-topic");
        assertThat(view.get("createdAt").isString()).isTrue();

        MockHttpServletResponse list = call(read, get("/v1/attention/topics").param("enabled", "false"));
        assertThat(list.getStatus()).isEqualTo(200);
        assertThat(json.readTree(list.getContentAsString()).get("items").get(0).get("id").asString())
                .isEqualTo("rest-topic");
        assertThat(call(read, get("/v1/attention/topics/rest-topic")).getStatus()).isEqualTo(200);
        MockHttpServletResponse missing = call(read, get("/v1/attention/topics/no-such-topic"));
        assertThat(missing.getStatus()).isEqualTo(404);
        assertThat(missing.getContentAsString()).contains(ErrorCodes.UNKNOWN_ATTENTION_TOPIC);
        MockHttpServletResponse invalid = call(agent, put("/v1/attention/topics/rest-topic")
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(topic("other", false))));
        assertThat(invalid.getStatus()).isEqualTo(400);
        assertThat(invalid.getContentAsString()).contains(ErrorCodes.TOPIC_INVALID);
        assertThat(call(agent, delete("/v1/attention/topics/rest-topic")).getStatus()).isEqualTo(204);
    }

    @Test
    @DisplayName("MCP list_attention_topics returns the topics for a read caller")
    void mcp() throws Exception {
        service.create(topic("mcp-topic", true), null);
        ToolContext context = new ToolContext(new McpCaller(null, new ReadCaller(null, false, true), Set.of(),
                "127.0.0.1"), "session", McpSessions.STREAMABLE);
        ToolResult result = tool.call(context, json.readTree("{\"enabled\":true}"));
        assertThat(result.error()).isFalse();
        assertThat(result.structured().get("items").get(0).get("id").asString()).isEqualTo("mcp-topic");
        assertThat(tool.requiredScope()).isEqualTo(Scope.READ);
        assertThat(tool.readOnlyHint()).isTrue();
    }
}
