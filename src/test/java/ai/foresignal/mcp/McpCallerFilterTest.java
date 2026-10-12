package ai.foresignal.mcp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.settings.CorsSettings;
import ai.foresignal.settings.CorsSettingsService;
import java.time.Clock;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

/** The MCP request filter (prompt 025): Origin check, caller attribute, session refusal, binding scope. */
class McpCallerFilterTest {

    private McpSessions sessions;
    private McpCallerResolver callers;
    private McpCallerFilter filter;
    private final McpCaller anonymous = new McpCaller(null, new ReadCaller(null, false, true), Set.of(), "127.0.0.1");

    @BeforeEach
    void setUp() {
        sessions = new McpSessions(Clock.systemUTC());
        callers = mock(McpCallerResolver.class);
        CorsSettingsService cors = mock(CorsSettingsService.class);
        when(cors.get()).thenReturn(new CorsSettings(List.of(), List.of("https://app.example")));
        filter = new McpCallerFilter(callers, sessions, cors, JsonMapper.builder().build());
    }

    private MockHttpServletRequest request(String origin, String sessionId) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/mcp");
        request.addHeader("Host", "localhost:8080");
        if (origin != null) {
            request.addHeader("Origin", origin);
        }
        if (sessionId != null) {
            request.addHeader("Mcp-Session-Id", sessionId);
        }
        when(callers.resolve(request)).thenReturn(anonymous);
        return request;
    }

    @Test
    @DisplayName("only /mcp paths are filtered")
    void paths() {
        assertThat(McpCallerFilter.isMcpPath("/mcp")).isTrue();
        assertThat(McpCallerFilter.isMcpPath("/mcp/sse")).isTrue();
        assertThat(McpCallerFilter.isMcpPath("/mcpx")).isFalse();
        assertThat(McpCallerFilter.isMcpPath("/v1/records")).isFalse();
        assertThat(McpCallerFilter.isMcpPath(null)).isFalse();
    }

    @Test
    @DisplayName("a request passes with its caller set, and a session opened during it is bound to that caller")
    void passesAndBinds() throws Exception {
        MockHttpServletRequest request = request(null, null);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain(new jakarta.servlet.http.HttpServlet() {
            @Override
            protected void service(jakarta.servlet.http.HttpServletRequest req,
                    jakarta.servlet.http.HttpServletResponse res) {
                sessions.bind("opened", McpSessions.STREAMABLE);
            }
        });
        filter.doFilter(request, response, chain);
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(request.getAttribute(McpCaller.CONTEXT_KEY)).isSameAs(anonymous);
        assertThat(sessions.matches("opened", anonymous)).isTrue();
    }

    @Test
    @DisplayName("a session of another caller is refused with 403 and a JSON-RPC error")
    void foreignSession() throws Exception {
        sessions.bind("foreign", McpSessions.STREAMABLE);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request(null, "foreign"), response, chain);
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains("\"jsonrpc\":\"2.0\"").contains("different token");
        assertThat(chain.getRequest()).isNull();
    }

    @Test
    @DisplayName("Origin: same host and CORS write origins pass; others are refused")
    void origins() throws Exception {
        assertThat(filter.originAllowed("http://localhost:8080", request(null, null))).isTrue();
        assertThat(filter.originAllowed("https://app.example", request(null, null))).isTrue();
        assertThat(filter.originAllowed("https://evil.example", request(null, null))).isFalse();
        assertThat(filter.originAllowed("::not a uri::", request(null, null))).isFalse();

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request("https://evil.example", null), response, new MockFilterChain());
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains("Origin");
    }
}
