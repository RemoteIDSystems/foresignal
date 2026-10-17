package ai.foresignal.mcp;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.mcp.tools.CheckTripTool;
import ai.foresignal.mcp.tools.GetCpaTool;
import ai.foresignal.mcp.tools.GetEtaTool;
import ai.foresignal.mcp.tools.GetPatternOfLifeTool;
import ai.foresignal.security.Scope;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/** The prompt 114 MCP tools are well formed and carry the scopes the prompt lists (contracts only, no services). */
class KinematicsTripPolToolsTest {

    static Stream<ForesignalMcpTool> tools() {
        return Stream.of(new GetCpaTool(null, null), new GetEtaTool(null, null), new GetPatternOfLifeTool(null, null),
                new CheckTripTool(null, null));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("tools")
    @DisplayName("each tool's schema, description and annotations are well formed")
    void wellFormed(ForesignalMcpTool tool) {
        McpToolSchemaTest.assertWellFormed(tool);
        assertThat(tool.costBearing()).isFalse();
        Scope expected = tool.name().startsWith("get_c") || tool.name().equals("get_eta") ? Scope.READ : Scope.AGENT;
        assertThat(tool.requiredScope()).isEqualTo(expected);
    }
}
