package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpToolException;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.restrict.ReadScope;
import ai.foresignal.security.restrict.ReadScopes;
import ai.foresignal.stats.glance.GlanceService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code get_glance} (read, prompt 038): the same ≤ 2 KB status document as {@code GET /v1/glance}
 * ({@code status}, open alerts and the latest three, source tallies, the latest briefing, activity in
 * the last hour, {@code lastSeq}), or its 8-line text form. Anonymous callers get no counts unless the
 * operator allows it; {@code place} narrows the alerts and needs a token.
 */
@Component
@ai.foresignal.security.restrict.RestrictedRead
public class GetGlanceTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "place", string("A place id (from list_places) to narrow the alerts to; needs a token.", 1, 64),
            "format", JsonSchemas.enumeration("json (default) or text: at most 8 lines of 40 ASCII characters",
                    "json", "json", "text")));

    private final GlanceService glance;
    private final ObjectProvider<ReadScopes> scopes;
    private final JsonMapper json;

    /** Creates the tool. */
    public GetGlanceTool(GlanceService glance, ObjectProvider<ReadScopes> scopes, JsonMapper json) {
        this.glance = glance;
        this.scopes = scopes;
        this.json = json;
    }

    @Override
    public String name() {
        return "get_glance";
    }

    @Override
    public String title() {
        return "Glance";
    }

    @Override
    public String description() {
        return UNTRUSTED_NOTE + " A compact status snapshot (at most 2 KB): overall status (ok, degraded, failing),"
                + " open alerts per severity with the latest three, source health tallies, the latest briefing,"
                + " versions ingested in the last hour and the latest seq. Use it first to see whether anything needs"
                + " attention; format=text gives 8 short lines. place narrows the alerts to a named place.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The Glance document");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        String place = McpArgs.text(args, "place");
        String format = McpArgs.text(args, "format");
        if (format != null && !List.of("json", "text").contains(format)) {
            throw new McpToolException("format must be json or text.");
        }
        ReadCaller caller = context.readCaller();
        if (place != null && !place.isBlank()) {
            if (context.caller().anonymous()) {
                throw new McpToolException("place needs a token: places are never shown to anonymous callers.");
            }
            ReadScopes resolver = scopes.getIfAvailable();
            if (resolver == null) {
                throw new McpToolException("Places are not available on this server.");
            }
            ReadRestriction narrowed = resolver.narrow(caller.restriction(),
                    new ReadScope(List.of(place.strip()), null, null, null), true);
            caller = new ReadCaller(caller.scope(), caller.exposeObserver(), caller.openReads(), narrowed);
        }
        GlanceService.Request request = new GlanceService.Request(caller, context.excludedSources(),
                place == null || place.isBlank() ? null : place.strip());
        if ("text".equals(format)) {
            return ToolResult.text(glance.text(request));
        }
        return ToolResult.json(json.readTree(glance.json(request)));
    }
}
