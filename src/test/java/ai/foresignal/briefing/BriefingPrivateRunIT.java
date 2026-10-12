package ai.foresignal.briefing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.mcp.McpSessions;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.tools.GetBriefingRunTool;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.Scope;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.briefing.BriefingHttp;
import ai.foresignal.testsupport.briefing.BriefingItSupport;
import ai.foresignal.testsupport.briefing.BriefingTestSources;
import ai.foresignal.testsupport.security.TestTokens;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;

/**
 * Briefing runs that cite a private source (prompt 039): an admin-owned briefing lists private records,
 * so its run is {@code 404} on {@code getBriefingRun} and {@code getLatestBriefingRun} and left out of
 * {@code listBriefingRuns} for callers that cannot see private sources, and visible to admin and to a
 * token with the private grant. The MCP {@code get_briefing_run} tool also hides it from callers without
 * the grant and from an Ask loop whose excluded set holds the cited source.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(BriefingTestSources.class)
class BriefingPrivateRunIT extends BriefingItSupport {

    @Autowired MockMvc mvc;
    @Autowired GetBriefingRunTool runTool;

    BriefingHttp http;
    String granted;
    UUID briefingId;
    UUID runId;

    @BeforeEach
    void seedRun() {
        http = new BriefingHttp(mvc);
        granted = TestTokens.bearer(TestTokens.create(tokens, "briefing-private-reader", Scope.READ, true));
        quake("q1", "Tokyo Bay", 5.5);
        secretQuake("s1", "Hidden Lake", 7.0);
        Map<String, Object> created = briefings.create(json(digestBody("Family digest", "0 7 * * *", null)), admin());
        briefingId = id(created);
        Map<String, Object> run = briefings.runNow(briefingId);
        runId = UUID.fromString(String.valueOf(run.get("id")));
        assertThat(briefings.citesPrivate(briefings.run(briefingId, runId)))
                .as("the admin-owned digest cites the private source").isTrue();
    }

    private String runPath() {
        return "/v1/briefings/" + briefingId + "/runs/" + runId;
    }

    @Test
    @DisplayName("REST: 404 for a read token, visible to admin and to the private grant; latest likewise")
    void rest() throws Exception {
        http.call("GET", runPath(), reader, null, 404);
        http.call("GET", "/v1/briefings/" + briefingId + "/runs/latest", reader, null, 404);
        http.call("GET", runPath(), agent, null, 404);
        http.call("GET", runPath(), admin, null, 200);
        JsonNode seen = http.call("GET", runPath(), granted, null, 200);
        assertThat(seen.toString()).contains("Hidden Lake");
        http.call("GET", "/v1/briefings/" + briefingId + "/runs/latest", granted, null, 200);
    }

    @Test
    @DisplayName("REST: listBriefingRuns leaves the run out for callers that cannot see private sources")
    void list() throws Exception {
        JsonNode hidden = http.call("GET", "/v1/briefings/" + briefingId + "/runs", reader, null, 200);
        assertThat(hidden.toString()).doesNotContain(runId.toString());
        JsonNode shown = http.call("GET", "/v1/briefings/" + briefingId + "/runs", granted, null, 200);
        assertThat(shown.toString()).contains(runId.toString());
        assertThat(http.call("GET", "/v1/briefings/" + briefingId + "/runs", admin, null, 200).toString())
                .contains(runId.toString());
    }

    private static ToolContext context(Scope scope, boolean privateAccess, Set<String> excluded) {
        AuthenticatedToken token = new AuthenticatedToken(UUID.randomUUID(), "tool-" + scope.wireValue(), scope, null,
                null, null, false, privateAccess);
        McpCaller caller = new McpCaller(token, new ReadCaller(scope, false, true, null, privateAccess), excluded,
                "127.0.0.1");
        return new ToolContext(caller, "session", McpSessions.STREAMABLE);
    }

    private JsonNode args() {
        return json("{\"briefingId\":\"" + briefingId + "\",\"runId\":\"" + runId + "\"}");
    }

    @Test
    @DisplayName("MCP get_briefing_run: hidden without the grant and when the cited source is excluded")
    void mcpTool() {
        assertThatThrownBy(() -> runTool.call(context(Scope.READ, false, Set.of(BriefingTestSources.SECRET)), args()))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> runTool.call(context(Scope.AGENT, true, Set.of(BriefingTestSources.SECRET)), args()))
                .as("an Ask loop for a remote provider excludes the private source").isInstanceOf(NotFoundException.class);
        assertThat(runTool.call(context(Scope.AGENT, true, Set.of()), args()).structured().toString())
                .contains("Hidden Lake");
        assertThat(runTool.call(context(Scope.ADMIN, false, Set.of()), args()).structured().toString())
                .contains("Hidden Lake");
    }
}
