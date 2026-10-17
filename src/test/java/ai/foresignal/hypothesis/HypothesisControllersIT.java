package ai.foresignal.hypothesis;

import static ai.foresignal.hypothesis.HypothesisFixtures.QUICK_LR;
import static ai.foresignal.hypothesis.HypothesisFixtures.intelIndicator;
import static ai.foresignal.hypothesis.HypothesisFixtures.manualIndicator;
import static ai.foresignal.hypothesis.HypothesisFixtures.outcomeSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.forecast.ForecasterRegistry;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.tools.AddEvidenceTool;
import ai.foresignal.mcp.tools.CreateHypothesisSetTool;
import ai.foresignal.mcp.tools.DraftHypothesisSetTool;
import ai.foresignal.mcp.tools.GetAchMatrixTool;
import ai.foresignal.mcp.tools.GetHypothesisSetTool;
import ai.foresignal.mcp.tools.GetInformationValueTool;
import ai.foresignal.mcp.tools.ListCollectionGapsTool;
import ai.foresignal.mcp.tools.ListHypothesisSetsTool;
import ai.foresignal.mcp.tools.ReviewHypothesisSetTool;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.stream.sse.FifoEvent;
import ai.foresignal.stream.sse.ReplayRequest;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.security.TestTokens;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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
 * The prompt 125 endpoints over HTTP, the {@code hypotheses} SSE topic and the MCP tools: scopes (reads open to read
 * tokens, writes agent only), the set lifecycle with optimistic locking, private sets, hypotheses, indicators,
 * evidence, assumptions, reviews, revisions, the ACH matrix, information value and collection gaps, the topic's
 * private filtering, and the tool shapes.
 */
@IntegrationTest
class HypothesisControllersIT {

    static final Instant NOW = Instant.parse("2026-10-17T12:00:00Z");

    @Autowired MockMvc mvc;
    @Autowired TokenService tokens;
    @Autowired JsonMapper json;
    @Autowired MutableClock clock;
    @Autowired ForecasterRegistry registry;
    @Autowired HypothesesTopic topic;
    @Autowired ListHypothesisSetsTool listSets;
    @Autowired GetHypothesisSetTool getSet;
    @Autowired GetAchMatrixTool getMatrix;
    @Autowired GetInformationValueTool getInformationValue;
    @Autowired ListCollectionGapsTool listGaps;
    @Autowired CreateHypothesisSetTool createSet;
    @Autowired AddEvidenceTool addEvidence;
    @Autowired ReviewHypothesisSetTool reviewSet;
    @Autowired DraftHypothesisSetTool draftSet;

    String read;
    String agent;
    String admin;

    @BeforeEach
    void setUp() {
        clock.setInstant(NOW);
        registry.registerBuiltIns();
        read = TestTokens.bearer(TestTokens.create(tokens, "hy-read", Scope.READ));
        agent = TestTokens.bearer(TestTokens.create(tokens, "hy-agent", Scope.AGENT));
        admin = TestTokens.bearer(TestTokens.create(tokens, "hy-admin", Scope.ADMIN));
    }

    static String setBody(String extra) {
        return outcomeSet("weekly", null, intelIndicator("Maritime disruption", "maritime-disruption",
                "ais-behaviour", null, QUICK_LR) + "," + manualIndicator("Military air activity", "aviation-military",
                "M", QUICK_LR), null, extra);
    }

    MockHttpServletResponse call(String bearer, MockHttpServletRequestBuilder request) throws Exception {
        if (bearer != null) {
            request = request.header(HttpHeaders.AUTHORIZATION, bearer);
        }
        return mvc.perform(request).andReturn().getResponse();
    }

    MockHttpServletResponse postJson(String bearer, String path, String body) throws Exception {
        return call(bearer, post(path).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    MockHttpServletResponse patchJson(String bearer, String path, String body) throws Exception {
        return call(bearer, patch(path).contentType("application/merge-patch+json").content(body));
    }

    JsonNode body(MockHttpServletResponse response) throws Exception {
        return json.readTree(response.getContentAsString(StandardCharsets.UTF_8));
    }

    JsonNode createSet(String bearer, String extra) throws Exception {
        MockHttpServletResponse created = postJson(bearer, "/v1/hypotheses/sets", setBody(extra));
        assertThat(created.getStatus()).as(created.getContentAsString()).isEqualTo(201);
        return body(created);
    }

    static String hypothesisId(JsonNode set, String label) {
        for (JsonNode h : set.get("hypotheses")) {
            if (h.get("label").asString().equals(label)) {
                return h.get("id").asString();
            }
        }
        throw new AssertionError("no hypothesis " + label);
    }

    @Test
    @DisplayName("sets: agent creates (201), read reads and lists, read cannot write, bad bodies 400, unknown 404")
    void sets() throws Exception {
        assertThat(postJson(read, "/v1/hypotheses/sets", setBody("")).getStatus()).isEqualTo(403);
        JsonNode set = createSet(agent, "");
        String id = set.get("id").asString();
        assertThat(set.get("hypotheses")).hasSize(4);
        assertThat(set.get("indicators")).hasSize(2);
        assertThat(set.get("assumptions")).hasSize(1);
        assertThat(set.get("origin").asString()).isEqualTo("agent");
        assertThat(set.get("status").asString()).isEqualTo("active");
        assertThat(set.get("recordRef").asString()).isEqualTo("record:foresignal-intel/" + id);
        assertThat(set.get("hypotheses").get(0).get("estimativeTerm").asString()).isEqualTo("unlikely");

        JsonNode got = body(call(read, get("/v1/hypotheses/sets/" + id)));
        assertThat(got.get("id").asString()).isEqualTo(id);
        assertThat(got.get("hypotheses").get(0).has("change")).isTrue();
        JsonNode list = body(call(read, get("/v1/hypotheses/sets?status=active&cadence=weekly")));
        assertThat(list.get("items")).anyMatch(item -> item.get("id").asString().equals(id));
        assertThat(list.get("items").get(0).has("indicators")).isFalse();
        assertThat(body(call(read, get("/v1/hypotheses/sets?cadence=monthly"))).get("items")).isEmpty();
        assertThat(call(read, get("/v1/hypotheses/sets?status=bogus")).getStatus()).isEqualTo(400);

        MockHttpServletResponse invalid = postJson(agent, "/v1/hypotheses/sets", setBody("")
                .replace("\"prior\":0.5", "\"prior\":0.9"));
        assertThat(invalid.getStatus()).isEqualTo(400);
        assertThat(body(invalid).get("code").asString()).isEqualTo("HYPOTHESIS_SET_INVALID");
        MockHttpServletResponse unknown = call(read, get("/v1/hypotheses/sets/01JA8Q3V7S2K9W4T6Y1B5N0M3C"));
        assertThat(unknown.getStatus()).isEqualTo(404);
        assertThat(body(unknown).get("code").asString()).isEqualTo("UNKNOWN_HYPOTHESIS_SET");
    }

    @Test
    @DisplayName("PATCH is optimistic: a stale version is 409, the current one applies")
    void optimisticLocking() throws Exception {
        JsonNode set = createSet(agent, "");
        String id = set.get("id").asString();
        int version = set.get("version").asInt();
        MockHttpServletResponse stale = patchJson(agent, "/v1/hypotheses/sets/" + id, "{\"cadence\":\"daily\","
                + "\"version\":" + (version + 5) + "}");
        assertThat(stale.getStatus()).isEqualTo(409);
        assertThat(body(stale).get("code").asString()).isEqualTo("HYPOTHESIS_SET_VERSION_CONFLICT");
        MockHttpServletResponse ok = patchJson(agent, "/v1/hypotheses/sets/" + id, "{\"cadence\":\"daily\","
                + "\"description\":\"Reviewed daily.\",\"version\":" + version + "}");
        assertThat(ok.getStatus()).as(ok.getContentAsString()).isEqualTo(200);
        assertThat(body(ok).get("cadence").asString()).isEqualTo("daily");
        assertThat(body(ok).get("version").asInt()).isGreaterThan(version);
        assertThat(patchJson(agent, "/v1/hypotheses/sets/" + id, "{\"cadence\":\"daily\"}").getStatus())
                .as("version is required").isEqualTo(400);
        assertThat(patchJson(read, "/v1/hypotheses/sets/" + id, "{\"version\":1}").getStatus()).isEqualTo(403);
    }

    @Test
    @DisplayName("private sets are invisible without private access, in the API and on the topic")
    void privateSets() throws Exception {
        JsonNode hidden = createSet(admin, "\"private\":true,");
        String id = hidden.get("id").asString();
        assertThat(hidden.get("private").asBoolean()).isTrue();
        assertThat(hidden.get("recordRef").isNull()).isTrue();
        assertThat(call(read, get("/v1/hypotheses/sets/" + id)).getStatus()).isEqualTo(404);
        assertThat(call(agent, get("/v1/hypotheses/sets/" + id + "/matrix")).getStatus()).isEqualTo(404);
        assertThat(body(call(read, get("/v1/hypotheses/sets"))).get("items"))
                .noneMatch(item -> item.get("id").asString().equals(id));
        assertThat(call(admin, get("/v1/hypotheses/sets/" + id)).getStatus()).isEqualTo(200);
        String privateReader = TestTokens.bearer(TestTokens.create(tokens, "hy-private", Scope.READ, true));
        assertThat(call(privateReader, get("/v1/hypotheses/sets/" + id)).getStatus()).isEqualTo(200);

        createSet(agent, "");
        List<FifoEvent> open = new ArrayList<>();
        topic.replay(new ReplayRequest(0, false), new HypothesisFilter(false), e -> open.add(e));
        assertThat(open).extracting(FifoEvent::event).contains(HypothesisEvents.SET_CREATED);
        assertThat(open).noneMatch(e -> String.valueOf(e.data()).contains(id));
        List<FifoEvent> all = new ArrayList<>();
        topic.replay(new ReplayRequest(0, false), new HypothesisFilter(true), e -> all.add(e));
        assertThat(all).anyMatch(e -> String.valueOf(e.data()).contains(id));
        assertThat(topic.head()).isEqualTo(all.get(all.size() - 1).cursor());
    }

    @Test
    @DisplayName("hypotheses, indicators, evidence, assumptions, reviews, revisions, matrix, information value, gaps")
    void children() throws Exception {
        JsonNode set = createSet(agent, "");
        String id = set.get("id").asString();
        String quick = hypothesisId(set, "within 72 h");

        MockHttpServletResponse added = postJson(agent, "/v1/hypotheses/sets/" + id + "/hypotheses",
                "{\"label\":\"proxy response\",\"statement\":\"A partner responds instead.\",\"type\":\"outcome\","
                        + "\"prior\":0.1,\"priorBasis\":{\"type\":\"operator\",\"rationale\":\"Precedent.\"}}");
        assertThat(added.getStatus()).as(added.getContentAsString()).isEqualTo(201);
        assertThat(body(added).get("probability").asDouble()).isCloseTo(0.1, org.assertj.core.api.Assertions.within(
                1e-9));
        String proxy = body(added).get("id").asString();
        MockHttpServletResponse patched = patchJson(agent, "/v1/hypotheses/" + proxy, "{\"statement\":\"A coalition"
                + " partner responds instead.\"}");
        assertThat(patched.getStatus()).isEqualTo(200);
        assertThat(patchJson(agent, "/v1/hypotheses/01JA8Q3V7S2K9W4T6Y1B5N0M3C", "{\"statement\":\"x\"}").getStatus())
                .isEqualTo(404);

        MockHttpServletResponse indicator = postJson(agent, "/v1/hypotheses/sets/" + id + "/indicators",
                manualIndicator("Operator sighting", "aviation-military", "H", "{\"" + quick + "\":{\"class\":"
                        + "\"medium\",\"direction\":\"supports\"}}"));
        assertThat(indicator.getStatus()).as(indicator.getContentAsString()).isEqualTo(201);
        String indicatorId = body(indicator).get("id").asString();
        assertThat(body(indicator).get("lr").get(quick).get("effective").asDouble()).isEqualTo(2.5);
        MockHttpServletResponse observed = patchJson(agent, "/v1/indicators/" + indicatorId,
                "{\"state\":\"observed\",\"observedEvidence\":{\"note\":\"Seen by the operator.\"}}");
        assertThat(observed.getStatus()).as(observed.getContentAsString()).isEqualTo(200);
        assertThat(body(observed).get("state").asString()).isEqualTo("observed");
        assertThat(body(call(read, get("/v1/indicators?state=observed&setId=" + id))).get("items")).hasSize(1);
        String watched = null;
        for (JsonNode i : set.get("indicators")) {
            if (i.get("description").asString().equals("Maritime disruption")) {
                watched = i.get("id").asString();
            }
        }
        MockHttpServletResponse notManual = patchJson(agent, "/v1/indicators/" + watched, "{\"state\":\"absent\"}");
        assertThat(notManual.getStatus()).isEqualTo(400);
        assertThat(body(notManual).get("code").asString()).isEqualTo("INDICATOR_INVALID");
        MockHttpServletResponse retired = call(agent, delete("/v1/indicators/" + watched));
        assertThat(retired.getStatus()).isEqualTo(200);
        assertThat(body(retired).get("state").asString()).isEqualTo("retired");

        MockHttpServletResponse evidence = postJson(agent, "/v1/hypotheses/sets/" + id + "/evidence",
                "{\"source\":\"manual\",\"description\":\"Diplomatic note.\",\"credibility\":\"M\",\"relevance\":\"H\","
                        + "\"ratings\":{\"" + quick + "\":\"I\"}}");
        assertThat(evidence.getStatus()).as(evidence.getContentAsString()).isEqualTo(201);
        String evidenceId = body(evidence).get("id").asString();
        JsonNode matrix = body(call(read, get("/v1/hypotheses/sets/" + id + "/matrix")));
        assertThat(matrix.get("rows")).hasSize(2);
        assertThat(matrix.get("scores").get(quick).asDouble()).isEqualTo(-2.0);
        assertThat(matrix.get("note").asString()).contains("Audit view");
        assertThat(call(agent, delete("/v1/hypotheses/sets/" + id + "/evidence/" + evidenceId)).getStatus())
                .isEqualTo(204);
        assertThat(call(agent, delete("/v1/hypotheses/sets/" + id + "/evidence/" + evidenceId)).getStatus())
                .isEqualTo(404);

        MockHttpServletResponse assumption = postJson(agent, "/v1/hypotheses/sets/" + id + "/assumptions",
                "{\"text\":\"Talks continue.\",\"linchpin\":true}");
        assertThat(assumption.getStatus()).isEqualTo(201);
        String assumptionId = body(assumption).get("id").asString();
        MockHttpServletResponse broken = patchJson(agent, "/v1/hypotheses/sets/" + id + "/assumptions/" + assumptionId,
                "{\"status\":\"broken\"}");
        assertThat(broken.getStatus()).isEqualTo(200);
        assertThat(body(call(read, get("/v1/hypotheses/sets/" + id))).get("reviewFlagged").asBoolean()).isTrue();

        MockHttpServletResponse review = postJson(agent, "/v1/hypotheses/sets/" + id + "/review",
                "{\"rationale\":\"Talks collapsed; the response window is open.\",\"probabilities\":{\"" + quick
                        + "\":0.5}}");
        assertThat(review.getStatus()).as(review.getContentAsString()).isEqualTo(201);
        assertThat(body(review).get("cause").asString()).isEqualTo("review");
        JsonNode revisions = body(call(read, get("/v1/hypotheses/sets/" + id + "/revisions?limit=2")));
        assertThat(revisions.get("items")).hasSize(2);
        assertThat(revisions.get("items").get(0).get("cause").asString()).isEqualTo("review");
        assertThat(revisions.get("nextCursor").isNull()).isFalse();
        assertThat(revisions.get("items").get(0).get("drivers").get(0).has("evidenceKey")).isFalse();
        JsonNode older = body(call(read, get("/v1/hypotheses/sets/" + id + "/revisions?cursor="
                + revisions.get("nextCursor").asString())));
        assertThat(older.get("items")).isNotEmpty();

        JsonNode value = body(call(read, get("/v1/hypotheses/sets/" + id + "/information-value")));
        assertThat(value.get("version").asString()).isEqualTo("InformationValue 1.0.0");
        assertThat(value.has("entropy")).isTrue();
        assertThat(call(read, get("/v1/collection-gaps?setId=" + id)).getStatus()).isEqualTo(200);
        assertThat(postJson(read, "/v1/hypotheses/sets/" + id + "/review", "{\"rationale\":\"x\"}").getStatus())
                .isEqualTo(403);
    }

    @Test
    @DisplayName("MCP: five read tools and four agent tools with the API shapes")
    void mcpTools() throws Exception {
        assertThat(listSets.requiredScope()).isEqualTo(Scope.READ);
        assertThat(getSet.requiredScope()).isEqualTo(Scope.READ);
        assertThat(getMatrix.requiredScope()).isEqualTo(Scope.READ);
        assertThat(getInformationValue.requiredScope()).isEqualTo(Scope.READ);
        assertThat(listGaps.requiredScope()).isEqualTo(Scope.READ);
        assertThat(createSet.requiredScope()).isEqualTo(Scope.AGENT);
        assertThat(addEvidence.requiredScope()).isEqualTo(Scope.AGENT);
        assertThat(reviewSet.requiredScope()).isEqualTo(Scope.AGENT);
        assertThat(draftSet.requiredScope()).isEqualTo(Scope.AGENT);
        assertThat(draftSet.alwaysAuthenticated()).isTrue();
        ToolContext ctx = new ToolContext(caller(), null, "test");
        JsonNode created = createSet.call(ctx, json.readTree(setBody(""))).structured();
        String id = created.get("id").asString();
        assertThat(created.get("origin").asString()).isEqualTo("agent");
        assertThat(String.valueOf(listSets.call(ctx, json.readTree("{\"status\":\"active\"}")).structured()))
                .contains(id);
        assertThat(getSet.call(ctx, json.readTree("{\"setId\":\"" + id + "\"}")).structured().get("indicators"))
                .hasSize(2);
        String quick = hypothesisId(created, "within 72 h");
        JsonNode row = addEvidence.call(ctx, json.readTree("{\"setId\":\"" + id + "\",\"source\":\"manual\","
                + "\"description\":\"A note.\",\"credibility\":\"L\",\"relevance\":\"L\",\"ratings\":{\"" + quick
                + "\":\"C\"}}")).structured();
        assertThat(row.get("weight").asDouble()).isEqualTo(0.25);
        assertThat(getMatrix.call(ctx, json.readTree("{\"setId\":\"" + id + "\"}")).structured().get("rows"))
                .hasSize(1);
        JsonNode revision = reviewSet.call(ctx, json.readTree("{\"setId\":\"" + id + "\",\"rationale\":\"Weekly"
                + " review.\"}")).structured();
        assertThat(revision.get("cause").asString()).isEqualTo("review");
        assertThat(getInformationValue.call(ctx, json.readTree("{\"setId\":\"" + id + "\"}")).structured()
                .has("indicators")).isTrue();
        assertThat(listGaps.call(ctx, json.readTree("{}")).structured().has("items")).isTrue();
    }

    McpCaller caller() {
        AuthenticatedToken token = new AuthenticatedToken(UUID.randomUUID(), "hy-mcp", Scope.AGENT,
                TokenRestrictions.NONE, null, null, false, false);
        return new McpCaller(token, new ReadCaller(Scope.AGENT, true, false, ReadRestriction.NONE, false), Set.of(),
                null);
    }
}
