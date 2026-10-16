package ai.foresignal.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.InfraController;
import ai.foresignal.api.InfraRulesController;
import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.mcp.McpSessions;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.mcp.tools.GetAffectedInfrastructureTool;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.testsupport.ingest.TestRecords;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import tools.jackson.databind.json.JsonMapper;

/** The infrastructure controllers, rules settings and MCP tool (prompt 104). */
@WebIntegrationTest
@Import(TestIngestSources.class)
class InfraControllersIT {

    @Autowired InfraController infra;
    @Autowired InfraRulesController rules;
    @Autowired InfraGraphBuilder builder;
    @Autowired GetAffectedInfrastructureTool affectedTool;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired JsonMapper json;

    private static ToolContext anonymous() {
        return new ToolContext(new McpCaller(null, new ReadCaller(null, false, true), Set.of(), "127.0.0.1"),
                "session", McpSessions.STREAMABLE);
    }

    @Test
    @DisplayName("before any build: status says not built and reads are 409")
    void beforeBuild() {
        assertThat(infra.getInfraStatus()).containsEntry("built", false).containsEntry("building", false);
        assertThatThrownBy(() -> infra.listInfraNodes(null, null, null, null, "10"))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("reads serve only the current build, page with a cursor and carry attribution")
    @SuppressWarnings("unchecked")
    void reads() {
        InfraGraphBuilderIT.seed(records, clock);
        InfraBuildEntity first = builder.rebuild(Map.of());
        InfraBuildEntity second = builder.rebuild(Map.of());
        Map<String, Object> page = infra.listInfraNodes(null, null, null, null, "2");
        assertThat(page).containsEntry("buildId", second.getId());
        assertThat((List<?>) page.get("items")).hasSize(2);
        assertThat(page.get("nextCursor")).isNotNull();
        Map<String, Object> rest = infra.listInfraNodes(null, null, null, String.valueOf(page.get("nextCursor")), "2");
        assertThat((List<?>) rest.get("items")).hasSize(1);
        assertThat(rest.get("nextCursor")).isNull();
        assertThat((List<String>) rest.get("attribution")).isNotEmpty();
        assertThat((List<?>) infra.listInfraNodes(null, List.of("health.hospital"), null, null, "10").get("items"))
                .hasSize(1);
        assertThat((List<?>) infra.listInfraNodes("-95.01,31.99,-94.97,32.01", null, null, null, "10").get("items"))
                .hasSize(2);
        assertThatThrownBy(() -> infra.listInfraNodes(null, null, null, "garbage", "10"))
                .isInstanceOf(ValidationException.class);

        Map<String, Object> edges = infra.listInfraEdges(null, List.of("proximity"), null, null, "10");
        assertThat((List<Map<String, Object>>) edges.get("items")).hasSize(2)
                .allSatisfy(e -> assertThat(e).containsKeys("ruleId", "ruleVersion", "params", "evidence", "licence"));
        assertThat((List<String>) edges.get("attribution")).contains("© OpenStreetMap contributors");

        long nodeId = ((Number) ((List<Map<String, Object>>) page.get("items")).getFirst().get("id")).longValue();
        Map<String, Object> node = infra.getInfraNode(nodeId);
        assertThat(node).containsKeys("edges", "attribution", "geometry");
        assertThat((Map<String, Object>) node.get("edges")).containsKeys("in", "out");
        assertThat((Map<String, Object>) node.get("geometry")).containsKey("type").containsKey("coordinates");
        assertThat((Map<String, Object>) node.get("point")).containsEntry("type", "Point");
        assertThat(first.getId()).isLessThan(second.getId());
    }

    @Test
    @DisplayName("a node of a superseded build is 404")
    void supersededNode() {
        InfraGraphBuilderIT.seed(records, clock);
        builder.rebuild(Map.of());
        assertThatThrownBy(() -> infra.getInfraNode(-1)).isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("rebuild answers 202 with a build record; status reports the current build")
    void rebuildAndStatus() {
        InfraGraphBuilderIT.seed(records, clock);
        Map<String, Object> started = infra.rebuildInfraGraph();
        assertThat(started).containsKeys("id", "status", "startedAt", "rulesVersion");
        Instant deadline = Instant.now().plus(Duration.ofSeconds(60));
        while (!Boolean.TRUE.equals(infra.getInfraStatus().get("built")) && Instant.now().isBefore(deadline)) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        Map<String, Object> status = infra.getInfraStatus();
        assertThat(status).containsEntry("built", true).containsKeys("lastBuild", "nodesByType");
    }

    @Test
    @DisplayName("the affected endpoint takes exactly one seed, and the MCP tool puts caveats first")
    void affected() {
        InfraGraphBuilderIT.seed(records, clock);
        builder.rebuild(Map.of());
        Map<String, Object> result = infra.computeAffectedInfrastructure(json.readTree("{\"geometry\":{\"type\":"
                + "\"Polygon\",\"coordinates\":[[[-95.01,31.99],[-94.99,31.99],[-94.99,32.01],[-95.01,32.01],"
                + "[-95.01,31.99]]]},\"depth\":2}"), new MockHttpServletRequest());
        assertThat(result).containsKeys("direct", "dependents", "caveats", "attribution", "buildId");
        assertThatThrownBy(() -> infra.computeAffectedInfrastructure(json.readTree("{\"depth\":2}"),
                new MockHttpServletRequest())).isInstanceOf(ValidationException.class);

        ToolResult tool = affectedTool.call(anonymous(), json.readTree("{\"record\":{\"sourceId\":"
                + "\"wri-power-plants\",\"externalId\":\"pp1\"},\"depth\":2}"));
        assertThat(tool.structured().properties().iterator().next().getKey()).isEqualTo("caveats");
        assertThat(tool.structured().path("attribution").toString()).contains("OpenStreetMap");
    }

    @Test
    @DisplayName("rules are read and updated")
    void rulesCrud() {
        assertThat(rules.getInfraRules()).isNotNull();
        rules.updateInfraRules(json.readTree("{\"version\":2}"));
        assertThat(rules.getInfraRules()).containsEntry("version", 2);
    }
}
