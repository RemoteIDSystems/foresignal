package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;

import ai.foresignal.attention.AttentionTopicService;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code list_attention_topics} (read; prompt 117): the attention topics with their GDELT query, Wikipedia articles,
 * keywords, regions, themes and whether they are enabled. Topics are configuration, never records.
 */
@Component
public class ListAttentionTopicsTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "enabled", JsonSchemas.bool("Only enabled (true) or disabled (false) topics; omit for all", null)));

    private final AttentionTopicService topics;
    private final JsonMapper json;

    /** Creates the tool. */
    public ListAttentionTopicsTool(AttentionTopicService topics, JsonMapper json) {
        this.topics = topics;
        this.json = json;
    }

    @Override
    public String name() {
        return "list_attention_topics";
    }

    @Override
    public String title() {
        return "List attention topics";
    }

    @Override
    public String description() {
        return "Lists the operator's attention topics: id, name, GDELT query, Wikipedia articles, keywords, region"
                + " codes, themes and enabled. Enabled topics drive the gdelt-timelines and wikimedia-pageviews"
                + " series (gdelt.{topicId}.vol, gdelt.{topicId}.tone, wiki.{project}.{article}); read them with"
                + " get_series.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.listOutput("Attention topic items");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public boolean readOnlyHint() {
        return true;
    }

    @Override
    public boolean idempotentHint() {
        return true;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        Boolean enabled = args != null && args.has("enabled") && args.get("enabled").isBoolean()
                ? args.get("enabled").asBoolean() : null;
        List<Map<String, Object>> items = topics.list(enabled, null, AttentionTopicService.MAX_LIMIT).items()
                .stream().map(AttentionTopicService::view).toList();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("total", items.size());
        out.put("truncated", items.size() >= AttentionTopicService.MAX_LIMIT);
        return ToolResult.json(json.valueToTree(out));
    }
}
