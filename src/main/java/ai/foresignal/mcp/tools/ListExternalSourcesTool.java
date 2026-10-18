package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;

import ai.foresignal.inbound.edp.binding.ExternalSourceSummaries;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code list_external_sources} (read; prompt 138): the registered External Data Protocol providers with their state,
 * access route, originator and streams (mode, item type, kind, item counts and bindings). Private providers the caller
 * may not see are left out; manifests, cursors and credentials are never returned.
 */
@Component
@RestrictedRead("provider registry metadata; no record is returned and hidden private providers are left out")
public class ListExternalSourcesTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "state", JsonSchemas.enumeration("Only providers in this state", null, "pending", "active", "suspended")));

    private final ExternalSourceSummaries summaries;
    private final JsonMapper json;

    /** Creates the tool. */
    public ListExternalSourcesTool(ExternalSourceSummaries summaries, JsonMapper json) {
        this.summaries = summaries;
        this.json = json;
    }

    @Override
    public String name() {
        return "list_external_sources";
    }

    @Override
    public String title() {
        return "List external data providers";
    }

    @Override
    public String description() {
        return "Lists the External Data Protocol providers registered on this instance (separate projects that push or"
                + " serve data: web maps, scrapers, channels, markets, niche feeds) with their state, access route"
                + " (official, unofficial-api, scrape, browser) and fragility, declared originator, and streams. A"
                + " stream does nothing analytic until it is bound (bindings > 0); provider data is reporting whose"
                + " independence is counted by originator, never by provider.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("ExternalSourceSummaryList");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public boolean idempotentHint() {
        return true;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        try {
            String state = ListNewsStoriesTool.text(args, "state");
            if (state != null && !List.of("pending", "active", "suspended").contains(state)) {
                return ToolResult.error("state must be pending, active or suspended");
            }
            List<Map<String, Object>> items = summaries.list(state, context.excludedSources());
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("items", items);
            out.put("total", items.size());
            return ToolResult.json(json.valueToTree(out));
        } catch (RuntimeException e) {
            return ToolResult.error(e.getMessage());
        }
    }
}
