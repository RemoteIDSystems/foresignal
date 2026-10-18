package ai.foresignal.inbound.edp.binding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.forecast.ForecasterRegistry;
import ai.foresignal.inbound.edp.ExternalEvent;
import ai.foresignal.inbound.edp.ExternalEventBus;
import ai.foresignal.inbound.edp.ExternalSourceChangedEvent;
import ai.foresignal.inbound.edp.ExternalTopic;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.mcp.McpSessions;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.mcp.tools.GetExternalSourceTool;
import ai.foresignal.mcp.tools.ListExternalSourcesTool;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.collect.CollectorTestSources;
import ai.foresignal.testsupport.inbound.EdpConformance;
import ai.foresignal.testsupport.security.TestTokens;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The external binding endpoints of prompt 138 over MockMvc: bindings (list, create, get, replace, delete, accept
 * suggested, preview without side effects), question links (list, link, unlink), their scopes and Problem codes
 * ({@code UNKNOWN_BINDING}, {@code UNKNOWN_EXTERNAL_SOURCE}, {@code BINDING_TARGET_INVALID},
 * {@code QUESTION_ALREADY_LINKED}, {@code UNKNOWN_QUESTION}, {@code UNKNOWN_QUESTION_LINK}), the binding counts of the
 * {@code ExternalSource} view, the MCP tools {@code list_external_sources} and {@code get_external_source}, and the
 * SSE topic {@code external}.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(CollectorTestSources.class)
class ExternalBindingControllersIT {

    static final Instant T = Instant.parse("2026-10-18T06:00:00Z");
    static final String MISSING = "01JA8Q3V7S2K9W4T6Y1B5N0M9Z";

    @Autowired MockMvc mvc;
    @Autowired TokenService tokens;
    @Autowired JsonMapper json;
    @Autowired MutableClock clock;
    @Autowired AppSettingService settings;
    @Autowired JdbcTemplate jdbc;
    @Autowired ForecasterRegistry forecasters;
    @Autowired ListExternalSourcesTool listTool;
    @Autowired GetExternalSourceTool getTool;
    @Autowired ExternalTopic topic;
    @Autowired ExternalEventBus events;

    String read;
    String agent;
    String admin;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        settings.invalidateCache();
        forecasters.registerBuiltIns();
        read = TestTokens.bearer(TestTokens.create(tokens, "xb-read", Scope.READ));
        agent = TestTokens.bearer(TestTokens.create(tokens, "xb-agent", Scope.AGENT));
        admin = TestTokens.bearer(TestTokens.create(tokens, "xb-admin", Scope.ADMIN));
    }

    MockHttpServletResponse call(String bearer, MockHttpServletRequestBuilder request) throws Exception {
        if (bearer != null) {
            request = request.header(HttpHeaders.AUTHORIZATION, bearer);
        }
        return mvc.perform(request).andReturn().getResponse();
    }

    MockHttpServletResponse send(String bearer, MockHttpServletRequestBuilder request, String body) throws Exception {
        return call(bearer, request.contentType(MediaType.APPLICATION_JSON).content(body));
    }

    JsonNode body(MockHttpServletResponse response) throws Exception {
        return json.readTree(response.getContentAsString(StandardCharsets.UTF_8));
    }

    String code(MockHttpServletResponse response) throws Exception {
        return body(response).get("code").asString();
    }

    /** Registers an approved provider from {@code manifest}; returns its ingest key. */
    String register(String manifest) throws Exception {
        MockHttpServletResponse response = send(admin, post("/v1/external/sources"),
                "{\"manifest\":" + manifest + ",\"approve\":true}");
        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(201);
        return body(response).get("ingestKey").asString();
    }

    /** Registers the fixture provider; returns its source id. */
    String fixture() throws Exception {
        String id = BindingItFixtures.newProvider("dock");
        register(BindingItFixtures.manifest(id, "dock-level"));
        return "ext-" + id;
    }

    @Test
    @DisplayName("bindings: create, list, get, replace and delete with admin; BINDING_TARGET_INVALID, UNKNOWN_* and scopes")
    void bindings() throws Exception {
        String source = fixture();
        String base = "/v1/external/sources/" + source + "/bindings";
        assertThat(body(call(admin, get(base)))).isEmpty();

        MockHttpServletResponse created = send(admin, post(base), "{\"streamId\":\"levels\",\"target\":\"series\","
                + "\"config\":{\"unit\":\"m\"},\"backfill\":false}");
        assertThat(created.getStatus()).as(created.getContentAsString()).isEqualTo(201);
        JsonNode b = body(created);
        String id = b.get("id").asString();
        assertThat(b.get("sourceId").asString()).isEqualTo(source);
        assertThat(b.get("streamId").asString()).isEqualTo("levels");
        assertThat(b.get("target").asString()).isEqualTo("series");
        assertThat(b.get("state").asString()).isEqualTo("active");
        assertThat(b.get("enabled").asBoolean()).isTrue();
        assertThat(b.get("applied").asLong()).isZero();
        assertThat(b.get("manifestSha256").asString()).hasSize(64);
        assertThat(b.get("createdAt").isNull()).isFalse();
        assertThat(b.get("createdBy").asString()).isNotBlank();

        assertThat(body(call(admin, get(base)))).hasSize(1);
        assertThat(body(call(admin, get("/v1/external/bindings/" + id))).get("config").get("unit").asString())
                .isEqualTo("m");
        JsonNode view = body(call(admin, get("/v1/external/sources/" + source)));
        for (JsonNode stream : view.get("streams")) {
            assertThat(stream.get("bindings").asLong()).as(stream.toString())
                    .isEqualTo("levels".equals(stream.get("streamId").asString()) ? 1 : 0);
        }

        MockHttpServletResponse replaced = send(admin, put("/v1/external/bindings/" + id), "{\"streamId\":\"levels\","
                + "\"target\":\"series\",\"config\":{\"unit\":\"cm\",\"transform\":\"diff\"},\"backfill\":false}");
        assertThat(replaced.getStatus()).as(replaced.getContentAsString()).isEqualTo(200);
        assertThat(body(replaced).get("config").get("transform").asString()).isEqualTo("diff");
        MockHttpServletResponse disabled = send(admin, put("/v1/external/bindings/" + id), "{\"streamId\":\"levels\","
                + "\"target\":\"series\",\"enabled\":false}");
        assertThat(body(disabled).get("state").asString()).isEqualTo("disabled");

        MockHttpServletResponse wrongType = send(admin, post(base), "{\"streamId\":\"levels\",\"target\":\"forecast\"}");
        assertThat(wrongType.getStatus()).isEqualTo(400);
        assertThat(code(wrongType)).isEqualTo("BINDING_TARGET_INVALID");
        assertThat(body(wrongType).get("detail").asString()).contains("observation", "forecast");
        MockHttpServletResponse wrongDocument = send(admin, post(base), "{\"streamId\":\"notes\",\"target\":\"social\"}");
        assertThat(code(wrongDocument)).isEqualTo("BINDING_TARGET_INVALID");
        assertThat(body(wrongDocument).get("detail").asString()).contains("note", "social-post");
        assertThat(code(send(admin, post(base), "{\"streamId\":\"nope\",\"target\":\"series\"}")))
                .isEqualTo("BINDING_TARGET_INVALID");
        assertThat(code(send(admin, post(base), "{\"streamId\":\"levels\",\"target\":\"nowhere\"}")))
                .isEqualTo("BINDING_TARGET_INVALID");
        assertThat(code(send(admin, post(base), "{\"streamId\":\"levels\",\"target\":\"series\","
                + "\"config\":{\"transform\":\"square\"}}"))).isEqualTo("BINDING_TARGET_INVALID");
        assertThat(code(send(admin, post(base), "{\"streamId\":\"incidents\",\"target\":\"canonical-family\"}")))
                .isEqualTo("BINDING_TARGET_INVALID");
        assertThat(code(send(admin, post(base), "{\"streamId\":\"notes\",\"target\":\"evidence\","
                + "\"config\":{\"hypothesisSetId\":\"" + MISSING + "\"}}"))).isEqualTo("BINDING_TARGET_INVALID");
        assertThat(code(send(admin, post(base), "{\"streamId\":\"levels\",\"target\":\"series\","
                + "\"filter\":[{\"path\":\"value\",\"op\":\"no-such-op\",\"value\":1}]}")))
                .isEqualTo("BINDING_TARGET_INVALID");

        MockHttpServletResponse unknownSource = call(admin, get("/v1/external/sources/ext-nope-zz/bindings"));
        assertThat(unknownSource.getStatus()).isEqualTo(404);
        assertThat(code(unknownSource)).isEqualTo("UNKNOWN_EXTERNAL_SOURCE");
        MockHttpServletResponse unknown = call(admin, get("/v1/external/bindings/" + MISSING));
        assertThat(unknown.getStatus()).isEqualTo(404);
        assertThat(code(unknown)).isEqualTo("UNKNOWN_BINDING");
        assertThat(code(call(admin, post("/v1/external/bindings/" + MISSING + "/preview")))).isEqualTo("UNKNOWN_BINDING");

        assertThat(call(agent, get(base)).getStatus()).isEqualTo(403);
        assertThat(call(read, get("/v1/external/bindings/" + id)).getStatus()).isEqualTo(403);
        assertThat(send(agent, post(base), "{\"streamId\":\"levels\",\"target\":\"series\"}").getStatus())
                .isEqualTo(403);
        assertThat(call(null, get(base)).getStatus()).isEqualTo(401);

        assertThat(call(admin, delete("/v1/external/bindings/" + id)).getStatus()).isEqualTo(204);
        assertThat(call(admin, get("/v1/external/bindings/" + id)).getStatus()).isEqualTo(404);
        assertThat(call(admin, delete("/v1/external/bindings/" + id)).getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("accept-suggested creates the manifest's bindings once; an incomplete suggestion is created disabled in error")
    void acceptSuggested() throws Exception {
        register(EdpConformance.manifest("hormuz-example.json"));
        String path = "/v1/external/sources/ext-hormuz-transits/bindings/accept-suggested";
        MockHttpServletResponse accepted = call(admin, post(path));
        assertThat(accepted.getStatus()).as(accepted.getContentAsString()).isEqualTo(201);
        List<String> targets = new ArrayList<>();
        for (JsonNode b : body(accepted)) {
            targets.add(b.get("target").asString());
            assertThat(b.get("enabled").asBoolean()).isTrue();
        }
        assertThat(targets).containsExactlyInAnyOrder("series", "canonical-family");
        assertThat(body(call(admin, post(path)))).as("duplicates are skipped").isEmpty();
        assertThat(call(agent, post(path)).getStatus()).isEqualTo(403);

        String source = fixture();
        JsonNode created = body(call(admin, post("/v1/external/sources/" + source + "/bindings/accept-suggested")));
        assertThat(created).hasSize(3);
        JsonNode evidence = null;
        for (JsonNode b : created) {
            if ("evidence".equals(b.get("target").asString())) {
                evidence = b;
            }
        }
        assertThat(evidence).isNotNull();
        assertThat(evidence.get("enabled").asBoolean()).isFalse();
        assertThat(evidence.get("state").asString()).isEqualTo("error");
        assertThat(evidence.get("lastError").asString()).contains("hypothesisSetId");
    }

    @Test
    @DisplayName("preview evaluates the last items without side effects; a filter that matches nothing previews nothing")
    void preview() throws Exception {
        String id = BindingItFixtures.newProvider("dock");
        String key = register(BindingItFixtures.manifest(id, "dock-level"));
        String source = "ext-" + id;
        MockHttpServletResponse pushed = mvc.perform(post("/v1/external/sources/" + source + "/items")
                .header("X-Foresignal-Ingest-Key", key).contentType(MediaType.APPLICATION_JSON)
                .content(BindingItFixtures.everything())).andReturn().getResponse();
        assertThat(pushed.getStatus()).as(pushed.getContentAsString()).isEqualTo(200);
        String base = "/v1/external/sources/" + source + "/bindings";

        String social = body(send(admin, post(base), "{\"streamId\":\"posts\",\"target\":\"social\","
                + "\"backfill\":false}")).get("id").asString();
        long recordsBefore = jdbc.queryForObject("select count(*) from record_version where source_id = ?",
                Long.class, source);
        MockHttpServletResponse previewed = call(admin, post("/v1/external/bindings/" + social + "/preview"));
        assertThat(previewed.getStatus()).as(previewed.getContentAsString()).isEqualTo(200);
        JsonNode p = body(previewed);
        assertThat(p.get("evaluated").asInt()).isEqualTo(1);
        assertThat(p.get("matched").asInt()).isEqualTo(1);
        assertThat(p.get("effects").get(0).get("action").asString()).isEqualTo("social-post");
        assertThat(p.get("effects").get(0).get("itemRef").asString()).startsWith("record:" + source + "/");
        assertThat(p.get("effects").get(0).get("targetRef").asString()).contains("telegram");
        assertThat(p.get("errors")).isEmpty();

        assertThat(jdbc.queryForObject("select count(*) from record_version where source_id = ?", Long.class, source))
                .as("no record written").isEqualTo(recordsBefore);
        assertThat(jdbc.queryForObject("select count(*) from external_binding_applied where binding_id = ?",
                Long.class, social)).isZero();
        assertThat(body(call(admin, get("/v1/external/bindings/" + social))).get("applied").asLong()).isZero();

        String filtered = body(send(admin, post(base), "{\"streamId\":\"articles\",\"target\":\"news\","
                + "\"backfill\":false,\"filter\":[{\"path\":\"language\",\"op\":\"==\",\"value\":\"fr\"}]}"))
                .get("id").asString();
        JsonNode none = body(call(admin, post("/v1/external/bindings/" + filtered + "/preview")));
        assertThat(none.get("evaluated").asInt()).isEqualTo(1);
        assertThat(none.get("matched").asInt()).isZero();
        assertThat(none.get("effects")).isEmpty();
        assertThat(call(agent, post("/v1/external/bindings/" + social + "/preview")).getStatus()).isEqualTo(403);
    }

    @Test
    @DisplayName("question links: read lists, agent links and unlinks; 409, 404 and 400 problem codes")
    void questionLinks() throws Exception {
        String source = fixture();
        String path = "/v1/external/sources/" + source + "/question-links";
        MockHttpServletResponse binary = send(agent, post("/v1/forecasts/questions"), BindingItFixtures.binaryQuestion());
        assertThat(binary.getStatus()).as(binary.getContentAsString()).isEqualTo(201);
        String binaryId = body(binary).get("id").asString();
        MockHttpServletResponse numeric = send(agent, post("/v1/forecasts/questions"), BindingItFixtures.numericQuestion());
        assertThat(numeric.getStatus()).as(numeric.getContentAsString()).isEqualTo(201);
        String numericId = body(numeric).get("id").asString();

        assertThat(body(call(read, get(path)))).isEmpty();
        String input = "{\"streamId\":\"odds\",\"externalQuestionId\":\"q-1\",\"questionId\":\"" + binaryId + "\"}";
        assertThat(send(read, post(path), input).getStatus()).isEqualTo(403);
        assertThat(send(null, post(path), input).getStatus()).isEqualTo(401);
        MockHttpServletResponse linked = send(agent, post(path), input);
        assertThat(linked.getStatus()).as(linked.getContentAsString()).isEqualTo(201);
        JsonNode link = body(linked);
        assertThat(link.get("forecasterId").asString()).isEqualTo("edp:" + source + "/odds");
        assertThat(link.get("orientation").asString()).isEqualTo("same");
        assertThat(link.get("questionId").asString()).isEqualTo(binaryId);
        assertThat(link.get("lastForecastAt").isNull()).isTrue();

        MockHttpServletResponse duplicate = send(agent, post(path), input);
        assertThat(duplicate.getStatus()).isEqualTo(409);
        assertThat(code(duplicate)).isEqualTo("QUESTION_ALREADY_LINKED");
        MockHttpServletResponse unknownQuestion = send(agent, post(path), "{\"streamId\":\"odds\","
                + "\"externalQuestionId\":\"q-2\",\"questionId\":\"" + MISSING + "\"}");
        assertThat(unknownQuestion.getStatus()).isEqualTo(404);
        assertThat(code(unknownQuestion)).isEqualTo("UNKNOWN_QUESTION");
        MockHttpServletResponse notForecast = send(agent, post(path), "{\"streamId\":\"levels\","
                + "\"externalQuestionId\":\"q-3\",\"questionId\":\"" + binaryId + "\"}");
        assertThat(notForecast.getStatus()).isEqualTo(400);
        assertThat(code(notForecast)).isEqualTo("BINDING_TARGET_INVALID");
        MockHttpServletResponse inverseNumeric = send(agent, post(path), "{\"streamId\":\"odds\","
                + "\"externalQuestionId\":\"q-4\",\"questionId\":\"" + numericId + "\",\"orientation\":\"inverse\"}");
        assertThat(inverseNumeric.getStatus()).isEqualTo(400);
        assertThat(code(inverseNumeric)).isEqualTo("BINDING_TARGET_INVALID");
        assertThat(send(agent, post("/v1/external/sources/ext-nope-zz/question-links"), input).getStatus())
                .isEqualTo(404);

        assertThat(body(call(read, get(path)))).hasSize(1);
        String linkId = link.get("id").asString();
        assertThat(call(read, delete("/v1/external/question-links/" + linkId)).getStatus()).isEqualTo(403);
        assertThat(call(agent, delete("/v1/external/question-links/" + linkId)).getStatus()).isEqualTo(204);
        MockHttpServletResponse gone = call(agent, delete("/v1/external/question-links/" + linkId));
        assertThat(gone.getStatus()).isEqualTo(404);
        assertThat(code(gone)).isEqualTo("UNKNOWN_QUESTION_LINK");
        assertThat(body(call(read, get(path)))).isEmpty();
    }

    @Test
    @DisplayName("MCP list_external_sources and get_external_source are read summaries; the external topic is admin")
    void mcpAndTopic() throws Exception {
        String source = fixture();
        assertThat(send(admin, post("/v1/external/sources/" + source + "/bindings"),
                "{\"streamId\":\"levels\",\"target\":\"series\",\"backfill\":false}").getStatus()).isEqualTo(201);
        assertThat(listTool.requiredScope()).isEqualTo(Scope.READ);
        assertThat(getTool.requiredScope()).isEqualTo(Scope.READ);
        assertThat(listTool.name()).isEqualTo("list_external_sources");
        assertThat(getTool.name()).isEqualTo("get_external_source");

        ToolResult list = listTool.call(context(Set.of()), json.readTree("{}"));
        assertThat(list.error()).isFalse();
        JsonNode mine = null;
        for (JsonNode item : list.structured().get("items")) {
            assertThat(item.has("manifest")).isFalse();
            if (source.equals(item.get("sourceId").asString())) {
                mine = item;
            }
        }
        assertThat(mine).isNotNull();
        assertThat(mine.get("state").asString()).isEqualTo("active");
        assertThat(mine.get("accessRoute").asString()).isEqualTo("scrape");
        assertThat(mine.get("originator").asString()).isEqualTo("example-harbor-lab");
        assertThat(mine.get("streams")).hasSize(7);
        assertThat(listTool.call(context(Set.of()), json.readTree("{\"state\":\"bogus\"}")).error()).isTrue();
        assertThat(listTool.call(context(Set.of()), json.readTree("{\"state\":\"pending\"}")).structured()
                .get("items").findValues("sourceId").stream().map(JsonNode::asString)).doesNotContain(source);

        ToolResult one = getTool.call(context(Set.of()), json.readTree("{\"sourceId\":\"" + source + "\"}"));
        assertThat(one.error()).isFalse();
        assertThat(one.structured().has("manifest")).isFalse();
        JsonNode levels = null;
        for (JsonNode s : one.structured().get("streams")) {
            if ("levels".equals(s.get("streamId").asString())) {
                levels = s;
            }
        }
        assertThat(levels.get("bindings").asLong()).isEqualTo(1);
        assertThat(levels.get("suggestedBindings").get(0).get("target").asString()).isEqualTo("series");
        assertThat(getTool.call(context(Set.of()), json.readTree("{}")).error()).isTrue();
        assertThat(getTool.call(context(Set.of()), json.readTree("{\"sourceId\":\"ext-nope-zz\"}")).error()).isTrue();

        // A private provider the caller may not see is left out and unknown.
        ToolResult hidden = listTool.call(context(Set.of(source)), json.readTree("{}"));
        assertThat(hidden.structured().get("items").findValues("sourceId").stream().map(JsonNode::asString))
                .doesNotContain(source);
        assertThat(getTool.call(context(Set.of(source)), json.readTree("{\"sourceId\":\"" + source + "\"}")).error())
                .isTrue();

        assertThat(topic.name()).isEqualTo("external");
        assertThat(topic.cursorKey()).isEqualTo("x");
        assertThat(topic.requiredScope()).isEqualTo(Scope.ADMIN);
        List<ExternalEvent> seen = new CopyOnWriteArrayList<>();
        AutoCloseable handle = events.subscribe(seen::add);
        try {
            events.onSourceChanged(ExternalSourceChangedEvent.of(source, "suspended",
                    ExternalSourceChangedEvent.SUSPENDED, T));
        } finally {
            handle.close();
        }
        assertThat(seen).hasSize(1);
        assertThat(seen.get(0).type()).isEqualTo(ExternalEvent.SOURCE_STATE);
        assertThat(seen.get(0).payload().get("change")).isEqualTo("suspended");
        assertThat(seen.get(0).payload().get("sourceId")).isEqualTo(source);
    }

    private static ToolContext context(Set<String> excluded) {
        return new ToolContext(new McpCaller(null, new ReadCaller(null, false, true), excluded, "127.0.0.1"),
                "session", McpSessions.STREAMABLE);
    }
}
