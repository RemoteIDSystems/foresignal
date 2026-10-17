package ai.foresignal.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.mcp.tools.GetPrecursorTool;
import ai.foresignal.mcp.tools.ListPrecursorsTool;
import ai.foresignal.mcp.tools.MinePrecursorsTool;
import ai.foresignal.precursor.PrecursorBatchEntity;
import ai.foresignal.precursor.PrecursorBatchRepository;
import ai.foresignal.precursor.PrecursorEntity;
import ai.foresignal.precursor.PrecursorRepository;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.Ulids;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.analytics.AnalyticsItSupport;
import ai.foresignal.testsupport.analytics.AnalyticsTestSources;
import ai.foresignal.testsupport.security.TestTokens;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The prompt 120 precursor endpoints over HTTP and the MCP tools: the library (read), retire and pin (agent, logged),
 * batches and candidates (agent), mining and re-tests as 103 jobs with the one-run lock
 * ({@code PRECURSOR_MINING_IN_PROGRESS}), the Problem codes and the tool shapes.
 */
@IntegrationTest
@Import(AnalyticsTestSources.class)
class PrecursorControllersIT extends AnalyticsItSupport {

    @Autowired MockMvc mvc;
    @Autowired TokenService tokens;
    @Autowired JsonMapper json;
    @Autowired PrecursorRepository precursors;
    @Autowired PrecursorBatchRepository batches;
    @Autowired ListPrecursorsTool listPrecursors;
    @Autowired GetPrecursorTool getPrecursor;
    @Autowired MinePrecursorsTool minePrecursors;

    String read;
    String agent;

    @BeforeEach
    void tokens() {
        read = TestTokens.bearer(TestTokens.create(tokens, "precursor-read", Scope.READ));
        agent = TestTokens.bearer(TestTokens.create(tokens, "precursor-agent", Scope.AGENT));
    }

    MockHttpServletResponse call(String bearer, MockHttpServletRequestBuilder request) throws Exception {
        if (bearer != null) {
            request = request.header(HttpHeaders.AUTHORIZATION, bearer);
        }
        return mvc.perform(request).andReturn().getResponse();
    }

    JsonNode body(MockHttpServletResponse response) throws Exception {
        return json.readTree(response.getContentAsString(StandardCharsets.UTF_8));
    }

    PrecursorEntity precursor() {
        Instant now = clock.instant();
        PrecursorEntity p = new PrecursorEntity(Ulids.next(now), "key-" + UUID.randomUUID(), "event", now);
        p.setAntecedent(Map.of("selector", Map.of("alertRuleTag", "gnss", "regionCodes", List.of("YE")), "family",
                "gnss-interference", "label", "GNSS interference alerts in YE"));
        p.setConsequent(Map.of("selector", Map.of("intelEventType", "maritime-disruption", "regionCodes",
                List.of("YE")), "family", "ais-behaviour", "label", "maritime disruption events in YE"));
        p.setSpatial(Map.of("rule", "sameRegion", "regionLevel", "country"));
        p.setRegionCodes(List.of("YE"));
        p.setFamilies(List.of("gnss-interference", "ais-behaviour"));
        p.setLagMaxSeconds(Duration.ofDays(3).toSeconds());
        p.setDiscovery(Map.of("window", Map.of("start", now.toString()), "n", 30, "verdict", "notable"));
        p.setStatus("validated");
        p.setBatchId(Ulids.next(now));
        p.setBatchSize(120);
        p.setDetector(Map.of("name", "precursor-tester", "version", "1.0.0"));
        p.setDescription("In YE, GNSS interference alerts was followed by maritime disruption events.");
        p.setUpdatedAt(now);
        return precursors.save(p);
    }

    PrecursorBatchEntity batch() {
        Instant now = clock.instant();
        PrecursorBatchEntity b = new PrecursorBatchEntity(Ulids.next(now), "mine", "schedule", now);
        b.setStatus("done");
        b.setTested(120);
        b.setCapped(30);
        b.setQ(0.10);
        return batches.save(b);
    }

    @Test
    @DisplayName("the library is a read: list with filters, get, UNKNOWN_PRECURSOR 404")
    void library() throws Exception {
        PrecursorEntity p = precursor();

        MockHttpServletResponse list = call(read, get("/v1/precursors").param("status", "validated")
                .param("region", "YE").param("family", "gnss-interference"));
        assertThat(list.getStatus()).as(list.getContentAsString()).isEqualTo(200);
        assertThat(body(list).get("items")).hasSize(1);
        assertThat(body(list).get("items").get(0).get("associationNote").asString())
                .isEqualTo("association in stored data; not causation");
        assertThat(body(call(read, get("/v1/precursors").param("kind", "series"))).get("items")).isEmpty();
        assertThat(body(call(read, get("/v1/precursors").param("status", "retired"))).get("items")).isEmpty();
        assertThat(call(read, get("/v1/precursors").param("status", "bogus")).getStatus()).isEqualTo(400);
        assertThat(call(read, get("/v1/precursors").param("region", "yemen")).getStatus()).isEqualTo(400);

        MockHttpServletResponse one = call(read, get("/v1/precursors/" + p.getId()));
        assertThat(one.getStatus()).isEqualTo(200);
        assertThat(body(one).get("lagMax").asString()).isEqualTo("P3D");
        assertThat(body(one).get("batchSize").asInt()).isEqualTo(120);
        MockHttpServletResponse missing = call(read, get("/v1/precursors/01JA8Q3V7S2K9W4T6Y1B5N0M3C"));
        assertThat(missing.getStatus()).isEqualTo(404);
        assertThat(body(missing).get("code").asString()).isEqualTo("UNKNOWN_PRECURSOR");
    }

    @Test
    @DisplayName("PATCH retires or pins (agent), logged in the history; read tokens get 403")
    void patchPrecursor() throws Exception {
        PrecursorEntity p = precursor();
        String retire = "{\"status\":\"retired\",\"reason\":\"a reporting artifact\"}";

        assertThat(call(read, patch("/v1/precursors/" + p.getId()).contentType("application/merge-patch+json")
                .content(retire)).getStatus()).isEqualTo(403);
        MockHttpServletResponse retired = call(agent, patch("/v1/precursors/" + p.getId())
                .contentType("application/merge-patch+json").content(retire));
        assertThat(retired.getStatus()).as(retired.getContentAsString()).isEqualTo(200);
        assertThat(body(retired).get("status").asString()).isEqualTo("retired");
        assertThat(body(retired).get("history")).hasSize(1);

        MockHttpServletResponse pinned = call(agent, patch("/v1/precursors/" + p.getId())
                .contentType("application/merge-patch+json")
                .content("{\"status\":\"validated\",\"reason\":\"confirmed by field reports\"}"));
        assertThat(body(pinned).get("operatorValidated").asBoolean()).isTrue();
        assertThat(body(pinned).get("statusReason").asString()).startsWith("pinned as operator-validated");
        assertThat(body(pinned).get("caveats").toString()).contains("OPERATOR_VALIDATED");
        assertThat(call(agent, patch("/v1/precursors/" + p.getId()).contentType("application/merge-patch+json")
                .content("{\"status\":\"stale\",\"reason\":\"x\"}")).getStatus()).isEqualTo(400);
        assertThat(call(agent, patch("/v1/precursors/" + p.getId()).contentType("application/merge-patch+json")
                .content("{\"status\":\"retired\"}")).getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("batches are agent reads; an unknown batch is 404")
    void batchesEndpoints() throws Exception {
        PrecursorBatchEntity b = batch();

        assertThat(call(read, get("/v1/precursors/batches")).getStatus()).isEqualTo(403);
        MockHttpServletResponse list = call(agent, get("/v1/precursors/batches"));
        assertThat(list.getStatus()).isEqualTo(200);
        assertThat(body(list).get("items").get(0).get("id").asString()).isEqualTo(b.getId());
        MockHttpServletResponse one = call(agent, get("/v1/precursors/batches/" + b.getId()));
        assertThat(body(one).get("capped").asInt()).isEqualTo(30);
        assertThat(body(one).get("method").asString()).isEqualTo("BH");
        assertThat(call(agent, get("/v1/precursors/batches/01JA8Q3V7S2K9W4T6Y1B5N0M3C")).getStatus())
                .isEqualTo(404);
    }

    @Test
    @DisplayName("candidates: create (201), validation (400), list, delete (204, then 404)")
    void candidates() throws Exception {
        String bodyText = "{\"name\":\"GNSS before disruptions\",\"kind\":\"event\",\"antecedent\":{\"selector\":"
                + "{\"alertRuleTag\":\"gnss-interference\"}},\"consequent\":{\"selector\":{\"intelEventType\":"
                + "\"maritime-disruption\"}},\"spatial\":{\"rule\":\"sameRegion\",\"regionLevel\":\"country\"},"
                + "\"lagMin\":\"P0D\",\"lagMax\":\"P3D\",\"regionCodes\":[\"YE\"]}";
        assertThat(call(read, post("/v1/precursors/candidates").contentType(MediaType.APPLICATION_JSON)
                .content(bodyText)).getStatus()).isEqualTo(403);
        MockHttpServletResponse created = call(agent, post("/v1/precursors/candidates")
                .contentType(MediaType.APPLICATION_JSON).content(bodyText));
        assertThat(created.getStatus()).as(created.getContentAsString()).isEqualTo(201);
        String id = body(created).get("id").asString();
        assertThat(body(created).get("antecedent").get("label").asString()).isEqualTo("alerts tagged gnss-interference");
        assertThat(body(created).get("lagMax").asString()).isEqualTo("P3D");

        MockHttpServletResponse invalid = call(agent, post("/v1/precursors/candidates")
                .contentType(MediaType.APPLICATION_JSON).content("{\"kind\":\"other\",\"lagMax\":\"P90D\"}"));
        assertThat(invalid.getStatus()).isEqualTo(400);
        assertThat(body(invalid).get("errors").toString()).contains("kind").contains("antecedent");
        assertThat(call(agent, post("/v1/precursors/candidates").contentType(MediaType.APPLICATION_JSON)
                .content("{\"kind\":\"series\",\"antecedent\":{\"seriesRef\":\"series:nope/x\"},\"consequent\":"
                        + "{\"seriesRef\":\"bad\"},\"lagMax\":\"P7D\"}")).getStatus()).isEqualTo(400);

        assertThat(body(call(agent, get("/v1/precursors/candidates"))).get("items").get(0).get("id").asString())
                .isEqualTo(id);
        assertThat(call(agent, delete("/v1/precursors/candidates/" + id)).getStatus()).isEqualTo(204);
        assertThat(call(agent, delete("/v1/precursors/candidates/" + id)).getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("mine and retest queue a precursor-mining job; a second run while one is running is 409")
    void mining() throws Exception {
        assertThat(call(read, post("/v1/precursors/mine")).getStatus()).isEqualTo(403);
        MockHttpServletResponse mined = call(agent, post("/v1/precursors/mine"));
        assertThat(mined.getStatus()).as(mined.getContentAsString()).isEqualTo(202);
        JsonNode job = body(mined);
        assertThat(job.get("type").asString()).isEqualTo("precursor-mining");

        String jobId = job.get("id").asString();
        org.awaitility.Awaitility.await().atMost(Duration.ofSeconds(30)).until(() -> List.of("done", "failed")
                .contains(jdbc.queryForObject("SELECT status FROM backtest_job WHERE id = ?", String.class, jobId)));
        assertThat(body(call(agent, get("/v1/precursors/batches"))).get("items").get(0).get("jobId").asString())
                .isEqualTo(jobId);
        jdbc.update("UPDATE backtest_job SET status = 'running' WHERE id = ?", jobId);
        MockHttpServletResponse busy = call(agent, post("/v1/precursors/mine"));
        assertThat(busy.getStatus()).isEqualTo(409);
        assertThat(body(busy).get("code").asString()).isEqualTo("PRECURSOR_MINING_IN_PROGRESS");
        MockHttpServletResponse retestBusy = call(agent, post("/v1/precursors/retest"));
        assertThat(retestBusy.getStatus()).isEqualTo(409);

        jdbc.update("UPDATE backtest_job SET status = 'done' WHERE id = ?", jobId);
        MockHttpServletResponse retest = call(agent, post("/v1/precursors/retest"));
        assertThat(retest.getStatus()).as(retest.getContentAsString()).isEqualTo(202);
        assertThat(body(retest).get("definition").get("mode").asString()).isEqualTo("retest");
    }

    @Test
    @DisplayName("MCP tools: names, scopes and result shapes")
    void mcpTools() {
        PrecursorEntity p = precursor();
        ToolContext reader = new ToolContext(caller(Scope.READ), null, "test");
        assertThat(listPrecursors.name()).isEqualTo("list_precursors");
        assertThat(listPrecursors.requiredScope()).isEqualTo(Scope.READ);
        assertThat(getPrecursor.name()).isEqualTo("get_precursor");
        assertThat(getPrecursor.requiredScope()).isEqualTo(Scope.READ);
        assertThat(minePrecursors.name()).isEqualTo("mine_precursors");
        assertThat(minePrecursors.requiredScope()).isEqualTo(Scope.AGENT);
        assertThat(text(listPrecursors.call(reader, json.readTree("{\"status\":[\"validated\"]}")))).contains(p.getId());
        assertThat(text(getPrecursor.call(reader, json.readTree("{\"precursorId\":\"" + p.getId() + "\"}"))))
                .contains("associationNote").contains("discovery");
    }

    static String text(ToolResult result) {
        return String.valueOf(result.structured());
    }

    McpCaller caller(Scope scope) {
        AuthenticatedToken token = new AuthenticatedToken(UUID.randomUUID(), "precursor-mcp-" + scope.wireValue(),
                scope, TokenRestrictions.NONE, null, "precursor-mcp", false, false);
        return new McpCaller(token, new ReadCaller(scope, true, false, ReadRestriction.NONE, false), Set.of(), null);
    }
}
