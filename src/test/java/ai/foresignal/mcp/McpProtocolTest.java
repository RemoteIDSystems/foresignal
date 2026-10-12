package ai.foresignal.mcp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.config.ForesignalVersion;
import ai.foresignal.mcp.tools.SituationReportPrompt;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.FailClosedRestrictionEnforcer;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.audit.AuditService;
import ai.foresignal.testsupport.mcp.McpTestTools;
import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.json.schema.jackson3.DefaultJsonSchemaValidator;
import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/** The protocol handlers (prompt 025): initialize, session checks, listings and calls by caller. */
class McpProtocolTest {

    private final JsonMapper json = JsonMapper.builder().build();
    private McpSessions sessions;
    private McpProtocol protocol;

    /** A resource that needs agent. */
    private final ForesignalMcpResource secret = new ForesignalMcpResource() {
        @Override
        public String uri() {
            return "foresignal://secret";
        }

        @Override
        public String name() {
            return "secret";
        }

        @Override
        public String description() {
            return "Agent only.";
        }

        @Override
        public Scope requiredScope() {
            return Scope.AGENT;
        }

        @Override
        public String read(ToolContext context) {
            return "{\"by\":\"" + context.actor() + "\"}";
        }
    };

    @BeforeEach
    void setUp() {
        sessions = new McpSessions(Clock.systemUTC());
        McpToolRegistry registry = new McpToolRegistry(List.of(McpTestTools.costBearing(), McpTestTools.image()),
                new DefaultJsonSchemaValidator(json), json, mock(AuditService.class),
                new FailClosedRestrictionEnforcer(), Clock.systemUTC());
        ForesignalVersion version = mock(ForesignalVersion.class);
        when(version.version()).thenReturn("1.2.3");
        protocol = new McpProtocol(registry, List.of(secret), List.of(new SituationReportPrompt()), sessions,
                new JacksonMcpJsonMapper(json), version);
    }

    private static McpCaller anonymous() {
        return new McpCaller(null, new ReadCaller(null, false, true), Set.of(), "127.0.0.1");
    }

    private static McpCaller agent() {
        AuthenticatedToken token = new AuthenticatedToken(UUID.randomUUID(), "agent", Scope.AGENT,
                TokenRestrictions.NONE, null, "p", false);
        return new McpCaller(token, new ReadCaller(Scope.AGENT, false, true), Set.of(), "127.0.0.1");
    }

    /** An exchange of a session bound to {@code owner}, carrying {@code caller}. */
    private McpAsyncServerExchange exchange(McpCaller owner, McpCaller caller) {
        String id = UUID.randomUUID().toString();
        try (McpSessions.CallerScope ignored = sessions.open(owner)) {
            sessions.bind(id, McpSessions.STREAMABLE);
        }
        McpTransportContext context = caller == null ? McpTransportContext.EMPTY
                : McpTransportContext.create(Map.of(McpCaller.CONTEXT_KEY, caller));
        return new McpAsyncServerExchange(id, null, null, null, context);
    }

    @SuppressWarnings("unchecked")
    private <T> T handle(String method, McpAsyncServerExchange exchange, Object params) {
        return (T) protocol.requestHandlers(McpSessions.STREAMABLE).get(method).handle(exchange, params).block();
    }

    @Test
    @DisplayName("initialize answers the client's protocol when supported, else the newest, with instructions")
    void initialize() {
        McpSchema.InitializeRequest request = new McpSchema.InitializeRequest("2025-06-18",
                new McpSchema.ClientCapabilities(null, null, null, null), new McpSchema.Implementation("c", "1"));
        McpSchema.InitializeResult result = protocol.initialize(request, List.of("2024-11-05", "2025-06-18")).block();
        assertThat(result.protocolVersion()).isEqualTo("2025-06-18");
        assertThat(result.serverInfo().name()).isEqualTo("foresignal");
        assertThat(result.serverInfo().version()).isEqualTo("1.2.3");
        assertThat(result.instructions()).isEqualTo(McpProtocol.INSTRUCTIONS);

        McpSchema.InitializeRequest future = new McpSchema.InitializeRequest("2099-01-01",
                new McpSchema.ClientCapabilities(null, null, null, null), new McpSchema.Implementation("c", "1"));
        assertThat(protocol.initialize(future, List.of("2024-11-05", "2025-06-18")).block().protocolVersion())
                .isEqualTo("2025-06-18");
    }

    @Test
    @DisplayName("tools/list depends on the caller")
    void toolsList() {
        McpSchema.ListToolsResult anon = handle(McpSchema.METHOD_TOOLS_LIST, exchange(anonymous(), anonymous()), null);
        assertThat(anon.tools()).extracting(McpSchema.Tool::name).containsExactly("test_image");
        McpCaller agent = agent();
        McpSchema.ListToolsResult full = handle(McpSchema.METHOD_TOOLS_LIST, exchange(agent, agent), null);
        assertThat(full.tools()).extracting(McpSchema.Tool::name).containsExactly("test_cost", "test_image");
    }

    @Test
    @DisplayName("a session used by another caller, or without a caller, is refused")
    void sessionMismatch() {
        assertThatThrownBy(() -> handle(McpSchema.METHOD_TOOLS_LIST, exchange(agent(), anonymous()), null))
                .isInstanceOf(McpError.class).hasMessageContaining("different token");
        assertThatThrownBy(() -> handle(McpSchema.METHOD_TOOLS_LIST, exchange(anonymous(), null), null))
                .isInstanceOf(McpError.class).hasMessageContaining("no authenticated caller");
    }

    @Test
    @DisplayName("tools/call runs the tool; an unknown tool is an invalid-params error")
    void toolsCall() {
        McpCaller caller = anonymous();
        McpSchema.CallToolResult result = handle(McpSchema.METHOD_TOOLS_CALL, exchange(caller, caller),
                Map.of("name", "test_image", "arguments", Map.of()));
        assertThat(result.isError()).isFalse();
        assertThat(result.content().getFirst()).isInstanceOf(McpSchema.ImageContent.class);
        assertThatThrownBy(() -> handle(McpSchema.METHOD_TOOLS_CALL, exchange(caller, caller),
                Map.of("name", "missing", "arguments", Map.of())))
                .isInstanceOf(McpError.class).hasMessageContaining("Unknown tool");
    }

    @Test
    @DisplayName("resources are listed and read by scope; an unknown URI is not found")
    void resources() {
        McpSchema.ListResourcesResult anon = handle(McpSchema.METHOD_RESOURCES_LIST, exchange(anonymous(), anonymous()), null);
        assertThat(anon.resources()).isEmpty();
        McpCaller agent = agent();
        McpSchema.ListResourcesResult listed = handle(McpSchema.METHOD_RESOURCES_LIST, exchange(agent, agent), null);
        assertThat(listed.resources()).extracting(McpSchema.Resource::uri).containsExactly("foresignal://secret");
        McpSchema.ReadResourceResult read = handle(McpSchema.METHOD_RESOURCES_READ, exchange(agent, agent),
                Map.of("uri", "foresignal://secret"));
        assertThat(((McpSchema.TextResourceContents) read.contents().getFirst()).text()).contains("token:agent");
        McpCaller anonymous = anonymous();
        assertThatThrownBy(() -> handle(McpSchema.METHOD_RESOURCES_READ, exchange(anonymous, anonymous),
                Map.of("uri", "foresignal://secret"))).isInstanceOf(McpError.class).hasMessageContaining("agent");
        assertThatThrownBy(() -> handle(McpSchema.METHOD_RESOURCES_READ, exchange(agent, agent),
                Map.of("uri", "foresignal://none"))).isInstanceOf(McpError.class).hasMessageContaining("not found");
        McpSchema.ListResourceTemplatesResult templates =
                handle(McpSchema.METHOD_RESOURCES_TEMPLATES_LIST, exchange(agent, agent), null);
        assertThat(templates.resourceTemplates()).isEmpty();
        assertThat(protocol.resources()).containsOnlyKeys("foresignal://secret");
    }

    @Test
    @DisplayName("prompts are listed and rendered; bad arguments are invalid params")
    void prompts() {
        McpCaller caller = anonymous();
        McpSchema.ListPromptsResult listed = handle(McpSchema.METHOD_PROMPT_LIST, exchange(caller, caller), null);
        assertThat(listed.prompts()).extracting(McpSchema.Prompt::name).containsExactly("situation-report");
        assertThat(listed.prompts().getFirst().arguments()).extracting(McpSchema.PromptArgument::name)
                .containsExactly("bbox", "since");
        McpSchema.GetPromptResult prompt = handle(McpSchema.METHOD_PROMPT_GET, exchange(caller, caller),
                Map.of("name", "situation-report", "arguments", Map.of("since", "2026-10-12T00:00:00Z")));
        assertThat(((McpSchema.TextContent) prompt.messages().getFirst().content()).text())
                .contains("since=\"2026-10-12T00:00:00Z\"").contains("everywhere");
        assertThatThrownBy(() -> handle(McpSchema.METHOD_PROMPT_GET, exchange(caller, caller),
                Map.of("name", "situation-report", "arguments", Map.of("bbox", "1,2,3"))))
                .isInstanceOf(McpError.class).hasMessageContaining("bbox");
        assertThatThrownBy(() -> handle(McpSchema.METHOD_PROMPT_GET, exchange(caller, caller),
                Map.of("name", "nope"))).isInstanceOf(McpError.class).hasMessageContaining("Unknown prompt");
        assertThat(protocol.prompts()).containsOnlyKeys("situation-report");
    }

    @Test
    @DisplayName("ping answers an empty object; notifications are accepted")
    void pingAndNotifications() {
        Object pong = handle(McpSchema.METHOD_PING, exchange(anonymous(), anonymous()), null);
        assertThat(pong).isEqualTo(Map.of());
        assertThat(protocol.notificationHandlers()).containsKeys(McpSchema.METHOD_NOTIFICATION_INITIALIZED,
                McpSchema.METHOD_NOTIFICATION_ROOTS_LIST_CHANGED);
        assertThat(protocol.capabilities().tools()).isNotNull();
    }
}
