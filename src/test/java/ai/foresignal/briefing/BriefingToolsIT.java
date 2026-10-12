package ai.foresignal.briefing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.ask.AskActionHandler;
import ai.foresignal.ask.AskCaller;
import ai.foresignal.mcp.McpSessions;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.tools.CreateBriefingTool;
import ai.foresignal.mcp.tools.GetBriefingRunTool;
import ai.foresignal.mcp.tools.GetDigestTool;
import ai.foresignal.mcp.tools.ListBriefingsTool;
import ai.foresignal.mcp.tools.RunBriefingTool;
import ai.foresignal.security.Scope;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.briefing.BriefingItSupport;
import ai.foresignal.testsupport.briefing.BriefingTestSources;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.JsonNode;

/**
 * The briefing MCP tools and Ask's {@code create_briefing} action (prompt 029): well-formed contracts,
 * scopes ({@code run_briefing} cost-bearing), and each tool and the action going through the same
 * service and validation as REST.
 */
@IntegrationTest
@Import(BriefingTestSources.class)
class BriefingToolsIT extends BriefingItSupport {

    @Autowired ListBriefingsTool listTool;
    @Autowired GetBriefingRunTool runTool;
    @Autowired GetDigestTool digestTool;
    @Autowired CreateBriefingTool createTool;
    @Autowired RunBriefingTool runNowTool;
    @Autowired List<AskActionHandler> handlers;

    static ToolContext context(Scope scope) {
        return new ToolContext(caller(scope, Set.of(BriefingTestSources.SECRET)), "session", McpSessions.STREAMABLE);
    }

    @Test
    @DisplayName("reads are read, create and run are agent, run is cost-bearing (McpServerIT checks every schema)")
    void contracts() {
        for (var tool : List.of(listTool, runTool, digestTool, createTool, runNowTool)) {
            assertThat(tool.inputSchema().get("additionalProperties")).as(tool.name()).isEqualTo(false);
        }
        assertThat(listTool.requiredScope()).isEqualTo(Scope.READ);
        assertThat(runTool.requiredScope()).isEqualTo(Scope.READ);
        assertThat(digestTool.requiredScope()).isEqualTo(Scope.READ);
        assertThat(createTool.requiredScope()).isEqualTo(Scope.AGENT);
        assertThat(runNowTool.requiredScope()).isEqualTo(Scope.AGENT);
        assertThat(runNowTool.costBearing()).isTrue();
        assertThat(createTool.costBearing()).isFalse();
        assertThat(digestTool.description()).startsWith(ai.foresignal.mcp.ForesignalMcpTool.UNTRUSTED_NOTE);
    }

    @Test
    @DisplayName("create, list, run and read a run through the tools; the digest tool returns the cursor")
    void toolsEndToEnd() {
        quake("q1", "Tokyo Bay", 5.5);
        JsonNode created = createTool.call(context(Scope.AGENT),
                json("{\"briefing\":" + digestBody("Via MCP", "0 7 * * *", null) + "}")).structured();
        String id = created.get("id").asString();
        assertThat(created.get("name").asString()).isEqualTo("Via MCP");

        JsonNode listed = listTool.call(context(Scope.READ), json("{}")).structured();
        assertThat(listed.get("total").asInt()).isEqualTo(1);

        JsonNode run = runNowTool.call(context(Scope.AGENT), json("{\"briefingId\":\"" + id + "\"}")).structured();
        assertThat(run.get("status").asString()).isEqualTo("succeeded");
        assertThat(run.has("stats")).isFalse();

        JsonNode latest = runTool.call(context(Scope.READ), json("{\"briefingId\":\"" + id + "\"}")).structured();
        assertThat(latest.get("id").asString()).isEqualTo(run.get("id").asString());
        assertThat(latest.get("title").asString()).isEqualTo("Via MCP");
        assertThat(latest.has("stats")).isFalse();
        JsonNode longForm = runTool.call(context(Scope.READ), json("{\"briefingId\":\"" + id + "\",\"runId\":\""
                + run.get("id").asString() + "\",\"detail\":\"long\"}")).structured();
        assertThat(longForm.has("stats")).isTrue();

        JsonNode digest = digestTool.call(context(Scope.READ), json("{\"kind\":[\"" + BriefingTestSources.QUAKE_KIND
                + "\"],\"limit\":3}")).structured();
        assertThat(digest.at("/next/sinceSeq").asLong()).isEqualTo(digest.get("toSeq").asLong());
        assertThat(digest.at("/counts/0/new").asLong()).isEqualTo(1);

        assertThatThrownBy(() -> createTool.call(context(Scope.AGENT),
                json("{\"briefing\":{\"name\":\"x\",\"schedule\":\"* * * * *\"}}")))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("Ask's create_briefing action validates like REST and creates the briefing with the confirming owner")
    void askAction() {
        AskActionHandler handler = handlers.stream().filter(h -> h.type().equals("create_briefing")).findFirst()
                .orElseThrow();
        AskCaller asker = new AskCaller(agentCaller());
        JsonNode payload = json(digestBody("Proposed", "30 6 * * 1-5", null));

        assertThat(handler.validate(payload, asker)).isEqualTo(payload);
        assertThat(handler.describe(payload)).contains("Proposed").contains("30 6 * * 1-5").contains("UTC");
        assertThatThrownBy(() -> handler.validate(json("{\"name\":\"bad\",\"schedule\":\"*/5 * * * *\"}"), asker))
                .isInstanceOf(ValidationException.class);

        JsonNode result = handler.execute(payload, asker);
        assertThat(result.get("name").asString()).isEqualTo("Proposed");
        BriefingEntity stored = briefingRepository.findAll().getFirst();
        assertThat(stored.getOwnerScope()).isEqualTo("agent");
        assertThat(stored.getSchedule()).isEqualTo("30 6 * * 1-5");
    }
}
