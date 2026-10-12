package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.detail;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.api.mapper.RecordMapper;
import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.api.support.GeometryOptions;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.McpToolException;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.RecordRow;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/** {@code get_record} (read): one record's current state, head plus live, as {@code getRecord}. */
@Component
public class GetRecordTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "source", string("The record's source id, the part before the colon in an item id.", 1, 200),
            "externalId", string("The record's id within its source, the part after the colon.", 1, 500),
            "detail", detail()), "source", "externalId");

    private final RecordQueryService queries;
    private final RecordItems items;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public GetRecordTool(RecordQueryService queries, RecordItems items, McpOutputShaper shaper) {
        this.queries = queries;
        this.items = items;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "get_record";
    }

    @Override
    public String title() {
        return "Get one record";
    }

    @Override
    public String description() {
        return UNTRUSTED_NOTE + " Returns one record's current state by source and externalId, including expired"
                + " and deleted ones (tombstone). Use detail=long for the full payload. seq and version identify the"
                + " persisted version to cite; live=true means the live fields are newer than that version.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The record");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        Detail detail = Detail.of(args);
        String source = McpArgs.requiredText(args, "source");
        String externalId = McpArgs.requiredText(args, "externalId");
        items.visibleSources(List.of(source), context.excludedSources());
        RecordRow row = queries.get(source, externalId, false, FieldSelection.ALL, GeometryOptions.DEFAULT,
                        context.excludedSources())
                .orElseThrow(() -> new McpToolException("No record " + source + ":" + externalId
                        + ". Check the id with query_records."));
        return ToolResult.json(shaper.object(items.item(row, RecordMapper.Shape.CURRENT, detail), detail));
    }
}
