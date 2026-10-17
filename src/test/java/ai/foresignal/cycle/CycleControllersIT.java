package ai.foresignal.cycle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.tools.GetAssessmentTool;
import ai.foresignal.mcp.tools.GetIntelBoardTool;
import ai.foresignal.mcp.tools.ListAssessmentsTool;
import ai.foresignal.mcp.tools.RunCycleTool;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ask.AskTestSources;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipInputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;

/**
 * The prompt 128 endpoints over HTTP and the MCP tools: cycle CRUD and runs with their scopes, the always-authenticated
 * spending paths, assessment formats (JSON, markdown, HTML), the bundle (JSON and GeoParquet ZIP), feedback into the
 * 102 feedback table, private assessments hidden, on-demand situation assessments, the intel board's ETag and
 * cache headers, the maintenance trigger, and the MCP tool shapes.
 */
@IntegrationTest
@Import(AskTestSources.class)
class CycleControllersIT extends CycleItSupport {

    @Autowired GetAssessmentTool getTool;
    @Autowired ListAssessmentsTool listTool;
    @Autowired GetIntelBoardTool boardTool;
    @Autowired RunCycleTool runTool;

    static final String WEEKLY = "{\"name\":\"Red Sea weekly\",\"cadence\":\"weekly\",\"schedule\":{\"cron\":"
            + "\"0 7 * * 1\",\"timezone\":\"Europe/London\"},\"scope\":{\"regionCodes\":[\"YE\"]},\"roles\":[],"
            + "\"enabled\":true}";

    MockHttpServletResponse call(String bearer, MockHttpServletRequestBuilder request) throws Exception {
        if (bearer != null) {
            request = request.header(HttpHeaders.AUTHORIZATION, bearer);
        }
        return mvc.perform(request).andReturn().getResponse();
    }

    JsonNode body(MockHttpServletResponse response) throws Exception {
        return JSON.readTree(response.getContentAsString(StandardCharsets.UTF_8));
    }

    MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String body) {
        return request.contentType(MediaType.APPLICATION_JSON).content(body);
    }

    AssessmentEntity assessment(boolean makePrivate) {
        situation("Red Sea shipping", "critical");
        clock.setInstant(clock.instant().plus(Duration.ofMinutes(1)));
        CycleRunEntity run = runNow(cycle("weekly", "[]", null, null));
        AssessmentEntity a = assessmentRows.findById(run.getAssessmentId()).orElseThrow();
        if (makePrivate) {
            jdbc.update("UPDATE assessment SET \"private\" = true WHERE id = ?", a.getId());
        }
        return a;
    }

    @Test
    @DisplayName("cycles: create (agent, 201), list and read (read), replace, delete (agent); 400, 404 and 403")
    void cycleCrud() throws Exception {
        assertThat(call(reader, json(post("/v1/analysis/cycles"), WEEKLY)).getStatus()).isEqualTo(403);
        MockHttpServletResponse created = call(agent, json(post("/v1/analysis/cycles"), WEEKLY));
        assertThat(created.getStatus()).isEqualTo(201);
        String id = body(created).get("id").asString();
        assertThat(body(created).get("nextRunAt").isNull()).isFalse();
        assertThat(body(created).get("maxQuestions").asInt()).isEqualTo(40);
        assertThat(body(call(null, get("/v1/analysis/cycles"))).get("items")).hasSize(1);
        assertThat(body(call(reader, get("/v1/analysis/cycles/" + id))).get("name").asString())
                .isEqualTo("Red Sea weekly");
        assertThat(call(agent, json(put("/v1/analysis/cycles/" + id), WEEKLY.replace("\"enabled\":true",
                "\"enabled\":false"))).getStatus()).isEqualTo(200);
        assertThat(cycles.get(id).getNextRunAt()).isNull();
        assertThat(call(agent, json(post("/v1/analysis/cycles"), "{\"name\":\"x\"}")).getStatus()).isEqualTo(400);
        assertThat(call(agent, json(post("/v1/analysis/cycles"), WEEKLY.replace("\"roles\":[]",
                "\"roles\":[],\"analystProfileId\":\"01JZZZZZZZZZZZZZZZZZZZZZZZ\""))).getStatus()).isEqualTo(404);
        assertThat(body(call(reader, get("/v1/analysis/cycles/01JZZZZZZZZZZZZZZZZZZZZZZZ"))).get("code").asString())
                .isEqualTo("UNKNOWN_CYCLE");
        assertThat(call(agent, delete("/v1/analysis/cycles/" + id)).getStatus()).isEqualTo(204);
        assertThat(call(agent, delete("/v1/analysis/cycles/" + id)).getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("running a cycle is agent and always authenticated (202); runs are agent with the packet on detail")
    void runs() throws Exception {
        AnalysisCycleEntity cycle = cycle("daily", "[]", null, null);
        assertThat(call(null, post("/v1/analysis/cycles/" + cycle.getId() + "/run")).getStatus()).isEqualTo(401);
        assertThat(call(reader, post("/v1/analysis/cycles/" + cycle.getId() + "/run")).getStatus()).isEqualTo(403);
        MockHttpServletResponse accepted = call(agent, post("/v1/analysis/cycles/" + cycle.getId() + "/run"));
        assertThat(accepted.getStatus()).isEqualTo(202);
        String runId = body(accepted).get("id").asString();
        org.awaitility.Awaitility.await().atMost(Duration.ofSeconds(30)).until(() -> CycleVocabulary.DIGEST_ONLY
                .equals(runs.findById(runId).orElseThrow().getStatus()));
        assertThat(call(reader, get("/v1/analysis/cycles/" + cycle.getId() + "/runs")).getStatus()).isEqualTo(403);
        JsonNode page = body(call(agent, get("/v1/analysis/cycles/" + cycle.getId() + "/runs")));
        assertThat(page.get("items")).hasSize(1);
        assertThat(page.get("items").get(0).get("packet").isNull()).isTrue();
        JsonNode run = body(call(agent, get("/v1/analysis/runs/" + runId)));
        assertThat(run.get("packet").get("version").asString()).isEqualTo(CyclePacketBuilder.VERSION);
        assertThat(body(call(agent, get("/v1/analysis/runs/01JZZZZZZZZZZZZZZZZZZZZZZZ"))).get("code").asString())
                .isEqualTo("UNKNOWN_CYCLE_RUN");
        assertThat(call(agent, get("/v1/analysis/cycles/" + cycle.getId() + "/runs?cursor=zzz")).getStatus())
                .isEqualTo(400);
    }

    @Test
    @DisplayName("assessments: list, latest, JSON, markdown and HTML; private ones hidden; 404 UNKNOWN_ASSESSMENT")
    void assessmentsFormats() throws Exception {
        assertThat(body(call(reader, get("/v1/assessments/latest"))).get("code").asString())
                .isEqualTo("UNKNOWN_ASSESSMENT");
        AssessmentEntity a = assessment(false);
        JsonNode list = body(call(null, get("/v1/assessments?cadence=weekly")));
        assertThat(list.get("items")).hasSize(1);
        assertThat(list.get("items").get(0).get("markdown").isNull()).isTrue();
        assertThat(body(call(reader, get("/v1/assessments/latest?cadence=weekly"))).get("id").asString())
                .isEqualTo(a.getId());
        JsonNode one = body(call(reader, get("/v1/assessments/" + a.getId())));
        assertThat(one.get("ref").asString()).isEqualTo("assessment:" + a.getId());
        assertThat(one.get("footer").asString()).isEqualTo(CycleVocabulary.FOOTER);
        MockHttpServletResponse md = call(reader, get("/v1/assessments/" + a.getId() + "?format=md"));
        assertThat(md.getContentType()).startsWith("text/markdown");
        assertThat(md.getContentAsString(StandardCharsets.UTF_8)).contains("## Bottom line up front");
        MockHttpServletResponse html = call(reader, get("/v1/assessments/" + a.getId())
                .header(HttpHeaders.ACCEPT, "text/html"));
        assertThat(html.getContentType()).startsWith("text/html");
        assertThat(html.getHeader("Content-Security-Policy")).isNotNull();
        assertThat(call(reader, get("/v1/assessments?cadence=hourly")).getStatus()).isEqualTo(400);
        assertThat(call(reader, get("/v1/assessments/01JZZZZZZZZZZZZZZZZZZZZZZZ")).getStatus()).isEqualTo(404);
        jdbc.update("UPDATE assessment SET \"private\" = true WHERE id = ?", a.getId());
        assertThat(call(reader, get("/v1/assessments/" + a.getId())).getStatus()).isEqualTo(404);
        assertThat(body(call(reader, get("/v1/assessments"))).get("items")).isEmpty();
        assertThat(call(privateAgent, get("/v1/assessments/" + a.getId())).getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("the bundle is JSON with the assessment, or a ZIP of GeoParquet files")
    void bundle() throws Exception {
        AssessmentEntity a = assessment(false);
        JsonNode bundle = body(call(reader, get("/v1/assessments/" + a.getId() + "/bundle")));
        assertThat(bundle.get("assessment").get("id").asString()).isEqualTo(a.getId());
        assertThat(bundle.has("objects")).isTrue();
        assertThat(bundle.has("manifests")).isTrue();
        MockHttpServletResponse zip = call(reader, get("/v1/assessments/" + a.getId() + "/bundle?format=geoparquet"));
        assertThat(zip.getContentType()).isEqualTo("application/zip");
        try (ZipInputStream in = new ZipInputStream(new java.io.ByteArrayInputStream(zip.getContentAsByteArray()))) {
            assertThat(in.getNextEntry().getName()).isEqualTo("assessment.json");
        }
        assertThat(call(reader, get("/v1/assessments/" + a.getId() + "/bundle?format=csv")).getStatus())
                .isEqualTo(400);
    }

    @Test
    @DisplayName("feedback is a read-scope write that lands in the 102 analyst feedback")
    void feedback() throws Exception {
        AssessmentEntity a = assessment(false);
        String path = "/v1/assessments/" + a.getId() + "/feedback";
        MockHttpServletResponse ok = call(reader, json(post(path), "{\"rating\":\"wrong\",\"comment\":\"Old news.\"}"));
        assertThat(ok.getStatus()).isEqualTo(201);
        assertThat(body(ok).get("rating").asString()).isEqualTo("wrong");
        assertThat(jdbc.queryForObject("select count(*) from analyst_feedback where target = 'assessment'",
                Long.class)).isEqualTo(1);
        assertThat(call(reader, json(post(path), "{\"rating\":\"meh\"}")).getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("on-demand situation assessments are agent and always authenticated; listed per situation")
    void situationAssessments() throws Exception {
        String id = situation("Red Sea shipping", "warning").getId();
        String path = "/v1/situations/" + id + "/assessment";
        assertThat(call(null, json(post(path), "{\"roles\":[]}")).getStatus()).isEqualTo(401);
        assertThat(call(reader, json(post(path), "{\"roles\":[]}")).getStatus()).isEqualTo(403);
        MockHttpServletResponse accepted = call(agent, json(post(path), "{\"roles\":[]}"));
        assertThat(accepted.getStatus()).isEqualTo(202);
        String runId = body(accepted).get("id").asString();
        org.awaitility.Awaitility.await().atMost(Duration.ofSeconds(30)).until(() -> CycleVocabulary.DIGEST_ONLY
                .equals(runs.findById(runId).orElseThrow().getStatus()));
        JsonNode list = body(call(reader, get("/v1/situations/" + id + "/assessments")));
        assertThat(list.get("items")).hasSize(1);
        assertThat(list.get("items").get(0).get("cadence").asString()).isEqualTo("on-demand");
        assertThat(call(agent, json(post("/v1/situations/01JZZZZZZZZZZZZZZZZZZZZZZZ/assessment"), "{}")).getStatus())
                .isEqualTo(404);
        assertThat(call(agent, json(post(path), "{\"effort\":\"max\"}")).getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("the intel board: read, ≤ 8 KB, ETag with 304, Cache-Control max-age=60")
    void intelBoard() throws Exception {
        assessment(false);
        MockHttpServletResponse first = call(null, get("/v1/intel/board"));
        assertThat(first.getStatus()).isEqualTo(200);
        assertThat(first.getContentAsByteArray().length).isLessThanOrEqualTo(IntelBoardBuilder.MAX_BYTES);
        assertThat(first.getHeader(HttpHeaders.CACHE_CONTROL)).contains("max-age=60");
        String etag = first.getHeader(HttpHeaders.ETAG);
        assertThat(etag).isNotNull();
        JsonNode board = body(first);
        assertThat(board.get("latestAssessments")).hasSize(1);
        assertThat(board.has("situations")).isTrue();
        assertThat(call(null, get("/v1/intel/board").header(HttpHeaders.IF_NONE_MATCH, etag)).getStatus())
                .isEqualTo(304);
    }

    @Test
    @DisplayName("the maintenance trigger is agent and always authenticated; one at a time")
    void maintenance() throws Exception {
        assertThat(call(null, post("/v1/analysis/maintenance/run")).getStatus()).isEqualTo(401);
        assertThat(call(reader, post("/v1/analysis/maintenance/run")).getStatus()).isEqualTo(403);
        MockHttpServletResponse accepted = call(agent, post("/v1/analysis/maintenance/run"));
        assertThat(accepted.getStatus()).isEqualTo(202);
        assertThat(body(accepted).get("steps")).hasSize(MonthlyMaintenance.STEPS.size());
        assertThat(body(accepted).get("trigger").asString()).isEqualTo("manual");
    }

    McpCaller caller(Scope scope, boolean privateAccess) {
        AuthenticatedToken token = new AuthenticatedToken(UUID.randomUUID(), "cycle-mcp", scope,
                TokenRestrictions.NONE, null, null, false, privateAccess);
        return new McpCaller(token, new ReadCaller(scope, privateAccess, false, ReadRestriction.NONE, privateAccess),
                Set.of(), null);
    }

    @Test
    @DisplayName("MCP: get_assessment, list_assessments, get_intel_board (read) and run_cycle (agent, cost-bearing)")
    void mcpTools() {
        assertThat(getTool.requiredScope()).isEqualTo(Scope.READ);
        assertThat(listTool.requiredScope()).isEqualTo(Scope.READ);
        assertThat(boardTool.requiredScope()).isEqualTo(Scope.READ);
        assertThat(runTool.requiredScope()).isEqualTo(Scope.AGENT);
        assertThat(runTool.costBearing()).isTrue();
        AssessmentEntity a = assessment(false);
        ToolContext ctx = new ToolContext(caller(Scope.READ, false), null, "test");
        JsonNode one = getTool.call(ctx, JSON.readTree("{\"assessmentId\":\"" + a.getId() + "\"}")).structured();
        assertThat(one.get("id").asString()).isEqualTo(a.getId());
        assertThat(one.get("html").isNull()).isTrue();
        JsonNode latest = getTool.call(ctx, JSON.readTree("{\"cadence\":\"weekly\"}")).structured();
        assertThat(latest.get("id").asString()).isEqualTo(a.getId());
        JsonNode list = listTool.call(ctx, JSON.readTree("{\"limit\":5}")).structured();
        assertThat(list.get("items")).hasSize(1);
        assertThat(list.get("total").asInt()).isEqualTo(1);
        JsonNode b = boardTool.call(ctx, JSON.readTree("{}")).structured();
        assertThat(b.has("generatedAt")).isTrue();
        assertThatThrownBy(() -> getTool.call(ctx, JSON.readTree("{\"assessmentId\":\"01JZZZZZZZZZZZZZZZZZZZZZZZ\"}")))
                .isInstanceOf(NotFoundException.class);
        AnalysisCycleEntity cycle = cycle("daily", "[]", null, null);
        JsonNode run = runTool.call(new ToolContext(caller(Scope.AGENT, false), null, "test"),
                JSON.readTree("{\"cycleId\":\"" + cycle.getId() + "\"}")).structured();
        assertThat(run.get("cycleId").asString()).isEqualTo(cycle.getId());
    }
}
