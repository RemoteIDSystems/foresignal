package ai.foresignal.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.analytics.finding.AnalyticFindingEntity;
import ai.foresignal.analytics.finding.FindingDraft;
import ai.foresignal.analytics.finding.FindingService;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.mcp.tools.GetFindingTool;
import ai.foresignal.mcp.tools.GetSeriesDataTool;
import ai.foresignal.mcp.tools.ListAnalysisSeriesTool;
import ai.foresignal.mcp.tools.ListFindingsTool;
import ai.foresignal.mcp.tools.RunEventStudyTool;
import ai.foresignal.mcp.tools.UpsertAnalysisSeriesTool;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.source.series.SeriesMeta;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.analytics.AnalyticsItSupport;
import ai.foresignal.testsupport.analytics.AnalyticsTestSources;
import ai.foresignal.testsupport.security.TestTokens;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
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
 * The prompt 119 discovery endpoints over HTTP and the MCP tools: series data, the registry, findings in three
 * sections, dismissal, batch event studies, their scopes ({@code read} reads, {@code agent} changes), the Problem codes
 * and the tool shapes.
 */
@IntegrationTest
@Import(AnalyticsTestSources.class)
class AnalyticsControllersIT extends AnalyticsItSupport {

    @Autowired MockMvc mvc;
    @Autowired TokenService tokens;
    @Autowired JsonMapper json;
    @Autowired FindingService findings;
    @Autowired ListFindingsTool listFindings;
    @Autowired GetFindingTool getFinding;
    @Autowired GetSeriesDataTool getSeriesData;
    @Autowired ListAnalysisSeriesTool listSeries;
    @Autowired UpsertAnalysisSeriesTool upsertSeries;
    @Autowired RunEventStudyTool runEventStudy;

    String read;
    String agent;

    @BeforeEach
    void tokens() {
        read = TestTokens.bearer(TestTokens.create(tokens, "analytics-read", Scope.READ));
        agent = TestTokens.bearer(TestTokens.create(tokens, "analytics-agent", Scope.AGENT));
        seedDaily(AnalyticsTestSources.SERIES, "oil", T0, constant(10, 70),
                SeriesMeta.of(SeriesMeta.PRICE, SeriesMeta.LOG_RETURN, 1));
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

    AnalyticFindingEntity finding() {
        return findings.record(new FindingDraft("change-point", "Change point: oil up 50%", "significant", null,
                List.of("YE"), T0, T0.plus(Duration.ofDays(1)), T0.plus(Duration.ofDays(2)),
                List.of("series:" + AnalyticsTestSources.SERIES + "/oil"), List.of(), 7, Map.of("effect", 0.5),
                Map.of("value", 1.0, "unit", "alarms-per-year", "method", "detector-false-alarm-rate", "text",
                        "once a year"), List.of(Map.of("code", "NOT_GROUND_TRUTH", "text", "t")), "changepoint-cusum",
                "1.0.0", Map.of(), "oil rose", false, "markets", List.of(), List.of(), "x")).finding();
    }

    @Test
    @DisplayName("series data: points and gaps; asOf; SERIES_REF_INVALID 400 and SERIES_NOT_FOUND 404")
    void seriesData() throws Exception {
        String ref = "series:" + AnalyticsTestSources.SERIES + "/oil";
        MockHttpServletResponse ok = call(read, get("/v1/analytics/series/data").param("ref", ref)
                .param("from", T0.toString()).param("to", T0.plus(Duration.ofDays(12)).toString()));
        assertThat(ok.getStatus()).as(ok.getContentAsString()).isEqualTo(200);
        JsonNode data = body(ok);
        assertThat(data.get("points")).hasSize(10);
        assertThat(data.get("gaps")).hasSize(1);
        assertThat(data.get("valuesWithheld").asBoolean()).isFalse();
        assertThat(data.get("points").get(0).has("knownAt")).isTrue();
        assertThat(call(read, get("/v1/analytics/series/data").param("ref", "nope")).getStatus()).isEqualTo(400);
        MockHttpServletResponse missing = call(read, get("/v1/analytics/series/data").param("ref",
                "series:" + AnalyticsTestSources.SERIES + "/none"));
        assertThat(missing.getStatus()).isEqualTo(404);
        assertThat(body(missing).get("code").asString()).isEqualTo("SERIES_NOT_FOUND");
    }

    @Test
    @DisplayName("registry: read lists, agent upserts (201 then 200) and deletes (204); read may not change it")
    void registry() throws Exception {
        String body = "{\"ref\":\"series:" + AnalyticsTestSources.SERIES + "/oil\",\"family\":\"markets\"}";
        assertThat(call(read, put("/v1/analytics/series").contentType(MediaType.APPLICATION_JSON).content(body))
                .getStatus()).isEqualTo(403);
        assertThat(call(agent, put("/v1/analytics/series").contentType(MediaType.APPLICATION_JSON).content(body))
                .getStatus()).isEqualTo(201);
        assertThat(call(agent, put("/v1/analytics/series").contentType(MediaType.APPLICATION_JSON).content(body))
                .getStatus()).isEqualTo(200);
        JsonNode page = body(call(read, get("/v1/analytics/series").param("family", "markets")));
        assertThat(page.get("items")).hasSize(1);
        assertThat(page.get("items").get(0).get("origin").asString()).isEqualTo("operator");
        assertThat(call(read, get("/v1/analytics/series").param("region", "usa")).getStatus()).isEqualTo(400);
        assertThat(call(agent, delete("/v1/analytics/series").param("ref", "series:" + AnalyticsTestSources.SERIES
                + "/oil")).getStatus()).isEqualTo(204);
        assertThat(call(agent, delete("/v1/analytics/series").param("ref", "series:" + AnalyticsTestSources.SERIES
                + "/oil")).getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("findings: list and get in evidence/inference/caveats sections; dismiss needs agent; 404 UNKNOWN_FINDING")
    void findingsEndpoints() throws Exception {
        AnalyticFindingEntity f = finding();
        JsonNode page = body(call(read, get("/v1/analytics/findings").param("type", "change-point")
                .param("region", "YE")));
        assertThat(page.get("items")).hasSize(1);
        JsonNode one = body(call(read, get("/v1/analytics/findings/" + f.getId())));
        assertThat(one.has("evidence")).isTrue();
        assertThat(one.get("inference").get("baseRate").get("text").asString()).isEqualTo("once a year");
        assertThat(one.get("caveats")).hasSize(1);
        assertThat(call(read, get("/v1/analytics/findings").param("type", "bogus")).getStatus()).isEqualTo(400);
        MockHttpServletResponse unknown = call(read, get("/v1/analytics/findings/01JA8Q3V7S2K9W4T6Y1B5N0M3C"));
        assertThat(unknown.getStatus()).isEqualTo(404);
        assertThat(body(unknown).get("code").asString()).isEqualTo("UNKNOWN_FINDING");

        String dismiss = "{\"status\":\"dismissed\",\"reason\":\"known holiday\"}";
        MediaType mergePatch = MediaType.parseMediaType("application/merge-patch+json");
        assertThat(call(read, patch("/v1/analytics/findings/" + f.getId()).contentType(mergePatch).content(dismiss))
                .getStatus()).isEqualTo(403);
        MockHttpServletResponse dismissed = call(agent, patch("/v1/analytics/findings/" + f.getId())
                .contentType(mergePatch).content(dismiss));
        assertThat(dismissed.getStatus()).as(dismissed.getContentAsString()).isEqualTo(200);
        assertThat(body(dismissed).get("status").asString()).isEqualTo("dismissed");
        assertThat(call(agent, patch("/v1/analytics/findings/" + f.getId()).contentType(mergePatch)
                .content("{\"status\":\"open\"}")).getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("event studies: agent submits a 202 job; read is refused; an invalid selector is EVENT_STUDY_INVALID")
    void eventStudies() throws Exception {
        String body = "{\"selector\":{\"timestamps\":[\"" + T0.plus(Duration.ofDays(5)) + "\"]},\"target\":\"series:"
                + AnalyticsTestSources.SERIES + "/oil\",\"from\":\"" + T0 + "\",\"to\":\""
                + T0.plus(Duration.ofDays(9)) + "\"}";
        assertThat(call(read, post("/v1/analytics/event-studies").contentType(MediaType.APPLICATION_JSON)
                .content(body)).getStatus()).isEqualTo(403);
        MockHttpServletResponse accepted = call(agent, post("/v1/analytics/event-studies")
                .contentType(MediaType.APPLICATION_JSON).content(body));
        assertThat(accepted.getStatus()).as(accepted.getContentAsString()).isEqualTo(202);
        assertThat(body(accepted).get("type").asString()).isEqualTo("event-study");
        MockHttpServletResponse invalid = call(agent, post("/v1/analytics/event-studies")
                .contentType(MediaType.APPLICATION_JSON).content(body.replace("timestamps", "referenceFamily")
                        .replace("[\"" + T0.plus(Duration.ofDays(5)) + "\"]", "\"seismic:m5\"")));
        assertThat(invalid.getStatus()).isEqualTo(400);
        assertThat(body(invalid).get("code").asString()).isEqualTo("EVENT_STUDY_INVALID");
    }

    @Test
    @DisplayName("stats: region= and groupBy=region are served from the region rollups")
    void statsRegions() throws Exception {
        MockHttpServletResponse response = call(read, get("/v1/stats/timeseries").param("groupBy", "region")
                .param("from", T0.toString()).param("to", T0.plus(Duration.ofHours(2)).toString()));
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(body(response).get("backend").asString()).isEqualTo("rollup");
    }

    @Test
    @DisplayName("MCP tools: names, scopes and result shapes")
    void mcpTools() {
        AnalyticFindingEntity f = finding();
        ToolContext reader = new ToolContext(caller(Scope.READ), null, "test");
        ToolContext writer = new ToolContext(caller(Scope.AGENT), null, "test");
        assertThat(listFindings.name()).isEqualTo("list_findings");
        assertThat(listFindings.requiredScope()).isEqualTo(Scope.READ);
        assertThat(upsertSeries.requiredScope()).isEqualTo(Scope.AGENT);
        assertThat(runEventStudy.requiredScope()).isEqualTo(Scope.AGENT);
        assertThat(text(listFindings.call(reader, json.readTree("{\"type\":[\"change-point\"]}")))).contains(f.getId());
        assertThat(text(getFinding.call(reader, json.readTree("{\"findingId\":\"" + f.getId() + "\"}"))))
                .contains("inference").contains("baseRate");
        assertThat(text(getSeriesData.call(reader, json.readTree("{\"ref\":\"series:" + AnalyticsTestSources.SERIES
                + "/oil\"}")))).contains("points");
        assertThat(text(upsertSeries.call(writer, json.readTree("{\"ref\":\"series:" + AnalyticsTestSources.SERIES
                + "/oil\"}")))).contains("operator");
        assertThat(text(listSeries.call(reader, json.readTree("{}")))).contains("series:" + AnalyticsTestSources.SERIES);
        assertThat(text(runEventStudy.call(writer, json.readTree("{\"selector\":{\"timestamps\":[\""
                + T0.plus(Duration.ofDays(5)) + "\"]},\"target\":\"series:" + AnalyticsTestSources.SERIES
                + "/oil\",\"from\":\"" + T0 + "\",\"to\":\"" + T0.plus(Duration.ofDays(9)) + "\"}"))))
                .contains("event-study");
    }

    static String text(ToolResult result) {
        return String.valueOf(result.structured());
    }

    McpCaller caller(Scope scope) {
        AuthenticatedToken token = new AuthenticatedToken(UUID.randomUUID(), "analytics-mcp-" + scope.wireValue(),
                scope, TokenRestrictions.NONE, null, "analytics-mcp", false, false);
        return new McpCaller(token, new ReadCaller(scope, true, false, ReadRestriction.NONE, false), Set.of(), null);
    }
}
