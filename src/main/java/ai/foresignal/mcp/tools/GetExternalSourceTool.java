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
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code get_external_source} (read; prompt 138): one External Data Protocol provider with its streams and their
 * suggested bindings. A private provider the caller may not see is reported as unknown.
 */
@Component
@RestrictedRead("provider registry metadata; no record is returned and a hidden private provider is unknown")
public class GetExternalSourceTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "sourceId", JsonSchemas.string("The provider's source id, e.g. ext-hormuz-transits", 1, 64)), "sourceId");

    private final ExternalSourceSummaries summaries;
    private final JsonMapper json;

    /** Creates the tool. */
    public GetExternalSourceTool(ExternalSourceSummaries summaries, JsonMapper json) {
        this.summaries = summaries;
        this.json = json;
    }

    @Override
    public String name() {
        return "get_external_source";
    }

    @Override
    public String title() {
        return "Get an external data provider";
    }

    @Override
    public String description() {
        return "Returns one External Data Protocol provider: state, access route and fragility, declared originator"
                + " and what it aggregates, license redistribution, and each stream with its item counts, bindings and"
                + " the bindings the provider suggests (series, canonical-family, social, news, evidence, indicator,"
                + " forecast, place, situation, chronicle).";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("ExternalSourceSummary");
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
        String sourceId = ListNewsStoriesTool.text(args, "sourceId");
        if (sourceId == null) {
            return ToolResult.error("sourceId is required");
        }
        Map<String, Object> summary = summaries.get(sourceId, context.excludedSources());
        if (summary == null) {
            return ToolResult.error("There is no external source '" + sourceId + "'.");
        }
        return ToolResult.json(json.valueToTree(summary));
    }
}
