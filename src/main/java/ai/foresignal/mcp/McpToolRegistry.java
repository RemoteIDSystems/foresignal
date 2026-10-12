package ai.foresignal.mcp;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.AccessRequirement;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.RestrictionEnforcer;
import ai.foresignal.security.Scope;
import ai.foresignal.security.audit.AuditRecord;
import ai.foresignal.security.audit.AuditService;
import io.modelcontextprotocol.json.schema.JsonSchemaValidator;
import io.modelcontextprotocol.spec.McpSchema;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Every {@link ForesignalMcpTool} bean, listed by caller and called with scope checks, argument
 * validation and an audit entry (prompt 025).
 *
 * <p><strong>Listing.</strong> Without a token (possible only while {@code openReads} is on) the
 * {@code read} tools are listed, except cost-bearing ones; with a token every tool its scope
 * reaches. <strong>Calling</strong> checks the same rules again and answers a call above the
 * caller's scope with a tool error that names the scope needed. Cost-bearing tools always need a
 * token (invariant SEC-5), mirroring {@code AccessPolicy}'s {@code alwaysAuthenticated} rules.
 * Tokens with data restrictions get the same refusal as on REST reads until prompt 035 enforces
 * them ({@link RestrictionEnforcer}).
 *
 * <p>Arguments are validated against the tool's input schema before the tool runs. Service
 * exceptions ({@link ForesignalException}: validation, not found, conflict, {@code RESYNC_REQUIRED})
 * become tool errors carrying the same message and field errors as the REST problem.
 *
 * <p><strong>Audit.</strong> Every call writes an {@code audit_event} with the tool name, the
 * argument <em>names</em> (never values), the token, the duration and the outcome
 * ({@code ok}, {@code error}, {@code denied}, {@code invalid}, {@code failed}, {@code unknown_tool}).
 */
@Component
public class McpToolRegistry {

    private static final Logger log = LoggerFactory.getLogger(McpToolRegistry.class);

    /** Tool names: lower snake case (MCP allows more; Foresignal keeps one style). */
    static final Pattern NAME = Pattern.compile("^[a-z][a-z0-9_]{0,63}$");

    /** The audit target type of a tool call. */
    public static final String AUDIT_TARGET = "mcp_tool";

    /** The audit method of a tool call. */
    public static final String AUDIT_METHOD = "MCP";

    private final Map<String, ForesignalMcpTool> tools;
    private final Map<String, McpSchema.Tool> definitions;
    private final JsonSchemaValidator validator;
    private final JsonMapper json;
    private final AuditService audit;
    private final RestrictionEnforcer restrictions;
    private final Clock clock;

    /**
     * Creates the registry.
     *
     * @throws IllegalStateException when two tools share a name, a name is not lower snake case, or an
     *     input schema is not valid JSON Schema 2020-12
     */
    public McpToolRegistry(List<ForesignalMcpTool> tools, JsonSchemaValidator validator, JsonMapper json,
            AuditService audit, RestrictionEnforcer restrictions, Clock clock) {
        Map<String, ForesignalMcpTool> byName = new TreeMap<>();
        for (ForesignalMcpTool tool : tools) {
            if (!NAME.matcher(tool.name()).matches()) {
                throw new IllegalStateException("MCP tool name '" + tool.name() + "' is not lower snake case");
            }
            if (byName.put(tool.name(), tool) != null) {
                throw new IllegalStateException("Two MCP tools are named '" + tool.name() + "'");
            }
            JsonSchemaValidator.ValidationResponse schema = validator.validateSchema(tool.inputSchema());
            if (!schema.valid()) {
                throw new IllegalStateException("MCP tool " + tool.name() + ": " + schema.errorMessage());
            }
        }
        this.tools = Map.copyOf(byName);
        Map<String, McpSchema.Tool> defs = new TreeMap<>();
        byName.forEach((name, tool) -> defs.put(name, definition(tool)));
        this.definitions = Map.copyOf(defs);
        this.validator = validator;
        this.json = json;
        this.audit = audit;
        this.restrictions = restrictions;
        this.clock = clock;
    }

    /** Every registered tool, by name. */
    public List<ForesignalMcpTool> tools() {
        return tools.values().stream().sorted(Comparator.comparing(ForesignalMcpTool::name)).toList();
    }

    /** The number of registered tools. */
    public int size() {
        return tools.size();
    }

    /** The tool named {@code name}. */
    public Optional<ForesignalMcpTool> tool(String name) {
        return Optional.ofNullable(name == null ? null : tools.get(name));
    }

    /** The MCP definition of a tool: name, description, schemas and annotations. */
    public static McpSchema.Tool definition(ForesignalMcpTool tool) {
        McpSchema.ToolAnnotations annotations = McpSchema.ToolAnnotations.builder()
                .title(tool.title())
                .readOnlyHint(tool.readOnlyHint())
                .destructiveHint(tool.destructiveHint())
                .idempotentHint(tool.idempotentHint())
                .openWorldHint(false)
                .build();
        McpSchema.Tool.Builder builder = McpSchema.Tool.builder()
                .name(tool.name())
                .title(tool.title())
                .description(tool.description())
                .inputSchema(tool.inputSchema())
                .annotations(annotations);
        if (tool.outputSchema() != null) {
            builder.outputSchema(tool.outputSchema());
        }
        return builder.build();
    }

    /** The definitions {@code caller} may see, sorted by name. */
    public List<McpSchema.Tool> visible(McpCaller caller) {
        List<McpSchema.Tool> out = new ArrayList<>();
        for (ForesignalMcpTool tool : tools()) {
            if (listed(tool, caller)) {
                out.add(definitions.get(tool.name()));
            }
        }
        return out;
    }

    /** True when {@code tool} appears in {@code caller}'s tool list. */
    static boolean listed(ForesignalMcpTool tool, McpCaller caller) {
        if (caller.anonymous()) {
            return caller.openReads() && tool.requiredScope() == Scope.READ && !tool.costBearing();
        }
        return caller.scope().includes(tool.requiredScope());
    }

    /**
     * Why {@code caller} may not call {@code tool}, or empty when it may.
     *
     * <p>The messages are written for the agent: what is needed and how to get it.
     */
    public Optional<String> denial(ForesignalMcpTool tool, McpCaller caller) {
        Scope required = tool.requiredScope();
        if (caller.anonymous()) {
            if (required != Scope.READ) {
                return Optional.of("The tool " + tool.name() + " needs a token with the " + required.wireValue()
                        + " scope; this request has no token. Connect with an Authorization: Bearer header.");
            }
            if (tool.costBearing()) {
                return Optional.of("The tool " + tool.name() + " spends a budget and always requires a token, even"
                        + " while open reads are on. Connect with an Authorization: Bearer header.");
            }
            if (!caller.openReads()) {
                return Optional.of("The tool " + tool.name() + " requires a token while open reads are off.");
            }
            return Optional.empty();
        }
        if (!caller.scope().includes(required)) {
            return Optional.of("The tool " + tool.name() + " needs the " + required.wireValue()
                    + " scope; this token has " + caller.scope().wireValue() + ".");
        }
        AuthenticatedToken token = caller.token();
        if (required == Scope.READ && token.restrictions().hasDataRestrictions()) {
            return restrictions.check(HttpMethod.GET, "/v1/records", AccessRequirement.READ, token)
                    .map(RestrictionEnforcer.Refusal::detail);
        }
        return Optional.empty();
    }

    /**
     * Calls a tool on behalf of {@code context}'s caller.
     *
     * @param name the tool name
     * @param arguments the call's arguments (null for none)
     * @param context the caller and session
     * @return the MCP result; a tool error for refusals, invalid arguments and service errors
     * @throws UnknownToolException when no tool has that name (a protocol error)
     */
    public McpSchema.CallToolResult call(String name, Map<String, Object> arguments, ToolContext context) {
        long started = System.nanoTime();
        List<String> argumentNames = arguments == null ? List.of() : arguments.keySet().stream().sorted().toList();
        ForesignalMcpTool tool = tools.get(name);
        String outcome = "ok";
        try {
            if (tool == null) {
                outcome = "unknown_tool";
                throw new UnknownToolException(name);
            }
            Optional<String> denied = denial(tool, context.caller());
            if (denied.isPresent()) {
                outcome = "denied";
                return toResult(tool, ToolResult.error(denied.get()));
            }
            Map<String, Object> args = arguments == null ? Map.of() : arguments;
            JsonSchemaValidator.ValidationResponse validation = validator.validate(tool.inputSchema(), args);
            if (!validation.valid()) {
                outcome = "invalid";
                return toResult(tool, ToolResult.error("Invalid arguments for " + tool.name() + ": "
                        + validation.errorMessage()));
            }
            JsonNode tree = json.valueToTree(args);
            ToolResult result = tool.call(context, tree);
            outcome = result.error() ? "error" : "ok";
            return toResult(tool, result);
        } catch (McpToolException e) {
            outcome = "error";
            return toResult(tool, ToolResult.error(e.getMessage()));
        } catch (ForesignalException e) {
            outcome = "error";
            return toResult(tool, ToolResult.error(describe(e)));
        } catch (UnknownToolException e) {
            throw e;
        } catch (RuntimeException e) {
            outcome = "failed";
            log.warn("MCP tool {} failed", name, e);
            return toResult(tool, ToolResult.error("The tool " + name + " failed unexpectedly ("
                    + e.getClass().getSimpleName() + "). The failure is logged on the server."));
        } finally {
            audit(name, argumentNames, context, outcome, (System.nanoTime() - started) / 1_000_000L);
        }
    }

    /** The agent-facing text of a service exception: its detail, code and field errors. */
    static String describe(ForesignalException e) {
        StringBuilder text = new StringBuilder();
        text.append(e.getMessage() == null ? e.getStatus().getReasonPhrase() : e.getMessage());
        if (e.getCode() != null) {
            text.append(" (").append(e.getCode()).append(')');
        }
        if (e instanceof ValidationException validation && validation.getErrors() != null
                && !validation.getErrors().isEmpty()) {
            text.append(':');
            for (FieldError error : validation.getErrors()) {
                text.append(' ').append(error.field()).append(": ").append(error.message()).append(';');
            }
            text.setLength(text.length() - 1);
        }
        return text.toString();
    }

    /** The SDK result of a {@link ToolResult}; structured content only when the tool declares a schema. */
    McpSchema.CallToolResult toResult(ForesignalMcpTool tool, ToolResult result) {
        List<McpSchema.Content> content = new ArrayList<>();
        for (ToolResult.Content block : result.content()) {
            switch (block) {
                case ToolResult.Text text -> content.add(new McpSchema.TextContent(text.text()));
                case ToolResult.Image image -> content.add(new McpSchema.ImageContent(null, image.data(),
                        image.mimeType()));
            }
        }
        McpSchema.CallToolResult.Builder builder = McpSchema.CallToolResult.builder()
                .content(content)
                .isError(result.error());
        if (!result.error() && result.structured() != null && tool != null && tool.outputSchema() != null) {
            builder.structuredContent(json.convertValue(result.structured(), Map.class));
        }
        return builder.build();
    }

    private void audit(String name, List<String> argumentNames, ToolContext context, String outcome, long millis) {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("tool", name);
        detail.put("arguments", argumentNames);
        detail.put("durationMs", millis);
        detail.put("outcome", outcome);
        detail.put("transport", context.transport());
        if (context.sessionId() != null) {
            detail.put("sessionId", context.sessionId());
        }
        AuthenticatedToken token = context.caller().token();
        try {
            audit.record(new AuditRecord(
                    clock.instant(),
                    AuditRecord.TOKEN,
                    token == null ? null : token.id(),
                    token == null ? null : token.name(),
                    token == null ? null : token.principalId(),
                    token == null ? null : token.scope().wireValue(),
                    AUDIT_METHOD,
                    McpSessions.LEGACY_SSE.equals(context.transport()) ? "/mcp/message" : "/mcp",
                    null,
                    null,
                    AUDIT_TARGET,
                    name,
                    context.caller().remoteIp(),
                    detail));
        } catch (RuntimeException e) {
            log.warn("Could not audit MCP tool call {}", name, e);
        }
    }

    /** A call to a tool that does not exist: a JSON-RPC error, not a tool error. */
    public static final class UnknownToolException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        /** Creates the exception for {@code name}. */
        public UnknownToolException(String name) {
            super("Unknown tool: " + name);
        }
    }
}
