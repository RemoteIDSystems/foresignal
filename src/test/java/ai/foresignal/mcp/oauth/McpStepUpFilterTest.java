package ai.foresignal.mcp.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.McpProperties;
import ai.foresignal.mcp.McpToolRegistry;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.McpOAuthGate;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import jakarta.servlet.ServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@link McpStepUpFilter} (prompt 141): an identity-provider token calling a tool above its scope gets {@code 403}
 * with {@code insufficient_scope}; every other request passes with its body intact.
 */
class McpStepUpFilterTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    McpStepUpFilter filter;
    final AtomicReference<ServletRequest> passed = new AtomicReference<>();

    @BeforeEach
    void setUp() {
        ForesignalMcpTool write = mock(ForesignalMcpTool.class);
        when(write.name()).thenReturn("create_watch");
        when(write.requiredScope()).thenReturn(Scope.AGENT);
        ForesignalMcpTool read = mock(ForesignalMcpTool.class);
        when(read.name()).thenReturn("query_records");
        when(read.requiredScope()).thenReturn(Scope.READ);
        McpToolRegistry registry = mock(McpToolRegistry.class);
        when(registry.tool(any())).thenReturn(Optional.empty());
        when(registry.tool("create_watch")).thenReturn(Optional.of(write));
        when(registry.tool("query_records")).thenReturn(Optional.of(read));
        DefaultListableBeanFactory beans = new DefaultListableBeanFactory();
        beans.registerSingleton("registry", registry);
        beans.registerSingleton("properties", new McpProperties(true, new McpProperties.LegacySse(false),
                Duration.ofSeconds(60), 4096));
        McpOAuthGate gate = mock(McpOAuthGate.class);
        when(gate.insufficientScope(any(), eq(Scope.AGENT))).thenReturn("Bearer error=\"insufficient_scope\","
                + " scope=\"foresignal:agent\", resource_metadata=\"http://localhost/.well-known/oauth-protected-resource/mcp\"");
        filter = new McpStepUpFilter(beans.getBeanProvider(McpToolRegistry.class),
                beans.getBeanProvider(McpProperties.class), gate, JSON);
    }

    static MockHttpServletRequest request(String body, Scope scope) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/mcp");
        request.setContentType("application/json");
        request.setContent(body.getBytes(StandardCharsets.UTF_8));
        if (scope != null) {
            AuthenticatedToken token = new AuthenticatedToken(UUID.randomUUID(), "oauth:ada", scope,
                    TokenRestrictions.NONE, null, "01JPRINCIPAL0000000000000A", false);
            request.setAttribute(McpOAuthGate.ATTRIBUTE, new McpOAuthGate.Accepted(token, "https://idp", "ada",
                    Set.of("foresignal:read")));
        }
        return request;
    }

    MockHttpServletResponse run(MockHttpServletRequest request) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, (req, res) -> passed.set(req));
        return response;
    }

    static String call(Object id, String tool) {
        return "{\"jsonrpc\":\"2.0\",\"id\":" + id + ",\"method\":\"tools/call\",\"params\":{\"name\":\"" + tool
                + "\",\"arguments\":{}}}";
    }

    @Test
    @DisplayName("a read token calling an agent tool: 403, insufficient_scope header, JSON-RPC error with the id")
    void refused() throws Exception {
        MockHttpServletResponse response = run(request(call(7, "create_watch"), Scope.READ));
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getHeader("WWW-Authenticate")).contains("error=\"insufficient_scope\"")
                .contains("scope=\"foresignal:agent\"").contains("resource_metadata=");
        JsonNode body = JSON.readTree(response.getContentAsString());
        assertThat(body.get("id").asInt()).isEqualTo(7);
        assertThat(body.get("error").get("message").asString()).contains("create_watch").contains("agent");
        assertThat(passed.get()).isNull();
    }

    @Test
    @DisplayName("a batch with one call above scope is refused; string ids are echoed")
    void batch() throws Exception {
        MockHttpServletResponse response = run(request("[" + call(1, "query_records") + "," + call("\"b\"",
                "create_watch") + "]", Scope.READ));
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(JSON.readTree(response.getContentAsString()).get("id").asString()).isEqualTo("b");
    }

    @Test
    @DisplayName("allowed calls, other methods, unknown tools and non-JSON pass through with the body intact")
    void passes() throws Exception {
        for (String body : new String[] {call(1, "query_records"), call(2, "unknown_tool"),
                "{\"jsonrpc\":\"2.0\",\"id\":3,\"method\":\"tools/list\"}", "not json", "",
                "{\"method\":\"tools/call\",\"params\":{}}", "[1,2]", "null"}) {
            passed.set(null);
            MockHttpServletResponse response = run(request(body, Scope.READ));
            assertThat(response.getStatus()).as(body).isEqualTo(200);
            assertThat(passed.get()).as(body).isNotNull();
            assertThat(new String(passed.get().getInputStream().readAllBytes(), StandardCharsets.UTF_8))
                    .as(body).isEqualTo(body);
        }
        passed.set(null);
        run(request(call(4, "create_watch"), Scope.AGENT));
        assertThat(new String(passed.get().getReader().lines().reduce("", String::concat))).contains("create_watch");
        assertThat(passed.get().getContentLength()).isEqualTo(call(4, "create_watch").length());
    }

    @Test
    @DisplayName("requests without an identity-provider token, GETs and other paths are not inspected")
    void notInspected() throws Exception {
        MockHttpServletRequest apiToken = request(call(1, "create_watch"), null);
        assertThat(run(apiToken).getStatus()).isEqualTo(200);
        assertThat(passed.get()).isSameAs(apiToken);
        MockHttpServletRequest get = request("", Scope.READ);
        get.setMethod("GET");
        run(get);
        assertThat(passed.get()).isSameAs(get);
        MockHttpServletRequest rest = request(call(1, "create_watch"), Scope.READ);
        rest.setRequestURI("/v1/records");
        run(rest);
        assertThat(passed.get()).isSameAs(rest);
    }

    @Test
    @DisplayName("a body over max-request-bytes is not inspected and reaches the transport whole")
    void oversized() throws Exception {
        String body = call(1, "create_watch").replace("{}}", "{\"pad\":\"" + "x".repeat(5000) + "\"}}");
        MockHttpServletResponse response = run(request(body, Scope.READ));
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(new String(passed.get().getInputStream().readAllBytes(), StandardCharsets.UTF_8)).isEqualTo(body);
        assertThat(passed.get().getContentLength()).isEqualTo(body.length());
    }
}
