package ai.foresignal.situation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.mcp.tools.CreateSituationTool;
import ai.foresignal.mcp.tools.GetSituationChangesTool;
import ai.foresignal.mcp.tools.GetSituationTool;
import ai.foresignal.mcp.tools.LinkToSituationTool;
import ai.foresignal.mcp.tools.ListSituationsTool;
import ai.foresignal.mcp.tools.MergeSituationsTool;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.visibility.SourceVisibility;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.stream.Subscription;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.security.TestTokens;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
 * The prompt 118 situation endpoints over HTTP, the {@code situations} SSE topic and the MCP tools: CRUD with the
 * evidence/inference/speculation sections (INT-4), member add (201, then 200), removal, timeline and changes, scopes,
 * the private-situation rule on every path (EGR-1), the linker dry run's validation, the brief's 409 without a
 * provider, the stream events and the MCP tool shapes.
 */
@IntegrationTest
@Import(TestIngestSources.class)
class SituationControllersIT {

    static final String STANDARD = TestIngestSources.STANDARD;

    @Autowired MockMvc mvc;
    @Autowired TokenService tokens;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper json;
    @Autowired SourceVisibility sources;
    @Autowired SituationEventBus bus;
    @Autowired SituationRepository situations;
    @Autowired SituationsTopic topic;
    @Autowired ListSituationsTool listTool;
    @Autowired GetSituationTool getTool;
    @Autowired GetSituationChangesTool changesTool;
    @Autowired CreateSituationTool createTool;
    @Autowired LinkToSituationTool linkTool;
    @Autowired MergeSituationsTool mergeTool;

    String read;
    String readPrivate;
    String agent;
    String admin;

    @BeforeEach
    void setUp() {
        clock.setInstant(Instant.parse("2026-03-01T12:00:00Z"));
        read = TestTokens.bearer(TestTokens.create(tokens, "situation-read", Scope.READ));
        readPrivate = TestTokens.bearer(TestTokens.create(tokens, "situation-read-private", Scope.READ, true));
        agent = TestTokens.bearer(TestTokens.create(tokens, "situation-agent", Scope.AGENT));
        admin = TestTokens.bearer(TestTokens.create(tokens, "situation-admin", Scope.ADMIN));
    }

    @AfterEach
    void restore() {
        sources.setOperatorPrivate(STANDARD, false);
        sources.refresh();
    }

    long seed(String externalId, double lon) {
        Instant now = clock.instant();
        records.ingestAt(STANDARD, now, NormalizedRecord.builder(externalId, "thing").observedAt(now)
                .point(lon, 32.0).put("title", "Thing " + externalId).build());
        return jdbc.queryForObject("select max(seq) from record_version where source_id = ? and external_id = ?",
                Long.class, STANDARD, externalId);
    }

    static String member(String externalId, long seq) {
        return "{\"memberType\":\"record\",\"ref\":\"" + STANDARD + "/" + externalId + "\",\"seq\":" + seq + "}";
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

    String create(String bearer, String body) throws Exception {
        MockHttpServletResponse response = send(bearer, post("/v1/situations"), body);
        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(201);
        return body(response).get("id").asString();
    }

    @Test
    @DisplayName("CRUD: create, read in three sections, list, patch, members, timeline and changes")
    void crud() throws Exception {
        long s1 = seed("r1", -95.0);
        String id = create(agent, "{\"title\":\"Storyline\",\"theme\":\"maritime\",\"members\":[" + member("r1", s1)
                + "]}");
        JsonNode situation = body(call(read, get("/v1/situations/" + id)));
        assertThat(situation.has("evidence")).isTrue();
        assertThat(situation.get("inference").get("summary").asString()).contains("1 signal from 1 family");
        assertThat(situation.get("speculation").get("label").asString()).isEqualTo("speculation");
        assertThat(situation.get("origin").asString()).isEqualTo("agent");
        assertThat(situation.get("evidence").get("families").get(0).get("members").get(0).get("objectRef").asString())
                .isEqualTo("record:" + STANDARD + "/r1@" + s1);
        assertThat(body(call(read, get("/v1/situations?theme=maritime"))).get("items").toString()).contains(id);
        assertThat(body(call(read, get("/v1/situations?theme=cyber"))).get("items")).isEmpty();
        assertThat(call(read, get("/v1/situations?status=bogus")).getStatus()).isEqualTo(400);

        int version = situation.get("version").asInt();
        MockHttpServletResponse patched = call(agent, patch("/v1/situations/" + id)
                .contentType("application/merge-patch+json")
                .content("{\"title\":\"Renamed\",\"severityOverride\":\"critical\",\"version\":" + version + "}"));
        assertThat(patched.getStatus()).as(patched.getContentAsString()).isEqualTo(200);
        assertThat(body(patched).get("severity").asString()).isEqualTo("critical");
        assertThat(body(patched).get("severityOverridden").asBoolean()).isTrue();
        assertThat(send(agent, patch("/v1/situations/" + id), "{\"title\":\"x\",\"version\":" + version + "}")
                .getStatus()).isEqualTo(409);

        long s2 = seed("r2", -95.1);
        MockHttpServletResponse added = send(agent, post("/v1/situations/" + id + "/members"), member("r2", s2));
        assertThat(added.getStatus()).as(added.getContentAsString()).isEqualTo(201);
        String memberId = body(added).get("id").asString();
        assertThat(send(agent, post("/v1/situations/" + id + "/members"), member("r2", s2)).getStatus())
                .isEqualTo(200);
        assertThat(send(agent, post("/v1/situations/" + id + "/members"), member("r2", s2 + 999)).getStatus())
                .isEqualTo(400);
        assertThat(body(call(read, get("/v1/situations/" + id + "/members?limit=1"))).get("nextCursor").isNull())
                .isFalse();
        assertThat(call(agent, delete("/v1/situations/" + id + "/members/" + memberId)).getStatus()).isEqualTo(204);
        assertThat(call(agent, delete("/v1/situations/" + id + "/members/" + memberId)).getStatus()).isEqualTo(400);

        JsonNode timeline = body(call(read, get("/v1/situations/" + id + "/timeline")));
        assertThat(timeline.get("items").toString()).contains("\"entryType\":\"member\"").contains("\"renamed\"");
        assertThat(call(read, get("/v1/situations/" + id + "/changes")).getStatus()).isEqualTo(400);
        JsonNode changes = body(call(read, get("/v1/situations/" + id + "/changes?since=2026-02-28T00:00:00Z")));
        assertThat(changes.get("situationId").asString()).isEqualTo(id);
        assertThat(call(read, get("/v1/situations/01ARZ3NDEKTSV4RRFFQ69G5FAV")).getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("writes need agent scope; reads need only read; the brief is 409 without a provider")
    void scopes() throws Exception {
        long s1 = seed("r1", -95.0);
        String body = "{\"title\":\"S\",\"theme\":\"mixed\",\"members\":[" + member("r1", s1) + "]}";
        assertThat(send(read, post("/v1/situations"), body).getStatus()).isEqualTo(403);
        String id = create(agent, body);
        assertThat(send(read, patch("/v1/situations/" + id), "{\"title\":\"x\"}").getStatus()).isEqualTo(403);
        assertThat(send(read, post("/v1/situations/linker/test"), "{\"fromSeq\":0,\"toSeq\":10}").getStatus())
                .isEqualTo(403);
        assertThat(call(agent, post("/v1/situations/" + id + "/brief")).getStatus()).isEqualTo(409);
        assertThat(call(read, get("/v1/situations/" + id)).getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("a private situation is hidden from callers without private access on every path (EGR-1)")
    void privateSituation() throws Exception {
        sources.setOperatorPrivate(STANDARD, true);
        sources.refresh();
        long s1 = seed("r1", -95.0);
        String body = "{\"title\":\"Secret\",\"theme\":\"mixed\",\"members\":[" + member("r1", s1) + "]}";
        // A caller without private access cannot pin a private member: reported as not found.
        MockHttpServletResponse refused = send(agent, post("/v1/situations"), body);
        assertThat(refused.getStatus()).isEqualTo(400);
        assertThat(refused.getContentAsString()).contains("SITUATION_MEMBER_NOT_FOUND");
        String id = create(admin, body);
        assertThat(body(call(admin, get("/v1/situations/" + id))).get("private").asBoolean()).isTrue();
        assertThat(call(read, get("/v1/situations/" + id)).getStatus()).isEqualTo(404);
        assertThat(call(read, get("/v1/situations/" + id + "/members")).getStatus()).isEqualTo(404);
        assertThat(body(call(read, get("/v1/situations"))).get("items").toString()).doesNotContain(id);
        assertThat(call(readPrivate, get("/v1/situations/" + id)).getStatus()).isEqualTo(200);
        assertThat(body(call(readPrivate, get("/v1/situations"))).get("items").toString()).contains(id);
        assertThat(send(agent, patch("/v1/situations/" + id), "{\"title\":\"x\"}").getStatus()).isEqualTo(404);
        // Never a foresignal-intel record (that source is public).
        assertThat(SituationRecordPublisher.publishable(situations.findById(id).orElseThrow(), clock.instant()))
                .isFalse();
        // The stream filter drops it for a caller without private access.
        SituationEvent event = new SituationEvent(1, SituationEvent.UPDATED, id, 1, Map.of("private", true), null,
                null, null, clock.instant());
        assertThat(new SituationFilter(false, null, null).matches(event)).isFalse();
        assertThat(new SituationFilter(true, null, null).matches(event)).isTrue();
    }

    @Test
    @DisplayName("the situations topic announces created, activated and merged; replay sends the current state")
    void sseTopic() throws Exception {
        List<SituationEvent> seen = new CopyOnWriteArrayList<>();
        try (Subscription subscription = bus.subscribe("situation-controllers-it", seen::add, 64)) {
            long s1 = seed("r1", -95.0);
            String a = create(agent, "{\"title\":\"A\",\"theme\":\"mixed\",\"status\":\"active\",\"members\":["
                    + member("r1", s1) + "]}");
            clock.setInstant(clock.instant().plusSeconds(60));
            long s2 = seed("r2", -95.0);
            String b = create(agent, "{\"title\":\"B\",\"theme\":\"mixed\",\"members\":[" + member("r2", s2) + "]}");
            assertThat(send(agent, post("/v1/situations/" + b + "/merge"), "{\"intoId\":\"" + a + "\"}").getStatus())
                    .isEqualTo(200);
            Awaitility.await().atMost(Duration.ofSeconds(5)).until(() -> seen.stream()
                    .anyMatch(e -> SituationEvent.MERGED.equals(e.event()) && e.situationId().equals(b)));
            assertThat(seen).extracting(SituationEvent::event).contains(SituationEvent.CREATED,
                    SituationEvent.ACTIVATED, SituationEvent.MERGED);
            SituationEvent merged = seen.stream().filter(e -> SituationEvent.MERGED.equals(e.event())).findFirst()
                    .orElseThrow();
            assertThat(merged.mergedInto()).isEqualTo(a);
            assertThat(merged.payload()).containsKeys("event", "situation", "version", "at");
        }
        assertThat(topic.head()).isPositive();
        assertThat(topic.name()).isEqualTo("situations");
        assertThat(topic.cursorKey()).isEqualTo("u");
    }

    @Test
    @DisplayName("the linker dry run validates its range and returns the SituationLinkerTestResult shape")
    void linkerTest() throws Exception {
        assertThat(send(agent, post("/v1/situations/linker/test"), "{\"fromSeq\":10,\"toSeq\":5}").getStatus())
                .isEqualTo(400);
        assertThat(send(agent, post("/v1/situations/linker/test"), "{\"fromSeq\":0,\"toSeq\":200001}").getStatus())
                .isEqualTo(400);
        JsonNode result = body(send(agent, post("/v1/situations/linker/test"), "{\"fromSeq\":0,\"toSeq\":10}"));
        assertThat(result.get("linkerVersion").asString()).isEqualTo("1.0.0");
        assertThat(result.has("decisions")).isTrue();
        assertThat(result.get("truncated").asBoolean()).isFalse();
    }

    @Test
    @DisplayName("MCP tools: list, get and changes read; create, link and merge write, with the API shapes")
    void mcpTools() throws Exception {
        ToolContext reader = new ToolContext(caller(Scope.READ), null, "test");
        ToolContext writer = new ToolContext(caller(Scope.AGENT), null, "test");
        long s1 = seed("r1", -95.0);
        ToolResult created = createTool.call(writer, json.readTree("{\"title\":\"From MCP\",\"theme\":\"weather\","
                + "\"members\":[" + member("r1", s1) + "]}"));
        String id = created.structured().get("id").asString();
        assertThat(created.structured().get("origin").asString()).isEqualTo("agent");

        JsonNode full = getTool.call(reader, json.readTree("{\"id\":\"" + id + "\"}")).structured();
        assertThat(full.has("evidence") && full.has("inference") && full.has("speculation")).isTrue();
        assertThat(listTool.call(reader, json.readTree("{\"theme\":[\"weather\"]}")).structured().get("items")
                .toString()).contains(id);
        JsonNode changes = changesTool.call(reader, json.readTree("{\"id\":\"" + id
                + "\",\"since\":\"2026-02-01T00:00:00Z\"}")).structured();
        assertThat(changes.has("newMembers")).isTrue();

        long s2 = seed("r2", -95.2);
        JsonNode linked = linkTool.call(writer, json.readTree("{\"situationId\":\"" + id
                + "\",\"memberType\":\"record\",\"ref\":\"" + STANDARD + "/r2\",\"seq\":" + s2 + "}")).structured();
        assertThat(linked.get("added").asBoolean()).isTrue();
        assertThat(linked.get("member").get("joinReason").get("method").asString()).isEqualTo("agent");

        clock.setInstant(clock.instant().plusSeconds(60));
        long s3 = seed("r3", -95.3);
        String other = createTool.call(writer, json.readTree("{\"title\":\"Other\",\"theme\":\"weather\","
                + "\"members\":[" + member("r3", s3) + "]}")).structured().get("id").asString();
        JsonNode survivor = mergeTool.call(writer, json.readTree("{\"situationId\":\"" + other + "\",\"intoId\":\""
                + id + "\",\"reason\":\"same\"}")).structured();
        assertThat(survivor.get("id").asString()).isEqualTo(id);
        assertThat(survivor.get("memberCount").asInt()).isEqualTo(3);
        assertThat(mergeTool.destructiveHint()).isTrue();
        assertThat(getTool.readOnlyHint()).isTrue();
        assertThat(createTool.readOnlyHint()).isFalse();
    }

    static McpCaller caller(Scope scope) {
        AuthenticatedToken token = new AuthenticatedToken(UUID.randomUUID(), "situation-mcp-" + scope.wireValue(),
                scope, TokenRestrictions.NONE, null, "situation-mcp", false, false);
        return new McpCaller(token, new ReadCaller(scope, true, false, ReadRestriction.NONE, false), Set.of(), null);
    }
}
