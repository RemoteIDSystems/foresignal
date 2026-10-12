package ai.foresignal.mcp.tools;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.mcp.McpToolException;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.ReadScopes;
import ai.foresignal.stats.glance.GlanceService;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code get_glance} (prompt 038): JSON and text forms, the read scope, and {@code place} refused
 * without a token.
 */
class GetGlanceToolTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    @SuppressWarnings("unchecked")
    final ObjectProvider<ReadScopes> scopes = mock(ObjectProvider.class);
    final GlanceService glance = mock(GlanceService.class);
    final GetGlanceTool tool = new GetGlanceTool(glance, scopes, JSON);

    static ToolContext anonymous() {
        return new ToolContext(new McpCaller(null, new ReadCaller(null, false, true), Set.of(), "127.0.0.1"), "s", "t");
    }

    @Test
    @DisplayName("a read tool whose description starts with the untrusted-content note")
    void shape() {
        assertThat(tool.name()).isEqualTo("get_glance");
        assertThat(tool.requiredScope()).isEqualTo(Scope.READ);
        assertThat(tool.description()).startsWith(ai.foresignal.mcp.ForesignalMcpTool.UNTRUSTED_NOTE);
        assertThat(tool.inputSchema().toString()).contains("place").contains("format");
    }

    @Test
    @DisplayName("json by default, text on request")
    void forms() {
        when(glance.json(any())).thenReturn("{\"status\":\"ok\",\"lastSeq\":5}");
        when(glance.text(any())).thenReturn("FORESIGNAL OK\nSEQ 5\n");

        ToolResult json = tool.call(anonymous(), JSON.readTree("{}"));
        assertThat(json.structured().get("status").asString()).isEqualTo("ok");
        ToolResult text = tool.call(anonymous(), JSON.valueToTree(Map.of("format", "text")));
        assertThat(text.structured()).isNull();
        assertThat(text.content().toString()).contains("SEQ 5");
    }

    @Test
    @DisplayName("place needs a token; an unknown format is refused")
    void refusals() {
        assertThatThrownBy(() -> tool.call(anonymous(), JSON.valueToTree(Map.of("place", "home"))))
                .isInstanceOf(McpToolException.class).hasMessageContaining("token");
        assertThatThrownBy(() -> tool.call(anonymous(), JSON.valueToTree(Map.of("format", "xml"))))
                .isInstanceOf(McpToolException.class);
    }
}
