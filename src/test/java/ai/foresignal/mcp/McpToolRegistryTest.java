package ai.foresignal.mcp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.FailClosedRestrictionEnforcer;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.audit.AuditRecord;
import ai.foresignal.security.audit.AuditService;
import ai.foresignal.testsupport.mcp.McpTestTools;
import io.modelcontextprotocol.json.schema.jackson3.DefaultJsonSchemaValidator;
import io.modelcontextprotocol.spec.McpSchema;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** The tool registry (prompt 025): listing by caller, scope checks, validation, results and audit. */
class McpToolRegistryTest {

    private final JsonMapper json = JsonMapper.builder().build();
    private AuditService audit;
    private McpToolRegistry registry;

    /** {@code test_echo}: read, echoes {@code text}, throws for {@code text = "invalid"}. */
    private final ForesignalMcpTool echo = new ForesignalMcpTool() {
        @Override
        public String name() {
            return "test_echo";
        }

        @Override
        public String description() {
            return "Echoes text back; a test tool for the registry.";
        }

        @Override
        public Map<String, Object> inputSchema() {
            return JsonSchemas.object(JsonSchemas.props(
                    "text", JsonSchemas.string("Text.", 1, 50),
                    "count", JsonSchemas.integer("A count.", 1, 3, null)), "text");
        }

        @Override
        public Map<String, Object> outputSchema() {
            return JsonSchemas.objectOutput("{echo}");
        }

        @Override
        public Scope requiredScope() {
            return Scope.READ;
        }

        @Override
        public ToolResult call(ToolContext context, JsonNode arguments) {
            String text = arguments.get("text").asString();
            if (text.equals("invalid")) {
                throw new ValidationException("The request is not valid.", List.of(new FieldError("text", "is bad")));
            }
            if (text.equals("crash")) {
                throw new IllegalStateException("boom");
            }
            if (text.equals("refuse")) {
                throw new McpToolException("Refused for the test.");
            }
            return ToolResult.json(json.createObjectNode().put("echo", text).put("actor", context.actor()));
        }
    };

    /** {@code test_write}: agent. */
    private final ForesignalMcpTool write = new ForesignalMcpTool() {
        @Override
        public String name() {
            return "test_write";
        }

        @Override
        public String description() {
            return "Writes nothing; a test tool that needs the agent scope.";
        }

        @Override
        public Map<String, Object> inputSchema() {
            return JsonSchemas.object(JsonSchemas.props());
        }

        @Override
        public Scope requiredScope() {
            return Scope.AGENT;
        }

        @Override
        public ToolResult call(ToolContext context, JsonNode arguments) {
            return ToolResult.text("written by " + context.actor());
        }
    };

    @BeforeEach
    void setUp() {
        audit = mock(AuditService.class);
        registry = new McpToolRegistry(List.of(echo, write, McpTestTools.costBearing(), McpTestTools.image()),
                new DefaultJsonSchemaValidator(json), json, audit, new FailClosedRestrictionEnforcer(),
                Clock.fixed(Instant.parse("2026-10-12T12:00:00Z"), ZoneOffset.UTC));
    }

    private static McpCaller anonymous(boolean openReads) {
        return new McpCaller(null, new ReadCaller(null, false, openReads), Set.of(), "127.0.0.1");
    }

    private static McpCaller token(Scope scope, TokenRestrictions restrictions) {
        AuthenticatedToken token = new AuthenticatedToken(UUID.randomUUID(), "bot", scope, restrictions, null,
                "principal-1", false);
        return new McpCaller(token, new ReadCaller(scope, false, true), Set.of(), "10.0.0.2");
    }

    private static List<String> names(List<McpSchema.Tool> tools) {
        return tools.stream().map(McpSchema.Tool::name).toList();
    }

    private McpSchema.CallToolResult call(String name, Map<String, Object> args, McpCaller caller) {
        return registry.call(name, args, new ToolContext(caller, "session-1", McpSessions.STREAMABLE));
    }

    private static String text(McpSchema.CallToolResult result) {
        return ((McpSchema.TextContent) result.content().getFirst()).text();
    }

    @Test
    @DisplayName("tools are listed by caller: anonymous read tools without cost; read adds cost-bearing; agent all")
    void listing() {
        assertThat(names(registry.visible(anonymous(true)))).containsExactly("test_echo", "test_image");
        assertThat(names(registry.visible(anonymous(false)))).isEmpty();
        assertThat(names(registry.visible(token(Scope.READ, TokenRestrictions.NONE))))
                .containsExactly("test_cost", "test_echo", "test_image");
        assertThat(names(registry.visible(token(Scope.AGENT, TokenRestrictions.NONE))))
                .containsExactly("test_cost", "test_echo", "test_image", "test_write");
        assertThat(registry.size()).isEqualTo(4);
        assertThat(registry.tool("test_echo")).contains(echo);
    }

    @Test
    @DisplayName("a call returns JSON text plus structuredContent when the tool declares an output schema")
    void jsonResult() {
        McpSchema.CallToolResult result = call("test_echo", Map.of("text", "hi"), token(Scope.READ, TokenRestrictions.NONE));
        assertThat(result.isError()).isFalse();
        assertThat(text(result)).contains("\"echo\":\"hi\"").contains("token:bot");
        assertThat(result.structuredContent()).isEqualTo(Map.of("echo", "hi", "actor", "token:bot"));
    }

    @Test
    @DisplayName("an image result carries the image block and its caption; no output schema, no structuredContent")
    void imageResult() {
        McpSchema.CallToolResult result = call("test_image", Map.of(), anonymous(true));
        assertThat(result.isError()).isFalse();
        assertThat(result.content()).hasSize(2);
        McpSchema.ImageContent image = (McpSchema.ImageContent) result.content().get(0);
        assertThat(image.mimeType()).isEqualTo("image/png");
        assertThat(image.data()).isEqualTo(McpTestTools.PIXEL_PNG);
        assertThat(((McpSchema.TextContent) result.content().get(1)).text()).isEqualTo("A transparent pixel.");
        assertThat(result.structuredContent()).isNull();
    }

    @Test
    @DisplayName("calls above the caller's scope are tool errors naming the scope")
    void scope() {
        McpSchema.CallToolResult read = call("test_write", Map.of(), token(Scope.READ, TokenRestrictions.NONE));
        assertThat(read.isError()).isTrue();
        assertThat(text(read)).contains("needs the agent scope").contains("this token has read");

        McpSchema.CallToolResult anon = call("test_write", Map.of(), anonymous(true));
        assertThat(anon.isError()).isTrue();
        assertThat(text(anon)).contains("no token");

        McpSchema.CallToolResult agent = call("test_write", Map.of(), token(Scope.AGENT, TokenRestrictions.NONE));
        assertThat(agent.isError()).isFalse();
        assertThat(text(agent)).isEqualTo("written by token:bot");
    }

    @Test
    @DisplayName("cost-bearing tools need a token even under openReads (SEC-5)")
    void costBearing() {
        McpSchema.CallToolResult anon = call("test_cost", Map.of("text", "x"), anonymous(true));
        assertThat(anon.isError()).isTrue();
        assertThat(text(anon)).contains("always requires a token");
        assertThat(call("test_cost", Map.of("text", "x"), token(Scope.READ, TokenRestrictions.NONE)).isError()).isFalse();
    }

    @Test
    @DisplayName("anonymous calls are refused while openReads is off")
    void openReadsOff() {
        McpSchema.CallToolResult result = call("test_echo", Map.of("text", "x"), anonymous(false));
        assertThat(result.isError()).isTrue();
        assertThat(text(result)).contains("open reads are off");
    }

    @Test
    @DisplayName("tokens with data restrictions are refused on read tools until 035 enforces them")
    void dataRestrictions() {
        TokenRestrictions restricted = new TokenRestrictions(List.of("usgs"), null, null, null);
        McpSchema.CallToolResult result = call("test_echo", Map.of("text", "x"), token(Scope.READ, restricted));
        assertThat(result.isError()).isTrue();
        assertThat(text(result)).contains("restrictions");
    }

    @Test
    @DisplayName("arguments are validated against the input schema before the tool runs")
    void validation() {
        McpSchema.CallToolResult missing = call("test_echo", Map.of(), anonymous(true));
        assertThat(missing.isError()).isTrue();
        assertThat(text(missing)).startsWith("Invalid arguments for test_echo");

        assertThat(call("test_echo", Map.of("text", "x", "extra", 1), anonymous(true)).isError())
                .as("additionalProperties false").isTrue();
        assertThat(call("test_echo", Map.of("text", "x", "count", 9), anonymous(true)).isError())
                .as("maximum").isTrue();
        assertThat(call("test_echo", Map.of("text", 5), anonymous(true)).isError()).as("type").isTrue();
    }

    @Test
    @DisplayName("service exceptions become tool errors with their code and field errors")
    void serviceErrors() {
        McpSchema.CallToolResult invalid = call("test_echo", Map.of("text", "invalid"), anonymous(true));
        assertThat(invalid.isError()).isTrue();
        assertThat(text(invalid)).contains("The request is not valid.").contains("VALIDATION_FAILED")
                .contains("text: is bad");

        McpSchema.CallToolResult refused = call("test_echo", Map.of("text", "refuse"), anonymous(true));
        assertThat(text(refused)).isEqualTo("Refused for the test.");

        McpSchema.CallToolResult crashed = call("test_echo", Map.of("text", "crash"), anonymous(true));
        assertThat(crashed.isError()).isTrue();
        assertThat(text(crashed)).contains("failed unexpectedly").contains("IllegalStateException")
                .doesNotContain("boom");
    }

    @Test
    @DisplayName("an unknown tool is a protocol error, and is audited")
    void unknownTool() {
        assertThatThrownBy(() -> call("nope", Map.of(), anonymous(true)))
                .isInstanceOf(McpToolRegistry.UnknownToolException.class);
        ArgumentCaptor<AuditRecord> captor = ArgumentCaptor.forClass(AuditRecord.class);
        verify(audit).record(captor.capture());
        assertThat(captor.getValue().detail()).containsEntry("outcome", "unknown_tool");
    }

    @Test
    @DisplayName("every call is audited with the tool, argument names only, token, duration and outcome")
    void audit() {
        call("test_echo", Map.of("text", "secret-value", "count", 2), token(Scope.READ, TokenRestrictions.NONE));
        call("test_write", Map.of(), token(Scope.READ, TokenRestrictions.NONE));

        ArgumentCaptor<AuditRecord> captor = ArgumentCaptor.forClass(AuditRecord.class);
        verify(audit, atLeastOnce()).record(captor.capture());
        List<AuditRecord> records = captor.getAllValues();
        assertThat(records).hasSize(2);
        AuditRecord ok = records.get(0);
        assertThat(ok.actorKind()).isEqualTo(AuditRecord.TOKEN);
        assertThat(ok.tokenName()).isEqualTo("bot");
        assertThat(ok.scope()).isEqualTo("read");
        assertThat(ok.method()).isEqualTo("MCP");
        assertThat(ok.path()).isEqualTo("/mcp");
        assertThat(ok.targetType()).isEqualTo("mcp_tool");
        assertThat(ok.targetId()).isEqualTo("test_echo");
        assertThat(ok.remoteIp()).isEqualTo("10.0.0.2");
        assertThat(ok.detail()).containsEntry("arguments", List.of("count", "text"))
                .containsEntry("outcome", "ok")
                .containsEntry("transport", "streamable")
                .containsKey("durationMs");
        assertThat(ok.detail().toString()).doesNotContain("secret-value");
        assertThat(records.get(1).detail()).containsEntry("outcome", "denied");
    }

    @Test
    @DisplayName("duplicate or badly named tools are refused at startup")
    void names() {
        assertThatThrownBy(() -> new McpToolRegistry(List.of(echo, echo), new DefaultJsonSchemaValidator(json), json,
                audit, new FailClosedRestrictionEnforcer(), Clock.systemUTC()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("test_echo");
        ForesignalMcpTool bad = new ForesignalMcpTool() {
            @Override
            public String name() {
                return "Bad-Name";
            }

            @Override
            public String description() {
                return "x";
            }

            @Override
            public Map<String, Object> inputSchema() {
                return JsonSchemas.object(JsonSchemas.props());
            }

            @Override
            public Scope requiredScope() {
                return Scope.READ;
            }

            @Override
            public ToolResult call(ToolContext context, JsonNode arguments) {
                return ToolResult.text("");
            }
        };
        assertThatThrownBy(() -> new McpToolRegistry(List.of(bad), new DefaultJsonSchemaValidator(json), json, audit,
                new FailClosedRestrictionEnforcer(), Clock.systemUTC()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("snake case");
    }

    @Test
    @DisplayName("definitions carry annotations derived from the scope")
    void definitions() {
        McpSchema.Tool readDef = McpToolRegistry.definition(echo);
        assertThat(readDef.annotations().readOnlyHint()).isTrue();
        assertThat(readDef.outputSchema()).isNotNull();
        McpSchema.Tool writeDef = McpToolRegistry.definition(write);
        assertThat(writeDef.annotations().readOnlyHint()).isFalse();
        assertThat(writeDef.annotations().idempotentHint()).isFalse();
        assertThat(writeDef.outputSchema()).isNull();
    }
}
